package org.example.project.booking.presentation.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun BookingDialog(
    title: String,
    subtitle: String,
    negativeText: String,
    positiveText: String,
    onNegativeClick: () -> Unit,
    onPositiveClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Message Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Message Subtitle
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNegativeClick,
                        modifier = Modifier
                            .weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = negativeText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(
                        onClick = onPositiveClick,
                        modifier = Modifier
                            .weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = positiveText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CancelAnywayDialog(
    onDismiss: () -> Unit = {},
    onCancelAnyway: () -> Unit = {},
    onReschedule: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BookingDialog(
        title = "Are you sure about cancelling this booking ?",
        subtitle = "You can always reschedule it.",
        negativeText = "Cancel anyway",
        positiveText = "Reschedule",
        onNegativeClick = onCancelAnyway,
        onPositiveClick = onReschedule,
        onDismiss = onDismiss,
        modifier = modifier
    )
}

@Composable
fun RescheduleConfirmDialog(
    onDismiss: () -> Unit = {},
    onCancel: () -> Unit = {},
    onReschedule: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BookingDialog(
        title = "Are you sure about rescheduling this booking ?",
        subtitle = "Your previous slot will be released.",
        negativeText = "Cancel",
        positiveText = "Reschedule",
        onNegativeClick = onCancel,
        onPositiveClick = onReschedule,
        onDismiss = onDismiss,
        modifier = modifier
    )
}
