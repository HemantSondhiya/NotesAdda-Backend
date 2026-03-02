# NotsHub API Documentation (Frontend Guide)

## Base URL
- Local: `http://localhost:9091`

## Auth Overview
- Auth endpoints are under `/api/auth`.
- Protected endpoints require JWT authentication.
- JWT can be sent as:
  - `Authorization: Bearer <token>`
  - or auth cookie returned by signin (if your client uses cookies).

## Roles
- `ROLE_STUDENT`
- `ROLE_FACULTY`
- `ROLE_UNIVERSITY_ADMIN`
- `ROLE_SUPER_ADMIN`

Admin-only endpoints require:
- `ROLE_UNIVERSITY_ADMIN` or `ROLE_SUPER_ADMIN`

## Common Response Wrappers

### Standard API response
```json
{
  "message": "text",
  "status": true,
  "data": {}
}
```

### Paginated response (`data`)
```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "last": true
}
```

## Error Format
- Validation errors (`400`): field-message map
```json
{
  "fieldName": "validation message"
}
```
- Business errors (`APIException`, `400`):
```json
{
  "message": "error message",
  "status": false,
  "data": null
}
```

---

## Auth APIs

### `POST /api/auth/signup`
- Public
- Body:
```json
{
  "username": "admin1",
  "email": "admin1@nots.com",
  "password": "Admin@123",
  "role": ["super_admin"]
}
```

### `POST /api/auth/signin`
- Public
- Body:
```json
{
  "username": "admin1",
  "password": "Admin@123"
}
```
- Response includes user info + token, and sets JWT cookie.

### `POST /api/auth/signout`
- Public

### `GET /api/auth/user`
- Authenticated

### `GET /api/auth/username`
- Authenticated

---

## University APIs

### `GET /api/universities?page=0&size=20`
- Public
- Returns paginated universities with nested hierarchy:
  - programs -> branches -> semesters -> subjects -> notes

### `POST /api/universities`
- Admin only
- Body:
```json
{
  "name": "VIT Vellore",
  "code": "VIT-V",
  "city": "Vellore",
  "state": "Tamil Nadu",
  "logoUrl": ""
}
```

### `PUT /api/universities/{id}`
- Admin only
- Same body as create.

### `DELETE /api/universities/{id}`
- Admin only

---

## Program APIs

### `GET /api/programs?page=0&size=20`
- Public

### `GET /api/programs/{id}`
- Public

### `GET /api/programs/university/{universityId}?page=0&size=20`
- Public

### `POST /api/programs`
- Admin only
- Body:
```json
{
  "name": "BTech",
  "type": "UG",
  "duration": 4,
  "universityId": "23ef26a9-b52b-4771-8f32-82946733138f"
}
```

### `PUT /api/programs/{id}`
- Admin only
- Same body as create.

### `DELETE /api/programs/{id}`
- Admin only

---

## Branch APIs

### `GET /api/branches?page=0&size=20`
- Authenticated

### `GET /api/branches/{id}`
- Authenticated

### `POST /api/branches`
- Admin only
- Body:
```json
{
  "name": "Computer Science Engineering",
  "code": "CSE",
  "programId": "831d3c6a-9c13-4b89-8894-a12178db5e40"
}
```

### `PUT /api/branches/{id}`
- Admin only
- Same body as create.

### `DELETE /api/branches/{id}`
- Admin only

---

## Semester APIs

### `GET /api/semesters?page=0&size=20`
- Authenticated

### `POST /api/semesters`
- Admin only
- Body:
```json
{
  "number": 1,
  "branchId": "cfb2de06-a402-48b4-b405-7e10325932cf"
}
```

### `PUT /api/semesters/{id}`
- Admin only
- Same body as create.

### `DELETE /api/semesters/{id}`
- Admin only

---

## Subject APIs

### `GET /api/subjects?page=0&size=20`
- Authenticated

### `GET /api/subjects/{id}`
- Authenticated

### `POST /api/subjects`
- Admin only
- Body:
```json
{
  "name": "Data Structures",
  "code": "CS301",
  "credits": 4,
  "syllabusUrl": "https://example.com/syllabus.pdf",
  "semesterId": "b9585fed-9b5d-4194-b1fe-aabb9725e491"
}
```

### `PUT /api/subjects/{id}`
- Admin only
- Same body as create.

### `DELETE /api/subjects/{id}`
- Admin only

---

## Notes APIs

### `GET /api/notes?page=0&size=20`
- Authenticated

### `POST /api/notes`
- Authenticated user upload
- Body:
```json
{
  "title": "Unit 1 Notes - Data Structures",
  "description": "Linked list and stack basics",
  "fileUrl": "https://example.com/files/ds-unit1.pdf",
  "fileKey": "notes/ds-unit1.pdf",
  "fileType": "PDF",
  "subjectId": "a9a6f01b-ed60-4cc5-8a0b-af0bf5137692"
}
```
- Behavior:
  - Admin uploader: auto-approved
  - Non-admin uploader: pending approval

### `PUT /api/notes/{id}`
- Admin only
- Same body as create.

### `PUT /api/notes/{id}/approve`
- Admin only
- No body required.

### `DELETE /api/notes/{id}`
- Admin only

---

## Frontend Integration Notes
- Always include auth token/cookie for protected endpoints.
- Treat `APIResponse.status` as business success flag.
- For lists, read from `data.content` (paginated wrapper).
- Use `page` and `size` query params for all paginated list endpoints.
- Notes file upload itself is external to this API (API stores metadata URL/key).
