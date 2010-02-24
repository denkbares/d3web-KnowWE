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

package de.d3web.we.action;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

import de.d3web.we.basic.Information;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.persistence.SessionPersistenceHandler;

public class LoadSessionAction extends AbstractKnowWEAction {


	@Override
	public String perform(KnowWEParameterMap map) {
		String dir = D3webModule.getSessionPath(map);
		String sessionFileName = map.get(KnowWEAttributes.SESSION_FILE);
		File session = new File(dir, sessionFileName);
		Broker broker = D3webModule.getBroker(map);
		List<Information> infos;
		try {
			infos = SessionPersistenceHandler.getInstance().loadSession(broker, session.toURI().toURL());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "ERROR: malformed SessionURL";
		}
		broker.clearDPSSession();
		broker.getSession().getBlackboard().setAllInformation(infos);
		broker.processInit();
		return "done";
	}

}
