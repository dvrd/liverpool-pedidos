// One-way, idempotent move from the Spanish collection names and fields
// to the English ones. Safe to run on every deploy. A database that is
// already English is left as it is.
const statusWords = {
  RECIBIDO: "RECEIVED",
  CONFIRMADO: "CONFIRMED",
  EN_PREPARACION: "PREPARING",
  ENVIADO: "SHIPPED",
  ENTREGADO: "DELIVERED",
  CANCELADO: "CANCELLED",
};

const channelWords = {
  APLICACION: "APP",
  SITIO_WEB: "WEBSITE",
};

function has(name) {
  return db.getCollectionNames().includes(name);
}

function renameCollection(from, to) {
  if (has(from) && !has(to)) {
    db.getCollection(from).renameCollection(to);
    print("renamed " + from + " -> " + to);
    return;
  }
  print("kept " + from + " (present=" + has(from) + ", " + to + " present=" + has(to) + ")");
}

function dropSecondaryIndexes(name) {
  if (!has(name)) return;
  db.getCollection(name).getIndexes().forEach((index) => {
    if (index.name === "_id_") return;
    db.getCollection(name).dropIndex(index.name);
    print("dropped index " + name + "." + index.name);
  });
}

function statusName(value) {
  return statusWords[value] || value;
}

renameCollection("usuarios", "customers");
renameCollection("pedidos", "orders");
renameCollection("secuencias", "sequences");

dropSecondaryIndexes("customers");
dropSecondaryIndexes("orders");

let customersMoved = 0;
if (has("customers")) {
  db.customers.find({ nombre: { $exists: true } }).forEach((doc) => {
    db.customers.updateOne({ _id: doc._id }, {
      $set: {
        _class: "mx.com.liverpool.orders.domain.Customer",
        firstName: doc.nombre,
        paternalLastName: doc.apellidoPaterno,
        maternalLastName: doc.apellidoMaterno,
        email: doc.correoElectronico,
        createdAt: doc.creadoEn,
        updatedAt: doc.actualizadoEn,
      },
      $unset: {
        nombre: "",
        apellidoPaterno: "",
        apellidoMaterno: "",
        correoElectronico: "",
        creadoEn: "",
        actualizadoEn: "",
      },
    });
    customersMoved += 1;
  });
  db.customers.updateMany(
    { _class: { $ne: "mx.com.liverpool.orders.domain.Customer" } },
    { $set: { _class: "mx.com.liverpool.orders.domain.Customer" } },
  );
}
print("customers moved " + customersMoved);

let ordersMoved = 0;
if (has("orders")) {
  db.orders.find({ folio: { $exists: true } }).forEach((doc) => {
    const person = doc.cliente || {};
    const history = (doc.historial || []).map((event) => ({
      status: statusName(event.estatus),
      occurredAt: event.ocurridoEn,
    }));
    db.orders.updateOne({ _id: doc._id }, {
      $set: {
        _class: "mx.com.liverpool.orders.domain.Order",
        orderNumber: doc.folio,
        customerId: doc.usuarioId,
        customer: {
          firstName: person.nombre,
          paternalLastName: person.apellidoPaterno,
          maternalLastName: person.apellidoMaterno,
          email: person.correoElectronico,
        },
        shippingAddress: doc.direccionEnvio,
        productCode: doc.codigoProducto,
        productDescription: doc.descripcionProducto,
        quantity: doc.cantidad,
        unitPrice: doc.precio,
        channel: channelWords[doc.canal] || doc.canal,
        status: statusName(doc.estatus),
        history: history,
        searchText: doc.textoBusqueda,
        createdAt: doc.creadoEn,
        updatedAt: doc.actualizadoEn,
      },
      $unset: {
        folio: "",
        usuarioId: "",
        cliente: "",
        direccionEnvio: "",
        codigoProducto: "",
        descripcionProducto: "",
        cantidad: "",
        precio: "",
        canal: "",
        estatus: "",
        historial: "",
        textoBusqueda: "",
        creadoEn: "",
        actualizadoEn: "",
      },
    });
    ordersMoved += 1;
  });
  db.orders.updateMany(
    { _class: { $ne: "mx.com.liverpool.orders.domain.Order" } },
    { $set: { _class: "mx.com.liverpool.orders.domain.Order" } },
  );
}
print("orders moved " + ordersMoved);

if (has("sequences")) {
  const previous = db.sequences.findOne({ _id: "pedido" });
  if (previous) {
    const value = previous.valor != null ? previous.valor : previous.value;
    const current = db.sequences.findOne({ _id: "order" });
    const kept = current && current.value != null ? Math.max(current.value, value) : value;
    db.sequences.updateOne(
      { _id: "order" },
      {
        $set: {
          value: kept,
          _class: "mx.com.liverpool.orders.domain.Sequence",
        },
      },
      { upsert: true },
    );
    db.sequences.deleteOne({ _id: "pedido" });
    print("sequence order=" + kept);
  }
}

const spanishLeft = has("pedidos") ? db.pedidos.countDocuments({ folio: { $exists: true } }) : 0;
const ordersNow = has("orders") ? db.orders.countDocuments() : 0;
print("orders now " + ordersNow + ", spanish left " + spanishLeft);
if (spanishLeft > 0 && ordersNow === 0) {
  print("Spanish orders were not migrated");
  quit(1);
}
