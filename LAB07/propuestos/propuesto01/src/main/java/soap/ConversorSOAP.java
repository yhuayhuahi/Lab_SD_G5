package soap;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@WebService
public class ConversorSOAP {

    @WebMethod
    public double celsiusAFahrenheit(double celsius) {
        return (celsius * 9.0 / 5.0) + 32;
    }

    @WebMethod
    public double fahrenheitACelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5.0 / 9.0;
    }
}