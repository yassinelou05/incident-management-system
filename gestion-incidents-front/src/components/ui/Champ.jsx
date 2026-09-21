/** Champs de formulaire uniformises (label, aide, message d'erreur). */

export function Champ({ label, requis, erreur, aide, children }) {
  return (
    <div className={`champ ${erreur ? 'champ--erreur' : ''}`}>
      {label && (
        <label className="champ__label">
          {label}
          {requis && <span className="requis">*</span>}
        </label>
      )}
      {children}
      {aide && !erreur && <div className="champ__aide">{aide}</div>}
      {erreur && <div className="champ__erreur">{erreur}</div>}
    </div>
  );
}

export function Selection({ valeur, onChanger, options, vide = 'Tous', ...reste }) {
  return (
    <select value={valeur ?? ''} onChange={(e) => onChanger(e.target.value || null)} {...reste}>
      {vide !== null && <option value="">{vide}</option>}
      {options.map((o) => (
        <option key={o.valeur} value={o.valeur}>
          {o.libelle}
        </option>
      ))}
    </select>
  );
}
