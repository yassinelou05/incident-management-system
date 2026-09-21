export function Pagination({ page, totalPages, totalElements, onChanger }) {
  if (!totalPages || totalPages <= 1) {
    return (
      <div className="pagination">
        <span>{totalElements ?? 0} résultat(s)</span>
      </div>
    );
  }

  const pages = [];
  const debut = Math.max(0, Math.min(page - 2, totalPages - 5));
  const fin = Math.min(totalPages, debut + 5);
  for (let i = debut; i < fin; i += 1) pages.push(i);

  return (
    <div className="pagination">
      <span>
        Page {page + 1} sur {totalPages} — {totalElements} résultat(s)
      </span>
      <div className="pagination__boutons">
        <button
          type="button"
          className="pagination__page"
          disabled={page === 0}
          onClick={() => onChanger(page - 1)}
        >
          ‹
        </button>
        {pages.map((p) => (
          <button
            key={p}
            type="button"
            className={`pagination__page ${p === page ? 'pagination__page--actif' : ''}`}
            onClick={() => onChanger(p)}
          >
            {p + 1}
          </button>
        ))}
        <button
          type="button"
          className="pagination__page"
          disabled={page >= totalPages - 1}
          onClick={() => onChanger(page + 1)}
        >
          ›
        </button>
      </div>
    </div>
  );
}
