/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.kbrenderer.verbalizer;

import java.util.Map;

import de.knowwe.kbrenderer.verbalizer.VerbalizationManager.RenderingFormat;

/**
 * The is the common interface of all Verbalizers. A verbalizer is a class, that
 * can verbalize (render to String representations) objects. The objects and
 * formats a verbalizers supports have to be specified in the appropriate
 * methods.
 * 
 * Verbalizers should be registered in the VerbalizationManager.
 * 
 * 
 * @author lemmerich
 * 
 */
public interface Verbalizer {

	// some parameter, that can be used in the parameter hash
	// we save them here to get a common interface
	String INDENT = "indent";
	String IS_SINGLE_LINE = "isSingleLine";
	String IS_NEGATIVE = "isNegative";
	String CONTEXT = "context";
	String ID_VISIBLE = "idVisible";
	String LOCALE = "locale";
	String USE_QUOTES = "useQuotes";

	/**
	 * Returns a verbalization (String representation) of the given object in
	 * the target format usind additional parameters.
	 * 
	 * 
	 * @param o the Object to be verbalized
	 * @param targetFormat The output format of the verbalization
	 *        (HTML/XML/PlainText...)
	 * @param parameter additional parameters used to adapt the verbalization
	 *        (e.g., singleLine, etc...)
	 * @return A String representation of given object o in the target format
	 */
	String verbalize(Object o, RenderingFormat targetFormat, Map<String, Object> parameter);

	/**
	 * Returns the classes a verbalizer can render
	 * 
	 * @return a array of all classes a verbalizer can render
	 */
	Class<?>[] getSupportedClassesForVerbalization();

	/**
	 * Returns all target formats a verbalizer can use.
	 * 
	 * @return the target formats a verbalizer can use.
	 */
	RenderingFormat[] getSupportedRenderingTargets();
}
