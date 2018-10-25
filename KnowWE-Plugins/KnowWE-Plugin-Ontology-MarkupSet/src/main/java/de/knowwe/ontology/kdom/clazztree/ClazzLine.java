package de.knowwe.ontology.kdom.clazztree;

import java.util.ArrayList;
import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.DashTreeUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.EmbracedContentFinder;
import de.knowwe.kdom.sectionFinder.UnquotedExpressionFinder;
import de.knowwe.ontology.compile.OntologyCompileScript;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.kdom.resource.Resource;
import de.knowwe.ontology.kdom.resource.ResourceDefinition;
import de.knowwe.ontology.turtle.FirstWordFinder;
import de.knowwe.ontology.turtle.TurtleURI;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 06.02.17.
 */
public class ClazzLine extends AbstractType {

	public Identifier getFullClassIdentifier(Section<ClazzLine> section, OntologyCompiler compiler) {
		Section<ResourceDefinition> resourceDefinitionSection = Sections.successor(section, ResourceDefinition.class);
		assert resourceDefinitionSection != null;
		return resourceDefinitionSection.get()
				.getTermIdentifier(compiler, resourceDefinitionSection);
	}

	@NotNull
	public static String getNamespace(Section<ClassHierarchyTreeMarkupType> markup) {
		String namespace = "lns";

		String namespaceAnnotation = DefaultMarkupType
				.getAnnotation(markup, ClassHierarchyTreeMarkupType.ANNOTATION_NAMESPACE);
		if (namespaceAnnotation != null) {
			namespace = namespaceAnnotation;
		}
		return namespace;
	}

	private static class ClassDefinition extends ResourceDefinition {

		public ClassDefinition() {
			super(Resource.class);
		}

		@Override
		public Identifier getTermIdentifier(TermCompiler compiler, Section<? extends Term> section) {
			Identifier identifier = (Identifier) section.getObject(IDENTIFIER_KEY);
			if (identifier == null) {
				Section<ClassHierarchyTreeMarkupType> markup = Sections.ancestor(section, ClassHierarchyTreeMarkupType.class);
				identifier = new Identifier(getNamespace(markup), getTermName(section));
				section.storeObject(IDENTIFIER_KEY, identifier);
			}
			return identifier;
		}
	}

	public ClazzLine() {
		this.setSectionFinder(new AllTextFinderTrimmed());

		AbstractType classIdentifier = new ClassDefinition();
		classIdentifier.setSectionFinder(new FirstWordFinder());
		this.addChildType(classIdentifier);

		this.addChildType(new ClassDescriptionEmbraced());

		this.addChildType(new ClazzLabel(new AllTextFinderTrimmed()));

		this.addCompileScript(new OntologyCompileScript<ClazzLine>() {

			@Override
			public void destroy(OntologyCompiler compiler, Section<ClazzLine> section) {

			}

			@Override
			public void compile(OntologyCompiler compiler, Section<ClazzLine> section) throws CompilerMessage {

				Section<ClazzLabel> labelSection = Sections.successor(section, ClazzLabel.class);
				Section<ClazzDescription> descriptionSection = Sections.successor(section, ClazzDescription.class);

				Section<ClassHierarchyTreeMarkupType> markup = Sections.ancestor(section, ClassHierarchyTreeMarkupType.class);

				String subClassAnnotation = DefaultMarkupType
						.getAnnotation(markup, ClassHierarchyTreeMarkupType.ANNOTATION_RELATION);

				Identifier subClassRelation = new Identifier("rdfs", "subClassOf");
				if (subClassAnnotation != null) {
					subClassRelation = new Identifier(subClassAnnotation.split(":"));
				}

				Identifier classIdentifier = getFullClassIdentifier(section, compiler);
				if (classIdentifier != null) {
					Rdf2GoCore core = compiler.getRdf2GoCore();

					URI clazzNode = TurtleURI.getNodeForIdentifier(core, classIdentifier);

					Collection<Statement> statements = new ArrayList<>();

					statements.add(core.createStatement(clazzNode, core.createIRI(Rdf2GoUtils.expandNamespace(core, "rdf:type")), core
							.createIRI(Rdf2GoUtils.expandNamespace(core, "owl:Class"))));

					// add label
					if (labelSection != null) {
						statements.add(core.createStatement(clazzNode, core.createIRI("http://www.w3.org/2004/02/skos/core#prefLabel"), core
								.createLiteral(Strings.unquote(labelSection.getText()))));
					}

					// add description
					if (descriptionSection != null) {
						statements.add(core.createStatement(clazzNode, core.createIRI(Rdf2GoUtils.expandNamespace(core, "rdfs:comment")), core
								.createLiteral(Strings
										.unquote(descriptionSection.getText()))));
					}

					// add relation rdfs:subClassOf to parent dash tree element
					Section<? extends DashTreeElement> parentDashTreeElement = DashTreeUtils.getParentDashTreeElement(Sections
							.ancestor(section, DashTreeElement.class));
					if (parentDashTreeElement != null) {
						Section<ClazzLine> parentClassLine = Sections.successor(parentDashTreeElement, ClazzLine.class);
						assert parentClassLine != null;
						Identifier parentIdentifier = parentClassLine.get()
								.getFullClassIdentifier(parentClassLine, compiler);
						statements.add(core.createStatement(clazzNode, TurtleURI.getNodeForIdentifier(core, subClassRelation), TurtleURI
								.getNodeForIdentifier(core, parentIdentifier)));

					}
					core.addStatements(statements);
				}
			}
		});

	}

	public static class ClazzLabel extends AbstractType {
		public ClazzLabel(SectionFinder sectionFinder) {
			super(sectionFinder);
		}
	}

	public static class ClassDescriptionEmbraced extends AbstractType {

		private static final char DESCRIPTION_OPEN = '{';
		private static final char DESCRIPTION_CLOSE = '}';

		public ClassDescriptionEmbraced() {
			this.setSectionFinder(new EmbracedContentFinder(DESCRIPTION_OPEN, DESCRIPTION_CLOSE));

			// TODO find better way to crop open and closing signs
			AnonymousType open = new AnonymousType(Character.toString(DESCRIPTION_OPEN));
			open.setRenderer(StyleRenderer.PROMPT);
			open.setSectionFinder(new UnquotedExpressionFinder(
					Character.toString(DESCRIPTION_OPEN)));
			this.addChildType(open);

			AnonymousType close = new AnonymousType(Character.toString(DESCRIPTION_CLOSE));
			close.setSectionFinder(new UnquotedExpressionFinder(
					Character.toString(DESCRIPTION_CLOSE)));
			close.setRenderer(StyleRenderer.PROMPT);
			this.addChildType(close);

			// the rest is definitions of answers
			this.addChildType(new ClazzDescription());
		}
	}

	public static class ClazzDescription extends AbstractType {
		public ClazzDescription() {
			super(new AllTextFinderTrimmed());
			this.setRenderer(StyleRenderer.COMMENT);
		}
	}

}
