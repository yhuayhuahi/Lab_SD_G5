import cors from 'cors';
import express from 'express';
import {
  asignar,
  cambiarEstado,
  camionesDisponibles,
  consultarEstado
} from './controllers/transporteController';

export function createApp(): express.Express {
  const app = express();

  app.use(cors());
  app.use(express.json());

  app.get('/health', (_req, res) => {
    res.json({ status: 'ok', servicio: 'transporte' });
  });

  app.post('/api/transporte/asignar', asignar);
  app.get('/api/transporte/estado/:pedidoId', consultarEstado);
  app.put('/api/transporte/estado/:pedidoId', cambiarEstado);
  app.get('/api/transporte/camiones', camionesDisponibles);

  app.get('/', (_req, res) => {
    res.type('html').send(`
      <!doctype html>
      <html lang="es">
      <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <title>LAB09 Transporte</title>
        <style>
          :root {
            color-scheme: light;
            --bg: #f6f7fb;
            --panel: #ffffff;
            --text: #152033;
            --muted: #5f6b7a;
            --line: #d8dee9;
            --accent: #14532d;
            --accent-2: #0f766e;
            --danger: #b42318;
          }

          * { box-sizing: border-box; }
          body {
            margin: 0;
            font-family: Arial, Helvetica, sans-serif;
            background: linear-gradient(180deg, #eef3ff 0%, var(--bg) 35%, #eef7f1 100%);
            color: var(--text);
          }
          .wrap {
            max-width: 960px;
            margin: 0 auto;
            padding: 32px 16px 48px;
          }
          .hero {
            background: rgba(255, 255, 255, 0.82);
            border: 1px solid var(--line);
            border-radius: 20px;
            padding: 24px;
            box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
            backdrop-filter: blur(10px);
          }
          h1 { margin: 0 0 8px; font-size: 2rem; }
          p { margin: 0; color: var(--muted); }
          .grid {
            display: grid;
            gap: 16px;
            margin-top: 18px;
          }
          @media (min-width: 860px) {
            .grid { grid-template-columns: 1.1fr 0.9fr; }
          }
          .card {
            background: var(--panel);
            border: 1px solid var(--line);
            border-radius: 18px;
            padding: 18px;
          }
          label {
            display: block;
            font-size: 0.9rem;
            font-weight: 700;
            margin: 0 0 6px;
          }
          input, select, button {
            width: 100%;
            border-radius: 12px;
            border: 1px solid var(--line);
            padding: 11px 12px;
            font: inherit;
          }
          input, select { background: #fff; }
          .row { display: grid; gap: 12px; margin-bottom: 12px; }
          .actions { display: grid; gap: 10px; }
          .buttons { display: grid; gap: 10px; grid-template-columns: 1fr; }
          @media (min-width: 560px) { .buttons { grid-template-columns: 1fr 1fr; } }
          button {
            cursor: pointer;
            border: 0;
            background: var(--accent);
            color: white;
            font-weight: 700;
          }
          button.secondary { background: var(--accent-2); }
          button.ghost { background: #fff; color: var(--text); border: 1px solid var(--line); }
          button.danger { background: var(--danger); }
          pre {
            min-height: 220px;
            margin: 0;
            padding: 16px;
            overflow: auto;
            background: #0b1220;
            color: #dbeafe;
            border-radius: 16px;
            border: 1px solid #1f2a44;
            white-space: pre-wrap;
            word-break: break-word;
          }
          .hint { font-size: 0.9rem; color: var(--muted); margin-top: 8px; }
        </style>
      </head>
      <body>
        <div class="wrap">
          <div class="hero">
            <h1>Servicio de Transporte</h1>
            <p>Interfaz simple para probar el servicio sin Postman. Asigna un camión, consulta el estado o actualiza el pedido desde aqui.</p>

            <div class="grid">
              <div class="card">
                <div class="row">
                  <div>
                    <label for="pedidoId">Pedido ID</label>
                    <input id="pedidoId" value="PED-001" />
                  </div>
                  <div>
                    <label for="estado">Estado</label>
                    <select id="estado">
                      <option value="ASIGNADO">ASIGNADO</option>
                      <option value="EN_RUTA">EN_RUTA</option>
                      <option value="ENTREGADO">ENTREGADO</option>
                      <option value="CANCELADO">CANCELADO</option>
                    </select>
                  </div>
                </div>

                <div class="buttons">
                  <button id="btnAsignar">Asignar camión</button>
                  <button class="secondary" id="btnEstado">Consultar estado</button>
                  <button class="ghost" id="btnCamiones">Ver camiones</button>
                  <button class="danger" id="btnActualizar">Actualizar estado</button>
                </div>
                <div class="hint">Abre esta pagina en <strong>http://localhost:8084</strong> cuando el servicio esté corriendo.</div>
              </div>

              <div class="card">
                <label>Respuesta</label>
                <pre id="salida">Listo para probar.</pre>
              </div>
            </div>
          </div>
        </div>

        <script>
          const salida = document.getElementById('salida');
          const pedidoIdInput = document.getElementById('pedidoId');
          const estadoInput = document.getElementById('estado');

          async function requestJson(url, options) {
            const response = await fetch(url, {
              headers: { 'Content-Type': 'application/json' },
              ...options
            });

            const text = await response.text();
            let data;

            try {
              data = text ? JSON.parse(text) : null;
            } catch {
              data = text;
            }

            if (!response.ok) {
              throw data || { mensaje: 'Error en la peticion' };
            }

            return data;
          }

          function pintar(data) {
            salida.textContent = typeof data === 'string' ? data : JSON.stringify(data, null, 2);
          }

          document.getElementById('btnAsignar').addEventListener('click', async () => {
            try {
              pintar(await requestJson('/api/transporte/asignar', {
                method: 'POST',
                body: JSON.stringify({ pedidoId: pedidoIdInput.value.trim() })
              }));
            } catch (error) {
              pintar(error);
            }
          });

          document.getElementById('btnEstado').addEventListener('click', async () => {
            try {
              const pedidoId = pedidoIdInput.value.trim();
              pintar(await requestJson('/api/transporte/estado/' + encodeURIComponent(pedidoId)));
            } catch (error) {
              pintar(error);
            }
          });

          document.getElementById('btnCamiones').addEventListener('click', async () => {
            try {
              pintar(await requestJson('/api/transporte/camiones'));
            } catch (error) {
              pintar(error);
            }
          });

          document.getElementById('btnActualizar').addEventListener('click', async () => {
            try {
              const pedidoId = pedidoIdInput.value.trim();
              pintar(await requestJson('/api/transporte/estado/' + encodeURIComponent(pedidoId), {
                method: 'PUT',
                body: JSON.stringify({ estado: estadoInput.value })
              }));
            } catch (error) {
              pintar(error);
            }
          });
        </script>
      </body>
      </html>
    `);
  });

  app.use((_req, res) => {
    res.status(404).json({ mensaje: 'Ruta no encontrada' });
  });

  return app;
}