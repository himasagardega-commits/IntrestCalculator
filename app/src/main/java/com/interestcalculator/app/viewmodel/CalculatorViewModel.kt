package com.interestcalculator.app.viewmodel

import androidx.lifecycle.ViewModel
import com.interestcalculator.app.data.CalculationMode
import com.interestcalculator.app.data.CalculationResult
import com.interestcalculator.app.data.InterestType
import com.interestcalculator.app.data.RateType
import com.interestcalculator.app.engine.InterestCalculatorEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.time.LocalDate

enum class AppScreen {
    HOME,
    DATE_CALCULATOR,
    PERIOD_CALCULATOR,
    RESULTS
}

data class CalculatorUiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val selectedInterestType: InterestType = InterestType.SIMPLE,
    val calculationMode: CalculationMode = CalculationMode.DATE_RANGE,
    val rateType: RateType = RateType.PER_MONTH,

    // Date Mode Inputs
    val datePrincipal: String = "",
    val dateRate: String = "",
    val startDate: LocalDate = LocalDate.now().minusYears(1),
    val endDate: LocalDate = LocalDate.now(),
    val isUseToday: Boolean = true,

    // Special Time Period Inputs (Agreed Duration & Paytime)
    val periodPrincipal: String = "",
    val periodRate: String = "",
    val borrowDate: LocalDate = LocalDate.now().minusMonths(6),
    val agreedYears: String = "0",
    val agreedMonths: String = "6",
    val agreedDays: String = "0",
    val payingDate: LocalDate = LocalDate.now(),
    val isPayToday: Boolean = true,
    val periodYears: String = "0",
    val periodMonths: String = "6",
    val periodDays: String = "0",

    // Validation Errors
    val principalError: String? = null,
    val rateError: String? = null,
    val dateError: String? = null,
    val periodError: String? = null,

    // Live preview or calculated result
    val currentResult: CalculationResult? = null
)

class CalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun navigateToDateCalculator(interestType: InterestType) {
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.DATE_CALCULATOR,
                selectedInterestType = interestType,
                calculationMode = CalculationMode.DATE_RANGE,
                principalError = null,
                rateError = null,
                dateError = null
            )
        }
        recalculateLiveDate()
    }

    fun navigateToPeriodCalculator() {
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.PERIOD_CALCULATOR,
                calculationMode = CalculationMode.SPECIFIED_PERIOD,
                principalError = null,
                rateError = null,
                periodError = null
            )
        }
        recalculateLivePeriod()
    }

    fun navigateToHome() {
        _uiState.update {
            it.copy(currentScreen = AppScreen.HOME)
        }
    }

    fun setInterestType(type: InterestType) {
        _uiState.update { it.copy(selectedInterestType = type) }
        if (_uiState.value.calculationMode == CalculationMode.DATE_RANGE) {
            recalculateLiveDate()
        } else {
            recalculateLivePeriod()
        }
    }

    fun onRateTypeChanged(type: RateType) {
        _uiState.update { it.copy(rateType = type) }
        if (_uiState.value.calculationMode == CalculationMode.DATE_RANGE) {
            recalculateLiveDate()
        } else {
            recalculateLivePeriod()
        }
    }

    // Date Mode actions
    fun onDatePrincipalChanged(value: String) {
        _uiState.update { it.copy(datePrincipal = value, principalError = null) }
        recalculateLiveDate()
    }

    fun onDateRateChanged(value: String) {
        _uiState.update { it.copy(dateRate = value, rateError = null) }
        recalculateLiveDate()
    }

    fun onStartDateChanged(date: LocalDate) {
        val currentEnd = _uiState.value.endDate
        if (currentEnd.isBefore(date)) {
            _uiState.update {
                it.copy(
                    startDate = date,
                    endDate = date, // automatically keep valid
                    dateError = null
                )
            }
        } else {
            _uiState.update { it.copy(startDate = date, dateError = null) }
        }
        recalculateLiveDate()
    }

    fun onEndDateChanged(date: LocalDate) {
        if (date.isBefore(_uiState.value.startDate)) {
            _uiState.update {
                it.copy(
                    endDate = date,
                    isUseToday = false,
                    dateError = "End date cannot be earlier than start date"
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    endDate = date,
                    isUseToday = (date == LocalDate.now()),
                    dateError = null
                )
            }
            recalculateLiveDate()
        }
    }

    fun onUseTodayToggled(useToday: Boolean) {
        val today = LocalDate.now()
        if (useToday) {
            val currentStart = _uiState.value.startDate
            val adjustedStart = if (currentStart.isAfter(today)) today else currentStart
            _uiState.update {
                it.copy(
                    isUseToday = true,
                    startDate = adjustedStart,
                    endDate = today,
                    dateError = null
                )
            }
            recalculateLiveDate()
        } else {
            _uiState.update { it.copy(isUseToday = false) }
        }
    }

    fun resetDateCalculator() {
        val today = LocalDate.now()
        _uiState.update {
            it.copy(
                datePrincipal = "",
                dateRate = "",
                startDate = today.minusYears(1),
                endDate = today,
                isUseToday = true,
                principalError = null,
                rateError = null,
                dateError = null,
                currentResult = null
            )
        }
    }

    // Special Time Period (Agreed Duration & Paytime) actions
    fun onPeriodPrincipalChanged(value: String) {
        _uiState.update { it.copy(periodPrincipal = value, principalError = null) }
        recalculateLivePeriod()
    }

    fun onPeriodRateChanged(value: String) {
        _uiState.update { it.copy(periodRate = value, rateError = null) }
        recalculateLivePeriod()
    }

    fun onBorrowDateChanged(date: LocalDate) {
        val currentPaying = _uiState.value.payingDate
        val adjustedPaying = if (currentPaying.isBefore(date)) date else currentPaying
        val days = InterestCalculatorEngine.calculateElapsedDays(date, adjustedPaying)
        val mData = InterestCalculatorEngine.convertDaysToMonths(days)

        _uiState.update {
            it.copy(
                borrowDate = date,
                payingDate = adjustedPaying,
                periodYears = mData.years.toString(),
                periodMonths = mData.monthsInYear.toString(),
                periodDays = mData.remainingDays.toString(),
                periodError = null
            )
        }
        recalculateLivePeriod()
    }

    fun onAgreedYearsChanged(value: String) {
        _uiState.update { it.copy(agreedYears = value.filter { it.isDigit() }, periodError = null) }
        recalculateLivePeriod()
    }

    fun onAgreedMonthsChanged(value: String) {
        _uiState.update { it.copy(agreedMonths = value.filter { it.isDigit() }, periodError = null) }
        recalculateLivePeriod()
    }

    fun onAgreedDaysChanged(value: String) {
        _uiState.update { it.copy(agreedDays = value.filter { it.isDigit() }, periodError = null) }
        recalculateLivePeriod()
    }

    fun setAgreedPreset(years: Long, months: Long, days: Long) {
        _uiState.update {
            it.copy(
                agreedYears = years.toString(),
                agreedMonths = months.toString(),
                agreedDays = days.toString(),
                periodError = null
            )
        }
        recalculateLivePeriod()
    }

    fun onPayingDateChanged(date: LocalDate) {
        if (date.isBefore(_uiState.value.borrowDate)) {
            _uiState.update {
                it.copy(
                    payingDate = date,
                    isPayToday = false,
                    periodError = "Paying date cannot be earlier than borrow date"
                )
            }
        } else {
            val days = InterestCalculatorEngine.calculateElapsedDays(_uiState.value.borrowDate, date)
            val mData = InterestCalculatorEngine.convertDaysToMonths(days)
            _uiState.update {
                it.copy(
                    payingDate = date,
                    isPayToday = (date == LocalDate.now()),
                    periodYears = mData.years.toString(),
                    periodMonths = mData.monthsInYear.toString(),
                    periodDays = mData.remainingDays.toString(),
                    periodError = null
                )
            }
            recalculateLivePeriod()
        }
    }

    fun onPayTodayToggled(useToday: Boolean) {
        val today = LocalDate.now()
        if (useToday) {
            val bDate = _uiState.value.borrowDate
            val adjustedBorrow = if (bDate.isAfter(today)) today else bDate
            val days = InterestCalculatorEngine.calculateElapsedDays(adjustedBorrow, today)
            val mData = InterestCalculatorEngine.convertDaysToMonths(days)
            _uiState.update {
                it.copy(
                    isPayToday = true,
                    borrowDate = adjustedBorrow,
                    payingDate = today,
                    periodYears = mData.years.toString(),
                    periodMonths = mData.monthsInYear.toString(),
                    periodDays = mData.remainingDays.toString(),
                    periodError = null
                )
            }
            recalculateLivePeriod()
        } else {
            _uiState.update { it.copy(isPayToday = false) }
        }
    }

    fun onPeriodYearsChanged(value: String) {
        val filtered = value.filter { it.isDigit() }
        val y = filtered.toLongOrNull() ?: 0L
        val m = _uiState.value.periodMonths.toLongOrNull() ?: 0L
        val d = _uiState.value.periodDays.toLongOrNull() ?: 0L
        val newPaying = _uiState.value.borrowDate.plusYears(y).plusMonths(m).plusDays(d)
        _uiState.update {
            it.copy(
                periodYears = filtered,
                payingDate = newPaying,
                isPayToday = (newPaying == LocalDate.now()),
                periodError = null
            )
        }
        recalculateLivePeriod()
    }

    fun onPeriodMonthsChanged(value: String) {
        val filtered = value.filter { it.isDigit() }
        val y = _uiState.value.periodYears.toLongOrNull() ?: 0L
        val m = filtered.toLongOrNull() ?: 0L
        val d = _uiState.value.periodDays.toLongOrNull() ?: 0L
        val newPaying = _uiState.value.borrowDate.plusYears(y).plusMonths(m).plusDays(d)
        _uiState.update {
            it.copy(
                periodMonths = filtered,
                payingDate = newPaying,
                isPayToday = (newPaying == LocalDate.now()),
                periodError = null
            )
        }
        recalculateLivePeriod()
    }

    fun onPeriodDaysChanged(value: String) {
        val filtered = value.filter { it.isDigit() }
        val y = _uiState.value.periodYears.toLongOrNull() ?: 0L
        val m = _uiState.value.periodMonths.toLongOrNull() ?: 0L
        val d = filtered.toLongOrNull() ?: 0L
        val newPaying = _uiState.value.borrowDate.plusYears(y).plusMonths(m).plusDays(d)
        _uiState.update {
            it.copy(
                periodDays = filtered,
                payingDate = newPaying,
                isPayToday = (newPaying == LocalDate.now()),
                periodError = null
            )
        }
        recalculateLivePeriod()
    }

    fun resetPeriodCalculator() {
        val today = LocalDate.now()
        val sixMonthsAgo = today.minusMonths(6)
        _uiState.update {
            it.copy(
                periodPrincipal = "",
                periodRate = "",
                borrowDate = sixMonthsAgo,
                agreedYears = "0",
                agreedMonths = "6",
                agreedDays = "0",
                payingDate = today,
                isPayToday = true,
                periodYears = "0",
                periodMonths = "6",
                periodDays = "0",
                principalError = null,
                rateError = null,
                periodError = null,
                currentResult = null
            )
        }
    }

    // Execution to go to Results Screen
    fun calculateAndShowResults(): Boolean {
        val state = _uiState.value
        return if (state.calculationMode == CalculationMode.DATE_RANGE) {
            val valid = validateDateInputs()
            if (valid) {
                val res = computeDateResult()
                _uiState.update { it.copy(currentResult = res, currentScreen = AppScreen.RESULTS) }
            }
            valid
        } else {
            val valid = validatePeriodInputs()
            if (valid) {
                val res = computePeriodResult()
                _uiState.update { it.copy(currentResult = res, currentScreen = AppScreen.RESULTS) }
            }
            valid
        }
    }

    fun recalculateFromResults() {
        val state = _uiState.value
        if (state.calculationMode == CalculationMode.DATE_RANGE) {
            _uiState.update { it.copy(currentScreen = AppScreen.DATE_CALCULATOR) }
        } else {
            _uiState.update { it.copy(currentScreen = AppScreen.PERIOD_CALCULATOR) }
        }
    }

    private fun recalculateLiveDate() {
        val state = _uiState.value
        val p = state.datePrincipal.toBigDecimalOrNull()
        val r = state.dateRate.toBigDecimalOrNull()
        if (p != null && p > BigDecimal.ZERO && r != null && r >= BigDecimal.ZERO && !state.endDate.isBefore(state.startDate)) {
            val res = computeDateResult()
            _uiState.update { it.copy(currentResult = res) }
        }
    }

    private fun recalculateLivePeriod() {
        val state = _uiState.value
        val p = state.periodPrincipal.toBigDecimalOrNull()
        val r = state.periodRate.toBigDecimalOrNull()
        if (p != null && p > BigDecimal.ZERO && r != null && r >= BigDecimal.ZERO && !state.payingDate.isBefore(state.borrowDate)) {
            val res = computePeriodResult()
            _uiState.update { it.copy(currentResult = res) }
        }
    }

    private fun validateDateInputs(): Boolean {
        val state = _uiState.value
        var isValid = true

        val principal = state.datePrincipal.toBigDecimalOrNull()
        if (principal == null || principal <= BigDecimal.ZERO) {
            _uiState.update { it.copy(principalError = "Principal amount must be greater than 0") }
            isValid = false
        }

        val rate = state.dateRate.toBigDecimalOrNull()
        if (rate == null || rate < BigDecimal.ZERO) {
            _uiState.update { it.copy(rateError = "Annual rate must be 0 or greater") }
            isValid = false
        }

        if (state.endDate.isBefore(state.startDate)) {
            _uiState.update { it.copy(dateError = "End date cannot be earlier than start date") }
            isValid = false
        }

        return isValid
    }

    private fun validatePeriodInputs(): Boolean {
        val state = _uiState.value
        var isValid = true

        val principal = state.periodPrincipal.toBigDecimalOrNull()
        if (principal == null || principal <= BigDecimal.ZERO) {
            _uiState.update { it.copy(principalError = "Principal amount must be greater than 0") }
            isValid = false
        }

        val rate = state.periodRate.toBigDecimalOrNull()
        if (rate == null || rate < BigDecimal.ZERO) {
            _uiState.update { it.copy(rateError = "Annual rate must be 0 or greater") }
            isValid = false
        }

        if (state.payingDate.isBefore(state.borrowDate)) {
            _uiState.update { it.copy(periodError = "Paying date cannot be earlier than borrow date") }
            isValid = false
        }

        return isValid
    }

    private fun computeDateResult(): CalculationResult {
        val state = _uiState.value
        val principal = state.datePrincipal.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val rate = state.dateRate.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val days = InterestCalculatorEngine.calculateElapsedDays(state.startDate, state.endDate)

        return when (state.selectedInterestType) {
            InterestType.SIMPLE -> InterestCalculatorEngine.calculateSimpleInterest(
                principal = principal,
                rate = rate,
                totalDays = days,
                rateType = state.rateType,
                mode = CalculationMode.DATE_RANGE,
                startDate = state.startDate,
                endDate = state.endDate
            )
            InterestType.COMPOUND -> InterestCalculatorEngine.calculateCompoundInterest(
                principal = principal,
                rate = rate,
                totalDays = days,
                rateType = state.rateType,
                mode = CalculationMode.DATE_RANGE,
                startDate = state.startDate,
                endDate = state.endDate
            )
        }
    }

    private fun computePeriodResult(): CalculationResult {
        val state = _uiState.value
        val principal = state.periodPrincipal.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val rate = state.periodRate.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val ay = state.agreedYears.toLongOrNull() ?: 0L
        val am = state.agreedMonths.toLongOrNull() ?: 0L
        val ad = state.agreedDays.toLongOrNull() ?: 0L

        return InterestCalculatorEngine.calculateSpecialPeriodInterest(
            principal = principal,
            rate = rate,
            rateType = state.rateType,
            interestType = state.selectedInterestType,
            borrowDate = state.borrowDate,
            payingDate = state.payingDate,
            agreedYears = ay,
            agreedMonths = am,
            agreedDays = ad
        )
    }
}
