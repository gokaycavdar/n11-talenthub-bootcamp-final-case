import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useState,
} from "react";
import { api } from "../lib/api";
import { sumCartQuantity } from "../lib/format";
import { useAuth } from "./AuthContext";

const CartContext = createContext(null);

function createEmptyCart(userId = null) {
    return {
        userId,
        items: [],
        totalPrice: 0,
        updatedAt: null,
    };
}

export function CartProvider({ children }) {
    const { user, isAuthenticated, loading: authLoading, withAuthorizedRequest } =
        useAuth();

    const [cart, setCart] = useState(createEmptyCart());
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const resetCart = useCallback((userId = null) => {
        setCart(createEmptyCart(userId));
    }, []);

    const loadCart = useCallback(
        async ({ silent = false } = {}) => {
            if (!isAuthenticated) {
                resetCart(null);
                return createEmptyCart(null);
            }

            if (!silent) {
                setLoading(true);
            }

            setError("");

            try {
                const response = await withAuthorizedRequest((token) =>
                    api.getMyCart(token)
                );

                const nextCart = response ?? createEmptyCart(user?.id ?? null);
                setCart(nextCart);
                return nextCart;
            } catch (requestError) {
                setError(requestError.message);
                throw requestError;
            } finally {
                if (!silent) {
                    setLoading(false);
                }
            }
        },
        [isAuthenticated, resetCart, user?.id, withAuthorizedRequest]
    );

    const addItem = useCallback(
        async (productId, quantity = 1) => {
            setLoading(true);
            setError("");

            try {
                const response = await withAuthorizedRequest((token) =>
                    api.addCartItem(token, {
                        productId,
                        quantity,
                    })
                );

                setCart(response);
                return response;
            } catch (requestError) {
                setError(requestError.message);
                throw requestError;
            } finally {
                setLoading(false);
            }
        },
        [withAuthorizedRequest]
    );

    const updateItemQuantity = useCallback(
        async (productId, quantity) => {
            setLoading(true);
            setError("");

            try {
                const response = await withAuthorizedRequest((token) =>
                    api.updateCartItemQuantity(token, productId, { quantity })
                );

                setCart(response);
                return response;
            } catch (requestError) {
                setError(requestError.message);
                throw requestError;
            } finally {
                setLoading(false);
            }
        },
        [withAuthorizedRequest]
    );

    const removeItem = useCallback(
        async (productId) => {
            setLoading(true);
            setError("");

            try {
                await withAuthorizedRequest((token) =>
                    api.removeCartItem(token, productId)
                );

                await loadCart({ silent: true });
            } catch (requestError) {
                setError(requestError.message);
                throw requestError;
            } finally {
                setLoading(false);
            }
        },
        [loadCart, withAuthorizedRequest]
    );

    const clearCart = useCallback(async () => {
        if (!isAuthenticated) {
            resetCart(null);
            return;
        }

        setLoading(true);
        setError("");

        try {
            await withAuthorizedRequest((token) => api.clearCart(token));
            resetCart(user?.id ?? null);
        } catch (requestError) {
            setError(requestError.message);
            throw requestError;
        } finally {
            setLoading(false);
        }
    }, [isAuthenticated, resetCart, user?.id, withAuthorizedRequest]);

    useEffect(() => {
        if (authLoading) {
            return;
        }

        if (!isAuthenticated) {
            resetCart(null);
            return;
        }

        loadCart().catch(() => {});
    }, [authLoading, isAuthenticated, loadCart, resetCart]);

    const value = useMemo(
        () => ({
            cart,
            loading,
            error,
            itemCount: sumCartQuantity(cart.items),
            hasItems: cart.items.length > 0,
            loadCart,
            addItem,
            updateItemQuantity,
            removeItem,
            clearCart,
            resetCart,
        }),
        [addItem, cart, clearCart, error, loadCart, loading, removeItem, resetCart, updateItemQuantity]
    );

    return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
    const context = useContext(CartContext);

    if (!context) {
        throw new Error("useCart must be used within CartProvider");
    }

    return context;
}
