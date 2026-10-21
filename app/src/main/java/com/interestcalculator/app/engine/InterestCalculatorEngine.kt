package com.interestcalculator.app.engine

import com.interestcalculator.app.data.CalculationMode
import com.interestcalculator.app.data.CalculationResult
import com.interestcalculator.app.data.CompoundingYearBreakdown
import com.interestcalculator.app.data.InterestType
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

object InterestCalculatorEngine {

    private val MATH_CONTEXT = MathContext.DECIMAL128
    private val HUNDRED = BigDecimal("100")
    private val DAYS_IN_YEAR = BigDecimal("365")

    /**
     * Calculates the actual number of elapsed calendar days between start and end dates.
     * The start date is excluded and the end date is included (standard date subtraction).
     * E.g. Jan 1 to Jan 2 is 1 day. Jan 1 to Jan 1 is 0 days.
     */
    fun calculateElapsedDays(startDate: LocalDate, endDate: LocalDate): Long {
        require(!endDate.isBefore(startDate)) {
            "End date cannot be earlier than start date"
        }
        return ChronoUnit.DAYS.between(startDate, endDate)
    }

    /**
     * Converts specified years, months, and days into standard calculation days.
     * Conversion rules:
     * 1 year = 365 days
     * 1 month = 30 days
     * Days remain unchanged.
     */
    fun convertPeriodToDays(years: Long, months: Long, days: Long): Long {
        require(years >= 0) { "Years cannot be negative" }
        require(months >= 0) { "Months cannot be negative" }
        require(days >= 0) { "Days cannot be negative" }
        return (years * 365L) + (months * 30L) + days
    }

    /**
     * Calculates Simple Interest:
     * Interest = Principal * Annual Rate * Number of Days / (100 * 365)
     * Principal remains unchanged throughout the entire duration.
     */
    fun calculateSimpleInterest(
        principal: BigDecimal,
        annualRate: BigDecimal,
        totalDays: Long,
        mode: CalculationMode = CalculationMode.DATE_RANGE,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): CalculationResult {
        validateInputs(principal, annualRate, totalDays)

        val principal2Dec = principal.setScale(2, RoundingMode.HALF_UP)

        if (totalDays == 0L || annualRate.compareTo(BigDecimal.ZERO) == 0) {
            val zeroInterest = BigDecimal("0.00")
            return CalculationResult(
                principal = principal2Dec,
                annualRate = annualRate,
                totalDays = totalDays,
                interestType = InterestType.SIMPLE,
                calculationMode = mode,
                startDate = startDate,
                endDate = endDate,
                totalInterest = zeroInterest,
                totalAmount = principal2Dec,
                yearsMonthsDaysDisplay = formatDaysBreakdown(totalDays)
            )
        }

        val rateFraction = annualRate.divide(HUNDRED, MATH_CONTEXT)
        val daysFraction = BigDecimal(totalDays).divide(DAYS_IN_YEAR, MATH_CONTEXT)
        val rawInterest = principal.multiply(rateFraction, MATH_CONTEXT).multiply(daysFraction, MATH_CONTEXT)

        val totalInterest = rawInterest.setScale(2, RoundingMode.HALF_UP)
        val totalAmount = principal2Dec.add(totalInterest)

        return CalculationResult(
            principal = principal2Dec,
            annualRate = annualRate,
            totalDays = totalDays,
            interestType = InterestType.SIMPLE,
            calculationMode = mode,
            startDate = startDate,
            endDate = endDate,
            totalInterest = totalInterest,
            totalAmount = totalAmount,
            yearsMonthsDaysDisplay = formatDaysBreakdown(totalDays)
        )
    }

    /**
     * Calculates Compound Interest with annual compounding:
     * - Every complete 365-day period adds interest earned to current balance.
     * - Proportional interest calculated for remaining days on updated balance.
     */
    fun calculateCompoundInterest(
        principal: BigDecimal,
        annualRate: BigDecimal,
        totalDays: Long,
        mode: CalculationMode = CalculationMode.DATE_RANGE,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): CalculationResult {
        validateInputs(principal, annualRate, totalDays)

        val principal2Dec = principal.setScale(2, RoundingMode.HALF_UP)

        if (totalDays == 0L || annualRate.compareTo(BigDecimal.ZERO) == 0) {
            val zeroInterest = BigDecimal("0.00")
            return CalculationResult(
                principal = principal2Dec,
                annualRate = annualRate,
                totalDays = totalDays,
                interestType = InterestType.COMPOUND,
                calculationMode = mode,
                startDate = startDate,
                endDate = endDate,
                totalInterest = zeroInterest,
                totalAmount = principal2Dec,
                yearsMonthsDaysDisplay = formatDaysBreakdown(totalDays)
            )
        }

        val fullYears = totalDays / 365L
        val remainingDays = totalDays % 365L
        val rateFraction = annualRate.divide(HUNDRED, MATH_CONTEXT)

        var balance = principal
        val breakdowns = mutableListOf<CompoundingYearBreakdown>()

        for (i in 1..fullYears) {
            val opening = balance
            val yearInterest = balance.multiply(rateFraction, MATH_CONTEXT)
            balance = balance.add(yearInterest, MATH_CONTEXT)
            breakdowns.add(
                CompoundingYearBreakdown(
                    yearNumber = i.toInt(),
                    periodLabel = "Year $i (365 days)",
                    openingBalance = opening.setScale(2, RoundingMode.HALF_UP),
                    interestEarned = yearInterest.setScale(2, RoundingMode.HALF_UP),
                    closingBalance = balance.setScale(2, RoundingMode.HALF_UP)
                )
            )
        }

        if (remainingDays > 0L) {
            val opening = balance
            val remFraction = BigDecimal(remainingDays).divide(DAYS_IN_YEAR, MATH_CONTEXT)
            val remInterest = balance.multiply(rateFraction, MATH_CONTEXT).multiply(remFraction, MATH_CONTEXT)
            balance = balance.add(remInterest, MATH_CONTEXT)
            breakdowns.add(
                CompoundingYearBreakdown(
                    yearNumber = (fullYears + 1).toInt(),
                    periodLabel = if (fullYears == 0L) "$remainingDays days" else "Partial Year ($remainingDays days)",
                    openingBalance = opening.setScale(2, RoundingMode.HALF_UP),
                    interestEarned = remInterest.setScale(2, RoundingMode.HALF_UP),
                    closingBalance = balance.setScale(2, RoundingMode.HALF_UP)
                )
            )
        }

        val rawInterest = balance.subtract(principal, MATH_CONTEXT)
        val totalInterest = rawInterest.setScale(2, RoundingMode.HALF_UP)
        val totalAmount = principal2Dec.add(totalInterest)

        return CalculationResult(
            principal = principal2Dec,
            annualRate = annualRate,
            totalDays = totalDays,
            interestType = InterestType.COMPOUND,
            calculationMode = mode,
            startDate = startDate,
            endDate = endDate,
            totalInterest = totalInterest,
            totalAmount = totalAmount,
            breakdownYears = breakdowns,
            yearsMonthsDaysDisplay = formatDaysBreakdown(totalDays)
        )
    }

    /**
     * Formats total days into years, months, and remaining days for readability.
     */
    fun formatDaysBreakdown(totalDays: Long): String {
        if (totalDays <= 0L) return "0 days"

        val years = totalDays / 365L
        val remAfterYears = totalDays % 365L
        val months = remAfterYears / 30L
        val days = remAfterYears % 30L

        val parts = mutableListOf<String>()
        if (years > 0) parts.add("$years ${if (years == 1L) "year" else "years"}")
        if (months > 0) parts.add("$months ${if (months == 1L) "month" else "months"}")
        if (days > 0 || parts.isEmpty()) parts.add("$days ${if (days == 1L) "day" else "days"}")

        return "${parts.joinToString(", ")} ($totalDays days)"
    }

    /**
     * Formats monetary values in Indian Rupees with Indian comma grouping (lakhs, crores).
     * E.g. ₹10,000.00, ₹1,00,000.00, ₹12,34,567.89
     */
    fun formatIndianCurrency(amount: BigDecimal): String {
        val rounded = amount.setScale(2, RoundingMode.HALF_UP)
        val isNegative = rounded.signum() < 0
        val absVal = rounded.abs()

        val plainStr = absVal.toPlainString()
        val dotIndex = plainStr.indexOf('.')
        val integerPart = if (dotIndex != -1) plainStr.substring(0, dotIndex) else plainStr
        val fractionPart = if (dotIndex != -1) plainStr.substring(dotIndex + 1) else "00"

        val formattedInt = formatIndianIntegerGrouping(integerPart)
        val signPrefix = if (isNegative) "-₹" else "₹"

        return "$signPrefix$formattedInt.$fractionPart"
    }

    private fun formatIndianIntegerGrouping(digits: String): String {
        if (digits.length <= 3) return digits

        val lastThree = digits.substring(digits.length - 3)
        var remaining = digits.substring(0, digits.length - 3)

        val groups = mutableListOf<String>()
        while (remaining.isNotEmpty()) {
            if (remaining.length > 2) {
                groups.add(0, remaining.substring(remaining.length - 2))
                remaining = remaining.substring(0, remaining.length - 2)
            } else {
                groups.add(0, remaining)
                remaining = ""
            }
        }
        groups.add(lastThree)
        return groups.joinToString(",")
    }

    private fun validateInputs(principal: BigDecimal, annualRate: BigDecimal, totalDays: Long) {
        require(principal > BigDecimal.ZERO) { "Principal must be greater than zero" }
        require(annualRate >= BigDecimal.ZERO) { "Interest rate cannot be negative" }
        require(totalDays >= 0L) { "Duration cannot be negative" }
    }
}
