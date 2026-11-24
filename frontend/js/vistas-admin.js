// ===================== Vistas de personal: miembros y catalogos =====================

// ---------- Miembros (ADMIN y RECEPTIONIST) ----------
async function verMiembros() {
    const planes = await api("GET", "/api/planes");
    contenido.innerHTML = `<h2>Miembros</h2>
        <details>
            <summary>Registrar un miembro nuevo</summary>
            <form id="formMiembro" class="formulario">
                <label>Documento <input id="mDocumento" required></label>
                <label>Nombre completo <input id="mNombre" required></label>
                <label>Correo <input id="mCorreo" type="email" required></label>
                <label>Plan <select id="mPlan">${planes.map(p => `<option value="${p.id}">${esc(p.name)} (${p.includedHoursPerMonth} h)</option>`).join("")}</select></label>
                <label>Usuario <input id="mUsuario" required></label>
                <label>Contrasena (minimo 6) <input id="mClave" type="password" minlength="6" required></label>
                <button type="submit">Registrar</button>
            </form>
        </details>
        <div class="barra">
            <label>Nombre contiene <input id="bNombre" placeholder="opcional"></label>
            <label>Estado <select id="bEstado"><option value="">Todos</option><option>ACTIVE</option><option>SUSPENDED</option></select></label>
            <button id="bBuscar">Buscar</button>
        </div>
        <div id="tablaMiembros"></div>`;

    $("formMiembro").onsubmit = async (evento) => {
        evento.preventDefault();
        try {
            const nuevo = await api("POST", "/api/miembros", {
                documentNumber: $("mDocumento").value.trim(),
                fullName: $("mNombre").value.trim(),
                email: $("mCorreo").value.trim(),
                planId: Number($("mPlan").value),
                username: $("mUsuario").value.trim(),
                password: $("mClave").value
            });
            mostrarMensaje("Miembro registrado: " + nuevo.fullName + " (usuario " + nuevo.username + ")", "ok");
            $("formMiembro").reset();
            await cargarMiembros();
        } catch (error) {
            mostrarError(error);
        }
    };
    $("bBuscar").onclick = cargarMiembros;
    $("tablaMiembros").onclick = clickMiembro;
    await cargarMiembros();
}

async function cargarMiembros() {
    let ruta = "/api/miembros?";
    if ($("bNombre").value.trim()) {
        ruta += "nombre=" + encodeURIComponent($("bNombre").value.trim()) + "&";
    }
    if ($("bEstado").value) {
        ruta += "estado=" + $("bEstado").value;
    }
    try {
        const lista = await api("GET", ruta);
        if (lista.length === 0) {
            $("tablaMiembros").innerHTML = "<p>No hay miembros con ese filtro.</p>";
            return;
        }
        const filas = lista.map(m => `<tr>
            <td>${m.id}</td><td>${esc(m.documentNumber)}</td><td>${esc(m.fullName)}</td><td>${esc(m.email)}</td>
            <td>${esc(m.planName)}</td><td>${esc(m.username || "(sin usuario)")}</td>
            <td><span class="etiqueta ${m.status}">${m.status}</span></td>
            <td>
                ${m.status === "ACTIVE"
                    ? `<button class="chico secundario" data-accion="suspender" data-id="${m.id}">Suspender</button>`
                    : `<button class="chico verde" data-accion="activar" data-id="${m.id}">Activar</button>`}
                <button class="chico" data-accion="consumo" data-id="${m.id}" data-nombre="${esc(m.fullName)}">Consumo</button>
                ${perfil.rol === "ADMIN" ? `<button class="chico peligro" data-accion="eliminar" data-id="${m.id}">Eliminar</button>` : ""}
            </td></tr>`).join("");
        $("tablaMiembros").innerHTML = `<div class="tabla"><table>
            <thead><tr><th>Id</th><th>Documento</th><th>Nombre</th><th>Correo</th><th>Plan</th><th>Usuario</th><th>Estado</th><th>Acciones</th></tr></thead>
            <tbody>${filas}</tbody></table></div>`;
    } catch (error) {
        mostrarError(error);
    }
}

async function clickMiembro(evento) {
    const boton = evento.target.closest("button[data-accion]");
    if (!boton) {
        return;
    }
    const id = boton.dataset.id;
    const accion = boton.dataset.accion;
    try {
        if (accion === "consumo") {
            await abrirConsumo(id, boton.dataset.nombre);
            return;
        }
        if (accion === "eliminar") {
            if (!confirm("Seguro que quieres eliminar al miembro " + id + "?")) {
                return;
            }
            await api("DELETE", "/api/miembros/" + id);
            mostrarMensaje("Miembro eliminado", "ok");
        } else {
            // suspender o activar
            await api("PATCH", "/api/miembros/" + id + "/" + accion);
            mostrarMensaje("Miembro actualizado", "ok");
        }
        await cargarMiembros();
    } catch (error) {
        mostrarError(error);
    }
}

async function abrirConsumo(id, nombre) {
    $("modalContenido").onclick = null;
    $("modalContenido").innerHTML = `<h3>Consumo de ${esc(nombre)}</h3>
        <div class="barra">
            <label>Mes <input type="month" id="mMes" value="${mesActual()}"></label>
            <button id="mConsultar">Consultar</button>
        </div>
        <div id="mConsumo"></div>`;
    const consultar = async () => {
        try {
            const consumo = await api("GET", "/api/miembros/" + id + "/consumo?mes=" + $("mMes").value);
            $("mConsumo").innerHTML = tarjetaConsumo(consumo);
        } catch (error) {
            mostrarError(error);
        }
    };
    $("mConsultar").onclick = consultar;
    $("modal").showModal();
    await consultar();
}

// ---------- Catalogos (solo ADMIN) ----------
// Cada catalogo se describe con datos y un solo codigo (CRUD generico) dibuja las cinco pantallas.
const CATALOGOS = {
    sedes: {
        titulo: "Sedes", ruta: "/api/sedes",
        columnas: [["name", "Nombre"], ["city", "Ciudad"], ["address", "Direccion"], ["openingTime", "Apertura"], ["closingTime", "Cierre"]],
        campos: [
            { n: "name", l: "Nombre", t: "text" }, { n: "city", l: "Ciudad", t: "text" },
            { n: "address", l: "Direccion", t: "text" }, { n: "openingTime", l: "Apertura", t: "time" },
            { n: "closingTime", l: "Cierre", t: "time" }
        ]
    },
    salas: {
        titulo: "Salas", ruta: "/api/salas",
        columnas: [["name", "Nombre"], ["type", "Tipo"], ["capacity", "Capacidad"], ["hourlyRate", "Tarifa", dinero],
            ["sedeName", "Sede"], ["equipamientos", "Equipamiento", v => v.map(e => e.name).join(", ")],
            ["active", "Activa", v => v ? "Si" : "No"]],
        campos: [
            { n: "name", l: "Nombre", t: "text" },
            { n: "type", l: "Tipo", t: "select", opciones: [["MEETING_ROOM", "MEETING_ROOM"], ["PRIVATE_OFFICE", "PRIVATE_OFFICE"], ["DESK", "DESK"]] },
            { n: "capacity", l: "Capacidad", t: "number" }, { n: "hourlyRate", l: "Tarifa por hora", t: "number" },
            { n: "active", l: "Activa", t: "checkbox" },
            { n: "sedeId", l: "Sede", t: "select", fuente: "/api/sedes", num: true },
            { n: "equipamientoIds", l: "Equipamiento", t: "multi", fuente: "/api/equipamientos" }
        ]
    },
    equipamientos: {
        titulo: "Equipamientos", ruta: "/api/equipamientos",
        columnas: [["name", "Nombre"]],
        campos: [{ n: "name", l: "Nombre", t: "text" }]
    },
    servicios: {
        titulo: "Servicios adicionales", ruta: "/api/servicios-adicionales",
        columnas: [["name", "Nombre"], ["unitPrice", "Precio unitario", dinero]],
        campos: [{ n: "name", l: "Nombre", t: "text" }, { n: "unitPrice", l: "Precio unitario", t: "number" }]
    },
    planes: {
        titulo: "Planes", ruta: "/api/planes",
        columnas: [["name", "Nombre"], ["includedHoursPerMonth", "Horas incluidas"], ["overageDiscount", "Descuento %"], ["maxActiveBookings", "Max. reservas activas"]],
        campos: [
            { n: "name", l: "Nombre", t: "text" }, { n: "includedHoursPerMonth", l: "Horas incluidas por mes", t: "number" },
            { n: "overageDiscount", l: "Descuento sobre excedentes (0 a 100)", t: "number" },
            { n: "maxActiveBookings", l: "Maximo de reservas activas", t: "number" }
        ]
    }
};

let catalogoActual = "sedes";
let elementosCatalogo = [];

async function verCatalogos() {
    contenido.innerHTML = `<h2>Catalogos</h2>
        <div class="barra" id="pestanas">${Object.keys(CATALOGOS).map(clave =>
            `<button class="secundario" data-catalogo="${clave}">${CATALOGOS[clave].titulo}</button>`).join("")}</div>
        <div id="zonaCatalogo"></div>`;
    $("pestanas").onclick = (evento) => {
        const boton = evento.target.closest("button[data-catalogo]");
        if (boton) {
            pintarCatalogo(boton.dataset.catalogo);
        }
    };
    await pintarCatalogo(catalogoActual);
}

async function pintarCatalogo(clave) {
    catalogoActual = clave;
    const catalogo = CATALOGOS[clave];
    try {
        elementosCatalogo = await api("GET", catalogo.ruta);
        for (const campo of catalogo.campos) {
            if (campo.fuente) {
                campo.datos = await api("GET", campo.fuente);
            }
        }
    } catch (error) {
        mostrarError(error);
        return;
    }
    document.querySelectorAll("#pestanas button").forEach(b => {
        b.className = b.dataset.catalogo === clave ? "" : "secundario";
    });

    const cabecera = catalogo.columnas.map(c => `<th>${c[1]}</th>`).join("");
    const filas = elementosCatalogo.map(item => `<tr>
        ${catalogo.columnas.map(c => `<td>${esc(c[2] ? c[2](item[c[0]]) : item[c[0]])}</td>`).join("")}
        <td><button class="chico" data-editar="${item.id}">Editar</button>
            <button class="chico peligro" data-eliminar="${item.id}">Eliminar</button></td></tr>`).join("");

    $("zonaCatalogo").innerHTML = `<div id="zonaFormulario"></div>
        <div class="tabla"><table><thead><tr>${cabecera}<th>Acciones</th></tr></thead><tbody>${filas}</tbody></table></div>`;
    $("zonaCatalogo").onclick = clickCatalogo;
    pintarFormularioCatalogo(null);
}

function valorDelCampo(campo, item) {
    if (!item) {
        return undefined;
    }
    if (campo.n === "equipamientoIds") {
        return item.equipamientos.map(e => e.id);
    }
    return item[campo.n];
}

function htmlCampo(campo, valor) {
    const id = "c_" + campo.n;
    if (campo.t === "checkbox") {
        return `<label class="chk"><input type="checkbox" id="${id}" ${valor !== false ? "checked" : ""}> ${esc(campo.l)}</label>`;
    }
    if (campo.t === "select") {
        const opciones = campo.opciones || campo.datos.map(d => [d.id, d.name]);
        return `<label>${esc(campo.l)}<select id="${id}">${opciones.map(o =>
            `<option value="${esc(o[0])}" ${String(o[0]) === String(valor) ? "selected" : ""}>${esc(o[1])}</option>`).join("")}</select></label>`;
    }
    if (campo.t === "multi") {
        const marcados = valor || [];
        return `<fieldset><legend>${esc(campo.l)}</legend>${campo.datos.map(d =>
            `<label class="chk"><input type="checkbox" class="multi_${campo.n}" value="${d.id}" ${marcados.includes(d.id) ? "checked" : ""}> ${esc(d.name)}</label>`).join("")}</fieldset>`;
    }
    const extra = campo.t === "number" ? 'step="any" min="0"' : "";
    return `<label>${esc(campo.l)}<input type="${campo.t}" id="${id}" value="${esc(valor === undefined ? "" : valor)}" ${extra} required></label>`;
}

// item = null: formulario para crear. item = objeto: formulario para editar
function pintarFormularioCatalogo(item) {
    const catalogo = CATALOGOS[catalogoActual];
    $("zonaFormulario").innerHTML = `<form id="formCatalogo" class="formulario">
        ${catalogo.campos.map(c => htmlCampo(c, valorDelCampo(c, item))).join("")}
        <button type="submit">${item ? "Guardar cambios" : "Crear"}</button>
        ${item ? '<button type="button" id="cancelarEdicion" class="secundario">Cancelar edicion</button>' : ""}
    </form>`;

    if (item) {
        $("cancelarEdicion").onclick = () => pintarFormularioCatalogo(null);
    }
    $("formCatalogo").onsubmit = async (evento) => {
        evento.preventDefault();
        const cuerpo = leerFormularioCatalogo(catalogo);
        try {
            if (item) {
                await api("PUT", catalogo.ruta + "/" + item.id, cuerpo);
                mostrarMensaje("Cambios guardados", "ok");
            } else {
                await api("POST", catalogo.ruta, cuerpo);
                mostrarMensaje("Creado correctamente", "ok");
            }
            await pintarCatalogo(catalogoActual);
        } catch (error) {
            mostrarError(error);
        }
    };
}

function leerFormularioCatalogo(catalogo) {
    const cuerpo = {};
    catalogo.campos.forEach(campo => {
        const id = "c_" + campo.n;
        if (campo.t === "checkbox") {
            cuerpo[campo.n] = $(id).checked;
        } else if (campo.t === "multi") {
            cuerpo[campo.n] = Array.from(document.querySelectorAll(".multi_" + campo.n + ":checked")).map(x => Number(x.value));
        } else if (campo.t === "number" || campo.num) {
            cuerpo[campo.n] = Number($(id).value);
        } else if (campo.t === "time") {
            const valor = $(id).value;
            cuerpo[campo.n] = valor.length === 5 ? valor + ":00" : valor;
        } else {
            cuerpo[campo.n] = $(id).value.trim();
        }
    });
    return cuerpo;
}

async function clickCatalogo(evento) {
    const catalogo = CATALOGOS[catalogoActual];
    const botonEditar = evento.target.closest("button[data-editar]");
    const botonEliminar = evento.target.closest("button[data-eliminar]");
    if (botonEditar) {
        const item = elementosCatalogo.find(x => String(x.id) === botonEditar.dataset.editar);
        pintarFormularioCatalogo(item);
        window.scrollTo(0, 0);
    }
    if (botonEliminar) {
        if (!confirm("Seguro que quieres eliminar este elemento?")) {
            return;
        }
        try {
            await api("DELETE", catalogo.ruta + "/" + botonEliminar.dataset.eliminar);
            mostrarMensaje("Eliminado correctamente", "ok");
            await pintarCatalogo(catalogoActual);
        } catch (error) {
            mostrarError(error);
        }
    }
}
