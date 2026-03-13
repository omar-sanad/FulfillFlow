import { motion } from 'framer-motion';
import { useQuery } from '@tanstack/react-query';
import {
  ShoppingCart, Truck, Package, Bell, TrendingUp, Activity, Clock,
} from 'lucide-react';
import { orderService, deliveryService, productService, notificationService } from '../lib/services';
import { OrderStatusBadge } from '../components/ui/StatusBadge';
import { relativeTime, shortId } from '../lib/utils';
import type { Order } from '../lib/types';

const stagger = {
  animate: { transition: { staggerChildren: 0.08 } },
};
const item = {
  initial: { opacity: 0, y: 16 },
  animate: { opacity: 1, y: 0, transition: { duration: 0.4, ease: 'easeOut' as const } },
};

function StatCard({
  icon: Icon, label, value, accent, sub,
}: { icon: React.ElementType; label: string; value: string | number; accent: string; sub?: string }) {
  return (
    <motion.div variants={item} className="glass p-5 relative overflow-hidden group">
      <div className={`absolute -top-8 -right-8 h-24 w-24 rounded-full blur-2xl ${accent} opacity-20 group-hover:opacity-30 transition-opacity`} />
      <div className="flex items-center justify-between">
        <div className={`h-10 w-10 rounded-xl grid place-items-center ${accent} bg-opacity-10`}>
          <Icon className="h-5 w-5" />
        </div>
        <TrendingUp className="h-4 w-4 text-mint/60" />
      </div>
      <div className="mt-4">
        <div className="font-display font-bold text-3xl text-white tabular-nums">{value}</div>
        <div className="text-sm text-slate-400 mt-0.5">{label}</div>
        {sub && <div className="text-[11px] font-mono text-slate-600 mt-1">{sub}</div>}
      </div>
    </motion.div>
  );
}

export function OverviewPage() {
  const ordersQ = useQuery({ queryKey: ['orders'], queryFn: orderService.list });
  const deliveriesQ = useQuery({ queryKey: ['deliveries'], queryFn: deliveryService.list });
  const productsQ = useQuery({ queryKey: ['products'], queryFn: productService.list });
  const notifsQ = useQuery({ queryKey: ['notifications'], queryFn: notificationService.list });

  const orders = ordersQ.data ?? [];
  const deliveries = deliveriesQ.data ?? [];
  const products = productsQ.data ?? [];
  const notifs = notifsQ.data ?? [];

  const fulfilled = orders.filter((o) => o.status === 'FULFILLED').length;
  const inTransit = deliveries.filter((d) => d.status === 'IN_TRANSIT').length;
  const recentOrders = [...orders].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()).slice(0, 6);

  return (
    <div className="space-y-8">
      <div>
        <motion.h1
          initial={{ opacity: 0, x: -10 }} animate={{ opacity: 1, x: 0 }}
          className="font-display font-bold text-3xl text-white"
        >
          Operations overview
        </motion.h1>
        <motion.p
          initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.1 }}
          className="text-slate-400 mt-1"
        >
          Real-time pulse of the fulfilment pipeline.
        </motion.p>
      </div>

      <motion.div variants={stagger} initial="initial" animate="animate" className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard icon={ShoppingCart} label="Total orders" value={orders.length} accent="text-amber-glow bg-amber-glow" sub={`${fulfilled} fulfilled`} />
        <StatCard icon={Truck} label="Deliveries" value={deliveries.length} accent="text-cyan-live bg-cyan-live" sub={`${inTransit} in transit`} />
        <StatCard icon={Package} label="Products" value={products.length} accent="text-mint bg-mint" sub="in catalogue" />
        <StatCard icon={Bell} label="Notifications" value={notifs.length} accent="text-amber-glow bg-amber-glow" sub="dispatched" />
      </motion.div>

      <div className="grid lg:grid-cols-3 gap-6">
        <motion.div
          initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}
          className="lg:col-span-2 glass p-6"
        >
          <div className="flex items-center justify-between mb-5">
            <div>
              <h2 className="font-display font-semibold text-lg text-white">Recent orders</h2>
              <p className="text-xs text-slate-500">Latest lifecycle events</p>
            </div>
            <Activity className="h-5 w-5 text-amber-glow/60" />
          </div>
          <div className="space-y-1">
            {recentOrders.length === 0 && (
              <div className="text-center py-10 text-slate-500 text-sm">No orders yet. Place one from the catalog.</div>
            )}
            {recentOrders.map((o: Order) => (
              <div key={o.id} className="flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-white/[0.03] transition-colors">
                <span className="font-mono text-xs text-amber-glow/80 w-20">{shortId(o.id)}</span>
                <OrderStatusBadge status={o.status} />
                <span className="text-sm text-slate-400 flex-1 truncate">
                  {o.lines.map((l) => `${l.quantity}× ${l.name}`).join(', ')}
                </span>
                <span className="flex items-center gap-1 text-[11px] font-mono text-slate-600 shrink-0">
                  <Clock className="h-3 w-3" />{relativeTime(o.createdAt)}
                </span>
              </div>
            ))}
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }}
          className="glass p-6"
        >
          <h2 className="font-display font-semibold text-lg text-white mb-1">Order funnel</h2>
          <p className="text-xs text-slate-500 mb-5">Distribution by status</p>
          <Funnel orders={orders} />
        </motion.div>
      </div>
    </div>
  );
}

function Funnel({ orders }: { orders: Order[] }) {
  const stages: { key: string; label: string; color: string }[] = [
    { key: 'CREATED', label: 'Created', color: 'from-cyan-live/40 to-cyan-live/10' },
    { key: 'PAID', label: 'Paid', color: 'from-amber-glow/40 to-amber-glow/10' },
    { key: 'FULFILLED', label: 'Fulfilled', color: 'from-mint/40 to-mint/10' },
    { key: 'FAILED', label: 'Failed', color: 'from-rose/40 to-rose/10' },
    { key: 'CANCELLED', label: 'Cancelled', color: 'from-slate-500/40 to-slate-500/10' },
  ];
  const max = Math.max(1, ...stages.map((s) => orders.filter((o) => o.status === s.key).length));
  return (
    <div className="space-y-3">
      {stages.map((s, i) => {
        const count = orders.filter((o) => o.status === s.key).length;
        return (
          <div key={s.key}>
            <div className="flex justify-between text-xs mb-1">
              <span className="text-slate-400">{s.label}</span>
              <span className="font-mono text-white tabular-nums">{count}</span>
            </div>
            <motion.div
              initial={{ width: 0 }}
              animate={{ width: `${Math.max(4, (count / max) * 100)}%` }}
              transition={{ duration: 0.7, delay: 0.4 + i * 0.1, ease: 'easeOut' }}
              className={`h-7 rounded-lg bg-gradient-to-r ${s.color} border border-white/5`}
            />
          </div>
        );
      })}
    </div>
  );
}
