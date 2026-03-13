import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Package, Plus, Minus, ShoppingBag, Loader2, Check, X } from 'lucide-react';
import { productService, orderService } from '../lib/services';
import { useAuth } from '../context/AuthContext';
import { formatPrice, cn } from '../lib/utils';
import type { Product } from '../lib/types';

export function CatalogPage() {
  const productsQ = useQuery({ queryKey: ['products'], queryFn: productService.list });
  const products = productsQ.data ?? [];
  const [cart, setCart] = useState<Record<string, number>>({});
  const [placed, setPlaced] = useState<string | null>(null);

  function add(id: string) { setCart((c) => ({ ...c, [id]: (c[id] ?? 0) + 1 })); }
  function remove(id: string) { setCart((c) => { const n = (c[id] ?? 0) - 1; const nc = { ...c }; if (n <= 0) delete nc[id]; else nc[id] = n; return nc; }); }

  const totalItems = Object.values(cart).reduce((a, b) => a + b, 0);
  const totalCents = products
    .filter((p) => cart[p.id])
    .reduce((sum, p) => sum + (cart[p.id] ?? 0) * p.priceCents, 0);

  const qc = useQueryClient();
  const navigate = useNavigate();
  const { user } = useAuth();
  const createOrder = useMutation({
    mutationFn: () => {
      const lines = products
        .filter((p) => cart[p.id])
        .map((p) => ({ productId: p.id, sku: p.sku, name: p.name, unitPriceCents: p.priceCents, quantity: cart[p.id] }));
      return orderService.create({
        lines,
        shippingAddress: { fullName: user?.name ?? 'Customer', line1: '1 Main St', city: 'Cairo', postalCode: '11511', country: 'EG' },
      });
    },
    onSuccess: (order) => {
      qc.invalidateQueries({ queryKey: ['orders'] });
      setCart({});
      setPlaced(order.id);
      setTimeout(() => navigate(`/orders/${order.id}`), 1200);
    },
  });

  return (
    <div className="space-y-6">
      <div className="flex items-end justify-between gap-4 flex-wrap">
        <div>
          <h1 className="font-display font-bold text-3xl text-white">Catalog</h1>
          <p className="text-slate-400 mt-1">Browse products and place an order.</p>
        </div>
        {totalItems > 0 && (
          <motion.div
            initial={{ scale: 0.9, opacity: 0 }} animate={{ scale: 1, opacity: 1 }}
            className="flex items-center gap-3 glass-strong px-4 py-2.5"
          >
            <ShoppingBag className="h-5 w-5 text-amber-glow" />
            <span className="text-sm text-slate-300"><b className="text-white">{totalItems}</b> items</span>
            <span className="font-mono text-amber-glow">{formatPrice(totalCents)}</span>
            <button
              onClick={() => createOrder.mutate()}
              disabled={createOrder.isPending}
              className="btn-primary text-sm py-1.5 px-3"
            >
              {createOrder.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <>Checkout</>}
            </button>
          </motion.div>
        )}
      </div>

      <AnimatePresence>
        {placed && (
          <motion.div
            initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0 }}
            className="flex items-center gap-2 px-4 py-3 rounded-xl bg-mint/10 border border-mint/30 text-mint"
          >
            <Check className="h-5 w-5" /> Order placed — redirecting to tracking…
          </motion.div>
        )}
      </AnimatePresence>

      {productsQ.isLoading ? (
        <div className="grid place-items-center py-20"><Loader2 className="h-8 w-8 animate-spin text-amber-glow" /></div>
      ) : products.length === 0 ? (
        <div className="glass p-12 text-center text-slate-500">No products available.</div>
      ) : (
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {products.map((p: Product, i) => {
            const qty = cart[p.id] ?? 0;
            const lowStock = p.availableQuantity <= 10;
            return (
              <motion.div
                key={p.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.06 }}
                whileHover={{ y: -4 }}
                className="glass p-5 flex flex-col"
              >
                <div className="flex items-start justify-between">
                  <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-amber/20 to-cyan-live/10 grid place-items-center">
                    <Package className="h-6 w-6 text-amber-glow" />
                  </div>
                  <span className={cn('chip border', lowStock ? 'border-rose/30 text-rose bg-rose/10' : 'border-mint/30 text-mint bg-mint/10')}>
                    {p.availableQuantity} in stock
                  </span>
                </div>
                <h3 className="mt-4 font-display font-semibold text-lg text-white leading-tight">{p.name}</h3>
                <p className="text-sm text-slate-500 mt-1 line-clamp-2 flex-1">{p.description}</p>
                <div className="flex items-center justify-between mt-4">
                  <span className="font-mono text-amber-glow font-semibold">{formatPrice(p.priceCents, p.currency)}</span>
                  <span className="text-[11px] font-mono text-slate-600">SKU {p.sku}</span>
                </div>
                <div className="mt-4 flex items-center justify-between">
                  {qty > 0 ? (
                    <div className="flex items-center gap-1 bg-ink-700/60 rounded-xl p-1">
                      <button onClick={() => remove(p.id)} className="h-8 w-8 grid place-items-center rounded-lg hover:bg-white/10 text-slate-300"><Minus className="h-4 w-4" /></button>
                      <span className="w-8 text-center font-mono text-white tabular-nums">{qty}</span>
                      <button onClick={() => add(p.id)} className="h-8 w-8 grid place-items-center rounded-lg hover:bg-white/10 text-amber-glow"><Plus className="h-4 w-4" /></button>
                    </div>
                  ) : (
                    <span className="text-xs text-slate-600">{p.reservedQuantity} reserved</span>
                  )}
                  <button
                    onClick={() => add(p.id)}
                    disabled={p.availableQuantity === 0}
                    className="btn-ghost text-sm py-2 px-3 disabled:opacity-40"
                  >
                    <Plus className="h-4 w-4" /> Add
                  </button>
                </div>
              </motion.div>
            );
          })}
        </div>
      )}
    </div>
  );
}
