package amazonlocker;

public class AmazonLockerDemo {

    public static void main(String[] args) {
        Locker locker = new Locker(new Compartment[]{
                new Compartment(CompartmentSize.SMALL),
                new Compartment(CompartmentSize.SMALL),
                new Compartment(CompartmentSize.MEDIUM),
                new Compartment(CompartmentSize.LARGE)
        });
        System.out.println("Locker " + locker.getLockerId() + " with 2 SMALL, 1 MEDIUM, 1 LARGE compartments");

        section("1. Delivery associate deposits packages");
        String firstSmallCode = locker.depositPackage(CompartmentSize.SMALL);
        String secondSmallCode = locker.depositPackage(CompartmentSize.SMALL);
        String mediumCode = locker.depositPackage(CompartmentSize.MEDIUM);
        System.out.println("SMALL package code:  " + firstSmallCode);
        System.out.println("SMALL package code:  " + secondSmallCode);
        System.out.println("MEDIUM package code: " + mediumCode);

        section("2. No SMALL compartment left");
        attempt(() -> locker.depositPackage(CompartmentSize.SMALL));

        section("3. Customer retrieves a package with a valid code");
        attempt(() -> locker.retrievePackage(firstSmallCode));

        section("4. The same code cannot be used twice");
        attempt(() -> locker.retrievePackage(firstSmallCode));

        section("5. Freed compartment accepts a new package");
        attempt(() -> locker.depositPackage(CompartmentSize.SMALL));

        section("6. Invalid input is rejected");
        attempt(() -> locker.retrievePackage("not-a-real-code"));
        attempt(() -> locker.retrievePackage("   "));
        attempt(() -> locker.retrievePackage(null));

        section("7. Manual expiry cleanup");
        // Packages expire 7 days after deposit, so nothing is expired yet and every code still works.
        // Injecting a Clock into Locker would let this flow be exercised without waiting.
        locker.cleanupExpiredPackages();
        attempt(() -> locker.retrievePackage(mediumCode));
    }

    private static void attempt(Runnable action) {
        try {
            action.run();
            System.out.println("OK");
        } catch (RuntimeException e) {
            System.out.println("Rejected: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("== " + title);
    }
}
