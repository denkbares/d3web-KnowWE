package de.d3web.we.search;

public class GenericSearchResult {
	private String pagename;
	private String[] contexts;
	private int score;

	public GenericSearchResult(String page, String[] contexts, int score) {
		this.pagename = page;
		this.contexts = contexts;
		this.score = score;
	}

	public String getPagename() {
		return pagename;
	}

	public void setPagename(String pagename) {
		this.pagename = pagename;
	}

	public String[] getContexts() {
		return contexts;
	}

	public void setContexts(String[] contexts) {
		this.contexts = contexts;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	@Override
	public int hashCode() {
		int hash = pagename.hashCode();
		for (String string : contexts) {
			hash += string.hashCode();
		}
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof GenericSearchResult) {
			GenericSearchResult other = ((GenericSearchResult) o);
			if (pagename.equals(other.pagename)) {
				for (int i = 0; i < contexts.length; i++) {
					if (!contexts[i].equals(other.contexts[i])) {
						return false;
					}
				}
				return true;
			}
		}

		return false;
	}

}
