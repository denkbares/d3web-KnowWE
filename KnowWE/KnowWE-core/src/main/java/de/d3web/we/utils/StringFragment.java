package de.d3web.we.utils;

public class StringFragment {

	private final String content;
	private final int offset;
	private final String fatherString;

	public StringFragment(String content, int offset, String fatherString) {
		super();
		this.content = content;
		this.offset = offset;
		this.fatherString = fatherString;
	}

	public String getContent() {
		return content;
	}

	public int getStart() {
		return offset;
	}

	public int getEnd() {
		return offset + content.length();
	}

	public String getFatherString() {
		return fatherString;
	}

}
