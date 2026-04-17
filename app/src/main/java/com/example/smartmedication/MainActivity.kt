package com.example.smartmedication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmedication.data.Medication
import com.example.smartmedication.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

// ── Aesthetic Blue Palette (Lighter Dark Blues) ───────────────────────────────
private val MedBlue          = Color(0xFF2D6FE8) // Vivid Royal Blue
private val MedPeach         = Color(0xFF5BB8F5) // Sky Blue (accent)
private val MedBlueSurface   = Color(0xFFEAF2FF) // Soft Ice Blue
private val MedWhite         = Color(0xFFF5F9FF) // Blue-tinted White
private val MedError         = Color(0xFFD64F6F) // Muted Rose Red
private val MedSuccess       = Color(0xFF3AAFA9) // Cool Teal Green
private val MedText          = Color(0xFF2E4E7A) // Lighter Navy Text (was 0xFF1A2D4F)
private val MedSubText       = Color(0xFF6889AA) // Lighter Steel Blue-Grey (was 0xFF5A7499)
private val MedDivider       = Color(0xFFCCDFF5) // Light Blue Divider
private val MedCardBg        = Color(0xFFFFFFFF) // Pure White Cards

private val MedColorScheme = lightColorScheme(
    primary            = MedBlue,
    onPrimary          = Color.White,
    primaryContainer   = MedBlueSurface,
    onPrimaryContainer = MedText,
    secondary          = MedPeach,
    onSecondary        = Color.White,
    tertiary           = Color(0xFF6FA8DC),
    tertiaryContainer  = Color(0xFFDCEEFB),
    background         = MedWhite,
    surface            = MedCardBg,
    onBackground       = MedText,
    onSurface          = MedText,
    error              = MedError,
    outline            = MedDivider
)

private val MedTypography = Typography(
    headlineLarge  = androidx.compose.ui.text.TextStyle(
        fontWeight = FontWeight.W300, fontSize = 28.sp, letterSpacing = (-0.5).sp, color = MedText
    ),
    headlineMedium = androidx.compose.ui.text.TextStyle(
        fontWeight = FontWeight.W400, fontSize = 22.sp, letterSpacing = (-0.3).sp, color = MedText
    ),
    titleLarge     = androidx.compose.ui.text.TextStyle(
        fontWeight = FontWeight.W600, fontSize = 17.sp, letterSpacing = 0.sp, color = MedText
    ),
    titleMedium    = androidx.compose.ui.text.TextStyle(
        fontWeight = FontWeight.W600, fontSize = 15.sp, letterSpacing = 0.1.sp, color = MedText
    ),
    bodyMedium     = androidx.compose.ui.text.TextStyle(
        fontWeight = FontWeight.W400, fontSize = 14.sp, letterSpacing = 0.2.sp, color = MedSubText
    ),
    labelSmall     = androidx.compose.ui.text.TextStyle(
        fontWeight = FontWeight.W500, fontSize = 11.sp, letterSpacing = 0.5.sp, color = MedSubText
    )
)

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = MedColorScheme,
                typography  = MedTypography
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background
                ) {
                    MedicationScreen(viewModel)
                }
            }
        }
    }
}

// ── Main Screen ───────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationScreen(viewModel: MainViewModel) {
    val meds by viewModel.medications.collectAsState()

    var expanded         by remember { mutableStateOf(false) }
    var showTimePicker   by remember { mutableStateOf(false) }
    var medicationToEdit by remember { mutableStateOf<Medication?>(null) }

    val calendar        = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour   = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE)
    )

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── Header ──────────────────────────────────────────────────────
        item {
            MedHeader()
        }

        // ── Input Card ──────────────────────────────────────────────────
        item {
            MedInputCard(
                viewModel         = viewModel,
                expanded          = expanded,
                onExpandChange    = { expanded = !expanded },
                onExpandDismiss   = { expanded = false },
                showTimePicker    = showTimePicker,
                onTimePickerClick = { showTimePicker = true },
                timePickerState   = timePickerState
            )
        }

        // ── Section Label ───────────────────────────────────────────────
        item {
            Row(
                modifier          = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text  = "Prescriptions",
                    style = MaterialTheme.typography.titleLarge,
                    color = MedText
                )
                Spacer(Modifier.width(8.dp))
                if (meds.isNotEmpty()) {
                    Surface(
                        shape = CircleShape,
                        color = MedPeach
                    ) {
                        Text(
                            text     = "${meds.size}",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // ── Empty State ─────────────────────────────────────────────────
        if (meds.isEmpty()) {
            item {
                MedEmptyState()
            }
        }

        // ── Medication Cards ─────────────────────────────────────────────
        items(meds) { med ->
            MedCard(
                med      = med,
                onEdit   = { medicationToEdit = med },
                onDelete = { viewModel.deleteMedication(med) }
            )
        }
    }

    // ── Time Picker Dialog ───────────────────────────────────────────────
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            shape            = RoundedCornerShape(24.dp),
            containerColor   = MedWhite,
            confirmButton    = {
                TextButton(onClick = {
                    viewModel.selectedTimeText =
                        String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("Confirm", color = MedBlue, fontWeight = FontWeight.W600) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = MedSubText)
                }
            },
            title = {
                Text(
                    "Set Reminder",
                    style = MaterialTheme.typography.titleLarge,
                    color = MedText
                )
            },
            text = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    TimePicker(
                        state  = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor                    = MedBlueSurface,
                            clockDialSelectedContentColor     = Color.White,
                            selectorColor                     = MedBlue,
                            timeSelectorSelectedContainerColor = MedBlueSurface,
                            timeSelectorSelectedContentColor   = MedBlue
                        )
                    )
                }
            }
        )
    }

    // ── Edit Dialog ──────────────────────────────────────────────────────
    medicationToEdit?.let { med ->
        EditMedicationDialog(
            medication = med,
            onDismiss  = { medicationToEdit = null },
            onSave     = { updated ->
                viewModel.updateMedication(updated)
                medicationToEdit = null
            }
        )
    }
}

// ── Header ────────────────────────────────────────────────────────────────────
@Composable
private fun MedHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2A5090), Color(0xFF2D6FE8)) // Lighter Navy to Royal Blue
                )
            )
            .padding(horizontal = 24.dp, vertical = 36.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint               = Color.White.copy(alpha = 0.85f),
                    modifier           = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = "Smart Medication App",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize      = 12.sp,
                        letterSpacing = 2.sp,
                        color         = Color.White.copy(alpha = 0.75f),
                        fontWeight    = FontWeight.W500
                    )
                )
            }
        }
    }
}

// ── Input Card ────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedInputCard(
    viewModel         : MainViewModel,
    expanded          : Boolean,
    onExpandChange    : () -> Unit,
    onExpandDismiss   : () -> Unit,
    showTimePicker    : Boolean,
    onTimePickerClick : () -> Unit,
    timePickerState   : TimePickerState
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .offset(y = (-16).dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MedCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Text(
                text  = "Add Medication",
                style = MaterialTheme.typography.titleMedium,
                color = MedText
            )
            Spacer(Modifier.height(16.dp))

            // Name Field
            OutlinedTextField(
                value         = viewModel.name,
                onValueChange = { viewModel.name = it },
                label         = { Text("Medication Name") },
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = medOutlinedFieldColors(),
                modifier      = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            // Dosage Field
            OutlinedTextField(
                value         = viewModel.dosage,
                onValueChange = { viewModel.dosage = it },
                label         = { Text("Dosage") },
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = medOutlinedFieldColors(),
                modifier      = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            // Frequency Dropdown
            ExposedDropdownMenuBox(
                expanded         = expanded,
                onExpandedChange = { onExpandChange() }
            ) {
                OutlinedTextField(
                    value         = viewModel.frequency,
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Frequency") },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    shape         = RoundedCornerShape(12.dp),
                    colors        = medOutlinedFieldColors(),
                    modifier      = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded         = expanded,
                    onDismissRequest = onExpandDismiss,
                    containerColor   = MedCardBg
                ) {
                    listOf("Daily", "Weekly").forEach { option ->
                        DropdownMenuItem(
                            text    = { Text(option, color = MedText) },
                            onClick = {
                                viewModel.frequency = option
                                onExpandDismiss()
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))

            // Time Picker Button
            OutlinedButton(
                onClick  = onTimePickerClick,
                shape    = RoundedCornerShape(12.dp),
                border   = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = MedBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = "Reminder · ${viewModel.selectedTimeText}",
                    fontWeight = FontWeight.W500,
                    fontSize   = 14.sp
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MedDivider, thickness = 1.dp)
            Spacer(Modifier.height(16.dp))

            // Action Buttons
            val canSubmit = viewModel.name.isNotBlank() && viewModel.dosage.isNotBlank()

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick  = {
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE,      timePickerState.minute)
                            set(Calendar.SECOND, 0)
                        }
                        if (cal.timeInMillis <= System.currentTimeMillis())
                            cal.add(Calendar.DAY_OF_YEAR, 1)
                        viewModel.addMedication(viewModel.name, viewModel.dosage, viewModel.frequency, cal.timeInMillis)
                    },
                    enabled  = canSubmit,
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = MedBlue,
                        contentColor           = Color.White,
                        disabledContainerColor = MedDivider,
                        disabledContentColor   = MedSubText
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Schedule", fontWeight = FontWeight.W600, fontSize = 14.sp)
                }

                FilledTonalButton(
                    onClick  = {
                        viewModel.addMedication(
                            viewModel.name, viewModel.dosage, viewModel.frequency,
                            System.currentTimeMillis() + 10_000
                        )
                    },
                    enabled  = canSubmit,
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.filledTonalButtonColors(
                        containerColor         = MedBlueSurface,
                        contentColor           = MedBlue,
                        disabledContainerColor = MedDivider,
                        disabledContentColor   = MedSubText
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Test 10s", fontWeight = FontWeight.W500, fontSize = 14.sp)
                }
            }
        }
    }
}

// ── Medication Card ───────────────────────────────────────────────────────────
@Composable
private fun MedCard(
    med      : Medication,
    onEdit   : () -> Unit,
    onDelete : () -> Unit
) {
    val statusColor by animateColorAsState(
        targetValue   = if (med.isTaken) MedSuccess else MedPeach,
        animationSpec = tween(400),
        label         = "statusColor"
    )
    val formatter = remember { SimpleDateFormat("hh:mm a · MMM dd", Locale.getDefault()) }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MedCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Left status indicator pill
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(statusColor)
            )
            Spacer(Modifier.width(14.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = med.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MedText
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text     = "${med.dosage}  ·  ${med.frequency}",
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MedSubText,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint               = MedSubText,
                        modifier           = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = formatter.format(Date(med.targetTimeInMillis)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MedSubText
                    )
                }
            }

            // Right: status badge + actions
            Column(horizontalAlignment = Alignment.End) {
                // Status chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.10f)
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = if (med.isTaken) Icons.Default.CheckCircle else Icons.Default.Pending,
                            contentDescription = null,
                            tint               = statusColor,
                            modifier           = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text       = if (med.isTaken) "Taken" else "Pending",
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.W600,
                            color      = statusColor
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Action icons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick  = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint               = MedSubText,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick  = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint               = MedError.copy(alpha = 0.75f),
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────
@Composable
private fun MedEmptyState() {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape    = CircleShape,
            color    = MedBlueSurface,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector        = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint               = MedBlue,
                    modifier           = Modifier.size(32.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text  = "No medications yet",
            style = MaterialTheme.typography.titleMedium,
            color = MedText
        )
    }
}

// ── Edit Dialog ───────────────────────────────────────────────────────────────
@Composable
fun EditMedicationDialog(
    medication : Medication,
    onDismiss  : () -> Unit,
    onSave     : (Medication) -> Unit
) {
    var editName      by remember { mutableStateOf(medication.name) }
    var editDosage    by remember { mutableStateOf(medication.dosage) }
    var editFrequency by remember { mutableStateOf(medication.frequency) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(24.dp),
        containerColor   = MedWhite,
        title            = {
            Text(
                "Edit Medication",
                style = MaterialTheme.typography.titleLarge,
                color = MedText
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value         = editName,
                    onValueChange = { editName = it },
                    label         = { Text("Name") },
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    colors        = medOutlinedFieldColors(),
                    modifier      = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value         = editDosage,
                    onValueChange = { editDosage = it },
                    label         = { Text("Dosage") },
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    colors        = medOutlinedFieldColors(),
                    modifier      = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(medication.copy(name = editName, dosage = editDosage, frequency = editFrequency))
                },
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MedBlue,
                    contentColor   = Color.White
                )
            ) {
                Text("Save", fontWeight = FontWeight.W600)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MedSubText)
            }
        }
    )
}

// ── Shared TextField Colors ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun medOutlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = MedBlue,
    unfocusedBorderColor    = MedDivider,
    focusedLabelColor       = MedBlue,
    unfocusedLabelColor     = MedSubText,
    cursorColor             = MedBlue,
    focusedTextColor        = MedText,
    unfocusedTextColor      = MedText,
    focusedContainerColor   = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)