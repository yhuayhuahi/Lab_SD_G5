// Ya no forma parte del diseño activo.

//  Antes, Medicine extendía Remote y cada medicina se trataba
// como un objeto remoto RMI, aunque solo almacenaba datos.

// Generaba más stubs y conexiones innecesarias.

// Ahora Medicine se envía como Serializable y el único objeto remoto es Stock

public class MedicineInterface {

    private MedicineInterface() {}
}