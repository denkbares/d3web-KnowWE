/*
 * Copyright (C) 2016 denkbares GmbH, Germany
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

package de.knowwe.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.knowwe.core.action.Action;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.util.Icon;

import static de.knowwe.core.Attributes.SECTION_ID;
import static de.knowwe.core.Attributes.TOPIC;

/**
 * A tool causing the given action being called asynchronously passing the given section id, waits for action completion and triggers a page reload.
 *
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 24.03.16.
 */
public class AsynchronousActionTool extends DefaultTool {


    public AsynchronousActionTool(Icon icon, String title, String description, Class<? extends Action> action, Section<?> section, Map<String, String> params) {
        this(icon, title, description, action, section, null, params);
    }


    public AsynchronousActionTool(Icon icon, String title, String description, Class<? extends Action> action, Section<?> section) {
        this(icon, title, description, action, section, null, new HashMap<>());
    }

    public AsynchronousActionTool(Icon icon, String title, String description, Class<? extends Action> action, Section<?> section, String currentPageTitle, Map<String, String> params) {
        super(icon, title, description,
                buildJsAction(action, section, currentPageTitle, "window.location.reload()", params),
                Tool.ActionType.ONCLICK, null);
    }

    public AsynchronousActionTool(Icon icon, String title, String description, Class<? extends Action> action, Section<?> section, String currentPageTitle) {
        super(icon, title, description,
                buildJsAction(action, section, currentPageTitle, "window.location.reload()", new HashMap<>()),
                Tool.ActionType.ONCLICK, null);
    }

    public AsynchronousActionTool(Icon icon, String title, String description, Class<? extends Action> action, Section<?> section, String currentPageTitle, String redirectPage) {
        super(icon, title, description,
                buildJsAction(action, section, currentPageTitle, "window.location='Wiki.jsp?page=" + redirectPage + "'", new HashMap<>()),
                Tool.ActionType.ONCLICK, null);
    }

    private static String buildJsAction(Class<? extends Action> action, Section<?> section, String currentPageTitle, String successFunction, Map<String, String> params) {
        params.put(TOPIC, currentPageTitle);
        return "jq$.ajax({url : 'action/" + action.getSimpleName() + "', " +
                "cache : false, " +
                createData(section.getID(), params) +
                "success : function() {" + successFunction + "} })";
    }

    private static String createData(String sectionId, Map<String, String> params) {
        String additionalKeyValuePairs = "";
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            additionalKeyValuePairs += "'," + key + " : '" + params.get(key);
        }
        return "data: { " + SECTION_ID + " : '" + sectionId + additionalKeyValuePairs + "'}, ";
    }
}
