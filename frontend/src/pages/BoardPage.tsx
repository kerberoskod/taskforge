import { useState, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { DragDropContext, Droppable, Draggable, DropResult } from '@hello-pangea/dnd';
import { getProject } from '../api/projects';
import { getTasks, createTask, updateTask, updateTaskPosition, Task, TaskStatus } from '../api/tasks';
import { getComments, createComment, Comment } from '../api/comments';
import { useWebSocket } from '../hooks/useWebSocket';
import Sidebar from '../components/layout/Sidebar';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import Modal from '../components/ui/Modal';

const COLUMNS: { key: TaskStatus; label: string; color: string }[] = [
  { key: 'TODO', label: 'To Do', color: 'bg-gray-100' },
  { key: 'IN_PROGRESS', label: 'In Progress', color: 'bg-blue-50' },
  { key: 'REVIEW', label: 'Review', color: 'bg-yellow-50' },
  { key: 'DONE', label: 'Done', color: 'bg-green-50' },
];

export default function BoardPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const queryClient = useQueryClient();

  const { data: project } = useQuery({
    queryKey: ['project', projectId],
    queryFn: () => getProject(projectId!),
    enabled: !!projectId,
  });

  const { data: tasks = [] } = useQuery({
    queryKey: ['tasks', projectId],
    queryFn: () => getTasks(projectId!),
    enabled: !!projectId,
  });

  const { data: projects = [] } = useQuery({
    queryKey: ['projects'],
    queryFn: () => import('../api/projects').then((m) => m.getProjects()),
  });

  const [showCreate, setShowCreate] = useState(false);
  const [createStatus, setCreateStatus] = useState<TaskStatus>('TODO');
  const [newTitle, setNewTitle] = useState('');
  const [newDesc, setNewDesc] = useState('');

  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentText, setCommentText] = useState('');

  useWebSocket(projectId, (msg) => {
    queryClient.invalidateQueries({ queryKey: ['tasks', projectId] });
  });

  const createMut = useMutation({
    mutationFn: () => createTask(projectId!, { title: newTitle, description: newDesc, status: createStatus }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', projectId] });
      setShowCreate(false);
      setNewTitle('');
      setNewDesc('');
    },
  });

  const updatePosMut = useMutation({
    mutationFn: (data: { taskId: string; status: string; position: number }) =>
      updateTaskPosition(projectId!, data),
  });

  const onDragEnd = useCallback(
    (result: DropResult) => {
      if (!result.destination || !projectId) return;

      const { draggableId, destination } = result;
      const newStatus = destination.droppableId as TaskStatus;

      const columnTasks = tasks
        .filter((t) => t.status === newStatus)
        .sort((a, b) => a.position - b.position);

      const newPosition = destination.index;

      updatePosMut.mutate({
        taskId: draggableId,
        status: newStatus,
        position: newPosition,
      });

      queryClient.setQueryData(['tasks', projectId], (old: Task[] | undefined) => {
        if (!old) return old;
        return old.map((t) =>
          t.id === draggableId ? { ...t, status: newStatus, position: newPosition } : t
        );
      });
    },
    [tasks, projectId, queryClient, updatePosMut]
  );

  const openTaskDetail = async (task: Task) => {
    setSelectedTask(task);
    try {
      const comments = await getComments(task.id);
      setComments(comments);
    } catch {
      setComments([]);
    }
  };

  const addComment = async () => {
    if (!commentText.trim() || !selectedTask) return;
    try {
      const comment = await createComment(selectedTask.id, commentText);
      setComments((prev) => [...prev, comment]);
      setCommentText('');
    } catch {}
  };

  return (
    <div className="flex h-screen bg-white">
      <Sidebar projects={projects} currentProjectId={projectId} />

      <main className="flex-1 flex flex-col overflow-hidden">
        <div className="p-4 border-b border-apple-border flex items-center justify-between">
          <h1 className="text-xl font-bold text-apple-dark">{project?.name || 'Board'}</h1>
          <div className="flex gap-2">
            <Button onClick={() => setShowCreate(true)}>Add Task</Button>
          </div>
        </div>

        <DragDropContext onDragEnd={onDragEnd}>
          <div className="flex-1 flex gap-4 p-4 overflow-x-auto">
            {COLUMNS.map((col) => {
              const columnTasks = tasks
                .filter((t) => t.status === col.key)
                .sort((a, b) => a.position - b.position);

              return (
                <div key={col.key} className="flex-1 min-w-[250px] flex flex-col">
                  <div className={`rounded-t-xl px-3 py-2 ${col.color}`}>
                    <h3 className="font-semibold text-sm text-apple-dark">
                      {col.label}
                      <span className="ml-2 text-apple-gray font-normal">
                        {columnTasks.length}
                      </span>
                    </h3>
                  </div>
                  <Droppable droppableId={col.key}>
                    {(provided, snapshot) => (
                      <div
                        ref={provided.innerRef}
                        {...provided.droppableProps}
                        className={`flex-1 p-2 rounded-b-xl ${
                          snapshot.isDraggingOver ? 'bg-apple-light' : col.color
                        } min-h-[200px] transition-colors`}
                      >
                        {columnTasks.map((task, index) => (
                          <Draggable key={task.id} draggableId={task.id} index={index}>
                            {(provided, snapshot) => (
                              <div
                                ref={provided.innerRef}
                                {...provided.draggableProps}
                                {...provided.dragHandleProps}
                                onClick={() => openTaskDetail(task)}
                                className={`bg-white rounded-lg p-3 mb-2 shadow-sm border border-apple-border
                                  cursor-pointer hover:shadow-md transition-shadow
                                  ${snapshot.isDragging ? 'shadow-lg rotate-2' : ''}`}
                              >
                                <p className="text-sm font-medium text-apple-dark">{task.title}</p>
                                {task.description && (
                                  <p className="text-xs text-apple-gray mt-1 line-clamp-2">
                                    {task.description}
                                  </p>
                                )}
                              </div>
                            )}
                          </Draggable>
                        ))}
                        {provided.placeholder}
                      </div>
                    )}
                  </Droppable>
                </div>
              );
            })}
          </div>
        </DragDropContext>
      </main>

      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="Add Task">
        <div className="flex flex-col gap-4">
          <Input
            label="Title"
            value={newTitle}
            onChange={(e) => setNewTitle(e.target.value)}
            required
          />
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-apple-dark">Description</label>
            <textarea
              className="px-3 py-2 border border-apple-border rounded-lg text-sm resize-none
                focus:outline-none focus:ring-2 focus:ring-apple-blue"
              rows={3}
              value={newDesc}
              onChange={(e) => setNewDesc(e.target.value)}
            />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-apple-dark">Column</label>
            <select
              className="px-3 py-2 border border-apple-border rounded-lg text-sm"
              value={createStatus}
              onChange={(e) => setCreateStatus(e.target.value as TaskStatus)}
            >
              {COLUMNS.map((col) => (
                <option key={col.key} value={col.key}>
                  {col.label}
                </option>
              ))}
            </select>
          </div>
          <Button onClick={() => createMut.mutate()} loading={createMut.isPending}>
            Create
          </Button>
        </div>
      </Modal>

      <Modal
        open={!!selectedTask}
        onClose={() => setSelectedTask(null)}
        title={selectedTask?.title || 'Task'}
      >
        {selectedTask && (
          <div className="flex flex-col gap-4">
            <p className="text-sm text-apple-gray">{selectedTask.description || 'No description'}</p>
            <div className="flex gap-2 text-xs text-apple-gray">
              <span className="px-2 py-1 bg-apple-light rounded">{selectedTask.status}</span>
              <span>{new Date(selectedTask.createdAt).toLocaleDateString()}</span>
            </div>

            <div className="border-t border-apple-border pt-4">
              <h4 className="text-sm font-semibold text-apple-dark mb-3">Comments</h4>
              <div className="flex flex-col gap-3 mb-4 max-h-48 overflow-y-auto">
                {comments.map((c) => (
                  <div key={c.id} className="text-sm">
                    <span className="font-medium text-apple-dark">{c.authorName}</span>
                    <span className="text-apple-gray text-xs ml-2">
                      {new Date(c.createdAt).toLocaleString()}
                    </span>
                    <p className="text-apple-dark mt-1">{c.content}</p>
                  </div>
                ))}
                {comments.length === 0 && (
                  <p className="text-xs text-apple-gray">No comments yet</p>
                )}
              </div>
              <div className="flex gap-2">
                <input
                  className="flex-1 px-3 py-2 border border-apple-border rounded-lg text-sm
                    focus:outline-none focus:ring-2 focus:ring-apple-blue"
                  placeholder="Write a comment..."
                  value={commentText}
                  onChange={(e) => setCommentText(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && addComment()}
                />
                <Button onClick={addComment}>Send</Button>
              </div>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
