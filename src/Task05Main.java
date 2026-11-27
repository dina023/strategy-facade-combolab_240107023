// ============================================================================
// TASK 05: Automated Coffee Brewing Station
// Combines Strategy (brewing parameters) + Facade (grind -> boil -> extract)
// ============================================================================

// Strategy Interface: Defines grind size, temperature, and extraction duration
interface BrewingStrategy {
    String getGrindSize();
    int getWaterTempC();
    int getExtractionSeconds();
}

// Concrete Strategy 1: High-pressure short espresso pull
class EspressoPullStrategy implements BrewingStrategy {
    public String getGrindSize() { return "Fine"; }
    public int getWaterTempC() { return 93; }
    public int getExtractionSeconds() { return 28; }
}

// Concrete Strategy 2: Controlled pour-over extraction
class PourOverStrategy implements BrewingStrategy {
    public String getGrindSize() { return "Medium-Fine"; }
    public int getWaterTempC() { return 96; }
    public int getExtractionSeconds() { return 180; }
}

// Concrete Strategy 3: Coarse immersion French press brew
class FrenchPressStrategy implements BrewingStrategy {
    public String getGrindSize() { return "Coarse"; }
    public int getWaterTempC() { return 95; }
    public int getExtractionSeconds() { return 240; }
}

// Subsystem 1: Grinds coffee beans to target coarseness
class BeanGrinder {
    public void grind(String beanType, String grindSize) {
        System.out.printf("[BeanGrinder] Grinding %s beans (%s)%n", beanType, grindSize);
    }
}

// Subsystem 2: Heats water to target temperature
class WaterBoiler {
    public void heatWater(int tempC) {
        System.out.printf("[WaterBoiler] Heating water to %d°C%n", tempC);
    }
}

// Subsystem 3: Performs timed coffee extraction
class BrewChamber {
    public void extract(int seconds) {
        System.out.printf("[BrewChamber] Extracting coffee for %d seconds... Ready!%n", seconds);
    }
}

// Facade: Executes the complete beverage workflow in one call
class BaristaFacade {
    private final BeanGrinder grinder = new BeanGrinder();
    private final WaterBoiler boiler = new WaterBoiler();
    private final BrewChamber chamber = new BrewChamber();
    private BrewingStrategy strategy;

    public BaristaFacade(BrewingStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(BrewingStrategy strategy) {
        this.strategy = strategy;
    }

    public void prepareBeverage(String beanType) {
        grinder.grind(beanType, strategy.getGrindSize());
        boiler.heatWater(strategy.getWaterTempC());
        chamber.extract(strategy.getExtractionSeconds());
    }
}

// Client Driver
public class Task05Main {
    public static void main(String[] args) {
        System.out.println("\n=== TASK 05: Automated Coffee Brewing Station ===");
        BaristaFacade barista = new BaristaFacade(new EspressoPullStrategy());
        barista.prepareBeverage("Ethiopian Yirgacheffe");

        barista.setStrategy(new FrenchPressStrategy());
        barista.prepareBeverage("Colombian Supremo");
    }
}
