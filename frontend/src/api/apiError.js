export function extraireMessageErreur(error) {
  const donnees = error?.response?.data
  if (donnees?.message) {
    return donnees.message
  }
  if (error?.message) {
    return error.message
  }
  return 'Une erreur inattendue est survenue'
}
