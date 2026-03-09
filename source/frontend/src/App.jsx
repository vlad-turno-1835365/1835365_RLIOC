import { useState, useEffect } from 'react'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

function App() {
  const [sensors, setSensors] = useState({});
  const [actuators, setActuators] = useState({
    cooling_fan: 'OFF',
    entrance_humidifier: 'OFF',
    hall_ventilation: 'OFF',
    habitat_heater: 'OFF'
  });
  
  const [tempHistory, setTempHistory] = useState([]);

  // --- STATO PER LE REGOLE DI AUTOMAZIONE ---
  const [rules, setRules] = useState([
    { id: 1, sensor: 'greenhouse_temperature', operator: '>', value: '28', actuator: 'cooling_fan', state: 'ON' }
  ]);
  const [newRule, setNewRule] = useState({
    sensor: 'greenhouse_temperature', operator: '>', value: '', actuator: 'cooling_fan', state: 'ON'
  });

  useEffect(() => {
    setSensors({
      "greenhouse_temperature": { value: 22.5, unit: "°C" },
      "water_tank_level": { value: 85, unit: "%" },
      "corridor_pressure": { value: 101.3, unit: "kPa" },
      "solar_array.power_kw": { value: 45.2, unit: "kW" }
    });

    setTempHistory([{ time: new Date().toLocaleTimeString(), temp: 22.5 }]);

    const interval = setInterval(() => {
      setSensors(prev => {
        const oldTemp = prev["greenhouse_temperature"]?.value || 22.5;
        const newTemp = +(oldTemp + (Math.random() - 0.5)).toFixed(1);
        
        setTempHistory(currentHistory => {
          const updatedHistory = [...currentHistory, { time: new Date().toLocaleTimeString(), temp: newTemp }];
          if (updatedHistory.length > 20) updatedHistory.shift();
          return updatedHistory;
        });

        return {
          ...prev,
          "greenhouse_temperature": { value: newTemp, unit: "°C" }
        };
      });
    }, 2000);

    return () => clearInterval(interval);
  }, []);

  const toggleActuator = (actuatorName) => {
    const currentState = actuators[actuatorName];
    const newState = currentState === 'ON' ? 'OFF' : 'ON';
    setActuators(prev => ({ ...prev, [actuatorName]: newState }));
  };

  const addRule = (e) => {
    e.preventDefault();
    if (!newRule.value) return;
    setRules([...rules, { ...newRule, id: Date.now() }]);
    setNewRule({ ...newRule, value: '' }); // Reset
  };

  const deleteRule = (id) => {
    setRules(rules.filter(r => r.id !== id));
  };

  return (
    // Sfruttiamo tutta la larghezza (w-full) e riduciamo il padding laterale (p-4)
    <div className="min-h-screen bg-slate-950 text-slate-100 p-4 font-mono w-full overflow-x-hidden">
      
      <header className="flex justify-between items-center mb-6 border-b border-slate-800 pb-4">
        <h1 className="text-3xl font-bold text-red-500 tracking-wider">🚀 MARS_OS // COMMAND CENTER</h1>
        <div className="text-emerald-500 flex items-center gap-2">
          <span className="relative flex h-3 w-3">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-3 w-3 bg-emerald-500"></span>
          </span>
          SYSTEM ONLINE
        </div>
      </header>
      
      {/* RIGA 1: TELEMETRIA (4 Colonne a tutto schermo) */}
      <div className="grid grid-cols-2 xl:grid-cols-4 gap-4 mb-6">
        {Object.entries(sensors).map(([name, data]) => (
          <div key={name} className="bg-slate-900 p-5 rounded border border-slate-700 hover:border-slate-500 transition-colors">
            <h3 className="text-xs text-slate-400 mb-1 uppercase tracking-wider truncate" title={name}>{name}</h3>
            <div className="text-4xl font-bold text-emerald-400">
              {data.value} <span className="text-xl text-slate-500">{data.unit || ''}</span>
            </div>
          </div>
        ))}
      </div>

      {/* RIGA 2: GRAFICO E ATTUATORI AFFIANCATI */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-6">
        
        {/* Grafico prende 2/3 dello spazio */}
        <div className="lg:col-span-2 bg-slate-900 p-5 rounded border border-slate-700">
          <h2 className="text-lg text-slate-300 mb-4 border-b border-slate-800 pb-2">📈 GREENHOUSE TEMP TREND</h2>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={tempHistory}>
                <CartesianGrid strokeDasharray="3 3" stroke="#334155" vertical={false} />
                <XAxis dataKey="time" stroke="#64748b" fontSize={10} tickMargin={10} />
                <YAxis stroke="#64748b" fontSize={10} domain={['dataMin - 1', 'dataMax + 1']} width={40} />
                <Tooltip contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155' }} />
                <Line type="monotone" dataKey="temp" stroke="#ef4444" strokeWidth={2} dot={false} isAnimationActive={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Attuatori prendono 1/3 dello spazio */}
        <div className="lg:col-span-1 bg-slate-900 p-5 rounded border border-slate-700 overflow-y-auto h-full">
          <h2 className="text-lg text-slate-300 mb-4 border-b border-slate-800 pb-2">⚙️ MANUAL OVERRIDE</h2>
          <div className="flex flex-col gap-3">
            {Object.entries(actuators).map(([name, state]) => (
              <div key={name} className="flex justify-between items-center bg-slate-800 p-3 rounded border border-slate-700">
                <span className="text-sm text-slate-300 uppercase truncate mr-2">{name.replace('_', ' ')}</span>
                <button 
                  onClick={() => toggleActuator(name)}
                  className={`px-4 py-2 rounded text-sm font-bold min-w-[80px] ${
                    state === 'ON' ? 'bg-red-500 text-white shadow-[0_0_10px_rgba(239,68,68,0.5)]' : 'bg-slate-700 text-slate-400'
                  }`}
                >
                  {state}
                </button>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* RIGA 3: MOTORE DI AUTOMAZIONE */}
      <div className="bg-slate-900 p-5 rounded border border-slate-700 mb-8">
        <h2 className="text-lg text-slate-300 mb-4 border-b border-slate-800 pb-2">⚡ AUTOMATION RULES ENGINE</h2>
        
        {/* Form per creare regole */}
        <form onSubmit={addRule} className="flex flex-wrap gap-2 mb-6 items-end bg-slate-950 p-4 rounded border border-slate-800">
          <div className="flex-1 min-w-[200px]">
            <label className="block text-xs text-slate-500 mb-1">IF SENSOR</label>
            <select className="w-full bg-slate-800 border border-slate-700 rounded p-2 text-sm focus:border-emerald-500 outline-none" 
              value={newRule.sensor} onChange={e => setNewRule({...newRule, sensor: e.target.value})}>
              <option value="greenhouse_temperature">greenhouse_temperature</option>
              <option value="water_tank_level">water_tank_level</option>
              <option value="corridor_pressure">corridor_pressure</option>
            </select>
          </div>
          <div className="w-20">
            <label className="block text-xs text-slate-500 mb-1">OP</label>
            <select className="w-full bg-slate-800 border border-slate-700 rounded p-2 text-sm outline-none"
              value={newRule.operator} onChange={e => setNewRule({...newRule, operator: e.target.value})}>
              <option value=">">&gt;</option>
              <option value="<">&lt;</option>
              <option value="=">=</option>
              <option value=">=">&gt;=</option>
              <option value="<=">&lt;=</option>
            </select>
          </div>
          <div className="w-24">
            <label className="block text-xs text-slate-500 mb-1">VALUE</label>
            <input type="number" required className="w-full bg-slate-800 border border-slate-700 rounded p-2 text-sm outline-none"
              value={newRule.value} onChange={e => setNewRule({...newRule, value: e.target.value})} placeholder="e.g. 28"/>
          </div>
          <div className="w-auto flex items-center justify-center px-2 text-slate-500 mt-6">THEN SET</div>
          <div className="flex-1 min-w-[200px]">
            <label className="block text-xs text-slate-500 mb-1">ACTUATOR</label>
            <select className="w-full bg-slate-800 border border-slate-700 rounded p-2 text-sm outline-none"
              value={newRule.actuator} onChange={e => setNewRule({...newRule, actuator: e.target.value})}>
              <option value="cooling_fan">cooling_fan</option>
              <option value="entrance_humidifier">entrance_humidifier</option>
              <option value="habitat_heater">habitat_heater</option>
            </select>
          </div>
          <div className="w-24">
            <label className="block text-xs text-slate-500 mb-1">TO</label>
            <select className="w-full bg-slate-800 border border-slate-700 rounded p-2 text-sm outline-none"
              value={newRule.state} onChange={e => setNewRule({...newRule, state: e.target.value})}>
              <option value="ON">ON</option>
              <option value="OFF">OFF</option>
            </select>
          </div>
          <button type="submit" className="bg-emerald-600 hover:bg-emerald-500 text-white font-bold py-2 px-6 rounded text-sm h-[38px] transition-colors mt-6">
            ADD RULE
          </button>
        </form>

        {/* Lista Regole Attive */}
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-400">
            <thead className="text-xs uppercase bg-slate-800 text-slate-500 border-b border-slate-700">
              <tr>
                <th className="px-4 py-3">IF Condition</th>
                <th className="px-4 py-3">THEN Action</th>
                <th className="px-4 py-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody>
              {rules.map((r) => (
                <tr key={r.id} className="border-b border-slate-800 bg-slate-900 hover:bg-slate-800">
                  <td className="px-4 py-3"><span className="text-emerald-400">{r.sensor}</span> {r.operator} <span className="text-white">{r.value}</span></td>
                  <td className="px-4 py-3 text-slate-300">Set <span className="text-white">{r.actuator}</span> to <span className={r.state === 'ON' ? 'text-red-400' : 'text-slate-500'}>{r.state}</span></td>
                  <td className="px-4 py-3 text-right">
                    <button onClick={() => deleteRule(r.id)} className="text-red-500 hover:text-red-400 uppercase text-xs font-bold">Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {rules.length === 0 && <div className="text-center py-6 text-slate-600 italic">No automation rules configured.</div>}
        </div>
      </div>

    </div>
  )
}

export default App