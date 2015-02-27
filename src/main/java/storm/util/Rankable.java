package storm.util;

public class Rankable implements Comparable<Rankable>{

	private Object obj;
	private Long count;
	
	public Rankable(Object obj, long count) {
		this.obj = obj;
		this.count = count;
	}
	
	@Override
	public int compareTo(Rankable o) {		
		return this.count.compareTo(o.getCount());
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((count == null) ? 0 : count.hashCode());
		result = prime * result + ((obj == null) ? 0 : obj.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (this == obj) {
			return true;
		}
		
		if (!(obj instanceof Rankable)) {
			return false;
		}
		
		Rankable other = (Rankable) obj;
		return this.obj.equals(other.obj) && this.count == other.count;
	}

	public Long getCount() {
		return this.count;
	}
	
	public Rankable copy() {
		return new Rankable(this.obj, this.count);
	}

}
