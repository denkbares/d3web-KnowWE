package de.knowwe.ontology.turtlePimped;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.terminology.TermRegistrationScope;
import de.knowwe.core.kdom.objects.SimpleDefinition;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.ontology.kdom.resource.AbbreviatedResourceReference;
import de.knowwe.ontology.kdom.resource.Resource;
import de.knowwe.ontology.kdom.resource.ResourceReference;


public class RDFTypeType extends SimpleDefinition {

	public RDFTypeType() {
		super(TermRegistrationScope.LOCAL, Resource.class);
		this.setSectionFinder(new RegexSectionFinder("[\\w]*?:type"));
		this.clearChildrenTypes();
		this.addChildType(new TurtleURI());
	}

	@Override
	public String getTermName(Section<? extends Term> section) {
		Section<Subject> turtleSubject = getTurtleSubject(section);
		Section<ResourceReference> resourceSection = Sections.findSuccessor(turtleSubject,
				ResourceReference.class);
		String resource = resourceSection.get().getTermName(resourceSection);
		return resource;
	}

	@Override
	public Identifier getTermIdentifier(Section<? extends Term> section) {
		Section<Subject> turtleSubject = getTurtleSubject(section);
		Section<AbbreviatedResourceReference> abbResDef = Sections.findSuccessor(
				turtleSubject,
				AbbreviatedResourceReference.class);
		if (abbResDef != null) {

		String abbreviation = abbResDef.get().getAbbreviation(abbResDef);

		return new Identifier(abbreviation, getTermName(section));
		}
		return null;
	}

	public static Section<Subject> getTurtleSubject(Section<? extends Term> section) {
		if (section.get() instanceof RDFTypeType) {

		Section<TurtleSentence> sentence = Sections.findAncestorOfType(section,
				TurtleSentence.class);
		Section<Subject> subjectSection = Sections.findSuccessor(sentence, Subject.class);
		return subjectSection;
		}
		return null;
	}



}
