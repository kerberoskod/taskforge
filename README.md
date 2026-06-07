# TaskForge — Project Management Board

![Java](https://img.shields.io/badge/Java-21-%23ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.4-%236DB33F?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-19-%2361DAFB?logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-5-%233178C6?logo=typescript&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-%234169E1?logo=postgresql&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-%23000000?logo=socket.io&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-%232496ED?logo=docker&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green)

**TaskForge** is a full-stack project management board inspired by Jira and Trello. Built with Java 21, Spring Boot 3.4, and React 19, it features real-time collaboration via WebSocket, a drag-and-drop Kanban interface, and full JWT-based authentication.

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Quick Start with Docker](#quick-start-with-docker)
- [Local Development](#local-development)
- [API Reference](#api-reference)
- [WebSocket Events](#websocket-events)
- [Environment Variables](#environment-variables)
- [Design Decisions](#design-decisions)
- [Why This Project](#why-this-project)
- [License](#license)
- [Contact](#contact)

---

## Features

| Feature | Description |
|---|---|
| **Kanban Board** | Drag-and-drop tasks across TODO → IN_PROGRESS → REVIEW → DONE |
| **Real-Time Sync** | WebSocket broadcasts changes to all connected clients instantly |
| **JWT Authentication** | Secure register/login with access + refresh token flow |
| **Project Management** | Create, rename, and delete projects |
| **Task Details** | Description, status, timestamps, assignee |
| **Comments** | Add and view comments on each task |
| **Responsive UI** | Apple-inspired design, works on desktop and tablet |
| **Dockerized** | One-command startup with docker-compose |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (React 19)                    │
│  Vite + TypeScript + Tailwind CSS + @hello-pangea/dnd    │
│  Zustand (auth) · React Query (server state) · SockJS    │
└──────────────┬──────────────────────────────────────────┘
               │ HTTP (REST)        │ WebSocket (STOMP)
               ▼                    ▼
┌─────────────────────────────────────────────────────────┐
│                  Backend (Spring Boot 3.4)               │
│                                                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐ │
│  │  Auth    │  │ Project  │  │   Task   │  │ Comment │ │
│  │ Service  │  │ Service  │  │  Service │  │ Service │ │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬────┘ │
│       │              │              │              │     │
│       └──────────────┴──────────────┴──────────────┘     │
│                          │                               │
│                    ┌─────┴──────┐                        │
│                    │ PostgreSQL │                        │
│                    └────────────┘                        │
│                                                          │
│  WebSocket STOMP Broker ── broadcasts board changes      │
└─────────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Category | Technology |
|---|---|
| **Backend** | Java 21, Spring Boot 3.4.4, Spring Security, Spring Data JPA, Spring WebSocket STOMP |
| **Frontend** | React 19, TypeScript 5, Vite 6, Tailwind CSS 3, @hello-pangea/dnd |
| **Database** | PostgreSQL 16 |
| **State** | Zustand (auth), TanStack React Query (server cache) |
| **Real-Time** | SockJS + STOMP over WebSocket |
| **HTTP** | Axios with interceptor-based token refresh |
| **Auth** | JWT (jjwt), BCrypt password hashing |
| **Build** | Maven 3.9, npm |
| **Deploy** | Docker Compose, Nginx (frontend), Spring Boot (backend) |

---

## Project Structure

```
taskforge/
├── docker-compose.yml            # One-command startup
├── .gitignore
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/taskforge/
│       ├── TaskForgeApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java         # Spring Security + CORS
│       │   └── WebSocketConfig.java        # STOMP broker
│       ├── auth/
│       │   ├── controller/AuthController.java
│       │   ├── dto/{LoginRequest,RegisterRequest,AuthResponse,RefreshTokenRequest}.java
│       │   ├── entity/User.java
│       │   ├── repository/UserRepository.java
│       │   ├── service/AuthService.java
│       │   └── security/
│       │       ├── JwtTokenProvider.java   # Token generation & validation
│       │       ├── JwtAuthFilter.java      # OncePerRequestFilter
│       │       └── UserDetailsServiceImpl.java
│       ├── project/
│       │   ├── controller/ProjectController.java
│       │   ├── dto/{CreateProjectRequest,ProjectResponse}.java
│       │   ├── entity/Project.java
│       │   ├── repository/ProjectRepository.java
│       │   └── service/ProjectService.java
│       ├── task/
│       │   ├── controller/TaskController.java
│       │   ├── dto/{CreateTaskRequest,UpdateTaskRequest,UpdateTaskPositionRequest,TaskResponse}.java
│       │   ├── entity/{Task,TaskStatus}.java
│       │   ├── repository/TaskRepository.java
│       │   └── service/TaskService.java
│       ├── comment/
│       │   ├── controller/CommentController.java
│       │   ├── dto/{CommentRequest,CommentResponse}.java
│       │   ├── entity/Comment.java
│       │   ├── repository/CommentRepository.java
│       │   └── service/CommentService.java
│       ├── websocket/
│       │   └── BoardEventController.java   # STOMP message handlers
│       └── exception/
│           ├── GlobalExceptionHandler.java # @RestControllerAdvice
│           └── ResourceNotFoundException.java
├── frontend/
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   ├── index.html
│   └── src/
│       ├── main.tsx
│       ├── App.tsx                     # Routes + ProtectedRoute
│       ├── api/
│       │   ├── client.ts               # Axios + token refresh interceptor
│       │   ├── auth.ts
│       │   ├── projects.ts
│       │   ├── tasks.ts
│       │   └── comments.ts
│       ├── store/
│       │   └── authStore.ts            # Zustand
│       ├── hooks/
│       │   └── useWebSocket.ts         # STOMP hook
│       ├── components/
│       │   ├── ui/{Button,Input,Modal}.tsx
│       │   └── layout/Sidebar.tsx
│       └── pages/
│           ├── LoginPage.tsx
│           ├── RegisterPage.tsx
│           ├── DashboardPage.tsx       # Project list
│           └── BoardPage.tsx           # Kanban + drag-drop + comments
```

---

## Prerequisites

- **Java 21+** — [Download](https://adoptium.net/)
- **Maven 3.9+** — [Download](https://maven.apache.org/download.cgi)
- **Node.js 22+** — [Download](https://nodejs.org/)

---

## Quick Start with Docker

```bash
git clone https://github.com/kerberoskod/taskforge.git
cd taskforge

docker compose up -d

# Open http://localhost:5173
# Register a new account and create your first project!
```

---

## Local Development

### Backend

```bash
cd backend

# Run with H2 in-memory database (default dev profile)
mvn spring-boot:run

# Backend starts at http://localhost:8080 with an H2 console at /h2-console
```

### Frontend

```bash
cd frontend
npm install
npm run dev
# Opens at http://localhost:5173, proxies /api to localhost:8080
```

### Verify

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"password123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
# → Returns accessToken, refreshToken, user
```

---

## API Reference

### Authentication

#### Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Response** `201 Created`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": "uuid",
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

#### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123"}'
```

### Projects

All project endpoints require `Authorization: Bearer <token>` header.

```bash
# List projects
curl http://localhost:8080/api/projects \
  -H "Authorization: Bearer <token>"

# Create project
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"My Project","description":"A sample project"}'

# Get project
curl http://localhost:8080/api/projects/{id} \
  -H "Authorization: Bearer <token>"

# Update project
curl -X PUT http://localhost:8080/api/projects/{id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"Updated Name","description":"Updated description"}'

# Delete project
curl -X DELETE http://localhost:8080/api/projects/{id} \
  -H "Authorization: Bearer <token>"
```

### Tasks

```bash
# List tasks in a project
curl http://localhost:8080/api/projects/{projectId}/tasks \
  -H "Authorization: Bearer <token>"

# Create task
curl -X POST http://localhost:8080/api/projects/{projectId}/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"title":"Implement login","description":"Add JWT auth","status":"TODO"}'

# Update task
curl -X PUT http://localhost:8080/api/projects/{projectId}/tasks/{taskId} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"status":"IN_PROGRESS"}'

# Move task (drag-drop position update)
curl -X PATCH http://localhost:8080/api/projects/{projectId}/tasks/position \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"taskId":"<taskId>","status":"DONE","position":0}'

# Delete task
curl -X DELETE http://localhost:8080/api/projects/{projectId}/tasks/{taskId} \
  -H "Authorization: Bearer <token>"
```

### Comments

```bash
# Get comments for a task
curl http://localhost:8080/api/tasks/{taskId}/comments \
  -H "Authorization: Bearer <token>"

# Add comment
curl -X POST http://localhost:8080/api/tasks/{taskId}/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"content":"This is a comment"}'

# Delete comment
curl -X DELETE http://localhost:8080/api/comments/{commentId} \
  -H "Authorization: Bearer <token>"
```

---

## WebSocket Events

TaskForge uses STOMP over WebSocket for real-time board updates.

### Connection

```javascript
const { Client } = require('@stomp/stompjs');
const SockJS = require('sockjs-client');

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
});

client.onConnect = () => {
  // Subscribe to board updates
  client.subscribe('/topic/projects/{projectId}', (message) => {
    console.log('Board updated:', JSON.parse(message.body));
  });

  // Send a move event
  client.publish({
    destination: '/app/board.move/{projectId}',
    body: JSON.stringify({ taskId: '...', status: 'DONE', position: 0 }),
  });
};

client.activate();
```

### Topics

| Destination | Direction | Payload |
|---|---|---|
| `/topic/projects/{projectId}` | Subscribe | `TaskResponse` (broadcast) |
| `/app/board.move/{projectId}` | Send | `TaskResponse` |
| `/app/board.create/{projectId}` | Send | `TaskResponse` |
| `/app/board.update/{projectId}` | Send | `TaskResponse` |
| `/app/board.delete/{projectId}` | Send | Task ID (string) |

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8080` | Backend HTTP port |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/taskforge` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `taskforge` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `taskforge` | Database password |
| `JWT_SECRET` | `taskforge-jwt-secret-change-in-production` | HMAC-SHA256 key for JWT |
| `JWT_ACCESS_TOKEN_EXPIRATION` | `86400000` | Access token TTL (ms, default 24h) |
| `JWT_REFRESH_TOKEN_EXPIRATION` | `604800000` | Refresh token TTL (ms, default 7d) |

---

## Design Decisions

| Decision | Rationale |
|---|---|
| **Full-Stack Approach** | Demonstrates end-to-end development capability — backend APIs, database modeling, real-time communication, and polished frontend UI |
| **React Query for Server State** | Automatic cache invalidation, refetching, and optimistic updates reduce boilerplate and improve UX |
| **Zustand for Auth** | Lightweight, hook-based state management — perfect for global auth state without the overhead of Redux |
| **JWT Access + Refresh Tokens** | Secure token rotation with silent refresh via Axios interceptor. Refresh token stored in localStorage for simplicity |
| **WebSocket (STOMP over SockJS)** | STOMP provides a pub-sub model over WebSocket with fallback options. Real-time board sync without polling |
| **@hello-pangea/dnd** | Maintained fork of react-beautiful-dnd with React 18/19 support. Accessible drag-and-drop |
| **Spring Security JWT Filter** | Stateless authentication without session management. JWT parsed in a OncePerRequestFilter |
| **Single Database per App** | Monolithic database for simplicity — the project demonstrates full-stack skill, not distributed data patterns |
| **Apple-Inspired UI** | Clean, minimal design with system fonts, generous whitespace, and subtle borders |

---

## Why This Project

TaskForge exists to demonstrate **full-stack development proficiency**:

- **Full-Stack Delivery** — From database schema to pixel-perfect UI
- **Real-Time Engineering** — WebSocket integration for live collaboration
- **Modern Frontend** — React 19, TypeScript, hooks, libraries
- **Secure APIs** — JWT auth with token refresh
- **Clean Code** — Layered architecture, separation of concerns, no Lombok
- **DevOps Ready** — Docker Compose, Nginx reverse proxy, multi-environment config

---

## License

MIT License — see [LICENSE](LICENSE) for details.

---

## Contact

**Developer:** kerberoskod

- **Email:** [kuvvetikarayan@mail.ru](mailto:kuvvetikarayan@mail.ru)
- **Discord:** [kerberoskod](https://discord.com/users/kerberoskod)
- **GitHub:** [kerberoskod](https://github.com/kerberoskod)

---

*Built with Java 21, Spring Boot 3.4, React 19, and a second pot of coffee.*
