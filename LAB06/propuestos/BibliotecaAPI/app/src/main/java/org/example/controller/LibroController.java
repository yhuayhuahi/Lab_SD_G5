package org.example.controller;

import org.example.model.Libro;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    // Lista en memoria para simular la base de datos
    private final List<Libro> biblioteca = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public LibroController() {
        // Libros iniciales por defecto
        biblioteca.add(new Libro(
                idGenerator.getAndIncrement(),
                "El Quijote",
                "Miguel de Cervantes",
                "Un hidalgo manchego se lanza a revivir la caballeria, confundiendo la realidad con sus ideales."
        ));
        biblioteca.add(new Libro(
                idGenerator.getAndIncrement(),
                "Cien anos de soledad",
                "Gabriel Garcia Marquez",
                "La saga de los Buendia en Macondo, entre lo cotidiano y lo fantastico."
        ));
        biblioteca.add(new Libro(
                idGenerator.getAndIncrement(),
                "La ciudad y los perros",
                "Mario Vargas Llosa",
                "En el Colegio Militar Leoncio Prado, un grupo de cadetes enfrenta violencia y lealtades." 
        ));
        biblioteca.add(new Libro(
                idGenerator.getAndIncrement(),
                "Pedro Paramo",
                "Juan Rulfo",
                "Juan Preciado llega a Comala para buscar a su padre y encuentra un pueblo de ecos y fantasmas."
        ));
        biblioteca.add(new Libro(
                idGenerator.getAndIncrement(),
                "Rayuela",
                "Julio Cortazar",
                "Una novela abierta sobre el amor, el azar y la busqueda de sentido entre Paris y Buenos Aires."
        ));
        biblioteca.add(new Libro(
                idGenerator.getAndIncrement(),
                "La fiesta del chivo",
                "Mario Vargas Llosa",
                "El fin de la dictadura de Trujillo en Republica Dominicana contado desde varias miradas."
        ));
    }

    // GET: Obtener todos los libros
    @GetMapping
    public List<Libro> obtenerTodos() {
        return biblioteca;
    }

    // GET: Obtener un libro por ID
    @GetMapping("/{id}")
    public Libro obtenerPorId(@PathVariable Long id) {
        return biblioteca.stream()
                .filter(libro -> libro.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado"));
    }

    // POST: Agregar un nuevo libro
    @PostMapping
    public Libro guardarLibro(@RequestBody Libro nuevoLibro) {
        nuevoLibro.setId(idGenerator.getAndIncrement());
        biblioteca.add(nuevoLibro);
        return nuevoLibro; // Devolvemos el libro con su ID asignado
    }

    // DELETE: Eliminar un libro por ID
    @DeleteMapping("/{id}")
    public void eliminarLibro(@PathVariable Long id) {
        boolean eliminado = biblioteca.removeIf(libro -> libro.getId().equals(id));
        if (!eliminado) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado");
        }
    }
}
