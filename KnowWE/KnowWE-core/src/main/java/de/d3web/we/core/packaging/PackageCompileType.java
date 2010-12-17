package de.d3web.we.core.packaging;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.KnowWETerm;
import de.d3web.we.kdom.objects.StringDefinition;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

public abstract class PackageCompileType extends StringDefinition implements PackageReference {

	public PackageCompileType() {
		this.subtreeHandler.clear();
		this.setTermScope(KnowWETerm.GLOBAL);
		this.sectionFinder = new AllTextSectionFinder();
		this.setIgnorePackageCompile(true);
		this.addSubtreeHandler(Priority.PRECOMPILE_LOW, new PackageCompileHandler());
	}

	@Override
	public String getTermName(Section<? extends KnowWETerm<String>> s) {
		return s.getTitle();
	}

	@Override
	public final List<String> getPackagesToReferTo(Section<? extends PackageReference> s) {
		return getPackagesToCompile(s);
	}

	/**
	 * Needs to return a List of package names. These are the packages compiled
	 * later...
	 * 
	 * @created 02.10.2010
	 * @param s should always be the Section calling this method.
	 * @return a List of package names.
	 */
	public abstract List<String> getPackagesToCompile(Section<? extends PackageReference> s);

	private static class PackageCompileHandler extends SubtreeHandler<PackageCompileType> {

		public PackageCompileHandler() {
			super(true);
		}

		@Override
		public boolean needsToCreate(KnowWEArticle article, Section<PackageCompileType> s) {
			return true;
		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<PackageCompileType> s) {

			KnowWEPackageManager packageMng = KnowWEEnvironment.getInstance().getPackageManager(
					article.getWeb());

			if (article.isFullParse() || !s.isReusedBy(article.getTitle())) {
				packageMng.registerPackageReference(article, s);
			}

			List<Section<?>> packageDefinitions = new LinkedList<Section<?>>();
			for (String referedPackages : s.get().getPackagesToCompile(s)) {
				if (referedPackages.equals(article.getTitle())) continue;
				List<Section<?>> tempPackageDefs = packageMng.getPackageDefinitions(referedPackages);
				for (Section<?> packageDef : tempPackageDefs) {
					if (!packageDef.getTitle().equals(article.getTitle())) {
						packageDefinitions.add(packageDef);
					}
				}
			}

			for (Section<?> packDef : packageDefinitions) {
				article.getReviseIterator().addRootSectionToRevise(packDef);
			}

			s.get().storeTermObject(article, s, s.get().getTermName(s));
			KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermDefinition(
					article, s);

			return null;
		}

		@Override
		public boolean needsToDestroy(KnowWEArticle article, Section<PackageCompileType> s) {
			return true;
		}

		@Override
		public void destroy(KnowWEArticle article, Section<PackageCompileType> s) {
			if (super.needsToDestroy(article, s)) {
				article.setFullParse(this.getClass());
			}
			KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermDefinition(
					article, s);
		}

	}

}


