export default function EmptyState({
                                       title,
                                       description,
                                       action,
                                   }) {
    return (
        <section className="empty-state">
            <div className="empty-state__icon">+</div>
            <h2>{title}</h2>
            <p>{description}</p>
            {action ? <div className="empty-state__action">{action}</div> : null}
        </section>
    );
}
