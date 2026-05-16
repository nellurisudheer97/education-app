# Plain Password Storage for Development - Setup Guide

## ✅ What's Implemented

You can now store and retrieve plain text passwords in the database for **development/debugging purposes only**.

### Components Added:

1. **User Model Update** - `User.java`
   - New field: `plainPassword` (nullable)
   - Stores the plain text password during registration and password reset

2. **Database Column** - `plain_password`
   - Added to `users` table
   - Indexed for faster queries during development

3. **Developer Endpoint** - `DeveloperCredentialsController.java`
   - `/dev/credentials/{email}` - Get credentials for a specific user
   - `/dev/credentials/all` - Get all user credentials
   - Only accessible to users with DEVELOPER role

4. **DTO** - `UserCredentialsDTO.java`
   - Returns: email, plain password, full name, and role

---

## 📋 Setup Instructions

### Step 1: Run Database Migration

Execute this SQL in your MySQL database:

```sql
USE eduapp_db;

-- Add plain_password column
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS plain_password VARCHAR(255) NULL;

-- Create index for faster queries
CREATE INDEX idx_plain_password ON users(plain_password);
```

### Step 2: Restart the Application

The new column will be automatically recognized by Hibernate.

### Step 3: Register or Update Test Users

New users will automatically have their plain password stored when they register:

```
POST /auth/register
{
  "email": "testuser@example.com",
  "password": "MyPassword123",
  "fullName": "Test User"
}
```

The database will now contain:
- **password**: `$2a$10$...` (encrypted BCrypt)
- **plain_password**: `MyPassword123` (plain text for reference)

---

## 🔍 How to Retrieve Passwords

### Method 1: Database Query (Direct)

```sql
SELECT email, plain_password, full_name, role 
FROM users 
WHERE email = 'your-email@example.com';
```

### Method 2: API Endpoint (Requires DEVELOPER Role)

First, create a user with DEVELOPER role:

```sql
INSERT INTO users (email, password, plain_password, full_name, role, is_active)
VALUES (
  'developer@eduapp.com',
  '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ',
  'devpassword123',
  'Developer User',
  'DEVELOPER',
  true
);
```

Then login as developer and call:

```bash
# Get specific user credentials
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/dev/credentials/student@example.com

# Response:
{
  "email": "student@example.com",
  "plainPassword": "StudentPassword123",
  "fullName": "Student User",
  "role": "STUDENT"
}
```

```bash
# Get all user credentials (be careful!)
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/dev/credentials/all

# Response:
[
  {
    "email": "admin@example.com",
    "plainPassword": "AdminPassword123",
    "fullName": "Admin User",
    "role": "ADMIN"
  },
  {
    "email": "student@example.com",
    "plainPassword": "StudentPassword123",
    "fullName": "Student User",
    "role": "STUDENT"
  }
]
```

---

## 🔐 Security Considerations

⚠️ **IMPORTANT - FOR DEVELOPMENT ONLY!**

1. **Never use in production** - Remove `DeveloperCredentialsController.java` before deploying
2. **Limit access** - Only DEVELOPER role can access the credentials endpoints
3. **Database backups** - Never commit database backups with plain passwords to version control
4. **Frontend** - Never send plain passwords to frontend - only use server-side debugging

### How to Disable Before Production:

```bash
# Option 1: Delete the controller file
rm backend/src/main/java/com/eduapp/controller/DeveloperCredentialsController.java

# Option 2: Remove/comment the plainPassword field from User.java
# (Keep it nullable to avoid migration issues)
```

---

## 📝 Troubleshooting Login Issues

### Common Issues & How to Debug:

#### Issue: "Invalid email or password"

1. Check if the user exists:
   ```sql
   SELECT email, plain_password FROM users WHERE email = 'user@example.com';
   ```

2. Compare what you're typing with what's in the database:
   - **Email**: Must match exactly (case-insensitive handling in backend)
   - **Password**: Must match the `plain_password` field exactly

3. Debug tips:
   - Check server logs for authentication attempts
   - Verify user account is active: `is_active = true`
   - Check user role: `role` should be STUDENT, ADMIN, or DEVELOPER

#### Issue: Can't remember encrypted password

Simply use the plain text password from the `plain_password` column!

```sql
-- Find your password
SELECT email, plain_password FROM users WHERE email = 'your-email@example.com';

-- Output shows: student@eduapp.com | SecurePass123
-- Use "SecurePass123" to login
```

---

## 🔄 Migration Flow

### Registration:
```
User enters password "MyPass123" 
    ↓
Backend receives: LoginRequest { email, password }
    ↓
Encrypts password: BCrypt hash
    ↓
Stores in database:
  - password: "$2a$10$..." (encrypted)
  - plain_password: "MyPass123" (plain text)
    ↓
User can now login with "MyPass123"
```

### Password Reset:
```
User requests password reset
    ↓
User enters new password "NewPass456"
    ↓
Backend receives: ResetPasswordRequest { token, newPassword }
    ↓
Encrypts new password: BCrypt hash
    ↓
Stores in database:
  - password: "$2a$10$..." (new encrypted)
  - plain_password: "NewPass456" (new plain text)
    ↓
User can now login with "NewPass456"
```

---

## ⚡ Quick Reference for Testing

### Create Test Users:

```sql
-- Test Student
INSERT INTO users (email, password, plain_password, full_name, role, is_active) VALUES (
  'student@test.com',
  '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ',
  'StudentTest123',
  'Test Student',
  'STUDENT',
  true
);

-- Test Instructor
INSERT INTO users (email, password, plain_password, full_name, role, is_active) VALUES (
  'instructor@test.com',
  '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ',
  'InstructorTest123',
  'Test Instructor',
  'INSTRUCTOR',
  true
);

-- Test Admin
INSERT INTO users (email, password, plain_password, full_name, role, is_active) VALUES (
  'admin@test.com',
  '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ',
  'AdminTest123',
  'Test Admin',
  'ADMIN',
  true
);

-- Test Developer
INSERT INTO users (email, password, plain_password, full_name, role, is_active) VALUES (
  'developer@test.com',
  '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ',
  'DeveloperTest123',
  'Test Developer',
  'DEVELOPER',
  true
);
```

Then login with:
- Email: `student@test.com` | Password: `StudentTest123`
- Email: `instructor@test.com` | Password: `InstructorTest123`
- Email: `admin@test.com` | Password: `AdminTest123`
- Email: `developer@test.com` | Password: `DeveloperTest123`

---

## ✅ Verification

After implementation, verify:

1. ✓ New column exists in database:
   ```sql
   DESCRIBE users;  -- Should show plain_password column
   ```

2. ✓ Application builds without errors

3. ✓ New users registration stores plain password:
   ```sql
   SELECT email, plain_password FROM users WHERE email = 'newuser@example.com';
   ```

4. ✓ Can retrieve credentials (if DEVELOPER role):
   ```bash
   curl -H "Authorization: Bearer JWT_TOKEN" \
     http://localhost:8080/dev/credentials/newuser@example.com
   ```

---

**Questions?** Refer to the troubleshooting section or check server logs for authentication details.
