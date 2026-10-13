package lrucache;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class LRUCacheDemo {

    public static void main(String[] args) {
        LRUCache<String, String> cache = new LRUCache<>(3);
        System.out.println("Cache with capacity 3");

        section("1. Fill the cache");
        cache.put("A", "apple");
        cache.put("B", "banana");
        cache.put("C", "cherry");
        System.out.println("Inserted A, B, C (A is least recently used). Size = " + cache.size());

        section("2. Reading a key makes it most recently used");
        System.out.println("get(A) = " + cache.get("A") + "  -> B is now least recently used");

        section("3. Inserting into a full cache evicts the least recently used key");
        cache.put("D", "date");
        System.out.println("put(D) evicted B");
        System.out.println("get(B) = " + cache.get("B") + " (expected empty)");
        System.out.println("get(A) = " + cache.get("A") + " (expected apple)");

        section("4. Updating an existing key changes the value without evicting");
        cache.put("C", "coconut");
        System.out.println("get(C) = " + cache.get("C") + ", size = " + cache.size() + " (expected 3)");

        section("5. Remove");
        System.out.println("remove(A) = " + cache.remove("A") + " (expected true)");
        System.out.println("remove(A) = " + cache.remove("A") + " (expected false, already removed)");
        System.out.println("size = " + cache.size() + " (expected 2)");

        section("6. Invalid input is rejected");
        attempt("new LRUCache<>(0)", () -> new LRUCache<String, String>(0));
        attempt("put(null, ...)", () -> cache.put(null, "nothing"));
        attempt("get(null)", () -> cache.get(null));
        attempt("put(..., ttl = 0)", () -> cache.put("E", "elderberry", Duration.ZERO));

        ttlDemo();
    }

    // A manual clock lets the demo move time forward instead of sleeping
    private static void ttlDemo() {
        ManualClock clock = new ManualClock();

        section("7. An entry expires after its TTL");
        LRUCache<String, String> sessions = new LRUCache<>(2, clock);
        sessions.put("session-1", "user-42", Duration.ofSeconds(5));
        clock.advance(Duration.ofSeconds(4));
        System.out.println("After 4s: get(session-1) = " + sessions.get("session-1") + " (expected user-42)");
        clock.advance(Duration.ofSeconds(2));
        System.out.println("After 6s: get(session-1) = " + sessions.get("session-1") + " (expected empty)");

        section("8. Writing a key again resets its TTL");
        sessions.put("session-2", "user-7", Duration.ofSeconds(5));
        clock.advance(Duration.ofSeconds(4));
        sessions.put("session-2", "user-7", Duration.ofSeconds(5));
        clock.advance(Duration.ofSeconds(4));
        System.out.println("8s after first write, 4s after second: get(session-2) = "
                + sessions.get("session-2") + " (expected user-7)");

        section("9. A full cache reclaims expired entries before evicting live ones");
        LRUCache<String, String> cache = new LRUCache<>(2, clock);
        cache.put("short-lived", "expires in 1s", Duration.ofSeconds(1));
        cache.put("long-lived", "no TTL");
        cache.get("short-lived");
        System.out.println("short-lived was just read, so long-lived is now least recently used");
        clock.advance(Duration.ofSeconds(2));
        cache.put("new", "value");
        System.out.println("After 2s, put(new) on the full cache:");
        System.out.println("  get(long-lived) = " + cache.get("long-lived")
                + " (expected present: the expired entry was reclaimed instead of the LRU entry)");
        System.out.println("  get(new) = " + cache.get("new"));
        System.out.println("  size = " + cache.size() + " (expected 2)");
    }

    private static final class ManualClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T00:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

    private static void attempt(String label, Runnable action) {
        try {
            action.run();
            System.out.println(label + ": OK");
        } catch (RuntimeException e) {
            System.out.println(label + ": rejected with " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("== " + title);
    }
}
