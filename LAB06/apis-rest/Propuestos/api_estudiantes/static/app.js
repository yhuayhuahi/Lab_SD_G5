const API = "http://127.0.0.1:5000";

let editando = false;
let idEditando = null;

const formulario = document.getElementById("formulario");

if (formulario) {
    formulario.addEventListener("submit", async function(e) {
        e.preventDefault();
        limpiarErrores();

        const estudiante = {
            nombre: document.getElementById("nombre").value.trim(),
            apellido: document.getElementById("apellido").value.trim(),
            edad: parseInt(document.getElementById("edad").value),
            carrera: document.getElementById("carrera").value.trim(),
            correo: document.getElementById("correo").value.trim()
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
        limpiarFormulario();
        cargarEstudiantes();
    });
}

function validarFormulario(estudiante) {
    const errores = {};

    if (!estudiante.nombre) errores.nombre = "El nombre es obligatorio.";
    if (!estudiante.apellido) errores.apellido = "El apellido es obligatorio.";

    if (!estudiante.edad) {
        errores.edad = "La edad es obligatoria.";
    } else if (estudiante.edad <= 0) {
        errores.edad = "La edad debe ser mayor que cero.";
    }

    if (!estudiante.carrera) errores.carrera = "La carrera es obligatoria.";

    if (!estudiante.correo) {
        errores.correo = "El correo es obligatorio.";
    } else if (!estudiante.correo.includes("@") || !estudiante.correo.includes(".")) {
        errores.correo = "Ingrese un correo válido.";
    }

    return errores;
}

function mostrarErrores(errores) {
    limpiarErrores();

    if (errores.nombre) {
        document.getElementById("errorNombre").textContent = errores.nombre;
        document.getElementById("nombre").classList.add("input-error");
    }

    if (errores.apellido) {
        document.getElementById("errorApellido").textContent = errores.apellido;
        document.getElementById("apellido").classList.add("input-error");
    }

    if (errores.edad) {
        document.getElementById("errorEdad").textContent = errores.edad;
        document.getElementById("edad").classList.add("input-error");
    }

    if (errores.carrera) {
        document.getElementById("errorCarrera").textContent = errores.carrera;
        document.getElementById("carrera").classList.add("input-error");
    }

    if (errores.correo) {
        document.getElementById("errorCorreo").textContent = errores.correo;
        document.getElementById("correo").classList.add("input-error");
    }
}

function limpiarErrores() {
    const campos = ["nombre", "apellido", "edad", "carrera", "correo"];

    campos.forEach(campo => {
        const error = document.getElementById("error" + campo.charAt(0).toUpperCase() + campo.slice(1));
        const input = document.getElementById(campo);

        if (error) error.textContent = "";
        if (input) input.classList.remove("input-error");
    });
}

async function cargarEstudiantes() {
    const tabla = document.getElementById("tablaEstudiantes");
    if (!tabla) return;

    const respuesta = await fetch(`${API}/estudiantes`);
    const estudiantes = await respuesta.json();

    tabla.innerHTML = "";

    if (estudiantes.length === 0) {
        tabla.innerHTML = `<tr><td colspan="7" class="mensaje-vacio">No hay estudiantes registrados.</td></tr>`;
        return;
    }

    estudiantes.forEach(e => {
        tabla.innerHTML += `
            <tr>
                <td>${e.id}</td>
                <td>${e.nombre}</td>
                <td>${e.apellido}</td>
                <td>${e.edad}</td>
                <td>${e.carrera}</td>
                <td>${e.correo}</td>
                <td>
                    <div class="acciones">
                        <button class="btn btn-editar" onclick='editarEstudiante(${JSON.stringify(e)})'>Editar</button>
                        <button class="btn btn-eliminar" onclick="eliminarEstudiante(${e.id})">Eliminar</button>
                    </div>
                </td>
            </tr>
        `;
    });
}

function editarEstudiante(e) {
    limpiarErrores();

    document.getElementById("nombre").value = e.nombre;
    document.getElementById("apellido").value = e.apellido;
    document.getElementById("edad").value = e.edad;
    document.getElementById("carrera").value = e.carrera;
    document.getElementById("correo").value = e.correo;

    editando = true;
    idEditando = e.id;

    const btn = document.getElementById("btnGuardar");
    btn.textContent = "Actualizar";
    btn.className = "btn btn-editar";
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
    cargarEstudiantes();
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
                <td>${e.nombre}</td>
                <td>${e.apellido}</td>
                <td>${e.edad}</td>
                <td>${e.carrera}</td>
                <td>${e.correo}</td>
                <td>
                    <button class="btn btn-guardar" onclick="restaurarEstudiante(${e.id})">Restaurar</button>
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
    cargarPapelera();
}

async function vaciarPapelera() {
    const seguro = confirm("¿Estás seguro de vaciar definitivamente la papelera? Esta acción no se podrá deshacer.");

    if (!seguro) return;

    const respuesta = await fetch(`${API}/papelera/vaciar`, {
        method: "DELETE"
    });

    const data = await respuesta.json();

    alert(data.mensaje);
    cargarPapelera();
}

function limpiarFormulario() {
    if (!formulario) return;

    formulario.reset();
    limpiarErrores();

    editando = false;
    idEditando = null;

    const btn = document.getElementById("btnGuardar");
    btn.textContent = "Guardar";
    btn.className = "btn btn-guardar";
}