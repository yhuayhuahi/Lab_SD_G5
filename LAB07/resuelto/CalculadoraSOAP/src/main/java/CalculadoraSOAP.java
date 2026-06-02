import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService(
    endpointInterface = "ICalculadoraSOAP",
    targetNamespace = "http://servicio.soap/"
)
public class CalculadoraSOAP implements ICalculadoraSOAP {
  
    @WebMethod
    public int sumar(int a, int b) {
        return a + b;
    }

    @WebMethod
    public int restar(int a, int b) {
        return a - b;
    }

    @WebMethod
    public int multiplicar(int a, int b) {
        return a * b;
    }

    @WebMethod
    public double dividir(double a, double b) {
        if (b == 0) {
            throw new ArithmeticException("No se puede dividir entre cero");
        }
        return a / b;
    }

    @WebMethod
    public double potencia(double base, double exponente) {
        return Math.pow(base, exponente);
    }

    @WebMethod
    public double raizCuadrada(double numero) {
        if (numero < 0) {
            throw new ArithmeticException("No existe raíz cuadrada real para números negativos");
        }
        return Math.sqrt(numero);
    }

    @WebMethod
    public int factorial(int n) {
        if (n < 0) {
            throw new ArithmeticException("El factorial no está definido para números negativos");
        }

        int resultado = 1;
        for (int i = 1; i <= n; i++) {
            resultado *= i;
        }

        return resultado;
    }
}