from flask import Flask, request, jsonify, render_template
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

estudiantes = []
papelera = []


def validar_estudiante(data, actualizando=False):
    campos = ["id", "nombre", "apellido", "edad", "carrera", "correo"]

    for campo in campos:
        if campo not in data or data[campo] in ["", None]:
            return f"El campo '{campo}' es obligatorio."

    if not isinstance(data["id"], int):
        return "El ID debe ser un número entero."

    if not isinstance(data["edad"], int) or data["edad"] <= 0:
        return "La edad debe ser un número entero positivo."

    if "@" not in data["correo"] or "." not in data["correo"]:
        return "El correo debe tener un formato válido."

    if not actualizando:
        if any(e["id"] == data["id"] for e in estudiantes):
            return "Ya existe un estudiante activo con ese ID."

        if any(e["id"] == data["id"] for e in papelera):
            return "Ese ID está en la papelera. Restaure o vacíe la papelera primero."

    return None


@app.route("/")
def inicio():
    return render_template("index.html")


@app.route("/estudiantes", methods=["GET"])
def listar_estudiantes():
    return jsonify(estudiantes), 200


@app.route("/estudiantes/<int:id>", methods=["GET"])
def buscar_estudiante(id):
    estudiante = next((e for e in estudiantes if e["id"] == id), None)

    if estudiante is None:
        return jsonify({"error": "Estudiante no encontrado"}), 404

    return jsonify(estudiante), 200


@app.route("/estudiantes", methods=["POST"])
def registrar_estudiante():
    data = request.json
    error = validar_estudiante(data)

    if error:
        return jsonify({"error": error}), 400

    estudiantes.append(data)
    return jsonify({"mensaje": "Estudiante registrado correctamente", "estudiante": data}), 201


@app.route("/estudiantes/<int:id>", methods=["PUT"])
def actualizar_estudiante(id):
    data = request.json
    estudiante = next((e for e in estudiantes if e["id"] == id), None)

    if estudiante is None:
        return jsonify({"error": "Estudiante no encontrado"}), 404

    data["id"] = id
    error = validar_estudiante(data, actualizando=True)

    if error:
        return jsonify({"error": error}), 400

    estudiante.update(data)
    return jsonify({"mensaje": "Estudiante actualizado correctamente", "estudiante": estudiante}), 200


@app.route("/estudiantes/<int:id>", methods=["DELETE"])
def eliminar_estudiante(id):
    estudiante = next((e for e in estudiantes if e["id"] == id), None)

    if estudiante is None:
        return jsonify({"error": "Estudiante no encontrado"}), 404

    estudiantes.remove(estudiante)
    papelera.append(estudiante)

    return jsonify({"mensaje": "Estudiante enviado a la papelera", "estudiante": estudiante}), 200


@app.route("/papelera", methods=["GET"])
def ver_papelera():
    return jsonify(papelera), 200


@app.route("/papelera/restaurar/<int:id>", methods=["POST"])
def restaurar_estudiante(id):
    estudiante = next((e for e in papelera if e["id"] == id), None)

    if estudiante is None:
        return jsonify({"error": "Estudiante no encontrado en la papelera"}), 404

    if any(e["id"] == id for e in estudiantes):
        return jsonify({"error": "No se puede restaurar porque ya existe un estudiante activo con ese ID"}), 409

    papelera.remove(estudiante)
    estudiantes.append(estudiante)

    return jsonify({"mensaje": "Estudiante restaurado correctamente", "estudiante": estudiante}), 200


@app.route("/papelera/vaciar", methods=["DELETE"])
def vaciar_papelera():
    papelera.clear()
    return jsonify({"mensaje": "Papelera vaciada correctamente"}), 200


if __name__ == "__main__":
    app.run(debug=True)