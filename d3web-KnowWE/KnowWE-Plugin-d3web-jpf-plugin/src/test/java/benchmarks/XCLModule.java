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
