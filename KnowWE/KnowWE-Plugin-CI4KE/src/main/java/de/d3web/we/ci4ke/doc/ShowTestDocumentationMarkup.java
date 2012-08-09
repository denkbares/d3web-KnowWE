/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.d3web.we.ci4ke.doc;

import java.util.List;

import de.d3web.testing.Test;
import de.d3web.testing.TestManager;
import de.d3web.testing.TestParameter;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Strings;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Shows documentation for all tests that are available on the current KnowWE installation
 * 
 * @author jochenreutelshofer
 * @created 31.07.2012 
 */
public class ShowTestDocumentationMarkup extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("TestDocumentation");
	}

	public ShowTestDocumentationMarkup() {
		super(MARKUP);
		this.setIgnorePackageCompile(true);
		this.setRenderer(new TestDocumentationRenderer());
	}
	
	class TestDocumentationRenderer extends DefaultMarkupRenderer {
		
		@Override
		public void render(Section<?> section, UserContext user, StringBuilder buff) {
			List<String> allTests = TestManager.findAllTestNames();
			
			StringBuffer buffer = new StringBuffer();
			
			buffer.append("<table class='wikitable'>");
			
			// table header line
			buffer.append("<th>");
			buffer.append("Name of test");
			buffer.append("</th>");
		
			buffer.append("<th>");
			buffer.append("Test object class");
			buffer.append("</th>");
			
				
			buffer.append("<th>");
			buffer.append("Description");
			buffer.append("</th>");
			
			buffer.append("<th>");
			buffer.append("Parameters");
			buffer.append("</th>");
			
			for (String testName : allTests) {
				buffer.append("<tr>");
				
				Test<?> t = TestManager.findTest(testName);
				
				// name
				buffer.append("<td>");
				buffer.append(testName);
				buffer.append("</td>");
				
				// test object class
				buffer.append("<td>");
				buffer.append(t.getTestObjectClass().getSimpleName());
				buffer.append("</td>");
				
				
				
				// description
				buffer.append("<td>");
				buffer.append(t.getDescription());
				buffer.append("</td>");
				
				
				// test parameters
				buffer.append("<td>");
				List<TestParameter> parameters = t.getParameterSpecification();
				if(parameters.size() == 0) {
					buffer.append("none");
				}
				int i = 1;
				for (TestParameter testParameter : parameters) {
					buffer.append(i+".: "+testParameter.toString()+"\n");
					i++;
				}
				buffer.append("</td>");
				
				buffer.append("</tr>");
			}
			
			buffer.append("</table>");
			
			buff.append(Strings.maskHTML(buffer.toString()));
		}
		
	}
	
}
