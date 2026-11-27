// ============================================================================
// TASK 15: Smart HVAC Climate Regulation Controller
// Combines Strategy (climate profiles) + Facade (sensors -> strategy -> actuators)
// ============================================================================

// Strategy Interface: Computes target compressor power and fan speed
interface ClimateStrategy {
    int[] computeActuatorStates(double currentTempC, double humidityPct); // [compressorPct, fanSpeedPct]
}

// Concrete Strategy 1: Wide deadband energy-saving mode when building is empty
class EcoAwayStrategy implements ClimateStrategy {
    public int[] computeActuatorStates(double currentTempC, double humidityPct) {
        int compressor = currentTempC > 26.0 ? 40 : 0;
        return new int[]{compressor, 25};
    }
}

// Concrete Strategy 2: Tight temperature and humidity regulation for occupied rooms
class ComfortHoldStrategy implements ClimateStrategy {
    public int[] computeActuatorStates(double currentTempC, double humidityPct) {
        int compressor = currentTempC > 22.0 ? 70 : 20;
        int fan = humidityPct > 60.0 ? 65 : 45;
        return new int[]{compressor, fan};
    }
}

// Concrete Strategy 3: Maximum cooling/heating output to reach target rapidly
class RapidPreconditionStrategy implements ClimateStrategy {
    public int[] computeActuatorStates(double currentTempC, double humidityPct) {
        return new int[]{100, 100};
    }
}

// Subsystem 1: Reads zone ambient temperature
class ThermostatSensor {
    public double readTemperatureC(String zoneId) {
        System.out.println("[ThermostatSensor] Zone " + zoneId + " temperature: 25.5°C");
        return 25.5;
    }
}

// Subsystem 2: Reads zone relative humidity
class HumidityMonitor {
    public double readHumidityPct(String zoneId) {
        System.out.println("[HumidityMonitor] Zone " + zoneId + " humidity: 64.0%");
        return 64.0;
    }
}

// Subsystem 3: Adjusts air duct damper flaps and fan airflow
class DamperActuator {
    public void setAirflow(String zoneId, int fanSpeedPct) {
        System.out.printf("[DamperActuator] Zone %s airflow fan set to %d%%%n", zoneId, fanSpeedPct);
    }
}

// Subsystem 4: Modulates HVAC compressor relay load
class CompressorRelay {
    public void setCompressorLoad(int compressorPct) {
        System.out.printf("[CompressorRelay] Compressor load set to %d%%%n", compressorPct);
    }
}

// Facade: Reads sensors, evaluates climate strategy, and drives hardware actuators
class HvacControlFacade {
    private final ThermostatSensor thermostat = new ThermostatSensor();
    private final HumidityMonitor humidityMonitor = new HumidityMonitor();
    private final DamperActuator damper = new DamperActuator();
    private final CompressorRelay compressor = new CompressorRelay();
    private ClimateStrategy strategy;

    public HvacControlFacade(ClimateStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(ClimateStrategy strategy) {
        this.strategy = strategy;
    }

    public void regulateZone(String zoneId) {
        double temp = thermostat.readTemperatureC(zoneId);
        double humidity = humidityMonitor.readHumidityPct(zoneId);
        int[] states = strategy.computeActuatorStates(temp, humidity);
        compressor.setCompressorLoad(states[0]);
        damper.setAirflow(zoneId, states[1]);
    }
}

// Client Driver
public class Task15Main {
    public static void main(String[] args) {
        System.out.println("\n=== TASK 15: Smart HVAC Climate Regulation Controller ===");
        HvacControlFacade hvac = new HvacControlFacade(new EcoAwayStrategy());
        hvac.regulateZone("ConferenceRoom-B");

        System.out.println("--- Switching to ComfortHoldStrategy ---");
        hvac.setStrategy(new ComfortHoldStrategy());
        hvac.regulateZone("ConferenceRoom-B");
    }
}
