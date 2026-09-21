export function EnTetePage({ titre, description, actions }) {
  return (
    <div className="page-entete">
      <div className="page-entete__texte">
        <h1>{titre}</h1>
        {description && <p>{description}</p>}
      </div>
      {actions && <div className="page-actions">{actions}</div>}
    </div>
  );
}
