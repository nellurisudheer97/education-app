# API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication
All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer {token}
```

---

## 1. Authentication Endpoints

### Register
**POST** `/auth/register`

Request Body:
```json
{
  "email": "user@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "role": "STUDENT"
}
```

Response:
```json
{
  "token": "jwt_token_here",
  "message": "Registration successful",
  "userId": 1,
  "role": "STUDENT",
  "fullName": "John Doe"
}
```

### Login
**POST** `/auth/login`

Request Body:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

Response:
```json
{
  "token": "jwt_token_here",
  "message": "Login successful",
  "userId": 1,
  "role": "STUDENT",
  "fullName": "John Doe"
}
```

---

## 2. Course Endpoints

### Get All Published Courses
**GET** `/courses`

Response:
```json
[
  {
    "id": 1,
    "title": "Web Development 101",
    "description": "Learn web development basics",
    "thumbnailUrl": "thumbnail.jpg",
    "instructorId": 2,
    "instructorName": "Jane Instructor",
    "isPublished": true,
    "totalLessons": 10
  }
]
```

### Get Course by ID
**GET** `/courses/{courseId}`

Response:
```json
{
  "id": 1,
  "title": "Web Development 101",
  "description": "Learn web development basics",
  "thumbnailUrl": "thumbnail.jpg",
  "instructorId": 2,
  "instructorName": "Jane Instructor",
  "isPublished": true,
  "totalLessons": 10
}
```

### Get Instructor's Courses
**GET** `/courses/instructor/{instructorId}`

Response: Same as Get All Courses

### Create Course
**POST** `/courses?instructorId={instructorId}` (Admin/Developer only)

Request Body:
```json
{
  "title": "New Course",
  "description": "Course description",
  "thumbnailUrl": "thumbnail.jpg",
  "isPublished": false
}
```

### Update Course
**PUT** `/courses/{courseId}` (Admin/Developer only)

Request Body:
```json
{
  "title": "Updated Title",
  "description": "Updated description",
  "isPublished": true
}
```

### Delete Course
**DELETE** `/courses/{courseId}` (Admin/Developer only)

---

## 3. Lesson Endpoints

### Get Lessons by Course
**GET** `/lessons/course/{courseId}`

Response:
```json
[
  {
    "id": 1,
    "title": "Introduction",
    "content": "Lesson content here",
    "videoUrl": "video.mp4",
    "resourceUrl": "resource.pdf",
    "courseId": 1,
    "orderIndex": 1
  }
]
```

### Get Lesson by ID
**GET** `/lessons/{lessonId}`

### Create Lesson
**POST** `/lessons` (Admin/Developer only)

Request Body:
```json
{
  "title": "New Lesson",
  "content": "Lesson content",
  "videoUrl": "video.mp4",
  "resourceUrl": "resource.pdf",
  "courseId": 1,
  "orderIndex": 1
}
```

### Update Lesson
**PUT** `/lessons/{lessonId}` (Admin/Developer only)

### Delete Lesson
**DELETE** `/lessons/{lessonId}` (Admin/Developer only)

---

## 4. Quiz Endpoints

### Get Quizzes by Lesson
**GET** `/quizzes/lesson/{lessonId}`

Response:
```json
[
  {
    "id": 1,
    "title": "Quiz 1",
    "description": "Chapter 1 Quiz",
    "lessonId": 1,
    "totalQuestions": 5,
    "passingScore": 70
  }
]
```

### Get Quiz by ID
**GET** `/quizzes/{quizId}`

### Create Quiz
**POST** `/quizzes` (Admin/Developer only)

Request Body:
```json
{
  "title": "Quiz 1",
  "description": "Chapter 1 Quiz",
  "lessonId": 1,
  "totalQuestions": 5,
  "passingScore": 70
}
```

### Update Quiz
**PUT** `/quizzes/{quizId}` (Admin/Developer only)

### Delete Quiz
**DELETE** `/quizzes/{quizId}` (Admin/Developer only)

---

## 5. File Upload Endpoints

### Upload Video
**POST** `/upload/video`

Form Data:
- `file`: File (video/mp4)

Response:
```
filename.mp4
```

### Upload Course Material
**POST** `/upload/course`

Form Data:
- `file`: File (application/pdf or similar)

### Upload Book
**POST** `/upload/book`

Form Data:
- `file`: File (application/pdf)

### Upload Thumbnail
**POST** `/upload/thumbnail`

Form Data:
- `file`: File (image/jpeg, image/png)

### Download File
**GET** `/upload/{type}/{filename}`

Parameters:
- `type`: video, course, book, or thumbnail
- `filename`: Filename returned from upload

---

## Error Responses

### 400 Bad Request
```json
{
  "message": "Invalid input"
}
```

### 401 Unauthorized
```json
{
  "message": "Invalid credentials"
}
```

### 403 Forbidden
```json
{
  "message": "Access denied"
}
```

### 404 Not Found
```json
{
  "message": "Resource not found"
}
```

### 500 Internal Server Error
```json
{
  "message": "Internal server error"
}
```

---

## Status Codes

- **200** OK - Request successful
- **201** Created - Resource created
- **400** Bad Request - Invalid input
- **401** Unauthorized - Authentication required
- **403** Forbidden - Insufficient permissions
- **404** Not Found - Resource not found
- **500** Internal Server Error

---

# Frontend Setup Guide

## Installation

```bash
npm install
```

## Environment Variables

Create a `.env` file in the frontend directory:
```
REACT_APP_API_URL=http://localhost:8080/api
```

## Development

```bash
npm start
```

## Production Build

```bash
npm run build
```

## Authentication Flow

1. User registers or logs in
2. JWT token is returned and stored in localStorage
3. Token is automatically included in all API requests
4. Token expires after configured time period
5. User must re-login after expiration
