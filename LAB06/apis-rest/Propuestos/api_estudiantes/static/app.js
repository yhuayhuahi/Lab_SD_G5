const API = "http://127.0.0.1:5000";

let estudiantesGlobal = [];
let editando = false;
let idEditando = null;

document.addEventListener("DOMContentLoaded", () => {
    cargarTodo();

    const formulario = document.getElementById("formulario");
    formulario.addEventListener("submit", guardarEstudiante);
});

function mostrarVista(id) {
    document.querySelectorAll(".vista").forEach(v => v.classList.remove("activa"));
    document.getElementById(id).classList.add("activa");

    document.querySelectorAll(".menu-btn").forEach(b => b.classList.remove("activo"));
    event.target.classList.add("activo");

    cargarTodo();
}

async function cargarTodo() {
    await cargarEstudiantes();
    await cargarPapelera();
    await cargarEstadisticas();
}

function abrirModal(estudiante = null) {
    limpiarFormulario();
    document.getElementById("modal").classList.add("activo");

    if (estudiante) {
        editando = true;
        idEditando = estudiante.id;

        document.getElementById("tituloModal").textContent = "Actualizar estudiante";
        document.getElementById("btnGuardar").textContent = "Actualizar";
        document.getElementById("btnGuardar").className = "btn btn-naranja";

        document.getElementById("nombre").value = estudiante.nombre;
        document.getElementById("apellido").value = estudiante.apellido;
        document.getElementById("edad").value = estudiante.edad;
        document.getElementById("carrera").value = estudiante.carrera;
        document.getElementById("correo").value = estudiante.correo;
        document.getElementById("telefono").value = estudiante.telefono || "";
        // campo estado siempre s establece activo
    }
}

function cerrarModal() {
    document.getElementById("modal").classList.remove("activo");
    limpiarFormulario();
}

async function guardarEstudiante(e) {
    e.preventDefault();
    limpiarErrores();

    const estudiante = {
    nombre: document.getElementById("nombre").value.trim(),
    apellido: document.getElementById("apellido").value.trim(),
    edad: parseInt(document.getElementById("edad").value),
    carrera: document.getElementById("carrera").value.trim(),
    correo: document.getElementById("correo").value.trim(),
    telefono: document.getElementById("telefono").value.trim()
    };

    const errores = validarFormulario(estudiante);
    if (Object.keys(errores).length > 0) {
        mostrarErrores(errores);
        return;
    }

    let url = `${API}/estudiantes`;
    let metodo = "POST";

    if (editando) {
        url = `${API}/estudiantes/${idEditando}`;
        metodo = "PUT";
    }

    const respuesta = await fetch(url, {
        method: metodo,
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(estudiante)
    });

    const data = await respuesta.json();

    if (!respuesta.ok) {
        if (data.errores) {
            mostrarErrores(data.errores);
        } else {
            alert(data.error);
        }
        return;
    }

    alert(data.mensaje);
    cerrarModal();
    cargarTodo();
    mostrarVista("consultar");
}

function validarFormulario(e) {
    const errores = {};

    const carrerasValidas = [
        "Ingeniería de Sistemas",
        "Ingeniería Industrial",
        "Ingeniería Civil",
        "Ingeniería Mecánica",
        "Ingeniería Eléctrica",
        "Ingeniería Electrónica",
        "Ingeniería de Minas",
        "Ingeniería Metalúrgica",
        "Ingeniería Química",
        "Agronomía",
        "Biología",
        "Medicina",
        "Derecho",
        "Administración",
        "Contabilidad"
    ];

    if (!e.nombre) errores.nombre = "El nombre es obligatorio.";
    if (!e.apellido) errores.apellido = "El apellido es obligatorio.";

    if (!e.edad) {
        errores.edad = "La edad es obligatoria.";
    } else if (e.edad < 16 || e.edad > 80) {
        errores.edad = "La edad debe estar entre 16 y 80 años.";
    }

    if (!e.carrera) {
        errores.carrera = "Seleccione una carrera.";
    } else if (!carrerasValidas.includes(e.carrera)) {
        errores.carrera = "Debe seleccionar una carrera válida de la lista.";
    }

    if (!e.correo) {
        errores.correo = "El correo es obligatorio.";
    } else if (!/^[\w\.-]+@[\w\.-]+\.\w+$/.test(e.correo)) {
        errores.correo = "Ingrese un correo válido.";
    }

    if (!e.telefono) {
        errores.telefono = "El teléfono es obligatorio.";
    } else if (!/^9\d{8}$/.test(e.telefono)) {
        errores.telefono = "El teléfono debe tener 9 dígitos y empezar con 9.";
    }

    return errores;
}

function mostrarErrores(errores) {
    limpiarErrores();

    Object.keys(errores).forEach(campo => {
        const error = document.getElementById("error" + campo.charAt(0).toUpperCase() + campo.slice(1));
        const input = document.getElementById(campo);

        if (error) error.textContent = errores[campo];
        if (input) input.classList.add("input-error");
    });
}

function limpiarErrores() {
    ["nombre", "apellido", "edad", "carrera", "correo", "telefono"].forEach(campo => {
        const error = document.getElementById("error" + campo.charAt(0).toUpperCase() + campo.slice(1));
        const input = document.getElementById(campo);

        if (error) error.textContent = "";
        if (input) input.classList.remove("input-error");
    });
}

function limpiarFormulario() {
    const formulario = document.getElementById("formulario");
    formulario.reset();

    limpiarErrores();

    editando = false;
    idEditando = null;

    document.getElementById("tituloModal").textContent = "Registrar estudiante";
    document.getElementById("btnGuardar").textContent = "Guardar";
    document.getElementById("btnGuardar").className = "btn btn-verde";
}

async function cargarEstudiantes() {
    const tabla = document.getElementById("tablaEstudiantes");
    if (!tabla) return;

    const respuesta = await fetch(`${API}/estudiantes`);
    estudiantesGlobal = await respuesta.json();

    mostrarTablaEstudiantes(estudiantesGlobal);
}

function filtrarEstudiantes() {
    const texto = normalizarTexto(document.getElementById("busqueda").value);

    const filtrados = estudiantesGlobal.filter(e =>
        normalizarTexto(e.id).includes(texto) ||
        normalizarTexto(e.nombre).includes(texto) ||
        normalizarTexto(e.apellido).includes(texto) ||
        normalizarTexto(e.carrera).includes(texto) ||
        normalizarTexto(e.correo).includes(texto) ||
        normalizarTexto(e.telefono).includes(texto) ||
        normalizarTexto(e.estado).includes(texto)
    );

    mostrarTablaEstudiantes(filtrados);
}

function mostrarTablaEstudiantes(lista) {
    const tabla = document.getElementById("tablaEstudiantes");
    tabla.innerHTML = "";

    if (lista.length === 0) {
        tabla.innerHTML = `<tr><td colspan="8" class="mensaje-vacio">No se encontraron estudiantes.</td></tr>`;
        return;
    }

    lista.forEach(e => {
        tabla.innerHTML += `
            <tr>
                <td>${e.id}</td>
                <td>${e.nombre} ${e.apellido}</td>
                <td>${e.edad}</td>
                <td>${e.carrera}</td>
                <td>${e.correo}</td>
                <td>${e.telefono || "-"}</td>
                <td>${e.estado}</td>
                <td>
                    <div class="acciones">
                        <button class="btn btn-naranja" onclick='abrirModal(${JSON.stringify(e)})'>Editar</button>
                        <button class="btn btn-rojo" onclick="eliminarEstudiante(${e.id})">Eliminar</button>
                    </div>
                </td>
            </tr>
        `;
    });
}

async function eliminarEstudiante(id) {
    const seguro = confirm("¿Estás seguro de eliminar este estudiante? Será enviado a la papelera.");

    if (!seguro) return;

    const respuesta = await fetch(`${API}/estudiantes/${id}`, {
        method: "DELETE"
    });

    const data = await respuesta.json();

    if (!respuesta.ok) {
        alert(data.error);
        return;
    }

    alert(data.mensaje);
    cargarTodo();
}

async function cargarPapelera() {
    const tabla = document.getElementById("tablaPapelera");
    if (!tabla) return;

    const respuesta = await fetch(`${API}/papelera`);
    const papelera = await respuesta.json();

    tabla.innerHTML = "";

    if (papelera.length === 0) {
        tabla.innerHTML = `<tr><td colspan="7" class="mensaje-vacio">La papelera está vacía.</td></tr>`;
        return;
    }

    papelera.forEach(e => {
        tabla.innerHTML += `
            <tr>
                <td>${e.id}</td>
                <td>${e.nombre} ${e.apellido}</td>
                <td>${e.edad}</td>
                <td>${e.carrera}</td>
                <td>${e.correo}</td>
                <td>${e.estado}</td>
                <td>
                    <div class="acciones">
                        <button class="btn btn-verde" onclick="restaurarEstudiante(${e.id})">Restaurar</button>
                        <button class="btn btn-rojo" onclick="eliminarPermanente(${e.id})">Eliminar permanentemente</button>
                    </div>
                </td>
            </tr>
        `;
    });
}

async function restaurarEstudiante(id) {
    const respuesta = await fetch(`${API}/papelera/restaurar/${id}`, {
        method: "POST"
    });

    const data = await respuesta.json();

    if (!respuesta.ok) {
        alert(data.error);
        return;
    }

    alert(data.mensaje);
    cargarTodo();
}

async function vaciarPapelera() {
    const seguro = confirm("¿Estás seguro de vaciar definitivamente la papelera? Esta acción no se podrá deshacer.");

    if (!seguro) return;

    const respuesta = await fetch(`${API}/papelera/vaciar`, {
        method: "DELETE"
    });

    const data = await respuesta.json();

    alert(data.mensaje);
    cargarTodo();
}

async function cargarEstadisticas() {
    const respuesta = await fetch(`${API}/estadisticas`);
    const data = await respuesta.json();

    document.getElementById("statTotal").textContent = data.total;
    document.getElementById("statActivos").textContent = data.activos;
    document.getElementById("statEliminados").textContent = data.eliminados;
}

async function eliminarPermanente(id) {
    const seguro = confirm("¿Estás seguro de eliminar permanentemente este estudiante? Esta acción no se podrá deshacer.");

    if (!seguro) return;

    const respuesta = await fetch(`${API}/papelera/${id}`, {
        method: "DELETE"
    });

    const data = await respuesta.json();

    if (!respuesta.ok) {
        alert(data.error);
        return;
    }

    alert(data.mensaje);
    cargarTodo();
}

function normalizarTexto(texto) {
    return String(texto || "")
        .toLowerCase()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "");
}

function exportarEstudiantes() {
    const formato = document.getElementById("formatoExportacion").value;

    if (estudiantesGlobal.length === 0) {
        alert("No hay estudiantes para exportar.");
        return;
    }

    if (formato === "csv") {
        exportarCSV(estudiantesGlobal);
    } else if (formato === "json") {
        exportarJSON(estudiantesGlobal);
    }
}

function exportarCSV(lista) {
    const encabezados = ["id", "nombre", "apellido", "edad", "carrera", "correo", "telefono", "estado", "fecha_registro"];

    const filas = lista.map(e =>
        encabezados.map(campo => `"${String(e[campo] || "").replace(/"/g, '""')}"`).join(",")
    );

    const contenido = [encabezados.join(","), ...filas].join("\n");
    descargarArchivo(contenido, "estudiantes.csv", "text/csv;charset=utf-8;");
}

function exportarJSON(lista) {
    const contenido = JSON.stringify(lista, null, 4);
    descargarArchivo(contenido, "estudiantes.json", "application/json");
}

function descargarArchivo(contenido, nombreArchivo, tipo) {
    const blob = new Blob([contenido], { type: tipo });
    const url = URL.createObjectURL(blob);

    const enlace = document.createElement("a");
    enlace.href = url;
    enlace.download = nombreArchivo;
    enlace.click();

    URL.revokeObjectURL(url);
}

async function importarEstudiantes() {
    const input = document.getElementById("archivoImportar");

    if (!input.files.length) {
        alert("Seleccione un archivo JSON.");
        return;
    }

    const archivo = input.files[0];

    if (!archivo.name.endsWith(".json")) {
        alert("Solo se permite importar archivos JSON.");
        return;
    }

    const texto = await archivo.text();

    let datos;
    try {
        datos = JSON.parse(texto);
    } catch (error) {
        alert("El archivo JSON no tiene un formato válido.");
        return;
    }

    const respuesta = await fetch(`${API}/estudiantes/importar`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(datos)
    });

    const data = await respuesta.json();

    if (!respuesta.ok) {
        alert(data.error);
        return;
    }

    alert(data.mensaje);

    if (data.rechazados && data.rechazados.length > 0) {
        console.log("Registros rechazados:", data.rechazados);
        alert("Algunos registros fueron rechazados. Revisa la consola del navegador para ver detalles.");
    }

    input.value = "";
    cargarTodo();
}