import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { useSidebarStore } from '../../store/sidebarStore';
import { useThemeStore } from '../../store/themeStore';

interface SidebarProps {
  projects: { id: string; name: string }[];
  currentProjectId?: string;
}

export default function Sidebar({ projects, currentProjectId }: SidebarProps) {
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);
  const navigate = useNavigate();
  const sidebarOpen = useSidebarStore((s) => s.open);
  const closeSidebar = useSidebarStore((s) => s.close);
  const { theme, toggle } = useThemeStore();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleNav = () => {
    closeSidebar();
  };

  return (
    <>
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black/40 z-30 md:hidden"
          onClick={closeSidebar}
        />
      )}

      <aside
        className={`
          fixed md:static inset-y-0 left-0 z-40 w-64
          bg-apple-light border-r border-apple-border
          flex flex-col transition-transform duration-200
          ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}
          md:translate-x-0
        `}
      >
        <div className="p-4 border-b border-apple-border flex items-center justify-between">
          <Link to="/" className="text-xl font-bold text-apple-dark" onClick={handleNav}>
            TaskForge
          </Link>
          <button
            onClick={closeSidebar}
            className="md:hidden text-apple-gray hover:text-apple-dark text-xl leading-none"
          >
            &times;
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-3">
          <p className="text-xs font-semibold text-apple-gray uppercase tracking-wider mb-2 px-2">
            Projects
          </p>
          {projects.map((p) => (
            <Link
              key={p.id}
              to={`/board/${p.id}`}
              onClick={handleNav}
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
          <button
            onClick={toggle}
            className="flex items-center gap-2 text-sm text-apple-gray hover:text-apple-dark mb-3 w-full transition-colors"
          >
            {theme === 'dark' ? (
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
            ) : (
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
              </svg>
            )}
            {theme === 'dark' ? 'Light Mode' : 'Dark Mode'}
          </button>
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
    </>
  );
}
