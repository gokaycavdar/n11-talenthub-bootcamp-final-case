import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import ThreeDsRunner from "../components/ThreeDsRunner";
import StatusBadge from "../components/StatusBadge";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { api } from "../lib/api";
import {
    formatCurrency,
    formatOrderStatus,
    getOrderStatusTone,
} from "../lib/format";

function getSessionStorageKey(orderId) {
    return `ecommerce.checkoutSession.${orderId}`;
}

function readStoredSession(orderId) {
    const rawValue = sessionStorage.getItem(getSessionStorageKey(orderId));

    if (!rawValue) {
        return null;
    }

    try {
        return JSON.parse(rawValue);
    } catch {
        sessionStorage.removeItem(getSessionStorageKey(orderId));
        return null;
    }
}

export default function PaymentFlowPage() {
    const { orderId } = useParams();
    const location = useLocation();
    const { withAuthorizedRequest } = useAuth();
    const { clearCart } = useCart();

    const initialSession = useMemo(() => {
        return location.state?.session ?? readStoredSession(orderId);
    }, [location.state, orderId]);

    const [session] = useState(initialSession);
    const [order, setOrder] = useState(null);
    const [phase, setPhase] = useState("processing");
    const [error, setError] = useState("");

    const clearedCartRef = useRef(false);

    useEffect(() => {
        if (location.state?.session) {
            sessionStorage.setItem(
                getSessionStorageKey(orderId),
                JSON.stringify(location.state.session)
            );
        }
    }, [location.state, orderId]);

    useEffect(() => {
        let active = true;
        let intervalId;

        async function syncOrderStatus() {
            try {
                const response = await withAuthorizedRequest((token) =>
                    api.getOrderById(token, orderId)
                );

                if (!active) {
                    return;
                }

                setOrder(response);

                if (response.status === "PAID") {
                    setPhase("success");
                    sessionStorage.removeItem(getSessionStorageKey(orderId));

                    if (!clearedCartRef.current) {
                        clearedCartRef.current = true;
                        clearCart().catch(() => {});
                    }

                    return true;
                }

                if (response.status === "PAYMENT_FAILED") {
                    setPhase("failed");
                    sessionStorage.removeItem(getSessionStorageKey(orderId));
                    return true;
                }

                setPhase("processing");
                return false;
            } catch (requestError) {
                if (!active) {
                    return true;
                }

                setError(requestError.message);
                return false;
            }
        }

        syncOrderStatus().then((done) => {
            if (done || !active) {
                return;
            }

            intervalId = window.setInterval(async () => {
                const finished = await syncOrderStatus();
                if (finished && intervalId) {
                    window.clearInterval(intervalId);
                }
            }, 2000);
        });

        return () => {
            active = false;
            if (intervalId) {
                window.clearInterval(intervalId);
            }
        };
    }, [clearCart, orderId, withAuthorizedRequest]);

    if (!session && !order) {
        return (
            <section className="page-state">
                <p>Bu odeme oturumu bulunamadi.</p>
                <Link to="/orders" className="button button--primary">
                    Siparislerime Git
                </Link>
            </section>
        );
    }

    return (
        <div className="page-stack">
            <section className="section-heading">
                <div>
                    <span className="eyebrow">Odeme Akisi</span>
                    <h1>3DS odeme durumu izleniyor</h1>
                </div>
            </section>

            {error ? <div className="alert alert--error">{error}</div> : null}

            <section className="panel payment-status">
                <div className="payment-status__header">
                    <div>
                        <h2>
                            Siparis #{order?.orderNumber ?? session?.orderNumber ?? orderId}
                        </h2>
                        <p>
                            Backend tarafinda payment event'leri order ve notification
                            servislerine aktariliyor.
                        </p>
                    </div>

                    <StatusBadge
                        label={formatOrderStatus(order?.status ?? "PENDING_PAYMENT")}
                        tone={getOrderStatusTone(order?.status ?? "PENDING_PAYMENT")}
                    />
                </div>

                <div className="payment-status__meta">
                    <div className="metric">
                        <span>Tutar</span>
                        <strong>{formatCurrency(order?.totalAmount ?? session?.totalAmount)}</strong>
                    </div>

                    <div className="metric">
                        <span>Conversation Id</span>
                        <strong>{order?.paymentConversationId ?? session?.conversationId}</strong>
                    </div>
                </div>
            </section>

            {phase === "processing" && session?.threeDsHtmlContent ? (
                <ThreeDsRunner
                    htmlContent={session.threeDsHtmlContent}
                    title="Odeme istegi gonderiliyor"
                />
            ) : null}

            {phase === "success" ? (
                <section className="result-card result-card--success">
                    <h2>Odeme tamamlandi</h2>
                    <p>Siparisin basariyla olusturuldu. Artik detay ekranina gecebiliriz.</p>
                    <div className="result-card__actions">
                        <Link to={`/orders/${orderId}`} className="button button--primary">
                            Siparis Detayi
                        </Link>
                        <Link to="/orders" className="button button--ghost">
                            Siparislerim
                        </Link>
                    </div>
                </section>
            ) : null}

            {phase === "failed" ? (
                <section className="result-card result-card--danger">
                    <h2>Odeme basarisiz</h2>
                    <p>
                        Siparis kaydi korunuyor ama durum PAYMENT_FAILED oldu. Sepetindeki
                        urunler yeniden denenebilmesi icin kalacak.
                    </p>
                    <div className="result-card__actions">
                        <Link to="/cart" className="button button--primary">
                            Sepete Don
                        </Link>
                        <Link to={`/orders/${orderId}`} className="button button--ghost">
                            Siparis Detayi
                        </Link>
                    </div>
                </section>
            ) : null}
        </div>
    );
}
