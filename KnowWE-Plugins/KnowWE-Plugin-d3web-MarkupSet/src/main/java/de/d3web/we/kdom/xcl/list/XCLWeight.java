/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.d3web.we.kdom.xcl.list;

import com.denkbares.strings.Strings;
import de.d3web.xcl.XCLRelationType;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.EmbracedContentFinder;

/**
 * Represents a weight definition of a particular XCL relation. Weight may be numerical, e.g. "[1.5]", where the default
 * weight is "1.0". Weight may also be special keywords, to define other type of relations. These are "[--]" for
 * "contradicting relations", "[!]" for "required relations", and "[++]" for "sufficient relations".
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 28.11.2018
 */
public class XCLWeight extends AbstractType {

	public static final char BOUNDS_OPEN = '[';
	public static final char BOUNDS_CLOSE = ']';

	public XCLWeight() {
		setSectionFinder(new EmbracedContentFinder(BOUNDS_OPEN, BOUNDS_CLOSE, 1));
		setRenderer(StyleRenderer.CONTENT.setMaskMode(StyleRenderer.MaskMode.jspwikiMarkup));
	}

	/**
	 * Returns the type of the relation.
	 */
	public XCLRelationType getXCLRelationType(Section<XCLWeight> section) {
		String text = section.getText();
		if (text.contains("--")) {
			return XCLRelationType.contradicted;
		}
		else if (text.contains("!")) {
			return XCLRelationType.requires;
		}
		else if (text.contains("++")) {
			return XCLRelationType.sufficiently;
		}
		else {
			return XCLRelationType.explains;
		}
	}

	/**
	 * Returns the weight of the relation, or Double.NaN if there is no weight specified explicitly.
	 */
	public double getXCLRelationWeight(Section<XCLWeight> section) {
		String text = section.getText();
		int start = text.indexOf(BOUNDS_OPEN);
		int end = text.lastIndexOf(BOUNDS_CLOSE);
		if (start == -1 || end == -1) return Double.NaN;
		try {
			return Double.parseDouble(Strings.trim(text.substring(start + 1, end)));
		}
		catch (NumberFormatException e) {
			return Double.NaN;
		}
	}
}
