/*
 * Copyright (C) 2023 denkbares GmbH, Germany
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

package de.knowwe.jspwiki;

import java.util.Comparator;
import java.util.List;

import org.apache.wiki.api.core.Page;

import com.denkbares.utils.Pair;

@SuppressWarnings("rawtypes")
public class PageComparator implements Comparator<Page> {
	private static final RecentChangesUtils util = new RecentChangesUtils();

	private final List<Pair<String, Comparator>> columnComparators;
	public PageComparator(List<Pair<String, Comparator>> columnComparators){
		this.columnComparators = columnComparators;
	}
	@Override
	public int compare(Page p1, Page p2) {
		for (Pair<String, Comparator> columnComparator : columnComparators) {
			String columnName = columnComparator.getA();
			Object o1 = util.getColumnObjectValueByName(columnName, p1);
			Object o2 = util.getColumnObjectValueByName(columnName, p2);
			@SuppressWarnings("unchecked") int result = columnComparator.getB().compare(o1, o2);
			if (result != 0) return result;
		}
		return 0;
	}

}
