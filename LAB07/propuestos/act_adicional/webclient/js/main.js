import { URL } from "./config.js"

const renderResponse = function(mensaje, tipo) {
  const divAnterior = document.querySelector(".response")
  const container = document.querySelector('#container')

  if (divAnterior)
    divAnterior.remove();

  const div = document.createElement("div")
  div.className = "response"
  
  if (tipo === "error") {
    div.id = "error"
    div.textContent = `Hubo un error en el proceso: ${mensaje}`
  } else {
    div.id = "ok"
    div.textContent = `El resultado es ${mensaje}`
  }

  container.insertAdjacentElement('beforeend', div)
}

const xmlRequest = function(operation, a, b) {
  return `
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:org="http://example.org/">
       <soapenv:Header/>
       <soapenv:Body>
          <org:${operation}>
             <a>${a}</a>
             <b>${b}</b>
          </org:${operation}>
       </soapenv:Body>
    </soapenv:Envelope>    
  `
}

const operar = async function(operacion) {
  // inputs
  const a = parseInt(document.querySelector('#inp-a').value.trim(), 10)
  const b = parseInt(document.querySelector('#inp-b').value.trim(), 10)

  if (isNaN(a) || isNaN(b)) {
    alert("Debe ingregar números correctos: Enteros")
    return;
  }

  try {
    const response = await fetch(URL, {
      method: 'POST',
      headers: {
        'content-type': 'text/xml'
      },
      body: xmlRequest(operacion, a, b)
    })

    if (!response.ok) {
      throw new Error('Error en la petición: ' + response.statusText);
    }

    const textData = await response.text();
    
    // Parsear la respuesta XML
    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(textData, "text/xml");

    // Extraer datos específicos (ajustar etiquetas según tu respuesta)
    const resultadoElemento = xmlDoc.getElementsByTagName("return")[0];
    console.log(resultadoElemento)
    const mensaje = resultadoElemento ? resultadoElemento.textContent : "Elemento no encontrado";

    renderResponse(mensaje, "ok")
  }
  catch (error){
    renderResponse(error, "error")
  }
}

const main = function() {
  const btnAdd = document.querySelector("#btn-add")
  const btnSub = document.querySelector("#btn-sub")
  const btnMul = document.querySelector("#btn-mul")
  const btnDiv = document.querySelector("#btn-div")

  btnAdd.addEventListener("click", async () => await operar("sumar"))
  btnSub.addEventListener("click", async () => await operar("restar"))
  btnMul.addEventListener("click", async () => await operar("multiplicar"))
  btnDiv.addEventListener("click", async () => await operar("dividir"))
}

document.addEventListener("DOMContentLoaded", () => main())
  
