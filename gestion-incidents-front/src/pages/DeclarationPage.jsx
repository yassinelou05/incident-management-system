import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { problemeApi } from '../api/problemeApi';
import { referentielApi } from '../api/referentielApi';
import { EnTetePage } from '../components/layout/EnTetePage';
import { Carte } from '../components/ui/Carte';
import { Champ } from '../components/ui/Champ';
import { Message } from '../components/ui/Etats';
import { LIBELLES_PRIORITE, PRIORITES } from '../utils/constants';

const FORMULAIRE_INITIAL = {
  titre: '',
  description: '',
  idCategorie: '',
  priorite: 'MOYENNE',
  equipementConcerne: '',
};

export function DeclarationPage() {
  const navigate = useNavigate();
  const [formulaire, setFormulaire] = useState(FORMULAIRE_INITIAL);
  const [categories, setCategories] = useState([]);
  const [erreur, setErreur] = useState(null);
  const [champs, setChamps] = useState({});
  const [envoi, setEnvoi] = useState(false);

  useEffect(() => {
    referentielApi
      .listerCategories(true)
      .then(setCategories)
      .catch(() => undefined);
  }, []);

  const maj = (cle) => (e) => setFormulaire((p) => ({ ...p, [cle]: e.target.value }));

  const soumettre = async (e) => {
    e.preventDefault();
    setErreur(null);
    setChamps({});
    setEnvoi(true);
    try {
      const cree = await problemeApi.declarer({
        ...formulaire,
        idCategorie: Number(formulaire.idCategorie),
      });
      navigate(`/problemes/${cree.idProbleme}`, {
        state: { message: `Problème ${cree.reference} enregistré avec succès.` },
      });
    } catch (e2) {
      setErreur(e2.message);
      setChamps(e2.champs || {});
    } finally {
      setEnvoi(false);
    }
  };

  const categorieChoisie = categories.find((c) => String(c.idCategorie) === String(formulaire.idCategorie));

  return (
    <>
      <EnTetePage
        titre="Déclarer un problème technique"
        description="Décrivez précisément la difficulté rencontrée : le support technique sera notifié immédiatement."
      />

      <div className="grille-2">
        <Carte titre="Formulaire de déclaration">
          <Message type="erreur">{erreur}</Message>

          <form onSubmit={soumettre}>
            <Champ label="Titre du problème" requis erreur={champs.titre}
                   aide="Résumez le problème en une phrase courte et explicite.">
              <input
                type="text"
                maxLength={200}
                value={formulaire.titre}
                onChange={maj('titre')}
                placeholder="Ex. : imprimante du bureau B-104 hors service"
                required
              />
            </Champ>

            <Champ label="Description détaillée" requis erreur={champs.description}
                   aide="Indiquez le contexte, le message d'erreur éventuel et les actions déjà tentées.">
              <textarea
                rows={6}
                value={formulaire.description}
                onChange={maj('description')}
                placeholder="Décrivez le problème, son apparition et son impact sur votre activité…"
                required
              />
            </Champ>

            <div className="grille-champs">
              <Champ label="Catégorie" requis erreur={champs.idCategorie}>
                <select value={formulaire.idCategorie} onChange={maj('idCategorie')} required>
                  <option value="">Sélectionner…</option>
                  {categories.map((c) => (
                    <option key={c.idCategorie} value={c.idCategorie}>{c.nom}</option>
                  ))}
                </select>
              </Champ>

              <Champ label="Priorité souhaitée" requis erreur={champs.priorite}>
                <select value={formulaire.priorite} onChange={maj('priorite')} required>
                  {PRIORITES.map((p) => (
                    <option key={p} value={p}>{LIBELLES_PRIORITE[p]}</option>
                  ))}
                </select>
              </Champ>
            </div>

            <Champ label="Équipement ou service concerné"
                   aide="Numéro d'inventaire, nom du poste, application concernée…">
              <input
                type="text"
                value={formulaire.equipementConcerne}
                onChange={maj('equipementConcerne')}
                placeholder="Ex. : poste de travail A-210"
              />
            </Champ>

            <div className="separateur" />

            <div className="ligne ligne--fin">
              <button type="button" className="btn btn--secondaire" onClick={() => navigate(-1)}>
                Annuler
              </button>
              <button type="submit" className="btn btn--primaire" disabled={envoi}>
                {envoi ? 'Enregistrement…' : 'Soumettre la déclaration'}
              </button>
            </div>
          </form>
        </Carte>

        <div className="pile">
          <Carte titre="Ce qui se passe ensuite">
            <ol style={{ paddingLeft: 18, margin: 0, fontSize: 13, lineHeight: 1.9 }}>
              <li>Le problème est enregistré au statut <strong>Nouveau</strong>.</li>
              <li>Le responsable support analyse la demande.</li>
              <li>Un technicien est affecté et vous êtes notifié.</li>
              <li>Le technicien diagnostique puis applique une solution.</li>
              <li>Vous confirmez la résolution avant la clôture.</li>
            </ol>
          </Carte>

          {categorieChoisie && (
            <Carte titre="Catégorie sélectionnée">
              <div className="definition__cle">Libellé</div>
              <div className="definition__valeur" style={{ marginBottom: 10 }}>
                {categorieChoisie.nom}
              </div>
              {categorieChoisie.description && (
                <>
                  <div className="definition__cle">Description</div>
                  <div className="definition__valeur" style={{ marginBottom: 10 }}>
                    {categorieChoisie.description}
                  </div>
                </>
              )}
              <div className="definition__cle">Délai cible</div>
              <div className="definition__valeur">{categorieChoisie.delaiCibleHeures} heures</div>
            </Carte>
          )}
        </div>
      </div>
    </>
  );
}
