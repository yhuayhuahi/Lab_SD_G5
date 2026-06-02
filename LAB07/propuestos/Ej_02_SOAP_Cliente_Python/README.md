# Ejercicio 02: Cliente SOAP con Python

## Descripción

Este ejercicio demuestra cómo consumir un servicio SOAP existente usando Python.

El cliente se conecta a un **Servicio SOAP de Calculadora** que expone las siguientes operaciones:
- **sumar(a, b)**: Suma dos números
- **restar(a, b)**: Resta dos números
- **multiplicar(a, b)**: Multiplica dos números
- **dividir(a, b)**: Divide dos números (lanza excepción si divisor es 0)

El servicio SOAP está disponible en: `http://localhost:8080/calculadora`

## Requisitos

- Python 3.7 o superior
- El servidor SOAP debe estar corriendo (ver `../act_adicional/SoapCalculator/`)

## Instalación

### 1. Instalar dependencias

```bash
pip install -r requirements.txt
```

O instalar manualmente:

```bash
pip install zeep==4.2.1
```

### 2. Iniciar el servidor SOAP

En la carpeta `../act_adicional/SoapCalculator/`:

```bash
./gradlew run
```

O en Windows:

```bash
gradlew.bat run
```

El servidor se iniciará en `http://localhost:8080/calculadora`

## Uso

### Modo Interactivo (Default)

```bash
python cliente_soap.py
```

Este modo presenta un menú interactivo donde puedes:
1. Seleccionar la operación (sumar, restar, multiplicar, dividir)
2. Ingresar los operandos
3. Ver el resultado

### Modo Ejemplos

```bash
python cliente_soap.py --modo ejemplos
```

Este modo ejecuta automáticamente varios ejemplos de uso del cliente SOAP:
- Sumar: 10 + 5
- Restar: 20 - 8
- Multiplicar: 6 × 7
- Dividir: 30 ÷ 3
- Dividir entre cero (genera error)

## Detalles Técnicos

### Biblioteca Zeep

**Zeep** es una librería Python para consumir servicios SOAP. Características:
- Descarga automática del WSDL
- Mapeo automático de tipos de datos
- Soporte para autenticación
- Manejo de excepciones SOAP

### WSDL (Web Services Description Language)

El cliente obtiene la descripción del servicio SOAP desde:
```
http://localhost:8080/calculadora?wsdl
```

Este archivo XML describe:
- Métodos disponibles
- Parámetros de cada método
- Tipos de datos
- Ubicación del servicio

### Flujo de Consumo

```
Cliente SOAP (Python)
      ↓
   zeep.Client
      ↓
HTTP/SOAP Request → http://localhost:8080/calculadora
      ↓
   Servidor SOAP (Java)
      ↓
Parsear XML SOAP
      ↓
Ejecutar método
      ↓
Generar XML SOAP response
      ↓
HTTP/SOAP Response → Cliente
      ↓
zeep parsea respuesta
      ↓
Retorna resultado
```

## Limitaciones de SOAP en Navegadores

Aunque el servidor SOAP tiene soporte CORS implementado, los navegadores modernos limitan significativamente el consumo directo de SOAP:

1. **Restricciones CORS**: Los navegadores bloquean peticiones POST a otros dominios sin headers CORS apropiados
2. **Falta de estándares**: SOAP es XML complejo; no hay una API estándar en JavaScript para parsearlo
3. **Complejidad**: SOAP requiere envelopes XML bien formados

Por eso SOAP se consume mejor desde:
- Clientes de escritorio (Java, C#, Python, etc.)
- Servidores backend (Node.js, Python, Java, etc.)
- Herramientas especializadas (SoapUI, Postman)

## Estructura del Código

```
cliente_soap.py
├── obtener_cliente()           # Conecta con el servidor SOAP
├── mostrar_operaciones_disponibles()  # Lista las operaciones
├── ejecutar_operacion()        # Ejecuta una operación SOAP
├── menu_interactivo()          # Menú para usuario
└── ejemplos_directos()         # Ejecuta ejemplos automáticos
```

## Manejo de Errores

El cliente incluye manejo para:
- Conexión fallida al servidor
- Operación SOAP fallida
- División entre cero

## Ejemplo de Salida

```
✓ Conectado exitosamente al servidor SOAP en: http://localhost:8080/calculadora?wsdl

============================================================
OPERACIONES DISPONIBLES EN EL SERVICIO SOAP
============================================================

Métodos disponibles:
  1. sumar(a, b)
  2. restar(a, b)
  3. multiplicar(a, b)
  4. dividir(a, b) - Puede lanzar excepción si b=0

============================================================
MENÚ PRINCIPAL
============================================================
1. Sumar
2. Restar
3. Multiplicar
4. Dividir
5. Salir
------------------------------------------------------------
Selecciona una operación (1-5): 1
Ingresa el primer número: 15
Ingresa el segundo número: 7

  15 + 7 = 22

```

## Referencias

- [Documentación de Zeep](https://docs.python-zeep.org/)
- [SOAP Specification](https://www.w3.org/TR/soap12/)
- [WSDL Specification](https://www.w3.org/TR/wsdl.html)

