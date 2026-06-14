"""
Cliente SOAP en Python
Consume el servicio de Calculadora SOAP expuesto en http://localhost:8080/calculadora

Requisitos:
- Tener instalado zeep: pip install zeep
- Tener el servidor SOAP corriendo en http://localhost:8080/calculadora
"""

from zeep import Client
from zeep.wsdl import wsdl
import sys


def obtener_cliente():
    """
    Obtiene el cliente SOAP conectado al servidor.
    
    Returns:
        Client: Cliente SOAP configurado
    """
    # La URL del WSDL se obtiene agregando ?wsdl a la URL del servicio SOAP
    wsdl_url = "http://localhost:8080/calculadora?wsdl"
    
    try: # wsdl significa Web Services Description Language, es un formato XML que describe los servicios web disponibles, sus métodos, parámetros y tipos de datos.
        client = Client(wsdl=wsdl_url)
        print(f"✓ Conectado exitosamente al servidor SOAP en: {wsdl_url}\n")
        return client
    except Exception as e:
        print(f"✗ Error al conectar con el servidor SOAP: {e}")
        print("Asegúrate de que el servidor está corriendo en http://localhost:8080/calculadora")
        sys.exit(1)


def mostrar_operaciones_disponibles(client):
    """
    Muestra las operaciones disponibles en el servicio SOAP.
    
    Args:
        client: Cliente SOAP
    """
    print("=" * 60) # Imprime una línea de separación para mejorar la legibilidad
    print("OPERACIONES DISPONIBLES EN EL SERVICIO SOAP")
    print("=" * 60)
    
    # Mostrar servicios/puertos detectados en el WSDL (útil para depuración)
    try:
        services = client.wsdl.services # services contendra los servicios definidos en el WSDL, cada servicio puede tener uno o más puertos (endpoints) que representan diferentes formas de acceder al servicio.
        print("\nServicios WSDL detectados:")
        for svc_name, svc in services.items():
            print(f" - {svc_name}:")
            for port_name, port in svc.ports.items():
                print(f"    - Puerto: {port_name}")
    except Exception:
        pass

    # Obtener el proxy de servicio por defecto
    service = client.service
    
    print("\nMétodos disponibles:")
    print("  1. sumar(a, b)")
    print("  2. restar(a, b)")
    print("  3. multiplicar(a, b)")
    print("  4. dividir(a, b) - Puede lanzar excepción si b=0\n")


def ejecutar_operacion(client, operacion, a, b):
    """
    Ejecuta una operación SOAP.
    
    Args:
        client: Cliente SOAP
        operacion: Nombre de la operación (sumar, restar, multiplicar, dividir)
        a: Primer operando
        b: Segundo operando
        
    Returns:
        int: Resultado de la operación
    """
    service = client.service
    
    try:
        if operacion == "sumar":
            resultado = service.sumar(a, b)
            print(f"  {a} + {b} = {resultado}")
        elif operacion == "restar":
            resultado = service.restar(a, b)
            print(f"  {a} - {b} = {resultado}")
        elif operacion == "multiplicar":
            resultado = service.multiplicar(a, b)
            print(f"  {a} × {b} = {resultado}")
        elif operacion == "dividir":
            resultado = service.dividir(a, b)
            print(f"  {a} ÷ {b} = {resultado}")
        else:
            print(f"  ✗ Operación no válida: {operacion}")
            return None
        
        return resultado
    except Exception as e:
        print(f"  ✗ Error al ejecutar operación: {e}")
        return None


def menu_interactivo():
    """
    Menú interactivo para consumir el servicio SOAP.
    """
    client = obtener_cliente()
    mostrar_operaciones_disponibles(client)
    
    while True:
        print("=" * 60)
        print("MENÚ PRINCIPAL")
        print("=" * 60)
        print("1. Sumar")
        print("2. Restar")
        print("3. Multiplicar")
        print("4. Dividir")
        print("5. Salir")
        print("-" * 60)
        
        opcion = input("Selecciona una operación (1-5): ").strip()
        
        if opcion == "5":
            print("\n¡Hasta luego!")
            break
        
        if opcion not in ["1", "2", "3", "4"]:
            print("✗ Opción no válida. Intenta de nuevo.\n")
            continue
        
        try:
            a = int(input("Ingresa el primer número: "))
            b = int(input("Ingresa el segundo número: "))
        except ValueError:
            print("✗ Por favor ingresa números válidos.\n")
            continue
        
        print()
        
        operaciones = {
            "1": "sumar",
            "2": "restar",
            "3": "multiplicar",
            "4": "dividir"
        }
        
        operacion = operaciones[opcion]
        ejecutar_operacion(client, operacion, a, b)
        print()


def ejemplos_directos():
    """
    Ejecuta algunos ejemplos directos sin menú interactivo.
    """
    client = obtener_cliente()
    
    print("=" * 60)
    print("EJEMPLOS DE USO DEL CLIENTE SOAP")
    print("=" * 60 + "\n")
    
    # Ejemplos de cada operación
    ejemplos = [
        ("sumar", 10, 5),
        ("restar", 20, 8),
        ("multiplicar", 6, 7),
        ("dividir", 30, 3),
    ]
    
    for operacion, a, b in ejemplos:
        print(f"Ejecutando: {operacion}({a}, {b})")
        ejecutar_operacion(client, operacion, a, b)
        print()
    
    # Ejemplo de error: división entre cero
    print("Ejecutando: dividir(10, 0) - Esta operación debe generar un error")
    ejecutar_operacion(client, "dividir", 10, 0)
    print()


if __name__ == "__main__":
    import argparse
    
    parser = argparse.ArgumentParser(
        description="Cliente SOAP para consumir servicio de Calculadora"
    )
    parser.add_argument(
        "--modo",
        choices=["interactivo", "ejemplos"],
        default="interactivo",
        help="Modo de ejecución (default: interactivo)"
    )
    
    args = parser.parse_args()
    
    if args.modo == "ejemplos":
        ejemplos_directos()
    else:
        menu_interactivo()
