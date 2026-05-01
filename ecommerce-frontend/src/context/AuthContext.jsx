import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useRef,
    useState,
} from "react";
import { api } from "../lib/api";

const ACCESS_TOKEN_KEY = "ecommerce.accessToken";
const USER_KEY = "ecommerce.user";

const AuthContext = createContext(null);

function readStoredUser() {
    const rawValue = localStorage.getItem(USER_KEY);

    if (!rawValue) {
        return null;
    }

    try {
        return JSON.parse(rawValue);
    } catch {
        localStorage.removeItem(USER_KEY);
        return null;
    }
}

export function AuthProvider({ children }) {
    const [accessToken, setAccessToken] = useState(
        () => localStorage.getItem(ACCESS_TOKEN_KEY) ?? ""
    );
    const [user, setUser] = useState(() => readStoredUser());
    const [loading, setLoading] = useState(true);

    const accessTokenRef = useRef(accessToken);
    const refreshPromiseRef = useRef(null);

    useEffect(() => {
        accessTokenRef.current = accessToken;

        if (accessToken) {
            localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
        } else {
            localStorage.removeItem(ACCESS_TOKEN_KEY);
        }
    }, [accessToken]);

    useEffect(() => {
        if (user) {
            localStorage.setItem(USER_KEY, JSON.stringify(user));
        } else {
            localStorage.removeItem(USER_KEY);
        }
    }, [user]);

    const clearSession = useCallback(() => {
        setAccessToken("");
        setUser(null);
    }, []);

    const applyAuthResponse = useCallback((response) => {
        setAccessToken(response.accessToken);
        setUser(response.user);
        return response;
    }, []);

    const loadCurrentUser = useCallback(async (token) => {
        const currentUser = await api.getCurrentUser(token);
        setUser(currentUser);
        return currentUser;
    }, []);

    const refreshSession = useCallback(async () => {
        if (refreshPromiseRef.current) {
            return refreshPromiseRef.current;
        }

        const promise = (async () => {
            const refreshResponse = await api.refreshAccessToken();
            setAccessToken(refreshResponse.accessToken);
            const currentUser = await loadCurrentUser(refreshResponse.accessToken);

            return {
                accessToken: refreshResponse.accessToken,
                user: currentUser,
            };
        })()
            .catch((error) => {
                clearSession();
                throw error;
            })
            .finally(() => {
                refreshPromiseRef.current = null;
            });

        refreshPromiseRef.current = promise;
        return promise;
    }, [clearSession, loadCurrentUser]);

    const register = useCallback(
        async (payload) => {
            const response = await api.register(payload);
            applyAuthResponse(response);
            return response;
        },
        [applyAuthResponse]
    );

    const login = useCallback(
        async (payload) => {
            const response = await api.login(payload);
            applyAuthResponse(response);
            return response;
        },
        [applyAuthResponse]
    );

    const logout = useCallback(async () => {
        try {
            await api.logout();
        } finally {
            clearSession();
        }
    }, [clearSession]);

    const withAuthorizedRequest = useCallback(
        async (operation) => {
            let token = accessTokenRef.current;

            if (!token) {
                const refreshed = await refreshSession();
                token = refreshed.accessToken;
            }

            try {
                return await operation(token);
            } catch (error) {
                if (error?.status === 401) {
                    const refreshed = await refreshSession();
                    return operation(refreshed.accessToken);
                }

                throw error;
            }
        },
        [refreshSession]
    );

    useEffect(() => {
        let cancelled = false;

        async function bootstrap() {
            setLoading(true);

            try {
                const storedToken = accessTokenRef.current;

                if (storedToken) {
                    try {
                        await loadCurrentUser(storedToken);

                        if (!cancelled) {
                            setLoading(false);
                        }
                        return;
                    } catch (error) {
                        if (error?.status !== 401) {
                            clearSession();

                            if (!cancelled) {
                                setLoading(false);
                            }
                            return;
                        }
                    }
                }

                try {
                    await refreshSession();
                } catch {
                    clearSession();
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }

        bootstrap();

        return () => {
            cancelled = true;
        };
    }, [clearSession, loadCurrentUser, refreshSession]);

    const value = useMemo(
        () => ({
            user,
            accessToken,
            loading,
            isAuthenticated: Boolean(accessToken && user),
            fullName: user ? `${user.firstName} ${user.lastName}`.trim() : "",
            login,
            register,
            logout,
            refreshSession,
            withAuthorizedRequest,
        }),
        [
            accessToken,
            loading,
            login,
            logout,
            refreshSession,
            register,
            user,
            withAuthorizedRequest,
        ]
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error("useAuth must be used within AuthProvider");
    }

    return context;
}
