// ============================================================================
// TASK 03: E-Commerce Shipping Cost & Dispatch System
// Combines Strategy (shipping rate calculation) + Facade (packaging & dispatch)
// ============================================================================

// Strategy Interface: Calculates shipping cost from weight and distance
interface ShippingRateStrategy {
    double calculateRate(double weightKg, double distanceKm);
}

// Concrete Strategy 1: Low-cost ground delivery
class StandardGroundStrategy implements ShippingRateStrategy {
    public double calculateRate(double weightKg, double distanceKm) {
        return 5.0 + (weightKg * 0.5) + (distanceKm * 0.02);
    }
}

// Concrete Strategy 2: Faster air freight pricing
class ExpressAirStrategy implements ShippingRateStrategy {
    public double calculateRate(double weightKg, double distanceKm) {
        return 15.0 + (weightKg * 1.5) + (distanceKm * 0.08);
    }
}

// Concrete Strategy 3: Premium local courier pricing
class SameDayCourierStrategy implements ShippingRateStrategy {
    public double calculateRate(double weightKg, double distanceKm) {
        return 25.0 + (weightKg * 2.0) + (distanceKm * 0.15);
    }
}

// Subsystem 1: Chooses box dimensions based on package weight
class BoxSelector {
    public String selectBox(double weightKg) {
        String box = weightKg > 10 ? "Large Reinforced Box" : "Standard Medium Box";
        System.out.println("[BoxSelector] Selected: " + box);
        return box;
    }
}

// Subsystem 2: Executes rate computation using the active strategy
class RateCalculator {
    public double compute(ShippingRateStrategy strategy, double weight, double distance) {
        double cost = strategy.calculateRate(weight, distance);
        System.out.printf("[RateCalculator] Computed cost: $%.2f%n", cost);
        return cost;
    }
}

// Subsystem 3: Prints physical shipping label
class LabelPrinter {
    public void printLabel(String orderId, String box, double cost) {
        System.out.printf("[LabelPrinter] Order #%s | %s | Postage: $%.2f%n", orderId, box, cost);
    }
}

// Facade: Orchestrates box selection, rate calculation, and label printing
class ShippingFacade {
    private final BoxSelector boxSelector = new BoxSelector();
    private final RateCalculator rateCalculator = new RateCalculator();
    private final LabelPrinter labelPrinter = new LabelPrinter();
    private ShippingRateStrategy strategy;

    public ShippingFacade(ShippingRateStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(ShippingRateStrategy strategy) {
        this.strategy = strategy;
    }

    public void dispatchOrder(String orderId, double weightKg, double distanceKm) {
        String box = boxSelector.selectBox(weightKg);
        double cost = rateCalculator.compute(strategy, weightKg, distanceKm);
        labelPrinter.printLabel(orderId, box, cost);
    }
}

// Client Driver
public class Task03Main {
    public static void main(String[] args) {
        System.out.println("\n=== TASK 03: E-Commerce Shipping Cost & Dispatch System ===");
        ShippingFacade shipping = new ShippingFacade(new StandardGroundStrategy());
        shipping.dispatchOrder("ORD-101", 4.5, 300);

        shipping.setStrategy(new ExpressAirStrategy());
        shipping.dispatchOrder("ORD-102", 12.0, 850);
    }
}
