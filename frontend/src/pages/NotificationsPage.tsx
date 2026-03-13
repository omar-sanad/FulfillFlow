import { motion } from 'framer-motion';
import { useQuery } from '@tanstack/react-query';
import { Bell, Mail, MessageSquare, Loader2, CheckCircle2, XCircle } from 'lucide-react';
import { notificationService } from '../lib/services';
import { formatDateTime, relativeTime, shortId } from '../lib/utils';
import { cn } from '../lib/utils';
import type { Notification } from '../lib/types';

const CHANNEL_ICON: Record<string, React.ElementType> = { EMAIL: Mail, SMS: MessageSquare, PUSH: Bell };

export function NotificationsPage() {
  const { data: notifs, isLoading } = useQuery({ queryKey: ['notifications'], queryFn: notificationService.list, refetchInterval: 6000 });
  const sorted = [...(notifs ?? [])].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display font-bold text-3xl text-white">Notifications</h1>
        <p className="text-slate-400 mt-1">Customer communications dispatched by the notification service.</p>
      </div>

      {isLoading ? (
        <div className="grid place-items-center py-20"><Loader2 className="h-8 w-8 animate-spin text-amber-glow" /></div>
      ) : sorted.length === 0 ? (
        <div className="glass p-12 text-center">
          <Bell className="h-10 w-10 text-slate-700 mx-auto mb-3" />
          <p className="text-slate-500">No notifications dispatched yet.</p>
        </div>
      ) : (
        <div className="space-y-2.5">
          {sorted.map((n: Notification, i) => {
            const Icon = CHANNEL_ICON[n.channel] ?? Bell;
            return (
              <motion.div
                key={n.id}
                initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.03 }}
                className="glass p-4 flex items-start gap-4"
              >
                <div className={cn(
                  'h-10 w-10 rounded-xl grid place-items-center shrink-0',
                  n.channel === 'EMAIL' ? 'bg-amber/10 text-amber-glow' : 'bg-cyan-live/10 text-cyan-live',
                )}>
                  <Icon className="h-5 w-5" />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="font-mono text-xs text-slate-400">{n.template}</span>
                    {n.orderId && <span className="text-[11px] font-mono text-amber-glow/70">order {shortId(n.orderId)}</span>}
                    <span className={cn('chip border ml-auto', n.status === 'SENT' ? 'border-mint/30 text-mint bg-mint/10' : 'border-rose/30 text-rose bg-rose/10')}>
                      {n.status === 'SENT' ? <CheckCircle2 className="h-3 w-3" /> : <XCircle className="h-3 w-3" />}
                      {n.status}
                    </span>
                  </div>
                  <div className="text-sm text-white mt-1 font-medium">{n.subject}</div>
                  <div className="flex items-center gap-3 mt-1.5 text-[11px] font-mono text-slate-600">
                    <span>to {n.recipient}</span>
                    <span>·</span>
                    <span>{relativeTime(n.createdAt)}</span>
                    <span>·</span>
                    <span>{formatDateTime(n.createdAt)}</span>
                  </div>
                </div>
              </motion.div>
            );
          })}
        </div>
      )}
    </div>
  );
}
