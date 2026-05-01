import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import EmptyState from "../components/EmptyState";
import StatusBadge from "../components/StatusBadge";
import { useAuth } from "../context/AuthContext";
import { api } from "../lib/api";
import {
    formatCurrency,
    formatDateTime,
    formatOrderStatus,
    getOrderStatusTone,
} from "../lib/format";

export default function OrdersPage() {
    const { withAuthorizedRequest } = useAuth();

    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let cancelled = false;

        async function loadOrders() {
            setLoading(true);
            setError("");

            try {
                const response = await withAuthorizedRequest((token) =>
                    api.getMyOrders(token)
                );

                if (!cancelled) {
                    setOrders(response);
                }
            } catch (requestError) {
                if (!cancelled) {
                    setError(requestError.message);
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }

        loadOrders();

        return () => {
            cancelled = true;
        };
    }, [withAuthorizedRequest]);

    if (loading) {
        return (
            <section className="page-state">
                <div className="spinner" />
                <p>Siparisler yukleniyor...</p>
            </section>
        );
    }

    if (!orders.length) {
        return (
            <EmptyState
                title="Henuz siparis yok"
                description="Checkout sonrasi olusan siparisler burada listelenecek."
                action={
                    <Link to="/" className="button button--primary">
                        Alisverise Basla
                    </Link>
                }
            />
        );
    }

    return (
        <div className="page-stack">
            <section className="section-heading">
                <div>
                    <span className="eyebrow">Siparisler</span>
                    <h1>Gecmis siparislerin</h1>
                </div>
            </section>

            {error ? <div className="alert alert--error">{error}</div> : null}

            <section className="orders-grid">
                {orders.map((order) => (
                    <article key={order.id} className="order-card">
                        <div className="order-card__header">
                            <div>
                                <h2>{order.orderNumber}</h2>
                                <p>{formatDateTime(order.createdAt)}</p>
                            </div>

                            <StatusBadge
                                label={formatOrderStatus(order.status)}
                                tone={getOrderStatusTone(order.status)}
                            />
                        </div>

                        <div className="order-card__body">
                            <div className="order-card__row">
                                <span>Tutar</span>
                                <strong>{formatCurrency(order.totalAmount)}</strong>
                            </div>

                            <div className="order-card__row">
                                <span>Teslimat</span>
                                <strong>{order.shippingFullName}</strong>
                            </div>

                            <div className="order-card__row">
                                <span>Adres</span>
                                <strong>
                                    {order.district}, {order.city}
                                </strong>
                            </div>
                        </div>

                        <Link to={`/orders/${order.id}`} className="button button--ghost">
                            Detayi Gor
                        </Link>
                    </article>
                ))}
            </section>
        </div>
    );
}
