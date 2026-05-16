# Development Setup Guide

## Quick Start

### Prerequisites
- JDK 17
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+

### Backend Setup (5 minutes)

1. **Start MySQL**
   ```bash
   mysql -u root -p
   # Run commands from docs/DATABASE_SETUP.md
   ```

2. **Configure Backend**
   - Edit `backend/src/main/resources/application.yml`
   - Update database credentials

3. **Build & Run**
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```
   Backend starts on: `http://localhost:8080/api`

### Frontend Setup (5 minutes)

1. **Install Dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Start Dev Server**
   ```bash
   npm start
   ```
   Frontend starts on: `http://localhost:3000`

---

## Project Features

### ✅ Implemented

- [x] User authentication (JWT)
- [x] Role-based access control
- [x] Course CRUD operations
- [x] Lesson management
- [x] Quiz system
- [x] File upload (videos, materials, books)
- [x] User progress tracking
- [x] Responsive UI
- [x] Admin dashboard

### 🚀 Ready to Add

- [ ] Real-time chat
- [ ] Video streaming optimization
- [ ] Mobile app
- [ ] Payment integration
- [ ] Advanced analytics
- [ ] Email notifications
- [ ] Certificates

---

## File Structure Summary

```
project/
├── backend/
│   ├── pom.xml (Dependencies)
│   ├── src/main/java/com/eduapp/
│   │   ├── model/ (Database entities)
│   │   ├── repository/ (Data access)
│   │   ├── service/ (Business logic)
│   │   ├── controller/ (REST APIs)
│   │   ├── dto/ (Data objects)
│   │   └── security/ (JWT utils)
│   └── src/main/resources/
│       └── application.yml (Configuration)
│
├── frontend/
│   ├── package.json
│   └── src/
│       ├── pages/ (Page components)
│       ├── components/ (Reusable components)
│       ├── services/ (API calls)
│       ├── styles/ (CSS)
│       ├── App.jsx (Main component)
│       └── index.js (Entry point)
│
├── uploads/ (File storage)
│   ├── courses/
│   ├── videos/
│   ├── books/
│   └── thumbnails/
│
└── docs/ (Documentation)
    ├── DATABASE_SETUP.md
    └── API_DOCUMENTATION.md
```

---

## Testing the Application

### Test Users (from DATABASE_SETUP.md)
- **Email**: admin@eduapp.com / instructor@eduapp.com / student@eduapp.com
- **Password**: password123 (if using default from setup script)

### Manual Testing Steps

1. **Register a new account**
   - Go to `/register`
   - Fill in details and select role
   - Click register

2. **Login**
   - Go to `/login`
   - Use credentials from step 1
   - Dashboard should load

3. **Create a course (as Admin/Developer)**
   - Click "+ Create Course"
   - Fill course details
   - Course appears in dashboard

4. **Add lessons to course**
   - Click on course
   - Click "+ Add Lesson"
   - Add lesson details

5. **Test file upload**
   - Upload videos and resources
   - Verify files are saved

---

## Troubleshooting

### Backend Issues

**Port 8080 already in use**
```bash
lsof -i :8080
kill -9 <PID>
```

**Database connection error**
- Verify MySQL is running
- Check credentials in application.yml
- Ensure database `eduapp_db` exists

**Build fails**
```bash
mvn clean
mvn install
```

### Frontend Issues

**Module not found**
```bash
rm -rf node_modules package-lock.json
npm install
```

**Port 3000 already in use**
```bash
npm start -- --port 3001
```

**API not responding**
- Check backend is running on 8080
- Check CORS settings in backend
- Check browser console for errors

---

## Environment Variables

### Backend (application.yml)
```yaml
spring.datasource.url=jdbc:mysql://localhost:3306/eduapp_db
spring.datasource.username=root
spring.datasource.password=your_password
jwt.secret=your_jwt_secret_key
jwt.expiration=86400000
file.upload-dir=./uploads
```

### Frontend (.env)
```
REACT_APP_API_URL=http://localhost:8080/api
```

---

## Deployment

### Build for Production

**Backend**
```bash
cd backend
mvn clean package
java -jar target/educational-platform-backend-1.0.0.jar
```

**Frontend**
```bash
cd frontend
npm run build
# Deploy 'build' folder to static hosting
```

---

## Performance Tips

1. **Index frequently queried columns** ✅ Already done in DB setup
2. **Use pagination for large datasets** - Add in future updates
3. **Cache course data** - Add Redis integration
4. **Optimize video streaming** - Use CDN
5. **Compress images** - Optimize thumbnails

---

## Security Checklist

- [x] JWT authentication
- [x] Password encryption (BCrypt)
- [x] CORS configured
- [x] Role-based access control
- [ ] Rate limiting (TODO)
- [ ] SSL/TLS (TODO for production)
- [ ] Input validation (needs enhancement)
- [ ] SQL injection prevention (Using JPA)

---

## Next Steps

1. ✅ Basic setup complete
2. Start adding test data
3. Create more features based on requirements
4. Set up CI/CD pipeline
5. Deploy to production

---

Happy Coding! 🚀
