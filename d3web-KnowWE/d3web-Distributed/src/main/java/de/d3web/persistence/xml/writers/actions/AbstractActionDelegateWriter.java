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

package de.d3web.persistence.xml.writers.actions;

import java.util.List;
import java.util.logging.Logger;

import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.psMethods.delegate.ActionDelegate;
import de.d3web.persistence.xml.writers.IXMLWriter;

public abstract class AbstractActionDelegateWriter implements IXMLWriter {
	

	/**
	 * @see AbstractXMLWriter#getXMLString(Object)
	 */
	public String getXMLString(Object o) {
		StringBuffer sb = new StringBuffer();
		List<NamedObject> theList = null;

		if (o == null) {
			Logger.getLogger(this.getClass().getName()).warning("null is no " + getType());
		} else if (!(o instanceof ActionDelegate)) {
			Logger.getLogger(this.getClass().getName()).warning(o.toString() + " is no " + getType());
		} else {
			ActionDelegate theAction = (ActionDelegate) o;

			sb.append("<Action type='" + getType() + "' ForeignNamespace='" + theAction.getTargetNamespace() + "'>\n");

			theList = theAction.getNamedObjects();
			if (theList != null) {
				if (!(theList.isEmpty())) {
					sb.append("<TargetNamedObjects>\n");
					for (NamedObject object : theList) {
						sb.append("<NamedObject ID='" + object.getId() + "'/>\n");
					}

					sb.append("</TargetNamedObjects>\n");
				}
			}
			sb.append("</Action>\n");
		}
		return sb.toString();
	}

	

	protected abstract String getType();

}
