import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getProjects, createProject, deleteProject, Project } from '../api/projects';
import { useSidebarStore } from '../store/sidebarStore';
import Sidebar from '../components/layout/Sidebar';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import Modal from '../components/ui/Modal';
import { CardSkeleton } from '../components/ui/Skeleton';

export default function DashboardPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const pageSize = 12;

  const { data: pageData, isLoading } = useQuery({
    queryKey: ['projects', page],
    queryFn: () => getProjects(page, pageSize),
  });
  const projects = pageData?.content ?? [];
  const totalPages = pageData?.totalPages ?? 0;

  const [showCreate, setShowCreate] = useState(false);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const toggleSidebar = useSidebarStore((s) => s.toggle);

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
    <div className="flex h-screen bg-white dark:bg-gray-950">
      <Sidebar projects={projects} />

      <main className="flex-1 p-4 md:p-8 overflow-y-auto">
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-3">
            <button
              onClick={toggleSidebar}
              className="md:hidden p-2 -ml-2 text-apple-dark dark:text-gray-100 hover:bg-apple-light dark:hover:bg-gray-800 rounded-lg"
              aria-label="Toggle sidebar"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>
            <h1 className="text-xl md:text-2xl font-bold text-apple-dark dark:text-gray-100">Projects</h1>
          </div>
          <Button onClick={() => setShowCreate(true)}>New Project</Button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {isLoading ? (
            <>
              <CardSkeleton />
              <CardSkeleton />
              <CardSkeleton />
            </>
          ) : projects.length === 0 ? (
            <div className="col-span-full text-center py-16 text-apple-gray dark:text-gray-400">
              <p className="text-lg mb-2">No projects yet</p>
              <p className="text-sm">Create your first project to get started</p>
            </div>
          ) : (
            projects.map((p: Project) => (
              <div
                key={p.id}
                className="border border-apple-border dark:border-gray-700 rounded-xl p-5 hover:shadow-md dark:hover:shadow-gray-900/50 transition-shadow"
              >
                <Link to={`/board/${p.id}`} className="block">
                  <h3 className="font-semibold text-apple-dark dark:text-gray-100 text-lg mb-1">{p.name}</h3>
                  {p.description && (
                    <p className="text-sm text-apple-gray dark:text-gray-400 line-clamp-2">{p.description}</p>
                  )}
                </Link>
                <div className="flex items-center justify-between mt-4 pt-3 border-t border-apple-border dark:border-gray-700">
                  <span className="text-xs text-apple-gray dark:text-gray-400">
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
            ))
          )}
        </div>

        {totalPages > 1 && (
          <div className="flex items-center justify-center gap-2 mt-6">
            <button
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
              className="px-3 py-1 text-sm border border-apple-border rounded-lg disabled:opacity-40 hover:bg-apple-light"
            >
              Previous
            </button>
            {Array.from({ length: totalPages }, (_, i) => (
              <button
                key={i}
                onClick={() => setPage(i)}
                className={`px-3 py-1 text-sm border border-apple-border rounded-lg ${
                  i === page ? 'bg-apple-blue text-white border-apple-blue' : 'hover:bg-apple-light'
                }`}
              >
                {i + 1}
              </button>
            ))}
            <button
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              className="px-3 py-1 text-sm border border-apple-border rounded-lg disabled:opacity-40 hover:bg-apple-light"
            >
              Next
            </button>
          </div>
        )}
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
