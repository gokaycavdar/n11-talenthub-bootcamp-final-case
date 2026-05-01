import { useEffect, useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import EmptyState from "../components/EmptyState";
import Pagination from "../components/Pagination";
import { api, resolveImageUrl } from "../lib/api";
import { formatCurrency } from "../lib/format";
import { useCart } from "../context/CartContext";
import { useAuth } from "../context/AuthContext";

const PAGE_SIZE = 8;

export default function HomePage() {
    const [searchParams, setSearchParams] = useSearchParams();
    const { addItem, loading: cartLoading } = useCart();
    const { isAuthenticated } = useAuth();

    const [productsPage, setProductsPage] = useState({
        content: [],
        page: 0,
        size: PAGE_SIZE,
        totalElements: 0,
        totalPages: 0,
        last: true,
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [busyProductId, setBusyProductId] = useState(null);

    const page = useMemo(() => {
        const rawValue = Number(searchParams.get("page") ?? "0");
        return Number.isNaN(rawValue) || rawValue < 0 ? 0 : rawValue;
    }, [searchParams]);

    useEffect(() => {
        let cancelled = false;

        async function loadProducts() {
            setLoading(true);
            setError("");

            try {
                const response = await api.getProducts({
                    page,
                    size: PAGE_SIZE,
                    sort: "id",
                });

                if (!cancelled) {
                    setProductsPage(response);
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

        loadProducts();

        return () => {
            cancelled = true;
        };
    }, [page]);

    async function handleAddToCart(productId) {
        if (!isAuthenticated) {
            setError("Sepete urun eklemek icin once giris yapmalisin.");
            return;
        }

        setBusyProductId(productId);
        setError("");

        try {
            await addItem(productId, 1);
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setBusyProductId(null);
        }
    }

    function handlePageChange(nextPage) {
        setSearchParams({ page: String(nextPage) });
    }

    return (
        <div className="page-stack">
            <section className="hero">
                <div>
                    <span className="eyebrow">n11 TalentHub Final Project</span>
                    <h1>Urunleri incele, sepete ekle, siparisini tamamla.</h1>
                    <p>
                        Spring Boot mikroservisleri ve React istemcisi uzerinden calisan
                        tam akisin on yuz tarafini burada topluyoruz.
                    </p>
                </div>

                <div className="hero__stats">
                    <div className="hero__stat">
                        <strong>{productsPage.totalElements}</strong>
                        <span>Toplam urun</span>
                    </div>
                    <div className="hero__stat">
                        <strong>{productsPage.totalPages}</strong>
                        <span>Sayfa</span>
                    </div>
                    <div className="hero__stat">
                        <strong>JWT</strong>
                        <span>Guvenli akis</span>
                    </div>
                </div>
            </section>

            {error ? <div className="alert alert--error">{error}</div> : null}

            {loading ? (
                <section className="page-state">
                    <div className="spinner" />
                    <p>Urunler yukleniyor...</p>
                </section>
            ) : productsPage.content.length === 0 ? (
                <EmptyState
                    title="Gosterilecek urun yok"
                    description="Veritabaninda aktif urun bulunamadi."
                />
            ) : (
                <>
                    <section className="product-grid">
                        {productsPage.content.map((product) => (
                            <article key={product.id} className="product-card">
                                <Link
                                    to={`/products/${product.id}`}
                                    className="product-card__media"
                                >
                                    <img
                                        src={resolveImageUrl(product.imageUrl)}
                                        alt={product.name}
                                    />
                                </Link>

                                <div className="product-card__body">
                                    <div className="product-card__meta">
                                        <span>{product.category}</span>
                                        <span>Stok: {product.stock}</span>
                                    </div>

                                    <Link to={`/products/${product.id}`} className="product-card__title">
                                        {product.name}
                                    </Link>

                                    <p className="product-card__description">
                                        {product.description}
                                    </p>

                                    <div className="product-card__footer">
                                        <strong>{formatCurrency(product.price)}</strong>
                                        <button
                                            type="button"
                                            className="button button--primary"
                                            onClick={() => handleAddToCart(product.id)}
                                            disabled={cartLoading || busyProductId === product.id}
                                        >
                                            {busyProductId === product.id ? "Ekleniyor..." : "Sepete Ekle"}
                                        </button>
                                    </div>
                                </div>
                            </article>
                        ))}
                    </section>

                    <Pagination
                        page={productsPage.page}
                        totalPages={productsPage.totalPages}
                        onChange={handlePageChange}
                    />
                </>
            )}
        </div>
    );
}
