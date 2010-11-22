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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An abstract implementation of a CITest, which implements the init(CIConfig)
 * and setParameters(List<String>) methods.
 * 
 * @author Marc-Oliver Ochlast (denkbares GmbH)
 * @created 14.11.2010
 */
public abstract class AbstractCITest implements CITest {

	protected CIConfig config;

	protected List<String> parameters;

	public AbstractCITest() {
		this.config = CIConfig.DUMMY_CONFIG;
		this.parameters = new ArrayList<String>();
	}

	@Override
	public void init(CIConfig config) {
		try {
			this.config = (CIConfig) config.clone();
		}
		catch (CloneNotSupportedException e) {
		}
	}

	@Override
	public void setParameters(List<String> parameters) {
		this.parameters = Collections.unmodifiableList(parameters);
	}

	public String getParameter(int index) {
		return parameters.get(index);
	}
}
