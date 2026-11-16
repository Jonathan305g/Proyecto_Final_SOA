// Verificación local con Chrome/Edge iniciado con --remote-debugging-port=9223.
// Solo usa módulos de Node; no añade dependencias a la aplicación.
const fs = require("node:fs");
const path = require("node:path");
const assert = require("node:assert/strict");

async function main() {
  const origen = process.env.APP_URL || "http://localhost:8080";
  const destino = path.resolve(".local/qa");
  fs.mkdirSync(destino, { recursive: true });
  const config = fs.readFileSync(".local/application.properties", "utf8");
  function propiedad(nombre) {
    const linea = config.split(/\r?\n/).find((l) => l.startsWith(nombre + "="));
    if (!linea) throw new Error("Falta configuración local: " + nombre);
    return linea.slice(nombre.length + 1);
  }
  const targets = await (await fetch("http://127.0.0.1:9223/json/list")).json();
  const target = targets.find((t) => t.type === "page");
  assert(target, "No hay una pestaña de navegador disponible.");
  const socket = new WebSocket(target.webSocketDebuggerUrl);
  await new Promise((resolve, reject) => {
    socket.onopen = resolve;
    socket.onerror = reject;
  });
  let siguiente = 0;
  const pendientes = new Map();
  const errores = [];
  socket.onmessage = (event) => {
    const mensaje = JSON.parse(event.data);
    if (mensaje.id) {
      const p = pendientes.get(mensaje.id);
      if (p) {
        pendientes.delete(mensaje.id);
        mensaje.error
          ? p.reject(new Error(mensaje.error.message))
          : p.resolve(mensaje.result);
      }
    } else if (mensaje.method === "Runtime.exceptionThrown")
      errores.push(mensaje.params.exceptionDetails.text);
  };
  function call(method, params = {}) {
    const id = ++siguiente;
    return new Promise((resolve, reject) => {
      pendientes.set(id, { resolve, reject });
      socket.send(JSON.stringify({ id, method, params }));
    });
  }
  async function evaluar(expression) {
    const result = await call("Runtime.evaluate", {
      expression,
      returnByValue: true,
      awaitPromise: true,
    });
    if (result.exceptionDetails)
      throw new Error("Falló la evaluación en navegador.");
    return result.result.value;
  }
  async function esperar(expression) {
    for (let intento = 0; intento < 100; intento++) {
      if (await evaluar(expression)) return;
      await new Promise((r) => setTimeout(r, 100));
    }
    throw new Error("La página no alcanzó el estado esperado.");
  }
  async function navegar(ruta, texto) {
    await call("Page.navigate", { url: origen + ruta });
    await esperar(
      `location.pathname === ${JSON.stringify(ruta)} && document.readyState === 'complete' && document.body.innerText.includes(${JSON.stringify(texto)})`,
    );
  }
  async function captura(nombre) {
    const result = await call("Page.captureScreenshot", {
      format: "png",
      captureBeyondViewport: true,
    });
    fs.writeFileSync(
      path.join(destino, nombre + ".png"),
      Buffer.from(result.data, "base64"),
    );
  }
  try {
    await call("Page.enable");
    await call("Runtime.enable");
    await call("Emulation.setDeviceMetricsOverride", {
      width: 1440,
      height: 1000,
      deviceScaleFactor: 1,
      mobile: false,
    });
    await navegar("/login", "Iniciar sesión");
    assert.equal(
      await evaluar(
        'document.querySelector("link[href*=bootstrap]").sheet !== null',
      ),
      true,
    );
    await captura("login-escritorio");
    // Las credenciales pasan directamente al navegador, sin imprimirlas ni guardarlas en informes.
    await evaluar(
      `document.querySelector('#username').value=${JSON.stringify(propiedad("app.bootstrap.username"))}; document.querySelector('#password').value=${JSON.stringify(propiedad("app.bootstrap.password"))}; document.querySelector('form').requestSubmit(); true`,
    );
    await esperar(
      `location.pathname==='/' && document.readyState==='complete' && document.body.innerText.includes('Su bodega, en orden.')`,
    );
    await captura("inicio-escritorio");
    for (const [ruta, texto] of [
      ["/productos", "Productos"],
      ["/inventario", "Inventario"],
      ["/entradas", "Historial de entradas"],
      ["/entradas/nueva", "Registrar entrada"],
      ["/admin/usuarios", "Usuarios"],
      ["/admin/categorias", "Categorías"],
      ["/admin/marcas", "Marcas"],
      ["/admin/proveedores", "Proveedores"],
    ]) {
      await navegar(ruta, texto);
      assert.equal(
        await evaluar(
          "document.documentElement.scrollWidth <= window.innerWidth",
        ),
        true,
        "Desbordamiento horizontal: " + ruta,
      );
      assert.equal(
        await evaluar('!!document.querySelector("input[name=_csrf]")'),
        true,
        "Falta token CSRF: " + ruta,
      );
    }
    await navegar("/inventario", "Inventario");
    await captura("inventario-escritorio");
    await call("Emulation.setDeviceMetricsOverride", {
      width: 390,
      height: 844,
      deviceScaleFactor: 1,
      mobile: true,
    });
    await captura("inventario-movil");
    assert.equal(
      await evaluar(
        "document.documentElement.scrollWidth <= window.innerWidth",
      ),
      true,
      "Desbordamiento en móvil.",
    );
    await navegar("/", "Su bodega, en orden.");
    await captura("inicio-movil");
    assert.equal(
      await evaluar(
        "document.documentElement.scrollWidth <= window.innerWidth",
      ),
      true,
    );
    await evaluar(
      'document.querySelector("form[action$=logout]").requestSubmit(); true',
    );
    await esperar(
      `location.pathname==='/login' && location.search.includes('logout') && document.readyState==='complete'`,
    );
    assert.deepEqual(errores, [], "Errores de ejecución en el navegador.");
    const informe = {
      navegador: "Chrome con CDP",
      origen,
      resultado: "APROBADO",
      verificaciones: [
        "Login y sesión HTTP real",
        "Ocho rutas autenticadas",
        "CSS Bootstrap local",
        "Tokens CSRF",
        "Escritorio 1440x1000",
        "Móvil 390x844 sin desbordamiento",
        "Logout POST",
      ],
      capturas: fs.readdirSync(destino).filter((p) => p.endsWith(".png")),
    };
    fs.writeFileSync(
      path.join(destino, "resultado.json"),
      JSON.stringify(informe, null, 2),
    );
    console.log(JSON.stringify(informe, null, 2));
  } finally {
    socket.close();
  }
}
main().catch((error) => {
  console.error(error.message);
  process.exitCode = 1;
});
