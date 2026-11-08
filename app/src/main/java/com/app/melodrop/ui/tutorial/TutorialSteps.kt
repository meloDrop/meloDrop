package com.app.melodrop.ui.tutorial

// Steps of the tour shown after an update, for the version installed.
// To update at each new version, like WhatsNewItems.
// Empty list: no tour for this version, the "Nouveautés" sheet opens instead
val TutorialSteps = listOf(
    TutorialStep(
        target = TutorialTarget.SEARCH,
        title = "Trouver quelqu'un",
        text = "Cherche un @pseudo pour ouvrir son profil et t'y abonner.",
    ),
    TutorialStep(
        target = TutorialTarget.FOLLOWS_FIRST,
        title = "Abonnements d'abord",
        text = "Les drops des gens que tu suis passent en premier dans le fil. Touche la pastille pour tout mélanger.",
    ),
    TutorialStep(
        target = TutorialTarget.PICK,
        title = "Le Pick",
        text = "Un titre que tu découvres et qui te plaît ? Touche l'étoile, ou glisse la carte vers le haut. Il rejoint « Mes likes » et « Mes picks ».",
    ),
    TutorialStep(
        target = TutorialTarget.TAB_PROFILE,
        title = "Ton profil",
        text = "Ta Mixtape, tes drops, et tes listes : likes, picks et skips.",
    ),
)

// Always the last step, added by itself after the others:
// the full list of changes stays under the "i" button
val TutorialLastStep = TutorialStep(
    target = TutorialTarget.INFO,
    title = "Les nouveautés",
    text = "Le détail de chaque version est ici. Tu peux aussi y revoir ce tuto.",
)
