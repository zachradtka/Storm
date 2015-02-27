package storm.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SlidingWindow<T> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3590928778023592299L;

	private Map<T, long[]> elementMap = new HashMap<T, long[]>();
	
	/** The number or slots in the window */
	private final int numSlots;
	
	/** The position of the first slot in the window */
	private int head;
	
	/** The position of the last slot in the window */
	private int tail;
	
	public SlidingWindow(int numSlots) {
		this.numSlots = numSlots;
		this.head = 0;
		this.tail = slotAfter(this.head);
	}
	
	
	public Map<T, Long> getCountAndAdvanceWindow() {
		
		Map<T, Long> counts = getTotals();
		clearSlot(this.tail);
		advanceWindow();
		
		return counts;
	}

	public void incrementCount(T element) {
		// Get the count at the head position
		long[] counts = elementMap.get(element);
		
		if (counts == null) {
			counts = new long[this.numSlots];
			elementMap.put(element, counts);
		}
		
		counts[this.head]++;
	}
	
	public void clearSlot(int slot) {
		for(long[] counts : elementMap.values()) {
			counts[slot] = 0;
		}
	}
	
	
	public Map<T, Long> getTotals() {
		
		Map<T, Long> counts = new HashMap<T, Long>();
		Set<T> elementToRemove = new HashSet<T>();

		// Get the sum for each element
		// If the sum is 0, set the element to be removed
		for (T element : elementMap.keySet()) {
			
			long sum = getSum(element);
			
			if (sum == 0) {
				elementToRemove.add(element);
			} else {
				counts.put(element, sum);
			}
		}
		
		// Remove elements that have 0 occurrences
		for (T element : elementToRemove) {
			elementMap.remove(element);
		}
		
		return counts;
	}
	
	/**
	 * Get the sum for a specific element
	 * 
	 * @param element
	 * @return
	 */
	private long getSum(T element) {
		
		long sum = 0;
		long[] counts = elementMap.get(element);
		
		for (long count : counts) {
			sum += count;
		}
		
		return sum;
	}
	
	
	private void advanceWindow() {
		this.head = tail;
		this.tail = slotAfter(this.head);
	}
	
	private int slotAfter(int slot) {
		return (slot + 1) % this.numSlots;
	}
	
	
	
}
