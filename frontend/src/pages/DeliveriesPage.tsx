import { motion } from 'framer-motion';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Truck, Loader2, PackageCheck, XCircle, Link as LinkIcon } from 'lucide-react';
import { Link } from 'react-router-dom';
import { deliveryService, orderService } from '../lib/services';
import { DeliveryStatusBadge } from '../components/ui/StatusBadge';
import { formatDateTime, relativeTime, shortId } from '../lib/utils';
import type { Delivery } from '../lib/types';

export function DeliveriesPage() {
  const { data: deliveries, isLoading } = useQuery({ queryKey: ['deliveries'], queryFn: deliveryService.list, refetchInterval: 5000 });
  const qc = useQueryClient();
  const sorted = [...(deliveries ?? [])].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

  const pickup = useMutation({ mutationFn: (id: string) => deliveryService.pickup(id), onSuccess: () => qc.invalidateQueries() });
  const complete = useMutation({ mutationFn: (id: string) => deliveryService.complete(id), onSuccess: () => qc.invalidateQueries() });
  const fail = useMutation({ mutationFn: (id: string) => deliveryService.fail(id, 'Could not complete'), onSuccess: () => qc.invalidateQueries() });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display font-bold text-3xl text-white">Deliveries</h1>
        <p className="text-slate-400 mt-1">Track and progress parcels through the delivery pipeline.</p>
      </div>

      {isLoading ? (
        <div className="grid place-items-center py-20"><Loader2 className="h-8 w-8 animate-spin text-amber-glow" /></div>
      ) : sorted.length === 0 ? (
        <div className="glass p-12 text-center">
          <Truck className="h-10 w-10 text-slate-700 mx-auto mb-3" />
          <p className="text-slate-500">No deliveries yet. Pay an order to schedule one.</p>
        </div>
      ) : (
        <div className="grid md:grid-cols-2 gap-4">
          {sorted.map((d: Delivery, i) => (
            <motion.div
              key={d.id}
              initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }}
              className="glass p-5"
            >
              <div className="flex items-start justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <Truck className="h-4 w-4 text-cyan-live" />
                    <span className="font-mono text-sm text-cyan-live">{d.trackingNumber}</span>
                  </div>
                  <Link to={`/orders/${d.orderId}`} className="flex items-center gap-1 text-xs text-slate-500 hover:text-amber-glow mt-1.5">
                    <LinkIcon className="h-3 w-3" /> Order {shortId(d.orderId)}
                  </Link>
                </div>
                <DeliveryStatusBadge status={d.status} />
              </div>

              <div className="grid grid-cols-2 gap-3 mt-4 text-xs">
                <div>
                  <div className="text-slate-500 font-mono uppercase tracking-wider mb-0.5">Courier</div>
                  <div className="text-slate-300">{d.courierId}</div>
                </div>
                <div>
                  <div className="text-slate-500 font-mono uppercase tracking-wider mb-0.5">Scheduled</div>
                  <div className="text-slate-300">{relativeTime(d.scheduledAt)}</div>
                </div>
                {d.deliveredAt && <div><div className="text-slate-500 font-mono uppercase tracking-wider mb-0.5">Delivered</div><div className="text-mint">{formatDateTime(d.deliveredAt)}</div></div>}
                {d.failureReason && <div className="col-span-2"><div className="text-slate-500 font-mono uppercase tracking-wider mb-0.5">Reason</div><div className="text-rose text-sm">{d.failureReason}</div></div>}
              </div>

              {(d.status === 'SCHEDULED' || d.status === 'IN_TRANSIT') && (
                <div className="flex gap-2 mt-4">
                  {d.status === 'SCHEDULED' && (
                    <button onClick={() => pickup.mutate(d.id)} disabled={pickup.isPending} className="btn-ghost text-xs py-2 flex-1">
                      {pickup.isPending ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : <Truck className="h-3.5 w-3.5" />} Pickup
                    </button>
                  )}
                  {d.status === 'IN_TRANSIT' && (
                    <button onClick={() => complete.mutate(d.id)} disabled={complete.isPending} className="btn-primary text-xs py-2 flex-1">
                      {complete.isPending ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : <PackageCheck className="h-3.5 w-3.5" />} Complete
                    </button>
                  )}
                  <button onClick={() => fail.mutate(d.id)} disabled={fail.isPending} className="btn-ghost text-xs py-2 text-rose hover:border-rose/40 hover:text-rose">
                    {fail.isPending ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : <XCircle className="h-3.5 w-3.5" />}
                  </button>
                </div>
              )}
            </motion.div>
          ))}
        </div>
      )}
    </div>
  );
}
