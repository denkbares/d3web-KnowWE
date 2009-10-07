package de.d3web.we.flow;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;


public class FlowchartSection extends AbstractXMLObjectType {
	
	protected KnowWEDomRenderer renderer = new FlowchartSectionRenderer();

	public FlowchartSection() {
		super("flowchart");
	}
	
	@Override
	protected void init() {
		this.childrenTypes.add(new NodeSection());
		this.childrenTypes.add(new XMLContent());
		
//		setNotRecyclable(true);
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return renderer;
	}
	
	public String getFlowchartName(Section sec) {
		Map<String, String> mapFor = this.getAttributeMapFor(sec);
		return mapFor.get("name");
	}


	public String getFlowchartID(Section sec) {
		Map<String, String> mapFor = this.getAttributeMapFor(sec);
		String id = mapFor.get("id");
		if (id == null) id = mapFor.get("name");
		if (id == null) id = "sheet_01";
		return id;
	}

	public String[] getStartNames(Section sec) {
		List<Section> startSections = new LinkedList<Section>();
		sec.findSuccessorsOfType(StartSection.class, startSections);
		return getSectionsContents(startSections);
	}
	
	public String[] getExitNames(Section sec) {
		List<Section> exitSections = new LinkedList<Section>();
		sec.findSuccessorsOfType(ExitSection.class, exitSections);
		return getSectionsContents(exitSections);
	}
	
	private static String[] getSectionsContents(List<Section> sections) {
		List<String> result = new LinkedList<String>();
		for (Section start : sections) {
			String content = getSectionContent(start);
			if (content != null) {
				result.add(content);
			}
		}
		return result.toArray(new String[result.size()]);
	}
	
	private static String getSectionContent(Section sec) {
		String result = null;
		
		List<Section> children = sec.getChildren();
		if (children.size() == 3) // HOTFIX for parser section returning enclosing xml-tags
			return children.get(1).getOriginalText();
		
		// Old mechanism
		for (Section child : children) {
			String text = child.getOriginalText();
			result = (result == null) ? text : (result + text);
		}
		return result;
	}
	
	
}
