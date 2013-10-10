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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Date;

import org.junit.Before;
import org.junit.Test;

import de.knowwe.timeline.Timespan;

public class TimespanTest {
	Date date1;
	Date date2;
	Date date3;
	private Timespan a;
	private Timespan b;

	@Before
	public void setUp() {
		date1 = new Date(0);
		date2 = new Date(2);
		date3 = new Date(5);
		a = new Timespan(date1);
		b = new Timespan(date2, date3);
	}

	@Test
	public void testGetters() {
		assertEquals(date1, a.getStart());
		assertEquals(date1, a.getEnd());
		assertEquals(date2, b.getStart());
		assertEquals(date3, b.getEnd());
	}

	@Test
	public void testToString() {
		assertEquals("[0;0]", a.toString());
		assertEquals("[2;5]", b.toString());
	}

	@Test
	public void testLength() {
		assertEquals(0, a.length());
		assertEquals(3, b.length());
	}

	@Test
	public void testEquals() {
		assertTrue(a.equals(a));
		assertTrue(a.equals(crTS(0, 0)));
		assertFalse(a.equals(b));
		assertFalse(a.equals(null));
		assertFalse(a.equals(crTS(0, 2)));
	}

	@Test
	public void testImmutability() {
		Date d1 = new Date(0);
		Date d2 = new Date(1);
		Timespan c = new Timespan(d1, d2);
		d1.setTime(5);
		d2.setTime(6);
		assertEquals(crTS(0, 1), c);
	}

	@Test
	public void testContains() {
		assertTrue("singular span contains", a.contains(date1));
		assertFalse("singular span not contains", a.contains(date2));
		assertTrue("not singular span contains", b.contains(date3));
	}

	@Test
	public void testUnion() {
		assertEquals(a, a.union(a));
		assertEquals(b, b.union(crTS(3, 4)));
		assertEquals(b, crTS(3, 4).union(b));
		assertEquals(crTS(0, 5), crTS(0, 2).union(crTS(2, 5)));
		assertEquals(crTS(0, 5), crTS(2, 5).union(crTS(0, 2)));
		assertEquals(crTS(0, 5), crTS(2, 5).union(crTS(0, 4)));
		try {
			a.union(b).toString();
			fail("Should have raised an IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void testIntersect() {
		assertNull(a.intersect(b));
		assertNull(b.intersect(a));
		assertEquals(b, b.intersect(b));
		Timespan e = new Timespan(date1, date3);
		assertEquals(b, e.intersect(b));
		assertEquals(b, b.intersect(e));
		assertEquals(a, e.intersect(a));
		assertEquals(a, a.intersect(e));
	}

	private Timespan crTS(long start, long end) {
		return new Timespan(new Date(start), new Date(end));
	}
}
