package de.d3web.we.hermes.maps;

import java.util.List;
import java.util.Map;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class MapType extends AbstractXMLObjectType {

    public MapType() {
	super("Map");
    }

    @Override
    public List<KnowWEObjectType> getAllowedChildrenTypes() {
	childrenTypes.add(new AbstractXMLObjectType("iframe"));
	return childrenTypes;
    }

    @Override
    public KnowWEDomRenderer getRenderer() {
	return new MapRenderer();
    }

    private class MapRenderer extends KnowWEDomRenderer {
	@Override
	public void render(Section sec, KnowWEUserContext user,
		StringBuilder string) {
	    Section iframeSection = sec
		    .findChildOfType(AbstractXMLObjectType.class);
	    AbstractXMLObjectType objectType = (AbstractXMLObjectType) iframeSection
		    .getObjectType();
	    if (objectType.getXMLTagName() != "iframe") {
		System.out.println("warning");
		return;
	    }
	    Map<String, String> attributeMap = AbstractXMLObjectType
		    .getAttributeMapFor(iframeSection);
	    String url = attributeMap.get("src");

	    string.append("URL: " + url + "<br />");
	    KMLLoader kmlLoader = new KMLLoader(url);
	    List<Placemark> placemark = kmlLoader.getPlacemarks();
	    string.append(placemark.toString());
	}
    }
}
