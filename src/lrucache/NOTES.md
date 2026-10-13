# LRU Cache
Implement a cache
## Clarifying questions & answers
- What kind of eviction strategy? = LRU
- Should we take updates/deletes - Yes
- Should it support expiration/TTLs? - Not right now (added later as a follow-up: per-entry TTL, expire-after-write)

## Requirements
1. Store item (k-v pair) in the cache
2. Retrieve item using k - makes item recently used
3. Invalidate - update/delete the item 
4. Eviction - Remove least recently used item from the cache if capacity exceeds

## Out of SCope

## Entities & relationships
LRUCache<K, V> - HashMap<K, Node> for O(1) lookup + doubly linked list (head/tail sentinels) for recency order
Node (private nested) - key, value, prev, next. Stores key so eviction can remove it from the map
ExpiryEntry (private record) - node + deadline snapshot, kept in a min-heap (PriorityQueue) ordered by deadline
Clock (injected) - lets tests move time forward without sleeping

## Class design

## APIs
- void put(K key, V value) - insert or update, never expires; marks most recently used; evicts LRU when full
- void put(K key, V value, Duration ttl) - same, but expires ttl after this write (a rewrite resets the TTL)
- Optional<V> get(K key) - empty on miss or if expired; marks most recently used on hit
- boolean remove(K key) - false if key was not present or already expired
- int size() - live entries only

Complexity: get O(1), remove O(1), put O(log n) amortised (heap push + purge), size O(log n) amortised. Space O(capacity).

## TTL design
- Lazy: get/remove treat an expired entry as absent and unlink it.
- Active: every put first pops expired entries off the min-heap, so a full cache reclaims expired slots
  before evicting a live LRU entry.
- Heap uses lazy deletion: updates/removals leave stale entries (detected by comparing the snapshot deadline
  and node identity), and they are skipped when popped. When the heap exceeds 2x capacity, stale entries are
  dropped in one pass, keeping it O(capacity).
- Not thread-safe. A background sweeper would require locking every public method.

## Design patterns used (and why)

## Trade-offs / what I'd do with more time
