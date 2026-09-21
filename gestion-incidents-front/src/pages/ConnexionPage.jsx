import { useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { Champ } from '../components/ui/Champ';
import { Message } from '../components/ui/Etats';
import { useAuth } from '../hooks/useAuth';
import '../styles/connexion.css';

export function ConnexionPage() {
  const { connexion, estConnecte, chargement } = useAuth();
  const navigate = useNavigate();
  const emplacement = useLocation();

  const [email, setEmail] = useState('');
  const [motDePasse, setMotDePasse] = useState('');
  const [erreur, setErreur] = useState(null);
  const [envoi, setEnvoi] = useState(false);

  if (!chargement && estConnecte) {
    return <Navigate to={emplacement.state?.depuis || '/tableau-bord'} replace />;
  }

  const soumettre = async (e) => {
    e.preventDefault();
    setErreur(null);
    setEnvoi(true);
    try {
      await connexion(email.trim(), motDePasse);
      navigate(emplacement.state?.depuis || '/tableau-bord', { replace: true });
    } catch (e2) {
      setErreur(e2.message || 'Identifiants invalides.');
    } finally {
      setEnvoi(false);
    }
  };

  return (
    <div className="connexion">
      <section className="connexion__panneau">
        <div className="connexion__organisme">
          Technical Incident Management System
        </div>
        <h1>
          Gestion et suivi des
          <br />
          incidents techniques
        </h1>
        <p>
          Plateforme unique de déclaration, d'affectation et de suivi des incidents informatiques
          du Service des Systèmes d'Information.
        </p>

        <div className="connexion__points">
          <div className="connexion__point">
            <span>▸</span>
            <span>Déclaration guidée et suivi en temps réel de chaque demande</span>
          </div>
          <div className="connexion__point">
            <span>▸</span>
            <span>Affectation aux techniciens et traçabilité complète des interventions</span>
          </div>
          <div className="connexion__point">
            <span>▸</span>
            <span>Indicateurs de performance et délais moyens de résolution</span>
          </div>
        </div>
      </section>

      <section className="connexion__formulaire">
        <div className="connexion__boite">
          <h2>Connexion</h2>
          <p>Accédez à votre espace avec vos identifiants professionnels.</p>

          <Message type="erreur">{erreur}</Message>

          <form onSubmit={soumettre}>
            <Champ label="Adresse email" requis>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                autoComplete="username"
                required
              />
            </Champ>

            <Champ label="Mot de passe" requis>
              <input
                type="password"
                value={motDePasse}
                onChange={(e) => setMotDePasse(e.target.value)}
                autoComplete="current-password"
                required
              />
            </Champ>

            <button type="submit" className="btn btn--primaire btn--bloc" disabled={envoi}>
              {envoi ? 'Connexion…' : 'Se connecter'}
            </button>
          </form>

          <div className="connexion__pied">
            En cas de problème de connexion, contactez le Service des Systèmes d'Information.
          </div>
        </div>
      </section>
    </div>
  );
}
