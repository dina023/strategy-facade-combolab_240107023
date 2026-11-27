// ============================================================================
// TASK 08: Checkout & Payment Gateway Orchestrator
// Combines Strategy (payment methods) + Facade (lock -> tax -> pay -> receipt/unlock)
// ============================================================================

// Strategy Interface: Encapsulates distinct payment authorization flows
interface PaymentStrategy {
    boolean authorizeAndCharge(String userId, double totalAmount);
}

// Concrete Strategy 1: Credit Card authorization with CVV/3DS simulation
class CreditCardPayment implements PaymentStrategy {
    private final String cardNumber;
    public CreditCardPayment(String cardNumber) { this.cardNumber = cardNumber; }

    public boolean authorizeAndCharge(String userId, double totalAmount) {
        System.out.printf("[CreditCardPayment] Authorizing card %s for $%.2f...%n", cardNumber, totalAmount);
        return !cardNumber.endsWith("0000"); // Simulate decline if card ends in 0000
    }
}

// Concrete Strategy 2: Digital Wallet token authorization
class DigitalWalletPayment implements PaymentStrategy {
    private final String walletId;
    public DigitalWalletPayment(String walletId) { this.walletId = walletId; }

    public boolean authorizeAndCharge(String userId, double totalAmount) {
        System.out.printf("[DigitalWalletPayment] Charging wallet (%s) $%.2f...%n", walletId, totalAmount);
        return true;
    }
}

// Concrete Strategy 3: Direct ACH / Bank Transfer
class BankTransferPayment implements PaymentStrategy {
    private final String iban;
    public BankTransferPayment(String iban) { this.iban = iban; }

    public boolean authorizeAndCharge(String userId, double totalAmount) {
        System.out.printf("[BankTransferPayment] Initiating wire from %s for $%.2f...%n", iban, totalAmount);
        return true;
    }
}

// Subsystem 1: Reserves and releases stock to prevent overselling
class InventoryLockService {
    public void lockItem(String itemId, int qty) {
        System.out.printf("[InventoryLockService] Locked %d unit(s) of %s%n", qty, itemId);
    }
    public void releaseItem(String itemId, int qty) {
        System.out.printf("[InventoryLockService] RELEASED lock on %d unit(s) of %s due to failure%n", qty, itemId);
    }
}

// Subsystem 2: Computes applicable sales tax / VAT
class TaxEngine {
    public double calculateTax(double subtotal) {
        double tax = subtotal * 0.12;
        System.out.printf("[TaxEngine] Calculated tax: $%.2f%n", tax);
        return tax;
    }
}

// Subsystem 3: Executes payment via the active PaymentStrategy
class PaymentProcessor {
    public boolean process(PaymentStrategy strategy, String userId, double amount) {
        return strategy.authorizeAndCharge(userId, amount);
    }
}

// Subsystem 4: Generates customer receipt after successful payment
class ReceiptGenerator {
    public void generate(String userId, String itemId, double total) {
        System.out.printf("[ReceiptGenerator] Receipt issued to %s | Item: %s | Paid: $%.2f%n", userId, itemId, total);
    }
}

// Facade: Coordinates checkout and ensures inventory is released if payment fails
class CheckoutFacade {
    private final InventoryLockService inventory = new InventoryLockService();
    private final TaxEngine taxEngine = new TaxEngine();
    private final PaymentProcessor processor = new PaymentProcessor();
    private final ReceiptGenerator receiptGen = new ReceiptGenerator();
    private PaymentStrategy strategy;

    public CheckoutFacade(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    // Allows switching payment methods per checkout attempt
    public void setStrategy(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean checkout(String userId, String itemId, int qty, double unitPrice) {
        inventory.lockItem(itemId, qty);
        double subtotal = unitPrice * qty;
        double total = subtotal + taxEngine.calculateTax(subtotal);

        boolean paid = processor.process(strategy, userId, total);
        if (!paid) {
            System.out.println("[CheckoutFacade] Payment declined! Rolling back inventory lock...");
            inventory.releaseItem(itemId, qty);
            return false;
        }
        receiptGen.generate(userId, itemId, total);
        return true;
    }
}

// Client Driver
public class Task08Main {
    public static void main(String[] args) {
        System.out.println("=== TASK 08: Checkout & Payment Gateway Orchestrator ===");
        // Attempt 1: Declined card triggers clean inventory release
        CheckoutFacade checkout = new CheckoutFacade(new CreditCardPayment("4111-XXXX-0000"));
        checkout.checkout("user_01", "LAPTOP-PRO", 1, 1200.00);

        // Attempt 2: Switch strategy at runtime to Digital Wallet
        System.out.println("--- Retrying with Digital Wallet ---");
        checkout.setStrategy(new DigitalWalletPayment("ApplePay-882"));
        checkout.checkout("user_01", "LAPTOP-PRO", 1, 1200.00);
    }
}
