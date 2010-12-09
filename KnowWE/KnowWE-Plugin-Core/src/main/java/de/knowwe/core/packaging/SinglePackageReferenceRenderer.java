package de.knowwe.core.packaging;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.packaging.KnowWEPackageManager;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMError;
import de.d3web.we.kdom.report.KDOMWarning;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class SinglePackageReferenceRenderer extends KnowWEDomRenderer {

	@Override
	public void render(KnowWEArticle article,
			Section sec,
			KnowWEUserContext user,
			StringBuilder string) {

		String packageName = sec.getOriginalText();

		KnowWEPackageManager packageManager = KnowWEEnvironment.getInstance().getPackageManager(
				article.getWeb());

		List<Section<?>> packageDefinitions = packageManager.getPackageDefinitions(packageName);

		Collection<KDOMError> kdomErrors = new LinkedList<KDOMError>();
		Collection<KDOMWarning> kdomWarnings = new LinkedList<KDOMWarning>();

		for (Section<?> packageDef : packageDefinitions) {
			kdomErrors.addAll(KnowWEUtils.getMessagesFromSubtree(article,
					packageDef, KDOMError.class));
			kdomWarnings.addAll(KnowWEUtils.getMessagesFromSubtree(article,
					packageDef, KDOMWarning.class));
		}

		int errorsCount = kdomErrors.size();
		int warningsCount = kdomWarnings.size();
		String headerErrorsCount = errorsCount > 0 ? "Errors: " + errorsCount : "";
		String headerWarningsCount = warningsCount > 0 ? "Warnings: " + warningsCount : "";

		String errorsAndWarnings = errorsCount > 0 || warningsCount > 0 ? headerErrorsCount
				+ (errorsCount > 0 && warningsCount > 0 ? ", " : "") + headerWarningsCount : "";

		String sectionsCount = "Sections: " + packageDefinitions.size();

		String headerSuffix = KnowWEEnvironment.getInstance().getArticleManager(
				article.getWeb()).getTitles().contains(packageName)
				|| packageName.equals(KnowWEPackageManager.THIS)
				? (sec.getTitle().equals(packageName)
						|| packageName.equals(KnowWEPackageManager.THIS)
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
			for (KDOMError error : kdomErrors) {
				string.append(KnowWEUtils.maskHTML(error.getVerbalization() + "<br/>\n"));
			}
			string.append(KnowWEUtils.maskHTML("<p/>"));
		}
		if (warningsCount > 0) {
			string.append(KnowWEUtils.maskHTML("<strong>Warnings:</strong><p/>\n"));
			for (KDOMWarning warning : kdomWarnings) {
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