package de.knowwe.jspwiki.types;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;

/**
 * 
 * @author Lukas Brehl
 * @created 26.09.2012
 */

public class TableCell extends AbstractType {

	public TableCell() {
		this.setSectionFinder(new RegexSectionFinder("\\|?\\|[^|]+",
				Pattern.DOTALL | Pattern.MULTILINE));
		this.addChildType(new ParagraphTypeForLists());
	}
}
