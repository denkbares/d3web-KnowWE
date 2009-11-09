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

package de.d3web.we.kdom.owlextension;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ExtensionRenderer extends KnowWEDomRenderer {

    private static KnowWEDomRenderer me;

    private ExtensionRenderer() {

    }

    @Override
    public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
    	String header;
		String footer = "</p>";
		String content = "";
		
		content=(String) KnowWEUtils.getStoredObject(sec,Extension.EXTENSION_RESULT_KEY);
		
		if (!content.equals("success")) {
			header = "<p class=\"box error\">";
		} else {
			header = "<p class=\"box ok\">";
		}
		string.append(KnowWEEnvironment.maskHTML(header + content + footer));
    }

    public static synchronized KnowWEDomRenderer getInstance() {
    	if (me == null)
			me = new ExtensionRenderer();
		return me;
    }

    /**
     * prevent cloning
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
    	throw new CloneNotSupportedException();
    }

}
