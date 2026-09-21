import { useNavigate } from 'react-router-dom';
import { problemeApi } from '../api/problemeApi';
import { EnTetePage } from '../components/layout/EnTetePage';
import { BadgePriorite, BadgeStatut } from '../components/ui/Badge';
import { Chargement, EtatVide, Message } from '../components/ui/Etats';
import { useAppelApi } from '../hooks/useAppelApi';
import { formaterDate, depuis } from '../utils/format';

/**
 * Page reutilisee pour deux perimetres :
 *   - "declarations" : les problemes declares par l'employe connecte ;
 *   - "affectations" : les problemes assignes au technicien connecte.
 */
export function MesProblemesPage({ mode = 'declarations' }) {
  const navigate = useNavigate();
  const estAffectation = mode === 'affectations';

  const { donnees, chargement, erreur } = useAppelApi(
    () => (estAffectation ? problemeApi.mesAffectations() : problemeApi.mesDeclarations()),
    [estAffectation],
  );

  const titre = estAffectation ? 'Mes affectations' : 'Mes déclarations';
  const description = estAffectation
    ? 'Problèmes qui vous sont actuellement assignés, du plus ancien au plus récent.'
    : 'Suivi en temps réel des problèmes que vous avez déclarés.';

  return (
    <>
      <EnTetePage
        titre={titre}
        description={description}
        actions={
          !estAffectation && (
            <button type="button" className="btn btn--primaire" onClick={() => navigate('/declarer')}>
              ＋ Déclarer un problème
            </button>
          )
        }
      />

      {erreur && <Message type="erreur">{erreur}</Message>}

      <section className="carte">
        {chargement ? (
          <Chargement />
        ) : !donnees?.length ? (
          <EtatVide
            icone={estAffectation ? '🧰' : '📝'}
            titre={estAffectation ? 'Aucune affectation en cours' : 'Aucune déclaration'}
            texte={
              estAffectation
                ? "Aucun problème ne vous est assigné pour le moment."
                : "Vous n'avez déclaré aucun problème technique."
            }
            action={
              !estAffectation && (
                <button
                  type="button"
                  className="btn btn--primaire"
                  onClick={() => navigate('/declarer')}
                >
                  Déclarer un problème
                </button>
              )
            }
          />
        ) : (
          <div className="tableau-conteneur">
            <table className="tableau">
              <thead>
                <tr>
                  <th>Référence</th>
                  <th>Titre</th>
                  <th>Catégorie</th>
                  <th>Statut</th>
                  <th>Priorité</th>
                  <th>{estAffectation ? 'Déclarant' : 'Technicien'}</th>
                  <th>Déclaré</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {donnees.map((p) => (
                  <tr
                    key={p.idProbleme}
                    style={{ cursor: 'pointer' }}
                    onClick={() => navigate(`/problemes/${p.idProbleme}`)}
                  >
                    <td className="tableau__ref">{p.reference}</td>
                    <td className="tableau__principal">{p.titre}</td>
                    <td>{p.categorie}</td>
                    <td><BadgeStatut statut={p.statut} /></td>
                    <td><BadgePriorite priorite={p.priorite} /></td>
                    <td>
                      {estAffectation
                        ? p.declarant
                        : p.technicien || <span className="texte-secondaire">Non affecté</span>}
                    </td>
                    <td className="tableau__secondaire" title={formaterDate(p.dateDeclaration)}>
                      {depuis(p.dateDeclaration)}
                    </td>
                    <td className="tableau__actions">
                      <span className="btn btn--fantome btn--petit">Ouvrir →</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  );
}
