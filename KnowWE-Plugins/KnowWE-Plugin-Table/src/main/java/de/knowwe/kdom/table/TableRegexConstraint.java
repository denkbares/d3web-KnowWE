package de.knowwe.kdom.table;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.constraint.SectionFinderConstraint;

/**
 * Constraint that find all table headers which matches the regex and columns
 *
 * @author Veronika Oschmann (denkbares GmbH)
 * @created 03.08.23
 */
public class TableRegexConstraint implements SectionFinderConstraint {

	private final Pattern regex;
	private final int[] columns;

	public TableRegexConstraint(String regex, int... columns) {
		this(Pattern.compile(regex), columns);
	}

	public TableRegexConstraint(Pattern regex, int... columns) {
		this.columns = columns;
		this.regex = regex;
	}

	private <T extends Type> boolean satisfiesConstraint(Section<?> father) {
		int column = TableUtils.getColumn(father);
		Section<TableCellContent> headerCell = TableUtils.getColumnHeader(father);
		if (headerCell == null) {
			return false;
		}
		String headerText = headerCell.getText().trim();
		if (headerText.startsWith("||")) {
			headerText = headerText.substring(2);
		}

		boolean columnOk = columns == null || columns.length == 0 || ArrayUtils.contains(columns, column);
		return isHeaderMatching(headerText) && columnOk;
	}

	private boolean isHeaderMatching(String headerText) {
		return regex.matcher(headerText).matches();
	}

	@Override
	public <T extends Type> void filterCorrectResults(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {
		if (satisfiesConstraint(father)) return;
		found.clear();
	}
}
