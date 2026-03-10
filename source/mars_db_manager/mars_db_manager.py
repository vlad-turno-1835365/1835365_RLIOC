from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import psycopg2
import os
import time
from typing import List

app = FastAPI(title="Mars DB Manager", description="Microservice for interacting with the rules database")

DB_HOST = os.getenv('DB_HOST', 'postgres')
DB_PORT = os.getenv('DB_PORT', '5432')
DB_USER = os.getenv('DB_USER', 'marsuser')
DB_PASSWORD = os.getenv('DB_PASSWORD', 'marspassword')
DB_NAME = os.getenv('DB_NAME', 'marsdb')

def get_db_connection():
    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            user=DB_USER,
            password=DB_PASSWORD,
            dbname=DB_NAME
        )
        return conn
    except Exception as e:
        print(f"[!] Errore connessione DB: {e}")
        return None

def init_db():
    conn = None
    for i in range(15):
        conn = get_db_connection()
        if conn:
            break
        print(f"[*] Attesa DB, ritento tra 2 secondi... ({i+1}/15)")
        time.sleep(2)
        
    if not conn:
        print("[!] Impossibile connettersi al DB.")
        return
    
    try:
        cur = conn.cursor()
        cur.execute("""
            CREATE TABLE IF NOT EXISTS automation_rules (
                id SERIAL PRIMARY KEY,
                sensor_name VARCHAR(100) NOT NULL,
                operator VARCHAR(2) NOT NULL,
                threshold_value FLOAT NOT NULL,
                actuator_name VARCHAR(100) NOT NULL,
                actuator_action VARCHAR(3) NOT NULL
            )
        """)
        
        cur.execute("SELECT COUNT(*) FROM automation_rules")
        if cur.fetchone()[0] == 0:
            cur.execute("INSERT INTO automation_rules (sensor_name, operator, threshold_value, actuator_name, actuator_action) VALUES ('greenhouse_temperature', '>', 35.0, 'cooling_fan', 'ON')")
            print("[*] Regole base inserite nel database.")
            
        conn.commit()
        cur.close()
    except Exception as e:
        print(f"[!] Errore setup DB: {e}")
    finally:
        conn.close()

@app.on_event("startup")
def startup_event():
    init_db()

class EvaluateRequest(BaseModel):
    sensor_name: str
    value: float

# Modello Pydantic per ricevere la regola dal frontend
class RuleCreate(BaseModel):
    sensor: str
    operator: str
    value: float
    actuator: str
    state: str

@app.post("/api/evaluate")
def evaluate_rules(request: EvaluateRequest):
    conn = get_db_connection()
    if not conn:
        raise HTTPException(status_code=500, detail="Database not available")
    
    triggered_actions = []
    try:
        cur = conn.cursor()
        cur.execute("SELECT operator, threshold_value, actuator_name, actuator_action FROM automation_rules WHERE sensor_name = %s", (request.sensor_name,))
        rules = cur.fetchall()
        cur.close()
        
        num_val = request.value
        
        for rule in rules:
            operator, threshold, actuator_name, actuator_action = rule
            condition_met = False
            
            if operator == '<' and num_val < threshold: condition_met = True
            elif operator == '<=' and num_val <= threshold: condition_met = True
            elif operator == '=' and num_val == threshold: condition_met = True
            elif operator == '>' and num_val > threshold: condition_met = True
            elif operator == '>=' and num_val >= threshold: condition_met = True
            
            if condition_met:
                triggered_actions.append({"actuator": actuator_name, "action": actuator_action})
                print(f"[!] AUTOMAZIONE: {request.sensor_name} {operator} {threshold} -> Imposto {actuator_name} a {actuator_action}")
                
    except Exception as e:
        print(f"[!] Errore durante la lettura/attuazione delle regole: {e}")
        raise HTTPException(status_code=500, detail="Error evaluating rules")
    finally:
        conn.close()
        
    return {"triggered_actions": triggered_actions}

@app.get("/api/rules")
def get_all_rules():
    conn = get_db_connection()
    if not conn:
        raise HTTPException(status_code=500, detail="Database not available")
        
    try:
        cur = conn.cursor()
        cur.execute("SELECT id, sensor_name, operator, threshold_value, actuator_name, actuator_action FROM automation_rules")
        rows = cur.fetchall()
        # RISOLTO: Ora restituiamo i nomi delle variabili che il frontend si aspetta!
        rules = [
            {
                "id": r[0], 
                "sensor": r[1], 
                "operator": r[2], 
                "value": r[3],
                "actuator": r[4],
                "state": r[5]
            } for r in rows
        ]
        cur.close()
        return rules
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        conn.close()

# NUOVO: Endpoint per creare una nuova regola
@app.post("/api/rules")
def create_rule(rule: RuleCreate):
    conn = get_db_connection()
    if not conn:
        raise HTTPException(status_code=500, detail="Database not available")
        
    try:
        cur = conn.cursor()
        # Mappiamo i nomi del frontend (sensor, value, state) sulle colonne del DB
        cur.execute("""
            INSERT INTO automation_rules (sensor_name, operator, threshold_value, actuator_name, actuator_action) 
            VALUES (%s, %s, %s, %s, %s) RETURNING id
        """, (rule.sensor, rule.operator, rule.value, rule.actuator, rule.state))
        
        new_id = cur.fetchone()[0]
        conn.commit()
        cur.close()
        
        # Restituiamo la regola appena creata comprensiva di ID
        return {**rule.dict(), "id": new_id}
    except Exception as e:
        conn.rollback()
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        conn.close()

# NUOVO: Endpoint per eliminare una regola
@app.delete("/api/rules/{rule_id}")
def delete_rule(rule_id: int):
    conn = get_db_connection()
    if not conn:
        raise HTTPException(status_code=500, detail="Database not available")
        
    try:
        cur = conn.cursor()
        cur.execute("DELETE FROM automation_rules WHERE id = %s", (rule_id,))
        conn.commit()
        cur.close()
        return {"status": "success", "message": f"Rule {rule_id} deleted"}
    except Exception as e:
        conn.rollback()
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        conn.close()