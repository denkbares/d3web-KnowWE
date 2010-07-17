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

package de.d3web.we.ci4ke.handling;

import de.d3web.we.event.ArticleCreatedEvent;
import de.d3web.we.event.Event;
import de.d3web.we.event.EventListener;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

public class CIEventForwarder implements EventListener<ArticleCreatedEvent> {

	public CIEventForwarder() {
	}

	@Override
	public Class<? extends Event> getEvent() {
		return ArticleCreatedEvent.class;
	}

	@Override
	public void notify(ArticleCreatedEvent event, String web, String username, 
			Section<? extends KnowWEObjectType> s) {
		if (s.getObjectType().getClass().equals(KnowWEArticle.class)) {
			CIHookManager.getInstance().triggerHooks(s.getID());
		}

	}

}
