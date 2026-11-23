# Diseño de la mesa de pedidos

Ejercicio técnico para el equipo de ventas de El Puerto de Liverpool. No es un producto oficial de la empresa.

## Problema

Los pedidos entran por dos canales, la aplicación y el sitio web. Ventas necesita un servicio para crearlos, consultarlos, corregirlos y eliminarlos, y una mesa donde encontrar un folio o un cliente sin recorrer la lista completa.

## Arquitectura

```mermaid
flowchart LR
  subgraph canales [Origen del pedido]
    App[Aplicación]
    Web[Sitio web]
  end
  Mesa[Mesa de pedidos<br/>Next.js]
  Api[API REST v1<br/>Spring Boot]
  Db[(MongoDB<br/>liverpool_pedidos)]
  App --> Api
  Web --> Api
  Mesa --> Api
  Api --> Db
```

La mesa es la interfaz del equipo de ventas. La aplicación y el sitio web no se reimplementan: el pedido guarda el canal de origen (`APLICACION` o `SITIO_WEB`).

La interfaz habla con `/api` en su mismo origen. Next.js reenvía esa ruta al servicio Java, así el navegador no depende de CORS.

## Modelo

Un **usuario** es el cliente: nombre, apellido paterno, apellido materno y correo único.

Un **pedido** referencia al cliente y, además, guarda una copia de sus datos en el momento de la compra. Si después se corrige el directorio, los pedidos ya hechos no cambian. También guarda dirección de envío, código y descripción del producto, cantidad, precio unitario, canal y estatus.

El importe no se almacena. La respuesta lo calcula como precio unitario por cantidad, con dos decimales.

El folio (`LIV-000001`) sale de un contador atómico (`findAndModify` sobre la colección `secuencias`). Así dos altas simultáneas no reciben el mismo folio y no hace falta un replica set.

Cada cambio de estatus se agrega al historial embebido del pedido. Un solo documento basta: la actualización de un pedido es atómica en MongoDB.

## Búsqueda

Al guardar un pedido se construye `textoBusqueda`: folio, cliente, correo, dirección, código, descripción, canal y estatus, en minúsculas y sin acentos. El filtro y el typeahead consultan ese campo con un regex literal. Quien escribe `jose` encuentra a José, y un punto o un `+` en el término no se interpretan como operadores.

`GET /api/v1/pedidos/sugerencias?termino=` devuelve hasta ocho opciones, sin repetir el mismo cliente, correo o código. Elegir una opción manda su valor como `termino` del listado.

## Capas

| Capa | Responsabilidad |
| --- | --- |
| `web` | Contratos HTTP, validación de entrada, códigos de respuesta |
| `servicio` | Reglas: correo único, copia del cliente, folio, historial, sugerencias |
| `repositorio` | MongoRepository para altas y bajas; `PedidoBusqueda` para el filtro |
| `dominio` | `Usuario`, `Pedido`, estatus y canal |

Los errores salen en un solo formato (`estatus`, `mensaje`, `detalles`, `ruta`) desde `ManejadorExcepciones`.

## API

Documentada con springdoc. Con el servicio en marcha:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

| Método | Ruta | Uso |
| --- | --- | --- |
| POST, GET | `/api/v1/usuarios` | Alta y listado de clientes |
| GET, PUT, DELETE | `/api/v1/usuarios/{id}` | Consulta, reemplazo y baja |
| POST, GET | `/api/v1/pedidos` | Alta y listado (`termino`, `estatus`, `pagina`, `tamano`) |
| GET | `/api/v1/pedidos/sugerencias?termino=` | Typeahead, desde dos caracteres |
| GET, PUT, DELETE | `/api/v1/pedidos/{id}` | Detalle, corrección y baja |
| PATCH | `/api/v1/pedidos/{id}/estatus` | Cambio de estatus |
| GET | `/api/v1/salud` | Servicio y ping a MongoDB |

No se elimina un cliente que todavía tiene pedidos: la API responde `409`.

## Interfaz

Dos pantallas.

1. **Mesa** (`/`). Lista de pedidos, typeahead, filtro por estatus y altas de cliente y de pedido en la misma vista.
2. **Ficha** (`/pedidos/{id}`). Los tres grupos del encargo —cliente, entrega y pedido—, el historial, el cambio de estatus, la corrección y la baja.

En ancho estrecho la tabla se sustituye por fichas. Las acciones siguen en la página.

La frase de vacío es una sola constante, `MENSAJE_SIN_RESULTADOS`, y la usan el typeahead y la lista:

> No se tienen resultados que coincidan con los criterios de búsqueda.

## Qué quedó fuera

No hay autenticación. En local la mesa habla con el API por el proxy de Next. La publicación gratuita en Render deja el API en una URL pública y la base en MongoDB Atlas, con los datos de prueba. El arranque está en el README.
