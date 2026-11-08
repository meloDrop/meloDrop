# Journal des modifications (Changelog)

Toutes les modifications notables apportées à Melodrop sont documentées dans ce fichier.

## [0.9.0] - 08-11-2026

### Ajouté
- Une carte par chanson dans Discover (ADR-0021) : quand plusieurs membres droppent le même titre, il n'apparaît qu'une fois avec « X personnes ont droppé cette chanson », et un like ou un pick compte pour chaque droppeur. Un appui sur cette ligne, ou sur les likes, ouvre qui a droppé le titre et qui l'a liké (un pick y apparaît comme un like).
- Qui a liké et qui a pické mon drop : depuis les tuiles likes et picks de l'écran Drop, et depuis les stats sous les drops de l'utilisateur (profil et « Mes drops »), en deux onglets.
- « Voir plus » et un compteur de drops cliquable sur le profil des autres : « Ses drops » affiche tous leurs drops.
- « drop par X personnes » dans « Mes likes », « Mes picks » et « Mes skips », qui ouvre la liste des droppeurs. La recherche de « Mes likes » et « Mes picks » cherche dans tous les droppeurs.
- Tutos sur des écrans d'exemple la première fois que Discover, Drop et Profil s'ouvrent sur un téléphone, et après la création d'un compte : les swipes sont montrés sur une carte d'exemple. « Revoir le tuto » dans la feuille « Nouveautés » enchaîne les trois.
- Badges automatiques : un badge peut avoir une règle et un seuil, et il est donné tout seul quand un membre l'atteint (dashboard admin).
- Backend : routes `GET /drops/{drop_id}/likers` et `GET /songs/{song_id}/likers` ; `droppers` dans `GET /feed`, `GET /me/likes`, `GET /me/picks` et `GET /me/skips` ; `credited_count` dans le repêchage d'un skip ; colonnes `rule` et `threshold` dans `badges` (migration Alembic).

### Modifié
- Le fil tire au sort les chansons, plus les drops : un titre droppé par beaucoup de membres ne passe pas en premier. Les titres déjà dans « Mes likes », skippés depuis moins de 7 jours, ou droppés par l'utilisateur et encore en ligne ne s'affichent pas.
- Un swipe, et un repêchage depuis « Mes skips », comptent pour chaque drop du titre encore en ligne. « Mes skips » affiche un titre une seule fois.
- La feuille « Nouveautés » attend la fin d'un tuto. Ses textes présentent la version 0.9.0.
- La vue du fil dans le dashboard admin suit les mêmes règles que l'appli.

### Corrigé
- Petits écrans : le texte d'aide de Discover sur une ligne suit l'ordre des boutons (Passer, Pick, Liker).


## [0.8.0] - 05-11-2026

### Ajouté
- « Signaler le profil » dans le menu « ⋮ » du profil des autres : une raison (nom, drop ou Mixtape, spam, harcèlement, autre) et un message, obligatoire pour « autre ». L'équipe meloDrop le traite depuis le dashboard admin.
- Recherche et tri dans « Mes likes » et « Mes picks » : recherche par titre, artiste, genre ou @droppeur, tri par date, genre ou droppeur, avec un petit titre pour chaque groupe.
- Vues d'un drop : une vue, c'est un swipe (like, pick ou skip). L'écran Drop affiche les likes, les picks, les vues et le pourcentage de likes du drop du jour, et les drops de l'utilisateur (profil et « Mes drops ») affichent leurs vues et leur pourcentage de likes.
- Dashboard admin : onglet Signalements avec un compteur rouge (retirer le nom et l'avatar, supprimer le drop, supprimer le compte, classer avec une note), « Donner à plusieurs » pour les badges, likes et picks séparés partout, vues et taux de like des drops, carte Abonnements, profil d'un utilisateur comme dans l'appli (Mixtape et Activité), « Les picks » et « Le podium surprise » dans le Récap.
- Backend : routes `POST /users/{user_id}/report`, `GET /admin/reports`, `PATCH /admin/reports/{report_id}` et `POST /admin/badges/{badge_id}/award` ; nouvelle table `reports` et colonne `is_pick` dans `saves` (migrations Alembic, les anciens swipes sont remplis à partir des picks de « Mes likes ») ; `picks_count` et `views_count` dans `GET /drops/me` et dans les drops de `GET /users/{id}/profile` ; `genre` dans `GET /me/likes` et `GET /me/picks`.

### Modifié
- « drop par @… » sous un pick ouvre le profil du droppeur, sur le profil et sur l'écran « Mes picks ».
- Les textes de la feuille « Nouveautés » présentent la version 0.8.0.

### Corrigé
- Petits écrans : le badge « ALL TIME » du podium s'écrivait lettre par lettre, en colonne. Il garde sa largeur, et c'est le titre qui passe sur deux lignes si besoin.
- Petits écrans : la pochette de Discover devenait minuscule, le bouton lecture la recouvrait et le genre était coupé. La pastille « Abonnements d'abord » prend moins de place, le texte d'aide tient sur une ligne, et sur une petite pochette le bouton lecture rétrécit et les pastilles du genre et « Abonné » sont masquées.
- Profil des autres : le bouton « Suivre en retour » écrasait les compteurs d'abonnés sur les écrans étroits. Il passe sur sa propre ligne, sur toute la largeur, sous les compteurs.


## [0.7.0] - 05-11-2026

### Ajouté
- Profil sur deux pages entre lesquelles on glisse : Activité (compteurs, drops, picks) et Mixtape. Ton profil s'ouvre sur Activité, celui des autres sur la Mixtape.
- Podium de la Mixtape : tes 3 titres préférés de tous les temps, avec leur extrait, à choisir depuis l'écran « Mon podium ».
- Sons à label sur la Mixtape : un titre pour chacun des 8 labels, comme « Le son qui me fait chialer » ou « Mon générique préféré », à choisir depuis l'écran « Mes chansons ».
- Badges : créés et donnés depuis le dashboard admin, affichés sur la Mixtape, 4 par ligne. Un appui ouvre leur description et la date où ils ont été reçus.
- Fiche d'un titre : un appui sur une pochette ouvre l'album, la date de sortie, la durée, le genre, et les drops, likes et picks du titre sur meloDrop. Elle s'ouvre depuis la Mixtape, l'Activité, « Mes likes », « Mes skips », « Mes drops » et « Mes picks ».
- Abonnements : bouton « Suivre » (ou « Suivre en retour ») sur le profil des autres, compteurs d'abonnés et d'abonnements sur chaque profil, et listes qui s'ouvrent depuis ces compteurs.
- Pastille « Abonnements d'abord » dans Discover, activée par défaut : les drops des gens suivis passent en premier, avec la mention « Abonné » sur leur carte.
- Dashboard admin : onglet Badges (créer, modifier, supprimer, avec un aperçu) et carte Badges sur la page d'un utilisateur (donner, retirer).
- Backend : routes `PUT /me/podium`, `PUT /me/labels/{label}`, `DELETE /me/labels/{label}`, `GET /songs/{song_id}`, `POST /users/{user_id}/follow`, `DELETE /users/{user_id}/follow`, `GET /users/{user_id}/followers`, `GET /users/{user_id}/following` et routes admin des badges ; nouvelles tables `podium_songs`, `labeled_songs`, `badges`, `user_badges` et `follows` (migrations Alembic) ; `GET /users/{id}/profile` renvoie le podium, les sons à label, les badges et les compteurs d'abonnements ; `GET /feed` prend le paramètre `follows_first`.

### Modifié
- Dans les grilles de drops et de picks, un appui sur la pochette ouvre la fiche du titre : seul le petit bouton lance l'extrait.
- Les textes de la feuille « Nouveautés » présentent la version 0.7.0.


## [0.6.0] - 05-11-2026

### Ajouté
- Picks : un troisième choix dans Discover, pour un titre que tu découvres et qui te plaît dès la première écoute. Appuie sur l'étoile jaune ou glisse la carte vers le haut. Un pick compte aussi comme un like pour l'auteur du drop.
- Étoile sur chaque titre de « Mes likes » : elle transforme un like en pick, et un deuxième appui le retire des picks.
- Onglets « Mes drops » et « Mes picks » sur ton profil : l'onglet « Mes picks » montre tes 5 derniers picks, et une tuile « Tout voir » ouvre l'écran « Mes picks » avec tous les autres.
- Feuille « Nouveautés » : elle s'ouvre toute seule une fois après chaque mise à jour, et le bouton « i » en forme de goutte de Discover la rouvre quand tu veux.
- Backend : routes `POST /drops/{drop_id}/pick`, `GET /me/picks`, `POST /me/picks/{song_id}` et `DELETE /me/picks/{song_id}`, nouvelle colonne `is_pick` dans `liked_songs` (migration Alembic), champ `is_pick` dans `GET /me/likes`.

### Modifié
- Les trois boutons de Discover (passer, pick, like) ont la même taille, et le bouton Pick est un peu surélevé.
- Le texte sous les cartes devient « Glisse à droite pour Liker, à gauche pour Passer », avec une deuxième ligne pour les picks.
- Lien « Mes skips » du profil en lavande : le jaune beurre est maintenant la couleur des picks.


## [0.5.0] - 02-11-2026

### Ajouté
- Recherche d'utilisateurs : une loupe en haut à droite de Discover, pour retrouver un autre utilisateur par le début de son @pseudo et ouvrir son profil.
- Bouton lecture sur chaque titre d'un profil (drop du jour et derniers drops), sur son propre profil comme sur celui des autres.
- Écran « Mes drops » : tous tes drops, du plus récent au plus ancien, avec leur extrait. Il s'ouvre depuis le compteur « drops » du profil.
- Backend : routes `GET /users/search` et `GET /users/{user_id}/drops`.

### Modifié
- La notification de drop est maintenant envoyée à l'expiration du drop, avec un nouveau texte : « Ton drop a expiré, dépose ta prochaine chanson ! ». Elle reste affichée jusqu'à ce que l'utilisateur la touche ou la balaie.
- Inscription : le pseudo ne peut contenir que des lettres sans accent, des chiffres, `_` et `-`. Un texte d'aide sous le champ rappelle la règle, et le clavier n'ajoute plus d'accents.


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