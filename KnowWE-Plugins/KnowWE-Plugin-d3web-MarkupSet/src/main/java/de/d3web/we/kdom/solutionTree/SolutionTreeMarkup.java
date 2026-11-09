/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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
package de.d3web.we.kdom.solutionTree;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class SolutionTreeMarkup extends DefaultMarkupType {

	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("Solution");
		m.addContentType(() -> new SolutionsDashTree());
		PackageManager.addPackageAnnotation(m);

		m.setDocumentation("""
				Defines the solutions (diagnoses) of the knowledge base, optionally as a hierarchy.
				<p>Each line defines one solution. Leading dashes place a solution below the solution of the previous
				level, so larger sets of solutions can be grouped into problem areas. <code>~ "Text"</code> after a
				solution sets its description. Prompts and other properties are set with <code>%%Property</code>.</p>
				""");
		m.addExample("""
				%%Solution
				VerificationCompleteSolution
				%
				""");
		m.addExample("""
				%%Solution
				Display problem
				- Main display defective ~ "Replace the main display unit."
				- Status display defective
				Power supply problem
				- Fuse defective
				%
				""");
	}

	public SolutionTreeMarkup() {
		super(m);
	}

}
