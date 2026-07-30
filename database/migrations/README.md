# Database Migrations

Put offline world database patches here as `.sql` files.

Naming convention:

```text
YYYYMMDD_short_description.sql
```

Rules:

- Migrations run in filename order after `offline_seed.sql` is imported.
- Make each migration idempotent when possible, for example `CREATE TABLE IF NOT EXISTS` or `ALTER TABLE ...` guarded by checks.
- Do not edit a migration after publishing it. Create a new file instead.
- The launcher stores applied migration checksums per world under `saves\<WorldName>\.migrations` and also records them in `offline_schema_migrations`.
