// ============================================================================
// TASK 01: Smart Home Lighting Controller
// Combines Strategy (lighting algorithms) + Facade (hardware coordination)
// ============================================================================

// Strategy Interface: Defines a common contract for calculating light settings
interface LightingStrategy {
    int[] calculateSettings(int ambientLux, double gridLoad); // returns [brightness %, colorTemp K]
}

// Concrete Strategy 1: Energy-saving dim lighting based on natural room light
class EcoDimStrategy implements LightingStrategy {
    public int[] calculateSettings(int ambientLux, double gridLoad) {
        int brightness = Math.max(20, 60 - (ambientLux / 10));
        return new int[]{brightness, 2700}; // Warm low light
    }
}

// Concrete Strategy 2: Bright, cool-white light optimized for reading
class ReadingFocusStrategy implements LightingStrategy {
    public int[] calculateSettings(int ambientLux, double gridLoad) {
        return new int[]{95, 5000};
    }
}

// Concrete Strategy 3: Balanced warm ambiance for social gatherings
class AmbientPartyStrategy implements LightingStrategy {
    public int[] calculateSettings(int ambientLux, double gridLoad) {
        return new int[]{75, 3200};
    }
}

// Subsystem 1: Monitors electrical grid load
class PowerGridMonitor {
    public double getCurrentLoad() {
        System.out.println("[PowerGridMonitor] Checking current grid load...");
        return 0.45;
    }
}

// Subsystem 2: Measures ambient light inside a specific room
class RoomSensor {
    public int getAmbientLight(String roomId) {
        System.out.println("[RoomSensor] Reading ambient light for " + roomId);
        return 150; // lux
    }
}

// Subsystem 3: Controls physical dimmer switches and bulb color temperature
class DimmerHardware {
    public void setLightOutput(String roomId, int brightness, int colorTemp) {
        System.out.printf("[DimmerHardware] %s set to %d%% brightness at %dK%n", roomId, brightness, colorTemp);
    }
}

// Facade: Hides subsystem complexity and delegates calculation to active Strategy
class HomeLightingFacade {
    private final PowerGridMonitor gridMonitor = new PowerGridMonitor();
    private final RoomSensor roomSensor = new RoomSensor();
    private final DimmerHardware dimmer = new DimmerHardware();
    private LightingStrategy strategy;

    public HomeLightingFacade(LightingStrategy initialStrategy) {
        this.strategy = initialStrategy;
    }

    // Allows client to switch algorithms dynamically at runtime
    public void setStrategy(LightingStrategy strategy) {
        this.strategy = strategy;
    }

    // Single high-level method exposed to the client
    public void applyScene(String roomId) {
        double load = gridMonitor.getCurrentLoad();
        int lux = roomSensor.getAmbientLight(roomId);
        int[] settings = strategy.calculateSettings(lux, load);
        dimmer.setLightOutput(roomId, settings[0], settings[1]);
    }
}

// Client Driver: Interacts strictly with the Facade
public class Task01Main {
    public static void main(String[] args) {
        System.out.println("=== TASK 01: Smart Home Lighting Controller ===");
        HomeLightingFacade lighting = new HomeLightingFacade(new EcoDimStrategy());
        lighting.applyScene("LivingRoom");

        // Switch strategy at runtime without touching subsystems
        lighting.setStrategy(new ReadingFocusStrategy());
        lighting.applyScene("LivingRoom");
    }
}
