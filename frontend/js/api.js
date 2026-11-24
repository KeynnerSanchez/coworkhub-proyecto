// ===================== Llamadas a la API y funciones de ayuda =====================

// Error que se lanza cuando la API responde con un estado de error (400, 401, 403, 404, 409...)
class ErrorApi extends Error {
    constructor(estado, cuerpo) {
        super(cuerpo && cuerpo.error ? cuerpo.error : "Error " + estado);
        this.estado = estado;
        this.codigo = cuerpo && cuerpo.codigo ? cuerpo.codigo : "";
    }
}

// ---- Sesion (se guarda en sessionStorage: se borra al cerrar la pestana) ----
function guardarSesion(token, perfil) {
    sessionStorage.setItem("token", token);
    sessionStorage.setItem("perfil", JSON.stringify(perfil));
}

function obtenerToken() {
    return sessionStorage.getItem("token");
}

function obtenerPerfil() {
    const texto = sessionStorage.getItem("perfil");
    return texto ? JSON.parse(texto) : null;
}

function borrarSesion() {
    sessionStorage.clear();
}

// ---- Funcion principal: hace la peticion con el token y devuelve el JSON ----
async function api(metodo, ruta, cuerpo) {
    const opciones = { method: metodo, headers: {} };
    const token = obtenerToken();
    if (token) {
        opciones.headers["Authorization"] = "Bearer " + token;
    }
    if (cuerpo !== undefined) {
        opciones.headers["Content-Type"] = "application/json";
        opciones.body = JSON.stringify(cuerpo);
    }

    let respuesta;
    try {
        respuesta = await fetch(API_URL + ruta, opciones);
    } catch (e) {
        throw new Error("No se pudo conectar con el servidor (" + API_URL + "). Esta encendido el backend?");
    }

    // algunas respuestas (como DELETE) vienen vacias
    const texto = await respuesta.text();
    let datos = null;
    if (texto) {
        try {
            datos = JSON.parse(texto);
        } catch (e) {
            datos = texto;
        }
    }

    if (!respuesta.ok) {
        // token vencido o invalido: se vuelve al login
        if (respuesta.status === 401 && token) {
            cerrarSesion("Tu sesion vencio. Inicia sesion de nuevo.");
        }
        throw new ErrorApi(respuesta.status, datos);
    }
    return datos;
}

// ---- Funciones de ayuda para mostrar datos ----

// Evita que un texto con < o > se interprete como HTML
function esc(texto) {
    const reemplazos = { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" };
    return String(texto === null || texto === undefined ? "" : texto).replace(/[&<>"']/g, c => reemplazos[c]);
}

function dinero(numero) {
    return "$ " + Number(numero || 0).toLocaleString("es-CO");
}

function fechaHora(texto) {
    return texto ? texto.replace("T", " ").substring(0, 16) : "";
}

// Fecha de hoy + N dias en formato yyyy-MM-dd (hora local)
function hoyMasDias(dias) {
    const fecha = new Date();
    fecha.setDate(fecha.getDate() + dias);
    const mes = String(fecha.getMonth() + 1).padStart(2, "0");
    const dia = String(fecha.getDate()).padStart(2, "0");
    return fecha.getFullYear() + "-" + mes + "-" + dia;
}

function mesActual() {
    return hoyMasDias(0).substring(0, 7);
}

// ---- Mensajes en pantalla ----
let temporizadorMensaje = null;

function mostrarMensaje(texto, tipo) {
    const caja = document.getElementById("mensaje");
    caja.textContent = texto;
    caja.className = "mensaje " + (tipo || "ok");
    clearTimeout(temporizadorMensaje);
    temporizadorMensaje = setTimeout(() => caja.classList.add("oculto"), 7000);
}

function mostrarError(error) {
    const codigo = error.codigo ? "[" + error.codigo + "] " : "";
    mostrarMensaje(codigo + error.message, "error");
}
