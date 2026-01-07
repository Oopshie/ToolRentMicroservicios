import { useEffect, useState } from "react";
import reportService from "../services/reportService";
import "../report.css"; 

export default function ActiveRentsReport() {
  // 1. Solo necesitamos una variable: rents
  const [rents, setRents] = useState([]);

  useEffect(() => {
    reportService.getActiveRents().then((res) => {
      console.log("Active rents recibidos:", res.data);
      setRents(res.data); // Guardamos los datos directo aquí
    });
  }, []);

  // (Aquí borré el bloque 'const result = ...' porque usaba variables que no existen)

  return (
    <div className="report-container">
      <div className="report-box">
        <h2 className="report-title">Arriendos Activos</h2>
        <div className="divider"></div>

        <table className="report-table">
          <thead>
            <tr>
              <th>Cliente</th>
              <th>Herramienta</th>
              <th>Fecha Inicio</th>
              <th>Fecha Término</th>
            </tr>
          </thead>

          <tbody>
            {/* 2. CAMBIO CLAVE: Usamos 'rents' directamente, no 'filtered' */}
            {rents.length === 0 ? (
              <tr>
                <td colSpan="4" style={{ textAlign: "center" }}>
                  No hay arriendos registrados
                </td>
              </tr>
            ) : (
              rents.map((r) => (
                <tr key={r.rentId ?? r.id}>
                  <td>{r.clientName}</td>
                  <td>{r.toolName}</td>
                  <td>{r.startDate}</td>
                  <td>{r.finishDate}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}