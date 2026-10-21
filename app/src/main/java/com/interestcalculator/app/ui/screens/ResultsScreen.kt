package com.interestcalculator.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.interestcalculator.app.data.CalculationMode
import com.interestcalculator.app.data.CalculationResult
import com.interestcalculator.app.data.InterestType
import com.interestcalculator.app.engine.InterestCalculatorEngine
import com.interestcalculator.app.ui.components.AppTopBar
import java.time.format.DateTimeFormatter

@Composable
fun ResultsScreen(
    result: CalculationResult?,
    onNavigateBack: () -> Unit,
    onRecalculate: () -> Unit,
    onGoHome: () -> Unit
) {
    val context = LocalContext.current
    var showBreakdown by remember { mutableStateOf(false) }

    if (result == null) {
        EmptyResultsView(onNavigateBack = onNavigateBack)
        return
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy") }

    val principalFormatted = InterestCalculatorEngine.formatIndianCurrency(result.principal)
    val interestFormatted = InterestCalculatorEngine.formatIndianCurrency(result.totalInterest)
    val totalAmountFormatted = InterestCalculatorEngine.formatIndianCurrency(result.totalAmount)

    fun copyToClipboard() {
        val summaryText = buildString {
            appendLine("=== Interest Calculation Summary ===")
            appendLine("Interest Type: ${result.interestType.displayName}")
            appendLine("Mode: ${result.calculationMode.title}")
            appendLine("Principal Amount: $principalFormatted")
            appendLine("Annual Interest Rate: ${result.annualRate}%")
            if (result.calculationMode == CalculationMode.DATE_RANGE && result.startDate != null && result.endDate != null) {
                appendLine("Start Date: ${result.startDate.format(dateFormatter)}")
                appendLine("End Date: ${result.endDate.format(dateFormatter)}")
            }
            if (result.borrowDate != null && result.payingDate != null) {
                appendLine("Borrow Date: ${result.borrowDate.format(dateFormatter)}")
                appendLine("Agreed Duration: ${result.agreedDurationDisplay}")
                if (result.agreedDueDate != null) {
                    appendLine("Promised Due Date: ${result.agreedDueDate.format(dateFormatter)}")
                }
                appendLine("Paying Date: ${result.payingDate.format(dateFormatter)}")
                appendLine("Status: ${result.differenceDisplay}")
                if (result.actualAgreedAmount != null) {
                    appendLine("Actual Amount (on Due Date): ${InterestCalculatorEngine.formatIndianCurrency(result.actualAgreedAmount)}")
                }
                if (result.isOverdue && result.amountAfterOverdue != null) {
                    val combinedBase = result.combinedOverduePrincipal ?: result.actualAgreedAmount ?: java.math.BigDecimal.ZERO
                    appendLine("Combined Overdue Base: ${InterestCalculatorEngine.formatIndianCurrency(combinedBase)}")
                    appendLine("Overdue Delay Interest: ${InterestCalculatorEngine.formatIndianCurrency(result.overdueInterest ?: java.math.BigDecimal.ZERO)}")
                    appendLine("Amount After Overdue Date: ${InterestCalculatorEngine.formatIndianCurrency(result.amountAfterOverdue)}")
                }
            }
            appendLine("Total Duration: ${result.totalDays} days (${result.yearsMonthsDaysDisplay})")
            appendLine("Total Interest: $interestFormatted")
            appendLine("Final Payable Amount: $totalAmountFormatted")
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Interest Calculation", summaryText))
        Toast.makeText(context, "Calculation summary copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun shareSummary() {
        val summaryText = buildString {
            appendLine("=== Interest Calculation Summary ===")
            appendLine("Interest Type: ${result.interestType.displayName}")
            appendLine("Principal: $principalFormatted")
            appendLine("Annual Rate: ${result.annualRate}%")
            if (result.startDate != null && result.endDate != null) {
                appendLine("Duration: ${result.startDate.format(dateFormatter)} to ${result.endDate.format(dateFormatter)}")
            }
            if (result.borrowDate != null && result.payingDate != null) {
                appendLine("Borrow Date: ${result.borrowDate.format(dateFormatter)}")
                appendLine("Agreed Duration: ${result.agreedDurationDisplay}")
                if (result.agreedDueDate != null) {
                    appendLine("Promised Due Date: ${result.agreedDueDate.format(dateFormatter)}")
                }
                appendLine("Paying Date: ${result.payingDate.format(dateFormatter)}")
                appendLine("Status: ${result.differenceDisplay}")
                if (result.actualAgreedAmount != null) {
                    appendLine("Actual Amount (on Due Date): ${InterestCalculatorEngine.formatIndianCurrency(result.actualAgreedAmount)}")
                }
                if (result.isOverdue && result.amountAfterOverdue != null) {
                    val combinedBase = result.combinedOverduePrincipal ?: result.actualAgreedAmount ?: java.math.BigDecimal.ZERO
                    appendLine("Combined Overdue Base: ${InterestCalculatorEngine.formatIndianCurrency(combinedBase)}")
                    appendLine("Overdue Delay Interest: ${InterestCalculatorEngine.formatIndianCurrency(result.overdueInterest ?: java.math.BigDecimal.ZERO)}")
                    appendLine("Amount After Overdue Date: ${InterestCalculatorEngine.formatIndianCurrency(result.amountAfterOverdue)}")
                }
            }
            appendLine("Total Days: ${result.totalDays} days")
            appendLine("Total Interest: $interestFormatted")
            appendLine("Final Payable Amount: $totalAmountFormatted")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Interest Calculation Result")
            putExtra(Intent.EXTRA_TEXT, summaryText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Calculation Result"))
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Calculation Results",
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

            // Prominent Highlight Cards: Total Amount / Amount After Overdue Date & Actual Amount on Due Date
            if (result.isOverdue && result.amountAfterOverdue != null && result.actualAgreedAmount != null) {
                // Overdue Total Amount Card
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(22.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "AMOUNT AFTER OVERDUE DATE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f),
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = totalAmountFormatted,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Compounded Overdue Total (Payable on Repayment Date)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actual Amount on Agreed Due Date Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACTUAL AMOUNT (ON DUE DATE)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = InterestCalculatorEngine.formatIndianCurrency(result.actualAgreedAmount),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Principal ($principalFormatted) + Agreed Interest (${InterestCalculatorEngine.formatIndianCurrency(result.agreedInterest ?: java.math.BigDecimal.ZERO)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Compounding explanation banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                        .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    val bannerBase = result.combinedOverduePrincipal ?: result.actualAgreedAmount
                    Text(
                        text = "ℹ️ Compounding Rule: Principal and Agreed Interest combined into ${InterestCalculatorEngine.formatIndianCurrency(bannerBase)} at due date for overdue calculation.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            } else {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "FINAL AMOUNT",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = totalAmountFormatted,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (result.actualAgreedAmount != null) {
                                "Actual Amount at Due Date: ${InterestCalculatorEngine.formatIndianCurrency(result.actualAgreedAmount)}"
                            } else {
                                "Principal ($principalFormatted) + Interest ($interestFormatted)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prominent Total Interest Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "TOTAL INTEREST EARNED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.85f),
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = interestFormatted,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }

                    Icon(
                        imageVector = if (result.interestType == InterestType.SIMPLE) Icons.Default.Calculate else Icons.Default.Timeline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Formula Card for Simple Interest: (P * T * R) / 100
            if (result.interestType == InterestType.SIMPLE) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "FORMULA USED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Simple Interest = (P × T × R) / 100",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "= ($principalFormatted × ${result.totalMonthsFraction.setScale(2, java.math.RoundingMode.HALF_UP)} months × ${result.monthlyRate}%) / 100",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "= $interestFormatted",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Detailed Breakdown Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Calculation Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row {
                            IconButton(onClick = { copyToClipboard() }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Summary",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { shareSummary() }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    DetailRow(label = "Principal (P)", value = principalFormatted)
                    DetailRow(label = "Entered Rate", value = "${result.enteredRate}% (${result.rateType.displayName})")
                    DetailRow(
                        label = "Monthly Rate (R)",
                        value = "${result.monthlyRate}% / mo",
                        subtitle = if (result.rateType == com.interestcalculator.app.data.RateType.PER_YEAR) "(${result.annualRate}% ÷ 12)" else null
                    )
                    DetailRow(label = "Interest Type", value = result.interestType.displayName)
                    DetailRow(label = "Calculation Mode", value = result.calculationMode.title)

                    if (result.calculationMode == CalculationMode.DATE_RANGE && result.startDate != null && result.endDate != null) {
                        DetailRow(label = "Start Date", value = result.startDate.format(dateFormatter))
                        DetailRow(label = "End Date", value = result.endDate.format(dateFormatter))
                    }

                    if (result.borrowDate != null && result.payingDate != null) {
                        DetailRow(label = "Borrow Date (Loan Taken)", value = result.borrowDate.format(dateFormatter))
                        DetailRow(label = "Agreed Return Duration", value = result.agreedDurationDisplay)
                        if (result.agreedDueDate != null) {
                            DetailRow(label = "Promised Return Date", value = result.agreedDueDate.format(dateFormatter))
                        }
                        DetailRow(label = "Paying Date (Repayment)", value = result.payingDate.format(dateFormatter))
                        DetailRow(
                            label = "Settlement Status",
                            value = result.differenceDisplay,
                            isBold = true,
                            valueColor = if (result.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        if (result.actualAgreedAmount != null) {
                            DetailRow(
                                label = "Actual Amount on Due Date",
                                value = InterestCalculatorEngine.formatIndianCurrency(result.actualAgreedAmount),
                                subtitle = "Principal ($principalFormatted) + Agreed Interest (${InterestCalculatorEngine.formatIndianCurrency(result.agreedInterest ?: java.math.BigDecimal.ZERO)})",
                                isBold = true
                            )
                        }
                        if (result.isOverdue && result.combinedOverduePrincipal != null && result.overdueInterest != null) {
                            DetailRow(
                                label = "Combined Base for Overdue",
                                value = InterestCalculatorEngine.formatIndianCurrency(result.combinedOverduePrincipal),
                                subtitle = "Principal & Interest combined at due date"
                            )
                            DetailRow(
                                label = "Overdue Delay Interest (Compounded)",
                                value = InterestCalculatorEngine.formatIndianCurrency(result.overdueInterest),
                                valueColor = MaterialTheme.colorScheme.error,
                                isBold = true
                            )
                            if (result.amountAfterOverdue != null) {
                                DetailRow(
                                    label = "Amount After Overdue Date",
                                    value = InterestCalculatorEngine.formatIndianCurrency(result.amountAfterOverdue),
                                    valueColor = MaterialTheme.colorScheme.error,
                                    isBold = true
                                )
                            }
                        }
                    }

                    DetailRow(
                        label = "Total Days",
                        value = "${result.totalDays} Days"
                    )
                    DetailRow(
                        label = "Time in Months (T)",
                        value = "${result.fullMonths} Months, ${result.remainingDays} Days",
                        subtitle = "${result.totalMonthsFraction.setScale(2, java.math.RoundingMode.HALF_UP)} months total"
                    )
                    DetailRow(
                        label = "Interest per Month",
                        value = InterestCalculatorEngine.formatIndianCurrency(result.monthlyInterestAmount)
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    DetailRow(
                        label = "Total Interest",
                        value = interestFormatted,
                        isBold = true,
                        valueColor = MaterialTheme.colorScheme.secondary
                    )
                    DetailRow(
                        label = "Final Amount",
                        value = totalAmountFormatted,
                        isBold = true,
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Optional: Compound Interest Annual Breakdown Table (if compounded)
            if (result.interestType == InterestType.COMPOUND && result.breakdownYears.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Annual Compounding Schedule",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { showBreakdown = !showBreakdown }) {
                                Icon(
                                    imageVector = if (showBreakdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Toggle schedule"
                                )
                            }
                        }

                        AnimatedVisibility(visible = showBreakdown) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                result.breakdownYears.forEach { item ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = item.periodLabel,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Opening: ${InterestCalculatorEngine.formatIndianCurrency(item.openingBalance)}",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "+ Interest: ${InterestCalculatorEngine.formatIndianCurrency(item.interestEarned)}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                            Text(
                                                text = "Closing: ${InterestCalculatorEngine.formatIndianCurrency(item.closingBalance)}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Navigation Buttons: Recalculate & Home
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onGoHome,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Home", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onRecalculate,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recalculate", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    subtitle: String? = null,
    isBold: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
                color = valueColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun EmptyResultsView(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Results",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No results calculated yet.", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateBack) {
                Text("Go to Calculator")
            }
        }
    }
}
