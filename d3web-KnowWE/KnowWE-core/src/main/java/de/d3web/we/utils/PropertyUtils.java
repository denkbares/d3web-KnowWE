/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
