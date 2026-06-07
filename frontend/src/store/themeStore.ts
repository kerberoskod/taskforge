import { create } from 'zustand';

type Theme = 'light' | 'dark';

interface ThemeStore {
  theme: Theme;
  toggle: () => void;
  setTheme: (theme: Theme) => void;
}

const getInitial = (): Theme => {
  if (typeof window === 'undefined') return 'light';
  const stored = localStorage.getItem('taskforge-theme');
  if (stored === 'dark' || stored === 'light') return stored;
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
};

export const useThemeStore = create<ThemeStore>((set) => ({
  theme: getInitial(),
  toggle: () =>
    set((state) => {
      const next = state.theme === 'light' ? 'dark' : 'light';
      localStorage.setItem('taskforge-theme', next);
      return { theme: next };
    }),
  setTheme: (theme: Theme) => {
    localStorage.setItem('taskforge-theme', theme);
    set({ theme });
  },
}));
