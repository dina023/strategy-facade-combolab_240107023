// ============================================================================
// TASK 12: Retail Loyalty & Dynamic Pricing Engine
// Combines Strategy (discount rules) + Facade (cart, profile, coupon, points)
// ============================================================================

// Strategy Interface: Computes discounted cart total
interface DiscountStrategy {
    double applyDiscount(double subtotal, String loyaltyTier, int itemCount);
}

// Concrete Strategy 1: Flat seasonal percentage off
class SeasonalPercentageDiscount implements DiscountStrategy {
    private final double percent;
    public SeasonalPercentageDiscount(double percent) { this.percent = percent; }

    public double applyDiscount(double subtotal, String loyaltyTier, int itemCount) {
        return subtotal * (1.0 - percent);
    }
}

// Concrete Strategy 2: Discount scaled by customer membership tier
class TieredLoyaltyDiscount implements DiscountStrategy {
    public double applyDiscount(double subtotal, String loyaltyTier, int itemCount) {
        double rate = switch (loyaltyTier) {
            case "GOLD" -> 0.20;
            case "SILVER" -> 0.10;
            default -> 0.05;
        };
        return subtotal * (1.0 - rate);
    }
}

// Concrete Strategy 3: Bulk discount when buying 3 or more items
class BundleThresholdDiscount implements DiscountStrategy {
    public double applyDiscount(double subtotal, String loyaltyTier, int itemCount) {
        return itemCount >= 3 ? subtotal - 25.0 : subtotal;
    }
}

// Subsystem 1: Computes raw cart subtotal and item count
class CartAnalyzer {
    public double getSubtotal(double[] itemPrices) {
        double sum = 0;
        for (double p : itemPrices) sum += p;
        System.out.printf("[CartAnalyzer] Cart items: %d | Subtotal: $%.2f%n", itemPrices.length, sum);
        return sum;
    }
}

// Subsystem 2: Retrieves member loyalty tier
class MemberProfileService {
    public String getLoyaltyTier(String userId) {
        System.out.println("[MemberProfileService] User " + userId + " tier: GOLD");
        return "GOLD";
    }
}

// Subsystem 3: Validates promo/coupon eligibility
class CouponValidator {
    public boolean isEligible(String userId, double subtotal) {
        System.out.println("[CouponValidator] Verifying promotional eligibility...");
        return subtotal > 20.0;
    }
}

// Subsystem 4: Credits loyalty reward points after purchase
class PointsLedger {
    public void addPoints(String userId, double finalPaid) {
        int pts = (int) (finalPaid * 10);
        System.out.printf("[PointsLedger] Credited %d reward points to %s%n", pts, userId);
    }
}

// Facade: Finalizes cart pricing and updates loyalty points
class BillingFacade {
    private final CartAnalyzer cartAnalyzer = new CartAnalyzer();
    private final MemberProfileService memberService = new MemberProfileService();
    private final CouponValidator couponValidator = new CouponValidator();
    private final PointsLedger pointsLedger = new PointsLedger();
    private DiscountStrategy strategy;

    public BillingFacade(DiscountStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(DiscountStrategy strategy) {
        this.strategy = strategy;
    }

    public double finalizeCart(String userId, double[] itemPrices) {
        double subtotal = cartAnalyzer.getSubtotal(itemPrices);
        String tier = memberService.getLoyaltyTier(userId);
        double finalTotal = subtotal;

        if (couponValidator.isEligible(userId, subtotal)) {
            finalTotal = strategy.applyDiscount(subtotal, tier, itemPrices.length);
        }
        System.out.printf("[BillingFacade] Final payable total: $%.2f%n", finalTotal);
        pointsLedger.addPoints(userId, finalTotal);
        return finalTotal;
    }
}

// Client Driver
public class Task12Main {
    public static void main(String[] args) {
        System.out.println("\n=== TASK 12: Retail Loyalty & Dynamic Pricing Engine ===");
        double[] cart = {50.0, 40.0, 30.0};

        BillingFacade billing = new BillingFacade(new TieredLoyaltyDiscount());
        billing.finalizeCart("member_77", cart);

        billing.setStrategy(new BundleThresholdDiscount());
        billing.finalizeCart("member_77", cart);
    }
}
