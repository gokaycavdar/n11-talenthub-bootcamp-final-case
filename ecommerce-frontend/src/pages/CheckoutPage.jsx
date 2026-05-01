import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import EmptyState from "../components/EmptyState";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { api } from "../lib/api";
import { formatCurrency } from "../lib/format";

function buildInitialForm(fullName = "") {
    return {
        shippingFullName: fullName,
        shippingAddressLine: "Ataturk Caddesi No 10",
        city: "Istanbul",
        district: "Kadikoy",
        postalCode: "34710",
        cardHolder: fullName || "Gokay Cavdar",
        cardNumber: "5555444433331111",
        expireMonth: "12",
        expireYear: "30",
        cvc: "123",
    };
}

export default function CheckoutPage() {
    const navigate = useNavigate();
    const { withAuthorizedRequest, fullName } = useAuth();
    const { cart, hasItems } = useCart();

    const initialForm = useMemo(() => buildInitialForm(fullName), [fullName]);

    const [form, setForm] = useState(initialForm);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    function handleChange(event) {
        const { name, value } = event.target;
        setForm((current) => ({
            ...current,
            [name]: value,
        }));
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setLoading(true);
        setError("");

        try {
            const response = await withAuthorizedRequest((token) =>
                api.checkout(token, form)
            );

            const storageKey = `ecommerce.checkoutSession.${response.orderId}`;
            sessionStorage.setItem(storageKey, JSON.stringify(response));

            navigate(`/checkout/session/${response.orderId}`, {
                state: { session: response },
            });
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setLoading(false);
        }
    }

    if (!hasItems) {
        return (
            <EmptyState
                title="Checkout icin sepette urun olmali"
                description="Checkout ekranina gecmeden once sepete urun eklemelisin."
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
            <section className="section-heading">
                <div>
                    <span className="eyebrow">Checkout</span>
                    <h1>Siparis ve odeme bilgileri</h1>
                </div>
            </section>

            {error ? <div className="alert alert--error">{error}</div> : null}

            <div className="checkout-layout">
                <form className="panel form-panel" onSubmit={handleSubmit}>
                    <div className="panel__header">
                        <h2>Teslimat bilgileri</h2>
                        <p>Siparis kaydi ve odeme denemesi icin gerekli alanlar.</p>
                    </div>

                    <div className="form-grid">
                        <label className="field field--full">
                            <span>Ad Soyad</span>
                            <input
                                name="shippingFullName"
                                value={form.shippingFullName}
                                onChange={handleChange}
                                required
                            />
                        </label>

                        <label className="field field--full">
                            <span>Adres</span>
                            <input
                                name="shippingAddressLine"
                                value={form.shippingAddressLine}
                                onChange={handleChange}
                                required
                            />
                        </label>

                        <label className="field">
                            <span>Sehir</span>
                            <input
                                name="city"
                                value={form.city}
                                onChange={handleChange}
                                required
                            />
                        </label>

                        <label className="field">
                            <span>Ilce</span>
                            <input
                                name="district"
                                value={form.district}
                                onChange={handleChange}
                                required
                            />
                        </label>

                        <label className="field">
                            <span>Posta kodu</span>
                            <input
                                name="postalCode"
                                value={form.postalCode}
                                onChange={handleChange}
                                required
                            />
                        </label>
                    </div>

                    <div className="panel__header">
                        <h2>Kart bilgileri</h2>
                        <p>
                            Mock akista kart numarasi <strong>0000</strong> ile biterse odeme
                            basarisiz olur.
                        </p>
                    </div>

                    <div className="form-grid">
                        <label className="field field--full">
                            <span>Kart sahibi</span>
                            <input
                                name="cardHolder"
                                value={form.cardHolder}
                                onChange={handleChange}
                                required
                            />
                        </label>

                        <label className="field field--full">
                            <span>Kart numarasi</span>
                            <input
                                name="cardNumber"
                                value={form.cardNumber}
                                onChange={handleChange}
                                required
                            />
                        </label>

                        <label className="field">
                            <span>Ay</span>
                            <input
                                name="expireMonth"
                                value={form.expireMonth}
                                onChange={handleChange}
                                required
                            />
                        </label>

                        <label className="field">
                            <span>Yil</span>
                            <input
                                name="expireYear"
                                value={form.expireYear}
                                onChange={handleChange}
                                required
                            />
                        </label>

                        <label className="field">
                            <span>CVC</span>
                            <input
                                name="cvc"
                                value={form.cvc}
                                onChange={handleChange}
                                required
                            />
                        </label>
                    </div>

                    <button
                        type="submit"
                        className="button button--primary"
                        disabled={loading}
                    >
                        {loading ? "Odeme baslatiliyor..." : "Siparisi Olustur"}
                    </button>
                </form>

                <aside className="summary-card">
                    <span className="eyebrow">Siparis Ozeti</span>
                    <h2>Son kontrol</h2>

                    {cart.items.map((item) => (
                        <div key={item.productId} className="summary-card__line">
              <span>
                {item.productName} x {item.quantity}
              </span>
                            <strong>{formatCurrency(item.lineTotal)}</strong>
                        </div>
                    ))}

                    <div className="summary-card__row summary-card__row--total">
                        <span>Genel toplam</span>
                        <strong>{formatCurrency(cart.totalPrice)}</strong>
                    </div>
                </aside>
            </div>
        </div>
    );
}
