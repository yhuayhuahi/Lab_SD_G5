import psycopg2
import time
import sys

# Configuración de conexiones
DB_CONFIG = {
    'arequipa': {'host': 'localhost', 'port': 5433, 'dbname': 'banco_arequipa', 'user': 'postgres', 'password': 'postgres'},
    'cusco': {'host': 'localhost', 'port': 5434, 'dbname': 'banco_cusco', 'user': 'postgres', 'password': 'postgres'}
}

def connect(node_config):
    """Establece conexión a un nodo específico."""
    return psycopg2.connect(**node_config)

def print_balances():
    """Muestra el estado actual de las cuentas en Arequipa y Cusco."""
    try:
        with connect(DB_CONFIG['arequipa']) as ca, connect(DB_CONFIG['cusco']) as cc:
            with ca.cursor() as cura, cc.cursor() as curc:
                cura.execute("SELECT id, owner, balance FROM accounts WHERE owner = 'Cuenta_Arequipa';")
                curc.execute("SELECT id, owner, balance FROM accounts WHERE owner = 'Cuenta_Cusco';")
                
                print("\n--- Saldos Actuales ---")
                print(f"Arequipa: {cura.fetchall()}")
                print(f"Cusco:    {curc.fetchall()}")
                print("-----------------------\n")
    except Exception as e:
        print(f"Error al obtener saldos: {e}")

def run_two_phase_commit(amount):
    """Ejecuta el protocolo 2PC para transferir dinero de Arequipa a Cusco."""
    print(f"Iniciando transferencia de S/ {amount} (Arequipa -> Cusco)...")
    
    conn_a = None
    conn_c = None

    try:
        # 1. Establecer conexiones
        conn_a = connect(DB_CONFIG['arequipa'])
        conn_c = connect(DB_CONFIG['cusco'])
        
        cur_a = conn_a.cursor()
        cur_c = conn_c.cursor()

        # 2. Generar Identificador Global de Transacción (GTRID)
        gtrid = f"tx_banco_{int(time.time())}"
        
        # 3. Crear XIDs para cada participante
        xid_a = conn_a.xid(42, gtrid, 'arequipa')
        xid_c = conn_c.xid(42, gtrid, 'cusco')

        # 4. Iniciar transacciones TPC
        conn_a.tpc_begin(xid_a)
        conn_c.tpc_begin(xid_c)

        # 5. Ejecutar operaciones DML locales
        print("Ejecutando operaciones en los nodos...")
        cur_a.execute("UPDATE accounts SET balance = balance - %s WHERE owner = 'Cuenta_Arequipa' RETURNING balance;", (amount,))
        res_a = cur_a.fetchone()
        
        cur_c.execute("UPDATE accounts SET balance = balance + %s WHERE owner = 'Cuenta_Cusco' RETURNING balance;", (amount,))
        res_c = cur_c.fetchone()

        if not res_a or not res_c:
            print("Error: Una de las cuentas no existe. Abortando.")
            conn_a.tpc_rollback()
            conn_c.tpc_rollback()
            return

        # 6. FASE 1: PREPARE
        print("Fase 1: Preparando transacciones (PREPARE)...")
        conn_a.tpc_prepare()
        conn_c.tpc_prepare()
        print("Ambos participantes en estado PREPARED (Vote: YES).")

        # --- SIMULACIÓN DE FALLO ---
        print("\n*** PUNTO CRÍTICO ***")
        print("Si deseas simular una falla, detén el contenedor de Cusco ahora:")
        print("  docker stop postgres_cusco")
        input("Presiona Enter para continuar con el COMMIT GLOBAL o simular fallo...")

        # Verificar disponibilidad antes de confirmar (Health check)
        print("Verificando disponibilidad de participantes...")
        with conn_a.cursor() as test_a: test_a.execute('SELECT 1;')
        with conn_c.cursor() as test_c: test_c.execute('SELECT 1;')

        # 7. FASE 2: COMMIT GLOBAL
        print("Fase 2: Ejecutando COMMIT GLOBAL...")
        conn_a.tpc_commit()
        conn_c.tpc_commit()
        
        print("¡Transacción Exitosa! Consistencia garantizada.")

    except psycopg2.OperationalError as e:
        print(f"\n[!] ERROR DE CONEXIÓN O RED DURANTE EL PROTOCOLO: \n{e}")
        print("[!] No es posible completar el COMMIT global. Las transacciones permanecerán en estado PREPARED (o abortadas si fue antes del prepare).")
        print("[!] Utiliza recover_transactions.py para inspeccionar y resolver transacciones huérfanas.\n")
    except Exception as e:
        print(f"\n[!] Excepción inesperada: {e}")
        if conn_a: conn_a.tpc_rollback()
        if conn_c: conn_c.tpc_rollback()
    finally:
        if conn_a and not conn_a.closed: conn_a.close()
        if conn_c and not conn_c.closed: conn_c.close()

if __name__ == "__main__":
    print_balances()
    
    # Monto a transferir según el caso de estudio
    MONTO_TRANSFERENCIA = 25000.00
    
    run_two_phase_commit(MONTO_TRANSFERENCIA)
    
    print_balances()