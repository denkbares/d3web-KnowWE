package de.knowwe.diaflux.utils;

import java.awt.*;

import sun.font.FontDesignMetrics;

/**
 * @author Adrian Müller
 * @created 27.01.17
 */
public abstract class AbstractDiaFluxToDotConverter extends AbstractDiaFluxConverter {

	private final FontMetrics metrics = FontDesignMetrics.getMetrics(new Font("Helvetica", Font.PLAIN, 16));
	private final int FONT_HEIGHT = 16;
	private final int MAX_NODE_WIDTH = 240;
	private final int MIN_NODE_WIDTH = 104;

	protected String convert() {
		return super.convert("digraph G {\n" +
				"\tgraph [fontname=Helvetica,\n" +
				"\t\tfontsize=16,\n" +
				"\t\tshape=box];\n" +
				"\tnode [fixedsize=true];", "}").toString();
	}

	protected String escapeQuoteAndBackslash(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	protected void appendWidthAndHeight(String type, String label) {
		int text_width;
		int text_height;
		int lines = 1;
		if (type.equals("action") || type.equals("decision")) {
			String[] labelparts = label.split(" = ");
			String object = labelparts[0];
			String value;
			if (labelparts.length == 2) {
				value = labelparts[1];
			}
			else {
				value = "ask";
			}
			// width little gif in background (-> 21)
			text_width = metrics.stringWidth(object) + 21;
			if (text_width > MAX_NODE_WIDTH) {
				lines += text_width / MAX_NODE_WIDTH;
				text_width = MAX_NODE_WIDTH;
			}
			lines++;
			int value_text_width = metrics.stringWidth(value);
			if (value_text_width > MAX_NODE_WIDTH) {
				lines += value_text_width / MAX_NODE_WIDTH;
				value_text_width = MAX_NODE_WIDTH;
			}
			if (value_text_width > text_width) {
				text_width = value_text_width;
			}
			// bit of space between object and value (-> 5)
			text_height = lines * FONT_HEIGHT + 5;
		}
		else {
			text_width = metrics.stringWidth(label);
			if (text_width > MAX_NODE_WIDTH) {
				lines += text_width / MAX_NODE_WIDTH;
				text_width = MAX_NODE_WIDTH;
			}
			text_height = lines * FONT_HEIGHT;
		}
		if (text_width < MIN_NODE_WIDTH) {
			text_width = MIN_NODE_WIDTH;
		}
		//decorator
		text_width += 13;
		text_height += 13;
		res.append(",\n\t\t")
				.append("width=")
				.append(text_width / 72d)
				.append(",\n\t\t")
				.append("height=")
				.append(text_height / 72d);
	}

}
