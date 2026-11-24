# Frontend de CoWorkHub

Pagina web hecha solo con HTML, CSS y JavaScript (sin librerias). Consume la API del backend.

## Como abrirlo
El backend debe estar corriendo en `http://localhost:8080`. Elige UNA opcion para servir esta carpeta:

- **IntelliJ**: abre `frontend/index.html`, y en la esquina superior derecha del editor pulsa el icono de un navegador.
- **Python**: dentro de la carpeta `frontend` ejecuta `python -m http.server 5500` y abre <http://localhost:5500>.
- **VS Code**: extension *Live Server* > clic derecho en `index.html` > *Open with Live Server*.
- **Node**: dentro de la carpeta `frontend` ejecuta `npx serve -l 5500`.

No lo abras con doble clic (`file://`): el navegador bloquea las llamadas a la API.

## Si cambias de puerto
- Si el backend usa otro puerto, cambia `API_URL` en `js/config.js`.
- Si abres el frontend en un puerto distinto de los permitidos, agrega ese origen a la variable `CORS_ORIGINS` del backend.

## Archivos
| Archivo | Que hace |
|---|---|
| `index.html` | Estructura de la pagina |
| `css/styles.css` | Estilos |
| `js/config.js` | Direccion del backend |
| `js/api.js` | Llamadas a la API con el token y funciones de ayuda |
| `js/app.js` | Login, menu segun el rol y navegacion |
| `js/vistas.js` | Inicio, salas, disponibilidad, nueva reserva, reservas y consumo |
| `js/vistas-admin.js` | Miembros y catalogos (CRUD) |
