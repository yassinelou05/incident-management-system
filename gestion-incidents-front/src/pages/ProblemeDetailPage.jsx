import { useCallback, useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { commentaireApi } from '../api/commentaireApi';
import { problemeApi } from '../api/problemeApi';
import { referentielApi } from '../api/referentielApi';
import { EnTetePage } from '../components/layout/EnTetePage';
import { BadgePriorite, BadgeStatut } from '../components/ui/Badge';
import { Carte } from '../components/ui/Carte';
import { Champ } from '../components/ui/Champ';
import { Chargement, EtatVide, Message } from '../components/ui/Etats';
import { Modale } from '../components/ui/Modale';
import { Toasts } from '../components/ui/Toasts';
import { useAuth } from '../hooks/useAuth';
import { useToast } from '../hooks/useToast';
import {
  LIBELLES_ACTION,
  LIBELLES_PRIORITE,
  LIBELLES_STATUT,
  PRIORITES,
  ORDRE_STATUT,
  TRANSITIONS,
} from '../utils/constants';
import { formaterDateHeure, formaterDuree, initiales } from '../utils/format';

export function ProblemeDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const emplacement = useLocation();
  const { utilisateur, peutSuperviser, estTechnicien } = useAuth();
  const toast = useToast();

  const [detail, setDetail] = useState(null);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);
  const [techniciens, setTechniciens] = useState([]);
  const [modale, setModale] = useState(null);
  const [action, setAction] = useState(false);

  const charger = useCallback(() => {
    setChargement(true);
    problemeApi
      .consulter(id)
      .then(setDetail)
      .catch((e) => setErreur(e.message))
      .finally(() => setChargement(false));
  }, [id]);

  useEffect(() => {
    charger();
  }, [charger]);

  useEffect(() => {
    if (peutSuperviser) {
      referentielApi.listerTechniciens().then(setTechniciens).catch(() => undefined);
    }
  }, [peutSuperviser]);

  useEffect(() => {
    if (emplacement.state?.message) {
      toast.succes(emplacement.state.message);
      window.history.replaceState({}, '');
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const executer = async (promesse, messageSucces) => {
    setAction(true);
    try {
      await promesse;
      toast.succes(messageSucces);
      charger();
      setModale(null);
    } catch (e) {
      toast.erreur(e.message);
    } finally {
      setAction(false);
    }
  };

  if (chargement) return <Chargement />;
  if (erreur) return <Message type="erreur">{erreur}</Message>;
  if (!detail) return null;

  const p = detail.probleme;
  const estDeclarant = p.idDeclarant === utilisateur?.idUtilisateur;
  const estTechnicienAssigne = estTechnicien && p.idTechnicien === utilisateur?.idUtilisateur;
  const modifiable = p.modifiable;

  // Statuts atteignables depuis le statut courant, presentes dans l'ordre de
  // la progression. La cloture reste reservee au responsable support et a
  // l'administrateur.
  const transitions = (TRANSITIONS[p.statut] || [])
    .filter((statut) => statut !== 'FERME' || peutSuperviser)
    .sort((a, b) => ORDRE_STATUT[a] - ORDRE_STATUT[b]);

  return (
    <>
      <EnTetePage
        titre={p.titre}
        description={
          <>
            <span className="tableau__ref">{p.reference}</span> · déclaré par {p.declarant} le{' '}
            {formaterDateHeure(p.dateDeclaration)}
          </>
        }
        actions={
          <button type="button" className="btn btn--secondaire" onClick={() => navigate(-1)}>
            ← Retour
          </button>
        }
      />

      <div className="ligne" style={{ marginBottom: 16 }}>
        <BadgeStatut statut={p.statut} />
        <BadgePriorite priorite={p.priorite} />
        <span className="badge badge--role">{p.categorie}</span>
        {!modifiable && <span className="badge badge--ferme">Lecture seule (RG8)</span>}
      </div>

      <div className="grille-2">
        {/* -------------------- Colonne principale -------------------- */}
        <div className="pile">
          <Carte titre="Description">
            <p style={{ whiteSpace: 'pre-wrap', margin: 0 }}>{p.description}</p>
            {p.equipementConcerne && (
              <>
                <div className="separateur" />
                <div className="definition__cle">Équipement ou service concerné</div>
                <div className="definition__valeur">{p.equipementConcerne}</div>
              </>
            )}
          </Carte>

          {(detail.diagnostic || detail.solution) && (
            <Carte titre="Intervention technique">
              {detail.diagnostic && (
                <>
                  <div className="definition__cle">Diagnostic</div>
                  <p style={{ whiteSpace: 'pre-wrap' }}>{detail.diagnostic}</p>
                </>
              )}
              {detail.solution && (
                <>
                  {detail.diagnostic && <div className="separateur" />}
                  <div className="definition__cle">Solution appliquée</div>
                  <p style={{ whiteSpace: 'pre-wrap', margin: 0 }}>{detail.solution}</p>
                </>
              )}
            </Carte>
          )}

          <ZoneCommentaires
            idProbleme={id}
            commentaires={detail.commentaires}
            modifiable={modifiable}
            onAjout={charger}
            toast={toast}
          />

          <Carte titre="Historique et traçabilité">
            {detail.historique?.length ? (
              <div className="chronologie">
                {[...detail.historique].reverse().map((h) => (
                  <div className="chrono-item" key={h.idHistorique}>
                    <div className="chrono-item__titre">
                      {LIBELLES_ACTION[h.typeAction] || h.typeAction}
                      {h.ancienStatut && h.nouveauStatut && (
                        <span className="texte-secondaire">
                          {' '}
                          — {LIBELLES_STATUT[h.ancienStatut]} → {LIBELLES_STATUT[h.nouveauStatut]}
                        </span>
                      )}
                    </div>
                    <div className="chrono-item__meta">
                      {h.auteur} · {formaterDateHeure(h.dateAction)}
                    </div>
                    {h.description && <div className="chrono-item__texte">{h.description}</div>}
                  </div>
                ))}
              </div>
            ) : (
              <EtatVide titre="Aucune action enregistrée" />
            )}
          </Carte>
        </div>

        {/* -------------------- Colonne laterale -------------------- */}
        <div className="pile">
          <Carte titre="Actions disponibles">
            {!modifiable ? (
              <div className="texte-secondaire">
                Ce problème est clôturé : aucune modification n'est possible.
              </div>
            ) : (
              <div className="pile" style={{ gap: 8 }}>
                {peutSuperviser && (
                  <>
                    <button
                      type="button"
                      className="btn btn--primaire btn--bloc"
                      onClick={() => setModale('affectation')}
                    >
                      {p.idTechnicien ? 'Réaffecter à un technicien' : 'Affecter à un technicien'}
                    </button>
                    <button
                      type="button"
                      className="btn btn--secondaire btn--bloc"
                      onClick={() => setModale('priorite')}
                    >
                      Modifier la priorité
                    </button>
                    {transitions.length > 0 && (
                      <button
                        type="button"
                        className="btn btn--secondaire btn--bloc"
                        onClick={() => setModale('statut')}
                      >
                        Changer le statut
                      </button>
                    )}
                  </>
                )}

                {estTechnicienAssigne && p.statut === 'ASSIGNE' && (
                  <button
                    type="button"
                    className="btn btn--primaire btn--bloc"
                    disabled={action}
                    onClick={() =>
                      executer(problemeApi.prendreEnCharge(id), 'Problème pris en charge.')
                    }
                  >
                    Prendre en charge
                  </button>
                )}

                {estTechnicienAssigne && (
                  <>
                    <button
                      type="button"
                      className="btn btn--secondaire btn--bloc"
                      onClick={() => setModale('diagnostic')}
                    >
                      Saisir un diagnostic
                    </button>
                    {transitions.length > 0 && (
                      <button
                        type="button"
                        className="btn btn--secondaire btn--bloc"
                        onClick={() => setModale('statut')}
                      >
                        Changer le statut
                      </button>
                    )}
                    {['ASSIGNE', 'EN_COURS', 'EN_ATTENTE'].includes(p.statut) && (
                      <button
                        type="button"
                        className="btn btn--primaire btn--bloc"
                        onClick={() => setModale('resolution')}
                      >
                        Marquer comme résolu
                      </button>
                    )}
                  </>
                )}

                {estDeclarant && p.statut === 'RESOLU' && !p.resolutionConfirmee && (
                  <>
                    <button
                      type="button"
                      className="btn btn--primaire btn--bloc"
                      disabled={action}
                      onClick={() =>
                        executer(
                          problemeApi.confirmerResolution(id, true, null),
                          'Résolution confirmée.',
                        )
                      }
                    >
                      Confirmer la résolution
                    </button>
                    <button
                      type="button"
                      className="btn btn--danger btn--bloc"
                      onClick={() => setModale('refus')}
                    >
                      Le problème persiste
                    </button>
                  </>
                )}

                {peutSuperviser && p.statut !== 'FERME' && (
                  <button
                    type="button"
                    className="btn btn--danger btn--bloc"
                    onClick={() => setModale('cloture')}
                  >
                    Clôturer le problème
                  </button>
                )}
              </div>
            )}
          </Carte>

          <Carte titre="Informations">
            <div className="pile" style={{ gap: 12 }}>
              <Info cle="Déclarant" valeur={p.declarant} />
              <Info cle="Technicien assigné" valeur={p.technicien || 'Non affecté'} />
              <Info cle="Catégorie" valeur={p.categorie} />
              <Info cle="Priorité" valeur={LIBELLES_PRIORITE[p.priorite]} />
              <Info cle="Déclaré le" valeur={formaterDateHeure(p.dateDeclaration)} />
              <Info cle="Affecté le" valeur={formaterDateHeure(p.dateAffectation)} />
              <Info cle="Pris en charge le" valeur={formaterDateHeure(p.datePriseEnCharge)} />
              <Info cle="Résolu le" valeur={formaterDateHeure(p.dateResolution)} />
              <Info cle="Clôturé le" valeur={formaterDateHeure(p.dateCloture)} />
              <Info cle="Délai de résolution" valeur={formaterDuree(p.delaiResolutionHeures)} />
              <Info
                cle="Résolution confirmée"
                valeur={p.resolutionConfirmee ? 'Oui, par le déclarant' : 'Non'}
              />
            </div>
          </Carte>

          {detail.affectations?.length > 0 && (
            <Carte titre="Historique des affectations">
              <div className="pile" style={{ gap: 10 }}>
                {detail.affectations.map((a) => (
                  <div key={a.idAffectation}>
                    <div className="ligne" style={{ justifyContent: 'space-between' }}>
                      <strong style={{ fontSize: 13 }}>{a.technicien}</strong>
                      <span className={`badge badge--${a.active ? 'resolu' : 'ferme'}`}>
                        {a.active ? 'Active' : 'Terminée'}
                      </span>
                    </div>
                    <div className="texte-secondaire">
                      {formaterDateHeure(a.dateAffectation)}
                      {a.affectePar ? ` · par ${a.affectePar}` : ''}
                    </div>
                    {a.noteAffectation && (
                      <div className="texte-secondaire" style={{ fontStyle: 'italic' }}>
                        « {a.noteAffectation} »
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </Carte>
          )}
        </div>
      </div>

      {/* -------------------- Modales d'action -------------------- */}
      <ModaleAffectation
        ouverte={modale === 'affectation'}
        onFermer={() => setModale(null)}
        techniciens={techniciens}
        idActuel={p.idTechnicien}
        enCours={action}
        onValider={(idTechnicien, note) =>
          executer(
            p.idTechnicien
              ? problemeApi.reaffecter(id, idTechnicien, note)
              : problemeApi.affecter(id, idTechnicien, note),
            'Problème affecté au technicien.',
          )
        }
      />

      <ModaleTexte
        ouverte={modale === 'diagnostic'}
        titre="Saisir le diagnostic"
        label="Diagnostic technique"
        aide="Décrivez la cause identifiée du problème."
        valeurInitiale={detail.diagnostic || ''}
        enCours={action}
        onFermer={() => setModale(null)}
        onValider={(texte) =>
          executer(problemeApi.enregistrerDiagnostic(id, texte), 'Diagnostic enregistré.')
        }
      />

      <ModaleTexte
        ouverte={modale === 'resolution'}
        titre="Marquer le problème comme résolu"
        label="Solution appliquée"
        aide="La description de la solution est obligatoire (RG7)."
        enCours={action}
        onFermer={() => setModale(null)}
        onValider={(texte) => executer(problemeApi.resoudre(id, texte), 'Problème marqué comme résolu.')}
      />

      <ModaleTexte
        ouverte={modale === 'refus'}
        titre="Signaler que le problème persiste"
        label="Précisions"
        aide="Expliquez pourquoi la solution ne vous convient pas ; le technicien sera notifié."
        enCours={action}
        onFermer={() => setModale(null)}
        onValider={(texte) =>
          executer(problemeApi.confirmerResolution(id, false, texte), 'Le technicien a été notifié.')
        }
      />

      <ModaleTexte
        ouverte={modale === 'cloture'}
        titre="Clôturer le problème"
        label="Motif de clôture"
        aide="Le problème deviendra définitivement non modifiable (RG8)."
        obligatoire={false}
        enCours={action}
        onFermer={() => setModale(null)}
        onValider={(texte) => executer(problemeApi.cloturer(id, texte), 'Problème clôturé.')}
      />

      <ModaleStatut
        ouverte={modale === 'statut'}
        transitions={transitions}
        enCours={action}
        onFermer={() => setModale(null)}
        onValider={(statut, motif) =>
          executer(problemeApi.changerStatut(id, statut, motif), 'Statut mis à jour.')
        }
      />

      <ModalePriorite
        ouverte={modale === 'priorite'}
        prioriteActuelle={p.priorite}
        enCours={action}
        onFermer={() => setModale(null)}
        onValider={(priorite, motif) =>
          executer(problemeApi.definirPriorite(id, priorite, motif), 'Priorité mise à jour.')
        }
      />

      <Toasts messages={toast.messages} onRetirer={toast.retirer} />
    </>
  );
}

/* ==================== Sous-composants ==================== */

function Info({ cle, valeur }) {
  return (
    <div>
      <div className="definition__cle">{cle}</div>
      <div className="definition__valeur">{valeur || '—'}</div>
    </div>
  );
}

function ZoneCommentaires({ idProbleme, commentaires, modifiable, onAjout, toast }) {
  const { estEmploye } = useAuth();
  const [contenu, setContenu] = useState('');
  const [interne, setInterne] = useState(false);
  const [envoi, setEnvoi] = useState(false);

  const ajouter = async (e) => {
    e.preventDefault();
    if (!contenu.trim()) return;
    setEnvoi(true);
    try {
      await commentaireApi.ajouter(idProbleme, contenu.trim(), interne);
      setContenu('');
      setInterne(false);
      toast.succes('Commentaire ajouté.');
      onAjout();
    } catch (e2) {
      toast.erreur(e2.message);
    } finally {
      setEnvoi(false);
    }
  };

  return (
    <Carte titre={`Commentaires (${commentaires?.length || 0})`}>
      {commentaires?.length ? (
        <div>
          {commentaires.map((c) => (
            <div
              className={`commentaire ${c.interne ? 'commentaire--interne' : ''}`}
              key={c.idCommentaire}
            >
              <div className="avatar">{initiales(c.auteur)}</div>
              <div className="commentaire__corps">
                <div className="commentaire__entete">
                  <span className="commentaire__auteur">{c.auteur}</span>
                  <span className="commentaire__date">{formaterDateHeure(c.dateCreation)}</span>
                  {c.interne && <span className="badge badge--en-cours">Interne</span>}
                </div>
                <div className="commentaire__texte">{c.contenu}</div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <EtatVide icone="💬" titre="Aucun commentaire" texte="Soyez le premier à réagir." />
      )}

      {modifiable && (
        <>
          <div className="separateur" />
          <form onSubmit={ajouter}>
            <Champ label="Ajouter un commentaire">
              <textarea
                rows={3}
                value={contenu}
                onChange={(e) => setContenu(e.target.value)}
                placeholder="Précisions, question, information complémentaire…"
              />
            </Champ>
            <div className="ligne" style={{ justifyContent: 'space-between' }}>
              {!estEmploye ? (
                <label className="case">
                  <input
                    type="checkbox"
                    checked={interne}
                    onChange={(e) => setInterne(e.target.checked)}
                  />
                  Commentaire interne (non visible par le déclarant)
                </label>
              ) : (
                <span />
              )}
              <button type="submit" className="btn btn--primaire" disabled={envoi || !contenu.trim()}>
                {envoi ? 'Envoi…' : 'Publier'}
              </button>
            </div>
          </form>
        </>
      )}
    </Carte>
  );
}

function ModaleAffectation({ ouverte, onFermer, techniciens, idActuel, onValider, enCours }) {
  const [idTechnicien, setIdTechnicien] = useState('');
  const [note, setNote] = useState('');

  useEffect(() => {
    if (ouverte) {
      setIdTechnicien('');
      setNote('');
    }
  }, [ouverte]);

  return (
    <Modale
      titre={idActuel ? 'Réaffecter le problème' : 'Affecter le problème'}
      ouverte={ouverte}
      onFermer={onFermer}
      pied={
        <>
          <button type="button" className="btn btn--secondaire" onClick={onFermer}>
            Annuler
          </button>
          <button
            type="button"
            className="btn btn--primaire"
            disabled={!idTechnicien || enCours}
            onClick={() => onValider(Number(idTechnicien), note)}
          >
            {enCours ? 'Traitement…' : 'Valider l’affectation'}
          </button>
        </>
      }
    >
      <Champ label="Technicien" requis aide="Les techniciens sont triés par charge de travail croissante.">
        <select value={idTechnicien} onChange={(e) => setIdTechnicien(e.target.value)}>
          <option value="">Sélectionner un technicien…</option>
          {[...techniciens]
            .sort((a, b) => (a.chargeTravail ?? 0) - (b.chargeTravail ?? 0))
            .map((t) => (
              <option key={t.idUtilisateur} value={t.idUtilisateur} disabled={t.idUtilisateur === idActuel}>
                {t.nomComplet}
                {t.specialite ? ` — ${t.specialite}` : ''} ({t.chargeTravail ?? 0} en cours)
              </option>
            ))}
        </select>
      </Champ>

      <Champ label="Note d'affectation" aide="Consignes ou contexte à transmettre au technicien.">
        <textarea rows={3} value={note} onChange={(e) => setNote(e.target.value)} />
      </Champ>
    </Modale>
  );
}

function ModaleTexte({
  ouverte,
  titre,
  label,
  aide,
  valeurInitiale = '',
  obligatoire = true,
  onFermer,
  onValider,
  enCours,
}) {
  const [texte, setTexte] = useState(valeurInitiale);

  useEffect(() => {
    if (ouverte) setTexte(valeurInitiale);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [ouverte]);

  return (
    <Modale
      titre={titre}
      ouverte={ouverte}
      onFermer={onFermer}
      pied={
        <>
          <button type="button" className="btn btn--secondaire" onClick={onFermer}>
            Annuler
          </button>
          <button
            type="button"
            className="btn btn--primaire"
            disabled={enCours || (obligatoire && !texte.trim())}
            onClick={() => onValider(texte.trim())}
          >
            {enCours ? 'Traitement…' : 'Valider'}
          </button>
        </>
      }
    >
      <Champ label={label} requis={obligatoire} aide={aide}>
        <textarea rows={6} value={texte} onChange={(e) => setTexte(e.target.value)} />
      </Champ>
    </Modale>
  );
}

function ModaleStatut({ ouverte, transitions, onFermer, onValider, enCours }) {
  const [statut, setStatut] = useState('');
  const [motif, setMotif] = useState('');

  useEffect(() => {
    if (ouverte) {
      setStatut(transitions[0] || '');
      setMotif('');
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [ouverte]);

  return (
    <Modale
      titre="Changer le statut"
      ouverte={ouverte}
      onFermer={onFermer}
      pied={
        <>
          <button type="button" className="btn btn--secondaire" onClick={onFermer}>
            Annuler
          </button>
          <button
            type="button"
            className="btn btn--primaire"
            disabled={!statut || enCours}
            onClick={() => onValider(statut, motif)}
          >
            {enCours ? 'Traitement…' : 'Appliquer'}
          </button>
        </>
      }
    >
      <Champ label="Nouveau statut" requis aide="Seules les transitions autorisées sont proposées.">
        <select value={statut} onChange={(e) => setStatut(e.target.value)}>
          {transitions.map((s) => (
            <option key={s} value={s}>{LIBELLES_STATUT[s]}</option>
          ))}
        </select>
      </Champ>
      <Champ label="Motif">
        <textarea rows={3} value={motif} onChange={(e) => setMotif(e.target.value)} />
      </Champ>
    </Modale>
  );
}

function ModalePriorite({ ouverte, prioriteActuelle, onFermer, onValider, enCours }) {
  const [priorite, setPriorite] = useState(prioriteActuelle);
  const [motif, setMotif] = useState('');

  useEffect(() => {
    if (ouverte) {
      setPriorite(prioriteActuelle);
      setMotif('');
    }
  }, [ouverte, prioriteActuelle]);

  return (
    <Modale
      titre="Modifier la priorité"
      ouverte={ouverte}
      onFermer={onFermer}
      pied={
        <>
          <button type="button" className="btn btn--secondaire" onClick={onFermer}>
            Annuler
          </button>
          <button
            type="button"
            className="btn btn--primaire"
            disabled={enCours}
            onClick={() => onValider(priorite, motif)}
          >
            {enCours ? 'Traitement…' : 'Appliquer'}
          </button>
        </>
      }
    >
      <Champ label="Priorité" requis>
        <select value={priorite} onChange={(e) => setPriorite(e.target.value)}>
          {PRIORITES.map((p) => (
            <option key={p} value={p}>{LIBELLES_PRIORITE[p]}</option>
          ))}
        </select>
      </Champ>
      <Champ label="Motif du changement">
        <textarea rows={3} value={motif} onChange={(e) => setMotif(e.target.value)} />
      </Champ>
    </Modale>
  );
}
