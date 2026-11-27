// ============================================================================
// TASK 07: User Notification Dispatch Service
// Combines Strategy (delivery channel) + Facade (lookup -> format -> send -> log)
// ============================================================================
import java.util.Map;

// Strategy Interface: Encapsulates message transport mechanisms
interface DeliveryChannelStrategy {
    boolean deliver(String contactEndpoint, String renderedMessage);
}

// Concrete Strategy 1: SMTP Email delivery
class EmailDelivery implements DeliveryChannelStrategy {
    public boolean deliver(String contactEndpoint, String renderedMessage) {
        System.out.printf("[EmailDelivery] Sending email to %s: %s%n", contactEndpoint, renderedMessage);
        return true;
    }
}

// Concrete Strategy 2: Mobile SMS gateway delivery
class SmsDelivery implements DeliveryChannelStrategy {
    public boolean deliver(String contactEndpoint, String renderedMessage) {
        System.out.printf("[SmsDelivery] Sending SMS to %s: %s%n", contactEndpoint, renderedMessage);
        return true;
    }
}

// Concrete Strategy 3: Mobile/Web Push Notification delivery
class PushNotificationDelivery implements DeliveryChannelStrategy {
    public boolean deliver(String contactEndpoint, String renderedMessage) {
        System.out.printf("[PushNotificationDelivery] Pushing to device %s: %s%n", contactEndpoint, renderedMessage);
        return true;
    }
}

// Subsystem 1: Retrieves user contact address/token
class UserPreferenceStore {
    public String getContactForChannel(String userId) {
        System.out.println("[UserPreferenceStore] Fetching contact endpoint for user " + userId);
        return userId + "@example.com";
    }
}

// Subsystem 2: Populates notification templates with dynamic payload data
class TemplateFormatter {
    public String format(String messageKey, Map<String, String> payload) {
        System.out.println("[TemplateFormatter] Rendering template '" + messageKey + "'");
        return "[" + messageKey + "] " + payload.toString();
    }
}

// Subsystem 3: Records delivery audit logs
class DeliveryLogger {
    public void logStatus(String userId, String messageKey, boolean success) {
        System.out.printf("[DeliveryLogger] User: %s | Key: %s | Delivered: %b%n", userId, messageKey, success);
    }
}

// Facade: Simplifies multi-step notification dispatch into a single call
class NotificationFacade {
    private final UserPreferenceStore prefStore = new UserPreferenceStore();
    private final TemplateFormatter formatter = new TemplateFormatter();
    private final DeliveryLogger logger = new DeliveryLogger();
    private DeliveryChannelStrategy strategy;

    public NotificationFacade(DeliveryChannelStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(DeliveryChannelStrategy strategy) {
        this.strategy = strategy;
    }

    public void sendAlert(String userId, String messageKey, Map<String, String> payload) {
        String contact = prefStore.getContactForChannel(userId);
        String message = formatter.format(messageKey, payload);
        boolean success = strategy.deliver(contact, message);
        logger.logStatus(userId, messageKey, success);
    }
}

// Client Driver
public class Task07Main {
    public static void main(String[] args) {
        System.out.println("\n=== TASK 07: User Notification Dispatch Service ===");
        NotificationFacade notifier = new NotificationFacade(new EmailDelivery());
        notifier.sendAlert("user_42", "LOGIN_ALERT", Map.of("ip", "192.168.1.10"));

        notifier.setStrategy(new PushNotificationDelivery());
        notifier.sendAlert("user_42", "ORDER_SHIPPED", Map.of("orderId", "9981"));
    }
}
