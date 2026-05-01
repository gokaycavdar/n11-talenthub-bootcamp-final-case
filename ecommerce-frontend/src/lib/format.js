const currencyFormatter = new Intl.NumberFormat("tr-TR", {
    style: "currency",
    currency: "TRY",
    maximumFractionDigits: 2,
});

const dateTimeFormatter = new Intl.DateTimeFormat("tr-TR", {
    dateStyle: "medium",
    timeStyle: "short",
});

export function formatCurrency(value) {
    const number = Number(value ?? 0);
    return currencyFormatter.format(Number.isNaN(number) ? 0 : number);
}

export function formatDateTime(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return "-";
    }

    return dateTimeFormatter.format(date);
}

export function formatOrderStatus(status) {
    const labels = {
        PENDING_PAYMENT: "Odeme Bekleniyor",
        PAID: "Odeme Alindi",
        PAYMENT_FAILED: "Odeme Basarisiz",
    };

    return labels[status] ?? status ?? "-";
}

export function getOrderStatusTone(status) {
    const tones = {
        PENDING_PAYMENT: "warning",
        PAID: "success",
        PAYMENT_FAILED: "danger",
    };

    return tones[status] ?? "neutral";
}

export function formatRole(role) {
    const labels = {
        ROLE_ADMIN: "Admin",
        ROLE_USER: "Musteri",
    };

    return labels[role] ?? role ?? "-";
}

export function sumCartQuantity(items = []) {
    return items.reduce((total, item) => total + Number(item.quantity ?? 0), 0);
}
