package lrucache;
/*
    Requirements
    1. Store item (k-v) in the cache
    2. retrieve item using k which make it recently use
    3. update/delete item from the cache
    4. Eviction - remove least recently used when capacity exceeds

 */

import java.util.HashMap;
import java.util.Map;

public class LRUCache{

    private final int capacity;
    private final Node head, tail;
    private final Map<Integer, Node> index;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.index = new HashMap<>();
        head = new Node(0, 0);
        tail = new Node(0, 0);
        head.next = tail;
        tail.prev = head;
    }

    public void put(int key, int value){
        //if key already exists, update value and move to front
        Node node = index.get(key);
        if(node != null){
            node.value = value;
            moveToFront(node);
            return;
        }
        // create item and add to front
        node = new Node(key, value);
        index.put(key, node);
        addToFront(node);
        if(index.size() > capacity){
            evict();
        }
    }

    public int get(int key){
        //throw error or return -1 if invalid key
        Node node = index.get(key);
        if(node == null)
            return -1;

        moveToFront(node);
        return node.value;
    }

    public void delete(int key){
        //get the node
        Node node = index.remove(key);
        if(node != null){
            unlink(node);
        }
    }

    private void addToFront(Node node){
        node.next = head.next;
        head.next.prev = node;
        node.prev = head;
        head.next = node;
    }
    private void unlink(Node node){
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
    private void moveToFront(Node node){
        unlink(node);
        addToFront(node);
    }
    private void evict(){
        Node lru = tail.prev;
        unlink(lru);
        index.remove(lru.key);
    }

    private static class Node{
        int key;
        int value;
        Node prev, next;

        Node(int key, int value){
            this.key = key;
            this.value = value;
            prev = null;
            next = null;
        }

    }
}