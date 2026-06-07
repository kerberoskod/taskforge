import client from './client';

export interface User {
  id: string;
  name: string;
  email: string;
}

export const getUsers = () =>
  client.get<User[]>('/users').then((r) => r.data);
