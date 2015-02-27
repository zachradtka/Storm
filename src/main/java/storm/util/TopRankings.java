package storm.util;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class TopRankings {

	private static final int DEFFAULT_SIZE = 10;
	
	private final List<Rankable> rankings = Lists.newArrayList();
	private final int maxSize;
	
	public TopRankings() {
		this(DEFFAULT_SIZE);
	}
	
	public TopRankings (int size) {
		
		if (size < 1) {
			throw new IllegalArgumentException("Size must be greater than or equal to one.");
		}
		this.maxSize = size;
	}
	
	
	public TopRankings(TopRankings rankings) {
		this(rankings.getSize());
		insert(rankings);
	}
	
	public void insert(TopRankings rankings) {
		for (Rankable r : rankings.getRankings()) {
			insert(r);
		}
	}
	
	public void insert(Rankable element) {
		
		// Decide if element is in map
		
		
		// insert element in map
		
		
		// If the size of the element is now larger, remove the end
		if (rankings.size() > this.maxSize) {
			rankings.remove(this.maxSize);
		}
	}
	
	public List<Rankable> getRankings() {
		
		List<Rankable> copy = Lists.newArrayList();
		
		for (Rankable r : copy) {
			copy.add(r.copy());
		}
				
		return ImmutableList.copyOf(copy);
	}
	
	public int getSize() {
		return this.maxSize;
	}
	
}
