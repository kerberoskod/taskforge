import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getProjects, createProject, deleteProject, Project } from '../api/projects';
import Sidebar from '../components/layout/Sidebar';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import Modal from '../components/ui/Modal';

export default function DashboardPage() {
  const { data: projects = [] } = useQuery({ queryKey: ['projects'], queryFn: getProjects });
  const queryClient = useQueryClient();
  const [showCreate, setShowCreate] = useState(false);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');

  const createMut = useMutation({
    mutationFn: () => createProject({ name, description }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      setShowCreate(false);
      setName('');
      setDescription('');
    },
  });

  const deleteMut = useMutation({
    mutationFn: (id: string) => deleteProject(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['projects'] }),
  });

  return (
    <div className="flex h-screen bg-white">
      <Sidebar projects={projects} />

      <main className="flex-1 p-8 overflow-y-auto">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-bold text-apple-dark">Projects</h1>
          <Button onClick={() => setShowCreate(true)}>New Project</Button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {projects.map((p: Project) => (
            <div
              key={p.id}
              className="border border-apple-border rounded-xl p-5 hover:shadow-md transition-shadow"
            >
              <Link to={`/board/${p.id}`} className="block">
                <h3 className="font-semibold text-apple-dark text-lg mb-1">{p.name}</h3>
                {p.description && (
                  <p className="text-sm text-apple-gray line-clamp-2">{p.description}</p>
                )}
              </Link>
              <div className="flex items-center justify-between mt-4 pt-3 border-t border-apple-border">
                <span className="text-xs text-apple-gray">
                  {new Date(p.createdAt).toLocaleDateString()}
                </span>
                <button
                  onClick={() => deleteMut.mutate(p.id)}
                  className="text-xs text-red-400 hover:text-red-600"
                >
                  Delete
                </button>
              </div>
            </div>
          ))}

          {projects.length === 0 && (
            <div className="col-span-full text-center py-16 text-apple-gray">
              <p className="text-lg mb-2">No projects yet</p>
              <p className="text-sm">Create your first project to get started</p>
            </div>
          )}
        </div>
      </main>

      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="New Project">
        <div className="flex flex-col gap-4">
          <Input
            label="Project Name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-apple-dark">Description</label>
            <textarea
              className="px-3 py-2 border border-apple-border rounded-lg text-sm resize-none
                focus:outline-none focus:ring-2 focus:ring-apple-blue"
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>
          <Button onClick={() => createMut.mutate()} loading={createMut.isPending}>
            Create
          </Button>
        </div>
      </Modal>
    </div>
  );
}
