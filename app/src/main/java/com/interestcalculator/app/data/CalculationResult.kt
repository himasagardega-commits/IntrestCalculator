package com.interestcalculator.app.data

import java.math.BigDecimal
import java.time.LocalDate

enum class RateType(val displayName: String, val shortSuffix: String) {
    PER_MONTH("% / Month (₹ per 100)", "% / mo"),
    PER_YEAR("% / Year (Annual p.a.)", "% / yr")
}

data class CompoundingYearBreakdown(
    val yearNumber: Int,
    val periodLabel: String,
    val openingBalance: BigDecimal,
    val interestEarned: BigDecimal,
    val closingBalance: BigDecimal
)

data class CalculationResult(
    val principal: BigDecimal,
    val enteredRate: BigDecimal = BigDecimal.ZERO,
    val rateType: RateType = RateType.PER_MONTH,
    val annualRate: BigDecimal,
    val monthlyRate: BigDecimal = BigDecimal.ZERO,
    val totalDays: Long,
    val fullMonths: Long = 0L,
    val remainingDays: Long = 0L,
    val totalMonthsFraction: BigDecimal = BigDecimal.ZERO,
    val interestType: InterestType,
    val calculationMode: CalculationMode,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val monthlyInterestAmount: BigDecimal = BigDecimal.ZERO,
    val totalInterest: BigDecimal,
    val totalAmount: BigDecimal,
    val breakdownYears: List<CompoundingYearBreakdown> = emptyList(),
    val yearsMonthsDaysDisplay: String = "",
    val monthsDisplay: String = "",
    val formulaDisplay: String = "",
    val borrowDate: LocalDate? = null,
    val payingDate: LocalDate? = null,
    val agreedYears: Long = 0L,
    val agreedMonths: Long = 0L,
    val agreedDays: Long = 0L,
    val agreedTotalDays: Long = 0L,
    val agreedDueDate: LocalDate? = null,
    val agreedDurationDisplay: String = "",
    val paytimeDurationDisplay: String = "",
    val isOverdue: Boolean = false,
    val differenceDisplay: String = "",
    val agreedInterest: BigDecimal? = null,
    val overdueInterest: BigDecimal? = null,
    val actualAgreedAmount: BigDecimal? = null,
    val combinedOverduePrincipal: BigDecimal? = null,
    val amountAfterOverdue: BigDecimal? = null
)
