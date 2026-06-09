import psycopg2

# Configuración de conexiones
NODES = {
    'Arequipa': {'host': 'localhost', 'port': 5433, 'dbname': 'banco_arequipa', 'user': 'postgres', 'password': 'postgres'},
    'Cusco': {'host': 'localhost', 'port': 5434, 'dbname': 'banco_cusco', 'user': 'postgres', 'password': 'postgres'}
}

def inspect_and_recover():
    """Busca transacciones huérfanas en estado PREPARED y permite confirmarlas o revertirlas."""
    print("--- HERRAMIENTA DE RECUPERACIÓN DE TRANSACCIONES ---")
    
    for node_name, config in NODES.items():
        try:
            conn = psycopg2.connect(**config)
            conn.autocommit = True
            cur = conn.cursor()
            
            # Consultar transacciones preparadas
            cur.execute("SELECT gid, prepared, owner, database FROM pg_prepared_xacts;")
            xacts = cur.fetchall()
            
            if not xacts:
                print(f"[{node_name}] Estado saludable. No hay transacciones huérfanas.")
            else:
                print(f"\n[!] ADVERTENCIA: Se encontraron transacciones PREPARED en {node_name}:")
                for xact in xacts:
                    gid, prep_time, owner, db = xact
                    print(f"  - GID: {gid} | Preparada: {prep_time}")
                    
                    action = input(f"¿Deseas confirmar (C) o revertir (R) la transacción {gid}? [C/R/Ignorar]: ").strip().upper()
                    
                    if action == 'C':
                        cur.execute(f"COMMIT PREPARED '{gid}';")
                        print(f"-> Transacción {gid} CONFIRMADA en {node_name}.")
                    elif action == 'R':
                        cur.execute(f"ROLLBACK PREPARED '{gid}';")
                        print(f"-> Transacción {gid} REVERTIDA en {node_name}.")
                    else:
                        print(f"-> Transacción {gid} ignorada.")

            cur.close()
            conn.close()
            
        except psycopg2.OperationalError:
            print(f"[{node_name}] INACCESIBLE. El nodo está caído.")
        except Exception as e:
            print(f"[{node_name}] Error: {e}")

if __name__ == "__main__":
    inspect_and_recover()