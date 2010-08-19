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

public class QuestionTreeModule extends KnowledgeModule {

	@Override
	public String generateModuleText(int size, int depth) {
		StringBuilder text = new StringBuilder();
		text.append("%%QuestionTree\n");

		text.append("\n");
		text.append("QPage1\n");
		text.append("QPage2\n");
		text.append("QPage3\n\n");
		text.append("QClass\n");

		for (int i = 0; i < size; i++) {

			int mod = i % 3;

			switch (mod) {
			case 0: {
				text.append("- Question" + i + " [oc]\n");
				text.append("-- Answer1\n");
				text.append("-- Answer2\n");
				break;
			}
			case 1: {
				text.append("- Question" + i + " [mc]\n");
				text.append("-- Answer1\n");
				text.append("-- Answer2\n");
				text.append("-- Answer3\n");
				break;
			}
			case 2: {
				text.append("- Question" + i + " [num]\n");
				text.append("-- < 1\n");
				text.append("--- QPage1\n");
				text.append("-- [2 4]\n");
				text.append("--- QPage2\n");
				text.append("-- > 5\n");
				text.append("--- QPage3\n");
				break;
			}
			}

		}

		text.append("%\n\n");
		return text.toString();
	}

}
