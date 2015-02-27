package storm.util;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

public class TopRankingsTest{

	private static final int RANKING_SIZE = 5;
	private static TopRankings rankings; 
	
	private static final Rankable A = new Rankable("A", 1);
	private static final Rankable B = new Rankable("B", 2);
	private static final Rankable C = new Rankable("C", 3);
	private static final Rankable D = new Rankable("D", 4);
	private static final Rankable E = new Rankable("E", 5);
	private static final Rankable F = new Rankable("F", 6);
	private static final Rankable G = new Rankable("G", 7);
	private static final Rankable H = new Rankable("H", 8);
	private static final Rankable I = new Rankable("I", 9);
	private static final Rankable J = new Rankable("J", 10);
	
	@BeforeClass
	public static void setup() {
		rankings = new TopRankings(RANKING_SIZE);
	}
	
	@Test
	public void testGetSize() {
		assertEquals(rankings.getSize(), RANKING_SIZE);
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void testIllegalSize() {
		rankings = new TopRankings(0);
	}
	

	
	@Ignore
	@Test
	public void testInsert() {
		rankings.insert(A);
		List<Rankable> expected = Lists.newArrayList(A);

		
	}

}
