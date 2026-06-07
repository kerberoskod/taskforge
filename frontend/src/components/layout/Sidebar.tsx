import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

interface SidebarProps {
  projects: { id: string; name: string }[];
  currentProjectId?: string;
}

export default function Sidebar({ projects, currentProjectId }: SidebarProps) {
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <aside className="w-64 h-screen bg-apple-light border-r border-apple-border flex flex-col">
      <div className="p-4 border-b border-apple-border">
        <Link to="/" className="text-xl font-bold text-apple-dark">
          TaskForge
        </Link>
      </div>

      <div className="flex-1 overflow-y-auto p-3">
        <p className="text-xs font-semibold text-apple-gray uppercase tracking-wider mb-2 px-2">
          Projects
        </p>
        {projects.map((p) => (
          <Link
            key={p.id}
            to={`/board/${p.id}`}
            className={`block px-3 py-2 rounded-lg text-sm mb-1 transition-colors
              ${
                currentProjectId === p.id
                  ? 'bg-white text-apple-dark font-medium shadow-sm'
                  : 'text-apple-gray hover:bg-white/50'
              }`}
          >
            {p.name}
          </Link>
        ))}
      </div>

      <div className="p-4 border-t border-apple-border">
        <p className="text-sm font-medium text-apple-dark">{user?.name}</p>
        <p className="text-xs text-apple-gray mb-2">{user?.email}</p>
        <button
          onClick={handleLogout}
          className="text-xs text-red-500 hover:text-red-700"
        >
          Sign out
        </button>
      </div>
    </aside>
  );
}
