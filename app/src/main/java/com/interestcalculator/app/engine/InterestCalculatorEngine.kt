package com.interestcalculator.app.engine

import com.interestcalculator.app.data.CalculationMode
import com.interestcalculator.app.data.CalculationResult
import com.interestcalculator.app.data.CompoundingYearBreakdown
import com.interestcalculator.app.data.InterestType
import com.interestcalculator.app.data.RateType
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object InterestCalculatorEngine {

    private val MATH_CONTEXT = MathContext.DECIMAL128
    private val HUNDRED = BigDecimal("100")
    private val TWELVE = BigDecimal("12")
    private val THIRTY = BigDecimal("30")

    /**
     * Calculates the actual number of elapsed calendar days between start and end dates.
     * The start date is excluded and the end date is included (standard date subtraction).
     */
    fun calculateElapsedDays(startDate: LocalDate, endDate: LocalDate): Long {
        require(!endDate.isBefore(startDate)) {
            "End date cannot be earlier than start date"
        }
        return ChronoUnit.DAYS.between(startDate, endDate)
    }

    /**
     * Converts specified years, months, and days into standard calculation days.
     * 1 year = 365 days (or 12 months * 30 days = 360 days)
     * 1 month = 30 days
     */
    fun convertPeriodToDays(years: Long, months: Long, days: Long): Long {
        require(years >= 0) { "Years cannot be negative" }
        require(months >= 0) { "Months cannot be negative" }
        require(days >= 0) { "Days cannot be negative" }
        return (years * 365L) + (months * 30L) + days
    }

    /**
     * Converts total days into full months, remaining days, and fractional months.
     * 30 days = 1 month, 12 months = 1 year.
     */
    fun convertDaysToMonths(totalDays: Long): MonthConversion {
        val fullMonths = totalDays / 30L
        val remainingDays = totalDays % 30L
        val years = fullMonths / 12L
        val monthsInYear = fullMonths % 12L

        val remFraction = if (remainingDays > 0) {
            BigDecimal(remainingDays).divide(THIRTY, MATH_CONTEXT)
        } else {
            BigDecimal.ZERO
        }
        val totalMonthsFraction = BigDecimal(fullMonths).add(remFraction, MATH_CONTEXT)

        val monthsDisplay = if (remainingDays > 0) {
            "$fullMonths months, $remainingDays days (${totalMonthsFraction.setScale(2, RoundingMode.HALF_UP)} months)"
        } else {
            "$fullMonths months"
        }

        return MonthConversion(
            totalDays = totalDays,
            fullMonths = fullMonths,
            remainingDays = remainingDays,
            years = years,
            monthsInYear = monthsInYear,
            totalMonthsFraction = totalMonthsFraction,
            monthsDisplay = monthsDisplay
        )
    }

    /**
     * Exact Simple Interest formula requested by user:
     * def simple_interest(p, t, r): return (p * t * r) / 100
     *
     * @param p Principal amount
     * @param t Time in months (or fractional months converted from days)
     * @param r Rate of interest per month
     */
    fun simpleInterest(p: BigDecimal, t: BigDecimal, r: BigDecimal): BigDecimal {
        return p.multiply(t, MATH_CONTEXT).multiply(r, MATH_CONTEXT).divide(HUNDRED, MATH_CONTEXT)
    }

    /**
     * Calculates Simple Interest using (P * T * R) / 100:
     * - Days are converted to months (30 days = 1 month).
     * - T = Time in months (Full months + remainingDays / 30).
     * - R = Rate per month (If entered as annual rate, R = annualRate / 12).
     * - Formula: Simple Interest = (P * T * R) / 100
     */
    fun calculateSimpleInterest(
        principal: BigDecimal,
        rate: BigDecimal,
        totalDays: Long,
        rateType: RateType = RateType.PER_YEAR,
        mode: CalculationMode = CalculationMode.DATE_RANGE,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): CalculationResult {
        validateInputs(principal, rate, totalDays)

        val principal2Dec = principal.setScale(2, RoundingMode.HALF_UP)
        val monthData = convertDaysToMonths(totalDays)

        val monthlyRate: BigDecimal
        val annualRate: BigDecimal

        if (rateType == RateType.PER_MONTH) {
            monthlyRate = rate.setScale(4, RoundingMode.HALF_UP)
            annualRate = rate.multiply(TWELVE, MATH_CONTEXT).setScale(2, RoundingMode.HALF_UP)
        } else {
            annualRate = rate.setScale(2, RoundingMode.HALF_UP)
            monthlyRate = rate.divide(TWELVE, MATH_CONTEXT).setScale(4, RoundingMode.HALF_UP)
        }

        val monthlyInterestAmount = principal2Dec.multiply(monthlyRate).divide(HUNDRED, MATH_CONTEXT).setScale(2, RoundingMode.HALF_UP)

        if (totalDays == 0L || rate.compareTo(BigDecimal.ZERO) == 0) {
            val zeroInterest = BigDecimal("0.00")
            return CalculationResult(
                principal = principal2Dec,
                enteredRate = rate,
                rateType = rateType,
                annualRate = annualRate,
                monthlyRate = monthlyRate,
                totalDays = totalDays,
                fullMonths = monthData.fullMonths,
                remainingDays = monthData.remainingDays,
                totalMonthsFraction = monthData.totalMonthsFraction,
                interestType = InterestType.SIMPLE,
                calculationMode = mode,
                startDate = startDate,
                endDate = endDate,
                monthlyInterestAmount = zeroInterest,
                totalInterest = zeroInterest,
                totalAmount = principal2Dec,
                yearsMonthsDaysDisplay = formatDaysBreakdown(totalDays),
                monthsDisplay = monthData.monthsDisplay,
                formulaDisplay = "(P × T × R) / 100 = 0.00"
            )
        }

        // Exact formula: (p * t * r) / 100
        val rawInterest = simpleInterest(principal, monthData.totalMonthsFraction, monthlyRate)
        val totalInterest = rawInterest.setScale(2, RoundingMode.HALF_UP)
        val totalAmount = principal2Dec.add(totalInterest)
        val formulaDisplay = "($principal2Dec × ${monthData.totalMonthsFraction.setScale(2, RoundingMode.HALF_UP)} mo × $monthlyRate%) / 100 = ₹$totalInterest"

        return CalculationResult(
            principal = principal2Dec,
            enteredRate = rate,
            rateType = rateType,
            annualRate = annualRate,
            monthlyRate = monthlyRate,
            totalDays = totalDays,
            fullMonths = monthData.fullMonths,
            remainingDays = monthData.remainingDays,
            totalMonthsFraction = monthData.totalMonthsFraction,
            interestType = InterestType.SIMPLE,
            calculationMode = mode,
            startDate = startDate,
            endDate = endDate,
            monthlyInterestAmount = monthlyInterestAmount,
            totalInterest = totalInterest,
            totalAmount = totalAmount,
            yearsMonthsDaysDisplay = formatDaysBreakdown(totalDays),
            monthsDisplay = monthData.monthsDisplay,
            formulaDisplay = formulaDisplay
        )
    }

    /**
     * Calculates Compound Interest using MONTHS with annual compounding (every 12 months):
     * - Every 12 months (1 complete year), interest is compounded and added to principal.
     * - For remaining months (< 12) and days, proportional monthly interest is applied.
     */
    fun calculateCompoundInterest(
        principal: BigDecimal,
        rate: BigDecimal,
        totalDays: Long,
        rateType: RateType = RateType.PER_YEAR,
        mode: CalculationMode = CalculationMode.DATE_RANGE,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): CalculationResult {
        validateInputs(principal, rate, totalDays)

        val principal2Dec = principal.setScale(2, RoundingMode.HALF_UP)
        val monthData = convertDaysToMonths(totalDays)

        val monthlyRate: BigDecimal
        val annualRate: BigDecimal

        if (rateType == RateType.PER_MONTH) {
            monthlyRate = rate.setScale(4, RoundingMode.HALF_UP)
            annualRate = rate.multiply(TWELVE, MATH_CONTEXT).setScale(2, RoundingMode.HALF_UP)
        } else {
            annualRate = rate.setScale(2, RoundingMode.HALF_UP)
            monthlyRate = rate.divide(TWELVE, MATH_CONTEXT).setScale(4, RoundingMode.HALF_UP)
        }

        val monthlyInterestAmount = principal2Dec.multiply(monthlyRate).divide(HUNDRED, MATH_CONTEXT).setScale(2, RoundingMode.HALF_UP)

        if (totalDays == 0L || rate.compareTo(BigDecimal.ZERO) == 0) {
            val zeroInterest = BigDecimal("0.00")
            return CalculationResult(
                principal = principal2Dec,
                enteredRate = rate,
                rateType = rateType,
                annualRate = annualRate,
                monthlyRate = monthlyRate,
                totalDays = totalDays,
                fullMonths = monthData.fullMonths,
                remainingDays = monthData.remainingDays,
                totalMonthsFraction = monthData.totalMonthsFraction,
                interestType = InterestType.COMPOUND,
                calculationMode = mode,
                startDate = startDate,
                endDate = endDate,
                monthlyInterestAmount = zeroInterest,
                totalInterest = zeroInterest,
                totalAmount = principal2Dec,
                yearsMonthsDaysDisplay = formatDaysBreakdown(totalDays),
                monthsDisplay = monthData.monthsDisplay
            )
        }

        val full12MonthYears = monthData.years
        val remMonthsInYear = monthData.monthsInYear
        val remDays = monthData.remainingDays

        val annualRateFraction = annualRate.divide(HUNDRED, MATH_CONTEXT)
        val monthlyRateFraction = monthlyRate.divide(HUNDRED, MATH_CONTEXT)

        var balance = principal
        val breakdowns = mutableListOf<CompoundingYearBreakdown>()

        for (i in 1..full12MonthYears) {
            val opening = balance
            val yearInterest = balance.multiply(annualRateFraction, MATH_CONTEXT)
            balance = balance.add(yearInterest, MATH_CONTEXT)
            breakdowns.add(
                CompoundingYearBreakdown(
                    yearNumber = i.toInt(),
                    periodLabel = "Year $i (12 months / 360 days)",
                    openingBalance = opening.setScale(2, RoundingMode.HALF_UP),
                    interestEarned = yearInterest.setScale(2, RoundingMode.HALF_UP),
                    closingBalance = balance.setScale(2, RoundingMode.HALF_UP)
                )
            )
        }

        // Partial period (remaining months and days)
        if (remMonthsInYear > 0L || remDays > 0L) {
            val opening = balance
            val remFraction = BigDecimal(remMonthsInYear).add(
                BigDecimal(remDays).divide(THIRTY, MATH_CONTEXT),
                MATH_CONTEXT
            )
            val partialInterest = balance.multiply(monthlyRateFraction, MATH_CONTEXT).multiply(remFraction, MATH_CONTEXT)
            balance = balance.add(partialInterest, MATH_CONTEXT)

            val partialLabel = buildString {
                if (full12MonthYears > 0L) append("Partial: ")
                if (remMonthsInYear > 0L) append("$remMonthsInYear mo")
                if (remMonthsInYear > 0L && remDays > 0L) append(", ")
                if (remDays > 0L) append("$remDays days")
            }

            breakdowns.add(
                CompoundingYearBreakdown(
                    yearNumber = (full12MonthYears + 1).toInt(),
                    periodLabel = partialLabel,
                    openingBalance = opening.setScale(2, RoundingMode.HALF_UP),
                    interestEarned = partialInterest.setScale(2, RoundingMode.HALF_UP),
                    closingBalance = balance.setScale(2, RoundingMode.HALF_UP)
                )
            )
        }

        val rawInterest = balance.subtract(principal, MATH_CONTEXT)
        val totalInterest = rawInterest.setScale(2, RoundingMode.HALF_UP)
        val totalAmount = principal2Dec.add(totalInterest)

        return CalculationResult(
            principal = principal2Dec,
            enteredRate = rate,
            rateType = rateType,
            annualRate = annualRate,
            monthlyRate = monthlyRate,
            totalDays = totalDays,
            fullMonths = monthData.fullMonths,
            remainingDays = monthData.remainingDays,
            totalMonthsFraction = monthData.totalMonthsFraction,
            interestType = InterestType.COMPOUND,
            calculationMode = mode,
            startDate = startDate,
            endDate = endDate,
            monthlyInterestAmount = monthlyInterestAmount,
            totalInterest = totalInterest,
            totalAmount = totalAmount,
            breakdownYears = breakdowns,
            yearsMonthsDaysDisplay = formatDaysBreakdown(totalDays),
            monthsDisplay = monthData.monthsDisplay
        )
    }

    /**
     * Calculates interest for Special Time Period mode:
     * - Initial agreement duration (agreedYears, agreedMonths, agreedDays) set by user
     *   represents how many days/months/years the borrower agreed to return.
     * - Agreed Due Date = borrowDate + agreed duration.
     * - On paytime, the actual time duration is calculated using borrowDate and payingDate.
     * - Interest is calculated on the actual paytime duration using (P * T * R) / 100.
     * - Provides agreed vs overdue breakdown.
     */
    fun calculateSpecialPeriodInterest(
        principal: BigDecimal,
        rate: BigDecimal,
        rateType: RateType = RateType.PER_MONTH,
        interestType: InterestType = InterestType.SIMPLE,
        borrowDate: LocalDate,
        payingDate: LocalDate,
        agreedYears: Long,
        agreedMonths: Long,
        agreedDays: Long
    ): CalculationResult {
        validateInputs(principal, rate, 0L)
        require(!payingDate.isBefore(borrowDate)) { "Paying date cannot be earlier than borrow date" }

        val principal2Dec = principal.setScale(2, RoundingMode.HALF_UP)
        val paytimeDays = calculateElapsedDays(borrowDate, payingDate)
        val agreedTotalDays = convertPeriodToDays(agreedYears, agreedMonths, agreedDays)
        val agreedDueDate = borrowDate.plusYears(agreedYears).plusMonths(agreedMonths).plusDays(agreedDays)

        val monthData = convertDaysToMonths(paytimeDays)
        val agreedMonthData = convertDaysToMonths(agreedTotalDays)

        val monthlyRate: BigDecimal
        val annualRate: BigDecimal
        if (rateType == RateType.PER_MONTH) {
            monthlyRate = rate.setScale(4, RoundingMode.HALF_UP)
            annualRate = rate.multiply(TWELVE, MATH_CONTEXT).setScale(2, RoundingMode.HALF_UP)
        } else {
            annualRate = rate.setScale(2, RoundingMode.HALF_UP)
            monthlyRate = rate.divide(TWELVE, MATH_CONTEXT).setScale(4, RoundingMode.HALF_UP)
        }

        val monthlyInterestAmount = principal2Dec.multiply(monthlyRate).divide(HUNDRED, MATH_CONTEXT).setScale(2, RoundingMode.HALF_UP)

        val agreedDurationDisplay = buildString {
            val parts = mutableListOf<String>()
            if (agreedYears > 0) parts.add("$agreedYears yr")
            if (agreedMonths > 0) parts.add("$agreedMonths mo")
            if (agreedDays > 0 || parts.isEmpty()) parts.add("$agreedDays d")
            append(parts.joinToString(", "))
            append(" ($agreedTotalDays days)")
        }

        val paytimeDurationDisplay = formatDaysBreakdown(paytimeDays)

        val isOverdue = payingDate.isAfter(agreedDueDate)
        val differenceDisplay: String
        val agreedInterest: BigDecimal
        val overdueInterest: BigDecimal

        if (isOverdue) {
            val overdueDays = ChronoUnit.DAYS.between(agreedDueDate, payingDate)
            val overdueMonthData = convertDaysToMonths(overdueDays)
            differenceDisplay = "Overdue by ${overdueMonthData.fullMonths} mo, ${overdueMonthData.remainingDays} d ($overdueDays days late)"
            agreedInterest = simpleInterest(principal, agreedMonthData.totalMonthsFraction, monthlyRate).setScale(2, RoundingMode.HALF_UP)
            val rawOverdue = simpleInterest(principal, overdueMonthData.totalMonthsFraction, monthlyRate).setScale(2, RoundingMode.HALF_UP)
            overdueInterest = rawOverdue
        } else if (payingDate.isEqual(agreedDueDate)) {
            differenceDisplay = "Paid exactly on agreed due date"
            val total = simpleInterest(principal, monthData.totalMonthsFraction, monthlyRate).setScale(2, RoundingMode.HALF_UP)
            agreedInterest = total
            overdueInterest = BigDecimal("0.00")
        } else {
            val earlyDays = ChronoUnit.DAYS.between(payingDate, agreedDueDate)
            val earlyMonthData = convertDaysToMonths(earlyDays)
            differenceDisplay = "Paid early by ${earlyMonthData.fullMonths} mo, ${earlyMonthData.remainingDays} d ($earlyDays days ahead)"
            agreedInterest = simpleInterest(principal, agreedMonthData.totalMonthsFraction, monthlyRate).setScale(2, RoundingMode.HALF_UP)
            overdueInterest = BigDecimal("0.00")
        }

        if (interestType == InterestType.SIMPLE) {
            val rawTotalInterest = if (paytimeDays == 0L || rate.compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal("0.00")
            } else {
                simpleInterest(principal, monthData.totalMonthsFraction, monthlyRate)
            }
            val totalInterest = rawTotalInterest.setScale(2, RoundingMode.HALF_UP)
            val totalAmount = principal2Dec.add(totalInterest)
            val formulaDisplay = "($principal2Dec × ${monthData.totalMonthsFraction.setScale(2, RoundingMode.HALF_UP)} mo × $monthlyRate%) / 100 = ₹$totalInterest"

            return CalculationResult(
                principal = principal2Dec,
                enteredRate = rate,
                rateType = rateType,
                annualRate = annualRate,
                monthlyRate = monthlyRate,
                totalDays = paytimeDays,
                fullMonths = monthData.fullMonths,
                remainingDays = monthData.remainingDays,
                totalMonthsFraction = monthData.totalMonthsFraction,
                interestType = InterestType.SIMPLE,
                calculationMode = CalculationMode.SPECIFIED_PERIOD,
                startDate = borrowDate,
                endDate = payingDate,
                monthlyInterestAmount = monthlyInterestAmount,
                totalInterest = totalInterest,
                totalAmount = totalAmount,
                yearsMonthsDaysDisplay = paytimeDurationDisplay,
                monthsDisplay = monthData.monthsDisplay,
                formulaDisplay = formulaDisplay,
                borrowDate = borrowDate,
                payingDate = payingDate,
                agreedYears = agreedYears,
                agreedMonths = agreedMonths,
                agreedDays = agreedDays,
                agreedTotalDays = agreedTotalDays,
                agreedDueDate = agreedDueDate,
                agreedDurationDisplay = agreedDurationDisplay,
                paytimeDurationDisplay = paytimeDurationDisplay,
                isOverdue = isOverdue,
                differenceDisplay = differenceDisplay,
                agreedInterest = agreedInterest,
                overdueInterest = overdueInterest
            )
        } else {
            val baseResult = calculateCompoundInterest(
                principal = principal,
                rate = rate,
                totalDays = paytimeDays,
                rateType = rateType,
                mode = CalculationMode.SPECIFIED_PERIOD,
                startDate = borrowDate,
                endDate = payingDate
            )
            return baseResult.copy(
                borrowDate = borrowDate,
                payingDate = payingDate,
                agreedYears = agreedYears,
                agreedMonths = agreedMonths,
                agreedDays = agreedDays,
                agreedTotalDays = agreedTotalDays,
                agreedDueDate = agreedDueDate,
                agreedDurationDisplay = agreedDurationDisplay,
                paytimeDurationDisplay = paytimeDurationDisplay,
                isOverdue = isOverdue,
                differenceDisplay = differenceDisplay,
                agreedInterest = agreedInterest,
                overdueInterest = overdueInterest
            )
        }
    }

    /**
     * Formats total days into years, months, and remaining days for readability.
     */
    fun formatDaysBreakdown(totalDays: Long): String {
        if (totalDays <= 0L) return "0 days"

        val fullMonths = totalDays / 30L
        val remDays = totalDays % 30L
        val years = fullMonths / 12L
        val months = fullMonths % 12L

        val parts = mutableListOf<String>()
        if (years > 0) parts.add("$years ${if (years == 1L) "year" else "years"}")
        if (months > 0) parts.add("$months ${if (months == 1L) "month" else "months"}")
        if (remDays > 0 || parts.isEmpty()) parts.add("$remDays ${if (remDays == 1L) "day" else "days"}")

        return "${parts.joinToString(", ")} ($totalDays days)"
    }

    /**
     * Formats monetary values in Indian Rupees with Indian comma grouping.
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

data class MonthConversion(
    val totalDays: Long,
    val fullMonths: Long,
    val remainingDays: Long,
    val years: Long,
    val monthsInYear: Long,
    val totalMonthsFraction: BigDecimal,
    val monthsDisplay: String
)
