import client from './client';

export interface LoginData {
  email: string;
  password: string;
}

export interface RegisterData {
  name: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  user: {
    id: string;
    name: string;
    email: string;
  };
}

export const loginApi = (data: LoginData) =>
  client.post<AuthResponse>('/auth/login', data).then((r) => r.data);

export const registerApi = (data: RegisterData) =>
  client.post<AuthResponse>('/auth/register', data).then((r) => r.data);

export const refreshApi = () =>
  client.post<AuthResponse>('/auth/refresh').then((r) => r.data);

export const logoutApi = () =>
  client.post('/auth/logout');
