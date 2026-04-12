package de.knowwe.rdfs.d3web;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.session.values.ChoiceID;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.DateValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.TextValue;
import de.d3web.core.session.values.Unknown;
import de.d3web.scoring.HeuristicRating;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.StatementSource;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

import static java.util.stream.Collectors.toList;

public class Rdf2GoSessionHandler implements StatementSource {
	private static final Logger LOGGER = LoggerFactory.getLogger(Rdf2GoSessionHandler.class);

	public static final String MC_SEPARATOR = ", ";
	private Map<Object, Resource> agentNodeCache = new HashMap<>();

	private Session session;

	private final String sessionId;
	protected final Rdf2GoCore core;
	private final boolean addProvExplanation;

	public Rdf2GoSessionHandler(Session session, Rdf2GoCore core, boolean addProvExplanation) {
		this.session = session;
		this.sessionId = session.getId();
		this.core = core;
		this.addProvExplanation = addProvExplanation;

		// we make sure prov is available as a namespace
		if (core.getNamespacesMap().get("prov") == null) {
			core.addNamespace("prov", "http://www.w3.org/ns/prov#");
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Rdf2GoSessionHandler that = (Rdf2GoSessionHandler) o;
		return sessionId.equals(that.sessionId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sessionId);
	}

	public void addSessionToRdf2GoCore() {
		addStatementsToRdf2GoCore(generateStatements());

		// free memory
		agentNodeCache = null;
		session = null;
	}

	@NotNull
	protected List<Statement> generateStatements() {
		ArrayList<Statement> statementsList = new ArrayList<>();
		IRI sessionIdIRI = Rdf2GoD3webUtils.getSessionIdIRI(core, session.getId());
		IRI sessionIRI = core.createLocalIRI(Session.class.getSimpleName());

		// lns:sessionId rdf:type lns:Session
		Rdf2GoUtils.addStatement(core, sessionIdIRI, RDF.TYPE, sessionIRI, statementsList);

//		Literal timeLiteral = core.createLiteral(Long.toString(session.getCreationDate().getTime()));
//		IRI hasCreationDateIRI = core.createLocalIRI("hasCreationDate");
//
//		// lns:sessionId lns:hasCreationDate "time milliseconds"
//		Rdf2GoUtils.addStatement(core, sessionIdIRI, hasCreationDateIRI, timeLiteral,
//				statementsList);

		IRI usesKnowledgeBaseIRI = core.createLocalIRI("usesKnowledgeBase");

		// lns:sessionId lns:usesKnowledgeBase lns:knowledgeBaseId
		String id = session.getKnowledgeBase().getId();
		if (id == null) id = "noId";
		Rdf2GoUtils.addStatement(core, sessionIdIRI, usesKnowledgeBaseIRI, id, statementsList);

		// add value facts already present in the session
		List<TerminologyObject> valuedObjects = new ArrayList<>(session.getBlackboard().getValuedObjects());
		valuedObjects.sort(Comparator.comparing(TerminologyObject::getName));
		for (TerminologyObject valuedObject : valuedObjects) {
			Fact valueFact = session.getBlackboard().getValueFact(valuedObject);
			generateFactStatements(session, valueFact, statementsList);
		}
		return statementsList;
	}

	protected void addStatementsToRdf2GoCore(List<Statement> statementsList) {
		// add statements to the core
		core.addStatements(this, statementsList);
	}

	public void removeSessionFromRdf2GoCore() {
		core.removeStatements(this);
	}

	protected void generateFactStatements(Session session, Fact fact, ArrayList<Statement> statements) {

		IRI factNode = getFactNode(session, fact);

		// lns:sessionID lns:hasFact blank node (Fact)
		IRI sessionIdIRI = Rdf2GoD3webUtils.getSessionIdIRI(core, session.getId());
		Rdf2GoUtils.addStatement(core, sessionIdIRI, Rdf2GoD3webUtils.getHasFactIRI(core), factNode, statements);

		// blank node (Fact) rdf:type lns:Fact
		Rdf2GoUtils.addStatement(core, factNode, RDF.TYPE, Rdf2GoD3webUtils.getFactIRI(core), statements);

		// blank node (Fact) lns:hasTerminologyObject lns:ObjectName
		Rdf2GoUtils.addStatement(core, factNode,
				Rdf2GoD3webUtils.getHasTerminologyObjectIRI(core), fact.getTerminologyObject().getName(), statements);

		Literal valueLiteral = valueToLiteral(core, fact.getValue());

		// blank node (Fact) lns:hasValue "value"
		Rdf2GoUtils.addStatement(core, factNode, Rdf2GoD3webUtils.getHasValueIRI(core), valueLiteral, statements);

		Literal formattedValueLiteral = valueToFormattedLiteral(core, fact.getTerminologyObject(), fact.getValue());
		if (formattedValueLiteral != null) {
			// blank node (Fact) lns:hasFormattedValue "value"
			Rdf2GoUtils.addStatement(core, factNode, Rdf2GoD3webUtils.getHasFormattedValueIRI(core), formattedValueLiteral, statements);
		}

		// add PROV statements
		if (addProvExplanation) {
			addProvStatements(session, fact, statements, factNode);
		}
	}

	private void addProvStatements(Session session, Fact fact, Collection<Statement> statements, IRI factNode) {
		Set<TerminologyObject> activeDerivationSources = fact.getPSMethod()
				.getActiveDerivationSources(fact.getTerminologyObject(), session);

		for (TerminologyObject activeDerivationSource : activeDerivationSources) {
			Fact valueFact = session.getBlackboard().getValueFact(activeDerivationSource);
			if (valueFact == null) continue;
			IRI sourceFactNode = getFactNode(session, valueFact);
			// blank node (Fact) prov:wasDerivedFrom lns:sourceFactNode
			Rdf2GoUtils.addStatement(core, factNode, getProvIRI("wasDerivedFrom"), sourceFactNode, statements);
		}

		// Resource (Fact) prov:wasAttributedTo blank node (Agent)
		Resource agentNode = getAgentNode(fact);
		Rdf2GoUtils.addStatement(core, factNode, getProvIRI("wasAttributedTo"), agentNode, statements);

		// blank node (Agent) rdf:type lns:PSMethod/Section...
		Rdf2GoUtils.addStatement(core, agentNode, RDF.TYPE, getAgentType(fact), statements);
	}

	private IRI getAgentType(Fact fact) {
		Object source = fact.getSource();
		if (source instanceof PSMethod) {
			return core.createLocalIRI(source.getClass().getSimpleName());
		}
		else if (source instanceof Section<?>) {
			return core.createLocalIRI(((Section) source).get().getName());
		}
		return core.createLocalIRI(source.toString());
	}

	private Resource getAgentNode(Fact fact) {
		Object source = fact.getSource();
		Resource agentNode = agentNodeCache.get(source);
		if (agentNode == null) {
			if (source instanceof Section<?>) {
				agentNode = core.createLocalIRI(((Section) source).getID());
			}
			else {
				agentNode = core.createBlankNode();
			}
			agentNodeCache.put(source, agentNode);
		}
		return agentNode;
	}

	private IRI getProvIRI(String name) {
		return core.createIRI("prov", name);
	}

	private IRI getFactNode(Session session, Fact fact) {
		return core.createLocalIRI(session.getId() + "_" + fact.getTerminologyObject().getName());
	}

	protected Literal valueToFormattedLiteral(Rdf2GoCore core, TerminologyObject terminologyObject, Value value) {
		if (value instanceof ChoiceValue choiceValue && terminologyObject instanceof QuestionChoice questionChoice) {
			Choice choice = choiceValue.getChoice(questionChoice);
			if (choice == null) return null;
			String prompt = choice.getInfoStore().getValue(MMInfo.PROMPT);
			if (prompt == null) prompt = choice.getName();
			return core.createDatatypeLiteral(prompt, XSD.STRING);
		}
		if (value instanceof NumValue numValue && terminologyObject instanceof QuestionNum questionNum) {
			int digits = BasicProperties.getDigits(questionNum);
			double doubleValue = numValue.getDouble();
			if (digits == Integer.MIN_VALUE) return null;
			DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
			decimalFormat.applyPattern((digits == 0) ? "0"
					: "0." + Strings.nTimes((digits < 0 ? "#" : "0"), Math.min(20, Math.abs(digits))));
			return core.createDatatypeLiteral(decimalFormat.format(doubleValue), XSD.STRING);
		}
		else if (value instanceof Unknown) {
			return core.createDatatypeLiteral(Unknown.getInstance().getValue().toString(), XSD.STRING);
		}
		return null;
	}

	private Literal valueToLiteral(Rdf2GoCore core, Value value) {
		if (value instanceof NumValue) {
			return Rdf2GoUtils.createDoubleLiteral(core, ((NumValue) value).getDouble());
		}
		else if (value instanceof DateValue) {
			return Rdf2GoUtils.createDateTimeLiteral(core, ((DateValue) value).getDate());
		}
		else if (value instanceof MultipleChoiceValue) {
			Collection<ChoiceID> choiceIDs = ((MultipleChoiceValue) value).getChoiceIDs();
			String[] strings = new String[choiceIDs.size()];
			int i = 0;
			for (ChoiceID choiceID : choiceIDs) {
				strings[i++] = choiceID.toString();
			}
			String parsableMCValue = Identifier.concatParsable(MC_SEPARATOR, strings);
			return core.createDatatypeLiteral(parsableMCValue, XSD.STRING);
		}
		else if (value instanceof HeuristicRating) {
			return Rdf2GoUtils.createDoubleLiteral(core, ((HeuristicRating) value).getScore());
		}
		else if (value instanceof Unknown) {
			return core.createDatatypeLiteral(Unknown.getInstance().getValue().toString(), XSD.STRING);
		}
		return core.createDatatypeLiteral(value.toString(), XSD.STRING);
	}

	public static Value literalToValue(Question question, Literal literal) {
		String valueString = literal.getLabel();
		if (valueString.equals(Unknown.getInstance().getValue().toString())) {
			return Unknown.getInstance();
		}
		if (question instanceof QuestionNum) {
			return new NumValue(Double.parseDouble(valueString));
		}
		else if (question instanceof QuestionOC) {
			return KnowledgeBaseUtils.findValue(question, valueString);
		}
		else if (question instanceof QuestionMC) {
			String[] valueStrings = Identifier.parseConcat(MC_SEPARATOR, valueString);
			List<ChoiceID> collect = Stream.of(valueStrings).map(ChoiceID::new).collect(toList());
			return new MultipleChoiceValue(collect);
		}
		else if (question instanceof QuestionText) {
			return new TextValue(valueString);
		}
		else if (question instanceof QuestionDate) {
			try {
				return new DateValue(Rdf2GoUtils.createDateFromDateTimeLiteral(literal));
			}
			catch (ParseException e) {
				// should not happen!
				LOGGER.error("Unable to parse date from XSD:dateTime literal '" + valueString + "'");
				return Unknown.getInstance();
			}
		}
		throw new IllegalArgumentException("Question type '" + question.getClass().getName() + "' no supported");
	}

	public String getSessionId() {
		return this.sessionId;
	}
}
