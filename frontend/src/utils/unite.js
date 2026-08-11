const FACTEURS_KG = { KG: 1, TONNE: 1000 }

export function estUnitePoids(unite) {
  return unite === 'KG' || unite === 'TONNE'
}

export function convertirQuantite(valeur, uniteSaisie, uniteCible) {
  if (uniteSaisie === uniteCible) return valeur
  if (!estUnitePoids(uniteSaisie) || !estUnitePoids(uniteCible)) return valeur
  const valeurEnKg = valeur * FACTEURS_KG[uniteSaisie]
  return valeurEnKg / FACTEURS_KG[uniteCible]
}

export const UNITES_POIDS = ['KG', 'TONNE']
