# Docker

## Archivos creados

- `Dockerfile`: construye la aplicacion Spring Boot con Java 21.
- `docker-compose.yml`: levanta la aplicacion conectandola a tu MySQL local.
- `.env.example`: variables de entorno base para `docker compose`.
- `.dockerignore`: evita enviar artefactos y carpetas locales al build.

## Uso

1. Copia `.env.example` a `.env`.
2. Ajusta `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` y `DB_PASSWORD` para que coincidan con tu MySQL local.
3. Ejecuta:

```bash
docker compose up --build
```

La aplicacion quedara disponible en `http://localhost:8080`.

## Conexion a MySQL local

- Los archivos subidos se guardan en el volumen `app_uploads`.
- La app usa `host.docker.internal` para llegar a tu MySQL de Windows desde Docker.
- Si tu MySQL local usa el puerto `3306`, deja `DB_PORT=3306`.
- Si tu MySQL local usa otro puerto, cambia `DB_PORT` en `.env`.

## Usuario inicial

- Si la base esta vacia, al arrancar se crean los perfiles base del sistema.
- Si no existe un administrador activo, se crea uno usando las variables:
  - `APP_BOOTSTRAP_ADMIN_USERNAME`
  - `APP_BOOTSTRAP_ADMIN_PASSWORD`
  - `APP_BOOTSTRAP_ADMIN_NOMBRE`

Valores por defecto:

```text
usuario: admin
password: admin123
```

## Notas

- La configuracion de `application.properties` ahora admite variables de entorno, pero mantiene valores por defecto para seguir funcionando fuera de Docker.
- Si quieres limpiar el volumen de archivos subidos:

```bash
docker compose down -v
```
