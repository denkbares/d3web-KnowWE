package de.knowwe.core.compile.packaging;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.knowwe.core.Environment;
import de.knowwe.core.compile.ConstraintModule;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TermIdentifier;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;

public abstract class PackageCompileType extends AbstractType implements PackageCompiler {

	public PackageCompileType() {
		this.setSectionFinder(new AllTextSectionFinder());
		this.setIgnorePackageCompile(true);
		this.addSubtreeHandler(Priority.PRECOMPILE_LOW, new PackageCompileHandler());
	}

	private static class PackageCompileHandler extends SubtreeHandler<PackageCompileType> {

		public PackageCompileHandler() {
			super(true);
			this.unregisterAllConstraintModules();
			this.registerConstraintModule(0, new CompileHandlerConstraint());
		}

		@Override
		public Collection<Message> create(Article article, Section<PackageCompileType> s) {

			PackageManager packageMng = Environment.getInstance().getPackageManager(
					article.getWeb());

			if (article.isFullParse() || !s.isReusedBy(article.getTitle())) {
				packageMng.registerPackageCompileSection(article, s);
			}

			List<Section<?>> sectionsOfPackage = new LinkedList<Section<?>>();
			for (String packagesToCompile : s.get().getPackagesToCompile(s)) {
				List<Section<?>> tempSectionsOfPackage = packageMng.getSectionsOfPackage(packagesToCompile);
				for (Section<?> sectionOfPackage : tempSectionsOfPackage) {
					if (!sectionOfPackage.getTitle().equals(article.getTitle())) {
						sectionsOfPackage.add(sectionOfPackage);
					}
				}
			}

			for (Section<?> sectionOfPackage : sectionsOfPackage) {
				article.getReviseIterator().addRootSection(sectionOfPackage);
			}

			KnowWEUtils.getGlobalTerminologyManager(article.getWeb()).registerTermDefinition(s,
					String.class, new TermIdentifier(s.getTitle()));

			return Messages.noMessage();
		}

		@Override
		public void destroy(Article article, Section<PackageCompileType> s) {
			KnowWEUtils.getTerminologyManager(article).unregisterTermDefinition(
					s, String.class, new TermIdentifier(s.getTitle()));
		}

		private class CompileHandlerConstraint extends ConstraintModule<PackageCompileType> {

			@Override
			public boolean violatedConstraints(Article article, Section<PackageCompileType> s) {
				return true;
			}

		}

	}

}
