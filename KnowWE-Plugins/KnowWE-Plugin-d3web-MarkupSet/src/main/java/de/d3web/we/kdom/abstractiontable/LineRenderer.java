package de.d3web.we.kdom.abstractiontable;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.table.TableLineRenderer;

public class LineRenderer extends TableLineRenderer {

	public LineRenderer() {
	}

	@Override
	protected String getClasses(Section<?> tableLine, UserContext user) {
		if (!Messages.getMessagesMap(tableLine, Message.Type.ERROR).isEmpty()) {
			return "lineerror";
		}
		return super.getClasses(tableLine, user);
	}
}
