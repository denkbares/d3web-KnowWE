package de.knowwe.ontology.kdom.table;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.table.Table;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableIndexConstraint;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.kdom.table.TableUtils;
import de.knowwe.ontology.compile.OntologyCompileScript;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.turtle.ObjectList;
import de.knowwe.ontology.turtle.Predicate;
import de.knowwe.ontology.turtle.Subject;
import de.knowwe.ontology.turtle.TurtleURI;
import de.knowwe.ontology.compile.provider.NodeProvider;
import de.knowwe.ontology.compile.provider.ResourceProvider;
import de.knowwe.ontology.compile.provider.URIProvider;
import de.knowwe.rdf2go.Rdf2GoCore;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 26.10.17.
 */
public class HierarchyTableMarkup extends DefaultMarkupType {

	private static DefaultMarkup MARKUP = null;

	public static final String ANNOTATION_TYPE_RELATION = "typeRelation";

	public static class HierarchyLevelType extends AbstractType {

		public HierarchyLevelType() {
			this.setSectionFinder(AllTextFinder.getInstance());
			this.addCompileScript(new HierarchyCompileScript());
		}

		static class HierarchyCompileScript extends OntologyCompileScript<HierarchyLevelType> {

			@Override
			public void compile(OntologyCompiler compiler, Section<HierarchyLevelType> section) throws CompilerMessage {
				Section<TableCellContent> hierarchyProperty = TableUtils.getCell(section, 0, 0);
				Section<URIProvider> propertyNodeProvider = Sections.successor(hierarchyProperty, URIProvider.class);

				int row = TableUtils.getRow(Sections.ancestor(section, TableLine.class));
				if (row == 1) {
					// no need to do something for first line
					return;
				}
				Section<TableCellContent> localConceptCell = TableUtils.getCell(section, 1, row);
				Section<ResourceProvider> subjectNodeProvider = Sections.successor(localConceptCell, ResourceProvider.class);

				Integer hierarchyLevel = getHierarchyLevel(section);
				if (hierarchyLevel == null) {
					Messages.storeMessage(compiler, section, this.getClass(), Messages.error("No a valid hierarchy level (use integer or dashes to indicate level)"));
				}
				else {
					Section<?> hierarchyLevelAbove = HierarchyTableUtils.findHierarchyLevelAbove(hierarchyLevel - 1, section);
					if (hierarchyLevelAbove == null) {
						Messages.storeMessage(compiler, section, this.getClass(), Messages.error("No a valid preceeding hierarchy level found (use integer or dashes to indicate level)"));
					}
					else {

						Section<TableLine> line = Sections.ancestor(hierarchyLevelAbove, TableLine.class);
						Section<TableCellContent> parentConcept = Sections.successors(line, TableCellContent.class)
								.get(1);
						Section<NodeProvider> parentNodeProvider = Sections.successor(parentConcept, NodeProvider.class);
						Rdf2GoCore core = compiler.getRdf2GoCore();
						assert subjectNodeProvider != null;
						Resource subjectURI = subjectNodeProvider.get().getResource(subjectNodeProvider, compiler);
						assert propertyNodeProvider != null;
						URI predicateNode = propertyNodeProvider.get().getIRI(propertyNodeProvider, compiler);
						assert parentNodeProvider != null;
						Value parentNode = parentNodeProvider.get().getNode(parentConcept, compiler);
						core.addStatements(section, core.createStatement(subjectURI, predicateNode, parentNode));
					}
				}
			}

			@Override
			public void destroy(OntologyCompiler compiler, Section<HierarchyLevelType> section) {
				Rdf2GoCore core = compiler.getRdf2GoCore();
				core.removeStatements(section);
			}
		}

		public static Integer getHierarchyLevel(Section<HierarchyLevelType> section) {
			String text = section.getText().trim();
			if (text.isEmpty()) {
				return 0;
			}
			if (text.matches("-*")) {
				return text.length();
			}
			try {
				return Integer.parseInt(text);
			}
			catch (NumberFormatException e) {
				return null;
			}
		}
	}

	static {
		MARKUP = new DefaultMarkup("HierarchyTable");
		Table content = new Table();
		MARKUP.addContentType(content);
		PackageManager.addPackageAnnotation(MARKUP);

		MARKUP.addAnnotation(ANNOTATION_TYPE_RELATION, false);
		MARKUP.addAnnotationContentType(ANNOTATION_TYPE_RELATION, new TurtleURI());


		/*
		Cell 0,0 (Class URI for type definition)
		 */
		OntologyTableMarkup.BasicURIType cell00 = new OntologyTableMarkup.BasicURIType();
		cell00.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				new TableIndexConstraint(0, 1, 0, 1)));
		content.injectTableCellContentChildtype(cell00);

		/*
		First column: cells 0, 1-n
		 */
		HierarchyLevelType hierarchyLevelType = new HierarchyLevelType();
		hierarchyLevelType.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				new TableIndexConstraint(0, 1, 1, Integer.MAX_VALUE)));
		content.injectTableCellContentChildtype(hierarchyLevelType);


		/*
		Cell 1,0 (Class URI for type definition)
		 */
		OntologyTableMarkup.BasicURIType cell10 = new OntologyTableMarkup.BasicURIType();
		cell10.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				new TableIndexConstraint(1, 2, 0, 1)));
		content.injectTableCellContentChildtype(cell10);

		/*
		Second column: cells 1, 2-n
		 */
		Subject resource = new Subject(new TableSubjectURIWithDefinition());
		resource.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				new TableIndexConstraint(1, 2, 1, Integer.MAX_VALUE)));
		content.injectTableCellContentChildtype(resource);

		/*
		Header Row: cells 2-n, 0
		 */
		Predicate property = new Predicate();
		property.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				new TableIndexConstraint(2, Integer.MAX_VALUE, 0, 1)));
		content.injectTableCellContentChildtype(property);

		/*
		Inner cell entries: cells 2-n,1-n
		 */
		ObjectList object = new ObjectList(new OntologyTableMarkup.OntologyTableTurtleObject());
		// add aux-type to enable drop-area-rendering
		OntologyTableCellEntry cellEntry = new OntologyTableCellEntry(object);
		cellEntry.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				new TableIndexConstraint(2, Integer.MAX_VALUE, 1, Integer.MAX_VALUE)));
		content.injectTableCellContentChildtype(cellEntry);

	}

	public HierarchyTableMarkup() {
		super(MARKUP);
	}

}
