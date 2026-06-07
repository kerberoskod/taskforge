import client from './client';
import type { PageResponse } from './projects';

export interface ActivityLogEntry {
  id: string;
  projectId: string;
  userId: string;
  userName: string;
  action: string;
  entityType: string;
  entityId: string;
  details: string;
  createdAt: string;
}

export const getActivityLogs = (projectId: string, page = 0, size = 50) =>
  client.get<PageResponse<ActivityLogEntry>>(`/projects/${projectId}/activity`, { params: { page, size } }).then((r) => r.data);
