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

package de.d3web.we.kdom.dashTree.propertyDefinition;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author Jochen
 * 
 * Type for defining domain and range of an object-property
 * syntax: DOMAINDEF --> RANGEDEF   (@see DomainRangeOperatorType for Operator)
 * 
 *
 */
public class PropertyDetails extends DefaultAbstractKnowWEObjectType{
	
	@Override
	protected void init() {
		this.sectionFinder = new PropertyDetailsSectionFinder();
		this.childrenTypes.add(new DomainDefinition());
		this.childrenTypes.add(new DomainRangeOperatorType());
		this.childrenTypes.add(new RangeDefinition());
		
	}
	

}


class PropertyDetailsSectionFinder extends SectionFinder {

	private AllTextFinderTrimmed textFinder = new AllTextFinderTrimmed();
	
	@Override
	public List<SectionFinderResult> lookForSections(String text,
			Section father, KnowWEObjectType type) {
		if(text.contains("-->")) {
			return textFinder.lookForSections(text, father, type);
		}
		return null;
	}
	
}

class DomainFinder extends SectionFinder {
	@Override
	public List<SectionFinderResult> lookForSections(String text,
			Section father, KnowWEObjectType type) {
		if(text.contains(DomainRangeOperatorType.DOMAIN_RANGE_OPERATOR)) {
			
			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			result.add(new SectionFinderResult(0, text.indexOf(DomainRangeOperatorType.DOMAIN_RANGE_OPERATOR)));
			return result;
		}
		return null;
	}
	
}

class RangeDefinition extends DefaultAbstractKnowWEObjectType{

	private static RangeDefinition defaultInstance = null;
	
	public static RangeDefinition getDefaultInstance() {
		if (defaultInstance == null) {
			defaultInstance = new RangeDefinition();
			
		}

		return defaultInstance;
	}

	@Override
	protected void init() {
		this.sectionFinder = new AllTextFinderTrimmed();
		this.setCustomRenderer(new RangeRenderer());
	}
}	
	
class RangeRenderer extends KnowWEDomRenderer {

	@Override
	public void render(KnowWEArticle article, Section sec,
			KnowWEUserContext user, StringBuilder string) {
		string.append(KnowWEUtils.maskHTML("<span title=\"Range restriction\">"));
		FontColorRenderer.getRenderer(FontColorRenderer.COLOR5).render(article, sec, user, string);
		string.append(KnowWEUtils.maskHTML("</span>"));
		
	}
	
}
class DomainDefinition extends DefaultAbstractKnowWEObjectType{

private static DomainDefinition defaultInstance = null;
	
	public static DomainDefinition getDefaultInstance() {
		if (defaultInstance == null) {
			defaultInstance = new DomainDefinition();
			
		}

		return defaultInstance;
	}
	
	@Override
	protected void init() {
		this.sectionFinder = new DomainFinder();
		this.setCustomRenderer(new DomainRenderer());
	}
}

class DomainRenderer extends KnowWEDomRenderer {

	@Override
	public void render(KnowWEArticle article, Section sec,
			KnowWEUserContext user, StringBuilder string) {
		string.append(KnowWEUtils.maskHTML("<span title=\"Domain restriction\">"));
		FontColorRenderer.getRenderer(FontColorRenderer.COLOR3).render(article, sec, user, string);
		string.append(KnowWEUtils.maskHTML("</span>"));
		
	}
	
}



