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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.wiki.api.core.Page;
import org.jetbrains.annotations.NotNull;

public class RecentChangesUtils {
	private FastDateFormat formatter;

	public String formatDateToDay(String dateString) {
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
		TemporalAccessor temporalAccessor = inputFormatter.parse(dateString);
		LocalDate date = LocalDate.from(temporalAccessor);
		formatter = FastDateFormat.getInstance("yyyy-MM-dd");
		return formatter.format(Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
	}

	@NotNull
	public String getFormattedDate(Page page) {
		LocalDate today = LocalDate.now();
		LocalDate date = page.getLastModified().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
		if (date.equals(today)) {
			formatter = FastDateFormat.getInstance("HH:mm:ss");
		}
		else {
			formatter = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
		}
		return formatter.format(page.getLastModified());
	}
}
