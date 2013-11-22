package de.knowwe.jspwiki.types;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomStyleMatcher {
	
	private static Pattern PATTERN_CLASSES = Pattern.compile("%%\\(class:([^)]+)\\).*", Pattern.DOTALL);
	private static int CLASS_GROUP = 1;
	
	public static String getCustomStyle(String text) {
		Matcher m = PATTERN_CLASSES.matcher(text);
		if (m.matches()) {
			String cssClass = m.group(CLASS_GROUP);
			return cssClass;
		}
		return null;
	}

}
