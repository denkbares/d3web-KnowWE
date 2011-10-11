package de.knowwe.core.utils;

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

	public int length() {
		return content.length();
	}

	public String getContentTrimmed() {
		return content.trim();
	}

	public int getStartTrimmed() {
		return offset + (content.indexOf(content.trim()));
	}

	public int getEndTrimmed() {
		return getStartTrimmed() + lengthTrimmed();
	}

	public int lengthTrimmed() {
		return content.trim().length();
	}

	public String getFatherString() {
		return fatherString;
	}

}
