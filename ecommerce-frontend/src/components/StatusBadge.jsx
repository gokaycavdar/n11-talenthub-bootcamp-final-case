const toneClassMap = {
    success: "status-badge status-badge--success",
    warning: "status-badge status-badge--warning",
    danger: "status-badge status-badge--danger",
    neutral: "status-badge status-badge--neutral",
};

export default function StatusBadge({
                                        label,
                                        tone = "neutral",
                                    }) {
    return (
        <span className={toneClassMap[tone] ?? toneClassMap.neutral}>
      {label}
    </span>
    );
}
