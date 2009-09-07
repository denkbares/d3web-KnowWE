package de.d3web.we.utils;

import javax.servlet.ServletContext;

//import org.iweb.application.Model;

/**
 * @author gbuscher
 */
public class PropertyUtils {
	
	/**
	 * Returns a String, where "$webapp_path$" is replaced by the context-path
	 * of the model's webapp.
	 */
//	public static String getRealPath(Model model, String varPath) {
//		return getRealPath(model.getWebApp().getServletConfig().getServletContext(), varPath);
//	}
	
	/**
	 * Returns a String, where "$webapp_path$" is replaced by the context-path.
	 */
	public static String getRealPath(ServletContext context, String varPath) {
		if (varPath.indexOf("$webapp_path$") != -1) {
			String realPath = context.getRealPath("");
			realPath = realPath.replace('\\', '/');
			while (realPath.endsWith("/")) {
				realPath = realPath.substring(0, realPath.length() - 1);
			}
			varPath = varPath.replaceAll("\\$webapp_path\\$", realPath);
		}
		return varPath;
	}
	
	/**
	 * Returns a String, where the context-path is replaced by "$webapp_path$".
	 */
	public static String getVariablePath(ServletContext context, String realPath) {
		String varPath = context.getRealPath("");
		varPath = varPath.replace('\\', '/');
		realPath = realPath.replaceAll(varPath, "\\$webapp_path\\$");
		return realPath;
	}

}
