package com.interestcalculator.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.interestcalculator.app.data.InterestType
import com.interestcalculator.app.data.RateType
import com.interestcalculator.app.engine.InterestCalculatorEngine
import com.interestcalculator.app.ui.components.AppTopBar
import com.interestcalculator.app.ui.components.CurrencyInputField
import com.interestcalculator.app.ui.components.DatePickerModal
import com.interestcalculator.app.viewmodel.CalculatorUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DateCalculatorScreen(
    uiState: CalculatorUiState,
    onNavigateBack: () -> Unit,
    onInterestTypeChanged: (InterestType) -> Unit,
    onPrincipalChanged: (String) -> Unit,
    onRateChanged: (String) -> Unit,
    onRateTypeChanged: (RateType) -> Unit,
    onStartDateChanged: (LocalDate) -> Unit,
    onEndDateChanged: (LocalDate) -> Unit,
    onUseTodayToggled: (Boolean) -> Unit,
    onCalculateClicked: () -> Unit,
    onResetClicked: () -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy") }

    val daysDiff = if (!uiState.endDate.isBefore(uiState.startDate)) {
        InterestCalculatorEngine.calculateElapsedDays(uiState.startDate, uiState.endDate)
    } else 0L

    val calendarBreakdown = if (!uiState.endDate.isBefore(uiState.startDate)) {
        InterestCalculatorEngine.formatCalendarBreakdown(uiState.startDate, uiState.endDate)
    } else "0 days"

    val calendarMonthConv = if (!uiState.endDate.isBefore(uiState.startDate)) {
        InterestCalculatorEngine.calculateCalendarMonthsAndDays(uiState.startDate, uiState.endDate)
    } else null

    if (showStartDatePicker) {
        DatePickerModal(
            initialDate = uiState.startDate,
            onDateSelected = { onStartDateChanged(it) },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerModal(
            initialDate = uiState.endDate,
            minDate = uiState.startDate,
            onDateSelected = { onEndDateChanged(it) },
            onDismiss = { showEndDatePicker = false }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = uiState.selectedInterestType.displayName,
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Interest Type Selector Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedInterestType == InterestType.SIMPLE,
                    onClick = { onInterestTypeChanged(InterestType.SIMPLE) },
                    label = { Text("Simple Interest", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                FilterChip(
                    selected = uiState.selectedInterestType == InterestType.COMPOUND,
                    onClick = { onInterestTypeChanged(InterestType.COMPOUND) },
                    label = { Text("Compound Interest", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Input Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Enter Calculation Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Principal Amount Input
                    CurrencyInputField(
                        value = uiState.datePrincipal,
                        onValueChange = onPrincipalChanged,
                        label = "Principal Amount (₹)",
                        prefix = "₹ ",
                        placeholder = "e.g. 10,000",
                        errorMessage = uiState.principalError
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rate Type Toggle: % / Month vs % / Year
                    Text(
                        text = "Interest Rate Type",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.rateType == RateType.PER_MONTH,
                            onClick = { onRateTypeChanged(RateType.PER_MONTH) },
                            label = { Text("% / Month (₹/100)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.rateType == RateType.PER_YEAR,
                            onClick = { onRateTypeChanged(RateType.PER_YEAR) },
                            label = { Text("% / Year (p.a.)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rate Input with Dynamic Label & Suffix
                    CurrencyInputField(
                        value = uiState.dateRate,
                        onValueChange = onRateChanged,
                        label = if (uiState.rateType == RateType.PER_MONTH) "Interest Rate (% per month / ₹ per 100)" else "Annual Interest Rate (%)",
                        suffix = if (uiState.rateType == RateType.PER_MONTH) "% / mo" else "% / yr",
                        placeholder = if (uiState.rateType == RateType.PER_MONTH) "e.g. 2 or 1.5" else "e.g. 12",
                        errorMessage = uiState.rateError
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dates Section Header
                    Text(
                        text = "Calendar Duration",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Start Date Picker Row
                    DateSelectorRow(
                        label = "Start Date",
                        formattedDate = uiState.startDate.format(dateFormatter),
                        onClick = { showStartDatePicker = true }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // End Date Picker Row
                    DateSelectorRow(
                        label = "End Date",
                        formattedDate = uiState.endDate.format(dateFormatter),
                        onClick = { showEndDatePicker = true }
                    )

                    if (uiState.dateError != null) {
                        Text(
                            text = uiState.dateError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // "Use Today's Date" checkbox/button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUseTodayToggled(!uiState.isUseToday) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.isUseToday,
                            onCheckedChange = onUseTodayToggled,
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Use Today's Date for End Date",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Duration Summary Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Total Duration: $daysDiff days",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = calendarBreakdown,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                                )
                                if (calendarMonthConv != null) {
                                    Text(
                                        text = "Monthly Basis: ${calendarMonthConv.monthsDisplay}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons: Calculate and Clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onResetClicked,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset inputs",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onCalculateClicked,
                    modifier = Modifier
                        .weight(2f)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Calculate",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Calculate", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Live Calculation Quick View (if available)
            if (uiState.currentResult != null) {
                LiveCalculationCard(
                    interestFormatted = InterestCalculatorEngine.formatIndianCurrency(uiState.currentResult.totalInterest),
                    totalFormatted = InterestCalculatorEngine.formatIndianCurrency(uiState.currentResult.totalAmount),
                    interestType = uiState.selectedInterestType,
                    onViewFullResults = onCalculateClicked
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun DateSelectorRow(
    label: String,
    formattedDate: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Pick $label",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun LiveCalculationCard(
    interestFormatted: String,
    totalFormatted: String,
    interestType: InterestType,
    onViewFullResults: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onViewFullResults)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Live Result (${interestType.displayName})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Interest: ",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = interestFormatted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Total Amount: ",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = totalFormatted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Button(
                onClick = onViewFullResults,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("View Report", fontSize = 12.sp)
            }
        }
    }
}
