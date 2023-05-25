package de.knowwe.core.kdom.basicType;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.regex.Pattern;

import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

/**
 * Date type for common date formats without time:
 * <li>yyyy-MM-dd</li>
 * <li>dd.MM.yyyy</li>
 *
 * @author Veronika Oschmann (denkbares GmbH)
 * @created 25.05.23
 */
public class DayDateType extends AbstractType {

	private static final String DATE = "(\\d{4}(-\\d{2}){2}|(\\d{2}\\.){2}\\d{4})";
	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	public static final DateFormat DATE_DE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

	private static final Pattern DATE_PATTERN = Pattern.compile(DATE);

	public DayDateType() {
		setSectionFinder(new AllTextFinderTrimmed());
		addCompileScript(new DateSubtreeHandler());
	}

	public static boolean isValid(String sectionText) {
		return DATE_PATTERN.matcher(sectionText).matches();
	}

	public static String createTimeAsTimeStamp(long time) {
		return DATE_FORMAT.format(new Date(time));
	}

	public static long getTimeInMillis(Section<DayDateType> sec) throws ParseException {
		return getDate(sec).getTime();
	}

	public static Date getDate(Section<DayDateType> sec) throws ParseException {
		try {
			return DATE_FORMAT.parse(sec.getText());
		}
		catch (ParseException ignore) {
		}
		return DATE_DE_FORMAT.parse(sec.getText());
	}

	public static String getDateAsISO(Section<DayDateType> sec) throws ParseException {
		return DATE_FORMAT.format(getDate(sec));
	}


	static class DateSubtreeHandler extends DefaultGlobalCompiler.DefaultGlobalHandler<DayDateType> {

		@Override
		public Collection<Message> create(DefaultGlobalCompiler compiler, Section<DayDateType> section) {
			if (DayDateType.isValid(section.getText())) {
				return Collections.emptyList();
			}
			else {
				LinkedList<Message> list = new LinkedList<>();
				list.add(Messages.syntaxError("Invalid date: '" + section.getText() + "'"));

				return list;
			}
		}
	}
}
