# NotsHub

API documentation for frontend developers:

- [API_DOCUMENTATION.md](/D:/NotsHub/API_DOCUMENTATION.md)

## Flyway Troubleshooting

If startup fails with a Flyway validation error (checksum/description mismatch), run:

```sql
USE notshub;
SELECT installed_rank, version, description, checksum
FROM flyway_schema_history
ORDER BY installed_rank;
```

Then repair metadata (preferred) using Maven:

```powershell
.\mvnw.cmd flyway:repair `
  -Dflyway.url="jdbc:mysql://localhost:3306/notshub" `
  -Dflyway.user="root" `
  -Dflyway.password="your_password"
```

Do not edit already-applied versioned migrations in shared environments.
