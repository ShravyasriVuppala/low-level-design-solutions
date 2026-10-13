# LRU Cache
Implement a cache
## Clarifying questions & answers
- What kind of eviction strategy? = LRU
- Should we take updates/deletes - Yes
- Should it support expiration/TTLs? - Not right now

## Requirements
1. Store item (k-v pair) in the cache
2. Retrieve item using k - makes item recently used
3. Invalidate - update/delete the item 
4. Eviction - Remove least recently used item from the cache if capacity exceeds

## Out of SCope

## Entities & relationships
LRUCache<K, V> - HashMap<K, Node> for O(1) lookup + doubly linked list (head/tail sentinels) for recency order
Node (private nested) - key, value, prev, next. Stores key so eviction can remove it from the map

## Class design

## APIs
- void put(K key, V value) - insert or update; marks most recently used; evicts LRU when full
- Optional<V> get(K key) - empty on miss; marks most recently used on hit
- boolean remove(K key) - false if key was not present
- int size()

All operations O(1) time, O(capacity) space.

## Design patterns used (and why)

## Trade-offs / what I'd do with more time
