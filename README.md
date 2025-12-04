# Expense Tracker: The Gamified Budgeting App

Welcome to Expense Tracker, a friendly and privacy-focused budgeting app for Android that helps you manage your money by turning financial planning into an engaging game. This app is built entirely with modern Android technologies, including Kotlin and Jetpack Compose.

The core philosophy of this app is to keep you motivated. A friendly mascot reacts to your spending habits, and a unique "commitment" system with penalty points encourages you to stick to your budget plan. All your data is stored locally on your device, ensuring complete privacy.

## Meet Your Budgeting Mascot!

Your financial journey is guided by a mascot that visually represents your progress.
- **Happy:** When you're well within your budget.
- **Nervous:** When you're getting close to your limit.
- **Sad:** When you've overspent.

This immediate visual feedback makes it easy and fun to stay on track!

## ✨ Features

- **Set Monthly Budgets:** Easily define budgets for various expense categories (e.g., Groceries, Transport).
- **Track Spending:** A clear table shows your spending for the current and previous months, with progress bars for each category.
- **Dynamic Categories:** Add your own custom expense categories at any time.
- **Commitment Periods:** Challenge yourself by setting a "commitment period." Changing your budget during this time incurs penalty points, encouraging mindful planning.
- **100% Private:** All financial data is stored locally on your device in a simple CSV file and SharedPreferences. No data is ever sent to a server.

## 🛠️ Tech Stack

- **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) for a fully declarative and modern UI.
- **Language:** 100% [Kotlin](https://kotlinlang.org/), including Coroutines and Flow.
- **Architecture:** State is managed locally using `remember` and `mutableStateOf`, with data persisted in `SharedPreferences` and a local CSV file.
- **Styling:** [Material 3](https://m3.material.io/) components and themes.
- **Layouts:** Uses `FlowRow` for responsive button layouts.

## 🚀 How It Works

The app operates in two main modes:

1.  **Budget Setup:** On the first run, the user is prompted to set up their monthly budgets for various categories and define a commitment period. They can return to this screen at any time to "Edit Budget."
2.  **Main Dashboard:** This screen provides a complete overview of the user's finances, including:
    - The reactive mascot.
    - A summary table of expenses.
    - An overall progress bar for the month's total budget.
    - Buttons to log new expenses for each category.

All data is read from and written to `SharedPreferences` (for budgets and settings) and a local `expenses.csv` file (for transaction history).

## 📂 Getting Started

To build and run this project locally, follow these steps:

1.  Clone the repository:
    
