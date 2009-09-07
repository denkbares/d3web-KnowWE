package de.d3web.we.codeCompletion;



public class CompletionFinding implements Comparable{
	private String termName;
	private String completion;
	private Integer priority;
	
	public CompletionFinding(String termName, String completion, int priority) {
		this.termName = termName;
		this.completion = completion;
		this.priority = priority;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof CompletionFinding) {
			return ((CompletionFinding)o).getTermName().equals(termName);
		}
		return this.equals(o);
	}
	
	@Override
	public int hashCode() {
		return termName.hashCode();
	}
	
	public int compareTo(Object o) {
		
		if(o instanceof CompletionFinding) {
			return ((CompletionFinding)o).priority.compareTo(priority);
		}
		return 0;
	}

	public String getCompletion() {
		return completion;
	}

	public Integer getPriority() {
		return priority;
	}

	public String getTermName() {
		return termName;
	}
	
	
}