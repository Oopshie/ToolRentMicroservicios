import { useEffect, useState } from "react";
import reportService from "../services/reportService";
import "../report.css"; 

export default function ActiveRentsReport() {
  // Solo necesitamos una variable para guardar lo que nos responde el servidor
  const [rents, setRents] = useState([]);

  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");

  // FUNCIÓN CENTRAL: Pide los datos al backend (con o sin fechas)
  const fetchReport = (startDate, endDate) => {
    reportService.getActiveRents(startDate, endDate)
      .then((res) => {
        console.log("Active rents recibidos:", res.data); 
        setRents(res.data); // Actualizamos la tabla directamente
      })
      .catch(err => {
        console.error("Error cargando reporte", err);
        setRents([]); // Si falla o no hay datos, tabla vacía
      });
  };

  // 1. Carga inicial: Pedimos todo sin filtros
  useEffect(() => {
    fetchReport("", "");
  }, []);

  // 2. Botón Filtrar: Pedimos de nuevo usando las fechas del input
  const filter = () => {
    // Ya no usamos .filter() de Javascript, llamamos al backend
    fetchReport(from, to);
  };

  return (
    <div className="report-container">
      <div className="report-box">
        <h2 className="report-title">Arriendos Activos</h2>
        <div className="divider"></div>

        {/* FILTRO */}
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

          <button className="filter-btn" onClick={filter}>
            Filtrar
          </button>
        </div>

        {/* TABLA */}
        <table className="report-table">
          <thead>
            <tr>
              <th>Cliente</th>
              <th>Herramienta</th>
              <th>Fecha Inicio</th>
              <th>Fecha Término</th>
              {/* Opcional: Agregué esta columna porque tu Backend la envía */}
              <th>Estado</th> 
            </tr>
          </thead>

          <tbody>
            {rents.length === 0 ? (
              <tr>
                <td colSpan="5" style={{ textAlign: "center" }}>
                  No hay arriendos en este rango
                </td>
              </tr>
            ) : (
              rents.map((r) => (
                <tr key={r.id}>
                  {/* Asegúrate que estos nombres coincidan con tu DTO de Java */}
                  <td>{r.clientName}</td>
                  <td>{r.toolName}</td>
                  <td>{r.startDate}</td>
                  <td>{r.finishDate}</td>
                  
                  {/* Lógica visual para mostrar si está atrasado (tu DTO envía 'late') */}
                  <td>
                    {r.late ? (
                       <span style={{color: 'red', fontWeight: 'bold'}}>Atrasado</span>
                    ) : (
                       <span style={{color: 'green'}}>Al día</span>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}