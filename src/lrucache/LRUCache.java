package lrucache;

import java.time.Clock;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;

public class LRUCache<K, V> {

    private static final long NO_EXPIRY = Long.MAX_VALUE;

    private static final class Node<K, V> {
        private final K key;
        private V value;
        private long expiresAtMillis;
        private Node<K, V> prev, next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.expiresAtMillis = NO_EXPIRY;
        }
    }

    // Snapshot of a node's deadline when it was queued. It goes stale once the node is
    // removed or its TTL is reset; stale entries are skipped instead of deleted from the heap.
    private record ExpiryEntry<K, V>(Node<K, V> node, long expiresAtMillis) { }

    private final int capacity;
    private final Map<K, Node<K, V>> index;
    // Min-heap by deadline, so expired entries can be reclaimed without scanning the whole cache
    private final PriorityQueue<ExpiryEntry<K, V>> expiryQueue;
    private final Clock clock;
    // Sentinels: head.next is most recently used, tail.prev is least recently used
    private final Node<K, V> head, tail;

    public LRUCache(int capacity) {
        this(capacity, Clock.systemUTC());
    }

    public LRUCache(int capacity, Clock clock) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero");
        }
        this.capacity = capacity;
        this.clock = Objects.requireNonNull(clock, "Clock cannot be null");
        this.index = new HashMap<>();
        this.expiryQueue = new PriorityQueue<>(
                Comparator.comparingLong((ExpiryEntry<K, V> entry) -> entry.expiresAtMillis())
        );
        head = new Node<>(null, null);
        tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }

    public Optional<V> get(K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        Node<K, V> node = index.get(key);
        if (node == null) {
            return Optional.empty();
        }
        if (isExpired(node, clock.millis())) {
            removeNode(node);
            return Optional.empty();
        }
        moveToFront(node);
        return Optional.of(node.value);
    }

    // Entry never expires; it leaves the cache only through LRU eviction or remove
    public void put(K key, V value) {
        putWithDeadline(key, value, NO_EXPIRY);
    }

    public void put(K key, V value, Duration ttl) {
        Objects.requireNonNull(ttl, "TTL cannot be null");
        if (ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be positive");
        }
        putWithDeadline(key, value, deadlineAfter(ttl));
    }

    public boolean remove(K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        Node<K, V> node = index.get(key);
        if (node == null) {
            return false;
        }
        boolean live = !isExpired(node, clock.millis());
        removeNode(node);
        return live;
    }

    // Number of live entries; expired entries are purged first so they are not counted
    public int size() {
        purgeExpired(clock.millis());
        return index.size();
    }

    private void putWithDeadline(K key, V value, long expiresAtMillis) {
        Objects.requireNonNull(key, "Key cannot be null");
        // Reclaim expired slots first, so a full cache drops dead entries before live LRU ones
        purgeExpired(clock.millis());

        Node<K, V> existing = index.get(key);
        if (existing != null) {
            existing.value = value;
            setExpiry(existing, expiresAtMillis);
            moveToFront(existing);
            return;
        }
        if (index.size() == capacity) {
            evictLeastRecentlyUsed();
        }
        Node<K, V> node = new Node<>(key, value);
        addToFront(node);
        index.put(key, node);
        setExpiry(node, expiresAtMillis);
    }

    private void setExpiry(Node<K, V> node, long expiresAtMillis) {
        node.expiresAtMillis = expiresAtMillis;
        if (expiresAtMillis != NO_EXPIRY) {
            expiryQueue.add(new ExpiryEntry<>(node, expiresAtMillis));
            compactExpiryQueueIfNeeded();
        }
    }

    private void purgeExpired(long nowMillis) {
        while (!expiryQueue.isEmpty() && expiryQueue.peek().expiresAtMillis() <= nowMillis) {
            ExpiryEntry<K, V> entry = expiryQueue.poll();
            if (isCurrent(entry)) {
                removeNode(entry.node());
            }
        }
    }

    // Updates and removals leave stale heap entries behind; drop them once they outnumber
    // the live ones so the heap stays O(capacity). Amortised O(1) per put.
    private void compactExpiryQueueIfNeeded() {
        if (expiryQueue.size() > 2 * capacity) {
            expiryQueue.removeIf(entry -> !isCurrent(entry));
        }
    }

    private boolean isCurrent(ExpiryEntry<K, V> entry) {
        Node<K, V> node = entry.node();
        return index.get(node.key) == node && node.expiresAtMillis == entry.expiresAtMillis();
    }

    private boolean isExpired(Node<K, V> node, long nowMillis) {
        return nowMillis >= node.expiresAtMillis;
    }

    private long deadlineAfter(Duration ttl) {
        try {
            return Math.addExact(clock.millis(), ttl.toMillis());
        } catch (ArithmeticException e) {
            // TTL too large to represent; treat as never expiring
            return NO_EXPIRY;
        }
    }

    private void evictLeastRecentlyUsed() {
        removeNode(tail.prev);
    }

    private void removeNode(Node<K, V> node) {
        unlink(node);
        index.remove(node.key);
    }

    private void moveToFront(Node<K, V> node) {
        unlink(node);
        addToFront(node);
    }

    private void addToFront(Node<K, V> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void unlink(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
}
