package de.knowwe.core.kdom.basicType;

/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.denkbares.strings.Strings;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

/**
 * This type represents an instant in time. Different units of time (and their
 * combination, eg 1h30m) is supported.
 * 
 * @author Florian Ziegler / Sebastian Furth
 * @created 12.08.2010
 */
public class TimeStampType extends AbstractType {

	public static final String DURATION = "\\s*(\\d+)\\s*(ms|s|min|h|d)\\s*";
	public static final String TIMESTAMP = "(\\s*-?\\s*" + DURATION + ")+";

	private static final long[] TIME_FACTORS = {
			1, 1000, 60 * 1000, 60 * 60 * 1000, 24 * 60 * 60 * 1000 };

	private static final String[] TIME_UNITS = {
			"ms", "s", "min", "h", "d" };

	// TODO does not work with isValid
	public static final Pattern DURATION_PATTERN = Pattern.compile(
			DURATION,
			Pattern.CASE_INSENSITIVE);

	public static final Pattern TIMESTAMP_PATTERN = Pattern.compile(
			TIMESTAMP,
			Pattern.CASE_INSENSITIVE);

	public TimeStampType() {
		setSectionFinder(new AllTextFinderTrimmed());
		addCompileScript(new TimeStampSubtreeHandler());
	}

	public static boolean isValid(String sectionText) {
		return TIMESTAMP_PATTERN.matcher(sectionText).matches();
	}

	public static long getTimeInMillis(Section<TimeStampType> sec) {
		return getTimeInMillis(sec.getText());
	}

	public static long getTimeInMillis(String time) throws NumberFormatException {
		time = Strings.trim(time);

		boolean negative = false;
		if (Strings.trim(time).startsWith("-")) {
			negative = true;
			time = time.substring(1);
		}

		Matcher matcher = DURATION_PATTERN.matcher(time);

		long result = 0;
		int index = 0;
		boolean found = false;
		while (matcher.find(index)) {
			found = true;
			String numString = matcher.group(1);
			String unit = matcher.group(2);
			double num = Double.parseDouble(numString);
			for (int i = 0; i < TIME_UNITS.length; i++) {
				if (TIME_UNITS[i].equalsIgnoreCase(unit)) {
					result += (long) Math.rint(TIME_FACTORS[i] * num);
				}
			}

			int next = matcher.end();
			if (next <= index) break;
			if (next >= time.length()) break;
			index = next;

		}
		if (!found) throw new NumberFormatException("No valid time found in '" + time + "'");
		return result * (negative ? -1 : 1);
	}

	class TimeStampSubtreeHandler extends DefaultGlobalCompiler.DefaultGlobalHandler<TimeStampType> {

		@Override
		public Collection<Message> create(DefaultGlobalCompiler compiler, Section<TimeStampType> s) {
			if (TimeStampType.isValid(s.getText())) {
				return Collections.emptyList();
			}
			else {
				LinkedList<Message> list = new LinkedList<>();
				list.add(Messages.syntaxError("Invalid time stamp: '" + s.getText() + "'"));
				return list;

			}

		}

	}

}
