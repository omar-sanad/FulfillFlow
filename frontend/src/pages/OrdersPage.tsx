import { motion } from 'framer-motion';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { ShoppingCart, Clock } from 'lucide-react';
import { orderService } from '../lib/services';
import { OrderStatusBadge } from '../components/ui/StatusBadge';
import { formatPrice, relativeTime, shortId } from '../lib/utils';
import type { Order } from '../lib/types';

export function OrdersPage() {
  const { data: orders, isLoading } = useQuery({ queryKey: ['orders'], queryFn: orderService.list });
  const sorted = [...(orders ?? [])].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display font-bold text-3xl text-white">Orders</h1>
        <p className="text-slate-400 mt-1">All orders across the system.</p>
      </div>

      {isLoading ? (
        <div className="glass p-12 text-center text-slate-500">Loading orders…</div>
      ) : sorted.length === 0 ? (
        <div className="glass p-12 text-center">
          <ShoppingCart className="h-10 w-10 text-slate-700 mx-auto mb-3" />
          <p className="text-slate-500">No orders yet.</p>
          <Link to="/catalog" className="btn-primary mt-4 inline-flex">Browse catalog</Link>
        </div>
      ) : (
        <div className="glass overflow-hidden">
          <div className="grid grid-cols-[120px_1fr_140px_120px_120px] px-5 py-3 text-[11px] font-mono uppercase tracking-wider text-slate-500 border-b border-white/[0.06]">
            <span>Order</span>
            <span>Items</span>
            <span>Status</span>
            <span className="text-right">Total</span>
            <span className="text-right">Created</span>
          </div>
          {sorted.map((o: Order, i) => (
            <motion.div
              key={o.id}
              initial={{ opacity: 0, x: -10 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: i * 0.03 }}
            >
              <Link
                to={`/orders/${o.id}`}
                className="grid grid-cols-[120px_1fr_140px_120px_120px] px-5 py-3.5 items-center border-b border-white/[0.04] hover:bg-white/[0.02] transition-colors group"
              >
                <span className="font-mono text-xs text-amber-glow/80 group-hover:text-amber-glow">{shortId(o.id)}</span>
                <span className="text-sm text-slate-300 truncate pr-3">{o.lines.map((l) => `${l.quantity}× ${l.name}`).join(', ')}</span>
                <span><OrderStatusBadge status={o.status} /></span>
                <span className="text-right font-mono text-white">{formatPrice(o.totalCents, o.currency)}</span>
                <span className="text-right flex items-center justify-end gap-1 text-[11px] font-mono text-slate-600">
                  <Clock className="h-3 w-3" />{relativeTime(o.createdAt)}
                </span>
              </Link>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  );
}
