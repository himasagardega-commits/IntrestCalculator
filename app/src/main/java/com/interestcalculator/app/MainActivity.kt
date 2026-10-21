package com.interestcalculator.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.interestcalculator.app.data.CalculationMode
import com.interestcalculator.app.data.InterestType
import com.interestcalculator.app.ui.screens.DateCalculatorScreen
import com.interestcalculator.app.ui.screens.HomeScreen
import com.interestcalculator.app.ui.screens.PeriodCalculatorScreen
import com.interestcalculator.app.ui.screens.ResultsScreen
import com.interestcalculator.app.ui.theme.InterestCalculatorTheme
import com.interestcalculator.app.viewmodel.AppScreen
import com.interestcalculator.app.viewmodel.CalculatorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InterestCalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InterestCalculatorApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun InterestCalculatorApp(viewModel: CalculatorViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle Android system back button
    BackHandler(enabled = uiState.currentScreen != AppScreen.HOME) {
        when (uiState.currentScreen) {
            AppScreen.RESULTS -> {
                if (uiState.calculationMode == CalculationMode.DATE_RANGE) {
                    viewModel.navigateToDateCalculator(uiState.selectedInterestType)
                } else {
                    viewModel.navigateToPeriodCalculator()
                }
            }
            AppScreen.DATE_CALCULATOR, AppScreen.PERIOD_CALCULATOR -> {
                viewModel.navigateToHome()
            }
            AppScreen.HOME -> {
                // Exit app
            }
        }
    }

    when (uiState.currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                onSelectSimpleInterest = {
                    viewModel.navigateToDateCalculator(InterestType.SIMPLE)
                },
                onSelectCompoundInterest = {
                    viewModel.navigateToDateCalculator(InterestType.COMPOUND)
                },
                onSelectSpecifiedPeriod = {
                    viewModel.navigateToPeriodCalculator()
                }
            )
        }

        AppScreen.DATE_CALCULATOR -> {
            DateCalculatorScreen(
                uiState = uiState,
                onNavigateBack = { viewModel.navigateToHome() },
                onInterestTypeChanged = { viewModel.setInterestType(it) },
                onPrincipalChanged = { viewModel.onDatePrincipalChanged(it) },
                onRateChanged = { viewModel.onDateRateChanged(it) },
                onRateTypeChanged = { viewModel.onRateTypeChanged(it) },
                onStartDateChanged = { viewModel.onStartDateChanged(it) },
                onEndDateChanged = { viewModel.onEndDateChanged(it) },
                onUseTodayToggled = { viewModel.onUseTodayToggled(it) },
                onCalculateClicked = { viewModel.calculateAndShowResults() },
                onResetClicked = { viewModel.resetDateCalculator() }
            )
        }

        AppScreen.PERIOD_CALCULATOR -> {
            PeriodCalculatorScreen(
                uiState = uiState,
                onNavigateBack = { viewModel.navigateToHome() },
                onInterestTypeChanged = { viewModel.setInterestType(it) },
                onPrincipalChanged = { viewModel.onPeriodPrincipalChanged(it) },
                onRateChanged = { viewModel.onPeriodRateChanged(it) },
                onRateTypeChanged = { viewModel.onRateTypeChanged(it) },
                onYearsChanged = { viewModel.onPeriodYearsChanged(it) },
                onMonthsChanged = { viewModel.onPeriodMonthsChanged(it) },
                onDaysChanged = { viewModel.onPeriodDaysChanged(it) },
                onSelectPreset = { y, m, d -> viewModel.setPeriodPreset(y, m, d) },
                onCalculateClicked = { viewModel.calculateAndShowResults() },
                onResetClicked = { viewModel.resetPeriodCalculator() }
            )
        }

        AppScreen.RESULTS -> {
            ResultsScreen(
                result = uiState.currentResult,
                onNavigateBack = {
                    if (uiState.calculationMode == CalculationMode.DATE_RANGE) {
                        viewModel.navigateToDateCalculator(uiState.selectedInterestType)
                    } else {
                        viewModel.navigateToPeriodCalculator()
                    }
                },
                onRecalculate = { viewModel.recalculateFromResults() },
                onGoHome = { viewModel.navigateToHome() }
            )
        }
    }
}
