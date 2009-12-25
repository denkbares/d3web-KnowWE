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

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.EditSectionRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class LocationDefinitionType extends DefaultAbstractKnowWEObjectType {
    private static final String START_TAG = "<<ORT:";
    private static final String END_TAG = ">>";

    @Override
    protected void init() {
	sectionFinder = new RegexSectionFinder(START_TAG + "[\\w|\\W]*?"
		+ END_TAG);
	this.setCustomRenderer(new EditSectionRenderer(LocationRenderer.getInstance()));
    }

    @Override
    public IntermediateOwlObject getOwl(Section s) {
	IntermediateOwlObject ioo = new IntermediateOwlObject();

	Placemark placem = extractPlacemark(s.getOriginalText());
	MapType.addPlacemarkToOwlObject(placem, ioo);
	return ioo;
    }

    private static Placemark extractPlacemark(String sectionText) {
	sectionText = sectionText.substring(START_TAG.length(), sectionText
		.length()
		- END_TAG.length());

	String locationName = null;
	double latitude = Double.NaN;
	double longitude = Double.NaN;
	String description = null;
	if (sectionText == null) {
	    return null;
	}
	String[] splittedSecText = sectionText.split(";");
	if (splittedSecText.length > 2 && splittedSecText.length < 5) {
	    locationName = splittedSecText[0];
	    latitude = Double.parseDouble(splittedSecText[1]);
	    longitude = Double.parseDouble(splittedSecText[2]);
	    if (splittedSecText.length == 4) {
		description = splittedSecText[3];
	    }
	} else {
	    return null;
	}

	return new Placemark(locationName, latitude, longitude, description);
    }

    public static class LocationRenderer extends KnowWEDomRenderer {

	private static LocationRenderer instance;

	public static LocationRenderer getInstance() {
	    if (instance == null) {
		instance = new LocationRenderer();
	    }
	    return instance;
	}

	@Override
	public void render(KnowWEArticle article, Section sec,
		KnowWEUserContext user, StringBuilder string) {
	    string.append(extractPlacemark(sec.getOriginalText())
		    .toHTMLString());
	}
    }

}
