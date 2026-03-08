from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from typing import List, Dict, Any
import uvicorn

app = FastAPI(title="Mars Dashboard API")

# Requisito esame: Mantenere in memoria l'ultimo stato di ogni sensore
latest_sensor_state: Dict[str, Any] = {}

# Gestore delle connessioni WebSocket per la dashboard in tempo reale
class ConnectionManager:
    def __init__(self):
        self.active_connections: List[WebSocket] = []

    async def connect(self, websocket: WebSocket):
        await websocket.accept()
        self.active_connections.append(websocket)
        # Appena la dashboard si connette, le inviamo subito lo stato attuale di tutti i sensori
        await websocket.send_json({"type": "init", "data": latest_sensor_state})

    def disconnect(self, websocket: WebSocket):
        self.active_connections.remove(websocket)

    async def broadcast(self, message: dict):
        # Invia il messaggio a tutte le dashboard connesse
        for connection in self.active_connections:
            try:
                await connection.send_json(message)
            except Exception as e:
                print(f"Errore invio WS: {e}")

manager = ConnectionManager()

# --- ENDPOINT REST (Usato dal tuo Consumer Kafka) ---
@app.post("/api/events")
async def receive_event(event: dict):
    """
    Riceve l'evento normalizzato dal Consumer Kafka.
    """
    sensor_name = event.get("sensor_name")
    
    if sensor_name:
        # 1. Aggiorna lo stato in memoria (caching)
        latest_sensor_state[sensor_name] = event
        
        # 2. Inoltra immediatamente l'evento al frontend via WebSocket
        await manager.broadcast({"type": "update", "data": event})
        
        return {"status": "success", "message": f"Dato {sensor_name} aggiornato."}
    
    return {"status": "error", "message": "Formato evento non valido, manca sensor_name"}

@app.get("/api/sensors/state")
async def get_sensor_state():
    """
    Restituisce tutto lo stato in memoria (utile per debug o polling iniziale).
    """
    return latest_sensor_state

# --- ENDPOINT WEBSOCKET (Usato dall'interfaccia grafica Front-end) ---
@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    """
    Endpoint a cui si connette la pagina HTML/JS della dashboard.
    """
    await manager.connect(websocket)
    try:
        while True:
            # Rimane in ascolto se il frontend volesse inviare comandi (es. bottoni attuatori)
            data = await websocket.receive_text()
            print(f"Messaggio ricevuto dalla dashboard: {data}")
    except WebSocketDisconnect:
        manager.disconnect(websocket)
        print("Dashboard disconnessa.")

if __name__ == "__main__":
    # Avvia il server sulla porta 8000
    uvicorn.run(app, host="0.0.0.0", port=8000)