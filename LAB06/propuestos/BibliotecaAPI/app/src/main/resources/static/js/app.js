const API_URL = "/api/libros";

const listContainer = document.querySelector("#lista");
const statusEl = document.querySelector("#estado");
const formCrear = document.querySelector("#form-crear");
const formBuscar = document.querySelector("#form-buscar");
const formEliminar = document.querySelector("#form-eliminar");
const buscarResultado = document.querySelector("#resultado-buscar");
const btnRecargar = document.querySelector("#btn-recargar");

const inputTitulo = document.querySelector("#titulo");
const inputAutor = document.querySelector("#autor");
const inputResumen = document.querySelector("#resumen");
const inputBuscarId = document.querySelector("#buscar-id");
const inputEliminarId = document.querySelector("#eliminar-id");

const setStatus = (message, isError = false) => {
    statusEl.textContent = message;
    statusEl.style.color = isError ? "#8a2a14" : "";
};

const renderList = (libros) => {
    listContainer.innerHTML = "";
    if (!libros.length) {
        listContainer.innerHTML = "<p class=\"status\">No hay libros registrados.</p>";
        return;
    }
    libros.forEach((libro) => {
        const card = document.createElement("article");
        card.className = "book";
        card.innerHTML = `
            <h3>${libro.titulo}</h3>
            <p><strong>ID:</strong> ${libro.id} · <strong>Autor:</strong> ${libro.autor}</p>
            <p>${libro.resumen || "Sin resumen"}</p>
        `;
        listContainer.appendChild(card);
    });
};

const fetchLibros = async () => {
    setStatus("Cargando libros...");
    try {
        const res = await fetch(API_URL);
        if (!res.ok) {
            throw new Error("No se pudo cargar la lista");
        }
        const data = await res.json();
        renderList(data);
        setStatus(`Libros cargados: ${data.length}`);
    } catch (error) {
        setStatus(error.message, true);
    }
};

formCrear.addEventListener("submit", async (event) => {
    event.preventDefault();
    const payload = {
        titulo: inputTitulo.value.trim(),
        autor: inputAutor.value.trim(),
        resumen: inputResumen.value.trim(),
    };
    setStatus("Guardando libro...");
    try {
        const res = await fetch(API_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });
        if (!res.ok) {
            throw new Error("No se pudo guardar el libro");
        }
        inputTitulo.value = "";
        inputAutor.value = "";
        inputResumen.value = "";
        await fetchLibros();
        setStatus("Libro registrado correctamente");
    } catch (error) {
        setStatus(error.message, true);
    }
});

const renderSingleBook = (libro, container) => {
    container.innerHTML = "";
    const card = document.createElement("article");
    card.className = "book";
    card.innerHTML = `
        <h3>${libro.titulo}</h3>
        <p><strong>ID:</strong> ${libro.id} · <strong>Autor:</strong> ${libro.autor}</p>
        <p>${libro.resumen || "Sin resumen"}</p>
    `;
    container.appendChild(card);
};

formBuscar.addEventListener("submit", async (event) => {
    event.preventDefault();
    const id = inputBuscarId.value.trim();
    buscarResultado.innerHTML = '<p class="status">Buscando...</p>';
    try {
        const res = await fetch(`${API_URL}/${id}`);
        if (!res.ok) {
            throw new Error("Libro no encontrado");
        }
        const libro = await res.json();
        renderSingleBook(libro, buscarResultado);
    } catch (error) {
        buscarResultado.innerHTML = `<p class="status error">${error.message}</p>`;
    }
});

formEliminar.addEventListener("submit", async (event) => {
    event.preventDefault();
    const id = inputEliminarId.value.trim();
    setStatus("Eliminando libro...");
    try {
        const res = await fetch(`${API_URL}/${id}`, { method: "DELETE" });
        if (!res.ok) {
            throw new Error("Libro no encontrado");
        }
        inputEliminarId.value = "";
        await fetchLibros();
        setStatus("Libro eliminado");
    } catch (error) {
        setStatus(error.message, true);
    }
});

btnRecargar.addEventListener("click", fetchLibros);

fetchLibros();
