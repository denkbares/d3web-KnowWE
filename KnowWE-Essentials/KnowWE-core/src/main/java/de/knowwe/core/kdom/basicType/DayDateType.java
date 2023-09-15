package de.knowwe.core.kdom.basicType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.regex.Pattern;

import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
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
	private static final Pattern DATE_PATTERN = Pattern.compile(DATE);

	public DayDateType() {
		setSectionFinder(new RegexSectionFinder(DATE_PATTERN));
		addCompileScript(new DateSubtreeHandler());
	}

	public static boolean isValid(String sectionText) {
		return DATE_PATTERN.matcher(sectionText).matches();
	}

	public static LocalDate getDate(Section<DayDateType> sec) {
		return LocalDate.parse(sec.getText());
	}

	public static String getISOLocalDate(Section<DayDateType> sec) {
		return getDate(sec).format(DateTimeFormatter.ISO_LOCAL_DATE);
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
