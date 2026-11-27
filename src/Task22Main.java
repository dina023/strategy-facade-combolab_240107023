// ============================================================================
// TASK 22: Emergency Triage & Hospital Bed Allocation System
// Combines DUAL Strategies (TriagePriorityStrategy + BedMatchingStrategy) + Facade
// ============================================================================

// Strategy Family 1: Ranks clinical urgency level (1 = Immediate Critical, 5 = Non-urgent)
interface TriagePriorityStrategy {
    int assessUrgency(int heartRate, int age, boolean traumaFlag);
}

class StandardSeverityIndex implements TriagePriorityStrategy {
    public int assessUrgency(int heartRate, int age, boolean traumaFlag) {
        return (heartRate > 130 || traumaFlag) ? 1 : 3;
    }
}

class PediatricFastTrack implements TriagePriorityStrategy {
    public int assessUrgency(int heartRate, int age, boolean traumaFlag) {
        return (age < 12) ? 1 : 2;
    }
}

class MassCasualtyProtocol implements TriagePriorityStrategy {
    public int assessUrgency(int heartRate, int age, boolean traumaFlag) {
        return traumaFlag ? 1 : 4; // Prioritizes salvageable acute trauma
    }
}

// Strategy Family 2: Selects optimal hospital ward and bed
interface BedMatchingStrategy {
    String matchBed(int urgencyLevel, String requiredSpecialty);
}

class ClosestUnitMatch implements BedMatchingStrategy {
    public String matchBed(int urgencyLevel, String requiredSpecialty) {
        return "ER-BAY-02 (Closest Available Unit)";
    }
}

class SpecialistWardMatch implements BedMatchingStrategy {
    public String matchBed(int urgencyLevel, String requiredSpecialty) {
        return requiredSpecialty + "-ICU-BED-05";
    }
}

// Subsystem 1: Captures incoming patient vital signs
class VitalsMonitor {
    public int readHeartRate(String patientName) {
        System.out.println("[VitalsMonitor] Recording vitals for " + patientName + " (HR: 138 bpm)");
        return 138;
    }
}

// Subsystem 2: Dispatches stat diagnostic lab orders
class LabOrderDispatcher {
    public void orderStatLabs(String patientName, int urgency) {
        System.out.printf("[LabOrderDispatcher] Dispatched Priority-%d blood/imaging panel for %s%n",
                urgency, patientName);
    }
}

// Subsystem 3: Reserves physical bed capacity in hospital wards
class WardCapacityTracker {
    public void reserveBed(String bedId, String patientName) {
        System.out.printf("[WardCapacityTracker] Reserved %s for %s%n", bedId, patientName);
    }
}

// Subsystem 4: Alerts attending physicians and nursing station
class AdmissionNotifier {
    public void notifyStaff(String patientName, String bedId, int urgency) {
        System.out.printf("[AdmissionNotifier] Paged trauma team: %s -> %s (Urgency Level %d)%n",
                patientName, bedId, urgency);
    }
}

// Facade: Coordinates dual triage/bed strategies with vitals, labs, capacity, and staff alerts
class EmergencyIntakeFacade {
    private final VitalsMonitor vitals = new VitalsMonitor();
    private final LabOrderDispatcher labDispatcher = new LabOrderDispatcher();
    private final WardCapacityTracker capacityTracker = new WardCapacityTracker();
    private final AdmissionNotifier notifier = new AdmissionNotifier();
    private TriagePriorityStrategy triageStrategy;
    private BedMatchingStrategy bedStrategy;

    public EmergencyIntakeFacade(TriagePriorityStrategy triageStrategy, BedMatchingStrategy bedStrategy) {
        this.triageStrategy = triageStrategy;
        this.bedStrategy = bedStrategy;
    }

    public void setStrategies(TriagePriorityStrategy triage, BedMatchingStrategy bed) {
        this.triageStrategy = triage;
        this.bedStrategy = bed;
    }

    public void admitPatient(String patientName, int age, boolean traumaFlag, String specialty) {
        int hr = vitals.readHeartRate(patientName);
        int urgency = triageStrategy.assessUrgency(hr, age, traumaFlag);
        String allocatedBed = bedStrategy.matchBed(urgency, specialty);

        capacityTracker.reserveBed(allocatedBed, patientName);
        labDispatcher.orderStatLabs(patientName, urgency);
        notifier.notifyStaff(patientName, allocatedBed, urgency);
    }
}

// Client Driver
public class Task22Main {
    public static void main(String[] args) {
        System.out.println("\n=== TASK 22: Emergency Triage & Hospital Bed Allocation ===");
        EmergencyIntakeFacade intake = new EmergencyIntakeFacade(
                new StandardSeverityIndex(), new SpecialistWardMatch());
        intake.admitPatient("Patient-A", 45, true, "CARDIOLOGY");

        intake.setStrategies(new PediatricFastTrack(), new ClosestUnitMatch());
        intake.admitPatient("Patient-B", 7, false, "PEDIATRICS");
    }
}
