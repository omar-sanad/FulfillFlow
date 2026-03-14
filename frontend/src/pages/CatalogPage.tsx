import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Package, Plus, Minus, ShoppingBag, Loader2, Check, X, PackagePlus } from 'lucide-react';
import { productService, orderService } from '../lib/services';
import { useAuth } from '../context/AuthContext';
import { formatPrice, cn } from '../lib/utils';
import type { Product } from '../lib/types';

export function CatalogPage() {
  const productsQ = useQuery({ queryKey: ['products'], queryFn: productService.list });
  const products = productsQ.data ?? [];
  const [cart, setCart] = useState<Record<string, number>>({});
  const [placed, setPlaced] = useState<string | null>(null);
  const [showCreate, setShowCreate] = useState(false);
  const [restockId, setRestockId] = useState<string | null>(null);

  const { hasAnyRole } = useAuth();
  const canManage = hasAnyRole('administrator', 'warehouse');
  const canOrder = hasAnyRole('customer', 'administrator');

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

  const createProduct = useMutation({
    mutationFn: (data: { sku: string; name: string; description: string; priceCents: number; currency: string; weightGrams: number }) =>
      productService.create(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['products'] });
      setShowCreate(false);
    },
  });

  const restock = useMutation({
    mutationFn: ({ id, qty }: { id: string; qty: number }) => productService.restock(id, qty),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['products'] });
      setRestockId(null);
    },
  });

  return (
    <div className="space-y-6">
      <div className="flex items-end justify-between gap-4 flex-wrap">
        <div>
          <h1 className="font-display font-bold text-3xl text-white">Catalog</h1>
          <p className="text-slate-400 mt-1">{canManage ? 'Manage products and stock levels.' : 'Browse products and place an order.'}</p>
        </div>
        <div className="flex items-center gap-3">
          {canManage && (
            <button onClick={() => setShowCreate(true)} className="btn-primary text-sm py-2 px-4">
              <PackagePlus className="h-4 w-4" /> Add product
            </button>
          )}
          {canOrder && totalItems > 0 && (
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
        <div className="glass p-12 text-center text-slate-500">
          <Package className="h-10 w-10 text-slate-700 mx-auto mb-3" />
          <p>No products available.</p>
          {canManage && <p className="text-sm mt-1">Click <b className="text-amber-glow">Add product</b> to create one.</p>}
        </div>
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
                  {canOrder ? (
                    qty > 0 ? (
                      <div className="flex items-center gap-1 bg-ink-700/60 rounded-xl p-1">
                        <button onClick={() => remove(p.id)} className="h-8 w-8 grid place-items-center rounded-lg hover:bg-white/10 text-slate-300"><Minus className="h-4 w-4" /></button>
                        <span className="w-8 text-center font-mono text-white tabular-nums">{qty}</span>
                        <button onClick={() => add(p.id)} className="h-8 w-8 grid place-items-center rounded-lg hover:bg-white/10 text-amber-glow"><Plus className="h-4 w-4" /></button>
                      </div>
                    ) : (
                      <span className="text-xs text-slate-600">{p.reservedQuantity} reserved</span>
                    )
                  ) : (
                    <span className="text-xs text-slate-600">{p.reservedQuantity} reserved</span>
                  )}
                  <div className="flex items-center gap-2">
                    {canManage && (
                      <button
                        onClick={() => setRestockId(p.id)}
                        className="btn-ghost text-xs py-2 px-2.5"
                        title="Restock"
                      >
                        <PackagePlus className="h-4 w-4" />
                      </button>
                    )}
                    {canOrder && (
                      <button
                        onClick={() => add(p.id)}
                        disabled={p.availableQuantity === 0}
                        className="btn-ghost text-sm py-2 px-3 disabled:opacity-40"
                      >
                        <Plus className="h-4 w-4" /> Add
                      </button>
                    )}
                  </div>
                </div>
              </motion.div>
            );
          })}
        </div>
      )}

      <AnimatePresence>
        {showCreate && (
          <CreateProductModal
            onClose={() => setShowCreate(false)}
            onSubmit={(d) => createProduct.mutate(d)}
            isPending={createProduct.isPending}
            error={createProduct.error instanceof Error ? createProduct.error.message : undefined}
          />
        )}
      </AnimatePresence>

      <AnimatePresence>
        {restockId && (
          <RestockModal
            productName={products.find((p) => p.id === restockId)?.name ?? 'Product'}
            onClose={() => setRestockId(null)}
            onSubmit={(qty) => restock.mutate({ id: restockId, qty })}
            isPending={restock.isPending}
            error={restock.error instanceof Error ? restock.error.message : undefined}
          />
        )}
      </AnimatePresence>
    </div>
  );
}

function ModalShell({ title, onClose, children }: { title: string; onClose: () => void; children: React.ReactNode }) {
  return (
    <motion.div
      initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
      className="fixed inset-0 z-50 grid place-items-center bg-ink-950/70 backdrop-blur-sm p-4"
      onClick={onClose}
    >
      <motion.div
        initial={{ scale: 0.95, opacity: 0, y: 10 }} animate={{ scale: 1, opacity: 1, y: 0 }} exit={{ scale: 0.95, opacity: 0 }}
        className="glass-strong w-full max-w-md p-6"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-5">
          <h2 className="font-display font-bold text-xl text-white">{title}</h2>
          <button onClick={onClose} className="text-slate-500 hover:text-white"><X className="h-5 w-5" /></button>
        </div>
        {children}
      </motion.div>
    </motion.div>
  );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <label className="block">
      <span className="text-xs font-mono text-slate-400 uppercase tracking-wider">{label}</span>
      <div className="mt-1">{children}</div>
    </label>
  );
}

const inputCls = 'w-full bg-ink-800/60 border border-white/10 rounded-xl px-3 py-2 text-sm text-white placeholder:text-slate-600 focus:outline-none focus:border-amber/40 transition-colors';

function CreateProductModal({ onClose, onSubmit, isPending, error }: {
  onClose: () => void;
  onSubmit: (d: { sku: string; name: string; description: string; priceCents: number; currency: string; weightGrams: number }) => void;
  isPending: boolean;
  error?: string;
}) {
  const [sku, setSku] = useState('');
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [priceCents, setPriceCents] = useState('');
  const [currency, setCurrency] = useState('USD');
  const [weightGrams, setWeightGrams] = useState('');

  function submit(e: React.FormEvent) {
    e.preventDefault();
    onSubmit({
      sku, name, description,
      priceCents: Math.round(parseFloat(priceCents) * 100),
      currency: currency.toUpperCase(),
      weightGrams: weightGrams ? parseInt(weightGrams, 10) : 0,
    });
  }

  return (
    <ModalShell title="New product" onClose={onClose}>
      <form onSubmit={submit} className="space-y-4">
        <Field label="SKU"><input className={inputCls} value={sku} onChange={(e) => setSku(e.target.value)} placeholder="WIDGET-01" required /></Field>
        <Field label="Name"><input className={inputCls} value={name} onChange={(e) => setName(e.target.value)} placeholder="Saga Widget" required /></Field>
        <Field label="Description"><textarea className={inputCls} rows={2} value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Short product description" /></Field>
        <div className="grid grid-cols-3 gap-3">
          <Field label="Price"><input type="number" step="0.01" min="0" className={inputCls} value={priceCents} onChange={(e) => setPriceCents(e.target.value)} placeholder="50.00" required /></Field>
          <Field label="Currency"><input className={inputCls} value={currency} onChange={(e) => setCurrency(e.target.value)} maxLength={3} required /></Field>
          <Field label="Weight (g)"><input type="number" min="0" className={inputCls} value={weightGrams} onChange={(e) => setWeightGrams(e.target.value)} placeholder="250" /></Field>
        </div>
        {error && <p className="text-sm text-rose">{error}</p>}
        <div className="flex justify-end gap-2 pt-1">
          <button type="button" onClick={onClose} className="btn-ghost text-sm py-2 px-4">Cancel</button>
          <button type="submit" disabled={isPending} className="btn-primary text-sm py-2 px-4">
            {isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : 'Create product'}
          </button>
        </div>
      </form>
    </ModalShell>
  );
}

function RestockModal({ productName, onClose, onSubmit, isPending, error }: {
  productName: string;
  onClose: () => void;
  onSubmit: (qty: number) => void;
  isPending: boolean;
  error?: string;
}) {
  const [qty, setQty] = useState('50');

  function submit(e: React.FormEvent) {
    e.preventDefault();
    onSubmit(parseInt(qty, 10));
  }

  return (
    <ModalShell title={`Restock · ${productName}`} onClose={onClose}>
      <form onSubmit={submit} className="space-y-4">
        <Field label="Quantity to add">
          <input type="number" min="1" className={inputCls} value={qty} onChange={(e) => setQty(e.target.value)} required />
        </Field>
        {error && <p className="text-sm text-rose">{error}</p>}
        <div className="flex justify-end gap-2 pt-1">
          <button type="button" onClick={onClose} className="btn-ghost text-sm py-2 px-4">Cancel</button>
          <button type="submit" disabled={isPending} className="btn-primary text-sm py-2 px-4">
            {isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : 'Restock'}
          </button>
        </div>
      </form>
    </ModalShell>
  );
}
