// ============================================================================
// TASK 14: Workout Telemetry & Calorie Estimation Assistant
// Combines Strategy (calorie formulas) + Facade (HR, timer, biometrics, repo)
// ============================================================================

// Strategy Interface: Computes burned kilocalories based on workout modality
interface CalorieCalculationStrategy {
    double estimateCalories(double weightKg, int durationMinutes, double avgHeartRate);
}

// Concrete Strategy 1: Steady-state cardiovascular MET calculation
class CardioMetStrategy implements CalorieCalculationStrategy {
    public double estimateCalories(double weightKg, int durationMinutes, double avgHeartRate) {
        double met = 8.5; // Running/cycling MET
        return (met * 3.5 * weightKg / 200.0) * durationMinutes;
    }
}

// Concrete Strategy 2: Resistance / weightlifting load calculation
class StrengthLoadStrategy implements CalorieCalculationStrategy {
    public double estimateCalories(double weightKg, int durationMinutes, double avgHeartRate) {
        double met = 5.0;
        return (met * 3.5 * weightKg / 200.0) * durationMinutes;
    }
}

// Concrete Strategy 3: High-Intensity Interval Training (HIIT) with EPOC boost
class HiitIntervalStrategy implements CalorieCalculationStrategy {
    public double estimateCalories(double weightKg, int durationMinutes, double avgHeartRate) {
        double met = 11.5;
        return ((met * 3.5 * weightKg / 200.0) * durationMinutes) * 1.12; // +12% afterburn
    }
}

// Subsystem 1: Processes raw heart-rate telemetry samples
class HeartRateSensor {
    public double computeAverageBpm(int[] samples) {
        int sum = 0;
        for (int bpm : samples) sum += bpm;
        double avg = (double) sum / samples.length;
        System.out.printf("[HeartRateSensor] Average HR: %.1f bpm%n", avg);
        return avg;
    }
}

// Subsystem 2: Tracks total active workout duration
class SessionTimer {
    public int getElapsedMinutes(int sampleCount) {
        int mins = sampleCount * 5; // Each sample represents a 5-min block
        System.out.println("[SessionTimer] Total session duration: " + mins + " mins");
        return mins;
    }
}

// Subsystem 3: Fetches user body weight and biometric profile
class UserBiometricsStore {
    public double getUserWeightKg(String userId) {
        System.out.println("[UserBiometricsStore] Loaded weight for " + userId + ": 72.0 kg");
        return 72.0;
    }
}

// Subsystem 4: Persists completed workout analytics
class ActivityLogRepository {
    public void saveSession(String userId, double calories, int durationMins) {
        System.out.printf("[ActivityLogRepository] Saved session for %s: %.1f kcal in %d mins%n",
                userId, calories, durationMins);
    }
}

// Facade: Coordinates telemetry aggregation, calorie estimation, and storage
class WorkoutSessionFacade {
    private final HeartRateSensor hrSensor = new HeartRateSensor();
    private final SessionTimer timer = new SessionTimer();
    private final UserBiometricsStore biometrics = new UserBiometricsStore();
    private final ActivityLogRepository repository = new ActivityLogRepository();
    private CalorieCalculationStrategy strategy;

    public WorkoutSessionFacade(CalorieCalculationStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(CalorieCalculationStrategy strategy) {
        this.strategy = strategy;
    }

    public void completeWorkout(String userId, int[] telemetrySamples) {
        double avgHr = hrSensor.computeAverageBpm(telemetrySamples);
        int duration = timer.getElapsedMinutes(telemetrySamples.length);
        double weight = biometrics.getUserWeightKg(userId);
        double kcal = strategy.estimateCalories(weight, duration, avgHr);
        repository.saveSession(userId, kcal, duration);
    }
}

// Client Driver
public class Task14Main {
    public static void main(String[] args) {
        System.out.println("\n=== TASK 14: Workout Telemetry & Calorie Estimation Assistant ===");
        int[] samples = {135, 148, 155, 162, 150, 140};

        WorkoutSessionFacade tracker = new WorkoutSessionFacade(new CardioMetStrategy());
        tracker.completeWorkout("athlete_09", samples);

        tracker.setStrategy(new HiitIntervalStrategy());
        tracker.completeWorkout("athlete_09", samples);
    }
}
