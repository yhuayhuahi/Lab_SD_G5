## Indicaciones para ejecutar el proyecto

1. Ubicación del proyecto

Asegúrate de estar ubicado en el directorio raíz del proyecto:

```bash
cd Lab_SD_G5/LAB01/Resultados/Caso02/
```

2. Comandos para ejecutar el proyecto

a. Compilar y ejecutar directamente
Si solo deseas ejecutar el proyecto, usa el siguiente 

comando:

```bash
gradle run
```

Esto compilará y ejecutará la aplicación automáticamente.

b. Compilar el proyecto
Si deseas compilar el proyecto sin ejecutarlo, usa:

```bash
gradle build
```

Esto generará los archivos compilados en la carpeta build.

c. Limpiar el proyecto
Si necesitas limpiar los archivos generados por Gradle, usa:

```bash
gradle clean
```

Esto eliminará la carpeta build y otros archivos temporales.

### Configuración necesaria en VS Code

Para trabajar correctamente con este proyecto en VS Code, asegúrate de tener las siguientes configuraciones:

1. Extensiones necesarias

- Extension Pack for Java: Incluye herramientas esenciales para trabajar con Java.
- Gradle for Java: Permite ejecutar tareas de Gradle directamente desde VS Code.

2. Configuración en settings.json

Agrega las siguientes configuraciones en el archivo settings.json de tu espacio de trabajo o usuario:

```json
{
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-17",
      "path": "C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.17.10-hotspot", // En mi caso, ajusta esta ruta según tu instalación de Java
      "default": true
    }
  ],
  "java.project.importOnFirstTimeStartup": "automatic",
  "java.project.referencedLibraries": [
    "app/build/libs/**/*.jar"
  ],
  "java.import.gradle.enabled": true,
  "java.import.gradle.wrapper.enabled": true
}
```

