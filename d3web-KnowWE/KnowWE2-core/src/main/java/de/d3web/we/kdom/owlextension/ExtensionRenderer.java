package de.d3web.we.kdom.owlextension;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ExtensionRenderer extends KnowWEDomRenderer {

    private static KnowWEDomRenderer me;

    private ExtensionRenderer() {

    }

    @Override
    public String render(Section sec, KnowWEUserContext user, String web, String topic) {
	String header = "<p>";
	String footer = "</p>";
	String content = "";
	ExtensionObject eo = ((Extension) sec.getObjectType())
		.getExtensionObject(sec);
	content += eo.getErrorreport();
	content += "";
	if (eo.isError()) {
	    header = "<p class=\"box error\">";
	} else {
	    header = "<p class=\"box ok\">";
	}
	return KnowWEEnvironment.maskHTML(header + content + footer);
    }

    public static synchronized KnowWEDomRenderer getInstance() {
	if (me == null) {
	    me = new ExtensionRenderer();
	}
	return me;
    }

    /**
     * prevent cloning
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
	throw new CloneNotSupportedException();
    }

}
