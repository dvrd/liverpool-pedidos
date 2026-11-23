# Mesa de pedidos

Sistema para que el equipo de ventas de El Puerto de Liverpool gestione los pedidos que entran por la aplicación y por el sitio web.

Ejercicio técnico. No es un sitio ni un servicio oficial de Liverpool.

Repositorio: <https://github.com/dvrd/liverpool-pedidos>

- API REST en Java 11 y Spring Boot 2.7, con MongoDB.
- Mesa en Next.js: lista con typeahead y ficha del pedido.
- Contrato publicado con springdoc (Swagger UI y OpenAPI).

El diseño de las capas, el folio y la búsqueda está en [docs/diseno.md](docs/diseno.md).

## Requisitos

- Java 11 o superior y Maven 3.8 o superior.
- Node.js 20 o superior.
- MongoDB 7, en local o con Docker.

## Datos de conexión

Base `liverpool_pedidos`, sin usuario, cuando MongoDB corre en la máquina:

```text
mongodb://localhost:27017/liverpool_pedidos
```

Con Docker Compose la base exige usuario. Esos datos solo sirven para el ejercicio:

| | |
| --- | --- |
| Host | `localhost` (desde la máquina) o `mongo` (entre contenedores) |
| Puerto | `27017` |
| Base | `liverpool_pedidos` |
| Usuario | `liverpool` |
| Contraseña | `liverpool_pedidos` |
| URI | `mongodb://liverpool:liverpool_pedidos@localhost:27017/liverpool_pedidos?authSource=admin` |

La variable `MONGODB_URI` reemplaza la URI por defecto del API.

## Arranque con Docker

Desde la raíz del repositorio:

```bash
docker compose up --build
```

- Mesa: <http://localhost:3000>
- Swagger: <http://localhost:8080/swagger-ui.html>
- Salud: <http://localhost:8080/api/v1/salud>

La primera vez el API carga los datos de prueba.

## Arranque en local, sin Docker

Terminal 1, MongoDB. Si ya tienes un `mongod` de la versión 7:

```bash
mkdir -p .local/mongo
mongod --dbpath .local/mongo --bind_ip 127.0.0.1 --port 27017
```

Terminal 2, API:

```bash
cd backend
mvn spring-boot:run
```

Terminal 3, mesa:

```bash
cd frontend
npm install
npm run dev
```

Abre <http://localhost:3000>. El typeahead y la lista pasan por el mismo origen: Next reenvía `/api` a `http://127.0.0.1:8080`. Para apuntar a otro servicio, define `API_INTERNA_URL`.

En macOS, `scripts/levantar-local.sh` hace los tres pasos si el binario está en `.tools/mongodb` o si el puerto 27017 ya está ocupado por MongoDB. `scripts/detener-local.sh` los baja.

## Datos de prueba

Se insertan solo cuando la colección `usuarios` está vacía. Para repetir la carga, vacía la base y reinicia el API.

Con el `mongod` local de este repositorio basta borrar `.local/mongo` con el proceso detenido. Con Docker:

```bash
docker compose exec mongo mongosh -u liverpool -p liverpool_pedidos --authenticationDatabase admin --eval "db.getSiblingDB('liverpool_pedidos').dropDatabase()"
docker compose restart api
```

Ocho clientes:

| Cliente | Correo |
| --- | --- |
| Mariana Castillo Reyes | mariana.castillo@correo.com |
| José Luis Herrera Díaz | jose.herrera@correo.com |
| Andrea Paola Núñez Solís | andrea.nunez@correo.com |
| Diego Armando Paredes Cruz | diego.paredes@correo.com |
| Fernanda Itzel Morales Vega | fernanda.morales@correo.com |
| Ricardo Alonso Peña Ruiz | ricardo.pena@correo.com |
| Camila Sofía Delgado Ortiz | camila.delgado@correo.com |
| Héctor Iván Ramos Gil | hector.ramos@correo.com |

Catorce pedidos. El folio sigue el orden de carga; la mesa los muestra del más reciente al más antiguo.

| Folio | Cliente | Código | Concepto | Cant. | Precio | Canal | Estatus |
| --- | --- | --- | --- | --- | --- | --- | --- |
| LIV-000001 | Mariana Castillo Reyes | LP-MOD-10442 | Vestido midi de lino | 1 | 1890.00 | Sitio web | Confirmado |
| LIV-000002 | José Luis Herrera Díaz | LP-ELC-77810 | Audífonos inalámbricos | 1 | 2499.00 | Aplicación | Enviado |
| LIV-000003 | Andrea Paola Núñez Solís | LP-PER-22018 | Eau de parfum 50 ml | 2 | 1650.00 | Sitio web | Entregado |
| LIV-000004 | Diego Armando Paredes Cruz | LP-HOG-33007 | Juego de sábanas queen | 1 | 1299.00 | Aplicación | En preparación |
| LIV-000005 | Fernanda Itzel Morales Vega | LP-MOD-11890 | Bolso estructurado | 1 | 3290.00 | Sitio web | Recibido |
| LIV-000006 | Ricardo Alonso Peña Ruiz | LP-ELC-78102 | Pantalla 55 pulgadas | 1 | 14999.00 | Aplicación | Confirmado |
| LIV-000007 | Camila Sofía Delgado Ortiz | LP-BEB-45021 | Carriola compacta | 1 | 6790.00 | Sitio web | Enviado |
| LIV-000008 | Héctor Iván Ramos Gil | LP-DEP-61044 | Tenis para correr | 2 | 2190.00 | Aplicación | Entregado |
| LIV-000009 | Mariana Castillo Reyes | LP-HOG-34110 | Licuadora de vaso | 1 | 1899.00 | Sitio web | Cancelado |
| LIV-000010 | Andrea Paola Núñez Solís | LP-PER-22190 | Set de crema corporal | 3 | 420.00 | Aplicación | En preparación |
| LIV-000011 | Diego Armando Paredes Cruz | LP-ELC-79015 | Barra de sonido | 1 | 3990.00 | Sitio web | Recibido |
| LIV-000012 | Fernanda Itzel Morales Vega | LP-MOD-12110 | Zapatillas de piel | 1 | 2590.00 | Aplicación | Confirmado |
| LIV-000013 | José Luis Herrera Díaz | LP-HOG-35220 | Cafetera espresso | 1 | 5490.00 | Sitio web | Enviado |
| LIV-000014 | Camila Sofía Delgado Ortiz | LP-BEB-45088 | Monitor para bebé | 1 | 2890.00 | Aplicación | Recibido |

Para probar el typeahead: `jose` encuentra a José Luis aunque se escriba sin acento; `LP-ELC` lista los electrónicos; `zzzz` no tiene coincidencias.

## API

La referencia navegable es Swagger UI en `/swagger-ui.html`. El JSON OpenAPI está en `/v3/api-docs`. Hay una colección corta en [docs/ejemplos.http](docs/ejemplos.http).

```bash
curl -s http://localhost:8080/api/v1/pedidos/sugerencias?termino=jose
curl -s http://localhost:8080/api/v1/pedidos?termino=LP-ELC
```

Crear un cliente y un pedido:

```bash
curl -s -X POST http://localhost:8080/api/v1/usuarios \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Paula","apellidoPaterno":"Ibarra","apellidoMaterno":"Nava","correoElectronico":"paula.ibarra@correo.com"}'
```

El cuerpo de un pedido nuevo pide `usuarioId`, `direccionEnvio`, `codigoProducto`, `descripcionProducto`, `cantidad`, `precio` y `canal` (`APLICACION` o `SITIO_WEB`). El estatus es opcional y arranca en `RECIBIDO`.

## Pruebas

```bash
cd backend && mvn test
cd frontend && npm run build
```

## Interfaz

La mesa está en `/`. La ficha está en `/pedidos/{id}`. En pantallas angostas la tabla pasa a fichas y el formulario se apila. La frase que aparece cuando la búsqueda no coincide es:

> No se tienen resultados que coincidan con los criterios de búsqueda.

## Evidencias

El relato, desde el problema hasta las dos pantallas, está en [docs/recorrido.html](docs/recorrido.html). El video y las capturas de ese mismo flujo están en [docs/evidencias](docs/evidencias):

| Archivo | Qué muestra |
| --- | --- |
| `demo.webm` | Diseño, Swagger, lista, typeahead, filtro, búsqueda sin coincidencias, ficha, alta, cambio de estatus, baja y las dos pantallas en un teléfono |
| `07-typeahead.png` | Opciones mientras se escribe |
| `08-filtro.png` | Lista filtrada al elegir una opción |
| `09-sin-resultados.png` | Aviso cuando ninguna opción coincide |
| `11-detalle.png` | Ficha del pedido |
| `15-movil-lista.png` y `16-movil-detalle.png` | Las mismas pantallas en un marco angosto |

El contrato exportado del servicio está en [docs/openapi.json](docs/openapi.json). En marcha también se consulta en `/v3/api-docs` y en Swagger UI.

## Publicación en Render

El plan gratuito no guarda disco, no ofrece MongoDB y no puede recibir tráfico en la red privada. La publicación sin costo son dos servicios web (`plan: free` en `render.yaml`) y un clúster MongoDB Atlas M0. La mesa llama al API por su URL pública de `onrender.com`.

Esos servicios se duermen tras 15 minutos sin visitas. La primera petición puede tardar cerca de un minuto por servicio. No hay inicio de sesión: quien tenga la URL del API puede crear, ver y borrar pedidos. Los datos de prueba son ficticios. No subas datos reales.

1. En [MongoDB Atlas](https://www.mongodb.com/atlas) crea un clúster M0 en AWS `us-west-2` (Oregon, la misma región que Render). Crea un usuario de base de datos. En Network Access permite `0.0.0.0/0`: las instancias gratuitas de Render no tienen una IP de salida fija.
2. Copia la cadena de conexión con el nombre de la base en la ruta:

```text
mongodb+srv://USUARIO:CONTRASEÑA@CLUSTER.mongodb.net/liverpool_pedidos?retryWrites=true&w=majority
```

3. Abre el blueprint <https://render.com/deploy?repo=https://github.com/dvrd/liverpool-pedidos>, entra con una cuenta gratuita y pega esa cadena en `MONGODB_URI`. El archivo ya pide el plan Free; no elijas un plan de pago.
4. Al terminar, la mesa es la URL `onrender.com` del servicio `liverpool-pedidos-mesa`. Si `usuarios` está vacía, el primer arranque carga los 14 pedidos de prueba.

Si la mesa responde que el servicio no está disponible, copia la URL pública del servicio `liverpool-pedidos-api` en la variable `API_INTERNA_URL` de la mesa y vuelve a desplegar.

## Límites

El servicio no autentica. En local la mesa habla con el API en la misma máquina. En Render el API queda público por el límite del plan gratuito: nombres, correos y direcciones de prueba quedan al alcance de quien tenga la URL.
