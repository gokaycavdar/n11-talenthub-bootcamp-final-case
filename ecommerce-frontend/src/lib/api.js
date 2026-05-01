const rawBaseUrl =
    import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1";

export const API_BASE_URL = rawBaseUrl.endsWith("/")
    ? rawBaseUrl.slice(0, -1)
    : rawBaseUrl;

export const API_ORIGIN = new URL(API_BASE_URL).origin;

export class ApiError extends Error {
    constructor({
                    status = 500,
                    errorCode = "UNKNOWN_ERROR",
                    message = "Beklenmeyen bir hata olustu.",
                    correlationId = null,
                    payload = null,
                }) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.errorCode = errorCode;
        this.correlationId = correlationId;
        this.payload = payload;
    }
}

function buildUrl(path, query) {
    const normalizedPath = path.startsWith("/") ? path : `/${path}`;
    const url = new URL(`${API_BASE_URL}${normalizedPath}`);

    if (query) {
        Object.entries(query).forEach(([key, value]) => {
            if (value === undefined || value === null || value === "") {
                return;
            }
            url.searchParams.set(key, String(value));
        });
    }

    return url.toString();
}

async function parseResponse(response) {
    if (response.status === 204) {
        return null;
    }

    const contentType = response.headers.get("content-type") ?? "";

    if (contentType.includes("application/json")) {
        return response.json();
    }

    const text = await response.text();
    return text || null;
}

function toApiError(response, payload) {
    return new ApiError({
        status: response.status,
        errorCode: payload?.errorCode ?? `HTTP_${response.status}`,
        message: payload?.message ?? response.statusText ?? "Beklenmeyen bir hata olustu.",
        correlationId:
            payload?.correlationId ?? response.headers.get("x-correlation-id"),
        payload,
    });
}

async function request(path, options = {}) {
    const {
        method = "GET",
        token,
        query,
        body,
        headers = {},
        credentials = "include",
    } = options;

    const finalHeaders = new Headers(headers);

    if (!finalHeaders.has("Accept")) {
        finalHeaders.set("Accept", "application/json");
    }

    if (body !== undefined && !finalHeaders.has("Content-Type")) {
        finalHeaders.set("Content-Type", "application/json");
    }

    if (token) {
        finalHeaders.set("Authorization", `Bearer ${token}`);
    }

    try {
        const response = await fetch(buildUrl(path, query), {
            method,
            headers: finalHeaders,
            credentials,
            body: body === undefined ? undefined : JSON.stringify(body),
        });

        const payload = await parseResponse(response);

        if (!response.ok) {
            throw toApiError(response, payload);
        }

        return payload;
    } catch (error) {
        if (error instanceof ApiError) {
            throw error;
        }

        throw new ApiError({
            status: 0,
            errorCode: "NETWORK_ERROR",
            message: "Sunucuya ulasilamadi. Servislerin ayakta oldugunu kontrol et.",
            correlationId: null,
            payload: null,
        });
    }
}

export function resolveImageUrl(imageUrl) {
    if (!imageUrl) {
        return "";
    }

    if (/^https?:\/\//i.test(imageUrl)) {
        return imageUrl;
    }

    if (imageUrl.startsWith("/")) {
        return imageUrl;
    }

    return `/${imageUrl}`;
}

export const api = {
    register(payload) {
        return request("/auth/register", {
            method: "POST",
            body: payload,
        });
    },

    login(payload) {
        return request("/auth/login", {
            method: "POST",
            body: payload,
        });
    },

    refreshAccessToken() {
        return request("/auth/refresh", {
            method: "POST",
        });
    },

    logout() {
        return request("/auth/logout", {
            method: "POST",
        });
    },

    getCurrentUser(token) {
        return request("/users/me", {
            token,
        });
    },

    getProducts({ page = 0, size = 8, sort = "id" } = {}) {
        return request("/products", {
            query: { page, size, sort },
        });
    },

    getProductById(productId) {
        return request(`/products/${productId}`);
    },

    getMyCart(token) {
        return request("/carts/me", {
            token,
        });
    },

    addCartItem(token, payload) {
        return request("/carts/items", {
            method: "POST",
            token,
            body: payload,
        });
    },

    updateCartItemQuantity(token, productId, payload) {
        return request(`/carts/items/${productId}`, {
            method: "PATCH",
            token,
            body: payload,
        });
    },

    removeCartItem(token, productId) {
        return request(`/carts/items/${productId}`, {
            method: "DELETE",
            token,
        });
    },

    clearCart(token) {
        return request("/carts/me/clear", {
            method: "DELETE",
            token,
        });
    },

    checkout(token, payload) {
        return request("/orders/checkout", {
            method: "POST",
            token,
            body: payload,
        });
    },

    getMyOrders(token) {
        return request("/orders/me", {
            token,
        });
    },

    getOrderById(token, orderId) {
        return request(`/orders/${orderId}`, {
            token,
        });
    },
};
