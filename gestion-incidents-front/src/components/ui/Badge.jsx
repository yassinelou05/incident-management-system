import {
  LIBELLES_PRIORITE,
  LIBELLES_ROLE,
  LIBELLES_STATUT,
  LIBELLES_STATUT_COMPTE,
} from '../../utils/constants';

const CLASSE_STATUT = {
  NOUVEAU: 'nouveau',
  ASSIGNE: 'assigne',
  EN_COURS: 'en-cours',
  EN_ATTENTE: 'en-attente',
  RESOLU: 'resolu',
  FERME: 'ferme',
};

export function BadgeStatut({ statut }) {
  if (!statut) return <span className="texte-secondaire">—</span>;
  return (
    <span className={`badge badge--${CLASSE_STATUT[statut] || 'en-attente'}`}>
      {LIBELLES_STATUT[statut] || statut}
    </span>
  );
}

export function BadgePriorite({ priorite }) {
  if (!priorite) return <span className="texte-secondaire">—</span>;
  return (
    <span className={`badge badge--${priorite.toLowerCase()}`}>
      {LIBELLES_PRIORITE[priorite] || priorite}
    </span>
  );
}

export function BadgeRole({ role }) {
  if (!role) return null;
  return <span className="badge badge--role">{LIBELLES_ROLE[role] || role}</span>;
}

export function BadgeStatutCompte({ statut }) {
  if (!statut) return null;
  return (
    <span className={`badge badge--${statut.toLowerCase()}`}>
      {LIBELLES_STATUT_COMPTE[statut] || statut}
    </span>
  );
}
