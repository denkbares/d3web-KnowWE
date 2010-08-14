package de.d3web.we.kdom.sparql.groovy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import groovy.lang.GroovyClassLoader;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sparql.SparqlDelegateRenderer;
import de.d3web.we.kdom.sparql.SparqlRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class GroovySparqlRendererRenderer extends
		KnowWEDomRenderer<GroovySparqlRendererContent> {

	private static final String CONTENT = "GROOVY_RENDERER_SOURCE_CONTENT";
	private static GroovySparqlRendererRenderer instance;

	private GroovySparqlRendererRenderer() {

	}

	public static synchronized GroovySparqlRendererRenderer getInstance() {
		if (instance == null) instance = new GroovySparqlRendererRenderer();
		return instance;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private String readFileAsString(String filePath)
			throws java.io.IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(
				new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}

	@Override
	public void render(KnowWEArticle article,
			Section<GroovySparqlRendererContent> sec, KnowWEUserContext user,
			StringBuilder string) {
		// rb = KnowWEEnvironment.getInstance().getKwikiBundle(user);
		Map<String, String> params = AbstractXMLObjectType
				.getAttributeMapFor((Section<? extends AbstractXMLObjectType>) sec
						.getFather());
		String name = "";
		if (!params.containsKey("name")) {
			string.append("no name given in groovysparqlrenderer-section use something like name=\"name\"");
			return;
		}
		else {
			name = params.get("name");
		}
		String content = sec.getOriginalText();
		KnowWEUtils.storeSectionInfo(sec, GroovySparqlRendererRenderer.CONTENT,
				content);

		GroovyClassLoader gcl = new GroovyClassLoader(this.getClass().getClassLoader());

		String basefile = KnowWEEnvironment.getInstance().getKnowWEExtensionPath()
				+ File.separatorChar + "BaseRenderer.groovy.tmpl";
		String basecontent = "";
		try {
			basecontent = readFileAsString(basefile);
		}
		catch (IOException e1) {
			string.append(e1.toString());
			return;
		}
		String finalcontent = basecontent.replace("%NAME%", name).replace("%CONTENT%", content);

		Class clazz = gcl.parseClass(finalcontent, "Renderer.groovy");
		Object aScript;

		try {
			aScript = clazz.newInstance();
			SparqlRenderer myRenderer = (SparqlRenderer) aScript;
			myRenderer.setID(content.hashCode());
			SparqlDelegateRenderer sdr = SparqlDelegateRenderer.getInstance();
			if (!sdr.hasRenderer(myRenderer.getID())) {
				sdr.addRenderer(myRenderer);
				string.append("renderer " + name + " already present");
			}
			else string.append("renderer " + name + " successfully added");
		}
		catch (InstantiationException e) {
			string.append(e.getMessage());
		}
		catch (IllegalAccessException e) {
			string.append(e.getMessage());
		}

	}

}
