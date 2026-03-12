import { motion } from 'framer-motion';
import { cn } from '../../lib/utils';
import type { OrderStatus, DeliveryStatus } from '../../lib/types';

const ORDER_STYLES: Record<OrderStatus, string> = {
  CREATED: 'bg-cyan-live/10 text-cyan-live border-cyan-live/30',
  PAID: 'bg-amber-glow/10 text-amber-glow border-amber/30',
  FULFILLED: 'bg-mint/10 text-mint border-mint/30',
  CANCELLED: 'bg-slate-500/10 text-slate-400 border-slate-500/30',
  FAILED: 'bg-rose/10 text-rose border-rose/30',
};

const DELIVERY_STYLES: Record<DeliveryStatus, string> = {
  SCHEDULED: 'bg-amber-glow/10 text-amber-glow border-amber/30',
  IN_TRANSIT: 'bg-cyan-live/10 text-cyan-live border-cyan-live/30',
  COMPLETED: 'bg-mint/10 text-mint border-mint/30',
  CANCELLED: 'bg-slate-500/10 text-slate-400 border-slate-500/30',
  FAILED: 'bg-rose/10 text-rose border-rose/30',
};

export function OrderStatusBadge({ status, animate = true }: { status: OrderStatus; animate?: boolean }) {
  const styles = ORDER_STYLES[status] ?? 'bg-slate-500/10 text-slate-400 border-slate-500/30';
  return (
    <span className={cn('chip border', styles)}>
      <span className="status-dot" style={{ backgroundColor: 'currentColor' }} />
      {status}
    </span>
  );
}

export function DeliveryStatusBadge({ status }: { status: DeliveryStatus }) {
  const styles = DELIVERY_STYLES[status] ?? 'bg-slate-500/10 text-slate-400 border-slate-500/30';
  return (
    <span className={cn('chip border', styles)}>
      {status === 'IN_TRANSIT' && (
        <motion.span
          animate={{ opacity: [1, 0.3, 1] }}
          transition={{ duration: 1.2, repeat: Infinity }}
          className="h-1.5 w-1.5 rounded-full bg-cyan-live"
        />
      )}
      {status}
    </span>
  );
}
