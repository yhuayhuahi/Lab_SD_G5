"""
Simulación simple de Two-Phase Commit (2PC) entre dos nodos PostgreSQL
Origen: Arequipa (port 5433, DB almacen_arequipa)
Destino: Cusco  (port 5434, DB almacen_cusco)

Modo de uso:
  - Arrancar los contenedores con `docker-compose up -d`
  - Instalar dependencias: `pip install -r requirements.txt`
  - Ejecutar: `python 2pc_sim.py --amount 25000`

Opciones de fallo:
  - Para simular fallo de red o caída de nodo, detener el contenedor `postgres_cusco` después de la preparación y antes del commit.
  - El script incluye una pausa (input) entre prepare y commit para permitir realizar la acción manualmente.
"""

import psycopg2
import sys
import argparse
import time


def connect(host_port, dbname):
    return psycopg2.connect(host='localhost', port=host_port, dbname=dbname, user='postgres', password='postgres')


def print_balances():
    with connect(5433, 'almacen_arequipa') as ca, connect(5434, 'almacen_cusco') as cc:
        with ca.cursor() as cura, cc.cursor() as curc:
            cura.execute("SELECT id, owner, balance FROM accounts;")
            ar = cura.fetchall()
            curc.execute("SELECT id, owner, balance FROM accounts;")
            cu = curc.fetchall()
    print('\n--- Saldos actuales ---')
    print('Arequipa:', ar)
    print('Cusco:   ', cu)


def run_two_phase(amount):
    # Conexiones a los participantes
    conn_a = connect(5433, 'almacen_arequipa')
    conn_c = connect(5434, 'almacen_cusco')

    # Generar XIDs simples (gtrid único)
    gtrid = 'tx_'+str(int(time.time()))

    try:
        # BEGIN TPC en cada participante
        xid_a = conn_a.xid(42, gtrid, 'a')
        xid_c = conn_c.xid(42, gtrid, 'c')

        conn_a.tpc_begin(xid_a)
        cur_a = conn_a.cursor()
        cur_a.execute("UPDATE accounts SET balance = balance - %s WHERE owner = 'Cuenta_Arequipa' RETURNING balance;", (amount,))
        bala = cur_a.fetchone()
        if bala is None:
            print('Fallo: cuenta de Arequipa no encontrada o saldo insuficiente')
            conn_a.tpc_rollback()
            return

        conn_c.tpc_begin(xid_c)
        cur_c = conn_c.cursor()
        cur_c.execute("UPDATE accounts SET balance = balance + %s WHERE owner = 'Cuenta_Cusco' RETURNING balance;", (amount,))
        bcu = cur_c.fetchone()
        if bcu is None:
            print('Fallo: cuenta de Cusco no encontrada')
            conn_a.tpc_rollback()
            conn_c.tpc_rollback()
            return

        # Prepare en cada participante
        print('Preparando transacciones en participantes...')
        conn_a.tpc_prepare()
        conn_c.tpc_prepare()
        print('Ambos participantes en estado PREPARED.')

        print('MOMENTO DE POSIBLE FALLO: pausa antes de COMMIT. Para simular, detenga el contenedor postgres_cusco ahora y presione Enter.')
        input('Presione Enter para continuar (commit) o detener contenedor para simular fallo...')

        # Verificar disponibilidad antes de enviar commit global
        print('Verificando disponibilidad de participantes antes de COMMIT...')
        try:
            with conn_a.cursor() as test_a:
                test_a.execute('SELECT 1;')
            with conn_c.cursor() as test_c:
                test_c.execute('SELECT 1;')
        except Exception as e:
            print('No es posible completar el COMMIT global. Las transacciones permanecerán en estado PREPARED para recuperación.')
            print('Use pg_prepared_xacts en cada nodo para ver el GID y ejecutar COMMIT PREPARED o ROLLBACK PREPARED después de recuperar el nodo.')
            return

        print('Enviando COMMIT a participantes...')
        conn_a.tpc_commit()
        conn_c.tpc_commit()

        print('COMMIT aplicado en todos los participantes. Transferencia completada.')

    except Exception as e:
        print('Excepción durante 2PC:', e)
        if 'conn_a' in locals() and conn_a is not None:
            try:
                conn_a.tpc_rollback()
            except Exception:
                pass
        if 'conn_c' in locals() and conn_c is not None:
            try:
                conn_c.tpc_rollback()
            except Exception:
                pass
    finally:
        conn_a.close()
        conn_c.close()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--amount', type=int, required=True, help='Cantidad a transferir')
    args = parser.parse_args()

    print_balances()
    run_two_phase(args.amount)
    print_balances()


if __name__ == '__main__':
    main()
