function buildPages(currentPage, totalPages) {
    const pages = [];

    for (let i = 0; i < totalPages; i += 1) {
        if (
            i === 0 ||
            i === totalPages - 1 ||
            Math.abs(i - currentPage) <= 1
        ) {
            pages.push(i);
        }
    }

    return pages.filter((page, index, array) => array.indexOf(page) === index);
}

export default function Pagination({
                                       page,
                                       totalPages,
                                       onChange,
                                   }) {
    if (!totalPages || totalPages <= 1) {
        return null;
    }

    const pages = buildPages(page, totalPages);

    return (
        <nav className="pagination" aria-label="Sayfalama">
            <button
                type="button"
                className="pagination__button"
                onClick={() => onChange(page - 1)}
                disabled={page === 0}
            >
                Geri
            </button>

            <div className="pagination__pages">
                {pages.map((pageNumber, index) => {
                    const previousPage = pages[index - 1];
                    const shouldShowDots =
                        previousPage !== undefined && pageNumber - previousPage > 1;

                    return (
                        <div key={pageNumber} className="pagination__group">
                            {shouldShowDots ? (
                                <span className="pagination__dots">...</span>
                            ) : null}

                            <button
                                type="button"
                                className={
                                    pageNumber === page
                                        ? "pagination__page pagination__page--active"
                                        : "pagination__page"
                                }
                                onClick={() => onChange(pageNumber)}
                            >
                                {pageNumber + 1}
                            </button>
                        </div>
                    );
                })}
            </div>

            <button
                type="button"
                className="pagination__button"
                onClick={() => onChange(page + 1)}
                disabled={page >= totalPages - 1}
            >
                Ileri
            </button>
        </nav>
    );
}
