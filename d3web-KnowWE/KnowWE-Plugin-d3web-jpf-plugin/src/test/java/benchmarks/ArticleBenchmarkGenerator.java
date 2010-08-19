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

import java.util.ArrayList;
import java.util.List;

public class ArticleBenchmarkGenerator {

	public static void main(String[] args) {

		List<KnowledgeModule> modules = new ArrayList<KnowledgeModule>();

		modules.add(new SolutionsModule());
		modules.add(new QuestionTreeModule());
		// modules.add(new XCLModule());
		modules.add(new RulesModule());

		for (KnowledgeModule module : modules) {
			System.out.println("[{KnowWEPlugin fullParse}]");
			System.out.println(module.generateModuleText(50, 0));
		}

	}

}
