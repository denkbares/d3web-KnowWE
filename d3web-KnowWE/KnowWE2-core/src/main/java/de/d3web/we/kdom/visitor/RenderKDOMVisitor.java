package de.d3web.we.kdom.visitor;

import java.util.List;

import de.d3web.we.kdom.PlainText;
import de.d3web.we.kdom.Section;

public class RenderKDOMVisitor implements Visitor {

	private StringBuffer buffi;

	@Override
	public void visit(Section s) {
		buffi = new StringBuffer();
		renderSubtree(s, 0, buffi);

	}

	public String getRenderedKDOM() {
		return buffi.toString();
	}
	
	private String translateHtml(String str) {
//		str = str.replaceAll("<", "&lt;");
//		str = str.replaceAll(">", "&gt;");
//		str = str.replaceAll("\"", "&quot;");
//		str = str.replaceAll("&", "&amp;");
//		str = str.replaceAll("<", "{{{<");
//		str = str.replaceAll(">", ">}}}");
		return str;
	}

	private void renderSubtree(Section s, int i, StringBuffer buffi) {
		buffi.append(getDashes(i));
		buffi.append(" ");
		buffi.append(translateHtml(s.verbalize()));
		buffi.append("\n <br>"); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
		i++;
		List<Section> children = s.getChildren();
		if (children.size() == 1
				&& children.get(0).getObjectType() instanceof PlainText) {

		} else {
			for (Section section : children) {
				renderSubtree(section, i, buffi);
			}
		}
	}

	private String getDashes(int cnt) {
		StringBuffer dashes = new StringBuffer();
		for (int i = 0; i < cnt; i++) {
			dashes.append("-");
		}
		return dashes.toString();
	}

}
