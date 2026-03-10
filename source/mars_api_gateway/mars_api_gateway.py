import os
import requests
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from typing import List, Dict, Any
import uvicorn

app = FastAPI(title="Mars API Gateway")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], 
    allow_credentials=True,
    allow_methods=["*"],  
    allow_headers=["*"],  
)

MEMORY_CACHE_URL = os.getenv('MEMORY_CACHE_URL', 'http://mars_memory_cache:8000')
DB_MANAGER_URL = os.getenv('DB_MANAGER_URL', 'http://mars_db_manager:8000')
SIMULATOR_URL = os.getenv('SIMULATOR_URL', 'http://mars-simulator:8080')

class ConnectionManager:
    def __init__(self):
        self.active_connections: List[WebSocket] = []

    async def connect(self, websocket: WebSocket):
        await websocket.accept()
        self.active_connections.append(websocket)
        
        try:
            response = requests.get(f"{MEMORY_CACHE_URL}/api/state", timeout=2.0)
            if response.status_code == 200:
                initial_state = response.json()
                await websocket.send_json({"type": "init", "data": initial_state})
        except Exception as e:
            print(f"Errore recupero stato iniziale: {e}")
            await websocket.send_json({"type": "init", "data": {}})

    def disconnect(self, websocket: WebSocket):
        self.active_connections.remove(websocket)

    async def broadcast(self, message: dict):
        for connection in self.active_connections:
            try:
                await connection.send_json(message)
            except Exception as e:
                print(f"Errore invio WS: {e}")

manager = ConnectionManager()

@app.post("/api/events")
async def receive_event(event: dict):
    sensor_name = event.get("sensor_name")
    if sensor_name:
        await manager.broadcast({"type": "update", "data": event})
        return {"status": "success", "message": f"Dato {sensor_name} aggiornato ed inoltrato"}
    return {"status": "error", "message": "Formato evento non valido"}

@app.get("/api/sensors/state")
def get_sensor_state():
    try:
        response = requests.get(f"{MEMORY_CACHE_URL}/api/state", timeout=2.0)
        if response.status_code == 200:
            return response.json()
    except Exception as e:
        pass
    return {}

@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    await manager.connect(websocket)
    try:
        while True:
            data = await websocket.receive_text()
            print(f"Messaggio dalla dashboard: {data}")
    except WebSocketDisconnect:
        manager.disconnect(websocket)
        print("Dashboard disconnessa")

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)

@app.get("/api/rules")
def get_rules():
    try:
        response = requests.get(f"{DB_MANAGER_URL}/api/rules", timeout=2.0)
        return response.json()
    except Exception as e:
        return []

@app.post("/api/rules")
def create_rule(rule: dict):
    try:
        response = requests.post(f"{DB_MANAGER_URL}/api/rules", json=rule, timeout=2.0)
        return response.json()
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.delete("/api/rules/{rule_id}")
def delete_rule(rule_id: int):
    try:
        response = requests.delete(f"{DB_MANAGER_URL}/api/rules/{rule_id}", timeout=2.0)
        return response.json()
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.post("/api/actuators/{actuator_name}")
def toggle_actuator(actuator_name: str, payload: dict):
    try:
        # Inoltra il comando direttamente all'API REST del simulatore
        response = requests.post(
            f"{SIMULATOR_URL}/api/actuators/{actuator_name}", 
            json=payload, 
            timeout=2.0
        )
        return response.json()
    except Exception as e:
        return {"status": "error", "message": str(e)}