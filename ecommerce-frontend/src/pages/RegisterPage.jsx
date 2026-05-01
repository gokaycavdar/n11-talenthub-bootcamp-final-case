import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const initialForm = {
    firstName: "",
    lastName: "",
    email: "",
    password: "",
};

export default function RegisterPage() {
    const navigate = useNavigate();
    const { register } = useAuth();

    const [form, setForm] = useState(initialForm);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

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
            await register(form);
            navigate("/", { replace: true });
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setLoading(false);
        }
    }

    return (
        <section className="auth-page">
            <div className="auth-card">
                <div className="auth-card__header">
                    <span className="eyebrow">Yeni hesap</span>
                    <h1>Kayit ol ve alisverise basla</h1>
                    <p>
                        Hesabini olusturdugunda access token body’de, refresh token ise
                        guvenli cookie’de yonetilecek.
                    </p>
                </div>

                {error ? <div className="alert alert--error">{error}</div> : null}

                <form className="auth-form" onSubmit={handleSubmit}>
                    <div className="auth-form__row">
                        <label className="field">
                            <span>Ad</span>
                            <input
                                type="text"
                                name="firstName"
                                value={form.firstName}
                                onChange={handleChange}
                                placeholder="Gokay"
                                required
                            />
                        </label>

                        <label className="field">
                            <span>Soyad</span>
                            <input
                                type="text"
                                name="lastName"
                                value={form.lastName}
                                onChange={handleChange}
                                placeholder="Cavdar"
                                required
                            />
                        </label>
                    </div>

                    <label className="field">
                        <span>E-posta</span>
                        <input
                            type="email"
                            name="email"
                            value={form.email}
                            onChange={handleChange}
                            placeholder="ornek@mail.com"
                            required
                        />
                    </label>

                    <label className="field">
                        <span>Sifre</span>
                        <input
                            type="password"
                            name="password"
                            value={form.password}
                            onChange={handleChange}
                            placeholder="En az 6 karakter"
                            minLength={6}
                            required
                        />
                    </label>

                    <button
                        type="submit"
                        className="button button--primary button--block"
                        disabled={loading}
                    >
                        {loading ? "Kayit olusturuluyor..." : "Kayit Ol"}
                    </button>
                </form>

                <p className="auth-card__footer">
                    Zaten hesabin var mi? <Link to="/login">Giris yap</Link>
                </p>
            </div>
        </section>
    );
}
