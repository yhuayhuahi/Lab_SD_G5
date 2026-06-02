import javax.jws.WebMethod;
import javax.jws.WebService;
// para que el cliente sepa que esta clase forma parte del servicio 
// puede obviarse pero es una buena practica 
@WebService(targetNamespace = "http://servicio.soap/") 
public interface ICalculadoraSOAP {

    @WebMethod
    int sumar(int a, int b);

    @WebMethod
    int restar(int a, int b);

    @WebMethod
    int multiplicar(int a, int b);

    @WebMethod
    double dividir(double a, double b);

    @WebMethod
    double potencia(double base, double exponente);

    @WebMethod
    double raizCuadrada(double numero);

    @WebMethod
    int factorial(int n);
}