package lrucache;

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
