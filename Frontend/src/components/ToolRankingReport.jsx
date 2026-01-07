import { useEffect, useState } from "react";
import reportService from "../services/reportService";
import "../report.css";

export default function ToolRankingReport() {
  const [ranking, setRanking] = useState([]);

  // 1. Estados para los filtros
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");

  // 2. Función para pedir datos (reutilizable)
  const fetchReport = (startDate, endDate) => {
    reportService.getToolRanking(startDate, endDate)
      .then((res) => {
        console.log("Ranking recibido:", res.data);
        setRanking(res.data);
      })
      .catch((err) => {
        console.error("Error cargando ranking", err);
        setRanking([]); // Limpiamos tabla si falla
      });
  };

  // 3. Carga inicial (trae todo el historial)
  useEffect(() => {
    fetchReport("", "");
  }, []);

  // 4. Acción del botón Filtrar
  const handleFilter = () => {
    fetchReport(from, to);
  };

  return (
    <div className="report-container">
      <div className="report-box">
        <h2 className="report-title">Ranking de Herramientas</h2>
        <div className="divider"></div>

        {/* 5. UI DE FILTROS */}
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

        <table className="report-table">
          <thead>
            <tr>
              <th>Herramienta</th>
              {/* Agregamos Categoría porque el DTO la trae */}
              <th>Categoría</th> 
              <th>Veces Arrendada</th>
            </tr>
          </thead>

          <tbody>
            {ranking.length === 0 ? (
              <tr>
                <td colSpan="3" style={{ textAlign: "center" }}>
                  No hay datos disponibles en este rango
                </td>
              </tr>
            ) : (
              ranking.map((row, index) => (
                <tr key={index}>
                  <td>{row.toolName}</td>
                  {/* Si category viene null, mostramos "General" */}
                  <td>{row.category || "General"}</td>
                  <td>{row.rentalCount}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}