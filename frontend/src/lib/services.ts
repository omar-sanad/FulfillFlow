import { api } from './api';
import type { Product, Order, Delivery, Notification, Page } from './types';

const BASE = {
  inventory: '/api/inventory',
  order: '/api/order',
  delivery: '/api/delivery',
  notification: '/api/notification',
};

export const productService = {
  list: async () => (await api.get<Page<Product>>(`${BASE.inventory}/products?activeOnly=true`)).content,
  get: (id: string) => api.get<Product>(`${BASE.inventory}/products/${id}`),
  create: (data: Partial<Product>) => api.post<Product>(`${BASE.inventory}/products`, data),
  restock: (id: string, qty: number) =>
    api.post<Product>(`${BASE.inventory}/products/${id}/restock`, { quantity: qty }),
};

export const orderService = {
  list: async () => (await api.get<Page<Order>>(`${BASE.order}/orders`)).content,
  get: (id: string) => api.get<Order>(`${BASE.order}/orders/${id}`),
  create: (data: {
    lines: { productId: string; sku: string; name: string; unitPriceCents: number; quantity: number }[];
    shippingAddress: { fullName: string; line1: string; city: string; postalCode: string; country: string };
  }) => api.post<Order>(`${BASE.order}/orders`, data),
  pay: (id: string) =>
    api.post<Order>(`${BASE.order}/orders/${id}/transitions`, { action: 'pay', reason: 'Card payment' }),
  cancel: (id: string, reason: string) =>
    api.post<Order>(`${BASE.order}/orders/${id}/transitions`, { action: 'cancel', reason }),
};

export const deliveryService = {
  list: () => api.get<Delivery[]>(`${BASE.delivery}/deliveries`),
  byOrder: (orderId: string) => api.get<Delivery>(`${BASE.delivery}/deliveries/by-order/${orderId}`),
  pickup: (id: string) => api.post<Delivery>(`${BASE.delivery}/deliveries/${id}/pickup`),
  complete: (id: string) => api.post<Delivery>(`${BASE.delivery}/deliveries/${id}/complete`),
  fail: (id: string, reason: string) =>
    api.post<Delivery>(`${BASE.delivery}/deliveries/${id}/fail`, { reason }),
};

export const notificationService = {
  list: () => api.get<Notification[]>(`${BASE.notification}/notifications`),
  byOrder: (orderId: string) =>
    api.get<Notification[]>(`${BASE.notification}/notifications/by-order/${orderId}`),
};
