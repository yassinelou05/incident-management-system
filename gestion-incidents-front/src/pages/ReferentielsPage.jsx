import { useCallback, useEffect, useState } from 'react';
import { referentielApi } from '../api/referentielApi';
import { EnTetePage } from '../components/layout/EnTetePage';
import { Champ } from '../components/ui/Champ';
import { Chargement, EtatVide, Message } from '../components/ui/Etats';
import { Modale } from '../components/ui/Modale';
import { Toasts } from '../components/ui/Toasts';
import { useToast } from '../hooks/useToast';

/**
 * Page de parametrage reutilisee pour les deux referentiels :
 *   - mode "categories"   : types de problemes et delais cibles ;
 *   - mode "departements" : entites organisationnelles.
 */
export function ReferentielsPage({ mode = 'categories' }) {
  const estCategorie = mode === 'categories';
  const toast = useToast();

  const [elements, setElements] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);
  const [modaleOuverte, setModaleOuverte] = useState(false);
  const [edition, setEdition] = useState(null);
  const [formulaire, setFormulaire] = useState({});
  const [envoi, setEnvoi] = useState(false);

  const vide = estCategorie
    ? { nom: '', description: '', delaiCibleHeures: 48, active: true }
    : { nom: '', description: '', localisation: '' };

  const charger = useCallback(() => {
    setChargement(true);
    const promesse = estCategorie
      ? referentielApi.listerCategories()
      : referentielApi.listerDepartements();
    promesse
      .then(setElements)
      .catch((e) => setErreur(e.message))
      .finally(() => setChargement(false));
  }, [estCategorie]);

  useEffect(() => { charger(); }, [charger]);

  const ouvrir = (element) => {
    setEdition(element);
    setFormulaire(element ? { ...element } : vide);
    setModaleOuverte(true);
  };

  const maj = (cle) => (e) =>
    setFormulaire((p) => ({
      ...p,
      [cle]: e.target.type === 'checkbox' ? e.target.checked : e.target.value,
    }));

  const enregistrer = async (e) => {
    e.preventDefault();
    setEnvoi(true);
    try {
      const id = edition?.idCategorie || edition?.idDepartement;
      if (estCategorie) {
        const corps = {
          nom: formulaire.nom,
          description: formulaire.description,
          delaiCibleHeures: Number(formulaire.delaiCibleHeures) || 48,
          active: formulaire.active !== false,
        };
        if (edition) await referentielApi.modifierCategorie(id, corps);
        else await referentielApi.creerCategorie(corps);
      } else {
        const corps = {
          nom: formulaire.nom,
          description: formulaire.description,
          localisation: formulaire.localisation,
        };
        if (edition) await referentielApi.modifierDepartement(id, corps);
        else await referentielApi.creerDepartement(corps);
      }
      toast.succes(edition ? 'Modification enregistrée.' : 'Élément créé.');
      setModaleOuverte(false);
      charger();
    } catch (e2) {
      toast.erreur(e2.message);
    } finally {
      setEnvoi(false);
    }
  };

  const supprimer = async (element) => {
    const id = element.idCategorie || element.idDepartement;
    // eslint-disable-next-line no-alert
    if (!window.confirm(`Supprimer « ${element.nom} » ?`)) return;
    try {
      if (estCategorie) await referentielApi.supprimerCategorie(id);
      else await referentielApi.supprimerDepartement(id);
      toast.succes('Élément supprimé.');
      charger();
    } catch (e) {
      toast.erreur(e.message);
    }
  };

  const basculerActivation = async (categorie) => {
    try {
      await referentielApi.changerActivationCategorie(categorie.idCategorie, !categorie.active);
      toast.succes(categorie.active ? 'Catégorie désactivée.' : 'Catégorie activée.');
      charger();
    } catch (e) {
      toast.erreur(e.message);
    }
  };

  return (
    <>
      <EnTetePage
        titre={estCategorie ? 'Catégories de problèmes' : 'Départements'}
        description={
          estCategorie
            ? "Paramétrage des types de problèmes et de leur délai cible de résolution."
            : "Entités organisationnelles auxquelles les utilisateurs sont rattachés."
        }
        actions={
          <button type="button" className="btn btn--primaire" onClick={() => ouvrir(null)}>
            ＋ {estCategorie ? 'Nouvelle catégorie' : 'Nouveau département'}
          </button>
        }
      />

      {erreur && <Message type="erreur">{erreur}</Message>}

      <section className="carte">
        {chargement ? (
          <Chargement />
        ) : !elements.length ? (
          <EtatVide icone="⌗" titre="Aucun élément" texte="Créez le premier élément du référentiel." />
        ) : (
          <div className="tableau-conteneur">
            <table className="tableau">
              <thead>
                <tr>
                  <th>Nom</th>
                  <th>Description</th>
                  {estCategorie ? <th>Délai cible</th> : <th>Localisation</th>}
                  {estCategorie && <th>État</th>}
                  <th />
                </tr>
              </thead>
              <tbody>
                {elements.map((el) => (
                  <tr key={el.idCategorie || el.idDepartement}>
                    <td className="tableau__principal">{el.nom}</td>
                    <td className="texte-secondaire">{el.description || '—'}</td>
                    <td>{estCategorie ? `${el.delaiCibleHeures} h` : el.localisation || '—'}</td>
                    {estCategorie && (
                      <td>
                        <span className={`badge badge--${el.active ? 'resolu' : 'ferme'}`}>
                          {el.active ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                    )}
                    <td className="tableau__actions">
                      <button
                        type="button"
                        className="btn btn--secondaire btn--petit"
                        onClick={() => ouvrir(el)}
                      >
                        Modifier
                      </button>
                      {estCategorie && (
                        <button
                          type="button"
                          className="btn btn--secondaire btn--petit"
                          onClick={() => basculerActivation(el)}
                        >
                          {el.active ? 'Désactiver' : 'Activer'}
                        </button>
                      )}
                      <button
                        type="button"
                        className="btn btn--fantome btn--petit"
                        onClick={() => supprimer(el)}
                      >
                        Supprimer
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <Modale
        titre={
          edition
            ? `Modifier « ${edition.nom} »`
            : estCategorie
              ? 'Nouvelle catégorie'
              : 'Nouveau département'
        }
        ouverte={modaleOuverte}
        onFermer={() => setModaleOuverte(false)}
        pied={
          <>
            <button type="button" className="btn btn--secondaire" onClick={() => setModaleOuverte(false)}>
              Annuler
            </button>
            <button type="submit" form="form-referentiel" className="btn btn--primaire" disabled={envoi}>
              {envoi ? 'Enregistrement…' : 'Enregistrer'}
            </button>
          </>
        }
      >
        <form id="form-referentiel" onSubmit={enregistrer}>
          <Champ label="Nom" requis>
            <input type="text" value={formulaire.nom || ''} onChange={maj('nom')} required />
          </Champ>

          <Champ label="Description">
            <textarea rows={3} value={formulaire.description || ''} onChange={maj('description')} />
          </Champ>

          {estCategorie ? (
            <>
              <Champ label="Délai cible (heures)" aide="Délai de résolution visé pour cette catégorie.">
                <input
                  type="number"
                  min="1"
                  value={formulaire.delaiCibleHeures ?? 48}
                  onChange={maj('delaiCibleHeures')}
                />
              </Champ>
              <label className="case">
                <input
                  type="checkbox"
                  checked={formulaire.active !== false}
                  onChange={maj('active')}
                />
                Catégorie active (proposée lors d'une déclaration)
              </label>
            </>
          ) : (
            <Champ label="Localisation">
              <input
                type="text"
                value={formulaire.localisation || ''}
                onChange={maj('localisation')}
                placeholder="Bâtiment, étage…"
              />
            </Champ>
          )}
        </form>
      </Modale>

      <Toasts messages={toast.messages} onRetirer={toast.retirer} />
    </>
  );
}
