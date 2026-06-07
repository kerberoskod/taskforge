import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { loginApi } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const setAuth = useAuthStore((s) => s.setAuth);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await loginApi({ email, password });
      setAuth(res.user, res.accessToken, res.refreshToken);
      navigate('/');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-apple-light flex items-center justify-center px-4">
      <div className="bg-white p-6 md:p-8 rounded-2xl shadow-sm w-full max-w-sm">
        <h1 className="text-xl md:text-2xl font-bold text-apple-dark mb-1">TaskForge</h1>
        <p className="text-apple-gray text-sm mb-6">Sign in to your account</p>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <Input
            label="Email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <Input
            label="Password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          {error && <p className="text-sm text-red-500">{error}</p>}

          <Button type="submit" loading={loading}>
            Sign In
          </Button>
        </form>

        <p className="text-sm text-apple-gray text-center mt-6">
          Don't have an account?{' '}
          <Link to="/register" className="text-apple-blue hover:underline">
            Register
          </Link>
        </p>
      </div>
    </div>
  );
}
