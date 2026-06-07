import { useState, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { DragDropContext, Droppable, Draggable, DropResult } from '@hello-pangea/dnd';
import { getProject, type PageResponse } from '../api/projects';
import { getTasks, createTask, updateTask, updateTaskPosition, Task, TaskStatus } from '../api/tasks';
import { getComments, createComment, Comment } from '../api/comments';
import { getUsers, User } from '../api/users';
import { getLabels, Label } from '../api/labels';
import { getCollaborators, addCollaborator, removeCollaborator, Collaborator } from '../api/collaborators';
import { getActivityLogs, ActivityLogEntry } from '../api/activity';
import { useAuthStore } from '../store/authStore';
import { useWebSocket } from '../hooks/useWebSocket';
import { useSidebarStore } from '../store/sidebarStore';
import Sidebar from '../components/layout/Sidebar';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import Modal from '../components/ui/Modal';
import { BoardColumnSkeleton } from '../components/ui/Skeleton';

const COLUMNS: { key: TaskStatus; label: string; color: string }[] = [
  { key: 'TODO', label: 'To Do', color: 'bg-gray-100' },
  { key: 'IN_PROGRESS', label: 'In Progress', color: 'bg-blue-50' },
  { key: 'REVIEW', label: 'Review', color: 'bg-yellow-50' },
  { key: 'DONE', label: 'Done', color: 'bg-green-50' },
];

export default function BoardPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const queryClient = useQueryClient();
  const toggleSidebar = useSidebarStore((s) => s.toggle);

  const { data: project } = useQuery({
    queryKey: ['project', projectId],
    queryFn: () => getProject(projectId!),
    enabled: !!projectId,
  });

  const { data: tasksPage, isLoading: tasksLoading } = useQuery({
    queryKey: ['tasks', projectId],
    queryFn: () => getTasks(projectId!),
    enabled: !!projectId,
  });
  const tasks = tasksPage?.content ?? [];

  const { data: projectsPage } = useQuery({
    queryKey: ['projects'],
    queryFn: () => import('../api/projects').then((m) => m.getProjects()),
  });
  const projects = projectsPage?.content ?? [];

  const { data: users = [] } = useQuery({
    queryKey: ['users'],
    queryFn: getUsers,
  });

  const userMap = new Map(users.map((u) => [u.id, u.name]));

  const { data: labels = [] } = useQuery({
    queryKey: ['labels', projectId],
    queryFn: () => getLabels(projectId!),
    enabled: !!projectId,
  });

  const labelMap = new Map(labels.map((l) => [l.id, l]));
  const currentUser = useAuthStore((s) => s.user);
  const isOwner = project?.ownerId === currentUser?.id;

  const [showCollaborators, setShowCollaborators] = useState(false);
  const { data: collaborators = [] } = useQuery({
    queryKey: ['collaborators', projectId],
    queryFn: () => getCollaborators(projectId!),
    enabled: !!projectId && isOwner,
  });
  const [newCollabUserId, setNewCollabUserId] = useState('');

  const addCollabMut = useMutation({
    mutationFn: () => addCollaborator(projectId!, { userId: newCollabUserId }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['collaborators', projectId] });
      setNewCollabUserId('');
    },
  });

  const removeCollabMut = useMutation({
    mutationFn: (userId: string) => removeCollaborator(projectId!, userId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['collaborators', projectId] }),
  });

  const [showActivity, setShowActivity] = useState(false);
  const { data: activityPage } = useQuery({
    queryKey: ['activity', projectId],
    queryFn: () => getActivityLogs(projectId!),
    enabled: !!projectId && showActivity,
  });
  const activityLogs = activityPage?.content ?? [];

  const [searchQuery, setSearchQuery] = useState('');
  const [filterLabelId, setFilterLabelId] = useState('');
  const [filterAssigneeId, setFilterAssigneeId] = useState('');

  const filteredTasks = tasks.filter((t) => {
    if (searchQuery && !t.title.toLowerCase().includes(searchQuery.toLowerCase()) && !t.description?.toLowerCase().includes(searchQuery.toLowerCase())) return false;
    if (filterLabelId && !t.labelIds.includes(filterLabelId)) return false;
    if (filterAssigneeId && t.assigneeId !== filterAssigneeId) return false;
    return true;
  });

  const columnTasks = (colKey: string) =>
    filteredTasks
      .filter((t) => t.status === colKey)
      .sort((a, b) => a.position - b.position);

  const [showCreate, setShowCreate] = useState(false);
  const [createStatus, setCreateStatus] = useState<TaskStatus>('TODO');
  const [newTitle, setNewTitle] = useState('');
  const [newDesc, setNewDesc] = useState('');
  const [newAssignee, setNewAssignee] = useState('');
  const [newDueDate, setNewDueDate] = useState('');

  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentText, setCommentText] = useState('');

  const [isEditing, setIsEditing] = useState(false);
  const [editTitle, setEditTitle] = useState('');
  const [editDesc, setEditDesc] = useState('');
  const [editStatus, setEditStatus] = useState<TaskStatus>('TODO');
  const [editAssignee, setEditAssignee] = useState('');
  const [editDueDate, setEditDueDate] = useState('');

  const updateTaskMut = useMutation({
    mutationFn: (data: { taskId: string; title: string; description: string; status: string; assigneeId?: string; dueDate?: string }) =>
      updateTask(projectId!, data.taskId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', projectId] });
      setIsEditing(false);
    },
  });

  useWebSocket(projectId, (msg) => {
    queryClient.invalidateQueries({ queryKey: ['tasks', projectId] });
  });

  const createMut = useMutation({
    mutationFn: () => createTask(projectId!, { title: newTitle, description: newDesc, status: createStatus, assigneeId: newAssignee || undefined, dueDate: newDueDate || undefined }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', projectId] });
      setShowCreate(false);
      setNewTitle('');
      setNewDesc('');
      setNewAssignee('');
      setNewDueDate('');
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

      queryClient.setQueryData(['tasks', projectId], (old: PageResponse<Task> | undefined) => {
        if (!old) return old;
        return {
          ...old,
          content: old.content.map((t) =>
            t.id === draggableId ? { ...t, status: newStatus, position: newPosition } : t
          ),
        };
      });
    },
    [tasks, projectId, queryClient, updatePosMut]
  );

  const openTaskDetail = async (task: Task) => {
    setSelectedTask(task);
    setIsEditing(false);
    setEditTitle(task.title);
    setEditDesc(task.description);
    setEditStatus(task.status);
    setEditAssignee(task.assigneeId || '');
    setEditDueDate(task.dueDate || '');
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
    <div className="flex h-screen bg-white dark:bg-gray-950">
      <Sidebar projects={projects} currentProjectId={projectId} />

      <main className="flex-1 flex flex-col overflow-hidden">
        <div className="p-4 border-b border-apple-border dark:border-gray-700 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={toggleSidebar}
              className="md:hidden text-apple-dark dark:text-gray-100 p-1"
              aria-label="Toggle sidebar"
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>
            <h1 className="text-lg md:text-xl font-bold text-apple-dark dark:text-gray-100">{project?.name || 'Board'}</h1>
          </div>
          <div className="flex items-center gap-2">
            <Button variant="secondary" onClick={() => setShowActivity(true)} className="text-sm md:text-base">
              Activity
            </Button>
            {isOwner && (
              <Button variant="secondary" onClick={() => setShowCollaborators(true)} className="text-sm md:text-base">
                Manage
              </Button>
            )}
            <Button onClick={() => setShowCreate(true)} className="text-sm md:text-base">Add Task</Button>
          </div>
        </div>

        <div className="px-4 pb-2 flex items-center gap-2 flex-wrap">
          <input
            type="text"
            placeholder="Search tasks..."
            className="px-2 py-1 border border-apple-border rounded-lg text-xs w-40"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
          <select
            className="px-2 py-1 border border-apple-border rounded-lg text-xs"
            value={filterAssigneeId}
            onChange={(e) => setFilterAssigneeId(e.target.value)}
          >
            <option value="">All Assignees</option>
            {users.map((u) => (
              <option key={u.id} value={u.id}>{u.name}</option>
            ))}
          </select>
          <select
            className="px-2 py-1 border border-apple-border rounded-lg text-xs"
            value={filterLabelId}
            onChange={(e) => setFilterLabelId(e.target.value)}
          >
            <option value="">All Labels</option>
            {labels.map((l) => (
              <option key={l.id} value={l.id}>{l.name}</option>
            ))}
          </select>
        </div>

        {tasksLoading ? (
          <div className="flex-1 flex md:flex-row flex-col gap-3 md:gap-4 p-3 md:p-4 overflow-x-auto">
            <BoardColumnSkeleton />
            <BoardColumnSkeleton />
            <BoardColumnSkeleton />
            <BoardColumnSkeleton />
          </div>
        ) : (
        <DragDropContext onDragEnd={onDragEnd}>
          <div className="flex-1 flex md:flex-row flex-col gap-3 md:gap-4 p-3 md:p-4 overflow-x-auto">
            {COLUMNS.map((col) => {
              const columnTasks = filteredTasks
                .filter((t) => t.status === col.key)
                .sort((a, b) => a.position - b.position);

              return (
                <div key={col.key} className="flex-1 min-w-[250px] flex flex-col">
                  <div className={`rounded-t-xl px-3 py-2 ${col.color} dark:opacity-90`}>
                    <h3 className="font-semibold text-sm text-apple-dark dark:text-gray-100">
                      {col.label}
                      <span className="ml-2 text-apple-gray dark:text-gray-400 font-normal">
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
                          snapshot.isDraggingOver ? 'bg-apple-light dark:bg-gray-800' : col.color
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
                                className={`bg-white dark:bg-gray-900 rounded-lg p-3 mb-2 shadow-sm border border-apple-border dark:border-gray-700
                                  cursor-pointer hover:shadow-md dark:hover:shadow-gray-900/50 transition-shadow
                                  ${snapshot.isDragging ? 'shadow-lg rotate-2' : ''}`}
                              >
                                <p className="text-sm font-medium text-apple-dark dark:text-gray-100">{task.title}</p>
                                {task.description && (
                                  <p className="text-xs text-apple-gray dark:text-gray-400 mt-1 line-clamp-2">
                                    {task.description}
                                  </p>
                                )}
                                <div className="flex items-center gap-2 mt-2 flex-wrap">
                                  {task.assigneeId && (
                                    <span className="text-xs text-apple-gray dark:text-gray-400">
                                      {userMap.get(task.assigneeId) || 'Unknown'}
                                    </span>
                                  )}
                                  {task.dueDate && (
                                    <span className={`text-xs ${new Date(task.dueDate) < new Date() && task.status !== 'DONE' ? 'text-red-400' : 'text-apple-gray dark:text-gray-400'}`}>
                                      {new Date(task.dueDate).toLocaleDateString()}
                                    </span>
                                  )}
                                </div>
                                {task.labelIds.length > 0 && (
                                  <div className="flex gap-1 mt-2 flex-wrap">
                                    {task.labelIds.map((lid) => {
                                      const label = labelMap.get(lid);
                                      return label ? (
                                        <span
                                          key={lid}
                                          className="text-xs px-1.5 py-0.5 rounded"
                                          style={{ backgroundColor: label.color + '20', color: label.color }}
                                        >
                                          {label.name}
                                        </span>
                                      ) : null;
                                    })}
                                  </div>
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
        )}
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
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-apple-dark">Assignee</label>
            <select
              className="px-3 py-2 border border-apple-border rounded-lg text-sm"
              value={newAssignee}
              onChange={(e) => setNewAssignee(e.target.value)}
            >
              <option value="">Unassigned</option>
              {users.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.name}
                </option>
              ))}
            </select>
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-apple-dark">Due Date</label>
            <input
              type="date"
              className="px-3 py-2 border border-apple-border rounded-lg text-sm"
              value={newDueDate}
              onChange={(e) => setNewDueDate(e.target.value)}
            />
          </div>
          <Button onClick={() => createMut.mutate()} loading={createMut.isPending}>
            Create
          </Button>
        </div>
      </Modal>

      <Modal open={showActivity} onClose={() => setShowActivity(false)} title="Activity Log">
        <div className="flex flex-col gap-3 max-h-96 overflow-y-auto">
          {activityLogs.length === 0 ? (
            <p className="text-sm text-apple-gray">No activity yet</p>
          ) : (
            activityLogs.map((log) => (
              <div key={log.id} className="text-sm border-b border-apple-border pb-2">
                <div className="flex items-center justify-between">
                  <span className="font-medium text-apple-dark">{log.userName}</span>
                  <span className="text-xs text-apple-gray">{new Date(log.createdAt).toLocaleString()}</span>
                </div>
                <p className="text-apple-gray mt-0.5">{log.details}</p>
              </div>
            ))
          )}
        </div>
      </Modal>

      <Modal open={showCollaborators} onClose={() => setShowCollaborators(false)} title="Manage Collaborators">
        <div className="flex flex-col gap-4">
          <div className="flex flex-col gap-2 max-h-48 overflow-y-auto">
            {collaborators.length === 0 ? (
              <p className="text-sm text-apple-gray">No collaborators yet</p>
            ) : (
              collaborators.map((c) => (
                <div key={c.id} className="flex items-center justify-between py-2 border-b border-apple-border">
                  <div>
                    <p className="text-sm font-medium">{c.userName}</p>
                    <p className="text-xs text-apple-gray">{c.userEmail}</p>
                  </div>
                  <button
                    onClick={() => removeCollabMut.mutate(c.userId)}
                    className="text-xs text-red-400 hover:text-red-600"
                  >
                    Remove
                  </button>
                </div>
              ))
            )}
          </div>
          <div className="flex gap-2">
            <select
              className="flex-1 px-3 py-2 border border-apple-border rounded-lg text-sm"
              value={newCollabUserId}
              onChange={(e) => setNewCollabUserId(e.target.value)}
            >
              <option value="">Select a user...</option>
              {users
                .filter((u) => u.id !== currentUser?.id && !collaborators.some((c) => c.userId === u.id))
                .map((u) => (
                  <option key={u.id} value={u.id}>{u.name}</option>
                ))}
            </select>
            <Button onClick={() => addCollabMut.mutate()} disabled={!newCollabUserId} loading={addCollabMut.isPending}>
              Add
            </Button>
          </div>
        </div>
      </Modal>

      <Modal
        open={!!selectedTask}
        onClose={() => { setSelectedTask(null); setIsEditing(false); }}
        title={selectedTask?.title || 'Task'}
      >
        {selectedTask && (
          <div className="flex flex-col gap-4">
            {isEditing ? (
              <>
                <Input label="Title" value={editTitle} onChange={(e) => setEditTitle(e.target.value)} />
                <div className="flex flex-col gap-1">
                  <label className="text-sm font-medium text-apple-dark">Description</label>
                  <textarea
                    className="px-3 py-2 border border-apple-border rounded-lg text-sm resize-none
                      focus:outline-none focus:ring-2 focus:ring-apple-blue"
                    rows={3}
                    value={editDesc}
                    onChange={(e) => setEditDesc(e.target.value)}
                  />
                </div>
                <div className="flex flex-col gap-1">
                  <label className="text-sm font-medium text-apple-dark">Status</label>
                  <select
                    className="px-3 py-2 border border-apple-border rounded-lg text-sm"
                    value={editStatus}
                    onChange={(e) => setEditStatus(e.target.value as TaskStatus)}
                  >
                    {COLUMNS.map((col) => (
                      <option key={col.key} value={col.key}>{col.label}</option>
                    ))}
                  </select>
                </div>
                <div className="flex flex-col gap-1">
                  <label className="text-sm font-medium text-apple-dark">Assignee</label>
                  <select
                    className="px-3 py-2 border border-apple-border rounded-lg text-sm"
                    value={editAssignee}
                    onChange={(e) => setEditAssignee(e.target.value)}
                  >
                    <option value="">Unassigned</option>
                    {users.map((u) => (
                      <option key={u.id} value={u.id}>{u.name}</option>
                    ))}
                  </select>
                </div>
                <div className="flex flex-col gap-1">
                  <label className="text-sm font-medium text-apple-dark">Due Date</label>
                  <input
                    type="date"
                    className="px-3 py-2 border border-apple-border rounded-lg text-sm"
                    value={editDueDate}
                    onChange={(e) => setEditDueDate(e.target.value)}
                  />
                </div>
                <div className="flex gap-2">
                  <Button onClick={() => updateTaskMut.mutate({
                    taskId: selectedTask.id,
                    title: editTitle,
                    description: editDesc,
                    status: editStatus,
                    assigneeId: editAssignee || undefined,
                    dueDate: editDueDate || undefined,
                  })} loading={updateTaskMut.isPending}>
                    Save
                  </Button>
                  <Button variant="secondary" onClick={() => setIsEditing(false)}>Cancel</Button>
                </div>
              </>
            ) : (
              <>
                <p className="text-sm text-apple-gray dark:text-gray-400">{selectedTask.description || 'No description'}</p>
                <div className="flex flex-wrap gap-2 text-xs text-apple-gray dark:text-gray-400">
                  <span className="px-2 py-1 bg-apple-light dark:bg-gray-800 rounded">{selectedTask.status}</span>
                  {selectedTask.assigneeId && (
                    <span className="px-2 py-1 bg-apple-light dark:bg-gray-800 rounded">
                      {userMap.get(selectedTask.assigneeId) || 'Unknown'}
                    </span>
                  )}
                  {selectedTask.dueDate && (
                    <span className={`px-2 py-1 rounded ${new Date(selectedTask.dueDate) < new Date() && selectedTask.status !== 'DONE' ? 'bg-red-100 dark:bg-red-900 text-red-600' : 'bg-apple-light dark:bg-gray-800 text-apple-gray dark:text-gray-400'}`}>
                      Due: {new Date(selectedTask.dueDate).toLocaleDateString()}
                    </span>
                  )}
                  <span>{new Date(selectedTask.createdAt).toLocaleDateString()}</span>
                </div>
                <Button onClick={() => setIsEditing(true)}>Edit</Button>
              </>
            )}

            <div className="border-t border-apple-border dark:border-gray-700 pt-4">
              <h4 className="text-sm font-semibold text-apple-dark dark:text-gray-100 mb-3">Comments</h4>
              <div className="flex flex-col gap-3 mb-4 max-h-48 overflow-y-auto">
                {comments.map((c) => (
                  <div key={c.id} className="text-sm">
                    <span className="font-medium text-apple-dark dark:text-gray-100">{c.authorName}</span>
                    <span className="text-apple-gray dark:text-gray-400 text-xs ml-2">
                      {new Date(c.createdAt).toLocaleString()}
                    </span>
                    <p className="text-apple-dark dark:text-gray-100 mt-1">{c.content}</p>
                  </div>
                ))}
                {comments.length === 0 && (
                  <p className="text-xs text-apple-gray dark:text-gray-400">No comments yet</p>
                )}
              </div>
              <div className="flex gap-2">
                <input
                  className="flex-1 px-3 py-2 border border-apple-border dark:border-gray-600 rounded-lg text-sm
                    bg-white dark:bg-gray-800 text-apple-dark dark:text-gray-100
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
