package de.knowwe.core.packaging;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

public class SinglePackageReferenceRenderer implements Renderer {

	@Override
	public void render(Section<?> sec,
			UserContext user,
			StringBuilder string) {

		String packageName = sec.getText();

		PackageManager packageManager = Environment.getInstance().getPackageManager(
				sec.getWeb());

		List<Section<?>> packageDefinitions = packageManager.getSectionsOfPackage(packageName);

		Collection<Message> kdomErrors = new LinkedList<Message>();
		Collection<Message> kdomWarnings = new LinkedList<Message>();
		Article article = KnowWEUtils.getCompilingArticles(sec).iterator().next();
		for (Section<?> packageDef : packageDefinitions) {
			Collection<Message> allmsgs = Messages.getMessagesFromSubtree(article, packageDef);

			kdomErrors.addAll(Messages.getErrors(Messages.getErrors(allmsgs)));
			kdomWarnings.addAll(Messages.getWarnings(Messages.getErrors(allmsgs)));
		}

		int errorsCount = kdomErrors.size();
		int warningsCount = kdomWarnings.size();
		String headerErrorsCount = errorsCount > 0 ? "Errors: " + errorsCount : "";
		String headerWarningsCount = warningsCount > 0 ? "Warnings: " + warningsCount : "";

		String errorsAndWarnings = errorsCount > 0 || warningsCount > 0 ? headerErrorsCount
				+ (errorsCount > 0 && warningsCount > 0 ? ", " : "") + headerWarningsCount : "";

		String sectionsCount = "Sections: " + packageDefinitions.size();

		String headerSuffix = Environment.getInstance().getArticleManager(
				article.getWeb()).getTitles().contains(packageName)
				|| packageName.equals(PackageManager.THIS)
				? (sec.getTitle().equals(packageName)
						|| packageName.equals(PackageManager.THIS)
						? " (Compiling this article)"
						: " (Names of articles besides the name of this one are disallowed)")
				: " ("
						+ sectionsCount
						+ (errorsAndWarnings.length() > 0 ? ", " : "") +
						errorsAndWarnings + ")";

		string.append("%%collapsebox-closed \n");
		string.append("! " + "Compiled package: " + packageName + headerSuffix + "\n");

		if (errorsCount > 0) {
			string.append(KnowWEUtils.maskHTML("<strong>Errors:</strong><p/>\n"));
			for (Message error : kdomErrors) {
				string.append(KnowWEUtils.maskHTML(error.getVerbalization() + "<br/>\n"));
			}
			string.append(KnowWEUtils.maskHTML("<p/>"));
		}
		if (warningsCount > 0) {
			string.append(KnowWEUtils.maskHTML("<strong>Warnings:</strong><p/>\n"));
			for (Message warning : kdomWarnings) {
				string.append(KnowWEUtils.maskHTML(warning.getVerbalization() + "<br/>\n"));
			}
			string.append(KnowWEUtils.maskHTML("<p/>"));
		}
		if (packageDefinitions.size() > 0) {
			string.append(KnowWEUtils.maskHTML("<strong>Compiled Sections:</strong><p/>\n"));
			for (Section<?> packDef : packageDefinitions) {
				string.append(KnowWEUtils.maskHTML(packDef.getTitle() + " - "
						+ packDef.get().getName() + "<br/>\n"));
				// TODO: Make links!
			}
		}
		string.append("/%\n");
	}

}