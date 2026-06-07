interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
}

export default function Input({ label, error, className = '', ...props }: InputProps) {
  return (
    <div className="flex flex-col gap-1">
      {label && (
        <label className="text-sm font-medium text-apple-dark">{label}</label>
      )}
      <input
        className={`px-3 py-2 border border-apple-border rounded-lg text-sm
          focus:outline-none focus:ring-2 focus:ring-apple-blue focus:border-transparent
          ${error ? 'border-red-400' : ''} ${className}`}
        {...props}
      />
      {error && <span className="text-xs text-red-500">{error}</span>}
    </div>
  );
}
