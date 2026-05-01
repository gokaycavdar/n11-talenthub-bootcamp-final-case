import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
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

export default function OrderDetailPage() {
    const { orderId } = useParams();
    const { withAuthorizedRequest } = useAuth();

    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let cancelled = false;

        async function loadOrder() {
            setLoading(true);
            setError("");

            try {
                const response = await withAuthorizedRequest((token) =>
                    api.getOrderById(token, orderId)
                );

                if (!cancelled) {
                    setOrder(response);
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

        loadOrder();

        return () => {
            cancelled = true;
        };
    }, [orderId, withAuthorizedRequest]);

    if (loading) {
        return (
            <section className="page-state">
                <div className="spinner" />
                <p>Siparis detayi yukleniyor...</p>
            </section>
        );
    }

    if (!order) {
        return (
            <EmptyState
                title="Siparis bulunamadi"
                description={error || "Istenen siparis kaydina ulasilamadi."}
                action={
                    <Link to="/orders" className="button button--primary">
                        Siparislere Don
                    </Link>
                }
            />
        );
    }

    return (
        <div className="page-stack">
            <nav className="breadcrumbs">
                <Link to="/orders">Siparislerim</Link>
                <span>/</span>
                <span>{order.orderNumber}</span>
            </nav>

            {error ? <div className="alert alert--error">{error}</div> : null}

            <section className="panel">
                <div className="detail-header">
                    <div>
                        <span className="eyebrow">Siparis Detayi</span>
                        <h1>{order.orderNumber}</h1>
                        <p>{formatDateTime(order.createdAt)}</p>
                    </div>

                    <StatusBadge
                        label={formatOrderStatus(order.status)}
                        tone={getOrderStatusTone(order.status)}
                    />
                </div>

                <div className="detail-grid">
                    <div className="detail-card">
                        <h2>Teslimat</h2>
                        <p>{order.shippingFullName}</p>
                        <p>{order.shippingAddressLine}</p>
                        <p>
                            {order.district} / {order.city}
                        </p>
                        <p>{order.postalCode}</p>
                    </div>

                    <div className="detail-card">
                        <h2>Odeme</h2>
                        <p>Conversation Id</p>
                        <strong>{order.paymentConversationId || "-"}</strong>
                        <p>Toplam</p>
                        <strong>{formatCurrency(order.totalAmount)}</strong>
                    </div>
                </div>
            </section>

            <section className="panel">
                <div className="panel__header">
                    <h2>Urunler</h2>
                </div>

                <div className="order-items">
                    {order.items.map((item) => (
                        <article key={item.productId} className="order-item">
                            <div>
                                <h3>{item.productName}</h3>
                                <p>
                                    {item.quantity} adet x {formatCurrency(item.unitPrice)}
                                </p>
                            </div>

                            <strong>{formatCurrency(item.lineTotal)}</strong>
                        </article>
                    ))}
                </div>
            </section>
        </div>
    );
}
