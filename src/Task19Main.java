// ============================================================================
// TASK 19: Autonomous Drone Delivery Flight Controller
// Combines Strategy (flight path planning) + Facade (latch check & battery failsafe)
// ============================================================================
import java.util.List;

// Strategy Interface: Computes flight altitude and thrust profile
interface FlightPathStrategy {
    String planTrajectory(List<String> waypoints);
}

class EnergyOptimalFlight implements FlightPathStrategy {
    public String planTrajectory(List<String> waypoints) {
        return "Cruise 60m altitude, eco-thrust across " + waypoints;
    }
}

class WindCompensationFlight implements FlightPathStrategy {
    public String planTrajectory(List<String> waypoints) {
        return "Low-altitude 35m vectoring with active gust stabilization across " + waypoints;
    }
}

class HighObstacleClearanceFlight implements FlightPathStrategy {
    public String planTrajectory(List<String> waypoints) {
        return "High-altitude 120m urban clearance corridor across " + waypoints;
    }
}

// Subsystem 1: GPS & barometric altitude telemetry
class GpsBarometerUnit {
    public void lockCoordinates() {
        System.out.println("[GpsBarometerUnit] GPS & Barometer locked (14 satellites).");
    }
}

// Subsystem 2: Monitors LiPo battery voltage
class BatteryHealthMonitor {
    private final double voltage;
    public BatteryHealthMonitor(double voltage) { this.voltage = voltage; }
    public double getVoltage() {
        System.out.printf("[BatteryHealthMonitor] Battery voltage: %.1fV%n", voltage);
        return voltage;
    }
}

// Subsystem 3: Verifies cargo magnetic/mechanical latch engagement
class PayloadLatchController {
    public boolean isPayloadSecured() {
        System.out.println("[PayloadLatchController] Pre-flight cargo latch verified: SECURED");
        return true;
    }
}

// Subsystem 4: Controls brushless motor ESC thrust and emergency landing
class RotorEscDriver {
    public void executeTrajectory(String trajectory) {
        System.out.println("[RotorEscDriver] Executing flight plan: " + trajectory);
    }
    public void triggerEmergencyLanding() {
        System.out.println("[RotorEscDriver] CRITICAL BATTERY! Overriding route -> EMERGENCY LANDING INITIATED!");
    }
}

// Facade: Enforces pre-flight safety checks and mid-flight battery failsafe override
class FlightControlFacade {
    private final GpsBarometerUnit gpsBaro = new GpsBarometerUnit();
    private final PayloadLatchController latch = new PayloadLatchController();
    private final RotorEscDriver rotors = new RotorEscDriver();
    private BatteryHealthMonitor battery;
    private FlightPathStrategy strategy;

    public FlightControlFacade(FlightPathStrategy strategy, double initialVoltage) {
        this.strategy = strategy;
        this.battery = new BatteryHealthMonitor(initialVoltage);
    }

    public void setBatteryVoltage(double voltage) {
        this.battery = new BatteryHealthMonitor(voltage);
    }

    public void executeDeliveryLeg(List<String> waypoints) {
        if (!latch.isPayloadSecured()) {
            throw new IllegalStateException("Abort: Payload latch unsecured!");
        }
        gpsBaro.lockCoordinates();

        // Automatic failsafe override if voltage drops critically (< 14.2V)
        if (battery.getVoltage() < 14.2) {
            rotors.triggerEmergencyLanding();
            return;
        }

        String plan = strategy.planTrajectory(waypoints);
        rotors.executeTrajectory(plan);
    }
}

// Client Driver
public class Task19Main {
    public static void main(String[] args) {
        System.out.println("\n=== TASK 19: Autonomous Drone Delivery Flight Controller ===");
        FlightControlFacade drone = new FlightControlFacade(new HighObstacleClearanceFlight(), 16.4);
        drone.executeDeliveryLeg(List.of("HUB-A", "SECTOR-4", "DROPZONE-9"));

        System.out.println("--- Simulating Critical Battery Drop ---");
        drone.setBatteryVoltage(13.8);
        drone.executeDeliveryLeg(List.of("DROPZONE-9", "HUB-A"));
    }
}
