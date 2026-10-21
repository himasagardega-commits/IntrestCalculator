package com.interestcalculator.app

import com.interestcalculator.app.data.CalculationMode
import com.interestcalculator.app.data.InterestType
import com.interestcalculator.app.engine.InterestCalculatorEngine
import org.junit.Assert.assertEquals
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

    // 2. Simple interest for 365 days.
    @Test
    fun testCase2_SimpleInterest_365Days() {
        // Principal = 10,000, Rate = 12%, Days = 365
        // Interest = 10000 * 12 * 365 / (100 * 365) = 1,200.00
        val principal = BigDecimal("10000")
        val rate = BigDecimal("12")
        val result = InterestCalculatorEngine.calculateSimpleInterest(principal, rate, 365L)
        assertEquals(BigDecimal("1200.00"), result.totalInterest)
        assertEquals(BigDecimal("11200.00"), result.totalAmount)
    }

    // 3. Simple interest for 730 days: interest must not compound.
    @Test
    fun testCase3_SimpleInterest_730Days_DoesNotCompound() {
        // Principal = 10,000, Rate = 12%, Days = 730
        // Expected: Interest = 2,400.00, Total = 12,400.00
        val principal = BigDecimal("10000")
        val rate = BigDecimal("12")
        val result = InterestCalculatorEngine.calculateSimpleInterest(principal, rate, 730L)
        assertEquals(BigDecimal("2400.00"), result.totalInterest)
        assertEquals(BigDecimal("12400.00"), result.totalAmount)
    }

    // 4. Compound interest for 365 days.
    @Test
    fun testCase4_CompoundInterest_365Days() {
        // Principal = 10,000, Rate = 12%, Days = 365
        // 1 full year = 1,200.00 interest, Total = 11,200.00
        val principal = BigDecimal("10000")
        val rate = BigDecimal("12")
        val result = InterestCalculatorEngine.calculateCompoundInterest(principal, rate, 365L)
        assertEquals(BigDecimal("1200.00"), result.totalInterest)
        assertEquals(BigDecimal("11200.00"), result.totalAmount)
    }

    // 5. Compound interest for 400 days: one full compounding period and 35 extra days.
    @Test
    fun testCase5_CompoundInterest_400Days() {
        // First 365 days: 10,000 * 12% = 1,200 (Balance = 11,200)
        // Remaining 35 days: 11,200 * 12% * 35 / 365 = 128.8767...
        // Total interest = 1,328.88, Total amount = 11,328.88
        val principal = BigDecimal("10000")
        val rate = BigDecimal("12")
        val result = InterestCalculatorEngine.calculateCompoundInterest(principal, rate, 400L)
        assertEquals(BigDecimal("1328.88"), result.totalInterest)
        assertEquals(BigDecimal("11328.88"), result.totalAmount)
    }

    // 6. Compound interest for 730 days: two complete compounding periods.
    @Test
    fun testCase6_CompoundInterest_730Days() {
        // Year 1: 10,000 + 1,200 = 11,200
        // Year 2: 11,200 + 1,344 = 12,544
        // Expected total balance: 12,544.00, Interest: 2,544.00
        val principal = BigDecimal("10000")
        val rate = BigDecimal("12")
        val result = InterestCalculatorEngine.calculateCompoundInterest(principal, rate, 730L)
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

    // 8. One-day duration.
    @Test
    fun testCase8_OneDayDuration() {
        // Principal = 36,500, Rate = 10%, Days = 1
        // Interest = 36500 * 10 * 1 / (100 * 365) = 10.00
        val principal = BigDecimal("36500")
        val rate = BigDecimal("10")
        val resultSimple = InterestCalculatorEngine.calculateSimpleInterest(principal, rate, 1L)
        assertEquals(BigDecimal("10.00"), resultSimple.totalInterest)
        assertEquals(BigDecimal("36510.00"), resultSimple.totalAmount)

        val resultCompound = InterestCalculatorEngine.calculateCompoundInterest(principal, rate, 1L)
        assertEquals(BigDecimal("10.00"), resultCompound.totalInterest)
        assertEquals(BigDecimal("36510.00"), resultCompound.totalAmount)
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

    // 11. Specified period of 45 days.
    @Test
    fun testCase11_SpecifiedPeriod_45Days() {
        val days = InterestCalculatorEngine.convertPeriodToDays(years = 0, months = 0, days = 45)
        assertEquals(45L, days)
    }

    // 12. Specified period of 6 months.
    @Test
    fun testCase12_SpecifiedPeriod_6Months() {
        // 6 months * 30 days = 180 days
        val days = InterestCalculatorEngine.convertPeriodToDays(years = 0, months = 6, days = 0)
        assertEquals(180L, days)
    }

    // 13. Specified period of 2 years.
    @Test
    fun testCase13_SpecifiedPeriod_2Years() {
        // 2 years * 365 days = 730 days
        val days = InterestCalculatorEngine.convertPeriodToDays(years = 2, months = 0, days = 0)
        assertEquals(730L, days)
    }

    // 14. Very large principal amounts and interest rates.
    @Test
    fun testCase14_VeryLargeAmountsAndRates() {
        val principal = BigDecimal("100000000000000.00") // 100 Trillion
        val rate = BigDecimal("50.00")
        val days = 365L * 5 // 5 years

        val resultSimple = InterestCalculatorEngine.calculateSimpleInterest(principal, rate, days)
        assertEquals(BigDecimal("250000000000000.00"), resultSimple.totalInterest)
        assertEquals(BigDecimal("350000000000000.00"), resultSimple.totalAmount)

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

    // Test combined specified period: 1 year and 90 days = 365 + 90 = 455 days
    @Test
    fun testCombinedSpecifiedPeriod() {
        val days = InterestCalculatorEngine.convertPeriodToDays(years = 1, months = 0, days = 90)
        assertEquals(455L, days)
    }
}
