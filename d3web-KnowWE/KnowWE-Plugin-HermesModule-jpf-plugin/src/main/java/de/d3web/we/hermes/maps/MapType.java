/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.hermes.maps;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.core.semantic.OwlHelper;
import de.d3web.we.core.semantic.OwlSubtreeHandler;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.core.semantic.UpperOntology;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class MapType extends AbstractXMLObjectType {

	private static final DecimalFormat format = new DecimalFormat("#.###");

	public MapType() {
		super("Map");
	}

	@Override
	public List<KnowWEObjectType> getAllowedChildrenTypes() {
		childrenTypes.add(new AbstractXMLObjectType("iframe"));
		this.setCustomRenderer(new MapRenderer());
		return childrenTypes;
	}

	private class MapTypeOWLSubTreeHandler extends OwlSubtreeHandler {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {
			IntermediateOwlObject ioo = new IntermediateOwlObject();
			String url = getIFrameSrcURL(s);
			KMLLoader kmlLoader = new KMLLoader(url);
			List<Placemark> placemarks = kmlLoader.getPlacemarks();
			for (Placemark placem : placemarks) {
				addPlacemarkToOwlObject(placem, ioo);
			}
			SemanticCoreDelegator.getInstance().addStatements(ioo, s);
			return null;
		}

	}

	private String getIFrameSrcURL(Section sec) {
		Section iframeSection = sec
				.findChildOfType(AbstractXMLObjectType.class);
		AbstractXMLObjectType objectType = (AbstractXMLObjectType) iframeSection
				.getObjectType();
		if (objectType.getXMLTagName() != "iframe") {
			// System.out.println("warning");
			return null;
		}
		Map<String, String> attributeMap = AbstractXMLObjectType
				.getAttributeMapFor(iframeSection);
		String url = attributeMap.get("src");
		return url;
	}

	public static void addPlacemarkToOwlObject(Placemark placem,
			IntermediateOwlObject ioo) {
		OwlHelper helper = UpperOntology.getInstance().getHelper();

		URI conceptURI = helper.createlocalURI(placem.getTitle());

		Literal latitude = helper.createLiteral(format.format(placem
				.getLatitude()));
		Literal longitude = helper.createLiteral(format.format(placem
				.getLongitude()));

		/* adding all OWL statements to ioo object */
		try {
			ArrayList<Statement> slist = new ArrayList<Statement>();
			slist.add(helper.createStatement(conceptURI, helper
					.createlocalURI("hasLatitude"), latitude));
			slist.add(helper.createStatement(conceptURI, helper
					.createlocalURI("hasLongitude"), longitude));
			ioo.addAllStatements(slist);
		}
		catch (RepositoryException e) {
			e.printStackTrace();
		}

	}

	private class MapRenderer extends KnowWEDomRenderer<MapType> {

		@Override
		public void render(KnowWEArticle article, Section<MapType> sec,
				KnowWEUserContext user, StringBuilder string) {
			string.append("<div id=\"map\" class=\"panel\">");
			string.append("<h3>Karte</h3>");
			String originalText = sec.getOriginalText();
			int start = originalText.indexOf("<Map>");
			int end = originalText.indexOf("</Map>");
			string.append(originalText.substring(start + 5, end));

			// dirty
			string.append("</a>");

			string.append("</div>");
		}
	}
}
