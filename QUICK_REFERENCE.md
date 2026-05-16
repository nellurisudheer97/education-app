# Quick Reference Guide

## Common Tasks

### 🚀 Start Development

**Terminal 1: Start Backend**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

**Terminal 2: Start Frontend**
```bash
cd frontend
npm install
npm start
```

**Terminal 3: MySQL (if needed)**
```bash
mysql -u root -p
# Run setup commands from docs/DATABASE_SETUP.md
```

---

### 🔐 Login Test

1. Go to `http://localhost:3000`
2. Click "Register here"
3. Create account with role ADMIN or DEVELOPER
4. You can now create courses

---

### ➕ Add New Course

1. After login, click "+ Create Course"
2. Enter course title and description
3. Course created with unpublished status
4. Click on course to add lessons

---

### 📝 Add Lesson to Course

1. Open a course
2. Click "+ Add Lesson"
3. Enter lesson details:
   - Title
   - Content (text)
   - Video URL (optional)
4. Lesson added to course

---

### 📤 Upload Files

**Backend processes file uploads via:**
- POST `/api/upload/video` → Videos
- POST `/api/upload/course` → Course materials
- POST `/api/upload/book` → Books/PDFs
- POST `/api/upload/thumbnail` → Thumbnails

**Files stored in:**
- `./uploads/videos/`
- `./uploads/courses/`
- `./uploads/books/`
- `./uploads/thumbnails/`

---

## API Quick Reference

### Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email":"user@test.com",
    "password":"pass123",
    "fullName":"Test User",
    "role":"STUDENT"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email":"user@test.com",
    "password":"pass123"
  }'
```

### Get All Courses
```bash
curl http://localhost:8080/api/courses
```

### Create Course
```bash
curl -X POST http://localhost:8080/api/courses?instructorId=1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title":"React Basics",
    "description":"Learn React fundamentals",
    "isPublished":false
  }'
```

### Upload Video
```bash
curl -X POST http://localhost:8080/api/upload/video \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@video.mp4"
```

---

## Database Quick Reference

### Connect to MySQL
```bash
mysql -u root -p eduapp_db
```

### Check Tables
```sql
SHOW TABLES;
```

### View Users
```sql
SELECT id, email, full_name, role FROM users;
```

### View Courses
```sql
SELECT id, title, instructor_id, is_published FROM courses;
```

### View Lessons
```sql
SELECT id, title, course_id FROM lessons;
```

### Reset Database
```sql
DROP DATABASE eduapp_db;
-- Then run setup commands again
```

---

## Frontend Quick Fixes

### Clear Cache
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

### Clear Browser Storage
```javascript
// Open DevTools Console
localStorage.clear();
sessionStorage.clear();
location.reload();
```

### Enable Debug Mode
Edit `frontend/src/App.jsx`:
```javascript
console.log('Debug: ', variable);
```

---

## Backend Quick Fixes

### Clean Build
```bash
cd backend
mvn clean
mvn install
```

### Rebuild Without Tests
```bash
mvn clean install -DskipTests
```

### Update Dependencies
```bash
mvn dependency:update-recursive
```

### Check Running Processes
```bash
# Linux/Mac
lsof -i :8080

# Windows
netstat -ano | findstr :8080
```

### Kill Process on Port 8080
```bash
# Linux/Mac
kill -9 <PID>

# Windows
taskkill /PID <PID> /F
```

---

## Common Errors & Solutions

### Error: Port 8080 Already in Use
```bash
# Find what's using port 8080
lsof -i :8080
# Kill the process
kill -9 <PID>
```

### Error: Database Connection Failed
```yaml
# Check in application.yml:
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/eduapp_db
    username: root  # Verify this matches MySQL user
    password: your_password  # Verify password
```

### Error: JWT Token Invalid
- Token might be expired
- Re-login to get new token
- Check JWT_SECRET in application.yml

### Error: Access Denied (Forbidden)
- Check user role (ADMIN, DEVELOPER, or STUDENT required)
- Verify authorization header in requests
- Check JWT token is included in Authorization header

### Error: File Upload Failed
- Check `/uploads` directory exists
- Verify write permissions on directory
- Check max-file-size in application.yml
- Verify file format is allowed

---

## Development Tips

### 1. Enable Debug Logging
```yaml
logging:
  level:
    com.eduapp: DEBUG
    org.springframework.web: DEBUG
```

### 2. Use REST Client (VS Code)
Create `requests.rest`:
```
### Get Courses
GET http://localhost:8080/api/courses

### Create Course
POST http://localhost:8080/api/courses?instructorId=1
Content-Type: application/json
Authorization: Bearer eyJhbGc...

{
  "title": "New Course",
  "description": "Description here"
}
```

### 3. Use Postman
- Import API collection
- Save environment variables
- Create test requests
- Set up automated tests

### 4. Use Browser DevTools
- Network tab to inspect API calls
- Console for JavaScript errors
- Storage tab for token/data
- Application tab for localStorage

---

## Testing Checklist

- [ ] User can register
- [ ] User can login
- [ ] User can create course (as Admin)
- [ ] Course appears in dashboard
- [ ] Can add lesson to course
- [ ] Can view lesson content
- [ ] Videos play (if added)
- [ ] Can upload files
- [ ] Can manage students
- [ ] Progress tracking works

---

## Deployment Checklist

**Before Deploying:**
- [ ] Update JWT secret in production
- [ ] Change database credentials
- [ ] Enable HTTPS/SSL
- [ ] Configure CORS for production domain
- [ ] Set NODE_ENV=production
- [ ] Optimize images
- [ ] Minify frontend code
- [ ] Run security scan
- [ ] Test SSL certificate
- [ ] Setup database backups

---

## Performance Tips

1. **Add database indexes**
   ```sql
   CREATE INDEX idx_course_instructor ON courses(instructor_id);
   ```

2. **Use pagination**
   - Limit results to 20-50 items
   - Add offset parameter

3. **Cache frequently accessed data**
   - Use Redis for sessions
   - Cache popular courses

4. **Optimize images**
   - Use WebP format
   - Compress thumbnails
   - Lazy load images

5. **Code splitting**
   - Load components on demand
   - Reduce bundle size

---

## Useful Links

- Spring Boot Docs: https://spring.io/projects/spring-boot
- React Docs: https://react.dev
- MySQL Docs: https://dev.mysql.com/doc/
- JWT.io: https://jwt.io/
- Postman: https://www.postman.com/

---

## Need Help?

1. Check `docs/` folder for detailed guides
2. Review error messages in console
3. Check logs in backend
4. Use browser DevTools
5. Search error in documentation

---

**Last Updated**: May 10, 2026
**Version**: 1.0.0
