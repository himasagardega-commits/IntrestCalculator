package com.interestcalculator.app.data

import java.math.BigDecimal
import java.time.LocalDate

data class CompoundingYearBreakdown(
    val yearNumber: Int,
    val periodLabel: String,
    val openingBalance: BigDecimal,
    val interestEarned: BigDecimal,
    val closingBalance: BigDecimal
)

data class CalculationResult(
    val principal: BigDecimal,
    val annualRate: BigDecimal,
    val totalDays: Long,
    val interestType: InterestType,
    val calculationMode: CalculationMode,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val totalInterest: BigDecimal,
    val totalAmount: BigDecimal,
    val breakdownYears: List<CompoundingYearBreakdown> = emptyList(),
    val yearsMonthsDaysDisplay: String = ""
)
