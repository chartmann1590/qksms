# Quick Rebuild Instructions

## What was fixed:
1. **Auto-registration** - User is automatically created on container startup from .env variables
2. **Rate limiting** - Changed from 5 attempts per HOUR to 20 attempts per 5 MINUTES (much more reasonable)

## Environment Variables Added to .env:
```
INITIAL_USERNAME=chay2
INITIAL_PASSWORD=Cm0NeY12051!
INITIAL_DEVICE_ID=a855a5b03876bf8e
```

## Rebuild and Start:
```bash
cd H:/qksms/web-interface

# Stop and remove old containers
docker-compose down

# Rebuild the server container
docker-compose build server

# Start everything
docker-compose up -d

# Watch logs to confirm user registration
docker-compose logs -f server
```

Look for this line in the logs:
```
✓ Auto-registered initial user 'chay2' with device ID 'a855a5b03876bf8e'
```

## Test in App:
1. Open app Settings → Web Sync
2. Server URL: `http://10.0.0.74:8080`
3. Username: `chay2`
4. Password: `Cm0NeY12051!`
5. Click "Test Connection"

Should see: **"Connection successful!"**

## If Still Getting Rate Limited:
Wait 5 minutes or restart the container:
```bash
docker-compose restart server
```
