interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger';
  loading?: boolean;
}

export default function Button({
  children,
  variant = 'primary',
  loading,
  className = '',
  disabled,
  ...props
}: ButtonProps) {
  const base = 'px-4 py-2 rounded-lg font-medium text-sm transition-all duration-150 disabled:opacity-50';
  const variants = {
    primary: 'bg-apple-blue text-white hover:brightness-110 active:brightness-90',
    secondary: 'bg-apple-light text-apple-dark border border-apple-border hover:bg-gray-200',
    danger: 'bg-red-500 text-white hover:bg-red-600',
  };

  return (
    <button
      className={`${base} ${variants[variant]} ${className}`}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? '...' : children}
    </button>
  );
}
