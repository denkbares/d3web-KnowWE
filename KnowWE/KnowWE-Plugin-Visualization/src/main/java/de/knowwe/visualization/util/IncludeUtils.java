package de.knowwe.visualization.util;


public class IncludeUtils {

	public enum FILE_TYPE {
		CSS, JAVASCRIPT
	}

	public static String includeFile(FILE_TYPE ft, String src) {
		String result = "";
		if (ft == FILE_TYPE.CSS) {
			result += "<link rel=\"stylesheet\" href=\"";
			result += src + "\">\n";
		}
		else if (ft == FILE_TYPE.JAVASCRIPT) {
			result += "<script type=\"text/javascript\" ";
			result += "src=\"" + src + "\" charset=\"utf-8\">";
			result += "</script>\n";
		}
		return result;
	}

}
