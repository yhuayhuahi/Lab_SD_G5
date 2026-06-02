"""
Ejemplos avanzados de uso del cliente SOAP en Python.

Este archivo demuestra diferentes formas de consumir el servicio SOAP
de forma programática, incluyendo:
- Acceso directo sin menú
- Manejo de errores
- Introspección del servicio
- Llamadas en lotes
"""

from zeep import Client
from zeep.wsdl import wsdl
import json
from datetime import datetime


class ClienteCalculadoraSOAP:
    """
    Clase wrapper para consumir el servicio SOAP de Calculadora.
    Proporciona métodos de alto nivel para interactuar con el servicio.
    """
    
    def __init__(self, url="http://localhost:8080/calculadora?wsdl"):
        """
        Inicializa el cliente SOAP.
        
        Args:
            url: URL del WSDL del servicio SOAP
        """
        try:
            self.client = Client(wsdl=url)
            # Usar el proxy de servicio por defecto (evita nombres hardcodeados)
            self.service = self.client.service
            self.url = url
            print(f"✓ Cliente inicializado correctamente\n")
        except Exception as e:
            print(f"✗ Error al inicializar cliente: {e}")
            raise
    
    def sumar(self, a, b):
        """Suma dos números"""
        return self.service.sumar(a, b)
    
    def restar(self, a, b):
        """Resta dos números"""
        return self.service.restar(a, b)
    
    def multiplicar(self, a, b):
        """Multiplica dos números"""
        return self.service.multiplicar(a, b)
    
    def dividir(self, a, b):
        """Divide dos números"""
        try:
            return self.service.dividir(a, b)
        except Exception as e:
            return f"Error: {str(e)}"
    
    def obtener_informacion_servicio(self):
        """Obtiene información sobre el servicio SOAP"""
        info = {
            "url": self.url,
            "timestamp": datetime.now().isoformat(),
            "operaciones": [
                "sumar(a, b)",
                "restar(a, b)",
                "multiplicar(a, b)",
                "dividir(a, b)"
            ]
        }
        return info


def ejemplo_1_uso_basico():
    """
    Ejemplo 1: Uso básico del cliente SOAP.
    Demuestra cómo llamar a cada operación.
    """
    print("=" * 70)
    print("EJEMPLO 1: USO BÁSICO DEL CLIENTE SOAP")
    print("=" * 70 + "\n")
    
    cliente = ClienteCalculadoraSOAP()
    
    print("Ejecutando operaciones básicas:")
    print(f"  20 + 8 = {cliente.sumar(20, 8)}")
    print(f"  50 - 12 = {cliente.restar(50, 12)}")
    print(f"  7 × 6 = {cliente.multiplicar(7, 6)}")
    print(f"  100 ÷ 4 = {cliente.dividir(100, 4)}")
    print()


def ejemplo_2_manejo_excepciones():
    """
    Ejemplo 2: Manejo de excepciones.
    Demuestra cómo manejar errores del servicio SOAP.
    """
    print("=" * 70)
    print("EJEMPLO 2: MANEJO DE EXCEPCIONES")
    print("=" * 70 + "\n")
    
    cliente = ClienteCalculadoraSOAP()
    
    print("Intentando operaciones que pueden generar errores:\n")
    
    # Operación válida
    print("1. División válida: 50 ÷ 5")
    try:
        resultado = cliente.dividir(50, 5)
        print(f"   Resultado: {resultado}\n")
    except Exception as e:
        print(f"   Error: {e}\n")
    
    # Operación inválida: división por cero
    print("2. División por cero: 50 ÷ 0")
    try:
        resultado = cliente.dividir(50, 0)
        print(f"   Resultado: {resultado}\n")
    except Exception as e:
        print(f"   Error capturado: {str(e)}\n")


def ejemplo_3_procesamiento_lotes():
    """
    Ejemplo 3: Procesamiento de lotes.
    Demuestra cómo procesar múltiples operaciones.
    """
    print("=" * 70)
    print("EJEMPLO 3: PROCESAMIENTO DE LOTES")
    print("=" * 70 + "\n")
    
    cliente = ClienteCalculadoraSOAP()
    
    # Lote de operaciones
    operaciones = [
        ("sumar", 15, 7),
        ("restar", 30, 10),
        ("multiplicar", 5, 8),
        ("dividir", 48, 6),
    ]
    
    print("Procesando lote de operaciones:\n")
    resultados = []
    
    for operacion, a, b in operaciones:
        try:
            if operacion == "sumar":
                resultado = cliente.sumar(a, b)
                operacion_str = f"{a} + {b}"
            elif operacion == "restar":
                resultado = cliente.restar(a, b)
                operacion_str = f"{a} - {b}"
            elif operacion == "multiplicar":
                resultado = cliente.multiplicar(a, b)
                operacion_str = f"{a} × {b}"
            elif operacion == "dividir":
                resultado = cliente.dividir(a, b)
                operacion_str = f"{a} ÷ {b}"
            
            print(f"  {operacion_str} = {resultado}")
            resultados.append({
                "operacion": operacion_str,
                "resultado": resultado,
                "exito": True
            })
        except Exception as e:
            print(f"  Error en {operacion_str}: {str(e)}")
            resultados.append({
                "operacion": operacion_str,
                "error": str(e),
                "exito": False
            })
    
    print(f"\nOperaciones procesadas: {len(resultados)}")
    print(f"Éxitos: {sum(1 for r in resultados if r['exito'])}")
    print()


def ejemplo_4_validacion_entrada():
    """
    Ejemplo 4: Validación de entrada.
    Demuestra cómo validar entradas antes de enviar al servidor.
    """
    print("=" * 70)
    print("EJEMPLO 4: VALIDACIÓN DE ENTRADA")
    print("=" * 70 + "\n")
    
    cliente = ClienteCalculadoraSOAP()
    
    def validar_y_operar(operacion, a, b):
        """Valida entrada y realiza la operación"""
        # Validar tipos
        if not isinstance(a, (int, float)) or not isinstance(b, (int, float)):
            return f"✗ Error: Los operandos deben ser números"
        
        # Validar operación
        operaciones_validas = ["sumar", "restar", "multiplicar", "dividir"]
        if operacion not in operaciones_validas:
            return f"✗ Error: Operación '{operacion}' no válida"
        
        # Validación específica para división
        if operacion == "dividir" and b == 0:
            return "⚠ Advertencia: No se puede dividir entre cero"
        
        # Ejecutar operación
        try:
            if operacion == "sumar":
                resultado = cliente.sumar(int(a), int(b))
            elif operacion == "restar":
                resultado = cliente.restar(int(a), int(b))
            elif operacion == "multiplicar":
                resultado = cliente.multiplicar(int(a), int(b))
            elif operacion == "dividir":
                resultado = cliente.dividir(int(a), int(b))
            
            return f"✓ Resultado: {resultado}"
        except Exception as e:
            return f"✗ Error en la operación: {str(e)}"
    
    # Pruebas de validación
    pruebas = [
        ("sumar", 10, 5),
        ("sumar", "abc", 5),  # Entrada inválida
        ("multiplicar", 3, 4),
        ("dividir", 20, 0),   # Validación de advertencia
        ("operacion_inexistente", 1, 2),  # Operación inválida
    ]
    
    print("Pruebas de validación:\n")
    for operacion, a, b in pruebas:
        resultado = validar_y_operar(operacion, a, b)
        print(f"  {operacion}({a}, {b})")
        print(f"  → {resultado}\n")


def ejemplo_5_introspecccion_servicio():
    """
    Ejemplo 5: Introspección del servicio.
    Demuestra cómo obtener información sobre el servicio.
    """
    print("=" * 70)
    print("EJEMPLO 5: INTROSPECCIÓN DEL SERVICIO")
    print("=" * 70 + "\n")
    
    cliente = ClienteCalculadoraSOAP()
    
    info = cliente.obtener_informacion_servicio()
    
    print("Información del servicio SOAP:")
    print(f"  URL: {info['url']}")
    print(f"  Conectado en: {info['timestamp']}")
    print(f"  Operaciones disponibles:")
    for op in info['operaciones']:
        print(f"    - {op}")
    print()


def ejemplo_6_operaciones_encadenadas():
    """
    Ejemplo 6: Operaciones encadenadas.
    Demuestra cómo encadenar múltiples operaciones.
    """
    print("=" * 70)
    print("EJEMPLO 6: OPERACIONES ENCADENADAS")
    print("=" * 70 + "\n")
    
    cliente = ClienteCalculadoraSOAP()
    
    print("Cálculo encadenado: ((10 + 5) × 3) - 15 ÷ 3\n")
    
    # Paso 1: 10 + 5
    paso1 = cliente.sumar(10, 5)
    print(f"Paso 1: 10 + 5 = {paso1}")
    
    # Paso 2: resultado × 3
    paso2 = cliente.multiplicar(paso1, 3)
    print(f"Paso 2: {paso1} × 3 = {paso2}")
    
    # Paso 3: 15 ÷ 3
    paso3 = cliente.dividir(15, 3)
    print(f"Paso 3: 15 ÷ 3 = {paso3}")
    
    # Paso 4: paso2 - paso3
    resultado_final = cliente.restar(paso2, paso3)
    print(f"Paso 4: {paso2} - {paso3} = {resultado_final}")
    print(f"\nResultado final: {resultado_final}\n")


def main():
    """Ejecuta todos los ejemplos"""
    print("\n")
    print("█" * 70)
    print("EJEMPLOS AVANZADOS DE USO DEL CLIENTE SOAP")
    print("█" * 70)
    print("\n")
    
    try:
        ejemplo_1_uso_basico()
        ejemplo_2_manejo_excepciones()
        ejemplo_3_procesamiento_lotes()
        ejemplo_4_validacion_entrada()
        ejemplo_5_introspecccion_servicio()
        ejemplo_6_operaciones_encadenadas()
        
        print("=" * 70)
        print("✓ Todos los ejemplos ejecutados exitosamente")
        print("=" * 70 + "\n")
        
    except Exception as e:
        print(f"\n✗ Error general: {e}")
        print("\nAsegúrate de que el servidor SOAP está corriendo en:")
        print("  http://localhost:8080/calculadora")


if __name__ == "__main__":
    main()
