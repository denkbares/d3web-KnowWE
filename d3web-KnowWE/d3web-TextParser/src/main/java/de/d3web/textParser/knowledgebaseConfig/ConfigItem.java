package de.d3web.textParser.knowledgebaseConfig;

public class ConfigItem {
	
	private String name;
	private String[] answers;
	public ConfigItem(String name, String[] answers) {
		this.name = name;
		this.answers = answers;
	}
	public String[] getAnswers() {
		return answers;
	}
	public String getName() {
		return name;
	}
	
	public boolean containsAnswer(String a) {
		for (int i = 0; i < answers.length; i++) {
			if(answers[i].trim().equalsIgnoreCase(a.trim())) {
				return true;
			}
		}
		return false;
	}

}
