const REGLES = [
  { test: (v) => v.length >= 8, message: 'au moins 8 caracteres' },
  { test: (v) => /[a-z]/.test(v), message: 'une minuscule' },
  { test: (v) => /[A-Z]/.test(v), message: 'une majuscule' },
  { test: (v) => /\d/.test(v), message: 'un chiffre' },
]

export function evaluerRobustesse(motDePasse) {
  const valeur = motDePasse ?? ''
  const manquants = REGLES.filter((regle) => !regle.test(valeur)).map((regle) => regle.message)

  return {
    robuste: valeur.length > 0 && manquants.length === 0,
    manquants,
  }
}
