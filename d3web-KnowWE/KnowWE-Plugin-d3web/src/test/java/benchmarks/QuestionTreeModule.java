package benchmarks;

public class QuestionTreeModule extends KnowledgeModule {

	@Override
	public String generateModuleText(int size, int depth) {
		StringBuilder text = new StringBuilder();
		text.append("%%QuestionTree\n");
		text.append("QClass\n");
		
		
		for (int i = 0; i < size; i++) {
			
			int mod = i % 6;
			
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
			case 2:{
				text.append("- Question" + i + " [num]\n");
				text.append("-- < 1\n");
				text.append("--- Diag1\n");
				text.append("-- [2 4]\n");
				text.append("--- Diag2\n");
				text.append("-- > 5\n");
				text.append("--- Diag3\n");
				break;
			}
			case 3: {
				break;
			}
			case 4: {
				break;
			}
			case 5: {
				break;
			}
			}
			
		}
		
		text.append("%");
		return text.toString();
	}

}
