from flask import Flask, request, jsonify, render_template
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

estudiantes = []
papelera = []
contador_id = 1


def validar_estudiante(data):
    errores = {}

    if not data.get("nombre", "").strip():
        errores["nombre"] = "El nombre es obligatorio."

    if not data.get("apellido", "").strip():
        errores["apellido"] = "El apellido es obligatorio."

    if "edad" not in data or data["edad"] in ["", None]:
        errores["edad"] = "La edad es obligatoria."
    elif not isinstance(data["edad"], int) or data["edad"] <= 0:
        errores["edad"] = "La edad debe ser mayor que cero."

    if not data.get("carrera", "").strip():
        errores["carrera"] = "La carrera es obligatoria."

    correo = data.get("correo", "").strip()
    if not correo:
        errores["correo"] = "El correo es obligatorio."
    elif "@" not in correo or "." not in correo:
        errores["correo"] = "Ingrese un correo válido."

    return errores


@app.route("/")
def inicio():
    return render_template("index.html")


@app.route("/estudiantes-view")
def estudiantes_view():
    return render_template("estudiantes.html")


@app.route("/papelera-view")
def papelera_view():
    return render_template("papelera.html")


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
    global contador_id

    data = request.json
    errores = validar_estudiante(data)

    if errores:
        return jsonify({"errores": errores}), 400

    estudiante = {
        "id": contador_id,
        "nombre": data["nombre"].strip(),
        "apellido": data["apellido"].strip(),
        "edad": data["edad"],
        "carrera": data["carrera"].strip(),
        "correo": data["correo"].strip()
    }

    estudiantes.append(estudiante)
    contador_id += 1

    return jsonify({"mensaje": "Estudiante registrado correctamente", "estudiante": estudiante}), 201


@app.route("/estudiantes/<int:id>", methods=["PUT"])
def actualizar_estudiante(id):
    data = request.json
    estudiante = next((e for e in estudiantes if e["id"] == id), None)

    if estudiante is None:
        return jsonify({"error": "Estudiante no encontrado"}), 404

    errores = validar_estudiante(data)

    if errores:
        return jsonify({"errores": errores}), 400

    estudiante["nombre"] = data["nombre"].strip()
    estudiante["apellido"] = data["apellido"].strip()
    estudiante["edad"] = data["edad"]
    estudiante["carrera"] = data["carrera"].strip()
    estudiante["correo"] = data["correo"].strip()

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

    papelera.remove(estudiante)
    estudiantes.append(estudiante)

    return jsonify({"mensaje": "Estudiante restaurado correctamente", "estudiante": estudiante}), 200


@app.route("/papelera/vaciar", methods=["DELETE"])
def vaciar_papelera():
    papelera.clear()
    return jsonify({"mensaje": "Papelera vaciada correctamente"}), 200


if __name__ == "__main__":
    app.run(debug=True)