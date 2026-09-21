export function Carte({ titre, actions, children, compact = false, className = '' }) {
  return (
    <section className={`carte ${className}`}>
      {(titre || actions) && (
        <header className="carte__entete">
          <h3 className="carte__titre">{titre}</h3>
          {actions && <div className="ligne">{actions}</div>}
        </header>
      )}
      <div className={compact ? 'carte__corps carte__corps--compact' : 'carte__corps'}>
        {children}
      </div>
    </section>
  );
}

export function Statistique({ libelle, valeur, complement, variante = '' }) {
  return (
    <div className={`stat ${variante ? `stat--${variante}` : ''}`}>
      <div className="stat__libelle">{libelle}</div>
      <div className="stat__valeur">{valeur ?? '—'}</div>
      {complement && <div className="stat__complement">{complement}</div>}
    </div>
  );
}
