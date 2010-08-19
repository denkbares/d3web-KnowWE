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

package benchmarks;

public class XCLModule extends KnowledgeModule {

	@Override
	public String generateModuleText(int size, int depth) {
		StringBuilder text = new StringBuilder();

		for (int i = 0; i < size; i++) {

			int mod = i % 3;

			switch (mod) {
			case 0: {
				// text.append("<SetCoveringList-section>\n");
				text.append("%%CoveringList\n");
				text.append("Diag" + i + " {\n");
				text.append("Question" + i + " = " + "Answer1,\n");
				break;
			}
			case 1: {
				text.append("Question" + i + " = " + "Answer3 OR\n");
				text.append("Question" + i + " = " + "Answer2,\n");
				break;
			}
			case 2: {
				text.append("Question" + i + " < " + "1 OR\n");
				text.append("Question" + i + " > " + "5,\n");
				text.append("}\n");
				text.append("%\n\n");
				// text.append("</SetCoveringList-section>\n\n");
				break;
			}
			}

		}

		if (!text.toString().endsWith("}\n%\n\n")) {
			text.append("}\n%\n\n");
		}
		return text.toString();
	}

}
