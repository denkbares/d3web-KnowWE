package de.d3web.we.utils;

import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SyntaxError;
import de.d3web.we.kdom.report.message.NewObjectCreated;

public final class MessageUtils {

	// avoid instantiation
	private MessageUtils() {
	}

	/**
	 * Returns a collection of one message to be used as the return value for
	 * SubtreeHandlers when they successfully created a single terminology
	 * object.
	 * 
	 * @created 16.08.2010
	 * @param object the terminology object created
	 * @return the list of messages
	 */
	public static final Collection<KDOMReportMessage> objectCreatedAsList(TerminologyObject object) {
		return Arrays.asList((KDOMReportMessage) objectCreated(object));

	}

	/**
	 * Returns a message to be used as the return value for SubtreeHandlers when
	 * they successfully created a single terminology object.
	 * 
	 * @created 16.08.2010
	 * @param object the terminology object created
	 * @return the message
	 */
	public static final KDOMReportMessage objectCreated(TerminologyObject object) {
		return new NewObjectCreated(
						object.getClass().getSimpleName() + " " + object.getName());

	}

	/**
	 * Returns a collection of one message to be returned by a SubtreeHandler
	 * recognizes an syntactial error within its markup.
	 * 
	 * @created 18.08.2010
	 * @param localizedMessage the message of the syntax error
	 * @return
	 */
	public static Collection<KDOMReportMessage> syntaxErrorAsList(String localizedMessage) {
		return Arrays.asList((KDOMReportMessage) syntaxError(localizedMessage));
	}

	/**
	 * Returns a message to be used when a SubtreeHandler recognizes an
	 * syntactial error within its markup.
	 * 
	 * @created 18.08.2010
	 * @param localizedMessage the message of the syntax error
	 * @return
	 */
	public static KDOMReportMessage syntaxError(String localizedMessage) {
		return new SyntaxError(localizedMessage);
	}

	/**
	 * Wraps a single or more messages into a collection to be returned by the
	 * SubtreeHandler implementations.
	 * 
	 * @created 16.08.2010
	 * @param messages the message(s) to be wrapped
	 * @return the wrapped message(s)
	 */
	public static final Collection<KDOMReportMessage> asList(KDOMReportMessage... messages) {
		return Arrays.asList(messages);
	}

}
