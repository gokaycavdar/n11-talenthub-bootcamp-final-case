import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import EmptyState from "../components/EmptyState";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { api, resolveImageUrl } from "../lib/api";
import { formatCurrency } from "../lib/format";

export default function ProductDetailPage() {
    const { productId } = useParams();
    const { isAuthenticated } = useAuth();
    const { addItem, loading: cartLoading } = useCart();

    const [product, setProduct] = useState(null);
    const [quantity, setQuantity] = useState(1);
    const [loading, setLoading] = useState(true);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        let cancelled = false;

        async function loadProduct() {
            setLoading(true);
            setError("");

            try {
                const response = await api.getProductById(productId);

                if (!cancelled) {
                    setProduct(response);
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

        loadProduct();

        return () => {
            cancelled = true;
        };
    }, [productId]);

    async function handleAddToCart() {
        if (!isAuthenticated) {
            setError("Sepete urun eklemek icin once giris yapmalisin.");
            return;
        }

        setBusy(true);
        setError("");

        try {
            await addItem(product.id, quantity);
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setBusy(false);
        }
    }

    function decreaseQuantity() {
        setQuantity((current) => Math.max(1, current - 1));
    }

    function increaseQuantity() {
        if (!product) {
            return;
        }

        setQuantity((current) => Math.min(product.stock ?? current + 1, current + 1));
    }

    if (loading) {
        return (
            <section className="page-state">
                <div className="spinner" />
                <p>Urun detayi yukleniyor...</p>
            </section>
        );
    }

    if (!product) {
        return (
            <EmptyState
                title="Urun bulunamadi"
                description={error || "Istenen urun bulunamadi veya artik aktif degil."}
                action={
                    <Link to="/" className="button button--primary">
                        Urunlere Don
                    </Link>
                }
            />
        );
    }

    return (
        <div className="page-stack">
            <nav className="breadcrumbs">
                <Link to="/">Urunler</Link>
                <span>/</span>
                <span>{product.name}</span>
            </nav>

            {error ? <div className="alert alert--error">{error}</div> : null}

            <section className="product-detail">
                <div className="product-detail__media">
                    <img src={resolveImageUrl(product.imageUrl)} alt={product.name} />
                </div>

                <div className="product-detail__content">
                    <span className="eyebrow">{product.category}</span>
                    <h1>{product.name}</h1>
                    <p className="product-detail__description">{product.description}</p>

                    <div className="product-detail__facts">
                        <div className="metric">
                            <span>Fiyat</span>
                            <strong>{formatCurrency(product.price)}</strong>
                        </div>
                        <div className="metric">
                            <span>Stok</span>
                            <strong>{product.stock}</strong>
                        </div>
                        <div className="metric">
                            <span>Durum</span>
                            <strong>{product.active ? "Aktif" : "Pasif"}</strong>
                        </div>
                    </div>

                    <div className="purchase-box">
                        <div className="input-stepper">
                            <button type="button" onClick={decreaseQuantity}>
                                -
                            </button>
                            <span>{quantity}</span>
                            <button type="button" onClick={increaseQuantity}>
                                +
                            </button>
                        </div>

                        <button
                            type="button"
                            className="button button--primary"
                            onClick={handleAddToCart}
                            disabled={cartLoading || busy}
                        >
                            {busy ? "Sepete ekleniyor..." : "Sepete Ekle"}
                        </button>
                    </div>

                    {!isAuthenticated ? (
                        <p className="muted-text">
                            Sepet ve siparis akisina devam etmek icin giris yapman gerekir.
                        </p>
                    ) : null}
                </div>
            </section>
        </div>
    );
}
