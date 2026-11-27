// ============================================================================
// TASK 20: Portfolio Rebalancing & Tax-Loss Harvesting Engine
// Combines Strategy (target asset allocation) + Facade (drift, tax lots, orders)
// ============================================================================
import java.util.Map;

// Strategy Interface: Defines target portfolio weights (Equities vs Bonds)
interface AllocationStrategy {
    Map<String, Double> getTargetWeights();
}

class AggressiveEquityStrategy implements AllocationStrategy {
    public Map<String, Double> getTargetWeights() {
        return Map.of("EQUITIES", 0.85, "BONDS", 0.15);
    }
}

class BalancedIndexStrategy implements AllocationStrategy {
    public Map<String, Double> getTargetWeights() {
        return Map.of("EQUITIES", 0.60, "BONDS", 0.40);
    }
}

class CapitalPreservationStrategy implements AllocationStrategy {
    public Map<String, Double> getTargetWeights() {
        return Map.of("EQUITIES", 0.25, "BONDS", 0.75);
    }
}

// Subsystem 1: Fetches live market valuations
class MarketPriceFeed {
    public double getPortfolioValue(String accountId) {
        System.out.println("[MarketPriceFeed] Valuing portfolio for " + accountId + ": $100,000");
        return 100000.0;
    }
}

// Subsystem 2: Calculates allocation drift from target weights
class DriftAnalyzer {
    public double calculateEquityDrift(double currentEquityPct, double targetEquityPct) {
        double drift = targetEquityPct - currentEquityPct;
        System.out.printf("[DriftAnalyzer] Equity drift: %+.0f%%%n", drift * 100);
        return drift;
    }
}

// Subsystem 3: Identifies unrealized loss tax lots for tax-loss harvesting
class TaxLotEvaluator {
    public String selectTaxLossLot(String assetClass) {
        String lot = "LOT-2025-Q3 (Unrealized Loss: -$1,200)";
        System.out.println("[TaxLotEvaluator] Prioritizing tax-loss harvesting on " + lot);
        return lot;
    }
}

// Subsystem 4: Submits net buy/sell orders to brokerage
class OrderExecutionBroker {
    public void submitOrder(String side, String assetClass, double amount, String taxLotNote) {
        System.out.printf("[OrderExecutionBroker] %s $%.2f of %s [%s]%n",
                side, amount, assetClass, taxLotNote);
    }
}

// Facade: Coordinates valuation, drift analysis, tax-loss lot selection, and order execution
class PortfolioFacade {
    private final MarketPriceFeed priceFeed = new MarketPriceFeed();
    private final DriftAnalyzer driftAnalyzer = new DriftAnalyzer();
    private final TaxLotEvaluator taxLotEvaluator = new TaxLotEvaluator();
    private final OrderExecutionBroker broker = new OrderExecutionBroker();
    private AllocationStrategy strategy;

    public PortfolioFacade(AllocationStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(AllocationStrategy strategy) {
        this.strategy = strategy;
    }

    public void rebalanceAccount(String accountId) {
        double totalVal = priceFeed.getPortfolioValue(accountId);
        double currentEquityPct = 0.70; // Simulated current 70% equities
        double targetEquityPct = strategy.getTargetWeights().get("EQUITIES");

        double drift = driftAnalyzer.calculateEquityDrift(currentEquityPct, targetEquityPct);
        if (drift < 0) {
            double sellAmount = Math.abs(drift) * totalVal;
            String lossLot = taxLotEvaluator.selectTaxLossLot("EQUITIES");
            broker.submitOrder("SELL", "EQUITIES", sellAmount, lossLot);
            broker.submitOrder("BUY", "BONDS", sellAmount, "Standard Allocation");
        } else if (drift > 0) {
            double buyAmount = drift * totalVal;
            broker.submitOrder("BUY", "EQUITIES", buyAmount, "Standard Allocation");
        }
    }
}

// Client Driver
public class Task20Main {
    public static void main(String[] args) {
        System.out.println("\n=== TASK 20: Portfolio Rebalancing & Tax-Loss Harvesting ===");
        PortfolioFacade portfolio = new PortfolioFacade(new CapitalPreservationStrategy());
        portfolio.rebalanceAccount("ACC-8841");

        portfolio.setStrategy(new AggressiveEquityStrategy());
        portfolio.rebalanceAccount("ACC-8841");
    }
}
