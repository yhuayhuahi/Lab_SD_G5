package org.example;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

@WebService
public interface CalculadoraSOAP {
  @WebMethod
  public int sumar(@WebParam(name = "a") int a, @WebParam(name = "b") int b);

  @WebMethod
  public int restar(@WebParam(name = "a") int a, @WebParam(name = "b") int b);

  @WebMethod
  public int multiplicar(@WebParam(name = "a") int a, @WebParam(name = "b") int b);

  @WebMethod
  public int dividir(@WebParam(name = "a") int a, @WebParam(name = "b") int b) throws Exception;
}

