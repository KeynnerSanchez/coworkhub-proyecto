// ===================== Vistas: inicio, salas, disponibilidad, reservas, consumo =====================

// ---------- Inicio ----------
async function verInicio() {
    let bloque = "";
    if (perfil.rol === "MEMBER") {
        const consumo = await api("GET", "/api/miembros/" + perfil.miembroId + "/consumo");
        bloque = "<h3>Mis horas de este mes</h3>" + tarjetaConsumo(consumo);
    } else {
        const pendientes = await api("GET", "/api/reservas?estado=PENDING");
        const confirmadas = await api("GET", "/api/reservas?estado=CONFIRMED");
        bloque = `<div class="tarjetas">
            <div class="tarjeta"><span>Reservas pendientes</span><strong>${pendientes.length}</strong></div>
            <div class="tarjeta"><span>Reservas confirmadas</span><strong>${confirmadas.length}</strong></div>
        </div>`;
    }
    contenido.innerHTML = `<h2>Hola, ${esc(perfil.usuario)}</h2>
        <p>Tu rol es <span class="etiqueta">${esc(perfil.rol)}</span></p>${bloque}`;
}

function tarjetaConsumo(c) {
    const porcentaje = c.horasIncluidas > 0 ? Math.min(100, (c.horasUsadas / c.horasIncluidas) * 100) : 100;
    return `<p>Mes: <strong>${esc(c.mes)}</strong></p>
        <div class="barraProgreso"><div style="width:${porcentaje}%"></div></div>
        <div class="tarjetas">
            <div class="tarjeta"><span>Horas incluidas</span><strong>${c.horasIncluidas}</strong></div>
            <div class="tarjeta"><span>Horas usadas</span><strong>${c.horasUsadas}</strong></div>
            <div class="tarjeta"><span>Horas restantes</span><strong>${c.horasRestantes}</strong></div>
            <div class="tarjeta"><span>Horas excedentes</span><strong>${c.horasExcedentes}</strong></div>
        </div>`;
}

// ---------- Salas ----------
async function verSalas() {
    const sedes = await api("GET", "/api/sedes");
    contenido.innerHTML = `<h2>Salas</h2>
        <div class="barra">
            <label>Sede
                <select id="filtroSede">
                    <option value="">Todas</option>
                    ${sedes.map(s => `<option value="${s.id}">${esc(s.name)} - ${esc(s.city)}</option>`).join("")}
                </select>
            </label>
        </div>
        <div id="tablaSalas"></div>`;

    const pintar = async () => {
        const sede = $("filtroSede").value;
        try {
            const salas = await api("GET", "/api/salas" + (sede ? "?sedeId=" + sede : ""));
            $("tablaSalas").innerHTML = tablaSalas(salas);
        } catch (error) {
            mostrarError(error);
        }
    };
    $("filtroSede").onchange = pintar;
    await pintar();
}

function tablaSalas(salas) {
    if (salas.length === 0) {
        return "<p>No hay salas.</p>";
    }
    const filas = salas.map(s => `<tr>
        <td>${esc(s.name)}</td><td>${esc(s.type)}</td><td>${s.capacity}</td>
        <td>${dinero(s.hourlyRate)}</td><td>${esc(s.sedeName)}</td>
        <td>${s.equipamientos.map(e => esc(e.name)).join(", ")}</td>
        <td>${s.active ? "Activa" : "Inactiva"}</td></tr>`).join("");
    return `<div class="tabla"><table>
        <thead><tr><th>Sala</th><th>Tipo</th><th>Capacidad</th><th>Tarifa por hora</th><th>Sede</th><th>Equipamiento</th><th>Estado</th></tr></thead>
        <tbody>${filas}</tbody></table></div>`;
}

// ---------- Disponibilidad ----------
async function verDisponibilidad() {
    const sedes = await api("GET", "/api/sedes");
    const equipos = await api("GET", "/api/equipamientos");
    contenido.innerHTML = `<h2>Disponibilidad de salas</h2>
        <form id="formDisponibilidad" class="formulario">
            <label>Sede <select id="dSede">${sedes.map(s => `<option value="${s.id}">${esc(s.name)}</option>`).join("")}</select></label>
            <label>Fecha <input type="date" id="dFecha" value="${hoyMasDias(1)}" required></label>
            <label>Hora inicio <input type="time" id="dInicio" value="10:00" required></label>
            <label>Hora fin <input type="time" id="dFin" value="12:00" required></label>
            <label>Capacidad minima <input type="number" id="dCapacidad" min="1" placeholder="opcional"></label>
            <fieldset><legend>Equipamiento (opcional)</legend>
                ${equipos.map(e => `<label class="chk"><input type="checkbox" class="dEquipo" value="${e.id}"> ${esc(e.name)}</label>`).join("")}
            </fieldset>
            <button type="submit">Buscar</button>
        </form>
        <div id="resultadoDisponibilidad"></div>`;

    $("formDisponibilidad").onsubmit = async (evento) => {
        evento.preventDefault();
        const fecha = $("dFecha").value;
        const inicio = $("dInicio").value;
        const fin = $("dFin").value;
        let ruta = "/api/disponibilidad?sedeId=" + $("dSede").value + "&fecha=" + fecha
            + "&horaInicio=" + inicio + "&horaFin=" + fin;
        if ($("dCapacidad").value) {
            ruta += "&capacidadMinima=" + $("dCapacidad").value;
        }
        document.querySelectorAll(".dEquipo:checked").forEach(c => {
            ruta += "&equipamientoIds=" + c.value;
        });
        try {
            const salas = await api("GET", ruta);
            if (salas.length === 0) {
                $("resultadoDisponibilidad").innerHTML = "<p>No hay salas libres con esas condiciones.</p>";
                return;
            }
            const filas = salas.map(s => `<tr><td>${esc(s.name)}</td><td>${s.capacity}</td><td>${dinero(s.hourlyRate)}</td>
                <td>${s.equipamientos.map(e => esc(e.name)).join(", ")}</td>
                <td><button class="chico verde" data-sala="${s.id}">Reservar</button></td></tr>`).join("");
            $("resultadoDisponibilidad").innerHTML = `<div class="tabla"><table>
                <thead><tr><th>Sala libre</th><th>Capacidad</th><th>Tarifa por hora</th><th>Equipamiento</th><th></th></tr></thead>
                <tbody>${filas}</tbody></table></div>`;
        } catch (error) {
            mostrarError(error);
        }
    };

    // boton "Reservar": lleva al formulario con los datos ya llenos
    $("resultadoDisponibilidad").onclick = (evento) => {
        const boton = evento.target.closest("button[data-sala]");
        if (boton) {
            irA("nueva", { salaId: boton.dataset.sala, fecha: $("dFecha").value, inicio: $("dInicio").value, fin: $("dFin").value });
        }
    };
}

// ---------- Nueva reserva ----------
async function verNueva(previo) {
    previo = previo || {};
    const todasLasSalas = await api("GET", "/api/salas");
    const salas = todasLasSalas.filter(s => s.active);
    const servicios = await api("GET", "/api/servicios-adicionales");
    let miembros = [];
    if (esStaff()) {
        miembros = await api("GET", "/api/miembros?estado=ACTIVE");
    }

    const campoMiembro = esStaff()
        ? `<label>Miembro <select id="nMiembro">${miembros.map(m => `<option value="${m.id}">${esc(m.fullName)}</option>`).join("")}</select></label>`
        : `<p>Reserva a nombre de: <strong>${esc(perfil.usuario)}</strong></p>`;

    const bloqueServicios = servicios.map(s => `<div class="servicio">
        <label class="chk"><input type="checkbox" class="chkServicio" value="${s.id}"> ${esc(s.name)} (${dinero(s.unitPrice)})</label>
        <input type="number" class="cantidad" id="cant${s.id}" min="1" value="1"></div>`).join("");

    contenido.innerHTML = `<h2>Nueva reserva</h2>
        <form id="formNueva" class="formulario">
            ${campoMiembro}
            <label>Sala <select id="nSala">${salas.map(s => `<option value="${s.id}" ${String(s.id) === String(previo.salaId) ? "selected" : ""}>${esc(s.name)} (${esc(s.sedeName)}, cap. ${s.capacity})</option>`).join("")}</select></label>
            <label>Fecha <input type="date" id="nFecha" value="${previo.fecha || hoyMasDias(1)}" required></label>
            <label>Hora inicio <input type="time" id="nInicio" value="${previo.inicio || "10:00"}" required></label>
            <label>Hora fin <input type="time" id="nFin" value="${previo.fin || "12:00"}" required></label>
            <label>Asistentes <input type="number" id="nAsistentes" min="1" value="1" required></label>
            <fieldset><legend>Servicios adicionales (opcional)</legend>${bloqueServicios}</fieldset>
            <button type="submit">Reservar</button>
        </form>
        <div id="resultadoNueva"></div>`;

    $("formNueva").onsubmit = async (evento) => {
        evento.preventDefault();
        const fecha = $("nFecha").value;
        const lineas = [];
        document.querySelectorAll(".chkServicio:checked").forEach(c => {
            lineas.push({ servicioId: Number(c.value), quantity: Number($("cant" + c.value).value) });
        });
        const cuerpo = {
            miembroId: esStaff() ? Number($("nMiembro").value) : perfil.miembroId,
            salaId: Number($("nSala").value),
            startTime: fecha + "T" + $("nInicio").value + ":00",
            endTime: fecha + "T" + $("nFin").value + ":00",
            attendees: Number($("nAsistentes").value),
            services: lineas
        };
        try {
            const reserva = await api("POST", "/api/reservas", cuerpo);
            mostrarMensaje("Reserva creada con el numero " + reserva.id, "ok");
            $("resultadoNueva").innerHTML = "<h3>Reserva creada</h3>" + tablaReservas([reserva]);
        } catch (error) {
            mostrarError(error);
        }
    };
}

// ---------- Reservas ----------
let rutaReservasActual = "";

async function verReservas() {
    if (!esStaff()) {
        contenido.innerHTML = `<h2>Mis reservas</h2><div id="tablaReservas"></div>`;
        $("tablaReservas").onclick = clickReserva;
        await cargarReservas("/api/reservas?miembroId=" + perfil.miembroId);
        return;
    }

    const miembros = await api("GET", "/api/miembros");
    const salas = await api("GET", "/api/salas");
    const estados = ["PENDING", "CONFIRMED", "CANCELLED", "COMPLETED"];
    contenido.innerHTML = `<h2>Reservas</h2>
        <div class="filtros">
            <div class="barra">
                <label>Por estado <select id="fEstado">${estados.map(e => `<option>${e}</option>`).join("")}</select></label>
                <button id="bEstado">Ver</button>
            </div>
            <div class="barra">
                <label>Por miembro <select id="fMiembro">${miembros.map(m => `<option value="${m.id}">${esc(m.fullName)}</option>`).join("")}</select></label>
                <button id="bMiembro">Ver</button>
            </div>
            <div class="barra">
                <label>Por sala <select id="fSala">${salas.map(s => `<option value="${s.id}">${esc(s.name)}</option>`).join("")}</select></label>
                <label>Fecha <input type="date" id="fFecha" value="${hoyMasDias(1)}"></label>
                <button id="bSala">Ver</button>
            </div>
        </div>
        <div id="tablaReservas"><p>Elige un filtro para ver las reservas.</p></div>`;

    $("bEstado").onclick = () => cargarReservas("/api/reservas?estado=" + $("fEstado").value);
    $("bMiembro").onclick = () => cargarReservas("/api/reservas?miembroId=" + $("fMiembro").value);
    $("bSala").onclick = () => cargarReservas("/api/reservas?salaId=" + $("fSala").value + "&fecha=" + $("fFecha").value);
    $("tablaReservas").onclick = clickReserva;
}

async function cargarReservas(ruta) {
    rutaReservasActual = ruta;
    try {
        const lista = await api("GET", ruta);
        $("tablaReservas").innerHTML = tablaReservas(lista);
    } catch (error) {
        mostrarError(error);
    }
}

function botonesReserva(r) {
    let botones = `<button class="chico" data-accion="detalle" data-id="${r.id}">Detalle</button>`;
    if (esStaff() && r.status === "PENDING") {
        botones += `<button class="chico verde" data-accion="confirmar" data-id="${r.id}">Confirmar</button>`;
    }
    if (esStaff() && r.status === "CONFIRMED") {
        botones += `<button class="chico verde" data-accion="completar" data-id="${r.id}">Completar</button>`;
    }
    if (r.status === "PENDING" || r.status === "CONFIRMED") {
        botones += `<button class="chico peligro" data-accion="cancelar" data-id="${r.id}">Cancelar</button>`;
    }
    return botones;
}

function tablaReservas(lista) {
    if (lista.length === 0) {
        return "<p>No hay reservas.</p>";
    }
    const filas = lista.map(r => `<tr>
        <td>${r.id}</td><td>${esc(r.salaNombre)}</td><td>${esc(r.miembroNombre)}</td>
        <td>${fechaHora(r.startTime)}</td><td>${fechaHora(r.endTime)}</td>
        <td><span class="etiqueta ${r.status}">${r.status}</span></td>
        <td>${dinero(r.totalCost)}</td><td>${dinero(r.cancellationFee)}</td>
        <td>${botonesReserva(r)}</td></tr>`).join("");
    return `<div class="tabla"><table>
        <thead><tr><th>N.</th><th>Sala</th><th>Miembro</th><th>Inicio</th><th>Fin</th><th>Estado</th><th>Total</th><th>Cargo cancelacion</th><th>Acciones</th></tr></thead>
        <tbody>${filas}</tbody></table></div>`;
}

// Un solo "click" para todos los botones de la tabla
async function clickReserva(evento) {
    const boton = evento.target.closest("button[data-accion]");
    if (!boton) {
        return;
    }
    const id = boton.dataset.id;
    const accion = boton.dataset.accion;
    try {
        if (accion === "detalle") {
            await abrirDetalle(id);
            return;
        }
        if (accion === "cancelar" && !confirm("Seguro que quieres cancelar la reserva " + id + "?")) {
            return;
        }
        const reserva = await api("POST", "/api/reservas/" + id + "/" + accion);
        if (accion === "cancelar") {
            mostrarMensaje("Reserva cancelada. Cargo de cancelacion: " + dinero(reserva.cancellationFee), "ok");
        } else {
            mostrarMensaje("Reserva " + id + " ahora esta " + reserva.status, "ok");
        }
        await cargarReservas(rutaReservasActual);
    } catch (error) {
        mostrarError(error);
    }
}

// ---------- Detalle de una reserva (ventana emergente) ----------
async function abrirDetalle(id) {
    $("modalContenido").onclick = null;
    const r = await api("GET", "/api/reservas/" + id);
    const editable = r.status === "PENDING";
    let servicios = [];
    if (editable) {
        servicios = await api("GET", "/api/servicios-adicionales");
    }

    const lineas = r.services.map(l => `<tr><td>${esc(l.servicioNombre)}</td><td>${l.quantity}</td>
        <td>${dinero(l.appliedUnitPrice)}</td><td>${dinero(l.subtotal)}</td>
        ${editable ? `<td><button class="chico peligro" data-quitar="${l.servicioId}">Quitar</button></td>` : ""}</tr>`).join("");

    const formularioServicio = editable ? `<div class="barra">
        <label>Servicio <select id="detServicio">${servicios.map(s => `<option value="${s.id}">${esc(s.name)} (${dinero(s.unitPrice)})</option>`).join("")}</select></label>
        <label>Cantidad <input type="number" id="detCantidad" min="1" value="1"></label>
        <button id="detAgregar">Agregar servicio</button></div>` : "<p>Los servicios solo se pueden cambiar si la reserva esta PENDING.</p>";

    $("modalContenido").innerHTML = `<h3>Reserva ${r.id} <span class="etiqueta ${r.status}">${r.status}</span></h3>
        <p><strong>${esc(r.salaNombre)}</strong> para ${esc(r.miembroNombre)}<br>
        ${fechaHora(r.startTime)} a ${fechaHora(r.endTime)} - ${r.attendees} asistente(s)</p>
        <div class="tarjetas">
            <div class="tarjeta"><span>Costo sala</span><strong>${dinero(r.roomCost)}</strong></div>
            <div class="tarjeta"><span>Servicios</span><strong>${dinero(r.servicesCost)}</strong></div>
            <div class="tarjeta"><span>Total</span><strong>${dinero(r.totalCost)}</strong></div>
        </div>
        <h4>Servicios adicionales</h4>
        ${r.services.length === 0 ? "<p>Sin servicios.</p>" : `<div class="tabla"><table><thead><tr><th>Servicio</th><th>Cantidad</th><th>Precio</th><th>Subtotal</th>${editable ? "<th></th>" : ""}</tr></thead><tbody>${lineas}</tbody></table></div>`}
        ${formularioServicio}`;

    const recargar = async () => {
        await abrirDetalle(id);
        if ($("tablaReservas") && rutaReservasActual) {
            await cargarReservas(rutaReservasActual);
        }
    };

    if (editable) {
        $("detAgregar").onclick = async () => {
            try {
                await api("POST", "/api/reservas/" + id + "/servicios", {
                    servicioId: Number($("detServicio").value),
                    quantity: Number($("detCantidad").value)
                });
                await recargar();
            } catch (error) {
                mostrarError(error);
            }
        };
        $("modalContenido").onclick = async (evento) => {
            const boton = evento.target.closest("button[data-quitar]");
            if (!boton) {
                return;
            }
            try {
                await api("DELETE", "/api/reservas/" + id + "/servicios/" + boton.dataset.quitar);
                await recargar();
            } catch (error) {
                mostrarError(error);
            }
        };
    }
    if (!$("modal").open) {
        $("modal").showModal();
    }
}

// ---------- Consumo de horas (para el MEMBER) ----------
async function verConsumo() {
    contenido.innerHTML = `<h2>Mi consumo de horas</h2>
        <div class="barra">
            <label>Mes <input type="month" id="cMes" value="${mesActual()}"></label>
            <button id="cConsultar">Consultar</button>
        </div>
        <div id="resultadoConsumo"></div>`;
    const consultar = async () => {
        try {
            const consumo = await api("GET", "/api/miembros/" + perfil.miembroId + "/consumo?mes=" + $("cMes").value);
            $("resultadoConsumo").innerHTML = tarjetaConsumo(consumo);
        } catch (error) {
            mostrarError(error);
        }
    };
    $("cConsultar").onclick = consultar;
    await consultar();
}
