/**
 * Le backend expose directement ses entites JPA (aucune couche DTO).
 * Ces fonctions aplatissent les objets imbriques pour que les composants
 * manipulent toujours la meme forme de donnees.
 */

export function versUtilisateur(u) {
  if (!u) return null;
  return {
    ...u,
    departement: u.departement?.nom ?? null,
    idDepartement: u.departement?.idDepartement ?? null,
  };
}

export function versProbleme(p) {
  if (!p) return null;
  const technicien = p.technicienAssigne;
  return {
    ...p,
    categorie: p.categorie?.nom ?? null,
    idCategorie: p.categorie?.idCategorie ?? null,
    declarant: p.declarant?.nomComplet ?? null,
    idDeclarant: p.declarant?.idUtilisateur ?? null,
    technicien: technicien?.nomComplet ?? null,
    idTechnicien: technicien?.idUtilisateur ?? null,
    nombreCommentaires: p.commentaires?.length ?? 0,
  };
}

export function versCommentaire(c) {
  if (!c) return null;
  return {
    ...c,
    auteur: c.auteur?.nomComplet ?? null,
    idAuteur: c.auteur?.idUtilisateur ?? null,
    roleAuteur: c.auteur?.role ?? null,
  };
}

export function versHistorique(h) {
  if (!h) return null;
  return {
    ...h,
    auteur: h.auteur?.nomComplet ?? 'Système',
  };
}

export function versAffectation(a) {
  if (!a) return null;
  return {
    ...a,
    technicien: a.technicien?.nomComplet ?? null,
    idTechnicien: a.technicien?.idUtilisateur ?? null,
    affectePar: a.affectePar?.nomComplet ?? null,
  };
}

/** Vue detaillee reconstituee a partir de l'entite Probleme complete. */
export function versDetailProbleme(p) {
  if (!p) return null;
  return {
    probleme: versProbleme(p),
    diagnostic: p.diagnostic ?? null,
    solution: p.solution ?? null,
    commentaires: (p.commentaires ?? []).map(versCommentaire),
    historique: (p.historiques ?? []).map(versHistorique),
    affectations: (p.affectations ?? []).map(versAffectation),
  };
}

/** Convertit un objet Page de Spring Data vers la forme utilisee par la pagination. */
export function versPage(page, mapper = (x) => x) {
  return {
    contenu: (page?.content ?? []).map(mapper),
    page: page?.number ?? 0,
    taille: page?.size ?? 0,
    totalElements: page?.totalElements ?? 0,
    totalPages: page?.totalPages ?? 0,
    premier: page?.first ?? true,
    dernier: page?.last ?? true,
  };
}

/** Ajoute les champs a plat aux problemes contenus dans un tableau de bord. */
export function versTableauBord(donnees) {
  if (!donnees) return null;
  return {
    ...donnees,
    derniersProblemes: (donnees.derniersProblemes ?? []).map(versProbleme),
  };
}
