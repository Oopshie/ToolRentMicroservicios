import { useEffect, useState } from "react";
import reportService from "../services/reportService";
import "../report.css";

export default function LateClientsReport() {
  // 1. Usamos 'clients' porque es una lista de clientes, no de fechas
  const [clients, setClients] = useState([]);

  useEffect(() => {
    reportService.getLateClients().then((res) => {
      console.log("📌 Datos recibidos en LateClientsReport:", res.data);
      setClients(res.data); // Guardamos directo
    });
  }, []);

  return (
    <div className="report-container">
      <div className="report-box">
        <h2 className="report-title">Clientes con Atraso (Histórico)</h2>
        <div className="divider"></div>

        <table className="report-table">
          <thead>
            <tr>
              {/* 2. COLUMNAS REALES: Coinciden con tu Java DTO */}
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
                  No hay clientes con atraso registrados
                </td>
              </tr>
            ) : (
              clients.map((c, index) => (
                // 3. KEY: Usamos clientId (o index si falla)
                <tr key={c.clientId || index}>
                  <td>{c.rut}</td>
                  <td>{c.clientName}</td>
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