# Journal des modifications (Changelog)

Toutes les modifications notables apportées à Melodrop sont documentées dans ce fichier.

## [0.4.1] - 29-10-2026

### Modifié
- « Mes skips » déplacé de « Mes likes » vers le profil : un lien jaune beurre « Mes skips › » sous les compteurs (retour des testeurs : un skip n'est pas un like).
- La flèche de retour de « Mes skips » renvoie désormais vers le profil.


## [0.4.0] - 29-10-2026

### Ajouté
- Écran « Mes skips » : les morceaux ignorés au cours des 7 derniers jours, avec leur extrait, leur auteur et l'indication si leur drop est toujours en ligne.
- Bouton cœur pour récupérer un morceau ignoré dans « Mes likes » :
    - drop toujours en ligne : l'auteur reçoit un like ;
    - drop expiré : pas de like pour l'auteur, et le morceau affiche « Récupéré dans les skips » dans « Mes likes ».
- Bouton « Mes skips › » en haut de « Mes likes ».
- Backend : routes `GET /me/skips` et `POST /me/skips/{drop_id}/rescue`, nouvelle colonne `liked_late` dans `liked_songs` (migration Alembic).
- Données de démonstration : trois skips sur des drops expirés pour le compte `litoxam`.

### Modifié
- « Mes likes » se recharge silencieusement lorsqu'il revient à l'écran.
- Liker un nouveau drop d'un morceau déjà récupéré en retard le transforme en un like normal, avec son auteur.


## [0.3.0] - 28-10-2026

### Ajouté
- Notification de rappel « Ton drop expire dans 15 min », envoyée 15 minutes avant l'expiration du drop quotidien, même lorsque l'application est fermée.
- Demande d'autorisation de notification juste après un drop (Android 13 et versions supérieures).
- Le rappel est annulé lorsque l'utilisateur se déconnecte ou supprime son compte.

### Modifié
- Les journaux du serveur (logs) affichent le nom d'utilisateur de chaque requête.

### Corrigé
- Les genres musicaux sont affichés en français (Deezer les envoyait en anglais).
- Script ponctuel pour rafraîchir le genre des morceaux déjà enregistrés.


## [0.2.1] - 27-10-2026

### Ajouté
- Un drop sans pochette affiche une goutte triste avec le message « Pas d'image dispo ! ».

### Corrigé
- Les morceaux sans pochette sur Deezer ne provoquent plus d'erreurs : la pochette est désormais optionnelle dans l'API et dans l'application.
- Les résultats de recherche utilisent le champ `artist`, comme le reste de l'API.


## [0.2.0] - 27-10-2026

### Ajouté
- Lecteur audio sur « Ton drop du jour », pour écouter l'extrait de ton propre drop.
- Boutons Like et Skip sous la carte Découvrir, pour ceux qui préfèrent appuyer plutôt que swiper.
- Bouton pour afficher / masquer le mot de passe sur l'écran de connexion.

### Modifié
- L'écran d'inscription rappelle de vérifier le dossier de spams pour le code de confirmation.


## [0.1.0] - 26-10-2026

Première version de Melodrop, envoyée aux premiers testeurs.

### Ajouté
- Compte
    - Inscription avec un nom d'utilisateur, un e-mail et un mot de passe (au moins 8 caractères, un chiffre et un caractère spécial).
    - E-mail confirmé avec un code à 6 chiffres envoyé par e-mail.
    - Connexion avec le nom d'utilisateur ou l'e-mail.
    - Mot de passe oublié : un code envoyé par e-mail pour en choisir un nouveau.
- Découvrir
    - Fil d'actualité aléatoire des drops du jour des autres utilisateurs.
    - Swipe vers la droite pour liker un drop, vers la gauche pour l'ignorer (skip).
    - Extrait de 30 secondes pour chaque morceau.
    - Genre du morceau, récupéré depuis son album Deezer.
    - Appui sur l'auteur pour ouvrir son profil.
    - Message « Plus de drop à découvrir » une fois que tout a été vu.
- Drop
    - Recherche d'un morceau sur Deezer par titre ou artiste.
    - Un seul drop à la fois, en ligne pendant 24 heures à compter du moment où il est publié.
    - « Ton drop du jour » : le drop actuel, son extrait, ses likes et le temps restant.
- Mes likes
    - Les morceaux likés, avec leur extrait et leur auteur.
    - Supprimer un morceau de la liste.
- Profil
    - Compteurs : drops, likes reçus, morceaux likés.
    - Derniers drops, avec le drop du jour marqué « EN LIGNE ».
    - Nom d'affichage public et avatar (couleur pastel et style goutte).
    - Profil des autres utilisateurs.
- Paramètres du compte : modifier l'e-mail (confirmé avec un code envoyé à la nouvelle adresse), modifier le mot de passe, se déconnecter, supprimer le compte.
- Identité visuelle : thème sombre avec des couleurs pastel, halos lumineux qui se déplacent entre les onglets, animation d'ondulation d'eau sur l'écran de connexion.
- Backend : FastAPI et MySQL, hébergé sur Scaleway à l'adresse api.melodrop-app.com, schéma de base de données géré avec des migrations Alembic.