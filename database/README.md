# Database

`schema.sql` is generated from the local dump with data rows removed. It should contain table/function structure only, not account/player data.

Import locally:

```sql
CREATE DATABASE nroserver CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE nroserver;
SOURCE database/schema.sql;
```

Add your own seed data/runtime templates before running the server.
