import os
import json
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import redis
from typing import Dict, Any

app = FastAPI(title="Mars Memory Cache", description="Microservice for interacting with Redis cache")

REDIS_HOST = os.getenv('REDIS_HOST', 'redis')
REDIS_PORT = int(os.getenv('REDIS_PORT', 6379))

try:
    redis_client = redis.Redis(host=REDIS_HOST, port=REDIS_PORT, decode_responses=True)
except Exception as e:
    print(f"[!] Errore connessione Redis: {e}")
    redis_client = None

class StateRequest(BaseModel):
    sensor_name: str
    data: Dict[str, Any]

@app.post("/api/state")
def save_state(request: StateRequest):
    if not redis_client:
        raise HTTPException(status_code=500, detail="Redis cache not available")
    
    try:
        redis_client.set(f"sensor_latest:{request.sensor_name}", json.dumps(request.data))
        return {"status": "success", "message": f"Saved state for {request.sensor_name}"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/state/{sensor_name}")
def get_sensor_state(sensor_name: str):
    if not redis_client:
        raise HTTPException(status_code=500, detail="Redis cache not available")
        
    data = redis_client.get(f"sensor_latest:{sensor_name}")
    if data:
        return json.loads(data)
    else:
        raise HTTPException(status_code=404, detail="No data found for this sensor")

@app.get("/api/state")
def get_all_states():
    if not redis_client:
        raise HTTPException(status_code=500, detail="Redis cache not available")
        
    try:
        keys = redis_client.keys("sensor_latest:*")
        states = {}
        for key in keys:
            sensor_name = key.split("sensor_latest:")[1]
            data = redis_client.get(key)
            if data:
                states[sensor_name] = json.loads(data)
        return states
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
