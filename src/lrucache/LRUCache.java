package lrucache;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class LRUCache<K, V> {

    private static final class Node<K, V> {
        private final K key;
        private V value;
        private Node<K, V> prev, next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int capacity;
    private final Map<K, Node<K, V>> index;
    // Sentinels: head.next is most recently used, tail.prev is least recently used
    private final Node<K, V> head, tail;

    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero");
        }
        this.capacity = capacity;
        this.index = new HashMap<>();
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
        moveToFront(node);
        return Optional.of(node.value);
    }

    public void put(K key, V value) {
        Objects.requireNonNull(key, "Key cannot be null");
        Node<K, V> existing = index.get(key);
        if (existing != null) {
            existing.value = value;
            moveToFront(existing);
            return;
        }
        if (index.size() == capacity) {
            evictLeastRecentlyUsed();
        }
        Node<K, V> node = new Node<>(key, value);
        addToFront(node);
        index.put(key, node);
    }

    public boolean remove(K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        Node<K, V> node = index.remove(key);
        if (node == null) {
            return false;
        }
        unlink(node);
        return true;
    }

    public int size() {
        return index.size();
    }

    private void evictLeastRecentlyUsed() {
        Node<K, V> lru = tail.prev;
        unlink(lru);
        index.remove(lru.key);
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
