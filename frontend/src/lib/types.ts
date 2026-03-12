export interface Product {
  id: string;
  sku: string;
  name: string;
  description: string;
  priceCents: number;
  currency: string;
  weightGrams: number;
  active: boolean;
  version: number;
  createdAt: string;
  updatedAt: string;
  availableQuantity: number;
  reservedQuantity: number;
}

export interface OrderLine {
  id: string;
  productId: string;
  sku: string;
  name: string;
  unitPriceCents: number;
  quantity: number;
  lineTotalCents: number;
  currency: string;
  createdAt: string;
}

export interface ShippingAddress {
  fullName: string;
  line1: string;
  line2: string | null;
  city: string;
  postalCode: string;
  country: string;
  phone: string | null;
}

export type OrderStatus =
  | 'CREATED' | 'PAID' | 'FULFILLED' | 'CANCELLED' | 'FAILED';

export interface Order {
  id: string;
  customerId: string;
  status: OrderStatus;
  totalCents: number;
  currency: string;
  lines: OrderLine[];
  shippingAddress: ShippingAddress;
  notes: string | null;
  placedAt: string | null;
  paidAt: string | null;
  fulfilledAt: string | null;
  cancelledAt: string | null;
  cancelReason: string | null;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  empty: boolean;
}

export type DeliveryStatus =
  | 'SCHEDULED' | 'IN_TRANSIT' | 'COMPLETED' | 'CANCELLED' | 'FAILED';

export interface Delivery {
  id: string;
  orderId: string;
  customerId: string;
  courierId: string;
  trackingNumber: string;
  status: DeliveryStatus;
  scheduledAt: string;
  pickedUpAt: string | null;
  deliveredAt: string | null;
  failedAt: string | null;
  cancelledAt: string | null;
  failureReason: string | null;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface Notification {
  id: string;
  orderId: string | null;
  customerId: string;
  channel: string;
  template: string;
  recipient: string;
  subject: string;
  status: string;
  sentAt: string | null;
  failureReason: string | null;
  version: number;
  createdAt: string;
  updatedAt: string;
}
