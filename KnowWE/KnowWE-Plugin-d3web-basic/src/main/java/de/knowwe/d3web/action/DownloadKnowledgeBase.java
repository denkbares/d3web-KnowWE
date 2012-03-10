package de.knowwe.d3web.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.we.basic.D3webKnowledgeHandler;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.knowRep.KnowledgeRepresentationHandler;

public class DownloadKnowledgeBase extends AbstractAction {

	public static final String PARAM_FILENAME = "filename";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String filename = context.getParameter(PARAM_FILENAME);
		String topic = context.getParameter(Attributes.TOPIC);
		String web = context.getParameter(Attributes.WEB);

		if (!Environment.getInstance().getWikiConnector().userCanViewPage(topic,
				context.getRequest())) {
			context.sendError(HttpServletResponse.SC_FORBIDDEN,
					"You are not allowed to download this knowledgebase");
		}

		KnowledgeRepresentationHandler handler = Environment.getInstance()
				.getKnowledgeRepresentationManager(web).getHandler("d3web");
		// before writing, check if the user defined a desired filename
		if (handler instanceof D3webKnowledgeHandler) {
			KnowledgeBase base = ((D3webKnowledgeHandler) handler).getKnowledgeBase(topic);
			String desired_filename = base.getInfoStore().getValue(BasicProperties.FILENAME);
			if (desired_filename != null) {
				filename = desired_filename;
			}
			// write the timestamp of the creation (Now!) into the knowledge
			// base
			base.getInfoStore().addValue(BasicProperties.CREATED, new Date());
		}

		URL home = handler.saveKnowledge(topic);

		InputStream in = home.openStream();
		context.setContentType("application/x-bin");

		context.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
		OutputStream outs = context.getOutputStream();

		int bit;
		try {
			while ((bit = in.read()) >= 0) {
				outs.write(bit);
			}

		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.out);
			context.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, String.valueOf(ioe));
		}
		finally {
			in.close();
		}

		outs.flush();
		outs.close();

	}

}
