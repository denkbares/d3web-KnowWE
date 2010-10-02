package de.d3web.we.core.packaging;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

public abstract class PackageCompileType extends DefaultAbstractKnowWEObjectType implements PackageReference {

	public PackageCompileType() {
		this.sectionFinder = new AllTextSectionFinder();
		this.addSubtreeHandler(Priority.PRECOMPILE_LOW, new PackageCompileHandler());
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

			return null;
		}

		@Override
		public void destroy(KnowWEArticle article, Section<PackageCompileType> s) {
			article.setFullParse(this);
		}

	}

}


