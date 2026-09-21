import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { notificationApi } from '../api/notificationApi';
import { EnTetePage } from '../components/layout/EnTetePage';
import { Chargement, EtatVide, Message } from '../components/ui/Etats';
import { depuis, formaterDateHeure } from '../utils/format';

const ICONES = {
  AFFECTATION: '➜',
  CHANGEMENT_STATUT: '⟳',
  COMMENTAIRE: '💬',
  RESOLUTION: '✓',
  CLOTURE: '⏹',
  RAPPEL: '⏰',
};

export function NotificationsPage() {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);

  const charger = useCallback(() => {
    setChargement(true);
    notificationApi
      .lister()
      .then(setNotifications)
      .catch((e) => setErreur(e.message))
      .finally(() => setChargement(false));
  }, []);

  useEffect(() => { charger(); }, [charger]);

  const ouvrir = async (n) => {
    if (!n.lue) {
      await notificationApi.marquerCommeLue(n.idNotification).catch(() => undefined);
    }
    if (n.idProbleme) navigate(`/problemes/${n.idProbleme}`);
    else charger();
  };

  const toutMarquer = async () => {
    await notificationApi.marquerToutesCommeLues().catch(() => undefined);
    charger();
  };

  const nonLues = notifications.filter((n) => !n.lue).length;

  return (
    <>
      <EnTetePage
        titre="Notifications"
        description={
          nonLues > 0 ? `${nonLues} notification(s) non lue(s)` : 'Toutes vos notifications sont lues.'
        }
        actions={
          nonLues > 0 && (
            <button type="button" className="btn btn--secondaire" onClick={toutMarquer}>
              Tout marquer comme lu
            </button>
          )
        }
      />

      {erreur && <Message type="erreur">{erreur}</Message>}

      <section className="carte">
        {chargement ? (
          <Chargement />
        ) : !notifications.length ? (
          <EtatVide icone="✉" titre="Aucune notification" texte="Vous êtes à jour." />
        ) : (
          <div style={{ padding: '4px 0' }}>
            {notifications.map((n) => (
              <button
                key={n.idNotification}
                type="button"
                onClick={() => ouvrir(n)}
                style={{
                  display: 'flex',
                  gap: 12,
                  width: '100%',
                  textAlign: 'left',
                  padding: '13px 18px',
                  border: 'none',
                  borderBottom: '1px solid var(--gris-200)',
                  background: n.lue ? 'transparent' : 'var(--vert-050)',
                  cursor: 'pointer',
                  fontFamily: 'inherit',
                }}
              >
                <span style={{ fontSize: 17, opacity: 0.7 }}>
                  {ICONES[n.typeNotification] || '•'}
                </span>
                <span style={{ flex: 1 }}>
                  <span
                    style={{
                      display: 'block',
                      fontSize: 13.5,
                      fontWeight: n.lue ? 400 : 600,
                      color: 'var(--gris-900)',
                    }}
                  >
                    {n.message}
                  </span>
                  <span className="texte-secondaire" title={formaterDateHeure(n.dateEnvoi)}>
                    {n.referenceProbleme ? `${n.referenceProbleme} · ` : ''}
                    {depuis(n.dateEnvoi)}
                  </span>
                </span>
                {!n.lue && (
                  <span
                    style={{
                      width: 8,
                      height: 8,
                      borderRadius: '50%',
                      background: 'var(--vert-500)',
                      alignSelf: 'center',
                    }}
                  />
                )}
              </button>
            ))}
          </div>
        )}
      </section>
    </>
  );
}
