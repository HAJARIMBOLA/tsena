const formateurNombre = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 2 })

export function formaterMontant(valeur) {
  return `${formateurNombre.format(Number(valeur ?? 0))} Ar`
}

export function formaterQuantite(valeur) {
  return formateurNombre.format(Number(valeur ?? 0))
}

export function formaterDate(valeur) {
  if (!valeur) return ''
  return new Date(valeur).toLocaleDateString('fr-FR')
}

export function formaterDateHeure(valeur) {
  if (!valeur) return ''
  return new Date(valeur).toLocaleString('fr-FR')
}
