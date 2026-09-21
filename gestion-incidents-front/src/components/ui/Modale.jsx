import { useEffect } from 'react';

export function Modale({ titre, ouverte, onFermer, children, pied, large = false }) {
  useEffect(() => {
    if (!ouverte) return undefined;
    const gerer = (e) => {
      if (e.key === 'Escape') onFermer?.();
    };
    document.addEventListener('keydown', gerer);
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', gerer);
      document.body.style.overflow = '';
    };
  }, [ouverte, onFermer]);

  if (!ouverte) return null;

  return (
    <div className="modale-fond" onMouseDown={(e) => e.target === e.currentTarget && onFermer?.()}>
      <div className={`modale ${large ? 'modale--large' : ''}`} role="dialog" aria-modal="true">
        <header className="modale__entete">
          <h3>{titre}</h3>
          <button type="button" className="modale__fermer" onClick={onFermer} aria-label="Fermer">
            ×
          </button>
        </header>
        <div className="modale__corps">{children}</div>
        {pied && <footer className="modale__pied">{pied}</footer>}
      </div>
    </div>
  );
}
