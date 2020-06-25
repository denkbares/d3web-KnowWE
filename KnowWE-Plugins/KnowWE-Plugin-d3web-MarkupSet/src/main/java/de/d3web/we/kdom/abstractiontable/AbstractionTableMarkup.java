package de.d3web.we.kdom.abstractiontable;

import java.util.Objects;
import java.util.regex.Pattern;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.table.Table;

import static de.knowwe.core.kdom.parsing.Sections.$;

public class AbstractionTableMarkup extends DefaultMarkupType {

	private static final String ANNOTATION_ACTION_COLUMNS = "actionColumns";
	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("AbstractionTable");
		MARKUP.addContentType(new Table());
		PackageManager.addPackageAnnotation(MARKUP);
		MARKUP.addAnnotation(ANNOTATION_ACTION_COLUMNS, false, Pattern.compile("\\d+"));
		MARKUP.getAnnotation(ANNOTATION_ACTION_COLUMNS)
				.setDocumentation("The number of action columns in this table. " +
						"The action columns are on the right side of the table and stand for the values to be assigned to the question or solution in the column header." +
						"<br>By default, only the last column to the right is an action column.");
		MARKUP.addAnnotationRenderer(ANNOTATION_ACTION_COLUMNS, NothingRenderer.getInstance());
	}

	public AbstractionTableMarkup() {
		super(MARKUP);
	}

	public static int getActionColumns(Section<?> abstractionTableSection) {
		return $(abstractionTableSection).closest(AbstractionTableMarkup.class)
				.map(m -> DefaultMarkupType.getAnnotation(m, ANNOTATION_ACTION_COLUMNS))
				.filter(Objects::nonNull)
				.filter(t -> t.matches("\\d+"))
				.map(Integer::parseInt).findFirst().orElse(1);
	}
}
