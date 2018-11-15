package de.knowwe.ontology.turtle;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.jetbrains.annotations.NotNull;

import com.denkbares.semanticcore.CachedTupleQueryResult;
import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.provider.NodeProvider;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 14.11.18.
 * <p>
 * Implements a '?' as an object term in a turtle sentence or a ontology turtle table, which serves as a query and shows
 * the existing values from the ontology for the given subject and predicate concepts connected with this object term.
 * For literals, you an specify a language and the values are filtered for this language, if possible.
 */
public class ShowOtherExistingValuesWildCard extends AbstractType implements NodeProvider<ShowOtherExistingValuesWildCard> {

	public static final String SYMBOL = "\\?(@\\w\\w)?";

	public ShowOtherExistingValuesWildCard() {
		this.setSectionFinder((text, father, type) -> {
			if (text.trim().matches(SYMBOL)) {
				return new AllTextFinderTrimmed().lookForSections(text, father, type);
			}
			return null;
		});
		this.setRenderer(new ShowExistingValuesWildCardRenderer());
	}

	@Override
	public Value getNode(Section<? extends ShowOtherExistingValuesWildCard> section, Rdf2GoCompiler core) {
		return core.getRdf2GoCore().createIRI(RDF.NIL.stringValue());
	}

	private static Locale getLocaleFilter(Section<?> section) {
		Pattern p = Pattern.compile(SYMBOL);
		Matcher m = p.matcher(section.getText());
		if (m.find()) {
			String languageTag = m.group(1);
			if (languageTag == null) {
				return null;
			}
			return Locale.forLanguageTag(languageTag);
		}
		return null;
	}

	private static class ShowExistingValuesWildCardRenderer implements Renderer {
		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
			if (!(section.get() instanceof ShowOtherExistingValuesWildCard)) {
				throw new IllegalArgumentException("Renderer only for " + ShowOtherExistingValuesWildCard.class.getSimpleName() + " but not for " + ((Type) section
						.get()).getClass().getSimpleName());
			}

			OntologyCompiler compiler = Compilers.getCompiler(section, OntologyCompiler.class);
			if (compiler == null) return;
			Rdf2GoCore core = compiler.getRdf2GoCore();

			TerminologyManager terminologyManager = compiler.getTerminologyManager();

			Section<Object> objectSection = Sections.ancestor(section, Object.class);

			assert objectSection != null;
			Section<Subject> subjectSection = objectSection.get().findSubjectSection(objectSection);
			Resource subjectResource = subjectSection.get().getResource(subjectSection, compiler);
			Section<Predicate> predicateSection = objectSection.get().getPredicateSection(objectSection);
			IRI predicate = predicateSection.get().getIRI(predicateSection, compiler);
			String var = "var";
			String query = "SELECT * WHERE { <" + subjectResource + "> <" + predicate + "> ?" + var + " . }";
			CachedTupleQueryResult queryResult = core.sparqlSelect(query);
			Collection<Value> values = new HashSet<>();
			for (BindingSet binding : queryResult) {
				values.add(binding.getValue(var));
			}

			Collection<Value> valuesToShow = filterValuesToShow(values, objectSection, compiler);
			if (valuesToShow.isEmpty()) {
				StyleRenderer.CONTENT.renderText("-", user, result);
				return;
			}

			Set<Locale> locales = getAllLocalesWithValues(valuesToShow);
			Locale localeFilter = getLocaleFilter(section);
			StyleRenderer.CONTENT.renderText("(", user, result);
			boolean first = true;
			for (Value value : valuesToShow) {
				if(!first) {
					StyleRenderer.CONTENT.renderText(", " , user, result);
				}
				if (value instanceof Literal) {
					// handle literal values
					String valueString = value.toString();
					String currentLanguageTag = getLanguageTag(valueString);
					if (currentLanguageTag != null && localeFilter != null) {
						if (locales.contains(localeFilter)) {
							// we have something with the correct language
							if (Locale.forLanguageTag(currentLanguageTag).equals(localeFilter)) {
								String valueTextToShow = valueString.substring(0, valueString.length() - 3);
								StyleRenderer.CONTENT.renderText(valueTextToShow, user, result);
							}
						}
						else {
							// we do not have the correct language, hence we add everything
							StyleRenderer.CONTENT.renderText(valueString, user, result);
						}
					}
					else {
						// we do not have a language tag on the literal, hence we add everything
						StyleRenderer.CONTENT.renderText(valueString, user, result);
					}
				}
				else {
					// handle non-literal values
					Identifier identifier = core.toIdentifier(core.createIRI(value.toString()));
					String valueTextToShow = identifier
							.toExternalForm()
							.replace("#", ":");
					if (terminologyManager != null) {
						Section<? extends Type> termDefiningSection = terminologyManager.getTermDefiningSection(identifier);
						termDefiningSection.get().getRenderer().render(termDefiningSection, user, result);
						/*
						ToolMenuDecoratingRenderer.renderToolMenuDecorator(valueTextToShow, termDefiningSection.getID(), ToolUtils
								.hasToolInstances(termDefiningSection, user), result);
								*/
					}
				}
				first = false;
			}
				StyleRenderer.CONTENT.renderText(")", user, result);
		}

		@NotNull
		public Set<Locale> getAllLocalesWithValues(Collection<Value> valuesToShow) {
			Set<Locale> locales = new HashSet<>();
			for (Value value : valuesToShow) {
				String valueString = value.toString();
				if (getLanguageTag(valueString) != null) {
					locales.add(Locale.forLanguageTag(Objects.requireNonNull(getLanguageTag(valueString))));
				}
			}
			return locales;
		}

		private static String getLanguageTag(String literal) {
			if (literal.length() > 3 && literal.charAt(literal.length() - 3) == '@') {
				return literal.substring(literal.length() - 2, literal.length());
			}
			return null;
		}

		private Collection<Value> filterValuesToShow(Collection<Value> values, Section<Object> objectSection, OntologyCompiler core) {
			Section<ObjectList> objectListSection = Sections.ancestor(objectSection, ObjectList.class);
			Collection<Value> result = new HashSet<>(values);
			List<Section<NodeProvider>> otherValueSections = Sections.successors(objectListSection, NodeProvider.class);
			otherValueSections.stream().map(section -> section.get().getNode(section, core)).forEach(result::remove);
			return result;
		}
	}
}
