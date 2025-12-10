# Render Post-Deployment Setup

## 🚨 CRITICAL: Set Environment Variables After Blueprint Deployment

After Render creates your services from the Blueprint, you **MUST** manually set these two environment variables in the **credit-application-service**.

---

## Step 1: Get Your Service URLs

After deployment completes (10-15 minutes), Render will assign URLs to your services:

1. **Risk Central Mock Service**: `https://risk-central-mock-service-XXXX.onrender.com`
2. **Credit Application Service**: `https://credit-application-service-XXXX.onrender.com`

---

## Step 2: Configure Environment Variables

Go to your **credit-application-service** in Render Dashboard:

### 1. Set `RISK_CENTRAL_URL`

```
Key: RISK_CENTRAL_URL
Value: https://risk-central-mock-service-XXXX.onrender.com/risk-evaluation
```

**Replace `XXXX` with your actual risk-central-mock-service URL**

### 2. Set `CORS_ALLOWED_ORIGINS`

```
Key: CORS_ALLOWED_ORIGINS
Value: https://credit-application-service-XXXX.onrender.com
```

**Replace `XXXX` with your actual credit-application-service URL**

---

## Step 3: Manual Steps in Render Dashboard

### Option A: Via Dashboard (Recommended)

1. Go to https://dashboard.render.com
2. Click on **credit-application-service**
3. Click **Environment** tab in the left sidebar
4. Find `RISK_CENTRAL_URL` (currently blank)
   - Click **Edit**
   - Paste: `https://risk-central-mock-service-XXXX.onrender.com/risk-evaluation`
   - Click **Save Changes**
5. Find `CORS_ALLOWED_ORIGINS` (currently blank)
   - Click **Edit**
   - Paste: `https://credit-application-service-XXXX.onrender.com`
   - Click **Save Changes**
6. Service will **automatically redeploy** with new variables

### Option B: Via Render CLI

```bash
# Set RISK_CENTRAL_URL
render env set RISK_CENTRAL_URL "https://risk-central-mock-service-XXXX.onrender.com/risk-evaluation" --service credit-application-service

# Set CORS_ALLOWED_ORIGINS
render env set CORS_ALLOWED_ORIGINS "https://credit-application-service-XXXX.onrender.com" --service credit-application-service
```

---

## Step 4: Verify Deployment

After the service redeploys (2-3 minutes):

### 1. Check Health
```bash
curl https://credit-application-service-XXXX.onrender.com/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

### 2. Test Registration Endpoint (with CORS)
```bash
curl -X POST https://credit-application-service-XXXX.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test@123",
    "role": "ROLE_ADMIN",
    "document": "1234567890"
  }'
```

Expected response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "testuser",
  "roles": ["ROLE_ADMIN"]
}
```

### 3. Test Swagger UI
Visit: `https://credit-application-service-XXXX.onrender.com/swagger-ui.html`

- Should load without errors
- Should be able to execute requests
- No CORS errors in browser console

---

## Why These Variables Are `sync: false`

The `render.yaml` sets these as `sync: false` because:

1. **RISK_CENTRAL_URL**: Render generates service URLs dynamically during deployment. We can't know the exact URL beforehand.

2. **CORS_ALLOWED_ORIGINS**: Must match the exact service URL to enable credentials (JWT tokens) in requests.

Using `sync: false` allows you to manually set these values **after** Render assigns the URLs.

---

## Troubleshooting CORS Errors

### Error: "Failed to fetch - CORS"

**Cause**: `CORS_ALLOWED_ORIGINS` not set correctly

**Fix**:
```bash
# Verify current value
render env get CORS_ALLOWED_ORIGINS --service credit-application-service

# Set correct value (use YOUR actual URL)
render env set CORS_ALLOWED_ORIGINS "https://credit-application-service-XXXX.onrender.com" --service credit-application-service
```

### Error: "Risk service connection failed"

**Cause**: `RISK_CENTRAL_URL` not set or incorrect

**Fix**:
```bash
# Verify risk service is running
curl https://risk-central-mock-service-XXXX.onrender.com/actuator/health

# Set correct URL
render env set RISK_CENTRAL_URL "https://risk-central-mock-service-XXXX.onrender.com/risk-evaluation" --service credit-application-service
```

---

## Complete Example

Assuming Render assigned these URLs:
- Risk Central: `https://risk-central-mock-service-8kht.onrender.com`
- Credit App: `https://credit-application-service-p3xw.onrender.com`

Set environment variables:

```bash
# RISK_CENTRAL_URL
RISK_CENTRAL_URL=https://risk-central-mock-service-8kht.onrender.com/risk-evaluation

# CORS_ALLOWED_ORIGINS
CORS_ALLOWED_ORIGINS=https://credit-application-service-p3xw.onrender.com
```

---

## Quick Checklist

- [ ] Wait for Blueprint deployment to complete (10-15 min)
- [ ] Note down both service URLs from Render Dashboard
- [ ] Set `RISK_CENTRAL_URL` in credit-application-service
- [ ] Set `CORS_ALLOWED_ORIGINS` in credit-application-service
- [ ] Wait for automatic redeployment (2-3 min)
- [ ] Test `/actuator/health` endpoint
- [ ] Test `/api/auth/register` endpoint
- [ ] Open Swagger UI and verify no CORS errors
- [ ] ✅ System is live!

---

## Need Help?

Check logs in Render Dashboard:
1. Go to **credit-application-service**
2. Click **Logs** tab
3. Look for CORS or connection errors

Common log messages:
- `CORS_ALLOWED_ORIGINS: <value>` - Confirms CORS setting loaded
- `RISK_CENTRAL_URL: <value>` - Confirms risk URL loaded
- `Started CreditApplicationServiceApplication` - Service started successfully
