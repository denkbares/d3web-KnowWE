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

/**
 * 
 */
package de.d3web.we.kdom.semanticAnnotation;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.DefaultSubjectContext;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

/**
 * @author kazamatzuri
 * 
 */
public class AnnotationSubject extends DefaultAbstractKnowWEObjectType {

    /*
     * (non-Javadoc)
     * 
     * @see de.d3web.we.dom.AbstractKnowWEObjectType#init()
     */
    @Override
    protected void init() {
    	this.sectionFinder = new AnnotationSubjectSectionFinder();
    }

    public static class AnnotationSubjectSectionFinder extends SectionFinder {

    	@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father) {
			if (father.hasRightSonOfType(AnnotationProperty.class, text)) {
				ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
				if (text.trim().length() == 0)
					return null;
				DefaultSubjectContext sol = new DefaultSubjectContext();
				sol.setSubject(text.trim());
				ContextManager.getInstance().attachContext(father, sol);
				result.add(new SectionFinderResult(0, text.length()));
				return result;
			}
			return null;
		}
    }
}
