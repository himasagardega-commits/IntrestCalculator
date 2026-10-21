# Interest Calculator - Android Application

A lightweight, modern, offline Android application for calculating **Simple Interest** and **Compound Interest** (with annual compounding and partial-year proportional calculations), supporting both **Calendar Dates** (Start & End Date) and **Specified Time Periods** (Days, Months, Years).

Developed using **Kotlin**, **Jetpack Compose**, and **Material Design 3**, targeting Android 8.0 (API level 26) through Android 14+ (API level 34).

---

## 📱 Deliverables Summary

- **Installable Release APK**: [`InterestCalculator-release.apk`](file:///d:/Cal/InterestCalculator-release.apk)
- **Direct Build Location**: [`app/build/outputs/apk/release/app-release.apk`](file:///d:/Cal/app/build/outputs/apk/release/app-release.apk)
- **Complete Source Code**: [`d:/Cal/`](file:///d:/Cal/)
- **One-Click Rebuild Script**: [`build_apk.bat`](file:///d:/Cal/build_apk.bat)
- **Automated Unit Tests**: [`InterestCalculatorEngineTest.kt`](file:///d:/Cal/app/src/test/java/com/interestcalculator/app/InterestCalculatorEngineTest.kt)

---

## 🌟 Key Features

1. **Clean Navigation & Home Screen**:
   - Two prominent selection buttons: **Simple Interest** and **Compound Interest**.
   - Distinct third option: **Specified Time Period**.
2. **Mode A: Start Date & End Date**:
   - Interactive Material 3 Date Pickers for Start Date and End Date.
   - End Date defaults to current date.
   - **"Use Today's Date"** toggle for automatic synchronization with current date.
   - Automatic validation: prevents End Date from being earlier than Start Date.
   - Live recalculation as inputs or dates change.
   - Detailed duration display in total calendar days and human-readable years, months, and remaining days.
3. **Mode B: Specified Time Period**:
   - Fixed-duration calculation in **Years**, **Months**, and **Days** (or combinations such as *1 year and 90 days*).
   - Standardized conversions: `1 Year = 365 Days`, `1 Month = 30 Days`.
   - Quick one-tap preset chips: *45 Days*, *6 Months*, *1 Year*, *2 Years*.
4. **Precision Financial Math**:
   - Zero floating-point rounding errors (`BigDecimal` with `MathContext.DECIMAL128`).
   - Annual compounding for full 365-day years with proportional interest on remaining days.
   - Guaranteed balance equation: `Total Amount = Original Principal + Total Interest`.
5. **Indian Rupee Formatting**:
   - Pre-formatted with `₹` currency symbol and standard Indian comma grouping (e.g. `₹10,000.00`, `₹1,00,000.00`, `₹12,34,567.89`).
6. **Detailed Results & Compounding Schedule**:
   - Large, prominent display cards for Total Amount and Total Interest.
   - Full breakdown: Principal, Rate, Dates, Total Days, and readable duration.
   - Expandable **Annual Compounding Schedule** detailing Opening Balance, Interest Earned, and Closing Balance for each period.
   - One-tap **Copy Summary** and **Share** options.
7. **100% Offline & Private**:
   - Zero internet permissions, zero background telemetry, zero ads, no login or registration.

---

## 📐 Mathematical Formulas and Calculation Logic

### 1. Duration Calculation (Date Difference Rule)
- Calendar difference uses standard date subtraction: **Start date is excluded and End date is included**.
- For example:
  - From `2024-01-01` to `2024-01-01` = **0 days**
  - From `2024-01-01` to `2024-01-02` = **1 day**
  - From `2024-01-01` to `2025-01-01` = **366 days** (leap year)

### 2. Simple Interest Formula
$$\text{Interest} = \frac{\text{Principal} \times \text{Annual Rate} \times \text{Number of Days}}{100 \times 365}$$

Where:
- $\text{Principal}$ is the initial capital.
- $\text{Annual Rate}$ is the interest rate percentage per annum.
- $\text{Number of Days}$ is the elapsed duration.
- The principal remains constant throughout the entire duration. Even when duration exceeds 365 days, simple interest is never added to the principal.
- Example: Principal = ₹10,000, Rate = 12%, Days = 730:
  $$\text{Interest} = \frac{10000 \times 12 \times 730}{100 \times 365} = ₹2,400.00$$
  $$\text{Total Amount} = ₹10,000 + ₹2,400 = ₹12,400.00$$

### 3. Compound Interest Formula (Annual Compounding)
Annual compounding algorithm:
1. Set $\text{Balance}_0 = \text{Principal}$.
2. For each complete 365-day period $k \in \{1, \dots, \lfloor\text{Days}/365\rfloor\}$:
   $$\text{Interest}_k = \text{Balance}_{k-1} \times \frac{\text{Rate}}{100}$$
   $$\text{Balance}_k = \text{Balance}_{k-1} + \text{Interest}_k$$
3. For remaining days $R = \text{Days} \pmod{365}$:
   $$\text{Interest}_{\text{rem}} = \text{Balance}_{\text{final}} \times \frac{\text{Rate}}{100} \times \frac{R}{365}$$
   $$\text{Total Balance} = \text{Balance}_{\text{final}} + \text{Interest}_{\text{rem}}$$
4. Final values:
   $$\text{Total Interest} = \text{Total Balance} - \text{Principal}$$
   $$\text{Total Amount} = \text{Principal} + \text{Total Interest}$$
- Example: Principal = ₹10,000, Rate = 12%, Days = 400:
  - First 365 days: ₹10,000 × 12% = ₹1,200.00 (Balance = ₹11,200.00)
  - Remaining 35 days: ₹11,200 × 12% × (35 / 365) = ₹128.8767...
  - Total Interest: ₹1,328.88
  - Total Amount: ₹11,328.88

### 4. Specified Time Period Conversions
- `1 Year = 365 Days`
- `1 Month = 30 Days`
- `Days = Days`
- Combined duration: $\text{Total Days} = (\text{Years} \times 365) + (\text{Months} \times 30) + \text{Days}$.

---

## 🏗️ Architecture & Project Structure

The project strictly decouples financial calculation logic from the UI layer:

```
d:\Cal\
├── build_apk.bat                   # 1-click build script
├── gradlew.bat / gradlew           # Gradle Wrapper 8.6
├── settings.gradle.kts             # Gradle root settings
├── build.gradle.kts                # Root build configuration
├── local.properties                # Android SDK path definition
├── InterestCalculator-release.apk  # Installable release APK
└── app\
    ├── build.gradle.kts            # App module build dependencies
    └── src\
        ├── test\java\com\interestcalculator\app\
        │   └── InterestCalculatorEngineTest.kt   # 15 automated test cases
        └── main\
            ├── AndroidManifest.xml
            ├── res\
            │   ├── values\ (strings.xml, colors.xml, themes.xml)
            │   ├── drawable\ (ic_launcher_background.xml, ic_launcher_foreground.xml)
            │   └── mipmap-anydpi-v26\ (ic_launcher.xml)
            └── java\com\interestcalculator\app\
                ├── MainActivity.kt               # Edge-to-edge Compose host
                ├── data\
                │   ├── InterestType.kt           # SIMPLE, COMPOUND
                │   ├── CalculationMode.kt        # DATE_RANGE, SPECIFIED_PERIOD
                │   ├── TimeUnit.kt               # DAYS, MONTHS, YEARS multipliers
                │   └── CalculationResult.kt      # Immutable result and schedule data
                ├── engine\
                │   └── InterestCalculatorEngine.kt # Financial calculations & Indian currency formatting
                ├── viewmodel\
                │   └── CalculatorViewModel.kt    # StateFlow-driven UI state machine
                └── ui\
                    ├── theme\ (Color.kt, Theme.kt, Type.kt)
                    ├── components\
                    │   ├── AppTopBar.kt          # Material 3 TopAppBar with back support
                    │   ├── CurrencyInputField.kt # Input field with ₹ prefix & clear button
                    │   └── DatePickerModal.kt    # Material 3 DatePickerDialog with date validation
                    └── screens\
                        ├── HomeScreen.kt         # Mode selection cards
                        ├── DateCalculatorScreen.kt # Mode A (Calendar dates)
                        ├── PeriodCalculatorScreen.kt # Mode B (Specified time periods)
                        └── ResultsScreen.kt      # Detailed report & compounding table
```

---

## 🧪 Automated Unit Tests

All 15 required scenarios from the prompt are verified in [`InterestCalculatorEngineTest.kt`](file:///d:/Cal/app/src/test/java/com/interestcalculator/app/InterestCalculatorEngineTest.kt):

| Test Case | Scenario Description | Expected Outcome | Status |
|:---:|:---|:---|:---:|
| **1** | Zero-day duration | Interest = ₹0.00, Total = Principal | Passed |
| **2** | Simple interest for 365 days (₹10,000 @ 12%) | Interest = ₹1,200.00 | Passed |
| **3** | Simple interest for 730 days (non-compounding) | Interest = ₹2,400.00, Total = ₹12,400.00 | Passed |
| **4** | Compound interest for 365 days (1 full cycle) | Interest = ₹1,200.00, Total = ₹11,200.00 | Passed |
| **5** | Compound interest for 400 days (365d + 35d) | Interest = ₹1,328.88, Total = ₹11,328.88 | Passed |
| **6** | Compound interest for 730 days (2 full cycles) | Interest = ₹2,544.00, Total = ₹12,544.00 | Passed |
| **7** | Zero interest rate (0%) | Interest = ₹0.00, Total = Principal | Passed |
| **8** | One-day duration (₹36,500 @ 10%) | Interest = ₹10.00, Total = ₹36,510.00 | Passed |
| **9** | End date equal to start date | Duration = 0 days | Passed |
| **10** | End date earlier than start date | Throws `IllegalArgumentException` | Passed |
| **11** | Specified period of 45 days | Duration = 45 days | Passed |
| **12** | Specified period of 6 months | Duration = 180 days (6 × 30) | Passed |
| **13** | Specified period of 2 years | Duration = 730 days (2 × 365) | Passed |
| **14** | Large values (₹100 Trillion @ 50% for 5 years) | Handled safely without overflow | Passed |
| **15** | Mathematical identity verification | $\text{Total} = \text{Principal} + \text{Interest}$ | Passed |

Run the test suite anytime:
```powershell
gradlew.bat test
```

---

## 🚀 How to Build the APK

### Prerequisites
- JDK 17+ (installed in `d:\tools\jdk-17.0.10+7`)
- Android SDK Platform 34 & Build-Tools 34.0.0 (installed in `d:\tools\android-sdk`)

### Option A: Using the 1-Click Script
Double-click [`build_apk.bat`](file:///d:/Cal/build_apk.bat) or execute in PowerShell:
```cmd
.\build_apk.bat
```

### Option B: Using Gradle Command Line
```powershell
$env:JAVA_HOME = "d:\tools\jdk-17.0.10+7"
$env:ANDROID_HOME = "d:\tools\android-sdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:PATH"

.\gradlew.bat assembleRelease
```
The signed APK will be output at:
`d:\Cal\app\build\outputs\apk\release\app-release.apk`

---

## 📲 How to Install the APK on an Android Phone

1. Transfer [`InterestCalculator-release.apk`](file:///d:/Cal/InterestCalculator-release.apk) to your Android device via USB, WhatsApp, Google Drive, or Bluetooth.
2. On your Android device, tap the downloaded APK file.
3. If prompted with *"Install unknown apps"*, enable permission for the app (e.g. Chrome or Files) to install APKs.
4. Tap **Install** and open **Interest Calculator**!
