# Docker Setup for Stampede V2

## Prerequisites
- Docker Desktop installed and running

## 1. Start PostgreSQL

```bash
docker run --name stampede-db \
  -e POSTGRES_DB=stampede \
  -e POSTGRES_USER=stampede \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 \
  -d postgres:16
```

## 2. Verify it's running

```bash
docker ps
```

You should see `stampede-db` in the list with status `Up`.

## 3. Connect to the DB (optional, to inspect tables)

```bash
docker exec -it stampede-db psql -U stampede -d stampede
```

Inside psql:
```sql
\dt              -- list all tables
SELECT * FROM products;
SELECT * FROM orders;
\q               -- quit
```

## 4. Stop and start the container

```bash
docker stop stampede-db    # pause (data is preserved)
docker start stampede-db   # resume
```

## 5. Reset everything (wipe all data)

```bash
docker stop stampede-db
docker rm stampede-db
# then re-run step 1
```

## Connection details (for application.yml)

```
host:     localhost
port:     5432
database: stampede
username: stampede
password: password
```
