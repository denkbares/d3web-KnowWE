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

import java.util.Arrays;
import java.util.Map;

import de.d3web.core.knowledge.terminology.Solution;
import com.denkbares.utils.Log;
import de.knowwe.kbrenderer.verbalizer.VerbalizationManager.RenderingFormat;

/**
 * This class can verbalize (render to String representation an Diagnosis
 * object. It implements the Verbalizer interface.
 * 
 * @author lemmerich
 * @date june 2008
 */
public class DiagnosisVerbalizer implements Verbalizer {

	/**
	 * Returns the classes RuleVerbalizer can render
	 */
	@Override
	public Class<?>[] getSupportedClassesForVerbalization() {
		return new Class[] { Solution.class };
	}

	@Override
	/**
	 * Returns the targetFormats (Verbalization.RenderingTarget) the
	 * RuleVerbalizer can render
	 */
	public RenderingFormat[] getSupportedRenderingTargets() {
		return new RenderingFormat[] {
				RenderingFormat.HTML, RenderingFormat.PLAIN_TEXT };
	}

	@Override
	/**
	 * Returns a verbalization (String representation) of the given Diagnosis in
	 * the target format using additional parameters.
	 * 
	 * 
	 * @param o
	 *            the Diagnosis to be verbalized. returns null and logs a warning for non-diagnosis.
	 * @param targetFormat
	 *            The output format of the verbalization (HTML/PlainText...)
	 * @param parameter
	 *            additional parameters used to adapt the verbalization (e.g.,
	 *            singleLine, etc...)
	 * @return A String representation of given object o in the target format
	 */
	public String verbalize(Object o, RenderingFormat targetFormat, Map<String, Object> parameter) {

		// These two ifs are for safety only and should not be needed
		if (!Arrays.asList(getSupportedRenderingTargets()).contains(targetFormat)) {
			// this should not happen, cause VerbalizationManager should not
			// delegate here in this case!
			Log.warning("RenderingTarget" + targetFormat + " is not supported by this verbalizer!");
			return null;
		}
		if (!(o instanceof Solution)) {
			// this should not happen, cause VerbalizationManager should not
			// delegate here in this case!
			Log.warning("Object " + o + " couldnt be rendered by this verbalizer!");
			return null;
		}

		// we can be sure here o is a diagnosis, so cast it
		Solution diag = (Solution) o;
		// set the default parameter for idVisible
		boolean idVisible = false;

		// read the parameter for idVisible from the parameter Hash, if possible
		if ((!(parameter == null)) && parameter.containsKey(Verbalizer.ID_VISIBLE)) {
			Object paraIDVisible = parameter.get(Verbalizer.ID_VISIBLE);
			// catch illegal object saved in parameter Hash
			if (paraIDVisible instanceof Boolean) idVisible = (Boolean) paraIDVisible;
		}

		String s = diag.getName();
		if (idVisible) {
			s += " (" + diag.getName() + ")";
		}

		return s;
	}
}
