import { Link } from "react-router-dom";
import EmptyState from "../components/EmptyState";
import { useCart } from "../context/CartContext";
import { formatCurrency } from "../lib/format";
import { resolveImageUrl } from "../lib/api";

export default function CartPage() {
    const {
        cart,
        loading,
        error,
        hasItems,
        updateItemQuantity,
        removeItem,
        clearCart,
    } = useCart();

    async function handleDecrease(item) {
        if (item.quantity <= 1) {
            await removeItem(item.productId);
            return;
        }

        await updateItemQuantity(item.productId, item.quantity - 1);
    }

    async function handleIncrease(item) {
        await updateItemQuantity(item.productId, item.quantity + 1);
    }

    if (!hasItems) {
        return (
            <EmptyState
                title="Sepetin bos"
                description="Urun ekledikten sonra burada siparis oncesi son kontrolu yapabilirsin."
                action={
                    <Link to="/" className="button button--primary">
                        Alisverise Don
                    </Link>
                }
            />
        );
    }

    return (
        <div className="page-stack">
            <section className="section-heading">
                <div>
                    <span className="eyebrow">Sepet</span>
                    <h1>Sepetindeki urunler</h1>
                </div>
            </section>

            {error ? <div className="alert alert--error">{error}</div> : null}

            <div className="cart-layout">
                <section className="cart-list">
                    {cart.items.map((item) => (
                        <article key={item.productId} className="cart-item">
                            <img
                                className="cart-item__image"
                                src={resolveImageUrl(item.imageUrl)}
                                alt={item.productName}
                            />

                            <div className="cart-item__content">
                                <h2>{item.productName}</h2>
                                <p>Birim fiyat: {formatCurrency(item.unitPrice)}</p>
                                <strong>{formatCurrency(item.lineTotal)}</strong>
                            </div>

                            <div className="cart-item__actions">
                                <div className="input-stepper">
                                    <button
                                        type="button"
                                        onClick={() => handleDecrease(item)}
                                        disabled={loading}
                                    >
                                        -
                                    </button>
                                    <span>{item.quantity}</span>
                                    <button
                                        type="button"
                                        onClick={() => handleIncrease(item)}
                                        disabled={loading}
                                    >
                                        +
                                    </button>
                                </div>

                                <button
                                    type="button"
                                    className="button button--ghost"
                                    onClick={() => removeItem(item.productId)}
                                    disabled={loading}
                                >
                                    Kaldir
                                </button>
                            </div>
                        </article>
                    ))}
                </section>

                <aside className="summary-card">
                    <span className="eyebrow">Ozet</span>
                    <h2>Siparis ozeti</h2>

                    <div className="summary-card__row">
                        <span>Urun sayisi</span>
                        <strong>{cart.items.length}</strong>
                    </div>

                    <div className="summary-card__row">
                        <span>Toplam</span>
                        <strong>{formatCurrency(cart.totalPrice)}</strong>
                    </div>

                    <div className="summary-card__actions">
                        <Link to="/checkout" className="button button--primary button--block">
                            Checkout
                        </Link>

                        <button
                            type="button"
                            className="button button--ghost button--block"
                            onClick={() => clearCart()}
                            disabled={loading}
                        >
                            Sepeti Temizle
                        </button>
                    </div>
                </aside>
            </div>
        </div>
    );
}
