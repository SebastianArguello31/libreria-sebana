# Sebana — Sistema de Préstamos de Libros

Proyecto final · Programación de Aplicaciones en Redes · FP-UNA 2026

**Desarrolladores:** Anamaria Acevedo y Sebastian Arguello

---

## Requisitos

- JDK 21
- PostgreSQL 18
- Node.js 24.15 o superior

---

## 1. Base de datos

```bash
psql -U postgres -c "CREATE DATABASE libreria-sebana;"
```

Las tablas se crean solas al iniciar el backend.

---

## 2. Backend

```bash
cd backend
cp .env.example .env
```

Completar `backend/.env` con las credenciales de PostgreSQL y un `JWT_SECRET` (Base64 de 32 bytes o más):

```bash
openssl rand -base64 32
```

Ejecutar:

```bash
./mvnw spring-boot:run
```

API en http://localhost:8080 · Swagger en http://localhost:8080/swagger-ui.html

---

## 3. Frontend

En otra terminal:

```bash
cd frontend
npm ci
npm start
```

Abrir http://localhost:4200

---

## Usuarios de prueba

| Email | Contraseña | Rol |
|---|---|---|
| admin@sebana.com | `ADMIN_PASSWORD` del `.env` | ADMIN |
| lector@sebana.com | `USUARIO_PASSWORD` del `.env` | USUARIO |