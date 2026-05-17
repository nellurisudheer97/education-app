# 🎓 Educational Platform

A full-stack educational application built with **Spring Boot** (backend), **React** (frontend), and **MySQL** (database).

## Features

✅ **Course Management** - Create, edit, and publish courses  
✅ **Video Streaming** - Upload and stream educational videos  
✅ **Quiz System** - Create quizzes with multiple-choice questions  
✅ **User Progress Tracking** - Track student completion and performance  
✅ **Admin Dashboard** - Powerful dashboard for course uploads and management  
✅ **Role-Based Access** - Student, Admin, and Developer roles  
✅ **File Upload** - Upload courses, videos, books, and resources  

## Project Structure

```
project/
├── backend/                 # Spring Boot application
│   ├── pom.xml
│   ├── src/main/java/com/eduapp/
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Data access layer
│   │   ├── service/        # Business logic
│   │   ├── controller/     # REST endpoints
│   │   ├── dto/            # Data transfer objects
│   │   └── security/       # JWT authentication
│   └── src/main/resources/
│       └── application.yml # Configuration
│
├── frontend/                # React application
│   ├── package.json
│   ├── src/
│   │   ├── components/     # Reusable components
│   │   ├── pages/          # Page components
│   │   ├── services/       # API service
│   │   ├── styles/         # CSS styles
│   │   ├── App.jsx
│   │   └── index.js
│   └── public/
│
├── uploads/                 # File storage
│   ├── courses/
│   ├── videos/
│   ├── books/
│   └── thumbnails/
│
└── docs/                    # Documentation
```

## Tech Stack

### Backend
- Spring Boot 3.1.5
- Spring Data JPA
- Spring Security
- JWT Authentication
- MySQL
- Maven

### Frontend
- React 18
- React Router v6
- Axios
- CSS/SASS

## Setup Instructions

### Prerequisites
- JDK 17+
- Maven 3.6+
- Node.js 16+
- npm 7+
- MySQL 8.0+

### Backend Setup

1. **Create Database**
```sql
CREATE DATABASE eduapp_db;
```

2. **Update Configuration**
Edit `backend/src/main/resources/application.yml`:
```yaml
datasource:
  url: jdbc:mysql://localhost:3306/eduapp_db
  username: root
  password: your_password
```

3. **Build and Run**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080/api`

### Frontend Setup

1. **Install Dependencies**
```bash
cd frontend
npm install
```

2. **Update API URL** (if needed)
Edit `src/services/api.js` if backend is running on different port

3. **Start Development Server**
```bash
npm start
```

The frontend will start on `http://localhost:3000`

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login

### Courses
- `GET /api/courses` - Get all published courses
- `GET /api/courses/{id}` - Get course details
- `POST /api/courses` - Create new course (Admin/Developer only)
- `PUT /api/courses/{id}` - Update course
- `DELETE /api/courses/{id}` - Delete course

### Lessons
- `GET /api/lessons/course/{courseId}` - Get all lessons for a course
- `POST /api/lessons` - Create lesson
- `PUT /api/lessons/{id}` - Update lesson
- `DELETE /api/lessons/{id}` - Delete lesson

### Quizzes
- `GET /api/quizzes/lesson/{lessonId}` - Get quizzes for a lesson
- `POST /api/quizzes` - Create quiz
- `PUT /api/quizzes/{id}` - Update quiz
- `DELETE /api/quizzes/{id}` - Delete quiz

### File Upload
- `POST /api/upload/video` - Upload video
- `POST /api/upload/course` - Upload course material
- `POST /api/upload/book` - Upload book/PDF
- `POST /api/upload/thumbnail` - Upload thumbnail

## User Roles

1. **Student**
   - View published courses
   - Watch videos
   - Complete quizzes
   - Track progress

2. **Admin**
   - Create and manage courses
   - Upload videos and materials
   - Create quizzes
   - View student progress

3. **Developer**
   - Same as Admin
   - System configuration
   - Content management

## Database Schema

### Tables
- `users` - User accounts
- `courses` - Course information
- `lessons` - Course lessons
- `quizzes` - Quiz questions
- `questions` - Individual quiz questions
- `user_progress` - Student progress tracking
- `quiz_results` - Quiz attempt results

## Default Credentials (for testing)

After running migrations, you can create test users through the register endpoint.

## Security Features

✅ JWT Authentication  
✅ Password Encryption (BCrypt)  
✅ Role-Based Access Control  
✅ CORS Configuration  
✅ Secure File Upload  




