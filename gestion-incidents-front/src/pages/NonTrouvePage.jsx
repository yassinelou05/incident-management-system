import { useNavigate } from 'react-router-dom';

export function NonTrouvePage() {
  const navigate = useNavigate();
  return (
    <div className="vide" style={{ paddingTop: 80 }}>
      <div className="vide__icone" style={{ fontSize: 46 }}>🧭</div>
      <div className="vide__titre" style={{ fontSize: 20 }}>Page introuvable</div>
      <div className="vide__texte">La page demandée n'existe pas ou a été déplacée.</div>
      <div style={{ marginTop: 18 }}>
        <button type="button" className="btn btn--primaire" onClick={() => navigate('/tableau-bord')}>
          Retour au tableau de bord
        </button>
      </div>
    </div>
  );
}
