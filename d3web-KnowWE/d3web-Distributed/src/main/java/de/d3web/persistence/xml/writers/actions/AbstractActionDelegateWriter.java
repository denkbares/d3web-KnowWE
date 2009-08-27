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
