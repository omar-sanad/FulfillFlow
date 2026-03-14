import { motion, AnimatePresence } from 'framer-motion';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  ArrowLeft, CreditCard, XCircle, Truck, CheckCircle2, Clock, Loader2, MapPin, User, Bell,
} from 'lucide-react';
import { orderService, deliveryService, notificationService } from '../lib/services';
import { useAuth } from '../context/AuthContext';
import { OrderStatusBadge, DeliveryStatusBadge } from '../components/ui/StatusBadge';
import { formatPrice, formatDateTime, shortId } from '../lib/utils';
import type { Order, Delivery, Notification } from '../lib/types';

interface TimelineEntry {
  label: string;
  reason: string;
  actor: string;
  occurredAt: string | null;
  icon: React.ElementType;
}

function buildTimeline(order: Order, delivery: Delivery | undefined, notifs: Notification[]): TimelineEntry[] {
  const entries: TimelineEntry[] = [];
  if (order.placedAt) entries.push({ label: 'Order placed', reason: 'Order created and awaiting payment', actor: 'order-service', occurredAt: order.placedAt, icon: Clock });
  if (order.paidAt) entries.push({ label: 'Payment received', reason: 'Customer paid — reservation saga triggered', actor: 'order-service', occurredAt: order.paidAt, icon: CreditCard });
  if (delivery?.scheduledAt) entries.push({ label: 'Delivery scheduled', reason: `Courier ${delivery.courierId} assigned · ${delivery.trackingNumber}`, actor: 'delivery-service', occurredAt: delivery.scheduledAt, icon: Truck });
  if (delivery?.pickedUpAt) entries.push({ label: 'Parcel picked up', reason: 'In transit to destination', actor: 'delivery-service', occurredAt: delivery.pickedUpAt, icon: Truck });
  if (delivery?.deliveredAt) entries.push({ label: 'Delivered', reason: 'Delivery completed successfully', actor: 'delivery-service', occurredAt: delivery.deliveredAt, icon: CheckCircle2 });
  if (delivery?.failedAt) entries.push({ label: 'Delivery failed', reason: delivery.failureReason ?? 'Delivery could not be completed', actor: 'delivery-service', occurredAt: delivery.failedAt, icon: XCircle });
  if (order.fulfilledAt) entries.push({ label: 'Order fulfilled', reason: 'Inventory confirmed, order closed', actor: 'order-service', occurredAt: order.fulfilledAt, icon: CheckCircle2 });
  if (order.cancelledAt) entries.push({ label: 'Order cancelled', reason: order.cancelReason ?? 'Cancelled', actor: 'order-service', occurredAt: order.cancelledAt, icon: XCircle });
  const schedNotif = notifs.find((n) => n.template === 'delivery.scheduled');
  if (schedNotif) entries.push({ label: 'SMS sent', reason: `Tracking ${delivery?.trackingNumber} dispatched to customer`, actor: 'notification-service', occurredAt: schedNotif.createdAt, icon: Bell });
  return entries.sort((a, b) => new Date(a.occurredAt ?? 0).getTime() - new Date(b.occurredAt ?? 0).getTime());
}

export function OrderDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const qc = useQueryClient();

  const { hasAnyRole } = useAuth();
  const canManageDelivery = hasAnyRole('administrator', 'warehouse', 'courier');
  const canManageOrder = hasAnyRole('customer', 'administrator');

  const orderQ = useQuery({
    queryKey: ['order', id], queryFn: () => orderService.get(id!),
    enabled: !!id, refetchInterval: 4000,
  });
  const deliveryQ = useQuery({
    queryKey: ['delivery-by-order', id], queryFn: () => deliveryService.byOrder(id!),
    enabled: !!id, refetchInterval: 4000,
  });
  const notifsQ = useQuery({
    queryKey: ['notifications-by-order', id], queryFn: () => notificationService.byOrder(id!),
    enabled: !!id, refetchInterval: 8000,
  });

  const order = orderQ.data;
  const delivery = deliveryQ.data;
  const notifs = notifsQ.data ?? [];

  const payMut = useMutation({ mutationFn: () => orderService.pay(id!), onSuccess: () => qc.invalidateQueries() });
  const cancelMut = useMutation({
    mutationFn: (reason: string) => orderService.cancel(id!, reason),
    onSuccess: () => qc.invalidateQueries(),
  });
  const pickupMut = useMutation({ mutationFn: () => deliveryService.pickup(delivery!.id), onSuccess: () => qc.invalidateQueries() });
  const completeMut = useMutation({ mutationFn: () => deliveryService.complete(delivery!.id), onSuccess: () => qc.invalidateQueries() });
  const failMut = useMutation({
    mutationFn: (reason: string) => deliveryService.fail(delivery!.id, reason),
    onSuccess: () => qc.invalidateQueries(),
  });

  const mutError = payMut.error || cancelMut.error || pickupMut.error || completeMut.error || failMut.error;

  if (orderQ.isLoading) return <div className="grid place-items-center py-24"><Loader2 className="h-8 w-8 animate-spin text-amber-glow" /></div>;
  if (!order) return <div className="glass p-12 text-center text-slate-500">Order not found. <Link to="/orders" className="text-amber-glow">Back</Link></div>;

  const timeline = buildTimeline(order, delivery, notifs);

  return (
    <div className="space-y-6">
      <button onClick={() => navigate('/orders')} className="flex items-center gap-2 text-sm text-slate-400 hover:text-amber-glow transition-colors">
        <ArrowLeft className="h-4 w-4" /> All orders
      </button>

      <AnimatePresence>
        {mutError && (
          <motion.div
            initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0 }}
            className="flex items-center gap-2 px-4 py-3 rounded-xl bg-rose/10 border border-rose/30 text-rose text-sm"
          >
            <XCircle className="h-5 w-5 shrink-0" />
            <span>{mutError instanceof Error ? mutError.message : 'Action failed'}</span>
          </motion.div>
        )}
      </AnimatePresence>

      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="font-display font-bold text-3xl text-white font-mono">{shortId(order.id)}</h1>
            <OrderStatusBadge status={order.status} />
          </div>
          <p className="text-slate-400 mt-1 text-sm">Created {formatDateTime(order.createdAt)} · {order.lines.length} line(s)</p>
        </div>
        <div className="flex gap-2">
          {canManageOrder && order.status === 'CREATED' && (
            <button onClick={() => payMut.mutate()} disabled={payMut.isPending} className="btn-primary text-sm">
              {payMut.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <><CreditCard className="h-4 w-4" /> Pay</>}
            </button>
          )}
          {canManageOrder && (order.status === 'CREATED' || order.status === 'PAID') && (
            <button onClick={() => cancelMut.mutate('Customer requested cancellation')} disabled={cancelMut.isPending} className="btn-ghost text-sm text-rose hover:border-rose/40 hover:text-rose">
              {cancelMut.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <XCircle className="h-4 w-4" />} Cancel
            </button>
          )}
        </div>
      </div>

      <div className="grid lg:grid-cols-3 gap-6">
        {/* Saga timeline */}
        <div className="lg:col-span-2 glass p-6">
          <h2 className="font-display font-semibold text-lg text-white mb-1">Saga timeline</h2>
          <p className="text-xs text-slate-500 mb-6">Event-driven state transitions</p>
          <Timeline entries={timeline} />
        </div>

        {/* Order summary */}
        <div className="space-y-6">
          <div className="glass p-6">
            <h2 className="font-display font-semibold text-lg text-white mb-4">Summary</h2>
            <div className="space-y-3">
              {order.lines.map((l, i) => (
                <div key={i} className="flex justify-between text-sm">
                  <span className="text-slate-300">{l.quantity}× {l.name}</span>
                  <span className="font-mono text-slate-400">{formatPrice(l.unitPriceCents * l.quantity, order.currency)}</span>
                </div>
              ))}
            </div>
            <div className="border-t border-white/[0.06] mt-4 pt-4 flex justify-between">
              <span className="font-display font-semibold text-white">Total</span>
              <span className="font-mono text-lg text-amber-glow">{formatPrice(order.totalCents, order.currency)}</span>
            </div>
            <div className="mt-5 space-y-2 text-sm">
              <div className="flex items-center gap-2 text-slate-400"><User className="h-4 w-4 text-slate-500" /> {order.shippingAddress.fullName}</div>
              <div className="flex items-center gap-2 text-slate-400"><MapPin className="h-4 w-4 text-slate-500" /> {order.shippingAddress.line1}, {order.shippingAddress.city}</div>
            </div>
          </div>

          {/* Delivery */}
          {delivery && (
            <div className="glass p-6">
              <div className="flex items-center justify-between mb-4">
                <h2 className="font-display font-semibold text-lg text-white">Delivery</h2>
                <DeliveryStatusBadge status={delivery.status} />
              </div>
              <div className="space-y-2 text-sm font-mono">
                <div className="flex justify-between"><span className="text-slate-500">Tracking</span><span className="text-cyan-live">{delivery.trackingNumber}</span></div>
                <div className="flex justify-between"><span className="text-slate-500">Courier</span><span className="text-slate-300">{delivery.courierId}</span></div>
              </div>
              {canManageDelivery && (
                <div className="flex flex-wrap gap-2 mt-4">
                  {delivery.status === 'SCHEDULED' && (
                    <button onClick={() => pickupMut.mutate()} disabled={pickupMut.isPending} className="btn-primary text-xs py-2">
                      {pickupMut.isPending ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : <><Truck className="h-3.5 w-3.5" /> Pickup</>}
                    </button>
                  )}
                  {delivery.status === 'IN_TRANSIT' && (
                    <button onClick={() => completeMut.mutate()} disabled={completeMut.isPending} className="btn-primary text-xs py-2">
                      {completeMut.isPending ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : <><CheckCircle2 className="h-3.5 w-3.5" /> Complete</>}
                    </button>
                  )}
                  {(delivery.status === 'SCHEDULED' || delivery.status === 'IN_TRANSIT') && (
                    <button onClick={() => failMut.mutate('Courier could not complete delivery')} disabled={failMut.isPending} className="btn-ghost text-xs py-2 text-rose hover:border-rose/40 hover:text-rose">
                      {failMut.isPending ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : <XCircle className="h-3.5 w-3.5" />} Fail
                    </button>
                  )}
                </div>
              )}
            </div>
          )}

          {/* Notifications */}
          {notifs.length > 0 && (
            <div className="glass p-6">
              <h2 className="font-display font-semibold text-lg text-white mb-4">Notifications</h2>
              <div className="space-y-2">
                {notifs.map((n) => (
                  <div key={n.id} className="flex items-center gap-2 text-xs">
                    <span className={`h-1.5 w-1.5 rounded-full ${n.status === 'SENT' ? 'bg-mint' : 'bg-rose'}`} />
                    <span className="font-mono text-slate-400">{n.template}</span>
                    <span className="text-slate-600 ml-auto">{n.channel}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function Timeline({ entries }: { entries: TimelineEntry[] }) {
  return (
    <div className="relative pl-8">
      <div className="absolute left-3 top-2 bottom-2 w-px bg-gradient-to-b from-amber-glow/40 via-cyan-live/20 to-transparent" />
      <AnimatePresence>
        {entries.map((h, i) => {
          const Icon = h.icon;
          return (
            <motion.div
              key={i}
              initial={{ opacity: 0, x: -16 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: i * 0.1, duration: 0.4 }}
              className="relative pb-6 last:pb-0"
            >
              <div className="absolute -left-[1.35rem] top-0.5 h-6 w-6 rounded-full grid place-items-center bg-ink-800 border border-amber/30">
                <Icon className="h-3.5 w-3.5 text-amber-glow" />
              </div>
              <div className="flex items-center gap-2 flex-wrap">
                <span className="font-display font-semibold text-white">{h.label}</span>
                <span className="chip border border-white/10 text-slate-400 bg-white/[0.03]">by {h.actor}</span>
              </div>
              <p className="text-sm text-slate-400 mt-0.5">{h.reason}</p>
              <p className="text-[11px] font-mono text-slate-600 mt-0.5">{formatDateTime(h.occurredAt)}</p>
            </motion.div>
          );
        })}
      </AnimatePresence>
    </div>
  );
}
