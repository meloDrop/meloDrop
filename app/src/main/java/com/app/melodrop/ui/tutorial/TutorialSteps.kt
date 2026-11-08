package com.app.melodrop.ui.tutorial

// Steps of each tutorial, shown on an example screen of the tab.
// Shown the first time a tab opens, and again with "Revoir le tuto" in the "i" sheet

// Discover: the card, the swipes and the buttons, then the top of the screen
val DiscoverSteps = listOf(
    TutorialStep(
        target = TutorialTarget.CARD,
        title = "Une chanson choisie par quelqu'un",
        text = "Chaque carte, c'est un titre déposé par un membre. Écoute l'extrait, puis glisse la carte : à droite pour liker, à gauche pour passer, vers le haut pour un pick.",
        // Lighter: the card that swipes stays visible when it leaves the hole
        veilAlpha = 0.55f,
    ),
    TutorialStep(
        target = TutorialTarget.CHOICE_BUTTONS,
        title = "Ou avec les boutons",
        text = "Tu préfères toucher ? ✕ passe le titre, ★ en fait un pick, ♥ le like. C'est pareil qu'un swipe.",
    ),
    TutorialStep(
        target = TutorialTarget.PICK,
        title = "Like ou pick ?",
        text = "Le like, c'est pour un titre qui te plaît. Le pick, pour une vraie découverte : un titre que tu ne connaissais pas et que tu adores. Les deux vont dans « Mes likes », les picks aussi dans « Mes picks ».",
    ),
    TutorialStep(
        target = TutorialTarget.CARD_PEOPLE,
        title = "Qui l'a droppée, qui l'a likée",
        text = "Quand plusieurs membres droppent la même chanson, elle n'apparaît qu'une fois, et ton like compte pour chacun. Touche ici pour voir leurs profils.",
    ),
    TutorialStep(
        target = TutorialTarget.FOLLOWS_FIRST,
        title = "Abonnements d'abord",
        text = "Les drops des gens que tu suis passent en premier. Touche la pastille pour tout mélanger.",
    ),
    TutorialStep(
        target = TutorialTarget.SEARCH,
        title = "Trouver quelqu'un",
        text = "Cherche un @pseudo pour ouvrir son profil et t'y abonner.",
    ),
    TutorialStep(
        target = TutorialTarget.INFO,
        title = "Les nouveautés",
        text = "Le détail de chaque version est ici. Tu peux aussi y revoir ce tuto.",
    ),
)

// Drop: the search, the choice, then the drop of the day
val DropSteps = listOf(
    TutorialStep(
        target = TutorialTarget.DROP_SEARCH,
        title = "Ta chanson du jour",
        text = "Chaque jour, tu peux déposer un titre à faire découvrir. Cherche-le par son nom ou par l'artiste.",
    ),
    TutorialStep(
        target = TutorialTarget.DROP_RESULT,
        title = "Écoute, puis choisis",
        text = "Touche ▶ pour écouter l'extrait, puis touche le titre pour le choisir. On ne peut malheureusement pas choisir le moment de l'extrait.",
    ),
    TutorialStep(
        target = TutorialTarget.DROP_CONFIRM,
        title = "Dépose-le",
        text = "Touche DROP : ton titre arrive dans le fil Discover des autres membres.",
    ),
    TutorialStep(
        target = TutorialTarget.DROP_STATS,
        title = "Ce que ton drop récolte",
        text = "Les likes, les picks et les vues de ton drop, en direct. Touche « likes » ou « picks » pour voir qui a aimé.",
    ),
    TutorialStep(
        target = TutorialTarget.DROP_TIMER,
        title = "24 heures en ligne",
        text = "Ton drop reste en ligne 24 h. Une notification te prévient quand il expire : tu peux alors en déposer un nouveau.",
    ),
)

// Profil: the activity page, then the Mixtape
val ProfileSteps = listOf(
    TutorialStep(
        target = TutorialTarget.PROFILE_STATS,
        title = "Ton activité",
        text = "Tes drops, les likes que tu as reçus et les titres que tu as likés. Touche « drops » ou « titres likés » pour voir toute la liste.",
    ),
    TutorialStep(
        target = TutorialTarget.PROFILE_SKIPS,
        title = "Un skip par erreur ?",
        text = "Les titres passés restent 7 jours dans « Mes skips » : tu peux les repêcher.",
    ),
    TutorialStep(
        target = TutorialTarget.PROFILE_TABS,
        title = "Tes drops et tes picks",
        text = "Tes derniers drops et tes picks. Sous chaque drop, ses vues et son taux de like : touche-les pour voir qui a aimé.",
    ),
    TutorialStep(
        target = TutorialTarget.PROFILE_PAGES,
        title = "Ta Mixtape",
        text = "Glisse vers la gauche, ou touche MIXTAPE. C'est la page que les autres voient en premier sur ton profil.",
    ),
    TutorialStep(
        target = TutorialTarget.PODIUM,
        title = "Ton podium",
        text = "Tes 3 titres préférés de tous les temps. Touche « Modifier » pour les choisir.",
    ),
    TutorialStep(
        target = TutorialTarget.LABELS,
        title = "Tes chansons",
        text = "Un son pour chaque moment : celui qui te fait chialer, celui que tu skippes jamais… Choisis-les avec « Modifier ».",
    ),
    TutorialStep(
        target = TutorialTarget.BADGES,
        title = "Tes badges",
        text = "Les badges que tu gagnes sur meloDrop s'affichent ici.",
    ),
)

// The steps shown on the Mixtape page of the example profile: the others are on the activity
val MixtapeTargets = setOf(
    TutorialTarget.PROFILE_PAGES,
    TutorialTarget.PODIUM,
    TutorialTarget.LABELS,
    TutorialTarget.BADGES,
)

// The steps of a tab
fun stepsFor(screen: TutorialScreen): List<TutorialStep> = when (screen) {
    TutorialScreen.DISCOVER -> DiscoverSteps
    TutorialScreen.DROP -> DropSteps
    TutorialScreen.PROFILE -> ProfileSteps
}
