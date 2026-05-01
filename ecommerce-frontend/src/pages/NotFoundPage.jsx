import { Link } from "react-router-dom";
import EmptyState from "../components/EmptyState";

export default function NotFoundPage() {
    return (
        <EmptyState
            title="Sayfa bulunamadi"
            description="Gelmeye calistigin rota bu frontend MVP icinde tanimli degil."
            action={
                <Link to="/" className="button button--primary">
                    Ana sayfaya don
                </Link>
            }
        />
    );
}
