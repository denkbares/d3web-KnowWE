/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Date;

import org.junit.Before;
import org.junit.Test;

import de.knowwe.timeline.Timeset;
import de.knowwe.timeline.Timespan;
import de.knowwe.timeline.tree.Comparators;

public class TimesetTest {
	Timeset t;

	@Before
	public void setUp() {
		t = new Timeset();
	}

	@Test
	public void testAdd() {
		t.add(crTS(2, 5));
		assertEquals("[2;5]", t.toString());
		t.add(crTS(0, 1));
		assertEquals("[0;1]U[2;5]", t.toString());
		t.add(crTS(1, 2));
		assertEquals("[0;5]", t.toString());
	}

	@Test
	public void testIterator() {
		Timespan t1 = crTS(2, 5);
		Timespan t2 = crTS(0, 1);
		t.add(t1).add(t2);
		boolean first = true;
		for (Timespan ts : t) {
			if (first) {
				assertEquals(t2, ts);
				first = false;
			} else {
				assertEquals(t1, ts);
			}
		}
	}

	@Test
	public void testContains() {
		t.add(crTS(3, 5)).add(crTS(0, 1));
		assertTrue(t.contains(new Date(1)));
		assertFalse(t.contains(new Date(2)));
		assertTrue(t.contains(new Date(3)));
		t.add(crTS(1, 3));
		assertTrue(t.contains(new Date(2)));
	}

	@Test
	public void testUnion() {
		t.add(crTS(0, 1)).union(t);
		assertEquals("[0;1]", t.toString());
		t.add(crTS(2, 3));
		t.add(crTS(4, 5));
		Timeset t2 = new Timeset();
		t2.add(crTS(1, 2));
		t2.add(crTS(3, 4));
		t.union(t2);
		assertEquals("[0;5]", t.toString());
	}

	@Test
	public void testIntersect() {
		t.add(crTS(0, 5));
		t.intersect(t);
		assertEquals("[0;5]", t.toString());
		Timeset t2 = new Timeset();
		t2.add(crTS(2, 3));
		assertEquals("[2;3]", t.intersect(t2).toString());
		Timeset t3 = new Timeset();
		assertEquals("", t.intersect(t3).toString());
		t3.add(crTS(0,5));
		t.add(crTS(7,9));
		assertEquals("", t.intersect(t3).toString());
	}

	@Test
	public void testRemoveShort() {
		t.add(crTS(0, 5));
		t.add(crTS(6, 10));
		t.add(crTS(11, 20));
		assertEquals("[0;5]U[11;20]", t.removeDurationNotMatching(5, Comparators.SGE).toString());
	}
	
	@Test
	public void testCount() {
		assertEquals(0, t.getCount());
		t.add(crTS(0,5));
		assertEquals(1,t.getCount());
		t.add(crTS(6,10));
		assertEquals(2,t.getCount());
		t.add(crTS(5,6));
		assertEquals(1,t.getCount());
	}

	private Timespan crTS(long start, long end) {
		return new Timespan(new Date(start), new Date(end));
	}
}
