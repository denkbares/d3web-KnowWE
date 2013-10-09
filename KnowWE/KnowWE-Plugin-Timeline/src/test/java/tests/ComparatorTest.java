package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import de.knowwe.timeline.tree.Comparators;


public class ComparatorTest {
	@Test
	public void testSGT() {
		Comparators comp = Comparators.SGT;
		assertTrue(comp.matches(5, 3));
		assertFalse(comp.matches(6, 6));
		assertFalse(comp.matches(2, 7));
	}
	
	@Test
	public void testSLT() {
		Comparators comp = Comparators.SLT;
		assertFalse(comp.matches(5, 3));
		assertFalse(comp.matches(6, 6));
		assertTrue(comp.matches(2, 7));
	}
	
	@Test
	public void testEQU() {
		Comparators comp = Comparators.EQU;
		assertFalse(comp.matches(5, 3));
		assertTrue(comp.matches(6, 6));
		assertFalse(comp.matches(2, 7));
		assertFalse(comp.matches("abc", "abcd"));
		assertTrue(comp.matches("abc", "abc"));
	}
	
	@Test
	public void testSGE() {
		Comparators comp = Comparators.SGE;
		assertTrue(comp.matches(5, 3));
		assertTrue(comp.matches(6, 6));
		assertFalse(comp.matches(2, 7));
	}
	
	@Test
	public void testSLE() {
		Comparators comp = Comparators.SLE;
		assertFalse(comp.matches(5, 3));
		assertTrue(comp.matches(6, 6));
		assertTrue(comp.matches(2, 7));
	}
	
	@Test
	public void testNEQ() {
		Comparators comp = Comparators.NEQ;
		assertTrue(comp.matches(5, 3));
		assertFalse(comp.matches(6, 6));
		assertTrue(comp.matches(2, 7));
		assertTrue(comp.matches("abc", "abcd"));
		assertFalse(comp.matches("abc", "abc"));
	}
}
