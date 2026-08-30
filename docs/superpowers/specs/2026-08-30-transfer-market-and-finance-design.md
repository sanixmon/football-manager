# Transfer Market, Contract Negotiations & Club Finance Design Spec

_Date: 2026-08-30 · Status: Approved · Architecture Path: Approach A (Clean Domain Decoupled Engine)_

---

## 1. Overview & Goals

This specification defines the **Transfer Market, Player Contract Negotiations, and Club Financial Management** subsystem for the Football Manager project. It introduces realistic transfer dynamics, contract negotiations, dynamic club budgets, player valuations, and dedicated Android management screens while strictly adhering to pure Kotlin JVM isolation in the `:engine` module and clean MVVM in `:app`.

### Key Capabilities:
1. **Deterministic Player Market Valuation**: Dynamic calculation of market value and expected wages based on position, age, overall rating, potential, and contract duration.
2. **Two-Phase Transfer Flow**:
   - **Phase 1: Club-to-Club Fee Negotiation**: Human or AI club submits transfer bids; selling club evaluates bids against player valuation and squad status (`ACCEPTED`, `COUNTERED`, `REJECTED`).
   - **Phase 2: Personal Terms Negotiation**: Offering wage, contract length (years), and promised squad status (`KEY_PLAYER`, `FIRST_TEAM`, `ROTATION`, `BACKUP`, `YOUTH`).
3. **Free Agents & Transfer Listed Status**:
   - Free agents (unattached players) available for contract negotiation anytime.
   - Transfer-listed players have discounted asking prices and higher AI selling willingness.
4. **Dynamic Club Budget & Board Controls**:
   - Club finances track `balance`, `transferBudget`, and `weeklyWageBudget`.
   - Transfer fees debit from `transferBudget` and player sales return 80% to the transfer budget.
   - Board blocks bids or contracts exceeding available transfer or wage budgets.
5. **Seasonal Transfer Windows**:
   - Summer/Pre-season Window and Mid-season Window restrict club-to-club transfers; Free Agents can be signed anytime.
6. **Dedicated UI Screens in `:app`**:
   - **`ScoutingScreen`**: Player search, position/rating/value filters, and scout summaries.
   - **`TransfersScreen`**: Transfer Hub, pending deals, transfer listed players, and league transfer history.
   - **`FinancesScreen`**: Balance summary, transfer/wage budget utilization bars, and payroll breakdown.
   - **Interactive Modals**: Transfer Fee Bidding Dialog & Contract Offer Dialog.

---

## 2. Architecture & Module Boundaries

```
football-manager/
├── engine/src/main/kotlin/com/footballmanager/
│   ├── model/
│   │   ├── TransferOffer.kt          # TransferBid, TransferStatus, DealPhase, BidStatus
│   │   ├── ContractOffer.kt          # ContractOffer terms & PlayerDecision
│   │   └── TransferWindow.kt         # Window schedule (Open/Closed)
│   ├── calculator/
│   │   ├── TransferValuationCalculator.kt  # Deterministic market value calculation
│   │   └── WageExpectationCalculator.kt    # Expected wage based on rating & squad status
│   ├── usecase/
│   │   ├── SubmitTransferBidUseCase.kt     # Validate budget & dispatch bid
│   │   ├── EvaluateTransferOfferUseCase.kt # AI selling club decision logic
│   │   ├── NegotiateContractUseCase.kt     # AI player contract decision logic
│   │   └── CompleteTransferUseCase.kt      # Atomic squad transfer & balance update
│   └── simulation/season/
│       └── SeasonState.kt                  # Extended with activeBids and transferHistory
└── app/src/main/kotlin/com/footballmanager/app/
    ├── ui/screens/
    │   ├── ScoutingScreen.kt               # Player search & filter table
    │   ├── TransfersScreen.kt              # Active deals, transfer list, history
    │   └── FinancesScreen.kt               # Balance, budgets, payroll breakdown
    ├── ui/components/
    │   ├── TransferBidDialog.kt            # Fee bidding modal
    │   └── ContractOfferDialog.kt          # Wage & duration offer modal
    └── ui/viewmodel/
        ├── GameViewModel.kt                # State flow & repository persistence
        └── GameUiState.kt                  # UI state additions for transfers & finance
```

---

## 3. Domain Model Specifications

### 3.1 Transfer Status & Enums
```kotlin
@Serializable
enum class TransferListingStatus {
    NONE,
    TRANSFER_LISTED,
    LOAN_LISTED,
    NOT_FOR_SALE,
}

@Serializable
enum class BidStatus {
    PENDING,
    ACCEPTED_BY_CLUB,
    REJECTED_BY_CLUB,
    TERMS_OFFERED,
    ACCEPTED_BY_PLAYER,
    REJECTED_BY_PLAYER,
    COMPLETED,
    CANCELLED,
}

@Serializable
data class TransferBid(
    val id: Long,
    val playerId: Long,
    val buyingClubId: Long,
    val sellingClubId: Long?, // null for Free Agents
    val feeOffered: Long,
    val status: BidStatus = BidStatus.PENDING,
    val dateSubmitted: LocalDate,
    val contractOffer: ContractOffer? = null,
)

@Serializable
data class ContractOffer(
    val weeklyWage: Long,
    val contractYears: Int,
    val squadStatus: SquadStatus,
    val signingBonus: Long = 0L,
)

@Serializable
data class TransferRecord(
    val id: Long,
    val playerId: Long,
    val playerName: String,
    val fromClubId: Long?,
    val toClubId: Long,
    val fee: Long,
    val date: LocalDate,
)
```

---

## 4. Valuation Formulas (`PlayerCalculator` Extensions)

### 4.1 Market Value Formula
The market value $V$ is calculated deterministically:
$$V = \text{BaseValue}(Rating) \times \text{AgeMultiplier}(Age) \times \text{ContractMultiplier}(MonthsRemaining) \times \text{ListingDiscount}$$

* **Base Value**:
  * Rating $< 60$: $50{,}000$ – $250{,}000$
  * Rating $60–69$: $250{,}000$ – $1{,}500{,}000$
  * Rating $70–79$: $1{,}500{,}000$ – $8{,}000{,}000$
  * Rating $80–89$: $8{,}000{,}000$ – $30{,}000{,}000$
  * Rating $\ge 90$: $30{,}000{,}000+$
* **Age Multiplier**:
  * Age $< 21$: $1.35$ (High potential premium)
  * Age $21–25$: $1.20$ (Prime development)
  * Age $26–29$: $1.00$ (Peak years)
  * Age $30–33$: $0.70$ (Declining resale value)
  * Age $\ge 34$: $0.40$ (Veteran)
* **Contract Multiplier**:
  * $< 6$ months remaining: $0.50$ (Impending free agent)
  * $6–12$ months: $0.75$
  * $1–3$ years: $1.00$
  * $> 3$ years: $1.15$
* **Listing Status**:
  * `TRANSFER_LISTED`: $0.80$ (20% discount)
  * `FREE_AGENT`: $0$ fee

### 4.2 Wage Expectation Formula
$$\text{ExpectedWeeklyWage} = \text{BaseWage}(Rating) \times \text{SquadStatusFactor}$$
* Base Wage scales with rating (e.g., rating 70 $\approx \$1{,}500$/wk, rating 80 $\approx \$8{,}000$/wk, rating 88 $\approx \$25{,}000$/wk).
* `SquadStatus.KEY_PLAYER`: $1.25\times$
* `SquadStatus.FIRST_TEAM`: $1.05\times$
* `SquadStatus.ROTATION`: $0.90\times$
* `SquadStatus.BACKUP`: $0.70\times$

---

## 5. Domain Use Cases

### 5.1 `SubmitTransferBidUseCase`
* **Inputs**: `buyingClubId`, `playerId`, `feeOffered`.
* **Validation**:
  * Buyer must have `transferBudget >= feeOffered`.
  * Window must be OPEN (or player is a Free Agent).
* **Output**: Created `TransferBid` registered to `SeasonState.activeBids`.

### 5.2 `EvaluateTransferOfferUseCase` (Selling Club AI)
* **Inputs**: `TransferBid`, `player`, `sellingClub`.
* **Decision Rules**:
  * If `feeOffered >= marketValue * 1.05`: `ACCEPTED_BY_CLUB`.
  * If player is `TRANSFER_LISTED` and `feeOffered >= marketValue * 0.85`: `ACCEPTED_BY_CLUB`.
  * If `feeOffered in (marketValue * 0.75 .. marketValue * 1.04)`: Returns counter-offer fee.
  * If `feeOffered < marketValue * 0.75`: `REJECTED_BY_CLUB`.

### 5.3 `NegotiateContractUseCase` (Player AI)
* **Inputs**: `player`, `ContractOffer`, `buyingClub`.
* **Decision Rules**:
  * Evaluates if offered wage $\ge 90\%$ of expected wage.
  * Player accepts if wage meets expectations and squad status is acceptable for player's rating.
  * Rejects if wage is significantly under market ($< 80\%$) or squad status too low.

### 5.4 `CompleteTransferUseCase`
* **Atomic Execution**:
  1. Deduct `fee` from `buyingClub.finance.transferBudget` and `balance`.
  2. Add $80\% \times \text{fee}$ to `sellingClub.finance.transferBudget` and full fee to `balance`.
  3. Remove player ID from `sellingClub.squad.playerIds` (if applicable).
  4. Add player ID to `buyingClub.squad.playerIds`.
  5. Update `Player.contract` with new wage, expiration date, and squad status.
  6. Add record to `SeasonState.transferHistory`.
  7. Remove bid from `SeasonState.activeBids`.

---

## 6. Android UI Specifications (`:app`)

### 6.1 `ScoutingScreen.kt`
* **Header Filter Bar**: Search textfield, Position dropdown filter (All, GK, DEF, MID, ATT), Age range slider, Max Value filter, Status chip (All, Listed, Free Agent).
* **Dense Player Table**: Name, Club, Position, Age, Overall, Fitness, Morale, Market Value, Wage, Action ("Bid" button).

### 6.2 `TransfersScreen.kt`
* **Sub-Tabs**:
  * **Active Deals**: Live incoming/outgoing bids with status badges (`Pending Club`, `Offer Terms`, `Accepted`, `Completed`).
  * **Transfer Listed**: Available players listed for immediate discount sale.
  * **History**: Season-wide transfer log showing date, player, clubs, and transfer fee.

### 6.3 `FinancesScreen.kt`
* **Club Overview Cards**: Total Balance, Transfer Budget, Weekly Wage Budget.
* **Payroll Utilization Progress Bar**: Current Weekly Spend vs Weekly Budget (Color-coded: Green $<85\%$, Amber $85–99\%$, Red $\ge 100\%$).
* **Payroll Breakdown Table**: Wage expenditure segmented by squad status (`Key Player`, `First Team`, `Rotation`, `Youth`).

### 6.4 Modals & Dialogs
* **`TransferBidDialog`**: Slider and quick buttons ($+\$100\text{k}$, $+\$500\text{k}$, Match Value) with live budget validation.
* **`ContractOfferDialog`**: Weekly wage input, contract duration picker (1–5 years), and squad status selector.

---

## 7. Quality & Verification Gates

1. **Architecture Fitness Enforcement**:
   - `ArchitectureFitnessTest` will assert zero Android/UI imports in all new usecases and calculators under `com.footballmanager.calculator` and `com.footballmanager.usecase`.
2. **Deterministic Reproducibility**:
   - AI transfer and contract decision evaluation must produce identical outcomes under fixed inputs.
3. **Integrity & Concurrency Tests**:
   - Balance conservation tests: Total monetary transfers between clubs must match transaction records.
   - Skuad containment tests: Player cannot belong to two clubs simultaneously post-transfer.
