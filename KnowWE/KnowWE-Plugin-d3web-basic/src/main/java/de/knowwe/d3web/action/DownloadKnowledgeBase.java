package de.knowwe.d3web.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.knowRep.KnowledgeRepresentationHandler;

public class DownloadKnowledgeBase extends AbstractAction {

	public static final String PARAM_FILENAME = "filename";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String filename = context.getParameter(PARAM_FILENAME);
		String title = context.getParameter(Attributes.TOPIC);
		String web = context.getParameter(Attributes.WEB);

		if (!Environment.getInstance().getWikiConnector().userCanViewArticle(title,
				context.getRequest())) {
			context.sendError(HttpServletResponse.SC_FORBIDDEN,
					"You are not allowed to download this knowledgebase");
		}

		KnowledgeRepresentationHandler handler = Environment.getInstance()
				.getKnowledgeRepresentationManager(web).getHandler("d3web");

		Collection<String> articlesWithWrongVersion = getArticlesOfKnowledgebaseLoadedWithWrongVersion(
				web, title);

		if (articlesWithWrongVersion.size() > 0) {
			context.sendError(409,
					"The following articles are currently loaded with an outdated version: "
							+ articlesWithWrongVersion.toString()
							+ ". Refresh them or go back to an up to date version to be"
							+ " able to download the knowledgebase.");
			return;
		}

		// before writing, check if the user defined a desired filename
		KnowledgeBase base = D3webUtils.getKnowledgeBase(web, title);
		String desiredFilename = base.getInfoStore().getValue(BasicProperties.FILENAME);
		if (desiredFilename != null) {
			filename = desiredFilename;
		}
		// write the timestamp of the creation (Now!) into the knowledge
		// base
		base.getInfoStore().addValue(BasicProperties.CREATED, new Date());

		URL home = null;
		try {
			home = handler.saveKnowledge(title);
		}
		catch (IOException e) {
			context.sendError(410, e.getMessage());
			return;
		}

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

	private Collection<String> getArticlesOfKnowledgebaseLoadedWithWrongVersion(String web, String title) {
		PackageManager packageManager = Environment.getInstance().getPackageManager(web);
		Set<String> compiledPackages = packageManager.getCompiledPackages(title);
		Set<Section<?>> compiledSections = new HashSet<Section<?>>();
		for (String compiledPackage : compiledPackages) {
			compiledSections.addAll(packageManager.getSectionsOfPackage(compiledPackage));
		}
		Set<Article> articlesOfKnowledgebase = new HashSet<Article>();
		for (Section<?> compiledSection : compiledSections) {
			articlesOfKnowledgebase.add(compiledSection.getArticle());
		}
		List<String> wrongVersionTitles = new LinkedList<String>();
		for (Article article : articlesOfKnowledgebase) {
			String articleText = article.getRootSection().getText();
			String connectorVersionOfArticleText = Environment.getInstance().getWikiConnector().getVersion(
					article.getTitle(), -1);
			if (!articleText.equals(connectorVersionOfArticleText)) {
				wrongVersionTitles.add(article.getTitle());
			}
		}
		return wrongVersionTitles;
	}
}
