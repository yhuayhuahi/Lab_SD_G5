package org.example;

import jakarta.jws.WebService;

@WebService(endpointInterface = "org.example.CalculadoraSOAP")
public class CalculadoraSOAPImpl implements CalculadoraSOAP {

  @Override
  public int sumar(int a, int b) {
    return a + b;
  }

  @Override
  public int restar(int a, int b) {
    return a - b;
  }

  @Override
  public int multiplicar(int a, int b) {
    return a * b;
  }

  @Override
  public int dividir(int a, int b) throws Exception {
    if (b == 0)
      throw new Exception("No se puede dividir entre cero");
    return a / b;
  }
}
