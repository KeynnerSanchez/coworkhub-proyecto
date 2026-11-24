// ===================== Arranque: login, menu y navegacion =====================

const contenido = document.getElementById("contenido");
let perfil = null; // { usuario, rol, miembroId }

function $(id) {
    return document.getElementById(id);
}

function esStaff() {
    return perfil !== null && (perfil.rol === "ADMIN" || perfil.rol === "RECEPTIONIST");
}

// Cada nombre de vista apunta a la funcion que la dibuja
const VISTAS = {
    inicio: verInicio,
    salas: verSalas,
    disponibilidad: verDisponibilidad,
    nueva: verNueva,
    reservas: verReservas,
    consumo: verConsumo,
    miembros: verMiembros,
    catalogos: verCatalogos
};

// El menu cambia segun el rol
function opcionesMenu() {
    const opciones = [
        ["inicio", "Inicio"],
        ["salas", "Salas"],
        ["disponibilidad", "Disponibilidad"],
        ["nueva", "Nueva reserva"],
        ["reservas", perfil.rol === "MEMBER" ? "Mis reservas" : "Reservas"]
    ];
    if (perfil.rol === "MEMBER") {
        opciones.push(["consumo", "Mi consumo"]);
    } else {
        opciones.push(["miembros", "Miembros"]);
    }
    if (perfil.rol === "ADMIN") {
        opciones.push(["catalogos", "Catalogos"]);
    }
    return opciones;
}

async function irA(nombre, datos) {
    document.querySelectorAll("#menu button").forEach(boton => {
        boton.classList.toggle("activo", boton.dataset.vista === nombre);
    });
    contenido.innerHTML = "<p class='cargando'>Cargando...</p>";
    try {
        await VISTAS[nombre](datos);
    } catch (error) {
        if (obtenerToken()) {
            contenido.innerHTML = "";
        }
        mostrarError(error);
    }
}

// ---------- Login ----------
function pintarLogin() {
    $("menu").innerHTML = "";
    $("usuarioInfo").innerHTML = "";
    contenido.innerHTML = `
        <div class="login">
            <h2>Iniciar sesion</h2>
            <form id="formLogin">
                <label>Usuario <input id="loginUsuario" required autofocus></label>
                <label>Contrasena <input id="loginClave" type="password" required></label>
                <button type="submit">Entrar</button>
            </form>
            <p class="ayuda">Usuarios de prueba: admin / Admin123, recepcion / Recep123,
            ana / Ana12345, luis / Luis12345</p>
        </div>`;

    $("formLogin").onsubmit = async (evento) => {
        evento.preventDefault();
        try {
            const respuesta = await api("POST", "/auth/login", {
                username: $("loginUsuario").value.trim(),
                password: $("loginClave").value
            });
            sessionStorage.setItem("token", respuesta.token);
            const datosPerfil = await api("GET", "/auth/perfil");
            guardarSesion(respuesta.token, datosPerfil);
            iniciarApp();
        } catch (error) {
            borrarSesion();
            mostrarError(error);
        }
    };
}

function cerrarSesion(mensaje) {
    borrarSesion();
    perfil = null;
    pintarLogin();
    if (mensaje) {
        mostrarMensaje(mensaje, "error");
    }
}

// ---------- Aplicacion (ya con sesion) ----------
function iniciarApp() {
    perfil = obtenerPerfil();
    $("menu").innerHTML = opcionesMenu()
        .map(op => `<button data-vista="${op[0]}">${op[1]}</button>`)
        .join("");
    $("menu").onclick = (evento) => {
        const boton = evento.target.closest("button[data-vista]");
        if (boton) {
            irA(boton.dataset.vista);
        }
    };
    $("usuarioInfo").innerHTML = `<span>${esc(perfil.usuario)} <span class="etiqueta">${esc(perfil.rol)}</span></span>
        <button id="botonSalir" class="secundario">Salir</button>`;
    $("botonSalir").onclick = () => cerrarSesion();
    irA("inicio");
}

// ---------- Ventana emergente ----------
$("cerrarModal").onclick = () => $("modal").close();

// ---------- Inicio de la pagina ----------
if (obtenerToken() && obtenerPerfil()) {
    iniciarApp();
} else {
    pintarLogin();
}
