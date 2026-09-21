import { useState } from 'react';
import { administrateurApi } from '../api/administrateurApi';
import { EnTetePage } from '../components/layout/EnTetePage';
import { BadgeRole } from '../components/ui/Badge';
import { Carte } from '../components/ui/Carte';
import { Champ } from '../components/ui/Champ';
import { Chargement, Message } from '../components/ui/Etats';
import { useAppelApi } from '../hooks/useAppelApi';
import { useAuth } from '../hooks/useAuth';
import { formaterDateHeure, initiales } from '../utils/format';

export function ProfilPage() {
  const { utilisateur } = useAuth();
  const { donnees: profil, chargement } = useAppelApi(() => administrateurApi.profil(), []);

  const [ancien, setAncien] = useState('');
  const [nouveau, setNouveau] = useState('');
  const [confirmation, setConfirmation] = useState('');
  const [message, setMessage] = useState(null);
  const [envoi, setEnvoi] = useState(false);

  const changer = async (e) => {
    e.preventDefault();
    setMessage(null);

    if (nouveau !== confirmation) {
      setMessage({ type: 'erreur', texte: 'Les deux mots de passe ne correspondent pas.' });
      return;
    }
    if (nouveau.length < 8) {
      setMessage({ type: 'erreur', texte: 'Le mot de passe doit contenir au moins 8 caractères.' });
      return;
    }

    setEnvoi(true);
    try {
      await administrateurApi.changerMotDePasse(ancien, nouveau);
      setMessage({ type: 'succes', texte: 'Mot de passe modifié avec succès.' });
      setAncien('');
      setNouveau('');
      setConfirmation('');
    } catch (e2) {
      setMessage({ type: 'erreur', texte: e2.message });
    } finally {
      setEnvoi(false);
    }
  };

  if (chargement) return <Chargement />;

  const p = profil || utilisateur;

  return (
    <>
      <EnTetePage titre="Mon profil" description="Informations personnelles et sécurité du compte." />

      <div className="grille-2">
        <Carte titre="Informations personnelles">
          <div className="ligne" style={{ marginBottom: 18 }}>
            <div className="avatar" style={{ width: 54, height: 54, fontSize: 18 }}>
              {initiales(p?.nomComplet)}
            </div>
            <div>
              <h2 style={{ marginBottom: 3 }}>{p?.nomComplet}</h2>
              <BadgeRole role={p?.role} />
            </div>
          </div>

          <div className="definitions">
            <div>
              <div className="definition__cle">Email</div>
              <div className="definition__valeur">{p?.email}</div>
            </div>
            <div>
              <div className="definition__cle">Téléphone</div>
              <div className="definition__valeur">{p?.telephone || '—'}</div>
            </div>
            <div>
              <div className="definition__cle">Département</div>
              <div className="definition__valeur">{p?.departement || '—'}</div>
            </div>
            <div>
              <div className="definition__cle">
                {p?.specialite ? 'Spécialité' : p?.service ? 'Service' : 'Poste'}
              </div>
              <div className="definition__valeur">
                {p?.specialite || p?.service || p?.poste || '—'}
              </div>
            </div>
            <div>
              <div className="definition__cle">Compte créé le</div>
              <div className="definition__valeur">{formaterDateHeure(p?.dateCreation)}</div>
            </div>
            <div>
              <div className="definition__cle">Dernière connexion</div>
              <div className="definition__valeur">{formaterDateHeure(p?.derniereConnexion)}</div>
            </div>
          </div>

          {p?.chargeTravail !== null && p?.chargeTravail !== undefined && (
            <>
              <div className="separateur" />
              <div className="definition__cle">Charge de travail actuelle</div>
              <div className="definition__valeur">{p.chargeTravail} problème(s) en cours</div>
            </>
          )}
        </Carte>

        <Carte titre="Changer mon mot de passe">
          {message && <Message type={message.type}>{message.texte}</Message>}

          <form onSubmit={changer}>
            <Champ label="Mot de passe actuel" requis>
              <input
                type="password"
                value={ancien}
                onChange={(e) => setAncien(e.target.value)}
                autoComplete="current-password"
                required
              />
            </Champ>
            <Champ label="Nouveau mot de passe" requis aide="8 caractères minimum.">
              <input
                type="password"
                value={nouveau}
                onChange={(e) => setNouveau(e.target.value)}
                autoComplete="new-password"
                required
              />
            </Champ>
            <Champ label="Confirmer le nouveau mot de passe" requis>
              <input
                type="password"
                value={confirmation}
                onChange={(e) => setConfirmation(e.target.value)}
                autoComplete="new-password"
                required
              />
            </Champ>
            <button type="submit" className="btn btn--primaire btn--bloc" disabled={envoi}>
              {envoi ? 'Modification…' : 'Modifier le mot de passe'}
            </button>
          </form>
        </Carte>
      </div>
    </>
  );
}
