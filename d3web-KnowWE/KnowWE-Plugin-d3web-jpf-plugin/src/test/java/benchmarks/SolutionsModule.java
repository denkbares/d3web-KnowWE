package benchmarks;

public class SolutionsModule extends KnowledgeModule {

	@Override
	public String generateModuleText(int size, int depth) {
		StringBuilder text = new StringBuilder();
		text.append("%%Solutions\n");

		for (int i = 0; i < size; i++) {

			text.append("Diag" + i + "\n");

		}

		text.append("%\n\n");
		return text.toString();
	}

}
