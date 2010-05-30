/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.hermes.maps;

import java.util.Collection;

import de.d3web.we.core.SemanticCore;
import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.EditSectionRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.kdom.subtreeHandler.OwlSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class LocationDefinitionType extends DefaultAbstractKnowWEObjectType {
	private static final String START_TAG = "<<ORT:";
	private static final String END_TAG = ">>";

	@Override
	protected void init() {
		sectionFinder = new RegexSectionFinder(START_TAG + "[\\w|\\W]*?"
				+ END_TAG);
		this.setCustomRenderer(new EditSectionRenderer(LocationRenderer
				.getInstance()));
		this.addSubtreeHandler(new LocationDefinitionTypeOWLSubTreeHandler());
	}

	private class LocationDefinitionTypeOWLSubTreeHandler extends
	OwlSubtreeHandler<LocationDefinitionType> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<LocationDefinitionType> s) {
			IntermediateOwlObject ioo = new IntermediateOwlObject();
			Placemark placem = extractPlacemark(s);
			MapType.addPlacemarkToOwlObject(placem, ioo);
			SemanticCore.getInstance().addStatements(ioo, s);
			return null;
		}

	}

	private static Placemark extractPlacemark(Section<LocationDefinitionType> section) {
		String sectionText = section.getOriginalText();
		sectionText = sectionText.substring(START_TAG.length(), sectionText
				.length()
				- END_TAG.length());

		String locationName = null;
		Double latitude = null;
		Double longitude = null;
		String description = null;
		if (sectionText == null) {
			return null;
		}
		String[] splittedSecText = sectionText.split(";");

		//  location name omitted
		if(splittedSecText.length == 2) {
			locationName = section.getTitle();
			try {
				latitude = Double.parseDouble(splittedSecText[0]);
				longitude = Double.parseDouble(splittedSecText[1]);
				}
				catch (NumberFormatException e) {
					return null;
				}
		}
		if (splittedSecText.length > 2 && splittedSecText.length < 5) {
			locationName = splittedSecText[0].trim();
			try {
			latitude = Double.parseDouble(splittedSecText[1]);
			longitude = Double.parseDouble(splittedSecText[2]);
			}
			catch (NumberFormatException e) {
				return null;
			}
			if (splittedSecText.length == 4) {
				description = splittedSecText[3];
			}
		}

		if (locationName == null || latitude == null || longitude == null) {
			return null;
		}

		return new Placemark(locationName, latitude, longitude, description);
	}

	public static class LocationRenderer extends KnowWEDomRenderer<LocationDefinitionType> {

		private static LocationRenderer instance;

		public static LocationRenderer getInstance() {
			if (instance == null) {
				instance = new LocationRenderer();
			}
			return instance;
		}

		@Override
		public void render(KnowWEArticle article, Section<LocationDefinitionType> sec,
				KnowWEUserContext user, StringBuilder string) {
			String originalText = sec.getOriginalText();
			Placemark extractPlacemark = extractPlacemark(sec);
			if (extractPlacemark == null) {
				string.append(KnowWEUtils.maskHTML("<span class='error' title='invalid syntax'>")
						+ originalText + KnowWEUtils.maskHTML("</span>"));
			}
			else {
				String htmlString = extractPlacemark
							.toHTMLString();
				string.append(KnowWEUtils.maskHTML(htmlString));
			}
		}
	}

}
