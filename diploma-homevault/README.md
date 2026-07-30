# HomeVault Diploma

Дипломный проект: self-hosted система для хранения файлов и заметок.

HomeVault запускается локально через Docker Compose и показывает полный backend/frontend flow: регистрация, JWT-авторизация, папки, файлы в MinIO, заметки, публичные ссылки, аудит и админка.

## Stack

Backend:

- Java 21;
- Spring Boot 3;
- Spring MVC / REST API;
- Spring Security + JWT;
- Spring Data JPA;
- PostgreSQL;
- Flyway;
- Bean Validation;
- MinIO;
- Springdoc OpenAPI + Swagger UI;
- Spring Boot Actuator;
- Redis;
- JUnit 5, Mockito, Testcontainers.

Frontend:

- React;
- TypeScript;
- Vite;
- Material UI;
- Redux Toolkit;
- RTK Query;
- React Router;
- React Hook Form;
- Zod.

Infrastructure:

- Docker;
- Docker Compose;
- nginx для frontend и proxy `/api` на backend.

## Architecture

Проект сделан как модульный монолит.

```text
diploma-homevault/
  backend/              # Spring Boot backend
  frontend/             # React frontend
  docs/                 # требования, архитектура, API, БД, защита
  docker-compose.yml    # Postgres, MinIO, Redis, backend, frontend
  .env.example          # пример переменных окружения
```

Основные backend-модули:

- `auth` — регистрация, login, JWT, refresh token, logout;
- `users` — профиль, роли, статус пользователя;
- `folders` — папки и структура хранения;
- `storage` — upload/download/delete файлов и MinIO integration;
- `notes` — заметки, теги, поиск;
- `sharing` — публичные ссылки с истечением и отзывом;
- `audit` — журнал действий;
- `admin` — пользователи, блокировка, статистика;
- `common` — security, ошибки, OpenAPI, общие DTO.

PostgreSQL хранит пользователей, папки, метаданные файлов, заметки, ссылки и аудит. MinIO хранит содержимое файлов. Frontend обращается к backend через nginx proxy `/api`.

## Quick start

Требования:

- Docker;
- Docker Compose.

Запуск:

```bash
cp .env.example .env
docker compose up --build
```

Остановка:

```bash
docker compose down
```

Остановка с удалением локальных данных:

```bash
docker compose down -v
```

## Services and ports

| Service | URL | Description |
|---|---|---|
| Frontend | http://localhost:3000 | React application |
| Backend API | http://localhost:8080/api/v1 | REST API |
| Swagger UI | http://localhost:8080/swagger-ui/index.html | API documentation |
| OpenAPI JSON | http://localhost:8080/v3/api-docs | OpenAPI spec |
| Actuator health | http://localhost:8080/actuator/health | Backend health |
| MinIO API | http://localhost:9000 | Object storage API |
| MinIO Console | http://localhost:9001 | Object storage admin UI |
| PostgreSQL | localhost:5433 | Database |
| Redis | localhost:6380 | Cache/session infrastructure |

MinIO demo credentials берутся из `.env`:

- login: `homevault`;
- password: `homevault-secret`.

## Demo credentials

Если запускать compose с `.env.example`, backend создаст demo admin:

- email: `admin@homevault.local`;
- password: `Admin12345`.

Seed admin создается только если заданы `HOMEVAULT_ADMIN_EMAIL` и `HOMEVAULT_ADMIN_PASSWORD`. Если пользователь с таким email уже существует, backend его не перезаписывает.

Обычного пользователя можно создать через страницу регистрации frontend.

## Showcase data

После запуска контейнеров можно создать готовый набор данных для защиты:

```bash
./scripts/seed-demo-data.sh
```

Скрипт создаст или переиспользует пользователя `demo@homevault.local` с паролем `Demo12345`, папки, файлы, заметки и публичные ссылки. Подробный сценарий показа находится в `docs/11-implementation-audit-and-demo.md`.

## Environment variables

| Variable | Default | Description |
|---|---|---|
| `POSTGRES_DB` | `homevault` | PostgreSQL database |
| `POSTGRES_USER` | `homevault` | PostgreSQL user |
| `POSTGRES_PASSWORD` | `homevault` | PostgreSQL password |
| `MINIO_ROOT_USER` | `homevault` | MinIO console/API user |
| `MINIO_ROOT_PASSWORD` | `homevault-secret` | MinIO password |
| `MINIO_BUCKET` | `homevault-files` | Bucket for file content |
| `REDIS_PASSWORD` | `homevault-redis` | Redis password |
| `JWT_SECRET` | demo value | JWT signing secret |
| `JWT_ACCESS_TTL_MINUTES` | `30` | Access token lifetime |
| `JWT_REFRESH_TTL_DAYS` | `14` | Refresh token lifetime |
| `HOMEVAULT_ADMIN_EMAIL` | `admin@homevault.local` | Demo admin email |
| `HOMEVAULT_ADMIN_PASSWORD` | `Admin12345` | Demo admin password |
| `HOMEVAULT_ADMIN_DISPLAY_NAME` | `HomeVault Admin` | Demo admin display name |
| `SPRING_PROFILES_ACTIVE` | `docker` | Spring profile for compose |
| `VITE_API_BASE_URL` | `/api/v1` | Frontend API base URL |

Для реального self-hosted запуска нужно заменить пароли и `JWT_SECRET`.

## Demo flow

1. Открыть frontend: http://localhost:3000.
2. Зарегистрировать обычного пользователя.
3. Создать папку.
4. Загрузить файл.
5. Скачать файл.
6. Создать заметку с тегами.
7. Создать публичную ссылку на файл или заметку.
8. Открыть публичную ссылку без JWT: `http://localhost:8080/api/v1/public/shares/{token}`.
9. Отозвать публичную ссылку.
10. Открыть `Audit` и проверить события.
11. Войти как `admin@homevault.local` / `Admin12345`.
12. Открыть `Admin stats`.
13. Открыть `Admin users` и заблокировать/разблокировать пользователя.
14. Открыть Swagger UI: http://localhost:8080/swagger-ui/index.html.

## Useful commands

Проверить compose:

```bash
docker compose config
```

Пересобрать и запустить:

```bash
docker compose up -d --build
```

Посмотреть логи backend:

```bash
docker compose logs -f backend
```

Запустить backend tests:

```bash
cd backend
mvn test
```

Запустить frontend build:

```bash
cd frontend
npm ci
npm run build
```

Frontend-тесты в этом дипломном проекте не используются: тестами покрывается backend.
