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

package de.d3web.we.kdom.xcl.list;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class CoveringListMarkup extends DefaultMarkupType {

	public static final String OTHER_QUESTIONS = "otherQuestions";
	public static final String OTHER_QUESTIONS_IGNORE = "ignore";
	public static final String OTHER_QUESTIONS_NORMAL_VALUE_COVERED = "normalValueCovered";
	public static final String ESTABLISHED_THRESHOLD = "establishedThreshold";
	public static final String SUGGESTED_THRESHOLD = "suggestedThreshold";
	public static final String MIN_SUPPORT = "minSupport";
	public static final String DESCRIPTION = "description";

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("CoveringList");
		MARKUP.addContentType(() -> new CoveringList());
		MARKUP.addAnnotation(ESTABLISHED_THRESHOLD, false);
		MARKUP.addAnnotation(SUGGESTED_THRESHOLD, false);
		MARKUP.addAnnotation(MIN_SUPPORT, false);
		PackageManager.addPackageAnnotation(MARKUP);
		MARKUP.addAnnotation(DESCRIPTION, false);
		MARKUP.addAnnotation(OTHER_QUESTIONS, false,
				OTHER_QUESTIONS_IGNORE, OTHER_QUESTIONS_NORMAL_VALUE_COVERED);

		MARKUP.setTemplate("%%CoveringList\n" +
				"«solution» {\n" +
				"  «covered-condition» ,\n" +
				"  «...» ,\n" +
				"}\n" +
				"%\n\n");

		MARKUP.setDocumentation("""
				Defines set-covering (XCL) knowledge, the findings that are explained (covered) by a solution.
				<p>The first line names the solution (several solutions separated by commas share the same list),
				followed by the covered conditions in braces, separated by commas. A condition may be followed by a
				weight in square brackets:</p>
				<ul>
				<li><code>[2]</code> – numeric weight of the relation (default 1),</li>
				<li><code>[--]</code> – contradicting: the finding rules out the solution,</li>
				<li><code>[!]</code> – required: the finding must be present for the solution,</li>
				<li><code>[++]</code> – sufficient: the finding alone establishes the solution.</li>
				</ul>
				<p>Lines starting with <code>//</code> are comments. The thresholds of the score algorithm can be
				overridden per solution with the annotations.</p>
				""");
		MARKUP.getAnnotation(ESTABLISHED_THRESHOLD)
				.setDocumentation("Score from which this solution is established, overriding the default of the score algorithm (usually 0.8).")
				.setExample("@establishedThreshold: 0.9");
		MARKUP.getAnnotation(SUGGESTED_THRESHOLD)
				.setDocumentation("Score from which this solution is suggested, overriding the default of the score algorithm (usually 0.3).")
				.setExample("@suggestedThreshold: 0.5");
		MARKUP.getAnnotation(MIN_SUPPORT)
				.setDocumentation("Minimal support this solution needs to be rated, overriding the default of the score algorithm (usually 0.01).")
				.setExample("@minSupport: 0.1");
		MARKUP.getAnnotation(DESCRIPTION)
				.setDocumentation("Sets the description of the solution.")
				.setExample("@description: The battery cannot provide enough energy to turn over the starter.");
		MARKUP.getAnnotation(OTHER_QUESTIONS)
				.setDocumentation("Defines how answered questions without a relation in this list are treated for this solution.")
				.setValueDocumentation(OTHER_QUESTIONS_IGNORE, "Only the questions referenced in this list are relevant for the solution.")
				.setValueDocumentation(OTHER_QUESTIONS_NORMAL_VALUE_COVERED, "Normal values of other questions count as covered by the solution.")
				.setExample("@otherQuestions: ignore");
		MARKUP.addExample("""
				%%CoveringList
				Flat battery {
				  "Check: Battery." = not ok [++],
				}
				%
				""");
		MARKUP.addExample("""
				%%CoveringList
				Clogged air filter {
				  // typical symptoms
				  Driving = weak acceleration [2],
				  Exhaust pipe color evaluation = abnormal,
				  Mileage evaluation = increased,
				  "Check: Air filter." = not ok [!],
				  Exhaust fumes = invisible [--],
				}
				@establishedThreshold: 0.9
				%
				""");
	}

	public CoveringListMarkup() {
		super(MARKUP);
	}
}
