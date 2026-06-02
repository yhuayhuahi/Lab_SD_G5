# Guía Paso a Paso: Cliente SOAP en Python

## 🎯 Objetivo

Consumir el servicio SOAP de Calculadora desde Python usando la librería **Zeep**.

---

## 📋 Requisitos Previos

- Python 3.7 o superior instalado
- El servidor SOAP corriendo en `http://localhost:8080/calculadora`
- Acceso a internet para descargar dependencias

---

## ✅ Paso 1: Instalar Zeep

Abre una terminal/PowerShell y ejecuta:

```bash
pip install zeep==4.2.1
```

O si estás en el directorio del proyecto:

```bash
pip install -r requirements.txt
```

**Verifica la instalación:**

```bash
python -c "import zeep; print(zeep.__version__)"
```

---

## ✅ Paso 2: Iniciar el Servidor SOAP

En **otra terminal**, navega a la carpeta del servidor:

```bash
cd ../act_adicional/SoapCalculator
```

Ejecuta el servidor:

**En Windows:**
```bash
gradlew.bat run
```

**En Linux/Mac:**
```bash
./gradlew run
```

**Verifica que esté corriendo:**
- Abre tu navegador en: `http://localhost:8080/calculadora?wsdl`
- Debes ver un documento XML con la descripción del servicio

---

## ✅ Paso 3: Ejecutar el Cliente SOAP (Modo Interactivo)

En la carpeta actual, ejecuta:

```bash
python cliente_soap.py
```

**Verás un menú como este:**

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
Selecciona una operación (1-5): 
```

---

## ✅ Paso 4: Interactúa con el Servicio

### Ejemplo: Sumar 15 + 8

```
Selecciona una operación (1-5): 1
Ingresa el primer número: 15
Ingresa el segundo número: 8

  15 + 8 = 23
```

### Ejemplo: Dividir entre cero (manejo de errores)

```
Selecciona una operación (1-5): 4
Ingresa el primer número: 10
Ingresa el segundo número: 0

  ✗ Error al ejecutar operación: No se puede dividir entre cero
```

---

## ✅ Paso 5: Ejecutar Ejemplos Automáticos

Si prefieres ver ejemplos sin menú interactivo:

```bash
python cliente_soap.py --modo ejemplos
```

**Salida esperada:**

```
✓ Conectado exitosamente al servidor SOAP en: http://localhost:8080/calculadora?wsdl

============================================================
EJEMPLOS DE USO DEL CLIENTE SOAP
============================================================

Ejecutando: sumar(10, 5)
  10 + 5 = 15

Ejecutando: restar(20, 8)
  20 - 8 = 12

Ejecutando: multiplicar(6, 7)
  6 × 7 = 42

Ejecutando: dividir(30, 3)
  30 ÷ 3 = 10

Ejecutando: dividir(10, 0) - Esta operación debe generar un error
  ✗ Error al ejecutar operación: No se puede dividir entre cero
```

---

## ✅ Paso 6: Ejecutar Ejemplos Avanzados

Para ver más casos de uso programático:

```bash
python ejemplos_avanzados.py
```

**Incluye:**
- Uso básico del cliente
- Manejo de excepciones
- Procesamiento de lotes
- Validación de entrada
- Introspección del servicio
- Operaciones encadenadas

---

## 🔧 Usar el Cliente desde tu Propio Código

Si quieres usar el cliente en tu propio script Python:

```python
from zeep import Client

# Conectar al servicio
client = Client(wsdl="http://localhost:8080/calculadora?wsdl")
service = client.bind('CalculadoraSOAPService', 'CalculadoraSOAPPort')

# Usar las operaciones
resultado = service.sumar(10, 5)
print(f"10 + 5 = {resultado}")

resultado = service.multiplicar(3, 4)
print(f"3 × 4 = {resultado}")

# Manejo de errores
try:
    resultado = service.dividir(10, 0)
except Exception as e:
    print(f"Error: {e}")
```

---

## 📚 Alternativa: Usar la Clase Wrapper

Para un código más limpio:

```python
from ejemplos_avanzados import ClienteCalculadoraSOAP

# Inicializar cliente
cliente = ClienteCalculadoraSOAP()

# Usar operaciones
print(cliente.sumar(20, 8))           # 28
print(cliente.multiplicar(5, 6))      # 30
print(cliente.dividir(100, 4))        # 25
```

---

## ❌ Solución de Problemas

### Error: "No se puede conectar con el servidor"

**Causa:** El servidor SOAP no está corriendo

**Solución:**
1. Verifica que el servidor está en ejecución
2. Abre `http://localhost:8080/calculadora?wsdl` en el navegador
3. Si ves XML, el servidor está bien
4. Reinicia el servidor si es necesario

### Error: "No module named 'zeep'"

**Causa:** Zeep no está instalado

**Solución:**
```bash
pip install zeep==4.2.1
```

### Error: "WSDL download failed"

**Causa:** Problema de conexión o URL incorrecta

**Solución:**
1. Verifica que la URL sea `http://localhost:8080/calculadora?wsdl`
2. Revisa tu conexión de red
3. Verifica el firewall

---

## 📖 Conceptos Clave

### WSDL (Web Services Description Language)
Documento XML que describe el servicio SOAP disponible en:
```
http://localhost:8080/calculadora?wsdl
```

### SOAP (Simple Object Access Protocol)
Protocolo que usa XML para enviar/recibir mensajes entre cliente y servidor.

### Zeep
Librería Python que:
1. Descarga el WSDL
2. Parsea la descripción del servicio
3. Crea un cliente automáticamente
4. Convierte llamadas Python a XML SOAP

### Binding
En Zeep, el binding vincula el cliente con el servicio específico:
```python
service = client.bind('CalculadoraSOAPService', 'CalculadoraSOAPPort')
```

---

## 🎓 Ejercicios Propuestos

### Ejercicio 1: Crear una Calculadora Encadenada
```python
# Calcular: ((50 + 25) × 2) - 20 ÷ 4
```

### Ejercicio 2: Validar Entrada del Usuario
```python
# Pedir números al usuario y validar que sean números válidos
```

### Ejercicio 3: Guardar Historial
```python
# Mantener un registro de todas las operaciones realizadas
```

### Ejercicio 4: Crear Interfaz GUI
```python
# Usar tkinter o PyQt para crear interfaz gráfica
```

---

## 📝 Resumen

| Paso | Acción | Comando |
|------|--------|---------|
| 1 | Instalar Zeep | `pip install -r requirements.txt` |
| 2 | Iniciar servidor | `gradlew.bat run` |
| 3 | Ejecutar cliente | `python cliente_soap.py` |
| 4 | Ver ejemplos | `python cliente_soap.py --modo ejemplos` |
| 5 | Ejemplos avanzados | `python ejemplos_avanzados.py` |

---

## ✨ ¿Qué aprendiste?

✅ Qué es SOAP y cómo funciona  
✅ Qué es WSDL y para qué sirve  
✅ Cómo usar la librería Zeep  
✅ Cómo consumir un servicio SOAP desde Python  
✅ Cómo manejar errores en SOAP  
✅ Por qué SOAP tiene limitaciones en navegadores

---

## 📚 Referencias

- [Documentación Zeep](https://docs.python-zeep.org/)
- [SOAP Specification W3C](https://www.w3.org/TR/soap12/)
- [WSDL Specification W3C](https://www.w3.org/TR/wsdl.html)

