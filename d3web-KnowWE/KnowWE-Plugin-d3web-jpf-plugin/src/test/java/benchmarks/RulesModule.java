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

public class RulesModule extends KnowledgeModule {

	@Override
	public String generateModuleText(int size, int depth) {
		StringBuilder text = new StringBuilder();
		text.append("%%Rules\n");

		for (int i = 0; i < size; i++) {

			int mod = i % 3;

			switch (mod) {
			case 0: {
				text.append("IF Question" + i + " = Answer2\n");
				text.append("THEN Diag1 = P7\n\n");
				text.append("IF Question" + i + " = Answer1\n");
				text.append("THEN Diag1 = N7\n\n");
				text.append("IF Question" + (i + 1) + " = Answer3\n");
				text.append("THEN Diag6 = P7\n\n");
				text.append("IF Question" + (i + 1) + " = Answer2\n");
				text.append("THEN Diag5 = P7\n\n");
				text.append("IF Question" + (i + 1) + " = Answer1\n");
				text.append("THEN Diag8 = P7\n\n");
				break;
			}
			case 1: {
				text.append("IF (Question" + i + " = Answer1 AND Question" + (i + 1) + " < 1)\n");
				text.append("THEN Diag3 = P6\n\n");
				text.append("IF (Question" + (i - 1) + " = Answer2 AND Question" + i
						+ " = Answer3)\n");
				text.append("THEN Diag3 = P2\n\n");
				text.append("IF (Question" + (i - 1) + " = Answer1 AND Question" + (i + 1)
						+ " > 5)\n");
				text.append("THEN Diag8 = P7\n\n");
				text.append("IF (Question" + i + " = Answer3 OR Question" + (i - 1)
						+ " = Answer2)\n");
				text.append("THEN Diag4 = P4\n\n");
				text.append("IF (Question" + (i - 1) + " = Answer2 AND Question" + i
						+ " = Answer2)\n");
				text.append("THEN Diag6 = P5\n\n");
				break;
			}
			case 2: {
				text.append("IF NOT (Question" + (i - 1) + " = Answer1 OR Question" + i + " > 5)\n");
				text.append("THEN Diag3 = P7\n\n");
				text.append("IF NOT (Question" + (i - 1) + " = Answer2 OR Question" + i + " < 1)\n");
				text.append("THEN Diag4 = N3\n\n");
				text.append("IF NOT (Question" + (i - 1) + " = Answer3 OR Question" + i + " > 5)\n");
				text.append("THEN Diag8 = P7\n\n");
				text.append("IF NOT (Question" + (i - 1) + " = Answer1 OR Question" + (i - 2)
						+ " = Answer1)\n");
				text.append("THEN Diag3 = N2\n\n");
				text.append("IF NOT (Question" + (i - 1) + " = Answer2 OR Question" + (i - 2)
						+ " = Answer2)\n");
				text.append("THEN Diag5 = N6\n\n");
				break;
			}
			}

		}

		text.append("%");
		return text.toString();
	}

}
