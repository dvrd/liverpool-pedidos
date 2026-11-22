export type EstatusPedido =
  | "RECIBIDO"
  | "CONFIRMADO"
  | "EN_PREPARACION"
  | "ENVIADO"
  | "ENTREGADO"
  | "CANCELADO";

export type CanalPedido = "APLICACION" | "SITIO_WEB";

export type Cliente = {
  nombre: string;
  apellidoPaterno: string;
  apellidoMaterno: string;
  nombreCompleto: string;
  correoElectronico: string;
};

export type EventoEstatus = {
  estatus: EstatusPedido;
  ocurridoEn: string;
};

export type Pedido = {
  id: string;
  folio: string;
  canal: CanalPedido;
  usuarioId: string;
  cliente: Cliente;
  direccionEnvio: string;
  codigoProducto: string;
  descripcionProducto: string;
  cantidad: number;
  precio: number;
  importeTotal: number;
  estatus: EstatusPedido;
  historial: EventoEstatus[];
  creadoEn: string;
  actualizadoEn: string;
};

export type Pagina<T> = {
  contenido: T[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
};

export type TipoSugerencia = "FOLIO" | "CLIENTE" | "CORREO" | "PRODUCTO";

export type Sugerencia = {
  tipo: TipoSugerencia;
  etiqueta: string;
  detalle: string;
  valor: string;
};

export type Usuario = {
  id: string;
  nombre: string;
  apellidoPaterno: string;
  apellidoMaterno: string;
  nombreCompleto: string;
  correoElectronico: string;
};

export type AltaUsuario = {
  nombre: string;
  apellidoPaterno: string;
  apellidoMaterno: string;
  correoElectronico: string;
};

export type AltaPedido = {
  usuarioId: string;
  direccionEnvio: string;
  codigoProducto: string;
  descripcionProducto: string;
  cantidad: number;
  precio: number;
  canal: CanalPedido;
  estatus?: EstatusPedido;
};

export type CorreccionPedido = {
  direccionEnvio: string;
  codigoProducto: string;
  descripcionProducto: string;
  cantidad: number;
  precio: number;
  canal: CanalPedido;
  estatus: EstatusPedido;
};
