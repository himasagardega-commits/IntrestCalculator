package com.interestcalculator.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.interestcalculator.app.data.InterestType
import com.interestcalculator.app.data.RateType
import com.interestcalculator.app.engine.InterestCalculatorEngine
import com.interestcalculator.app.ui.components.AppTopBar
import com.interestcalculator.app.ui.components.CurrencyInputField
import com.interestcalculator.app.viewmodel.CalculatorUiState

@Composable
fun PeriodCalculatorScreen(
    uiState: CalculatorUiState,
    onNavigateBack: () -> Unit,
    onInterestTypeChanged: (InterestType) -> Unit,
    onPrincipalChanged: (String) -> Unit,
    onRateChanged: (String) -> Unit,
    onRateTypeChanged: (RateType) -> Unit,
    onYearsChanged: (String) -> Unit,
    onMonthsChanged: (String) -> Unit,
    onDaysChanged: (String) -> Unit,
    onSelectPreset: (Long, Long, Long) -> Unit,
    onCalculateClicked: () -> Unit,
    onResetClicked: () -> Unit
) {
    val y = uiState.periodYears.toLongOrNull() ?: 0L
    val m = uiState.periodMonths.toLongOrNull() ?: 0L
    val d = uiState.periodDays.toLongOrNull() ?: 0L
    val totalDays = InterestCalculatorEngine.convertPeriodToDays(y, m, d)
    val breakdown = InterestCalculatorEngine.formatDaysBreakdown(totalDays)

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Specified Time Period",
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

            // Interest Type Selection Chips
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
                        text = "Principal & Rate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Principal Input
                    CurrencyInputField(
                        value = uiState.periodPrincipal,
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
                        value = uiState.periodRate,
                        onValueChange = onRateChanged,
                        label = if (uiState.rateType == RateType.PER_MONTH) "Interest Rate (% per month / ₹ per 100)" else "Annual Interest Rate (%)",
                        suffix = if (uiState.rateType == RateType.PER_MONTH) "% / mo" else "% / yr",
                        placeholder = if (uiState.rateType == RateType.PER_MONTH) "e.g. 2 or 1.5" else "e.g. 12",
                        errorMessage = uiState.rateError
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Specified Period Header
                    Text(
                        text = "Time Period Duration",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input fields for Years, Months, Days
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CurrencyInputField(
                            value = uiState.periodYears,
                            onValueChange = onYearsChanged,
                            label = "Years",
                            suffix = "yr",
                            placeholder = "0",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )

                        CurrencyInputField(
                            value = uiState.periodMonths,
                            onValueChange = onMonthsChanged,
                            label = "Months",
                            suffix = "mo",
                            placeholder = "0",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )

                        CurrencyInputField(
                            value = uiState.periodDays,
                            onValueChange = onDaysChanged,
                            label = "Days",
                            suffix = "d",
                            placeholder = "0",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (uiState.periodError != null) {
                        Text(
                            text = uiState.periodError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick presets
                    Text(
                        text = "Quick Presets:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SuggestionChip(
                            onClick = { onSelectPreset(0, 0, 45) },
                            label = { Text("45 Days", fontSize = 12.sp) }
                        )
                        SuggestionChip(
                            onClick = { onSelectPreset(0, 6, 0) },
                            label = { Text("6 Months", fontSize = 12.sp) }
                        )
                        SuggestionChip(
                            onClick = { onSelectPreset(1, 0, 0) },
                            label = { Text("1 Year", fontSize = 12.sp) }
                        )
                        SuggestionChip(
                            onClick = { onSelectPreset(2, 0, 0) },
                            label = { Text("2 Years", fontSize = 12.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Fixed-duration calculation explanation banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Standardized Fixed-Duration",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "1 Year = 365 Days • 1 Month = 30 Days. Total calculated duration: $totalDays days ($breakdown).",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                    lineHeight = 16.sp
                                )
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
