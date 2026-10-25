# StockFlow deployment checklist

## Before deployment

1. Choose a host with Docker Engine and Docker Compose. Keep MySQL on the private Compose network; Compose binds its host port to `127.0.0.1`.
2. Copy `.env.example` to `.env`. Replace every placeholder with unique values. Use separate strong values for `DB_ROOT_PASSWORD`, `DB_PASSWORD`, `JWT_SECRET`, and `INITIAL_ADMIN_PASSWORD`. Keep `.env` out of source control and restrict its file permissions.
   If a password contains `$`, wrap that value in single quotes in `.env` so Compose does not try to expand it as a variable. Check `docker compose config --quiet` for interpolation warnings before startup.
3. Set `CORS_ALLOWED_ORIGINS` to the exact HTTPS origin serving StockFlow. The frontend proxies `/api/v1` to the backend over the private Compose network.
4. Put a TLS reverse proxy such as Caddy or Nginx in front of the frontend. Publish only ports 80/443 externally. The app and database ports are loopback-bound by default.
5. Back up the existing MySQL database and verify that it can be restored. The MySQL volume is persistent, but it is not a backup.

## Database migration

On a new empty database, Flyway applies `V1__initial_schema.sql` and creates the three roles. Hibernate validates the resulting schema and does not alter it.

For an existing installation, do not start production against the database until the schema has been compared with V1 and a restorable backup exists. Flyway baseline is intentionally disabled. If the schema is already equivalent, explicitly baseline it at version 1 using Flyway before starting the application. If it is not equivalent, prepare a reviewed migration first. Never drop or truncate the existing database to make startup pass.

## Start and check

```bash
docker compose config --quiet
docker compose up --build -d
docker compose ps
```

The frontend and backend become healthy only after their HTTP health checks pass. Open the configured frontend URL, sign in with the bootstrap admin, and verify the end-to-end checks below. Bootstrap credentials are only used when the users table is empty; they do not reset or replace existing users.

```bash
docker compose logs --tail=100 backend frontend mysql
```

Do not paste logs or rendered Compose configuration publicly without reviewing them for sensitive details.

## Final verification

- Confirm `/api/v1/health` reports both API and database as up from the server.
- Sign in as ADMIN; verify `/api/v1/auth/me`, create test MANAGER and STAFF users, and check each role’s allowed and forbidden operations.
- Verify inventory reads, stock inbound/outbound/transfer, low-stock alerts, and audit records with isolated test records.
- Verify refresh rotates the refresh token, logout revokes the active refresh token, and old tokens no longer work.
- Test the deployed site through HTTPS, including browser refresh, session expiry, and the same-origin `/api/v1` proxy.
- Confirm backups and restore procedure, monitoring, and incident contact are ready.

## Limitations

This repository does not configure a public DNS name, TLS certificate, cloud database, automated backups, external monitoring, or CI/CD deployment. Those depend on the hosting provider and must be configured for the target environment. The included health endpoint checks database connectivity; it is not a full readiness or monitoring platform.
