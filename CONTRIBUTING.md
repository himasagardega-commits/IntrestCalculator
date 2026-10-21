# Contributing to Interest Calculator

Thank you for your interest in contributing to **Interest Calculator**! We welcome contributions, bug reports, feature suggestions, and improvements from the community.

---

## 📋 Code of Conduct

Please be respectful, collaborative, and considerate when interacting with contributors and maintainers.

---

## 🛠️ Development Setup

1. **Prerequisites**:
   - Android Studio (Hedgehog 2023.1.1 or later recommended)
   - Java Development Kit (JDK) 17
   - Android SDK 34 (Android 14)

2. **Clone the Repository**:
   ```bash
   git clone https://github.com/himasagardega-commits/IntrestCalculator.git
   cd IntrestCalculator
   ```

3. **Build the Project**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

---

## 📐 Mathematical Standards

All financial arithmetic within this repository must adhere to the following principles:
- **Zero Floating-Point Drift**: Never use standard `Double` or `Float` for currency calculations. All financial math is strictly executed using `BigDecimal` with `MathContext.DECIMAL128` and rounded to 2 decimal places using `RoundingMode.HALF_UP`.
- **Month-Based Time Conversion**:
  - Full months are counted from calendar days ($30\text{ days} = 1\text{ month}$).
  - Days are converted to fractional months: $\text{Months} = \lfloor\frac{\text{Days}}{30}\rfloor + \frac{\text{Days} \pmod{30}}{30}$.
- **Simple Interest Formula**:
  $$\text{Interest} = \frac{\text{Principal} \times \text{Time in Months} \times \text{Rate}}{100}$$
- **Currency Standard**: All monetary values are formatted with Indian numbering grouping (`₹##,##,##0.00`).

---

## 🏷️ Commit Message Conventions

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

- `feat:` Introduces a new user-facing feature
- `fix:` Fixes a bug or calculation discrepancy
- `docs:` Documentation updates only (e.g., README, API docs)
- `test:` Adding or updating unit tests
- `refactor:` Code refactoring without changing observable behavior
- `chore:` Build scripts, dependencies, CI configuration

---

## 🚀 Submitting Pull Requests

1. Fork the repository and create a descriptive branch:
   ```bash
   git checkout -b feat/your-feature-name
   ```
2. Ensure all 19+ unit tests pass:
   ```bash
   ./gradlew test
   ```
3. Commit with a clear, concise Conventional Commit message.
4. Push to your fork and submit a Pull Request targeting `main`.
