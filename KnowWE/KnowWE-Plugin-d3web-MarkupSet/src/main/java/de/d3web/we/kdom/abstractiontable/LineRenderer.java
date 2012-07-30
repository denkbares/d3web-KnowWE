package de.d3web.we.kdom.abstractiontable;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.table.TableLineRenderer;

public class LineRenderer extends TableLineRenderer {

	@Override
	protected String getClasses(Section<?> tableLine, UserContext user) {
		if (Messages.getMessages(tableLine, Message.Type.ERROR).size() > 0) {
			return "lineerror";
		}
		return super.getClasses(tableLine, user);
	}

}
