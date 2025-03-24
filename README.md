# Fortis Bank `System`
# Project Architecture Overview


---

## `com.fortisbank` Package Breakdown

### 1. `business.services`
**Purpose:** Contains the business logic of the application.

- Implements services like `AccountService`, `CustomerService`, `TransactionService`, etc.
- Each service uses a corresponding repository (defined in `repositories`) to fetch or persist data.
- Interfaces like `IAccountService` define contracts, and implementations like `AccountService` fulfill them.

**Depends on:**
- `repositories` (for data access)
- `models` (for data types and business entities)

---

### 2. `data.database`
**Purpose:** Manages the connection to the database.

- `DatabaseConnection` and `IDatabaseConnection` handle low-level DB access logic.

**Used by:**
- `repositories` that interact with relational data.

---

### 3. `data.file`
**Purpose:** Handles file-based operations.

- `FileManager` and `FileRepository` are used to read/write data to files (e.g., reports, logs).

**Used by:**
- `repositories` using file-based storage (e.g., `AccountRepositoryFile`)
- `utils.ReportExporter`

---

### 4. `repositories`
**Purpose:** Acts as the Data Access Layer (DAL).

- Includes both database and file-based repositories for Accounts, Customers, and Transactions.
- `IAccountRepository`, `ICustomerRepository`, etc. are interfaces for abstraction.
- `RepositoryFactory` dynamically provides the correct repository depending on the chosen `StorageMode`.

**Used by:**
- `services` to abstract data storage
- `ui` indirectly via services

---

### 5. `models`
**Purpose:** Defines the domain models/entities used across the app.

**Sub-packages:**
- `accounts`: `Account`, `CheckingAccount`, `CreditAccount`, etc.
- `transactions`: `Transaction`, `DepositTransaction`, `WithdrawalTransaction`, etc.
- `reports`: `Report`, `CustomerStatementReport`, etc.
- `collections`: Helper collections like `AccountList`, `CustomerList`
- `BankManager`, `Customer`: user entities.

**Used by:**
- `services`, `repositories`, `ui`

---

### 6. `ui`
**Purpose:** Contains the user interface logic.

- UIs like `CustomerUI`, `BankManagerUI`, `TransactionUI`, etc. represent views for different users.
- Uses `services` to retrieve/update data and `models` to display data.

---

### 7. `utils`
**Purpose:** General-purpose utility classes.

- `Constants`, `Logger`, `IdGenerator`, `ReportExporter`, `SecurityUtils`, etc.
- Used throughout the app for things like generating IDs, exporting reports, logging, etc.

---
---
# UPDATES SECTION
---
### Update March 23 - 

## `ReportExporter` Utility (Package: `com.fortisbank.utils`)

This utility class provides functionality to export banking reports to **CSV files**, making them easily viewable in **Excel** and other spreadsheet applications.

---

### ✦ Responsibilities

#### **CustomerStatementReport Export**
- Outputs a detailed monthly statement for a specific customer.
- Includes report metadata (ID, period, balances).
- Lists all related transactions with correctly signed amounts based on customer account context.

#### **BankSummaryReport Export**
- Outputs a high-level summary of the bank’s overall status.
- Includes total customers, account types, total balance, credit usage, and collected fees.
- Provides a breakdown of low-balance accounts (< $50) for financial risk monitoring.

---

### ✦ Notes
- Report files are saved in `.csv` format for **Excel compatibility**.
- Uses a simple `FileWriter` approach for lightweight and readable output.
- Automatically handles **contextual transaction signing** based on the accounts involved.

---

### refactor: redesign report system as runtime-generated logic, not persistent data

- Replaced the old Serializable Report model with an abstract Report base class
- Reports are now fully dynamic, generated at runtime from repositories
- No longer treated as persistent data — reports are NOT stored in the database
- Created specialized report classes:
  - CustomerStatementReport: per-customer monthly activity
  - BankSummaryReport: global snapshot of all customers, accounts, and transactions
- Updated ReportService to centralize report generation logic
- Promotes clean separation of responsibilities: data is retrieved, aggregated, and presented without altering persistence layers
- Future-ready for export capabilities (PDF, CSV, JSON) using polymorphism on the Report base class
- Added UML diagram to help the team understand the new report flow
![image](https://github.com/user-attachments/assets/2e80cba7-e2e4-4e8e-be5f-b68dd9730a55)


### refactor: integrate custom collection classes into all repositories

- Replaced List<T> with model-specific collection classes:
  - AccountList in AccountRepository
  - CustomerList in CustomerRepository
  - TransactionList in TransactionRepository
- Updated method return types in repositories and interfaces accordingly
- Improved readability and future extensibility (e.g., filtering, sorting, aggregating)
- Collection classes are located in models.collections and extend ArrayList<T>
- Ensures consistency and encapsulation of collection-level logic per model type
![image](https://github.com/user-attachments/assets/04e331e9-8e93-4255-b93c-c6abc2eac89c)


### feat: add file-based repository system with switchable storage mode

- Implemented CustomerRepositoryFile with file serialization support
- Created FileManager utility for generic file I/O operations
- Added FileRepository<T> abstract base class for reuse
- Introduced StorageMode enum to toggle between FILE and DATABASE modes
- Implemented RepositoryFactory to return appropriate repository implementation
- Cleaned up structure for modular, pluggable storage architecture
- Added Markdown documentation and PlantUML diagram for repository flow
![image](https://github.com/user-attachments/assets/11cc633d-1221-487d-9d23-8bf68f8d141a)

---

## Updates March 20:

### Refactored Transaction System and Updated Related Repositories

- Modularized `Transaction` by introducing an `abstract class` and `TransactionInterface`.
- Replaced `String transactionType` with `TransactionType` Enum for better type safety.
- Created specialized transaction subclasses: `DepositTransaction`, `WithdrawalTransaction`, `TransferTransaction`, and `FeeTransaction`.
- Implemented `TransactionFactory` to handle dynamic transaction creation.
- Removed `fees` field from `Transaction` as fees are now separate transactions.
- Updated `Account` model:
  - Replaced `String accountType` with `AccountType` Enum.
  - Removed `transactionFees` field (now handled via `FeeTransaction`).
  - Updated `applyFees()` to use `TransactionFactory` for creating fee transactions.
- Refactored `TransactionRepository`:
  - Updated SQL queries to use `TransactionType` Enum.
  - Replaced direct instantiation with `TransactionFactory`.
  - Improved logging with `Logger`.
- Refactored `AccountRepository`:
  - Updated `mapResultSetToAccount()` to instantiate correct account subclasses.
  - Standardized SQL queries and improved error handling.

This refactoring improves modularity, maintainability, and ensures consistency across the banking system.

![image](https://github.com/user-attachments/assets/6a5efe75-3826-45d3-956d-2bd6fbbaed4a)

---

### Refactored Account System with Transaction Integration and Currency Support

- Implemented `AccountInterface` to enforce core account operations.
- Updated `Account` abstract class to:
  - Use transactions for all deposits, withdrawals, and transfers.
  - Ensure all balance modifications are logged with `TransactionFactory`.
  - Standardize fee applications using `applyFees()`.
  - Introduce `closeAccount()` validation to prevent closure with a non-zero balance.

- Refactored all account subclasses:
  - `CheckingAccount`: Now enforces free transaction limits and applies fees after the threshold.
  - `SavingsAccount`: Supports automatic interest application.
  - `CreditAccount`: Implements credit limits and interest application.
  - `CurrencyAccount`: Now integrates with `CurrencyType` for exchange rates.
  - All account types now use `super.deposit()` and `super.withdraw()` to ensure transaction logging.

- Introduced `CurrencyType`:
  - Manages exchange rates with a singleton pattern.
  - Supports adding, updating, and removing currency values dynamically.
  - Ensures accuracy in foreign currency deposits and withdrawals.

- Updated `CurrencyAccount` to:
  - Use `CurrencyType` for real-time exchange rate conversion.
  - Implement currency-specific deposit and withdrawal functionality.
  - Track last activity to enable auto-closing inactive accounts.

- Improved modularity by integrating `AccountFactory`:
  - Supports dynamic account creation based on `AccountType`.
  - Ensures correct parameters are used for each account type.

- Refactored `AccountRepository`:
  - Now uses `AccountFactory` to instantiate accounts.
  - Improved transaction tracking with `recordTransaction()` and `getTransactionsForAccount()`.
  - Integrated `CurrencyType` for accurate exchange rate retrieval in `CurrencyAccount`.

- Updated `IAccountRepository`:
  - Added `recordTransaction(Transaction transaction)` to store transactions related to accounts.
  - Added `getTransactionsForAccount(String accountId)` to retrieve an account’s transaction history.
  - Ensures all repositories support transaction logging and retrieval.

- Improved logging in `AccountRepository` for better debugging.
- Added updated PlantUML class diagram to reflect new architecture.

This update ensures transaction-driven operations for all accounts, enables accurate currency exchange handling, and improves system maintainability.
![image](https://github.com/user-attachments/assets/3dde4cf0-17c2-4093-9e00-952093fc627c)

---

