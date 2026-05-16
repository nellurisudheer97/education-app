# Password Reset Feature - Implementation Guide

## Overview
A complete email-based password reset feature has been implemented for the Educational Platform. Students can now securely reset their forgotten passwords by receiving email reset links.

---

## ✅ What's Been Implemented

### Backend Components

#### 1. **Database Entity** - `PasswordResetToken.java`
- Stores password reset tokens with expiration
- Tracks token usage status
- 24-hour expiration by default
- Validation methods for checking token validity

#### 2. **Email Service** - `EmailService.java`
- Sends password reset emails via SMTP
- Falls back to console logging for development (no email configured)
- HTML-formatted email templates
- Includes reset link with token

#### 3. **API Endpoints**
All endpoints are **public** (no authentication required):

- **POST** `/auth/forgot-password`
  - Request: `{ "email": "user@example.com" }`
  - Response: Confirmation message (secure - doesn't reveal if email exists)
  - Generates reset token and sends email

- **POST** `/auth/verify-reset-token`
  - Request: `{ "token": "reset_token_here" }`
  - Response: Token validity status
  - Frontend uses this to validate link before showing form

- **POST** `/auth/reset-password`
  - Request: `{ "token": "reset_token_here", "newPassword": "pass123", "confirmPassword": "pass123" }`
  - Response: Success/error message
  - Updates password and marks token as used

#### 4. **Database Changes**
New table created automatically: `password_reset_tokens`
```sql
CREATE TABLE password_reset_tokens (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  token VARCHAR(255) UNIQUE NOT NULL,
  user_id BIGINT NOT NULL,
  expiry_date DATETIME NOT NULL,
  used BOOLEAN DEFAULT FALSE,
  created_at DATETIME NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

### Frontend Components

#### 1. **Forgot Password Page** - `ForgotPassword.jsx`
- Simple email input form
- Validation of email format
- Success message with redirect to login
- Accessible UI with icons

#### 2. **Reset Password Page** - `ResetPassword.jsx`
- Extracts token from URL query parameter
- Verifies token validity on page load
- Password and confirm password fields
- Password visibility toggle
- Validates password strength (min 8 characters)
- Success redirect to login

#### 3. **Login Page Update**
- Added "Forgot password?" link below password field
- Links to `/forgot-password` page

#### 4. **Routes** - `App.jsx`
- `/forgot-password` - Public route
- `/reset-password?token=XXX` - Public route
- Both routes redirect to login if already authenticated

---

## 🚀 How to Use

### For Users

#### Scenario 1: User Forgets Password
1. Go to login page → Click "Forgot password?"
2. Enter email address
3. Check email for reset link (subject: "Password Reset Request - EduForge")
4. Click link in email (or copy token to URL)
5. Enter new password (min 8 characters)
6. Confirm password
7. Click "Reset password"
8. Redirected to login with success message
9. Login with new password

#### Scenario 2: Invalid or Expired Token
- If token is invalid or expired (>24 hours)
- User sees error message
- Option to request new reset link
- Process starts over from step 1

---

## ⚙️ Configuration

### Email Configuration (Optional)

For production, configure email in environment variables:

```bash
# Gmail Example (using App Password)
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
export MAIL_AUTH=true
export MAIL_STARTTLS_ENABLE=true
export MAIL_STARTTLS_REQUIRED=true
export MAIL_USERNAME_SENDER=your-email@gmail.com
```

For Azure/Office365:
```bash
export MAIL_HOST=smtp.office365.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@outlook.com
export MAIL_PASSWORD=your-password
```

For SendGrid:
```bash
export MAIL_HOST=smtp.sendgrid.net
export MAIL_PORT=587
export MAIL_USERNAME=apikey
export MAIL_PASSWORD=SG.xxxxxxxxxxxxx
```

### Frontend Configuration

Update frontend URL (for email reset links):
```bash
export FRONTEND_URL=https://yourdomain.com
# Default: http://localhost:3000
```

### Token Expiration

Edit in `AuthService.java`:
```java
private static final long RESET_TOKEN_EXPIRY_HOURS = 24; // Change as needed
```

---

## 🧪 Testing

### Test 1: Forgot Password Flow

```bash
# Step 1: Request password reset
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "student@example.com"}'

# Response:
# {
#   "token": null,
#   "message": "If an account with that email exists, a reset link has been sent",
#   "userId": null,
#   "role": null,
#   "fullName": null
# }

# Check console/email for reset token (in development, printed to console)
# Token format: UUID like "a1b2c3d4-e5f6-4789-ab01-cd2ef3456789"
```

### Test 2: Verify Token

```bash
curl -X POST http://localhost:8080/api/auth/verify-reset-token \
  -H "Content-Type: application/json" \
  -d '{"token": "your-token-here"}'

# Expected response:
# {
#   "token": null,
#   "message": "Token is valid",
#   "userId": null,
#   "role": null,
#   "fullName": null
# }
```

### Test 3: Reset Password

```bash
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "your-token-here",
    "newPassword": "NewPassword123",
    "confirmPassword": "NewPassword123"
  }'

# Expected response:
# {
#   "token": null,
#   "message": "Password reset successful. Please login with your new password",
#   "userId": null,
#   "role": null,
#   "fullName": null
# }
```

### Test 4: Frontend Flow

1. Start backend: `mvn spring-boot:run`
2. Start frontend: `npm start`
3. Go to http://localhost:3000/login
4. Click "Forgot password?"
5. Enter test email (e.g., student@example.com)
6. In development (no email configured):
   - Check backend console for output like: "⚠️  Email not configured. Reset link: http://localhost:3000/reset-password?token=..."
   - Copy the token from console
   - Manually visit: `http://localhost:3000/reset-password?token=YOUR_TOKEN`
7. Enter new password
8. Click "Reset password"
9. Should redirect to login
10. Login with new credentials

---

## 🔒 Security Features

✅ **Token-based**: Uses UUID v4 for cryptographically secure tokens
✅ **Time-limited**: Tokens expire after 24 hours by default
✅ **One-time use**: Tokens are marked as used after password reset
✅ **Email validation**: Doesn't reveal if email exists in system
✅ **Password hashing**: New password is BCrypt hashed (12 rounds)
✅ **HTTPS ready**: Safe for production with HTTPS
✅ **Secure email**: Uses TLS/STARTTLS for email transmission

---

## 📊 Database Queries

### Check reset tokens in database

```sql
-- View all active reset tokens
SELECT id, token, user_id, expiry_date, used, created_at 
FROM password_reset_tokens 
WHERE used = 0 AND expiry_date > NOW();

-- View expired tokens
SELECT id, token, user_id, expiry_date, used, created_at 
FROM password_reset_tokens 
WHERE expiry_date < NOW();

-- Clean up old tokens (run periodically)
DELETE FROM password_reset_tokens 
WHERE expiry_date < DATE_SUB(NOW(), INTERVAL 7 DAY);
```

---

## 🐛 Troubleshooting

### Email Not Sending

**In Development:**
- Email not configured is OK
- Check backend console for reset link

**In Production:**
- Verify SMTP credentials
- Check firewall/port 587 is open
- Try with different email provider
- Check email logs in backend

### Token Shows as Invalid

**Possible causes:**
1. Token expired (>24 hours)
2. Token already used
3. Typo in token URL
4. Token doesn't exist in database

**Solution:**
- Request new password reset
- Check token expiry in database

### Password Reset Doesn't Work

**Check:**
1. Password meets minimum length (8 characters)
2. Passwords match (typo in confirm password)
3. Token is still valid
4. User account exists

---

## 📝 API Documentation

### Request/Response Examples

#### Forgot Password
```json
// Request
POST /api/auth/forgot-password
{
  "email": "student@example.com"
}

// Response (200 OK)
{
  "token": null,
  "message": "If an account with that email exists, a reset link has been sent",
  "userId": null,
  "role": null,
  "fullName": null
}
```

#### Reset Password
```json
// Request
POST /api/auth/reset-password
{
  "token": "a1b2c3d4-e5f6-4789-ab01-cd2ef3456789",
  "newPassword": "MyNewPassword123",
  "confirmPassword": "MyNewPassword123"
}

// Response (200 OK)
{
  "token": null,
  "message": "Password reset successful. Please login with your new password",
  "userId": null,
  "role": null,
  "fullName": null
}

// Response (400 Bad Request) - Invalid token
{
  "token": null,
  "message": "Reset token has expired or already used",
  "userId": null,
  "role": null,
  "fullName": null
}
```

---

## 📦 Files Created/Modified

### New Backend Files
✅ `model/PasswordResetToken.java`
✅ `repository/PasswordResetTokenRepository.java`
✅ `dto/ForgotPasswordRequest.java`
✅ `dto/ResetPasswordRequest.java`
✅ `dto/VerifyResetTokenRequest.java`
✅ `service/EmailService.java`

### Modified Backend Files
✅ `service/AuthService.java` - Added password reset methods
✅ `controller/AuthController.java` - Added endpoints
✅ `pom.xml` - Added spring-boot-starter-mail dependency
✅ `application.yml` - Added email configuration

### New Frontend Files
✅ `pages/ForgotPassword.jsx`
✅ `pages/ResetPassword.jsx`

### Modified Frontend Files
✅ `pages/Login.jsx` - Added forgot password link
✅ `App.jsx` - Added routes
✅ `services/api.js` - Added API methods

---

## 🚀 Deployment Checklist

- [ ] Backend build: `mvn clean install`
- [ ] Frontend build: `npm run build`
- [ ] Database migrations applied (auto with Hibernate)
- [ ] Email credentials configured (if needed)
- [ ] Frontend URL configured in backend
- [ ] JWT secret configured
- [ ] Test full flow:
  - [ ] Request password reset
  - [ ] Verify email received
  - [ ] Reset password with token
  - [ ] Login with new password
- [ ] Monitor logs for errors
- [ ] Backup database before deployment

---

## 📞 Support

For issues or questions about the password reset feature:
1. Check troubleshooting section above
2. Review backend logs for errors
3. Verify email configuration
4. Test with console.log in frontend
5. Check database for token records

---

**Implementation Status**: ✅ Complete
**Build Status**: ✅ Success
**Ready for Testing**: ✅ Yes
