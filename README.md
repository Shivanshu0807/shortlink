# ShortLink - URL Shortener (Spring Boot)

A backend URL shortener service. Paste a long URL, get a short link, and get redirected when you open it. Supports optional link expiry.

## Features
- Create short link: `POST /api/urls`
- Redirect: `GET /{shortCode}` returns `302` to the original URL
- Optional expiry (`expiresInDays`); expired links return `410 Gone`
- Unknown code returns `404`, invalid input returns `400`
- Only `http` / `https` URLs are accepted (blocks `javascript:` and similar schemes)

## Tech Stack
Java 17, Spring Boot 4, Spring Data JPA (Hibernate), MySQL 8, Bean Validation, Lombok, Maven

## How the short code works
1. Save the URL row, so MySQL generates an auto-increment `id`.
2. Convert the `id` to **Base62** (`0-9a-zA-Z`), e.g. `125` becomes `21`.
3. Store it in a unique `short_code` column (same transaction).

No random collisions, no retry loops, and codes stay short.

## Project Structure
```
controller  ->  service  ->  repository  ->  MySQL
   dto (request/response)     entity     util (Base62)
```

## Run Locally

**1. Create the database and user (MySQL)**
```sql
CREATE DATABASE shortlink_db;
CREATE USER 'shortlink_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON shortlink_db.* TO 'shortlink_user'@'localhost';
```

**2. Set the DB password as an environment variable**
```
# Windows PowerShell
$env:DB_PASSWORD="your_password"

# Linux / macOS
export DB_PASSWORD=your_password
```

**3. Start the app**
```
./mvnw spring-boot:run
```
App runs on `http://localhost:8080`.

## API

**Create a short link**
```
POST /api/urls
Content-Type: application/json

{ "originalUrl": "https://github.com/Shivanshu0807", "expiresInDays": 7 }
```
Response `201 Created`
```json
{
  "shortCode": "1",
  "shortUrl": "http://localhost:8080/1",
  "originalUrl": "https://github.com/Shivanshu0807"
}
```

**Open a short link**
```
GET /1   ->   302 Found, Location: https://github.com/Shivanshu0807
```

| Status | When |
|--------|------|
| 201 | Short link created |
| 302 | Redirect to original URL |
| 400 | Blank / invalid / non-http(s) URL |
| 404 | Short code does not exist |
| 410 | Link has expired |

## Roadmap
- [ ] JWT authentication (signup / login, per-user links)
- [ ] Redis caching for redirects + rate limiting
- [ ] Click analytics (date-wise, device-wise)
- [ ] Swagger / OpenAPI docs
- [ ] Unit and integration tests

## Author
**Shivanshu Jaiswal** - [GitHub](https://github.com/Shivanshu0807)