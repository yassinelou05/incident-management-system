import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { problemeApi } from '../api/problemeApi';
import { referentielApi } from '../api/referentielApi';
import { EnTetePage } from '../components/layout/EnTetePage';
import { BadgePriorite, BadgeStatut } from '../components/ui/Badge';
import { Carte } from '../components/ui/Carte';
import { Chargement, EtatVide, Message } from '../components/ui/Etats';
import { Pagination } from '../components/ui/Pagination';
import { LIBELLES_PRIORITE, LIBELLES_STATUT, PRIORITES, STATUTS } from '../utils/constants';
import { formaterDate } from '../utils/format';

const FILTRES_INITIAUX = {
  statut: '',
  priorite: '',
  idCategorie: '',
  idTechnicien: '',
  motCle: '',
};

export function ProblemesPage() {
  const navigate = useNavigate();
  const [filtres, setFiltres] = useState(FILTRES_INITIAUX);
  const [page, setPage] = useState(0);
  const [resultat, setResultat] = useState(null);
  const [categories, setCategories] = useState([]);
  const [techniciens, setTechniciens] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);

  useEffect(() => {
    referentielApi.listerCategories().then(setCategories).catch(() => undefined);
    referentielApi.listerTechniciens().then(setTechniciens).catch(() => undefined);
  }, []);

  const charger = useCallback(() => {
    setChargement(true);
    setErreur(null);
    const params = { page, taille: 15 };
    Object.entries(filtres).forEach(([cle, valeur]) => {
      if (valeur !== '' && valeur !== null) params[cle] = valeur;
    });

    problemeApi
      .rechercher(params)
      .then(setResultat)
      .catch((e) => setErreur(e.message))
      .finally(() => setChargement(false));
  }, [filtres, page]);

  useEffect(() => {
    charger();
  }, [charger]);

  const majFiltre = (cle, valeur) => {
    setPage(0);
    setFiltres((p) => ({ ...p, [cle]: valeur }));
  };

  const reinitialiser = () => {
    setPage(0);
    setFiltres(FILTRES_INITIAUX);
  };

  return (
    <>
      <EnTetePage
        titre="Liste des problèmes"
        description="Recherchez, filtrez et consultez l'ensemble des déclarations enregistrées."
        actions={
          <button type="button" className="btn btn--secondaire" onClick={reinitialiser}>
            Réinitialiser les filtres
          </button>
        }
      />

      <Carte className="carte" compact>
        <div className="filtres">
          <div className="filtre-champ">
            <label htmlFor="f-motcle">Recherche</label>
            <input
              id="f-motcle"
              type="search"
              placeholder="Référence, titre, description…"
              value={filtres.motCle}
              onChange={(e) => majFiltre('motCle', e.target.value)}
            />
          </div>

          <div className="filtre-champ">
            <label htmlFor="f-statut">Statut</label>
            <select
              id="f-statut"
              value={filtres.statut}
              onChange={(e) => majFiltre('statut', e.target.value)}
            >
              <option value="">Tous</option>
              {STATUTS.map((s) => (
                <option key={s} value={s}>{LIBELLES_STATUT[s]}</option>
              ))}
            </select>
          </div>

          <div className="filtre-champ">
            <label htmlFor="f-priorite">Priorité</label>
            <select
              id="f-priorite"
              value={filtres.priorite}
              onChange={(e) => majFiltre('priorite', e.target.value)}
            >
              <option value="">Toutes</option>
              {PRIORITES.map((p) => (
                <option key={p} value={p}>{LIBELLES_PRIORITE[p]}</option>
              ))}
            </select>
          </div>

          <div className="filtre-champ">
            <label htmlFor="f-categorie">Catégorie</label>
            <select
              id="f-categorie"
              value={filtres.idCategorie}
              onChange={(e) => majFiltre('idCategorie', e.target.value)}
            >
              <option value="">Toutes</option>
              {categories.map((c) => (
                <option key={c.idCategorie} value={c.idCategorie}>{c.nom}</option>
              ))}
            </select>
          </div>

          <div className="filtre-champ">
            <label htmlFor="f-technicien">Technicien</label>
            <select
              id="f-technicien"
              value={filtres.idTechnicien}
              onChange={(e) => majFiltre('idTechnicien', e.target.value)}
            >
              <option value="">Tous</option>
              {techniciens.map((t) => (
                <option key={t.idUtilisateur} value={t.idUtilisateur}>{t.nomComplet}</option>
              ))}
            </select>
          </div>
        </div>
      </Carte>

      <div style={{ height: 16 }} />

      <section className="carte">
        {erreur && <div style={{ padding: 18 }}><Message type="erreur">{erreur}</Message></div>}

        {chargement ? (
          <Chargement />
        ) : !resultat?.contenu?.length ? (
          <EtatVide
            icone="🔍"
            titre="Aucun problème trouvé"
            texte="Modifiez les critères de recherche pour élargir les résultats."
          />
        ) : (
          <>
            <div className="tableau-conteneur">
              <table className="tableau">
                <thead>
                  <tr>
                    <th>Référence</th>
                    <th>Titre</th>
                    <th>Catégorie</th>
                    <th>Statut</th>
                    <th>Priorité</th>
                    <th>Déclarant</th>
                    <th>Technicien</th>
                    <th>Déclaré le</th>
                  </tr>
                </thead>
                <tbody>
                  {resultat.contenu.map((p) => (
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
                      <td>{p.declarant}</td>
                      <td>{p.technicien || <span className="texte-secondaire">Non affecté</span>}</td>
                      <td className="tableau__secondaire">{formaterDate(p.dateDeclaration)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <Pagination
              page={resultat.page}
              totalPages={resultat.totalPages}
              totalElements={resultat.totalElements}
              onChanger={setPage}
            />
          </>
        )}
      </section>
    </>
  );
}
