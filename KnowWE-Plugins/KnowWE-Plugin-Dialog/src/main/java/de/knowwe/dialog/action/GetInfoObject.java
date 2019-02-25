/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.AnswerNo;
import de.d3web.core.knowledge.terminology.AnswerYes;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.QuestionZC;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.knowledge.terminology.info.abnormality.DefaultAbnormality;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.ChoiceID;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.DateValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.TextValue;
import de.d3web.core.session.values.Unknown;
import de.d3web.we.basic.SessionProvider;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.dialog.SessionConstants;
import de.knowwe.dialog.Utils;

/**
 * A command that delivers all info objects for a given list of object identifiers.
 * <p>
 * The ids in the list are separated by a comma. The info objects are delivered in the specified language or any
 * language if no entries are found for that language.
 *
 * @author Volker Belli
 */
public class GetInfoObject extends AbstractAction {

	public static final String PARAM_IDS = "ids";
	public static final String PARAM_LOCALE = "lang";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String idsString = context.getParameter(PARAM_IDS);
		Locale locale = Utils.parseLocale(context.getParameter(PARAM_LOCALE));

		KnowledgeBase base = (KnowledgeBase) context.getSession().getAttribute(
				SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE);
		Session session = SessionProvider.getSession(context, base);
		Writer writer = context.getWriter();
		writer.append("<kbinfo language='").append(locale.toString()).append("'>");
		String[] ids = idsString.split(",");
		for (String objectID : ids) {
			appendInfoObject(base, session, objectID, locale, writer);
		}
		writer.append("\n</kbinfo>");
	}

	public void appendInfoObject(KnowledgeBase base, Session session, String objectID, Locale locale, Writer writer) throws IOException {
		NamedObject object = base.getManager().search(objectID);

		if (object instanceof Solution) {
			appendInfoObject((Solution) object, session, locale, writer);
		}
		else if (object instanceof Question) {
			appendInfoObject((Question) object, session, locale, writer);
		}
		else if (object instanceof QContainer) {
			appendInfoObject((QContainer) object, session, locale, writer);
		}
		else {
			writer.append("<unknown id='").append(objectID).append("'></unknown>");
		}
	}

	public void appendInfoObject(Solution object, Session session, Locale locale, Writer writer) throws IOException {
		appendInfoObject(object, session, locale, null, writer);
	}

	public void appendInfoObject(Solution object, Session session, Locale locale, String additionalDescription, Writer writer) throws IOException {
		writer.append("\t<solution");
		writer.append(" id='").append(encodeXML(object.getName())).append("'");
		writer.append(" name='").append(encodeXML(object.getName())).append("'");
		writer.append(">\n");
		appendDCEntries(object, locale, additionalDescription, writer);
		appendChilds(object.getChildren(), writer);
		writer.append("\t</solution>\n");
	}

	public void appendInfoObject(Question object, Session session, Locale locale, Writer writer) throws IOException {
		writer.append("\t<question");
		writer.append(" id='").append(encodeXML(object.getName())).append("'");
		writer.append(" name='").append(encodeXML(object.getName())).append("'");
		if (BasicProperties.isAbstract(object)) {
			writer.append(" abstract='true'");
		}
		writer.append(" type='");
		writer.append((object instanceof QuestionYN) ? "bool"
				: (object instanceof QuestionOC) ? "oc"
				: (object instanceof QuestionMC) ? "mc"
				: (object instanceof QuestionDate) ? "date"
				: (object instanceof QuestionNum) ? "num"
				: (object instanceof QuestionText) ? "text"
				: "???"
		);
		writer.append("'");
		writer.append(">\n");

		appendDCEntries(object, locale, writer);

		if (session != null) {
			Value answer = session.getBlackboard().getValue(object);
			Collection<String> answerStrings = Collections.emptyList();
			if (answer instanceof ChoiceValue) {
				answerStrings = Collections.singleton(((ChoiceValue) answer).getAnswerChoiceID());
			}
			else if (answer instanceof MultipleChoiceValue) {
				answerStrings = new ArrayList<>();
				for (ChoiceID choiceID : ((MultipleChoiceValue) answer).getChoiceIDs()) {
					answerStrings.add(choiceID.getText());
				}
			}
			else if (answer instanceof NumValue) {
				answerStrings = Collections.singleton(answer.getValue().toString());
			}
			else if (answer instanceof TextValue) {
				answerStrings = Collections.singleton(encodeXML(answer.getValue().toString()));
			}
			else if (answer instanceof DateValue) {
				DateValue dvalue = (DateValue) answer.getValue();
				Date date = (Date) dvalue.getValue();
				answerStrings = Collections.singleton(String.valueOf(date.getTime()));
			}

			// add value, but at least add an empty value
			if (answerStrings.isEmpty()) {
				answerStrings = Collections.singleton("");
			}
			for (String answerString : answerStrings) {
				writer.append("\t\t<value>").append(encodeXML(answerString)).append("</value>");
			}
		}
		appendChilds(object.getChildren(), writer);
		if (object instanceof QuestionChoice) {
			DefaultAbnormality abnormality = object.getInfoStore().getValue(
					BasicProperties.DEFAULT_ABNORMALITY);
			for (Choice answer : ((QuestionChoice) object).getAllAlternatives()) {
				String prompt = getPrompt(answer, locale);
				String link = getLink(answer, locale);
				boolean isSelected = isSelected((QuestionChoice) object, answer, session);
				writer.append("\t\t<choice id='").append(encodeXML(answer.getName())).append("'");
				if (link != null && !link.isEmpty()) {
					writer.append(" link='").append(encodeXML(link)).append("'");
				}
				if (abnormality != null) {
					writer.append(" abnormality='")
							.append(String.valueOf(abnormality.getValue(new ChoiceValue(answer))))
							.append("'");
				}
				writer.append(" selected='").append(String.valueOf(isSelected)).append("'>");
				writer.append(encodeXML(prompt));
				writer.append("</choice>\n");
			}
		}
		else if (object instanceof QuestionNum) {
			NumericalInterval intervall = object.getInfoStore().getValue(
					BasicProperties.QUESTION_NUM_RANGE);
			if (intervall != null) {
				writer.append("<min>").append(String.valueOf(intervall.getLeft())).append(
						"</min>\n");
				writer.append("<max>").append(String.valueOf(intervall.getRight())).append(
						"</max>\n");
			}
			// object.getProperties().getProperty(Property.INT_NUMBER_REQUIRED);
			String unit = object.getInfoStore().getValue(MMInfo.UNIT);
			if (unit != null) {
				writer.append("<unit>").append(encodeXML(unit)).append("</unit>\n");
			}
		}
		// append choice unknown if visible
		if (BasicProperties.isUnknownVisible(object) || (object instanceof QuestionZC)) {
			Unknown value = Unknown.getInstance();
			String text = (object instanceof QuestionZC) ? "Next" : MMInfo.getUnknownPrompt(object);
			writer.append("\t\t<choice id='").append(value.getId()).append("'>");
			writer.append(encodeXML(text));
			writer.append("</choice>\n");
		}
		writer.append("\t</question>\n");
	}

	private boolean isSelected(QuestionChoice question, Choice answer, Session session) {
		if (session != null) {
			Value sessionValue = session.getBlackboard().getValue(question);
			if (sessionValue instanceof ChoiceValue && question instanceof QuestionOC) {
				ChoiceValue choiceValue = (ChoiceValue) sessionValue;
				return choiceValue.getChoiceID().equals(new ChoiceID(answer));
			}
			if (sessionValue instanceof MultipleChoiceValue && question instanceof QuestionMC) {
				MultipleChoiceValue multipleChoiceValue = (MultipleChoiceValue) sessionValue;
				return multipleChoiceValue.contains(answer);
			}
		}
		return false;
	}

	private String getPrompt(Choice answer, Locale locale) {
		// use prompt first if available
		String prompt = getMMInfoSubject(answer, MMInfo.PROMPT, locale);
		if (prompt != null) {
			return prompt;
		}

		// use ja/nein for German language
		if (locale != null && locale.getLanguage().equals("de")) {
			if (answer instanceof AnswerYes) {
				return "Ja";
			}
			if (answer instanceof AnswerNo) {
				return "Nein";
			}
		}

		// else use name
		return answer.getName();
	}

	public void appendInfoObject(QContainer object, Session session, Locale locale, Writer writer) throws IOException {
		writer.append("\t<qset");
		writer.append(" id='").append(encodeXML(object.getName())).append("'");
		writer.append(" name='").append(encodeXML(object.getName())).append("'");
		writer.append(">\n");
		appendDCEntries(object, locale, writer);
		appendChilds(object.getChildren(), writer);
		writer.append("\t</qset>\n");
	}

	private void appendDCEntries(TerminologyObject object, Locale locale, Writer writer) throws IOException {
		appendDCEntries(object, locale, null, writer);
	}

	private void appendDCEntries(TerminologyObject object, Locale locale, String additionalDescription, Writer writer) throws IOException {
		// append text to be prompted
		String prompt = getMMInfoSubject(object, MMInfo.PROMPT, locale);
		if (prompt != null) {
			writer.append("\t\t<text>").append(encodeXML(prompt)).append("</text>\n");
		}
		// append description
		String description = getMMInfoSubject(object, MMInfo.DESCRIPTION, locale);
		String fullDescription;
		if (Strings.isBlank(description)) {
			fullDescription = additionalDescription;
		}
		else if (Strings.isBlank(additionalDescription)) {
			fullDescription = description;
		}
		else {
			fullDescription = description + "<p>" + additionalDescription;
		}
		if (!Strings.isBlank(fullDescription)) {
			writer.append("\t\t<description>").append(encodeXML(fullDescription))
					.append("</description>\n");
		}
		// append clickable images
		try {
			Class<?> imageMapUtilsClass = Class.forName("de.knowwe.imagemap.ImageMapUtils");
			Method appendClickableImages = imageMapUtilsClass.getDeclaredMethod("appendClickableImages", TerminologyObject.class, Writer.class);
			appendClickableImages.invoke(null, object, writer);
		}
		catch (ClassNotFoundException ignore) {
		}
		catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			Log.warning("Unable to load and apply image map properties to dialog, probably incompatible plugins.");
		}

		// append multimedia information
		String link = getMMInfoSubject(object, MMInfo.LINK, locale);
		if (link != null) {
			if (MMInfo.isResourceLink(link)) {
				writer.append("\t\t<multimedia>").append(encodeXML(link)).append(
						"</multimedia>\n");
			}
			else {
				writer.append("\t\t<link>").append(encodeXML(link)).append("</link>\n");
			}
		}
	}

	private void appendChilds(TerminologyObject[] childs, Writer writer) throws IOException {
		for (TerminologyObject child : childs) {
			writer.append("\t\t<child>");
			writer.append(encodeXML(child.getName()));
			writer.append("</child>\n");
		}
	}

	private String getMMInfoSubject(NamedObject object, Property<String> subject, Locale locale) {
		InfoStore infoStore = object.getInfoStore();
		String s = locale != null
				? infoStore.getValue(subject, locale)
				: infoStore.getValue(subject);
		return s;
	}

	private String getLink(Choice answer, Locale locale) {
		String descr = answer.getInfoStore().getValue(MMInfo.LINK);
		return descr;
	}

	private static String encodeXML(String text) {
		return Utils.encodeXML(text);
	}
}
