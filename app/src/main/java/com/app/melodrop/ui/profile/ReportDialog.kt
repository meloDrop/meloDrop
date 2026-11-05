package com.app.melodrop.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.melodrop.ui.theme.MeloDropTheme

// A reason of the report form: the code sent to the backend and the text shown
data class ReportReason(val code: String, val label: String)

// Same codes as ReportReason in the backend (schemas/reports.py), in the order of the form
val REPORT_REASONS = listOf(
    ReportReason("name", "Nom ou pseudo inapproprié"),
    ReportReason("content", "Drop ou Mixtape choquant"),
    ReportReason("spam", "Spam ou faux compte"),
    ReportReason("harassment", "Harcèlement"),
    ReportReason("other", "Autre raison"),
)

// Longest message the backend accepts
private const val MESSAGE_MAX = 500

// "⋮" button on the profile of another user, with "Signaler le profil"
@Composable
internal fun UserProfileMenu(onReportClick: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }

    // Box: the menu opens right under the button
    Box {
        IconButton(
            onClick = { menuOpen = true },
            modifier = Modifier.semantics { contentDescription = "Plus d'actions" },
        ) {
            Text(
                text = "⋮",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
        ) {
            DropdownMenuItem(
                text = { Text("Signaler le profil") },
                onClick = {
                    menuOpen = false
                    onReportClick()
                },
            )
        }
    }
}

// The report window: a reason, a message, then "Envoyer". Once sent, it says thank you
@Composable
internal fun ReportDialog(
    username: String,
    // True while the backend has not answered: the button waits
    isSending: Boolean,
    // Shown under the message when the report could not be sent
    error: String?,
    // True once the report is saved
    isSent: Boolean,
    // True when the user had already reported this profile and it is not handled yet
    wasAlreadyReported: Boolean,
    onSend: (reason: String, message: String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (isSent) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Merci") },
            text = {
                Text(
                    if (wasAlreadyReported) "Tu as déjà signalé ce profil : l'équipe meloDrop s'en occupe."
                    else "Ton signalement est envoyé. L'équipe meloDrop va regarder ce profil. @$username ne saura pas que c'est toi.",
                )
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            },
        )
        return
    }

    // rememberSaveable: what the user chose and wrote survives a rotation
    var reason by rememberSaveable { mutableStateOf<String?>(null) }
    var message by rememberSaveable { mutableStateOf("") }
    // "Autre raison" needs a few words to know what it is about
    val needsMessage = reason == "other"
    val canSend = reason != null && !isSending && (!needsMessage || message.isNotBlank())

    AlertDialog(
        onDismissRequest = { if (!isSending) onDismiss() },
        title = { Text("Signaler @$username") },
        text = {
            // Scrolls on a small screen, when the keyboard is open
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Qu'est-ce qui ne va pas ?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // One reason at a time: TalkBack reads them as radio buttons
                Column(modifier = Modifier.selectableGroup()) {
                    REPORT_REASONS.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = reason == option.code,
                                    role = Role.RadioButton,
                                    onClick = { reason = option.code },
                                )
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // The whole line is clickable: the button only shows the choice
                            RadioButton(selected = reason == option.code, onClick = null)
                            Text(
                                text = option.label,
                                modifier = Modifier.padding(start = 12.dp),
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = message,
                    // The backend refuses more than 500 characters: the field stops there
                    onValueChange = { message = it.take(MESSAGE_MAX) },
                    label = { Text(if (needsMessage) "Explique ce qui ne va pas" else "Un détail ? (facultatif)") },
                    supportingText = { Text("${message.length} / $MESSAGE_MAX") },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )

                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { reason?.let { onSend(it, message) } },
                enabled = canSend,
            ) {
                Text(if (isSending) "ENVOI…" else "ENVOYER")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSending) {
                Text("ANNULER")
            }
        },
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun ReportDialogPreview() {
    MeloDropTheme {
        ReportDialog(
            username = "elliot",
            isSending = false,
            error = null,
            isSent = false,
            wasAlreadyReported = false,
            onSend = { _, _ -> },
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun ReportSentPreview() {
    MeloDropTheme {
        ReportDialog(
            username = "elliot",
            isSending = false,
            error = null,
            isSent = true,
            wasAlreadyReported = false,
            onSend = { _, _ -> },
            onDismiss = {},
        )
    }
}
