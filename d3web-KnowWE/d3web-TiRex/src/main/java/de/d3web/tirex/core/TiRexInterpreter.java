package de.d3web.tirex.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.tirex.core.extractionStrategies.ExtractionStrategy;

/**
 * This is the core class of TiRex. It provides the methods to extract
 * Questions, Answers and Diagnosis out of lines taken from a wiki page.
 * 
 * @author Dmitrij Frese
 * @date 02/2008
 */
public class TiRexInterpreter {

	public static final String TIREX_START_TAG = "<TiRex(?:[ ]solution[ ]*=[ ]*[\"]?([a-zA-Z0-9äööüÄÖÜß]+)[\"]?)?>";
	public static final String TIREX_END_TAG = "</TiRex>";

	/**
	 * The class is implemented as a Singleton as virtually every class in
	 * TiRex.
	 */
	private static TiRexInterpreter instance;

	private TiRexInterpreter() {
	}

	public static TiRexInterpreter getInstance() {
		if (instance == null) {
			instance = new TiRexInterpreter();
		}

		return instance;
	}

	/**
	 * @param wikiPage
	 *            The wiki page that is to be parsed.
	 * @return The part that contains the knowledge is returned, if the parse
	 *         was successful.
	 */
	public String getExtractedKnowledgeBlock(String wikiPage) {
		Collection<String> knowledgeBlocks = getExtractedKnowledgeBlocks(wikiPage);

		StringBuffer knowledgeBlock = new StringBuffer();
		for (String s : knowledgeBlocks) {
			knowledgeBlock.append(s + "\n");
		}

		return knowledgeBlock.toString();
	}

	/**
	 * @param wikiPage
	 *            The wiki page that is to be parsed.
	 * @return The parts that contain the knowledge are returned, if the parse
	 *         was successful. It is possible to recognize the "important" parts
	 *         of the wiki page by using a certain regular expression, which is
	 *         defined in the TiRexSettings ResouceBundle.
	 */
	public Collection<String> getExtractedKnowledgeBlocks(String wikiPage) {
		Pattern p = Pattern.compile(TiRexSettings.getInstance().TIREX_SETTINGS
				.get("regex.blockExtraction"));

		/*
		 * hotfix
		 */
		p = Pattern.compile(TIREX_START_TAG + "[\\w|\\W]*" + TIREX_END_TAG);

		Matcher m = p.matcher(wikiPage);

		Collection<String> knowledgeBlocks = new ArrayList<String>();

		while (m.find()) {
			knowledgeBlocks.add(m.group());
		}

		return knowledgeBlocks;
	}

	/**
	 * @param kb
	 *            The knowledgebase, which TiRex works with.
	 * @param knowledge
	 *            The line of knowledge, which all the ExtractionStrategies are
	 *            to be tested on.
	 * @param strategies
	 *            A Collection of different ExtractionStrategies, which are to
	 *            be applied to "knowledge".
	 * @return The extracted Diagnosis wrapped into an
	 *         "OriginalMatchAndStrategy" object along with other useful data.
	 */
	public OriginalMatchAndStrategy extractDiagnosis(KnowledgeBase kb,
			String knowledge, Collection<ExtractionStrategy> strategies) {
		for (ExtractionStrategy strategy : strategies) {
			for (Diagnosis d : kb.getDiagnoses()) {
				OriginalMatchAndStrategy match = null;

				match = strategy.extract(d, knowledge);
				if (match != null) {
					return match;
				}
			}
		}

		return null;
	}

	/**
	 * @param kb
	 *            The knowledgebase, which TiRex works with.
	 * @param knowledge
	 *            The line of knowledge, which all the ExtractionStrategies are
	 *            to be tested on.
	 * @param strategies
	 *            A Collection of different ExtractionStrategies, which are to
	 *            be applied to "knowledge".
	 * @return The extracted Question wrapped into an "OriginalMatchAndStrategy"
	 *         object along with other useful data.
	 */
	public OriginalMatchAndStrategy extractQuestion(KnowledgeBase kb,
			String knowledge, Collection<ExtractionStrategy> strategies) {

		for (Question q : kb.getQuestions()) {
			for (ExtractionStrategy strategy : strategies) {
				OriginalMatchAndStrategy match = null;

				match = strategy.extract(q, knowledge);
				if (match != null) {
					return match;
				}
			}
		}

		return null;
	}

	public Collection<OriginalMatchAndStrategy> extractQuestions(
			KnowledgeBase kb, String knowledge,
			Collection<ExtractionStrategy> strategies) {
		Collection<OriginalMatchAndStrategy> returnCollection = new ArrayList<OriginalMatchAndStrategy>();

		for (Question q : kb.getQuestions()) {
			for (ExtractionStrategy strategy : strategies) {
				OriginalMatchAndStrategy match = null;
				match = strategy.extract(q, knowledge);

				if (match != null) {
					returnCollection.add(match);
				}
			}
		}

		return returnCollection;
	}

	/**
	 * @param q
	 *            The Question, that we're trying to get the Answer for.
	 * @param knowledge
	 *            The line of knowledge, which all the ExtractionStrategies are
	 *            to be tested on.
	 * @param kb
	 *            The knowledgebase, which TiRex works with.
	 * @param strategies
	 *            A Collection of different ExtractionStrategies, which are to
	 *            be applied to "knowledge".
	 * @return The extracted Answer wrapped into an "OriginalMatchAndStrategy"
	 *         object along with other useful data.
	 */
	public OriginalMatchAndStrategy extractAnswer(Question q, String knowledge,
			KnowledgeBase kb, Collection<ExtractionStrategy> strategies) {

		Collection<AnswerChoice> answers = null;
		if (q != null) {
			answers = TiRexUtilities.getInstance().getAllPossibleAnswers(q);
		} else {
			answers = TiRexUtilities.getInstance().getAllPossibleAnswers(kb);
		}

		// If no predefined answer exists, we still want to test the
		// ExtractionStrategies on the line of knowledge once, since some of
		// them might not require a predefined answer. (Strategies working with
		// regular expressions are an example).
		if (answers.size() == 0) {
			OriginalMatchAndStrategy match = null;

			for (ExtractionStrategy strategy : strategies) {
				match = strategy.extract(null, knowledge);
				if (match != null) {
					return match;
				}
			}
		} else {
			for (ExtractionStrategy strategy : strategies) {
				for (AnswerChoice answer : answers) {
					OriginalMatchAndStrategy match = null;

					match = strategy.extract(answer, knowledge);
					if (match != null) {
						return match;
					}
				}
			}
		}

		return null;
	}

	public Collection<OriginalMatchAndStrategy> extractAnswers(Question q,
			String knowledge, KnowledgeBase kb,
			Collection<ExtractionStrategy> strategies) {
		Collection<OriginalMatchAndStrategy> returnList = new ArrayList<OriginalMatchAndStrategy>();

		Collection<AnswerChoice> answers = null;
		if (q != null) {
			answers = TiRexUtilities.getInstance().getAllPossibleAnswers(q);
		} else {
			answers = TiRexUtilities.getInstance().getAllPossibleAnswers(kb);
		}

		// If no predefined answer exists, we still want to test the
		// ExtractionStrategies on the line of knowledge once, since some of
		// them might not require a predefined answer. (Strategies working with
		// regular expressions are an example).
		if (answers.size() == 0) {
			OriginalMatchAndStrategy match = null;

			for (ExtractionStrategy strategy : strategies) {
				match = strategy.extract(null, knowledge);
				if (match != null) {
					returnList.add(match);
				}
			}
		} else {
			for (ExtractionStrategy strategy : strategies) {
				for (AnswerChoice answer : answers) {
					OriginalMatchAndStrategy match = null;

					match = strategy.extract(answer, knowledge);
					if (match != null) {
						returnList.add(match);
					}
				}
			}
		}

		return returnList;
	}

	/**
	 * @param kb
	 *            The knowledgebase, which TiRex works with.
	 * @param knowledge
	 *            The line of knowledge, which all the ExtractionStrategies are
	 *            to be tested on.
	 * @param questionStrategies
	 *            A Collection of different ExtractionStrategies, which are to
	 *            be applied to "knowledge" to extract questions.
	 * @param answerStrategies
	 *            A Collection of different ExtractionStrategies, which are to
	 *            be applied to "knowledge" to extract answers.
	 * @return Set of rated question answer pairs, sorted by rating
	 */
	public TreeSet<QuestionAndAnswerWithRating> extractQuestionsAndAnswers(
			KnowledgeBase kb, String knowledge,
			Collection<ExtractionStrategy> questionStrategies,
			Collection<ExtractionStrategy> answerStrategies) {
		TreeSet<QuestionAndAnswerWithRating> resultSet = new TreeSet<QuestionAndAnswerWithRating>();

		boolean searchedAnswersOnly = false;

		for (ExtractionStrategy qStrat : questionStrategies) {
			Collection<ExtractionStrategy> questionStrategy = new Vector<ExtractionStrategy>();
			questionStrategy.add(qStrat);

			Collection<OriginalMatchAndStrategy> questions = null;
			Collection<OriginalMatchAndStrategy> answers = null;

			questions = extractQuestions(kb, knowledge, questionStrategy);

			for (OriginalMatchAndStrategy question : questions) {
				for (ExtractionStrategy aStrat : answerStrategies) {
					Collection<ExtractionStrategy> answerStrategy = new Vector<ExtractionStrategy>();
					answerStrategy.add(aStrat);

					Question q = null;
					if (question != null) {
						q = (Question) question.getIDObject();
					} else {
						searchedAnswersOnly = true;
					}

					answers = extractAnswers(q, knowledge, kb, answerStrategy);

					if (answers.size() == 0) {
						// TiRexLogger.getInstance().updateContent(question.toString());
						resultSet.add(new QuestionAndAnswerWithRating(question,
								null));
					}

					for (OriginalMatchAndStrategy answer : answers) {
						// TiRexLogger.getInstance().updateContent(question.toString());
						// TiRexLogger.getInstance().updateContent(" + " +
						// answer.toString());
						if ((answer.getMatch()).equals(question.getMatch())
								|| (answer.getMatch()).contains(question
										.getMatch())
								|| (question.getMatch()).contains(answer
										.getMatch())) {
							if (question.getRating() > answer.getRating()) {
								resultSet.add(new QuestionAndAnswerWithRating(
										question, null));
							} else {
								resultSet.add(new QuestionAndAnswerWithRating(
										null, answer));
							}
						} else {
							resultSet.add(new QuestionAndAnswerWithRating(
									question, answer));
						}
					}
				}
			}
		}

		if (searchedAnswersOnly == false) {
			for (ExtractionStrategy aStrat : answerStrategies) {
				Collection<ExtractionStrategy> answerStrategy = new Vector<ExtractionStrategy>();
				answerStrategy.add(aStrat);

				Collection<OriginalMatchAndStrategy> answers = extractAnswers(
						null, knowledge, kb, answerStrategy);

				for (OriginalMatchAndStrategy answer : answers) {
					// TiRexLogger.getInstance().updateContent(answer.toString());
					resultSet
							.add(new QuestionAndAnswerWithRating(null, answer));
				}
			}
		}

		return resultSet;
	}
}
