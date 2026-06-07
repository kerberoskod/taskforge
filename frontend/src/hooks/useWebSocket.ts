import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export function useWebSocket(
  projectId: string | undefined,
  onMessage: (msg: any) => void
) {
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!projectId) return;

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/projects/${projectId}`, (message) => {
          const body = JSON.parse(message.body);
          onMessage(body);
        });
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [projectId]);
}
