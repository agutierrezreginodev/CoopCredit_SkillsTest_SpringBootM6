# 🔧 Grafana Dashboard - No Data Troubleshooting Guide

## ⚠️ Problem: Grafana Shows No Data

If your Grafana dashboard is showing no data, follow this troubleshooting guide step by step.

---

## 🔍 Step 1: Verify All Services Are Running

### Check Docker Containers

```bash
# List all running containers
docker compose ps

# Expected output:
# NAME                         STATUS
# coopcredit-db               Up (healthy)
# risk-central-mock           Up (health: starting)
# credit-application-service  Up (health: starting)
# coopcredit-prometheus       Up
# coopcredit-grafana          Up
```

### If Services Are Not Running

```bash
# Start all services
docker compose up -d

# Wait 30 seconds for services to start
sleep 30

# Check status again
docker compose ps
```

---

## 🔍 Step 2: Verify Prometheus Can Scrape Metrics

### Check Prometheus Targets

1. **Open Prometheus UI:**
   ```
   http://localhost:9090
   ```

2. **Go to:** Status → Targets

3. **Look for these targets:**
   - `prometheus` (localhost:9090)
   - `credit-application-service` (credit-application-service:8080)
   - `risk-central-mock-service` (risk-central-mock-service:8081)

### Expected Status: All Should Show "UP" ✓

```
Endpoint                                    State    Labels
prometheus:9090                             UP       service=prometheus
credit-application-service:8080             UP       application=credit-application-service
risk-central-mock-service:8081              UP       application=risk-central-mock-service
```

### If Status Shows "DOWN" ❌

**Problem:** Prometheus cannot reach the services

**Solutions:**

#### Option 1: Check Service Health

```bash
# Check Credit Application Service
curl http://localhost:8080/actuator/health

# Check Risk Central Mock Service
curl http://localhost:8081/actuator/health

# Expected response:
# {"status":"UP","components":{...}}
```

#### Option 2: Check Docker Network

```bash
# Verify services are on same network
docker network inspect coopcredit_skillstest_springbootm6_coopcredit-network

# Look for: credit-application-service, risk-central-mock-service, coopcredit-prometheus
```

#### Option 3: Check Prometheus Configuration

```bash
# Verify prometheus.yml is correct
cat monitoring/prometheus.yml

# Check for correct service names:
# - credit-application-service:8080
# - risk-central-mock-service:8081
```

#### Option 4: Restart Prometheus

```bash
# Stop Prometheus
docker compose stop coopcredit-prometheus

# Start Prometheus
docker compose start cookcredit-prometheus

# Wait 10 seconds
sleep 10

# Check targets again
# http://localhost:9090/targets
```

---

## 🔍 Step 3: Verify Metrics Are Being Exported

### Check Metrics Endpoints Directly

```bash
# Check Credit Application Service metrics
curl http://localhost:8080/actuator/prometheus | head -20

# Check Risk Central Mock Service metrics
curl http://localhost:8081/actuator/prometheus | head -20

# Expected output: Lines starting with #
# # HELP jvm_memory_used_bytes The amount of used memory
# # TYPE jvm_memory_used_bytes gauge
# jvm_memory_used_bytes{area="heap",...} 123456789
```

### If Metrics Endpoint Returns 404

**Problem:** Actuator endpoints not exposed

**Solution:** Check application.yml

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

---

## 🔍 Step 4: Check Prometheus Data Storage

### Query Prometheus Directly

1. **Open Prometheus UI:**
   ```
   http://localhost:9090
   ```

2. **Go to:** Graph tab

3. **Enter a query:**
   ```
   jvm_memory_used_bytes
   ```

4. **Click Execute**

### Expected Result: Graph with data points

If no data appears:
- Metrics haven't been collected yet (wait 15-30 seconds)
- Services are not exporting metrics
- Prometheus is not scraping correctly

---

## 🔍 Step 5: Verify Grafana Data Source

### Check Prometheus Data Source in Grafana

1. **Open Grafana:**
   ```
   http://localhost:3000
   ```

2. **Login:** admin / admin123

3. **Go to:** Configuration → Data Sources

4. **Look for:** Prometheus

5. **Click on Prometheus**

6. **Check URL:** Should be `http://prometheus:9090`

7. **Click "Test"** button

### Expected Result: "Data source is working"

### If Test Fails

**Problem:** Grafana cannot reach Prometheus

**Solutions:**

#### Option 1: Check Network

```bash
# Verify Prometheus is running
docker ps | grep prometheus

# Check Prometheus logs
docker logs coopcredit-prometheus
```

#### Option 2: Update Data Source URL

If using localhost:
```
http://localhost:9090
```

If using Docker network:
```
http://prometheus:9090
```

#### Option 3: Restart Grafana

```bash
docker compose restart coopcredit-grafana
```

---

## 🔍 Step 6: Verify Dashboard Configuration

### Check Dashboard Queries

1. **Open Grafana Dashboard**
2. **Click on a panel** (e.g., "JVM Memory")
3. **Click "Edit"** (pencil icon)
4. **Check the query:**
   ```
   jvm_memory_used_bytes{instance="credit-application-service:8080"}
   ```

### If Query Shows No Data

**Problem:** Query syntax or metric name is wrong

**Solutions:**

#### Option 1: Use Prometheus Query Builder

1. In Grafana panel editor
2. Click "Metrics browser"
3. Select metric from list
4. Grafana auto-completes the query

#### Option 2: Test Query in Prometheus

1. Go to http://localhost:9090
2. Paste the query
3. Click Execute
4. If it works in Prometheus, it should work in Grafana

#### Option 3: Check Metric Names

```bash
# List all available metrics
curl http://localhost:8080/actuator/prometheus | grep "^[a-z]" | cut -d'{' -f1 | sort | uniq
```

---

## 🚀 Quick Fix Checklist

### 1. Restart Everything

```bash
# Stop all services
docker compose down

# Wait 5 seconds
sleep 5

# Start all services
docker compose up -d

# Wait 30 seconds for services to start
sleep 30

# Check status
docker compose ps
```

### 2. Verify Prometheus Targets

```
http://localhost:9090/targets
```

All targets should show "UP" ✓

### 3. Verify Prometheus Has Data

```
http://localhost:9090/graph
Query: up
Execute
```

Should show 5 data points (prometheus, credit-app, risk-central, db, etc.)

### 4. Verify Grafana Data Source

```
http://localhost:3000
Configuration → Data Sources → Prometheus → Test
```

Should show "Data source is working"

### 5. Verify Dashboard Queries

```
http://localhost:3000
Click dashboard → Edit panel → Check query
```

Query should return data in Prometheus

---

## 📊 Common Issues & Solutions

### Issue 1: "No data points" in Prometheus

**Cause:** Services not exporting metrics

**Solution:**
```bash
# Check if metrics endpoint works
curl http://localhost:8080/actuator/prometheus

# If 404: Enable actuator in application.yml
# If works: Wait 15-30 seconds for Prometheus to scrape
```

### Issue 2: "Unable to connect to Prometheus" in Grafana

**Cause:** Grafana cannot reach Prometheus

**Solution:**
```bash
# Check Prometheus is running
docker ps | grep prometheus

# Check Prometheus logs
docker logs coopcredit-prometheus

# Verify URL in Grafana data source
# Should be: http://prometheus:9090 (Docker network)
# Or: http://localhost:9090 (localhost)
```

### Issue 3: "Targets DOWN" in Prometheus

**Cause:** Prometheus cannot reach services

**Solution:**
```bash
# Check services are running
docker ps | grep credit-application

# Check service health
curl http://localhost:8080/actuator/health

# Check Prometheus config
cat monitoring/prometheus.yml

# Verify service names in prometheus.yml match container names
```

### Issue 4: "Empty dashboard" in Grafana

**Cause:** Dashboard queries are wrong or data doesn't exist

**Solution:**
```bash
# Test query in Prometheus
http://localhost:9090/graph

# Try simple query: up
# Should return 5 data points

# If works: Update dashboard query
# If fails: Check metrics are being exported
```

---

## 🔧 Advanced Troubleshooting

### Check Prometheus Logs

```bash
docker logs coopcredit-prometheus

# Look for errors like:
# - "connection refused"
# - "context deadline exceeded"
# - "no such host"
```

### Check Grafana Logs

```bash
docker logs cookcredit-grafana

# Look for errors like:
# - "connection refused"
# - "data source not found"
```

### Check Service Logs

```bash
# Credit Application Service
docker logs credit-application-service

# Risk Central Mock Service
docker logs risk-central-mock-service

# Look for errors in metric export
```

### Test Metrics Manually

```bash
# Get all metrics from Credit Application Service
curl http://localhost:8080/actuator/prometheus > /tmp/metrics.txt

# Count metrics
grep "^[a-z]" /tmp/metrics.txt | wc -l

# Should show 100+ metrics

# Search for specific metric
grep "jvm_memory" /tmp/metrics.txt
```

---

## 📋 Step-by-Step Recovery Plan

### Step 1: Stop Everything (2 minutes)

```bash
docker compose down
```

### Step 2: Clean Up (1 minute)

```bash
# Remove Prometheus data (optional)
docker volume rm coopcredit_skillstest_springbootm6_prometheus_data

# Remove Grafana data (optional)
docker volume rm coopcredit_skillstest_springbootm6_grafana_data
```

### Step 3: Start Fresh (5 minutes)

```bash
docker compose up -d
sleep 30
```

### Step 4: Verify (5 minutes)

```bash
# Check all services running
docker compose ps

# Check Prometheus targets
curl http://localhost:9090/api/v1/targets | jq '.data.activeTargets[] | {job: .job, state: .health}'

# Check Prometheus has data
curl http://localhost:9090/api/v1/query?query=up | jq '.data.result | length'

# Check Grafana data source
curl http://localhost:3000/api/datasources | jq '.[] | {name: .name, type: .type}'
```

### Step 5: Verify Dashboard (5 minutes)

```
http://localhost:3000
Login: admin / admin123
Check dashboard for data
```

---

## 🎯 Expected Timeline

| Step | Action | Time | Expected Result |
|------|--------|------|-----------------|
| 1 | Start services | 30 sec | All containers running |
| 2 | Wait for startup | 30 sec | Services healthy |
| 3 | Check Prometheus | 1 min | All targets UP |
| 4 | Check metrics | 1 min | Metrics exported |
| 5 | Check Grafana | 1 min | Data source working |
| 6 | View dashboard | 1 min | Dashboard shows data |
| **Total** | | **5 min** | **Grafana working** |

---

## 📞 Still Having Issues?

### Check These Files

1. **monitoring/prometheus.yml** - Scrape configuration
2. **docker-compose.yml** - Service configuration
3. **application.yml** - Actuator configuration

### Verify These URLs

```
Prometheus:     http://localhost:9090
Grafana:        http://localhost:3000
Credit App:     http://localhost:8080/actuator/prometheus
Risk Central:   http://localhost:8081/actuator/prometheus
```

### Run These Commands

```bash
# Check all services
docker compose ps

# Check Prometheus targets
curl http://localhost:9090/targets

# Check metrics
curl http://localhost:8080/actuator/prometheus | head -20

# Check Grafana data source
curl http://localhost:3000/api/datasources
```

---

## ✅ Success Indicators

When everything is working:

✅ All Docker containers show "Up"
✅ Prometheus shows all targets as "UP"
✅ Prometheus query returns data
✅ Grafana data source test passes
✅ Dashboard shows data points
✅ Graphs display metrics

---

## 🎉 If All Else Fails

**Nuclear Option: Complete Reset**

```bash
# Stop everything
docker compose down -v

# Remove all volumes
docker volume prune -f

# Start fresh
docker compose up -d

# Wait 60 seconds
sleep 60

# Verify
docker compose ps
curl http://localhost:9090/targets
curl http://localhost:3000
```

---

**Last Updated:** 2025-12-10
**Status:** ✅ Complete Troubleshooting Guide

