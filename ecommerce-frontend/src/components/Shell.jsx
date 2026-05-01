import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

function navClassName({ isActive }) {
    return isActive ? "shell__nav-link shell__nav-link--active" : "shell__nav-link";
}

export default function Shell({ children }) {
    const navigate = useNavigate();
    const { isAuthenticated, fullName, user, logout } = useAuth();
    const { itemCount } = useCart();

    async function handleLogout() {
        await logout();
        navigate("/");
    }

    return (
        <div className="shell">
            <header className="shell__header">
                <div className="shell__inner">
                    <Link to="/" className="shell__brand">
                        <span className="shell__brand-mark">n11</span>
                        <div>
                            <strong>TalentHub Store</strong>
                            <small>Microservices Commerce Demo</small>
                        </div>
                    </Link>

                    <nav className="shell__nav">
                        <NavLink to="/" className={navClassName}>
                            Urunler
                        </NavLink>

                        {isAuthenticated ? (
                            <>
                                <NavLink to="/orders" className={navClassName}>
                                    Siparislerim
                                </NavLink>
                                <NavLink to="/cart" className={navClassName}>
                                    Sepet
                                    {itemCount > 0 ? (
                                        <span className="shell__cart-count">{itemCount}</span>
                                    ) : null}
                                </NavLink>
                            </>
                        ) : null}
                    </nav>

                    <div className="shell__actions">
                        {isAuthenticated ? (
                            <>
                                <div className="shell__user">
                                    <strong>{fullName}</strong>
                                    <small>{user?.role === "ROLE_ADMIN" ? "Admin" : "Musteri"}</small>
                                </div>
                                <button
                                    type="button"
                                    className="button button--ghost"
                                    onClick={handleLogout}
                                >
                                    Cikis
                                </button>
                            </>
                        ) : (
                            <>
                                <Link to="/login" className="button button--ghost">
                                    Giris
                                </Link>
                                <Link to="/register" className="button button--primary">
                                    Kayit Ol
                                </Link>
                            </>
                        )}
                    </div>
                </div>
            </header>

            <main className="shell__main">
                <div className="shell__inner">{children}</div>
            </main>
        </div>
    );
}
