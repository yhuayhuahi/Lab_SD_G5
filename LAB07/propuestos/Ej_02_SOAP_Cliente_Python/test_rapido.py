"""
Script rápido para probar el cliente SOAP.

Uso:
    python test_rapido.py
    
Este script ejecuta una serie de pruebas rápidas del cliente SOAP.
"""

from zeep import Client
from zeep.exceptions import Fault
import sys


def test_conexion():
    """Prueba la conexión con el servidor SOAP"""
    print("🔌 Probando conexión con el servidor...")
    try:
        client = Client(wsdl="http://localhost:8080/calculadora?wsdl")
        print("✓ Conexión establecida correctamente\n")
        return client
    except Exception as e:
        print(f"✗ Error de conexión: {e}")
        print("\n❌ El servidor SOAP no está disponible.")
        print("Asegúrate de que está corriendo en: http://localhost:8080/calculadora")
        sys.exit(1)


def test_operaciones(client):
    """Prueba las operaciones disponibles"""
    print("🧮 Probando operaciones SOAP...")
    
    # Usar proxy de servicio por defecto
    service = client.service
    
    # Casos de prueba
    casos = [
        ("sumar", 25, 15, service.sumar),
        ("restar", 100, 30, service.restar),
        ("multiplicar", 8, 7, service.multiplicar),
        ("dividir", 144, 12, service.dividir),
    ]
    
    print()
    for nombre, a, b, operacion in casos:
        try:
            resultado = operacion(a, b)
            print(f"  ✓ {nombre}({a}, {b}) = {resultado}")
        except Exception as e:
            print(f"  ✗ {nombre}({a}, {b}) falló: {e}")
    
    print()


def test_manejo_errores(client):
    """Prueba el manejo de errores"""
    print("⚠️  Probando manejo de errores...")
    
    # Usar proxy de servicio por defecto
    service = client.service
    
    print()
    print("  Intentando división por cero (debe generar error)...")
    try:
        resultado = service.dividir(10, 0)
        print(f"  ✗ Inesperado: No se lanzó error. Resultado: {resultado}")
    except Fault as e:
        print(f"  ✓ Error capturado correctamente: {e}")
    except Exception as e:
        print(f"  ✓ Error capturado correctamente: {e}")
    
    print()


def main():
    """Ejecuta todas las pruebas"""
    print("\n")
    print("=" * 60)
    print("PRUEBA RÁPIDA DEL CLIENTE SOAP")
    print("=" * 60)
    print()
    
    # Paso 1: Probar conexión
    client = test_conexion()
    
    # Paso 2: Probar operaciones
    test_operaciones(client)
    
    # Paso 3: Probar manejo de errores
    test_manejo_errores(client)
    
    print("=" * 60)
    print("✓ Todas las pruebas completadas exitosamente")
    print("=" * 60)
    print()
    print("Próximos pasos:")
    print("  • python cliente_soap.py          (modo interactivo)")
    print("  • python cliente_soap.py --modo ejemplos")
    print("  • python ejemplos_avanzados.py    (ejemplos completos)")
    print()


if __name__ == "__main__":
    main()
