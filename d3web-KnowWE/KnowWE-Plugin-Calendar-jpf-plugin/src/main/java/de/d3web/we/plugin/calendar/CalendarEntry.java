/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.plugin.calendar;

public class CalendarEntry implements Comparable {

	private DateType date;
	private String author = "<i>-NV-</i>";
	private String value;

	public CalendarEntry() {
		date = new DateType();
		value = "<i>-NV-</i>";
	}

	public CalendarEntry(String[] s) {

		date = new DateType(s[0], s[1]);
		author = s[2];
		value = s[3];

	}

	public DateType getDate() {
		return date;
	}

	public void setDate(DateType date) {
		this.date = date;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean after(CalendarEntry c) {
		if (date.equals(c.date)) {
			return (author.compareTo(c.author) > 0);
		}
		return date.after(c.date);
	}

	public boolean before(CalendarEntry c) {
		if (date.equals(c.date)) {
			return (author.compareTo(c.author) < 0);
		}
		return date.before(c.date);
	}

	public boolean equals(CalendarEntry c) {
		return date.equals(c.date) && author.equals(c.author)
				&& value.equals(c.value);
	}

	public String toString() {

		return "[" + date.toString() + " (" + author + "): " + value + "]";
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof CalendarEntry) {
			if (this.equals(o)) {
				return 0;
			}
			if (this.before((CalendarEntry) o)) {
				return -1;
			}
			else {
				return 1;
			}
		}
		else {
			return 0;
		}
	}
}
