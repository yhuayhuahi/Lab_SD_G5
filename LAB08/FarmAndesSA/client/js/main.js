// Configuración
const SERVERS = {
  arequipa: { name: 'Arequipa', port: 3001, icon: '🏔️', url: 'http://localhost:3001' },
  lima: { name: 'Lima', port: 3002, icon: '🌊', url: 'http://localhost:3002' },
  cusco: { name: 'Cusco', port: 3003, icon: '🏛️', url: 'http://localhost:3003' }
};

// Estado global
let currentServer = null;
let inventoryData = [];

// Elementos DOM
const serverButtons = document.querySelectorAll('.server-btn');
const statusDot = document.querySelector('.status-dot');
const statusText = document.getElementById('statusText');
const inventoryBody = document.getElementById('inventoryBody');
const refreshBtn = document.getElementById('refreshInventory');
const productoSelect = document.getElementById('productoSelect');
const cantidadInput = document.getElementById('cantidad');
const destinoSelect = document.getElementById('destinoSelect');
const transferBtn = document.getElementById('transferBtn');
const transferResult = document.getElementById('transferResult');
const logsContainer = document.getElementById('logsContainer');
const clearLogsBtn = document.getElementById('clearLogs');

// ============ FUNCIONES DE LOG ============
function addLog(message, type = 'info') {
  const logEntry = document.createElement('div');
  logEntry.className = `log-entry ${type}`;

  const timestamp = new Date().toLocaleTimeString();
  let icon = '';

  switch (type) {
    case 'success': icon = '✅ '; break;
    case 'error': icon = '❌ '; break;
    case 'warning': icon = '⚠️ '; break;
    default: icon = 'ℹ️ ';
  }

  logEntry.textContent = `${timestamp} ${icon}${message}`;
  logsContainer.appendChild(logEntry);
  logsContainer.scrollTop = logsContainer.scrollHeight;

  // Limitar logs (mantener últimos 100)
  while (logsContainer.children.length > 100) {
    logsContainer.removeChild(logsContainer.firstChild);
  }
}

function clearLogs() {
  logsContainer.innerHTML = '';
  addLog('Logs limpiados', 'info');
}

// ============ FUNCIONES DE API ============
async function fetchAPI(endpoint, options = {}) {
  if (!currentServer) {
    throw new Error('No hay servidor seleccionado');
  }

  const url = `${SERVERS[currentServer].url}${endpoint}`;
  const response = await fetch(url, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ error: `HTTP ${response.status}` }));
    throw new Error(error.error || error.message || `Error ${response.status}`);
  }

  return response.json();
}

async function loadInventory() {
  if (!currentServer) {
    inventoryBody.innerHTML = '<tr><td colspan="3" class="loading">Selecciona un servidor para ver el inventario</td></tr>';
    productoSelect.innerHTML = '<option value="">Selecciona un producto</option>';
    return;
  }

  try {
    addLog(`Cargando inventario de ${SERVERS[currentServer].name}...`, 'info');

    const data = await fetchAPI('/inventario');
    inventoryData = data.inventario || data;

    if (!Array.isArray(inventoryData)) {
      inventoryData = [];
    }

    renderInventoryTable();
    updateProductSelect();

    addLog(`Inventario cargado: ${inventoryData.length} productos`, 'success');

  } catch (error) {
    console.error('Error loading inventory:', error);
    inventoryBody.innerHTML = `<tr><td colspan="3" class="loading">❌ Error: ${error.message}</td></tr>`;
    addLog(`Error cargando inventario: ${error.message}`, 'error');
  }
}

function renderInventoryTable() {
  if (!inventoryData.length) {
    inventoryBody.innerHTML = '<tr><td colspan="3" class="loading">📭 No hay productos en el inventario</td></tr>';
    return;
  }

  inventoryBody.innerHTML = inventoryData.map(item => `
        <tr>
            <td><strong>${escapeHtml(item.producto)}</strong></td>
            <td class="${item.stock < 10 ? 'low-stock' : ''}">${item.stock} unidades</td>
            <td>
                <button class="btn-transferir" data-producto="${escapeHtml(item.producto)}" data-stock="${item.stock}">
                    Transferir
                </button>
            </td>
        </tr>
    `).join('');

  // Agregar event listeners a los botones de transferencia rápida
  document.querySelectorAll('.btn-transferir').forEach(btn => {
    btn.addEventListener('click', () => {
      const producto = btn.dataset.producto;
      productoSelect.value = producto;
      cantidadInput.focus();
      addLog(`Producto "${producto}" seleccionado para transferencia`, 'info');
    });
  });
}

function updateProductSelect() {
  const productos = [...new Set(inventoryData.map(item => item.producto))];

  productoSelect.innerHTML = '<option value="">Selecciona un producto</option>' +
    productos.map(p => `<option value="${escapeHtml(p)}">${escapeHtml(p)}</option>`).join('');
}

async function transferProduct(producto, cantidad, destino) {
  if (!currentServer) {
    throw new Error('No hay servidor seleccionado');
  }

  if (currentServer === destino) {
    throw new Error('No puedes transferir al mismo almacén');
  }

  addLog(`Iniciando transferencia: ${cantidad} de "${producto}" de ${SERVERS[currentServer].name} → ${SERVERS[destino].name}`, 'info');

  const response = await fetchAPI('/transferir', {
    method: 'POST',
    body: JSON.stringify({ producto, cantidad, destino })
  });

  return response;
}

// ============ ACTUALIZAR ESTADO DEL SERVIDOR ============
function setServer(serverId) {
  currentServer = serverId;

  // Actualizar UI de botones
  serverButtons.forEach(btn => {
    if (btn.dataset.server === serverId) {
      btn.classList.add('active');
    } else {
      btn.classList.remove('active');
    }
  });

  // Actualizar estado visual
  statusDot.classList.add('connected');
  statusText.textContent = `Conectado a ${SERVERS[serverId].name} (puerto ${SERVERS[serverId].port})`;

  addLog(`Conectado a ${SERVERS[serverId].name}`, 'success');

  // Cargar inventario
  loadInventory();
}

async function checkServerHealth(serverId) {
  const server = SERVERS[serverId];
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 3000);

    const response = await fetch(`${server.url}/health`, { signal: controller.signal });
    clearTimeout(timeoutId);

    if (response.ok) {
      return true;
    }
  } catch (error) {
    // Servidor no responde
  }
  return false;
}

// ============ MANEJO DE TRANSFERENCIA ============
async function handleTransfer() {
  const producto = productoSelect.value;
  const cantidad = parseInt(cantidadInput.value);
  const destino = destinoSelect.value;

  // Validaciones
  if (!producto) {
    showTransferResult('Selecciona un producto', 'error');
    return;
  }

  if (!cantidad || cantidad <= 0) {
    showTransferResult('Ingresa una cantidad válida (mayor a 0)', 'error');
    return;
  }

  if (!destino) {
    showTransferResult('Selecciona un almacén destino', 'error');
    return;
  }

  if (currentServer === destino) {
    showTransferResult(`No puedes transferir de ${SERVERS[currentServer].name} a sí mismo`, 'error');
    return;
  }

  // Verificar stock disponible
  const product = inventoryData.find(p => p.producto === producto);
  if (!product || product.stock < cantidad) {
    const available = product ? product.stock : 0;
    showTransferResult(`Stock insuficiente. Disponible: ${available}, solicitado: ${cantidad}`, 'error');
    return;
  }

  // Deshabilitar botón durante la operación
  transferBtn.disabled = true;
  transferBtn.textContent = '⏳ Procesando...';
  showTransferResult('', '');

  try {
    const result = await transferProduct(producto, cantidad, destino);

    if (result.success) {
      showTransferResult(result.message, 'success');
      addLog(`✅ Transferencia exitosa: ${cantidad} de "${producto}" a ${SERVERS[destino].name}`, 'success');

      // Recargar inventario
      await loadInventory();
    } else {
      showTransferResult(result.message, 'error');
      addLog(`❌ Transferencia fallida: ${result.message}`, 'error');
    }
  } catch (error) {
    console.error('Transfer error:', error);
    let errorMsg = error.message;

    if (error.message.includes('fetch')) {
      errorMsg = 'Error de conexión. ¿El servidor está corriendo?';
    } else if (error.message.includes('timeout')) {
      errorMsg = 'Tiempo de espera agotado. El servidor destino no respondió.';
    }

    showTransferResult(errorMsg, 'error');
    addLog(`❌ Error en transferencia: ${errorMsg}`, 'error');
  } finally {
    transferBtn.disabled = false;
    transferBtn.textContent = '🚚 Transferir';
  }
}

function showTransferResult(message, type) {
  transferResult.textContent = message;
  transferResult.className = 'transfer-result';
  if (type) {
    transferResult.classList.add(type);
  }
}

// ============ HELPER ============
function escapeHtml(str) {
  if (!str) return '';
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

// ============ EVENT LISTENERS ============
serverButtons.forEach(btn => {
  btn.addEventListener('click', async () => {
    const serverId = btn.dataset.server;

    // Verificar salud del servidor
    const isHealthy = await checkServerHealth(serverId);

    if (!isHealthy) {
      addLog(`${SERVERS[serverId].name} no responde. ¿El contenedor está corriendo?`, 'warning');
      showTransferResult(`⚠️ ${SERVERS[serverId].name} no está disponible. ¿Ejecutaste "docker compose up -d"?`, 'error');
      return;
    }

    setServer(serverId);
  });
});

refreshBtn.addEventListener('click', loadInventory);
transferBtn.addEventListener('click', handleTransfer);
clearLogsBtn.addEventListener('click', clearLogs);

// Validar que destino no sea el mismo que origen
destinoSelect.addEventListener('change', () => {
  if (destinoSelect.value === currentServer) {
    showTransferResult(`⚠️ No puedes transferir al mismo almacén (${SERVERS[currentServer]?.name})`, 'warning');
  } else {
    showTransferResult('', '');
  }
});

// Enter en cantidad para transferir
cantidadInput.addEventListener('keypress', (e) => {
  if (e.key === 'Enter') {
    handleTransfer();
  }
});

// Mensaje inicial
addLog('Sistema de inventario distribuido iniciado', 'info');
addLog('Selecciona un servidor (Arequipa, Lima o Cusco) para comenzar', 'info');
