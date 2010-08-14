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
