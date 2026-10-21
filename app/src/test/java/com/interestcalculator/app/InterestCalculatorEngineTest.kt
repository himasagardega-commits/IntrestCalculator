package com.interestcalculator.app

import com.interestcalculator.app.data.CalculationMode
import com.interestcalculator.app.data.InterestType
import com.interestcalculator.app.engine.InterestCalculatorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class InterestCalculatorEngineTest {

    // 1. Zero-day duration: interest must be zero.
    @Test
    fun testCase1_ZeroDayDuration_ReturnsZeroInterest() {
        val principal = BigDecimal("10000")
        val rate = BigDecimal("12")
        val resultSimple = InterestCalculatorEngine.calculateSimpleInterest(principal, rate, 0L)
        assertEquals(BigDecimal("0.00"), resultSimple.totalInterest)
        assertEquals(BigDecimal("10000.00"), resultSimple.totalAmount)

        val resultCompound = InterestCalculatorEngine.calculateCompoundInterest(principal, rate, 0L)
        assertEquals(BigDecimal("0.00"), resultCompound.totalInterest)
        assertEquals(BigDecimal("10000.00"), resultCompound.totalAmount)
    }

    // 2. Simple interest for 12 months (360 days) @ 12% p.a. (= 1% per month).
    @Test
    fun testCase2_SimpleInterest_12Months() {
        // Principal = 10,000, Annual Rate = 12% (1% per month), 360 days = 12 months
        // Interest = 10,000 * 1% * 12 = 1,200.00
        val principal = BigDecimal("10000")
        val rate = BigDecimal("12")
        val result = InterestCalculatorEngine.calculateSimpleInterest(principal, rate, 360L)
        assertEquals(BigDecimal("1200.00"), result.totalInterest)
        assertEquals(BigDecimal("11200.00"), result.totalAmount)
        assertEquals(12L, result.fullMonths)
        assertEquals(0L, result.remainingDays)
    }

    // 3. Simple interest for 24 months (720 days): interest must not compound.
    @Test
    fun testCase3_SimpleInterest_24Months_DoesNotCompound() {
        // Principal = 10,000, Rate = 12% p.a. (1% p.m.), 720 days = 24 months
        // Expected: Interest = 10,000 * 1% * 24 = 2,400.00, Total = 12,400.00
        val principal = BigDecimal("10000")
        val rate = BigDecimal("12")
        val result = InterestCalculatorEngine.calculateSimpleInterest(principal, rate, 720L)
        assertEquals(BigDecimal("2400.00"), result.totalInterest)
        assertEquals(BigDecimal("12400.00"), result.totalAmount)
    }

    // 4. Compound interest for 12 months (360 days).
    @Test
    fun testCase4_CompoundInterest_12Months() {
        // Principal = 10,000, Rate = 12% p.a., 12 months = 1,200.00 interest
        val principal = BigDecimal("10000")
        val rate = BigDecimal("12")
        val result = InterestCalculatorEngine.calculateCompoundInterest(principal, rate, 360L)
        assertEquals(BigDecimal("1200.00"), result.totalInterest)
        assertEquals(BigDecimal("11200.00"), result.totalAmount)
    }

    // 5. Compound interest for 400 days: 13 months and 10 days (1 full 12-month year + 1 mo 10 days).
    @Test
    fun testCase5_CompoundInterest_400Days_MonthlyCompounding() {
        // 400 days = 13 months, 10 days = 1 year (12 mo) + partial (1 mo, 10 days = 1.3333... mo)
        // Year 1 (12 months): 10,000 * 12% = 1,200 (Balance = 11,200)
        // Remaining 1 mo 10 days: 11,200 * 1% * (1 + 10/30) = 149.33
        // Total interest = 1,349.33, Total amount = 11,349.33
        val principal = BigDecimal("10000")
        val rate = BigDecimal("12")
        val result = InterestCalculatorEngine.calculateCompoundInterest(principal, rate, 400L)
        assertEquals(BigDecimal("1349.33"), result.totalInterest)
        assertEquals(BigDecimal("11349.33"), result.totalAmount)
    }

    // 6. Compound interest for 24 months (720 days): two complete 12-month compounding periods.
    @Test
    fun testCase6_CompoundInterest_24Months() {
        // Year 1 (12 mo): 10,000 + 1,200 = 11,200
        // Year 2 (12 mo): 11,200 + 1,344 = 12,544
        // Expected total balance: 12,544.00, Interest: 2,544.00
        val principal = BigDecimal("10000")
        val rate = BigDecimal("12")
        val result = InterestCalculatorEngine.calculateCompoundInterest(principal, rate, 720L)
        assertEquals(BigDecimal("2544.00"), result.totalInterest)
        assertEquals(BigDecimal("12544.00"), result.totalAmount)
    }

    // 7. Zero interest rate.
    @Test
    fun testCase7_ZeroInterestRate() {
        val principal = BigDecimal("50000")
        val rate = BigDecimal("0")
        val resultSimple = InterestCalculatorEngine.calculateSimpleInterest(principal, rate, 500L)
        assertEquals(BigDecimal("0.00"), resultSimple.totalInterest)
        assertEquals(BigDecimal("50000.00"), resultSimple.totalAmount)

        val resultCompound = InterestCalculatorEngine.calculateCompoundInterest(principal, rate, 500L)
        assertEquals(BigDecimal("0.00"), resultCompound.totalInterest)
        assertEquals(BigDecimal("50000.00"), resultCompound.totalAmount)
    }

    // 8. One-day duration with monthly calculation.
    @Test
    fun testCase8_OneDayDuration() {
        // Principal = 30,000, Rate = 12% p.a. (1% p.m. = ₹300/mo)
        // 1 day = 1/30 month = ₹300 / 30 = ₹10.00
        val principal = BigDecimal("30000")
        val rate = BigDecimal("12")
        val resultSimple = InterestCalculatorEngine.calculateSimpleInterest(principal, rate, 1L)
        assertEquals(BigDecimal("10.00"), resultSimple.totalInterest)
        assertEquals(BigDecimal("30010.00"), resultSimple.totalAmount)
    }

    // 9. End date equal to start date.
    @Test
    fun testCase9_EndDateEqualToStartDate() {
        val date = LocalDate.of(2024, 5, 10)
        val elapsed = InterestCalculatorEngine.calculateElapsedDays(date, date)
        assertEquals(0L, elapsed)
    }

    // 10. Invalid date range (end date earlier than start date).
    @Test
    fun testCase10_InvalidDateRange_ThrowsException() {
        val start = LocalDate.of(2024, 6, 15)
        val end = LocalDate.of(2024, 6, 14)
        assertThrows(IllegalArgumentException::class.java) {
            InterestCalculatorEngine.calculateElapsedDays(start, end)
        }
    }

    // 11. Specified period of 45 days (1 month, 15 days = 1.5 months).
    @Test
    fun testCase11_SpecifiedPeriod_45Days() {
        val conv = InterestCalculatorEngine.convertDaysToMonths(45L)
        assertEquals(1L, conv.fullMonths)
        assertEquals(15L, conv.remainingDays)

        // Principal = 10,000, 12% p.a. (1% p.m.) -> 1.5 months = 150.00
        val res = InterestCalculatorEngine.calculateSimpleInterest(BigDecimal("10000"), BigDecimal("12"), 45L)
        assertEquals(BigDecimal("150.00"), res.totalInterest)
    }

    // 12. Specified period of 6 months (180 days).
    @Test
    fun testCase12_SpecifiedPeriod_6Months() {
        val conv = InterestCalculatorEngine.convertDaysToMonths(180L)
        assertEquals(6L, conv.fullMonths)
        assertEquals(0L, conv.remainingDays)

        val res = InterestCalculatorEngine.calculateSimpleInterest(BigDecimal("10000"), BigDecimal("12"), 180L)
        assertEquals(BigDecimal("600.00"), res.totalInterest)
    }

    // 13. Specified period of 2 years (730 days -> 24 months, 10 days).
    @Test
    fun testCase13_SpecifiedPeriod_2Years() {
        val conv = InterestCalculatorEngine.convertDaysToMonths(730L)
        assertEquals(24L, conv.fullMonths)
        assertEquals(10L, conv.remainingDays)
    }

    // 14. Very large principal amounts and interest rates.
    @Test
    fun testCase14_VeryLargeAmountsAndRates() {
        val principal = BigDecimal("100000000000000.00") // 100 Trillion
        val rate = BigDecimal("48.00") // 4% per month
        val days = 360L * 5 // 5 years = 60 months

        val resultSimple = InterestCalculatorEngine.calculateSimpleInterest(principal, rate, days)
        assertEquals(BigDecimal("240000000000000.00"), resultSimple.totalInterest)
        assertEquals(BigDecimal("340000000000000.00"), resultSimple.totalAmount)

        val resultCompound = InterestCalculatorEngine.calculateCompoundInterest(principal, rate, days)
        assertTrue(resultCompound.totalInterest > BigDecimal.ZERO)
        assertEquals(
            resultCompound.principal.add(resultCompound.totalInterest),
            resultCompound.totalAmount
        )
    }

    // 15. Verify that displayed total equals principal plus interest.
    @Test
    fun testCase15_VerifyTotalEqualsPrincipalPlusInterest() {
        val testCases = listOf(
            Triple(BigDecimal("12345.67"), BigDecimal("8.75"), 147L),
            Triple(BigDecimal("999999.99"), BigDecimal("15.5"), 832L),
            Triple(BigDecimal("500"), BigDecimal("3.25"), 19L),
            Triple(BigDecimal("87654321.12"), BigDecimal("11.11"), 1095L)
        )

        for ((p, r, d) in testCases) {
            val simple = InterestCalculatorEngine.calculateSimpleInterest(p, r, d)
            assertEquals(
                "Simple total must equal principal + interest",
                simple.principal.add(simple.totalInterest),
                simple.totalAmount
            )

            val compound = InterestCalculatorEngine.calculateCompoundInterest(p, r, d)
            assertEquals(
                "Compound total must equal principal + interest",
                compound.principal.add(compound.totalInterest),
                compound.totalAmount
            )
        }
    }

    // 16. User's exact Python formula: (p * t * r) / 100
    @Test
    fun testCase16_UserFormula_P_T_R_Over_100() {
        val p = BigDecimal("10000")
        val t = BigDecimal("12") // 12 months
        val r = BigDecimal("2")  // 2% per month
        val result = InterestCalculatorEngine.simpleInterest(p, t, r)
        // (10000 * 12 * 2) / 100 = 2400.00
        assertEquals(BigDecimal("2400.00"), result.setScale(2, java.math.RoundingMode.HALF_UP))
    }

    // 17. Monthly Rate calculation via RateType.PER_MONTH
    @Test
    fun testCase17_SimpleInterest_MonthlyRateMode() {
        val principal = BigDecimal("10000")
        val monthlyRate = BigDecimal("2") // 2% per month
        val days = 360L // 12 months
        val result = InterestCalculatorEngine.calculateSimpleInterest(
            principal = principal,
            rate = monthlyRate,
            totalDays = days,
            rateType = com.interestcalculator.app.data.RateType.PER_MONTH
        )
        // (10000 * 12 * 2) / 100 = 2400.00
        assertEquals(BigDecimal("2400.00"), result.totalInterest)
        assertEquals(BigDecimal("12400.00"), result.totalAmount)
        assertEquals(BigDecimal("2.0000"), result.monthlyRate)
        assertEquals(BigDecimal("24.00"), result.annualRate)
    }

    // Test Indian currency formatting
    @Test
    fun testIndianCurrencyFormatting() {
        assertEquals("₹0.00", InterestCalculatorEngine.formatIndianCurrency(BigDecimal("0")))
        assertEquals("₹100.00", InterestCalculatorEngine.formatIndianCurrency(BigDecimal("100")))
        assertEquals("₹1,000.00", InterestCalculatorEngine.formatIndianCurrency(BigDecimal("1000")))
        assertEquals("₹10,000.00", InterestCalculatorEngine.formatIndianCurrency(BigDecimal("10000")))
        assertEquals("₹1,00,000.00", InterestCalculatorEngine.formatIndianCurrency(BigDecimal("100000")))
        assertEquals("₹12,34,567.89", InterestCalculatorEngine.formatIndianCurrency(BigDecimal("1234567.89")))
        assertEquals("₹1,00,00,000.00", InterestCalculatorEngine.formatIndianCurrency(BigDecimal("10000000")))
    }

    // 18. Special Time Period: Agreed 6 months, paid late (Overdue)
    @Test
    fun testCase18_SpecialTimePeriod_Overdue() {
        val borrowDate = LocalDate.of(2025, 1, 1)
        val agreedYears = 0L
        val agreedMonths = 6L
        val agreedDays = 0L
        val payingDate = LocalDate.of(2025, 9, 1)

        val principal = BigDecimal("10000")
        val rate = BigDecimal("2") // 2% per month

        val res = InterestCalculatorEngine.calculateSpecialPeriodInterest(
            principal = principal,
            rate = rate,
            rateType = com.interestcalculator.app.data.RateType.PER_MONTH,
            interestType = com.interestcalculator.app.data.InterestType.SIMPLE,
            borrowDate = borrowDate,
            payingDate = payingDate,
            agreedYears = agreedYears,
            agreedMonths = agreedMonths,
            agreedDays = agreedDays
        )

        assertTrue(res.isOverdue)
        assertEquals(LocalDate.of(2025, 7, 1), res.agreedDueDate)
        assertEquals(243L, res.totalDays) // Elapsed days Jan 1 to Sep 1
        assertEquals(8L, res.fullMonths)
        assertEquals(3L, res.remainingDays)
        // 8 + 3/30 = 8.1 months
        // (10000 * 8.1 * 2) / 100 = 1620.00
        assertEquals(BigDecimal("1620.00"), res.totalInterest)
        assertEquals(BigDecimal("11620.00"), res.totalAmount)
        assertTrue(res.differenceDisplay.contains("Overdue"))
    }

    // 19. Special Time Period: Agreed 6 months, paid early
    @Test
    fun testCase19_SpecialTimePeriod_PaidEarly() {
        val borrowDate = LocalDate.of(2025, 1, 1)
        val agreedYears = 0L
        val agreedMonths = 6L
        val agreedDays = 0L
        val payingDate = LocalDate.of(2025, 5, 1)

        val principal = BigDecimal("10000")
        val rate = BigDecimal("2")

        val res = InterestCalculatorEngine.calculateSpecialPeriodInterest(
            principal = principal,
            rate = rate,
            rateType = com.interestcalculator.app.data.RateType.PER_MONTH,
            interestType = com.interestcalculator.app.data.InterestType.SIMPLE,
            borrowDate = borrowDate,
            payingDate = payingDate,
            agreedYears = agreedYears,
            agreedMonths = agreedMonths,
            agreedDays = agreedDays
        )

        assertFalse(res.isOverdue)
        assertEquals(LocalDate.of(2025, 7, 1), res.agreedDueDate)
        assertEquals(120L, res.totalDays) // Elapsed days Jan 1 to May 1
        assertEquals(4L, res.fullMonths)
        assertEquals(0L, res.remainingDays)
        // (10000 * 4 * 2) / 100 = 800.00
        assertEquals(BigDecimal("800.00"), res.totalInterest)
        assertEquals(BigDecimal("10800.00"), res.totalAmount)
        assertTrue(res.differenceDisplay.contains("Paid early"))
    }
}
