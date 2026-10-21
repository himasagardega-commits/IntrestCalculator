package com.interestcalculator.app.viewmodel

import androidx.lifecycle.ViewModel
import com.interestcalculator.app.data.CalculationMode
import com.interestcalculator.app.data.CalculationResult
import com.interestcalculator.app.data.InterestType
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

    // Date Mode Inputs
    val datePrincipal: String = "",
    val dateRate: String = "",
    val startDate: LocalDate = LocalDate.now().minusYears(1),
    val endDate: LocalDate = LocalDate.now(),
    val isUseToday: Boolean = true,

    // Period Mode Inputs
    val periodPrincipal: String = "",
    val periodRate: String = "",
    val periodYears: String = "0",
    val periodMonths: String = "0",
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

    // Period Mode actions
    fun onPeriodPrincipalChanged(value: String) {
        _uiState.update { it.copy(periodPrincipal = value, principalError = null) }
        recalculateLivePeriod()
    }

    fun onPeriodRateChanged(value: String) {
        _uiState.update { it.copy(periodRate = value, rateError = null) }
        recalculateLivePeriod()
    }

    fun onPeriodYearsChanged(value: String) {
        _uiState.update { it.copy(periodYears = value.filter { it.isDigit() }, periodError = null) }
        recalculateLivePeriod()
    }

    fun onPeriodMonthsChanged(value: String) {
        _uiState.update { it.copy(periodMonths = value.filter { it.isDigit() }, periodError = null) }
        recalculateLivePeriod()
    }

    fun onPeriodDaysChanged(value: String) {
        _uiState.update { it.copy(periodDays = value.filter { it.isDigit() }, periodError = null) }
        recalculateLivePeriod()
    }

    fun setPeriodPreset(years: Long, months: Long, days: Long) {
        _uiState.update {
            it.copy(
                periodYears = years.toString(),
                periodMonths = months.toString(),
                periodDays = days.toString(),
                periodError = null
            )
        }
        recalculateLivePeriod()
    }

    fun resetPeriodCalculator() {
        _uiState.update {
            it.copy(
                periodPrincipal = "",
                periodRate = "",
                periodYears = "0",
                periodMonths = "0",
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
        val y = state.periodYears.toLongOrNull() ?: 0L
        val m = state.periodMonths.toLongOrNull() ?: 0L
        val d = state.periodDays.toLongOrNull() ?: 0L
        if (p != null && p > BigDecimal.ZERO && r != null && r >= BigDecimal.ZERO && (y > 0 || m > 0 || d > 0)) {
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

        val y = state.periodYears.toLongOrNull() ?: -1L
        val m = state.periodMonths.toLongOrNull() ?: -1L
        val d = state.periodDays.toLongOrNull() ?: -1L

        if (y < 0 || m < 0 || d < 0 || (y == 0L && m == 0L && d == 0L)) {
            _uiState.update { it.copy(periodError = "Please specify at least 1 day, month, or year") }
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
                annualRate = rate,
                totalDays = days,
                mode = CalculationMode.DATE_RANGE,
                startDate = state.startDate,
                endDate = state.endDate
            )
            InterestType.COMPOUND -> InterestCalculatorEngine.calculateCompoundInterest(
                principal = principal,
                annualRate = rate,
                totalDays = days,
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
        val y = state.periodYears.toLongOrNull() ?: 0L
        val m = state.periodMonths.toLongOrNull() ?: 0L
        val d = state.periodDays.toLongOrNull() ?: 0L

        val totalDays = InterestCalculatorEngine.convertPeriodToDays(y, m, d)

        return when (state.selectedInterestType) {
            InterestType.SIMPLE -> InterestCalculatorEngine.calculateSimpleInterest(
                principal = principal,
                annualRate = rate,
                totalDays = totalDays,
                mode = CalculationMode.SPECIFIED_PERIOD
            )
            InterestType.COMPOUND -> InterestCalculatorEngine.calculateCompoundInterest(
                principal = principal,
                annualRate = rate,
                totalDays = totalDays,
                mode = CalculationMode.SPECIFIED_PERIOD
            )
        }
    }
}
