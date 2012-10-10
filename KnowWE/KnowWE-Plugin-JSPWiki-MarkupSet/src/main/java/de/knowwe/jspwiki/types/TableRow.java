package de.knowwe.jspwiki.types;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;

/**
 * 
 * @author Lukas Brehl
 * @created 26.09.2012
 */

public class TableRow extends AbstractType {

	public TableRow() {
		this.setSectionFinder(new RegexSectionFinder("(\\|?\\|[^|\n]+)+\n",
				Pattern.DOTALL | Pattern.MULTILINE));
		this.addChildType(new TableCell());
	}
}
