import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const initialForm = {
    email: "",
    password: "",
};

export default function LoginPage() {
    const navigate = useNavigate();
    const location = useLocation();
    const { login } = useAuth();

    const [form, setForm] = useState(initialForm);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const nextPath = location.state?.from ?? "/";

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
            await login(form);
            navigate(nextPath, { replace: true });
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
                    <span className="eyebrow">Hos geldin</span>
                    <h1>Hesabina giris yap</h1>
                    <p>Sepet, siparis ve odeme akisina devam etmek icin giris yap.</p>
                </div>

                {error ? <div className="alert alert--error">{error}</div> : null}

                <form className="auth-form" onSubmit={handleSubmit}>
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
                            placeholder="******"
                            required
                        />
                    </label>

                    <button
                        type="submit"
                        className="button button--primary button--block"
                        disabled={loading}
                    >
                        {loading ? "Giris yapiliyor..." : "Giris Yap"}
                    </button>
                </form>

                <p className="auth-card__footer">
                    Hesabin yok mu? <Link to="/register">Kayit ol</Link>
                </p>
            </div>
        </section>
    );
}
