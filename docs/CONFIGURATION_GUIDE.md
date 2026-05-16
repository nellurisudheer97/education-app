# Configuration Guide

## Backend Configuration (application.yml)

### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/eduapp_db
    username: root
    password: password123
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### JWT Configuration
```yaml
jwt:
  secret: your-super-secret-jwt-key-change-this-in-production
  expiration: 86400000  # 24 hours in milliseconds
```

### File Upload Configuration
```yaml
file:
  upload-dir: ./uploads
  video-dir: ./uploads/videos
  course-dir: ./uploads/courses
  book-dir: ./uploads/books
```

### Multi-part File Upload
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 500MB
      max-request-size: 500MB
```

### JPA/Hibernate Configuration
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # or 'create' for fresh start
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
```

---

## Frontend Configuration (.env)

```env
# API Configuration
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_API_TIMEOUT=30000

# Features
REACT_APP_ENABLE_CHAT=false
REACT_APP_ENABLE_NOTIFICATIONS=true

# Environment
NODE_ENV=development
```

---

## Production Considerations

### Backend (application-prod.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://prod-db-host:3306/eduapp_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

server:
  port: 8080
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

logging:
  level:
    root: WARN
    com.eduapp: INFO
```

### Frontend (Production Build)
```bash
# Build for production
npm run build

# Build output is in 'build' folder
# Deploy to CDN or static hosting
```

---

## Docker Setup (Optional)

### Dockerfile for Backend
```dockerfile
FROM openjdk:17-slim
WORKDIR /app
COPY target/educational-platform-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Dockerfile for Frontend
```dockerfile
FROM node:16-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Docker Compose (Optional)
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password123
      MYSQL_DATABASE: eduapp_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    environment:
      - DB_URL=jdbc:mysql://mysql:3306/eduapp_db

  frontend:
    build: ./frontend
    ports:
      - "3000:80"
    depends_on:
      - backend

volumes:
  mysql_data:
```

---

## SSL/TLS Configuration (Production)

### Generate Self-Signed Certificate
```bash
keytool -genkey -alias tomcat -storetype PKCS12 -keystore keystore.p12 -storepass password -validity 365
```

### Update application-prod.yml
```yaml
server:
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: password
    key-store-type: PKCS12
    key-alias: tomcat
```

---

## NGINX Configuration (Reverse Proxy)

```nginx
upstream backend {
    server localhost:8080;
}

upstream frontend {
    server localhost:3000;
}

server {
    listen 80;
    server_name yourdomain.com;

    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    # Frontend
    location / {
        proxy_pass http://frontend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # API
    location /api/ {
        proxy_pass http://backend/api/;
        proxy_set_header Authorization $http_authorization;
    }
}
```

---

## Environment Variables Summary

### Required
- `DB_URL` - Database connection URL
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `JWT_SECRET` - JWT secret key

### Optional
- `SERVER_PORT` - Server port (default: 8080)
- `LOG_LEVEL` - Logging level (default: INFO)
- `FILE_UPLOAD_DIR` - File upload directory (default: ./uploads)

---

## Security Best Practices

1. **Never commit secrets** - Use environment variables
2. **Rotate JWT secrets** - Change in production
3. **Use HTTPS** - Enable SSL/TLS in production
4. **Database backups** - Regular automated backups
5. **Rate limiting** - Implement API rate limiting
6. **Input validation** - Validate all user inputs
7. **CORS settings** - Restrict to known domains
8. **Dependency updates** - Keep libraries updated

---

## Monitoring & Logging

### Backend Logs Configuration
```yaml
logging:
  level:
    root: WARN
    com.eduapp: INFO
  file:
    name: logs/application.log
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d %p %c{1.} [%t] %m%n"
```

---

## Performance Optimization

### Database
- Use connection pooling
- Index frequently queried columns
- Archive old data
- Regular maintenance

### Frontend
- Code splitting
- Lazy loading
- Image optimization
- Caching strategies

### Cache (Redis - Optional)
```yaml
spring:
  cache:
    type: redis
  redis:
    host: localhost
    port: 6379
```

---

This configuration guide covers development through production deployment!
