from flask import Flask, request, jsonify, render_template # jsonify para convertir a json
from flask_cors import CORS
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime
import re

app = Flask(__name__)
CORS(app)

app.config["SQLALCHEMY_DATABASE_URI"] = "sqlite:///estudiantes.db" # usamos sqlite para la base de datos, el archivo se llamará estudiantes.db
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False 


db = SQLAlchemy(app) # sqlalchemy permite interactuar con la bd mas facil

CARRERAS_VALIDAS = [   # se usara para validar
    "Ingeniería de Sistemas",
    "Ingeniería Industrial",
    "Ingeniería Civil",
    "Ingeniería Mecánica",
    "Ingeniería Eléctrica",
    "Ingeniería Electrónica",
    "Ingeniería de Minas",
    "Ingeniería Metalúrgica",
    "Ingeniería Química",
    "Agronomía",
    "Biología",
    "Medicina",
    "Derecho",
    "Administración",
    "Contabilidad"
]


class Estudiante(db.Model): # define el modelo, con atributos y tipos de datos
    id = db.Column(db.Integer, primary_key=True)
    nombre = db.Column(db.String(100), nullable=False)
    apellido = db.Column(db.String(100), nullable=False)
    edad = db.Column(db.Integer, nullable=False)
    carrera = db.Column(db.String(150), nullable=False)
    correo = db.Column(db.String(150), nullable=False, unique=True)
    telefono = db.Column(db.String(20), nullable=False, unique=True)
    estado = db.Column(db.String(30), default="Activo")
    eliminado = db.Column(db.Boolean, default=False)
    fecha_registro = db.Column(db.String(30), default=lambda: datetime.now().strftime("%Y-%m-%d %H:%M"))

    def to_dict(self):   # convertir a dict, para luego convertir a json
        return {
            "id": self.id,
            "nombre": self.nombre,
            "apellido": self.apellido,
            "edad": self.edad,
            "carrera": self.carrera,
            "correo": self.correo,
            "telefono": self.telefono,
            "estado": self.estado,
            "eliminado": self.eliminado,
            "fecha_registro": self.fecha_registro
        }


def validar(data, editando=False, id_actual=None):
    errores = {}

    if not data.get("nombre", "").strip():
        errores["nombre"] = "El nombre es obligatorio."

    if not data.get("apellido", "").strip():
        errores["apellido"] = "El apellido es obligatorio."

    if "edad" not in data or data["edad"] in ["", None]: 
        errores["edad"] = "La edad es obligatoria."
    elif not isinstance(data["edad"], int) or data["edad"] < 16 or data["edad"] > 80:
        errores["edad"] = "La edad debe estar entre 16 y 80 años."

    carrera = data.get("carrera", "").strip()
    if not carrera:
        errores["carrera"] = "Seleccione una carrera."
    elif carrera not in CARRERAS_VALIDAS:
        errores["carrera"] = "Debe seleccionar una carrera válida de la lista."

    correo = data.get("correo", "").strip()
    if not correo:
        errores["correo"] = "El correo es obligatorio."
    elif not re.match(r"^[\w\.-]+@[\w\.-]+\.\w+$", correo): # la estructura correcta es texto, @, texto, punto, texto
        errores["correo"] = "Ingrese un correo válido."

    telefono = data.get("telefono", "").strip()
    if not telefono:
        errores["telefono"] = "El teléfono es obligatorio."
    elif not re.match(r"^9\d{8}$", telefono):
        errores["telefono"] = "El teléfono debe tener 9 dígitos y empezar con 9."

    existente_correo = Estudiante.query.filter_by(correo=correo).first()
    if existente_correo and (not editando or existente_correo.id != id_actual):
        errores["correo"] = "Este correo ya está registrado."

    existente_telefono = Estudiante.query.filter_by(telefono=telefono).first()
    if existente_telefono and (not editando or existente_telefono.id != id_actual):
        errores["telefono"] = "Este número ya está registrado."

    return errores


@app.route("/")
def inicio():
    return render_template("index.html") # renderiza index.html


@app.route("/estudiantes", methods=["GET"]) # lista estudiantes activos
def listar():
    estudiantes = Estudiante.query.filter_by(eliminado=False).all()
    return jsonify([e.to_dict() for e in estudiantes]), 200


@app.route("/estudiantes/<int:id>", methods=["GET"]) # busca esstudiante con id
def buscar_por_id(id):
    estudiante = Estudiante.query.get(id)
    if not estudiante or estudiante.eliminado:
        return jsonify({"error": "Estudiante no encontrado"}), 404
    return jsonify(estudiante.to_dict()), 200


@app.route("/estudiantes", methods=["POST"]) # registrar
def registrar():
    data = request.json # se espera el json con los campos para crear
    errores = validar(data) # valida

    if errores:
        return jsonify({"errores": errores}), 400 # si hay errores, status 400 q significa bad request

    estudiante = Estudiante( # crea
        nombre=data["nombre"].strip(),
        apellido=data["apellido"].strip(),
        edad=data["edad"],
        carrera=data["carrera"].strip(),
        correo=data["correo"].strip(),
        telefono=data.get("telefono", "").strip(),
        estado="Activo"
    )

    db.session.add(estudiante) # añade a la bd 
    db.session.commit() # guarda los cambios
    # status 201 que significa created
    return jsonify({"mensaje": "Estudiante registrado correctamente", "estudiante": estudiante.to_dict()}), 201


@app.route("/estudiantes/<int:id>", methods=["PUT"]) # actualizzar
def actualizar(id):
    estudiante = Estudiante.query.get(id) # busca el estudiante

    if not estudiante or estudiante.eliminado: # si no existe o esta elimnado
        return jsonify({"error": "Estudiante no encontrado"}), 404 # significa not found

    data = request.json # espera el json con los campos
    errores = validar(data, editando=True, id_actual=id)

    if errores:
        return jsonify({"errores": errores}), 400

    estudiante.nombre = data["nombre"].strip()
    estudiante.apellido = data["apellido"].strip()
    estudiante.edad = data["edad"]
    estudiante.carrera = data["carrera"].strip()
    estudiante.correo = data["correo"].strip()
    estudiante.telefono = data.get("telefono", "").strip()
    estudiante.estado = "Activo" # estado activo 

    db.session.commit()
    # status 200 que significa ok
    return jsonify({"mensaje": "Estudiante actualizado correctamente", "estudiante": estudiante.to_dict()}), 200


@app.route("/estudiantes/<int:id>", methods=["DELETE"])
def eliminar(id):
    estudiante = Estudiante.query.get(id)

    if not estudiante or estudiante.eliminado:
        return jsonify({"error": "Estudiante no encontrado"}), 404

    estudiante.eliminado = True
    estudiante.estado = "Eliminado"
    db.session.commit()

    return jsonify({"mensaje": "Estudiante enviado a la papelera"}), 200


@app.route("/papelera", methods=["GET"]) # listar estudiantes eliminados
def papelera():
    estudiantes = Estudiante.query.filter_by(eliminado=True).all() # si su atributo eliminado es true, entonces esta en la papelera
    return jsonify([e.to_dict() for e in estudiantes]), 200 # devuelve la lista


@app.route("/papelera/restaurar/<int:id>", methods=["POST"]) # restaurar equivale a actualizar el atributo eliminado a false y estado a activo
def restaurar(id):
    estudiante = Estudiante.query.get(id)

    if not estudiante or not estudiante.eliminado: # si no existe o no esta eliminado, entonces no se puede restaurar
        return jsonify({"error": "Estudiante no encontrado en papelera"}), 404

    estudiante.eliminado = False
    estudiante.estado = "Activo"
    db.session.commit()

    return jsonify({"mensaje": "Estudiante restaurado correctamente"}), 200

@app.route("/papelera/<int:id>", methods=["DELETE"]) # elimina permanentemente, se borra de la base de datos
def eliminar_permanente(id):
    estudiante = Estudiante.query.get(id)

    if not estudiante or not estudiante.eliminado:
        return jsonify({"error": "Estudiante no encontrado en papelera"}), 404

    db.session.delete(estudiante)
    db.session.commit()

    return jsonify({"mensaje": "Estudiante eliminado permanentemente"}), 200


@app.route("/papelera/vaciar", methods=["DELETE"])
def vaciar_papelera():
    Estudiante.query.filter_by(eliminado=True).delete() # todos los q tengan eliminado true
    db.session.commit()
    return jsonify({"mensaje": "Papelera vaciada correctamente"}), 200


@app.route("/estadisticas", methods=["GET"]) # cuenta total de activos, eliminados y total general
def estadisticas():
    activos = Estudiante.query.filter_by(eliminado=False).count()
    eliminados = Estudiante.query.filter_by(eliminado=True).count()
    total = Estudiante.query.count()

    return jsonify({
        "activos": activos,
        "eliminados": eliminados,
        "total": total
    }), 200


@app.route("/estudiantes/importar", methods=["POST"]) # 
def importar_estudiantes():
    data = request.json # se espera la lista

    if not isinstance(data, list):
        return jsonify({"error": "El archivo debe contener una lista de estudiantes."}), 400

    importados = 0
    rechazados = [] # para almacenar rechazados

    for item in data:
        errores = validar(item) # valida cada estudiante

        if errores:
            rechazados.append({
                "estudiante": item, # agrega a rechazados
                "errores": errores
            })
            continue

        existente = Estudiante.query.filter_by(correo=item["correo"].strip()).first()
        if existente:
            rechazados.append({
                "estudiante": item,   # si el correo ya existe, se rechaza
                "errores": {"correo": "Este correo ya está registrado."}
            })
            continue

        estudiante = Estudiante(    # si es valido y no existe, se crea el estudiante
            nombre=item["nombre"].strip(),
            apellido=item["apellido"].strip(),
            edad=item["edad"],
            carrera=item["carrera"].strip(),
            correo=item["correo"].strip(),
            telefono=item["telefono"].strip(),
            estado="Activo"
        )

        db.session.add(estudiante)
        importados += 1

    db.session.commit()

    return jsonify({ # para imprimir en consola
        "mensaje": f"Importación finalizada. Importados: {importados}. Rechazados: {len(rechazados)}.",
        "importados": importados,
        "rechazados": rechazados
    }), 200

if __name__ == "__main__":
    with app.app_context():
        db.create_all()
    app.run(debug=True)