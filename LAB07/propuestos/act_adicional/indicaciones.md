## Actividad adicional (HTML + JS)

Consumir SOAP desde navegador

```html
<!DOCTYPE html>
<html>
  <body>
    <button onclick="consumir()">
      Consultar
    </button>
    <script>
      function consumir(){
        fetch("http://localhost:8080/calculadora")
          .then(r => console.log(r))
      }
    </script>
  </body>
</html>
```

Analizar por qué SOAP presenta limitaciones en consumo directo desde navegador.

