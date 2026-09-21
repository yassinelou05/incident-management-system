export function Chargement({ texte = 'Chargement en cours…' }) {
  return (
    <div className="chargement">
      <span className="spinner" aria-hidden="true" />
      <span>{texte}</span>
    </div>
  );
}

export function EtatVide({ icone = '📄', titre = 'Aucun résultat', texte, action }) {
  return (
    <div className="vide">
      <div className="vide__icone">{icone}</div>
      <div className="vide__titre">{titre}</div>
      {texte && <div className="vide__texte">{texte}</div>}
      {action && <div style={{ marginTop: 14 }}>{action}</div>}
    </div>
  );
}

export function Message({ type = 'info', children }) {
  if (!children) return null;
  return <div className={`message message--${type}`}>{children}</div>;
}
