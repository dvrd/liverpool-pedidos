export type OrderStatus =
  | "RECEIVED"
  | "CONFIRMED"
  | "PREPARING"
  | "SHIPPED"
  | "DELIVERED"
  | "CANCELLED";

export type OrderChannel = "APP" | "WEBSITE";

export type CustomerSnapshot = {
  firstName: string;
  paternalLastName: string;
  maternalLastName: string;
  fullName: string;
  email: string;
};

export type StatusEvent = {
  status: OrderStatus;
  occurredAt: string;
};

export type Order = {
  id: string;
  orderNumber: string;
  channel: OrderChannel;
  customerId: string;
  customer: CustomerSnapshot;
  shippingAddress: string;
  productCode: string;
  productDescription: string;
  quantity: number;
  unitPrice: number;
  totalAmount: number;
  status: OrderStatus;
  history: StatusEvent[];
  createdAt: string;
  updatedAt: string;
};

export type Page<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type SuggestionType = "ORDER_NUMBER" | "CUSTOMER" | "EMAIL" | "PRODUCT";

export type Suggestion = {
  type: SuggestionType;
  label: string;
  detail: string;
  value: string;
};

export type Customer = {
  id: string;
  firstName: string;
  paternalLastName: string;
  maternalLastName: string;
  fullName: string;
  email: string;
};

export type CreateCustomer = {
  firstName: string;
  paternalLastName: string;
  maternalLastName: string;
  email: string;
};

export type CreateOrder = {
  customerId: string;
  shippingAddress: string;
  productCode: string;
  productDescription: string;
  quantity: number;
  unitPrice: number;
  channel: OrderChannel;
  status?: OrderStatus;
};

export type UpdateOrder = {
  shippingAddress: string;
  productCode: string;
  productDescription: string;
  quantity: number;
  unitPrice: number;
  channel: OrderChannel;
  status: OrderStatus;
};
