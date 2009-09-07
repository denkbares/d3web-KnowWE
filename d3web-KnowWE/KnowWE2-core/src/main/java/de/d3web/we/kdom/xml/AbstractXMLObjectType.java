package de.d3web.we.kdom.xml;

import java.util.Map;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.utils.KnowWEUtils;

public class AbstractXMLObjectType extends DefaultAbstractKnowWEObjectType{
	
	private String xmlTagName;
	
	public Section getContentChild(Section s) {
		if(s.getObjectType() instanceof AbstractXMLObjectType) {
			Section content = s.findSuccessor(XMLContent.class);
			return content;
		}
		return null;
	}
	
	@Override
	public String getName() {
		return getXMLTagName();
	}
	
	public Map<String, String> getMapFor(Section s) {
		KnowWEArticle art = s.getArticle();
		if (art != null) {
			return (Map<String, String>) KnowWEUtils.getStoredObject(art.getWeb(), art.getTitle(), s.getId(), XMLSectionFinder.ATTRIBUTE_MAP);
		}
		return null;
	}

	public AbstractXMLObjectType(String type) {
		this.xmlTagName = type;
		childrenTypes.add(0, new XMLHead());
		childrenTypes.add(1, new XMLTail());
	}

	/**
	 * XXX Override sectioner here. If in constructor strange breaks in layout occur.
	 */
	@Override
	public SectionFinder getSectioner() {
		if (isActivated) {
			return this.sectionFinder = new XMLSectionFinder(xmlTagName, this);
		}
		return null;
	}

	public String getXMLTagName() {
		return xmlTagName;
	}

	@Override
	protected void init() {
		// nothing todo hiere
		
	}
}
