package storm.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RankableTest {

	private static final Rankable A = new Rankable("A", 1);
	private static final Rankable B = new Rankable("B", 2);
	private static final Rankable C = new Rankable("C", 3);
	
	@Test
	public void testCompareToEquals() {
		assertEquals(0, B.compareTo(B) );
	}
	
	@Test
	public void testCompareToLessThan() {
		assertTrue(B.compareTo(C) < 0);
	}

	@Test
	public void testCompareToMoreThan() {
		assertTrue(B.compareTo(A) > 0);
	}

	@Test
	public void testGetCount() {
		assertEquals(Long.valueOf(2), B.getCount());
	}
	
	@Test
	public void testCopy() {
		Rankable copy = B.copy();
		
		// Can only test if count was copied
		assertEquals(B.getCount(), copy.getCount());
	}
	
	@Test
	public void testEqauls() {
		Rankable expected = new Rankable("B", 2);
		
		// Can only test if count was copied
		assertTrue(B.equals(expected));
	}
	
	@Test
	public void testEqaulsFalse() {
		Rankable expected = new Rankable("B", 3);
		
		// Can only test if count was copied
		assertFalse(B.equals(expected));
	}
	
	@Test
	public void testEqaulsFalse1() {
		Rankable expected = new Rankable("A", 2);
		
		// Can only test if count was copied
		assertFalse(B.equals(expected));
	}
	
	@Test
	public void testEqaulsFalse2() {
		Rankable expected = new Rankable("A", 4);
		
		// Can only test if count was copied
		assertFalse(B.equals(expected));
	}
	
}
