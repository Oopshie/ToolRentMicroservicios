import { useEffect, useState } from "react";
import reportService from "../services/reportService";
import "../report.css";

export default function LateClientsReport() {
  // 1. Estado único para la lista que viene del servidor
  const [clients, setClients] = useState([]);

  // 2. Estados para los inputs de fecha
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");

  // 3. Función reutilizable para pedir datos al backend
  const fetchReport = (startDate, endDate) => {
    reportService.getLateClients(startDate, endDate)
      .then((res) => {
        console.log("📌 Datos recibidos en LateClientsReport:", res.data);
        setClients(res.data);
      })
      .catch((err) => {
        console.error("Error cargando reporte", err);
        setClients([]); // Tabla vacía si hay error
      });
  };

  // 4. Carga inicial (trae todo el historial)
  useEffect(() => {
    fetchReport("", "");
  }, []);

  // 5. Botón Filtrar: Llama al backend con las fechas seleccionadas
  const handleFilter = () => {
    fetchReport(from, to);
  };

  return (
    <div className="report-container">
      <div className="report-box">
        <h2 className="report-title">Clientes con Atraso</h2>
        <div className="divider"></div>

        {/* FILTROS */}
        <div className="filter-row">
          <label>Desde:</label>
          <input 
            type="date" 
            value={from} 
            onChange={(e) => setFrom(e.target.value)} 
          />

          <label>Hasta:</label>
          <input 
            type="date" 
            value={to} 
            onChange={(e) => setTo(e.target.value)} 
          />

          <button className="filter-btn" onClick={handleFilter}>
            Filtrar
          </button>
        </div>

        {/* TABLA ACTUALIZADA */}
        <table className="report-table">
          <thead>
            <tr>
              {/* Agregamos RUT y cambiamos columnas según el DTO */}
              <th>RUT</th>
              <th>Cliente</th>
              <th>Total Días Atraso</th>
              <th>Veces Atrasado</th>
            </tr>
          </thead>

          <tbody>
            {clients.length === 0 ? (
              <tr>
                <td colSpan="4" style={{ textAlign: "center" }}>
                  No hay clientes con atraso en este rango
                </td>
              </tr>
            ) : (
              clients.map((c, index) => (
                // Usamos clientId como key (o index si no viene)
                <tr key={c.clientId || index}>
                  <td>{c.rut}</td>         {/* Dato nuevo desde ms-client */}
                  <td>{c.clientName}</td>  {/* Ojo: camelCase, igual que en Java */}
                  <td>{c.totalLateDays}</td>
                  <td>{c.totalLateOccurrences}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}