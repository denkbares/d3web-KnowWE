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

package de.d3web.we.kdom.semanticAnnotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.AnnotationContext;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class AnnotationRelationOperator extends DefaultAbstractKnowWEObjectType {

	private HashMap<String, String> opstore;
	private static AnnotationRelationOperator me;

	private AnnotationRelationOperator() {
		opstore = new HashMap<String, String>();
	}

	public static synchronized AnnotationRelationOperator getInstance() {
		if (me == null) {
			me = new AnnotationRelationOperator();
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

	@Override
	public KnowWEDomRenderer getDefaultRenderer() {
		return DelegateRenderer.getInstance();
	}


	public static class AnnotationPropertySectionFinder extends SectionFinder {
		private String pattern;
		AnnotationRelationOperator type;

		public AnnotationPropertySectionFinder(AnnotationRelationOperator type) {
			this.pattern = "[\\w\\W]*::";
			this.type = type;
		}

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {
		
			ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(text);
			if(m.find()) {
				result.add(new SectionFinderResult(m.start(), m.end()));
				String prop = text.substring(m.start(),
						m.end()).replaceAll("::", "").trim();
				type.setOperator(text.substring(m.start(),
						m.end()), prop);
				AnnotationContext con = new AnnotationContext(prop);
				ContextManager.getInstance().attachContext(
						father.getFather().getFather(), con);
			}
			return result;
		}
	}

	public void setOperator(String sec, String op) {
		opstore.put(sec, op);
	}

	public String getOperator(Section sec) {
		return opstore.get(sec);
	}


	@Override
	protected void init() {
		this.sectionFinder = new AnnotationPropertySectionFinder(this);		
	}

}
