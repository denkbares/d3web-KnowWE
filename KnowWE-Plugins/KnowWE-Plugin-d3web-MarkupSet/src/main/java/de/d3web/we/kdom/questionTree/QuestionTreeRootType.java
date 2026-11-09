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

package de.d3web.we.kdom.questionTree;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;

public class QuestionTreeRootType extends QuestionTree {

	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("Question");
		m.addContentType(() -> new QuestionDashTree());
		PackageManager.addPackageAnnotation(m);

		m.setDocumentation("""
				Defines the questions of the knowledge base as a dash tree, optionally grouped into questionnaires.
				<p>A line without leading dashes defines a questionnaire (or a top-level question); the questions below
				it are indented with one dash per level. The question type follows in square brackets:
				<code>[oc]</code> one choice, <code>[mc]</code> multiple choice, <code>[yn]</code> yes/no,
				<code>[num]</code> numeric, <code>[date]</code>, <code>[text]</code> and <code>[info]</code>. The answers
				of a choice question are listed one level deeper.</p>
				<ul>
				<li>Numeric questions may declare a unit in braces and a range in parentheses:
				<code>Battery voltage [num] {V} (0 15)</code>.</li>
				<li><code>&lt;abstract&gt;</code> at the end of the line marks a question whose value is derived and not
				asked.</li>
				<li><code>~ "Text"</code> adds a prompt to a question or questionnaire.</li>
				<li>A question or questionnaire below an answer is a follow-up, indicated by that answer;
				<code>&amp;REF Name</code> references a question defined elsewhere in the tree.</li>
				<li><code>#1</code>, <code>#2</code>, … after a questionnaire makes it an initial questionnaire,
				presented at the start of every session in that order.</li>
				</ul>
				""");
		m.addExample("""
				%%Question
				Observations #2
				- Exhaust fumes [oc]
				-- black
				-- blue
				-- invisible
				- Engine start [oc]
				-- engine barely starts
				-- engine starts
				-- does not start

				Technical Examinations ~ "Checks in the workshop"
				- "Check: Battery." [oc]
				-- ok
				-- not ok
				%
				""");
		m.addExample("""
				%%Question
				General #1
				- Make of car [oc]
				-- VW
				-- Opel
				-- Other
				- Year of construction [num]
				-- >= 1995
				--- Does your car have ABS? [yn]
				- "Real mileage  /100km" [num] {l} (0 50)
				- Driving [mc]
				-- unsteady idle speed
				-- weak acceleration
				- Remarks [text]
				%
				""");
	}

	public QuestionTreeRootType() {
		super(m);
	}

}
