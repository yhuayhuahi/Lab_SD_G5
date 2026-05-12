// Ya no forma parte del diseño activo.

//  Antes, Medicine extendía Remote y cada medicina se trataba
// como un objeto remoto RMI, aunque solo almacenaba datos.

// Generaba más stubs y conexiones innecesarias.
// stubs consumen recursos de red y memoria y complican la gestión de objetos remotos.

// Ahora Medicine se envía como Serializable y el único objeto remoto es Stock
// serializable reduce la complejidad ya que las medicinas se copian entre cliente y servidor
public class MedicineInterface {

    private MedicineInterface() {}
}

// VERSION ANTERIOR

// public interface MedicineInterface extends Remote {
// public Medicine getMedicine( int amount ) throws Exception;
// public int getStock() throws Exception;
// public String print() throws Exception;
// }