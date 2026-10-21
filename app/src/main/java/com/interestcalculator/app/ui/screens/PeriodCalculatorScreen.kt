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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import java.time.temporal.ChronoUnit

@Composable
fun PeriodCalculatorScreen(
    uiState: CalculatorUiState,
    onNavigateBack: () -> Unit,
    onInterestTypeChanged: (InterestType) -> Unit,
    onPrincipalChanged: (String) -> Unit,
    onRateChanged: (String) -> Unit,
    onRateTypeChanged: (RateType) -> Unit,
    onBorrowDateChanged: (LocalDate) -> Unit,
    onAgreedYearsChanged: (String) -> Unit,
    onAgreedMonthsChanged: (String) -> Unit,
    onAgreedDaysChanged: (String) -> Unit,
    onAgreedPreset: (Long, Long, Long) -> Unit,
    onPayingDateChanged: (LocalDate) -> Unit,
    onPayTodayToggled: (Boolean) -> Unit,
    onCalculateClicked: () -> Unit,
    onResetClicked: () -> Unit
) {
    var showBorrowDatePicker by remember { mutableStateOf(false) }
    var showPayingDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy") }

    val ay = uiState.agreedYears.toLongOrNull() ?: 0L
    val am = uiState.agreedMonths.toLongOrNull() ?: 0L
    val ad = uiState.agreedDays.toLongOrNull() ?: 0L
    val agreedDueDate = uiState.borrowDate.plusYears(ay).plusMonths(am).plusDays(ad)
    val agreedTotalDays = InterestCalculatorEngine.convertPeriodToDays(ay, am, ad)

    val paytimeDays = if (!uiState.payingDate.isBefore(uiState.borrowDate)) {
        InterestCalculatorEngine.calculateElapsedDays(uiState.borrowDate, uiState.payingDate)
    } else 0L
    val paytimeBreakdown = if (!uiState.payingDate.isBefore(uiState.borrowDate)) {
        InterestCalculatorEngine.formatCalendarBreakdown(uiState.borrowDate, uiState.payingDate)
    } else "0 days"
    val paytimeMonthData = if (!uiState.payingDate.isBefore(uiState.borrowDate)) {
        InterestCalculatorEngine.calculateCalendarMonthsAndDays(uiState.borrowDate, uiState.payingDate)
    } else InterestCalculatorEngine.convertDaysToMonths(0L)

    val isOverdue = uiState.payingDate.isAfter(agreedDueDate)
    val isExactDue = uiState.payingDate.isEqual(agreedDueDate)

    if (showBorrowDatePicker) {
        DatePickerModal(
            initialDate = uiState.borrowDate,
            onDateSelected = { onBorrowDateChanged(it) },
            onDismiss = { showBorrowDatePicker = false }
        )
    }

    if (showPayingDatePicker) {
        DatePickerModal(
            initialDate = uiState.payingDate,
            minDate = uiState.borrowDate,
            onDateSelected = { onPayingDateChanged(it) },
            onDismiss = { showPayingDatePicker = false }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Special Time Period",
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

            // Card 1: Principal & Rate
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
                        text = "Principal & Interest Rate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    CurrencyInputField(
                        value = uiState.periodPrincipal,
                        onValueChange = onPrincipalChanged,
                        label = "Principal Amount (₹)",
                        prefix = "₹ ",
                        placeholder = "e.g. 10,000",
                        errorMessage = uiState.principalError
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Rate Type",
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

                    CurrencyInputField(
                        value = uiState.periodRate,
                        onValueChange = onRateChanged,
                        label = if (uiState.rateType == RateType.PER_MONTH) "Interest Rate (% per month / ₹ per 100)" else "Annual Interest Rate (%)",
                        suffix = if (uiState.rateType == RateType.PER_MONTH) "% / mo" else "% / yr",
                        placeholder = if (uiState.rateType == RateType.PER_MONTH) "e.g. 2 or 1.5" else "e.g. 12",
                        errorMessage = uiState.rateError
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: 1. Borrow Date & Initial Agreement (Actual Time Period)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "1. Borrow Date & Initial Agreement",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "How many days/months borrower promised to return",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Borrow Date Picker Box
                    Text(
                        text = "Borrow Date (Loan Taken)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { showBorrowDatePicker = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.borrowDate.format(dateFormatter),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Pick Borrow Date",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Initial Agreement Duration Inputs (Actual time period agreed)
                    Text(
                        text = "Agreed Return Duration (Borrower Promised)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CurrencyInputField(
                            value = uiState.agreedYears,
                            onValueChange = onAgreedYearsChanged,
                            label = "Years",
                            suffix = "yr",
                            placeholder = "0",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )

                        CurrencyInputField(
                            value = uiState.agreedMonths,
                            onValueChange = onAgreedMonthsChanged,
                            label = "Months",
                            suffix = "mo",
                            placeholder = "6",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )

                        CurrencyInputField(
                            value = uiState.agreedDays,
                            onValueChange = onAgreedDaysChanged,
                            label = "Days",
                            suffix = "d",
                            placeholder = "0",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SuggestionChip(
                            onClick = { onAgreedPreset(0, 1, 0) },
                            label = { Text("1 Mo", fontSize = 11.sp) }
                        )
                        SuggestionChip(
                            onClick = { onAgreedPreset(0, 3, 0) },
                            label = { Text("3 Mo", fontSize = 11.sp) }
                        )
                        SuggestionChip(
                            onClick = { onAgreedPreset(0, 6, 0) },
                            label = { Text("6 Mo", fontSize = 11.sp) }
                        )
                        SuggestionChip(
                            onClick = { onAgreedPreset(1, 0, 0) },
                            label = { Text("1 Yr", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Promised Due Date Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Promised Return Date",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${agreedDueDate.format(dateFormatter)} ($agreedTotalDays days agreement)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 3: 2. Repayment Date & Paytime Duration
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
                        text = "2. Repayment Date (Paytime)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Duration is calculated from borrow date to paying date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Paying Date Picker Box
                    Text(
                        text = "Paying Date (Repayment)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { showPayingDatePicker = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.payingDate.format(dateFormatter),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Pick Paying Date",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Use Today Checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.isPayToday,
                            onCheckedChange = { onPayTodayToggled(it) },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary)
                        )
                        Text(
                            text = "Repaid Today (${LocalDate.now().format(dateFormatter)})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Paytime Duration Breakdown Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
                            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Time Period on Paytime:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "$paytimeDays days",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = paytimeBreakdown,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Formula Months (T): ${paytimeMonthData.totalMonthsFraction.setScale(2, java.math.RoundingMode.HALF_UP)} months (${paytimeMonthData.monthsDisplay})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Badge (Overdue vs On-Time vs Early)
                    if (isOverdue) {
                        val lateDays = ChronoUnit.DAYS.between(agreedDueDate, uiState.payingDate)
                        val lateMonthData = InterestCalculatorEngine.calculateCalendarMonthsAndDays(agreedDueDate, uiState.payingDate)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFF3CD))
                                .border(1.dp, Color(0xFFFFC107), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFB78103),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "⚠️ OVERDUE / DELAYED",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF856404)
                                    )
                                    Text(
                                        text = "Delayed by ${lateMonthData.fullMonths} mo, ${lateMonthData.remainingDays} d ($lateDays days past agreement)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF856404)
                                    )
                                }
                            }
                        }
                    } else if (isExactDue) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFD4EDDA))
                                .border(1.dp, Color(0xFF28A745), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF155724),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = " Paid exactly on agreed due date!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF155724)
                                )
                            }
                        }
                    } else {
                        val earlyDays = ChronoUnit.DAYS.between(uiState.payingDate, agreedDueDate)
                        val earlyMonthData = InterestCalculatorEngine.calculateCalendarMonthsAndDays(uiState.payingDate, agreedDueDate)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFD4EDDA))
                                .border(1.dp, Color(0xFF28A745), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF155724),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = " PAID EARLY (ON TIME)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF155724)
                                    )
                                    Text(
                                        text = "Paid ${earlyMonthData.fullMonths} mo, ${earlyMonthData.remainingDays} d ($earlyDays days) ahead of agreed date",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF155724)
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.periodError != null) {
                        Text(
                            text = uiState.periodError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 4: Live Results Preview
            if (uiState.currentResult != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ESTIMATED TOTAL INTEREST",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "Formula: (P×T×R)/100",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = InterestCalculatorEngine.formatIndianCurrency(uiState.currentResult.totalInterest),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (uiState.currentResult.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (uiState.currentResult.isOverdue && uiState.currentResult.actualAgreedAmount != null) {
                            Text(
                                text = "Amount After Overdue Date: ${InterestCalculatorEngine.formatIndianCurrency(uiState.currentResult.totalAmount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Actual Amount on Due Date: ${InterestCalculatorEngine.formatIndianCurrency(uiState.currentResult.actualAgreedAmount)} (P + I combined)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Total Payable Amount: ${InterestCalculatorEngine.formatIndianCurrency(uiState.currentResult.totalAmount)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Action Buttons
            Button(
                onClick = onCalculateClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Calculate Full Breakdown",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onResetClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reset All Inputs",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
