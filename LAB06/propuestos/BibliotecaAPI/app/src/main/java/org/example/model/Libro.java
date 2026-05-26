package org.example.model;

public class Libro {
    private Long id;
    private String titulo;
    private String autor;
    private String resumen;

    // Constructor vacío requerido por Spring para deserializar JSON (POST)
    public Libro() {}

    public Libro(Long id, String titulo, String autor, String resumen) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.resumen = resumen;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }
}
