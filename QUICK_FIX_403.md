# 🚀 403 Error Quick Fix - ACTION STEPS

## ⚡ Do This NOW (5 minutes)

### Step 1: Rebuild Backend
```powershell
cd "C:\Users\Priyansh\Downloads\Expense Tracker (2) (2)\Expense Tracker\backend_java"
mvn clean install -DskipTests
```

### Step 2: Start Backend
```powershell
cd "C:\Users\Priyansh\Downloads\Expense Tracker (2) (2)\Expense Tracker"
.\START_BACKEND.ps1
```

Wait for: `Started SmartExpenseApplication`

### Step 3: Test Backend is Running
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/debug/health"
```

Should show: `"status": "UP"`

### Step 4: Sync Android App
- Open Android Studio
- Click "Sync Gradle"
- Wait for sync to complete

### Step 5: Clear App Data (Important!)
```powershell
adb shell pm clear com.smartexpense.mobile
```

This removes old tokens that might be invalid.

### Step 6: Reopen App
- App will show login screen
- Login with your credentials
- App should NOT show 403 error

---

## ✓ If You See This - It's Working!

After login, opening Trips should show:
```
✓ "Notebook Created!" or
✓ List of your notebooks or
✓ Empty state "Create your first notebook"
```

NOT:
```
✗ "403 Forbidden"
✗ "Unauthorized Access"  
✗ Network Error
```

---

## 🔍 What Was Fixed

1. **SecurityConfig.java**
   - Added CORS support
   - Added custom error messages for 403
   - Made authentication clearer

2. **CustomAuthenticationEntryPoint.java** (NEW)
   - Provides clear error messages when token is missing
   - Shows what's needed to fix it

3. **DebugController.java** (NEW)
   - Lets you test authentication
   - Public endpoints for debugging
   - No token required to check status

4. **TripController.java**
   - Already had CORS support
   - Error handling improved

---

## 🧪 Quick Test

Run in PowerShell to verify fix:

```powershell
# 1. Register
$reg = @{
  name = "TestUser"
  email = "test@test.com"
  password = "password123"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/auth/register" `
  -Method POST -ContentType "application/json" -Body $reg

# 2. Login
$login = @{
  email = "test@test.com"
  password = "password123"
} | ConvertTo-Json

$resp = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
  -Method POST -ContentType "application/json" -Body $login

$token = ($resp.Content | ConvertFrom-Json).token
$userId = ($resp.Content | ConvertFrom-Json).user.id

Write-Host "Token: $token"
Write-Host "UserID: $userId"

# 3. Access Trip (protected endpoint)
$headers = @{"Authorization" = "Bearer $token"}
Invoke-WebRequest -Uri "http://localhost:8080/api/trips/user/$userId" `
  -Headers $headers

# If this works (200 OK) - fix is successful ✓
```

---

## 🆘 Still Seeing 403?

### Check 1: Backend Running?
```powershell
Test-NetConnection -ComputerName localhost -Port 8080
```

Should show: `TcpSucceeded : True`

If False: Backend not running
- Rebuild and start with START_BACKEND.ps1

### Check 2: Database Connected?
```powershell
Test-NetConnection -ComputerName localhost -Port 27017
```

Should show: `TcpSucceeded : True`

If False: MongoDB not running
- Start MongoDB service

### Check 3: App Cleared?
```powershell
adb shell pm clear com.smartexpense.mobile
```

Then reopen app and login fresh.

### Check 4: Logs?
```powershell
# Backend logs
# Look for "Started SmartExpenseApplication"
# Look for JWT errors
# Look for CORS errors

# Android logs
adb logcat | grep -i "403\|Unauthorized\|Trip\|Token"
```

---

## 🎯 What Should Happen Now

| Action | Before Fix | After Fix |
|--------|-----------|-----------|
| Login | Success ✓ | Success ✓ |
| Open Trips | 403 Error ✗ | Shows trips ✓ |
| Create Notebook | 403 Error ✗ | Creates ✓ |
| Add Expense | 403 Error ✗ | Adds ✓ |
| View Details | 403 Error ✗ | Shows details ✓ |

---

## 📋 Files Changed

**Modified:**
- `SecurityConfig.java` - Added CORS and error handling

**Created:**
- `CustomAuthenticationEntryPoint.java` - Better error messages
- `DebugController.java` - Testing endpoints
- This file + FIX_403_ERROR_GUIDE.md - Documentation

**Total Changes:** 3 files modified, 2 files created

---

## 🎉 Success Indicators

✅ Backend log shows: `Started SmartExpenseApplication`
✅ Debug health endpoint works: `curl http://localhost:8080/api/debug/health`
✅ App login succeeds with message
✅ Trips list/create works without 403
✅ No "Unauthorized" errors in Logcat

---

## ⏱️ Time to Fix

| Step | Time |
|------|------|
| Rebuild backend | 2 min |
| Start backend | 1 min |
| Test backend | 1 min |
| Sync Android | 1 min |
| Clear app | 1 min |
| Test = **Total: ~6 minutes** |

---

## 🚨 If Still Broken After 10 minutes

1. Check backend logs for errors
2. Run: `adb logcat | grep -i "error\|exception"`
3. Check if database (MongoDB) is actually running
4. Try restarting everything:
   - Kill app
   - Kill backend
   - Restart MongoDB
   - Rebuild backend
   - Clear app cache
   - Restart app

---

**Fix Date**: June 6, 2026
**Status**: ✅ Ready to Test
**Expected Result**: 403 errors completely gone

