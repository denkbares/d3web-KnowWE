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
package de.knowwe.core.kdom;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import de.knowwe.core.compile.terminology.PageTitleTermCompileScript;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

/**
 * 
 * @author Reinhard Hatko Created on: 17.12.2009
 */
public class RootType extends AbstractType {

	private static RootType instance;

	static {
		instance = new RootType();
	}

	private RootType() {
        this.addCompileScript(new PageTitleTermCompileScript());
		this.setSectionFinder(AllTextFinder.getInstance());
		this.setRenderer((section, user, string) -> {
			Map<de.knowwe.core.compile.Compiler, Collection<Message>> messages = Messages.getMessagesMap(section);
			for (Entry<de.knowwe.core.compile.Compiler, Collection<Message>> entry : messages.entrySet()) {
				for (Message message : entry.getValue()) {
					String tag = (message.getType() == Message.Type.ERROR)
							? "error"
							: (message.getType() == Message.Type.WARNING)
									? "warning"
									: "information";
					string.append("\n%%").append(tag).append("\n");
					string.append(message.getVerbalization());
					string.append("\n/%\n\n");
				}
			}
			DelegateRenderer.getInstance().render(section, user, string);
		});

	}

	public static RootType getInstance() {
		return instance;
	}

}
