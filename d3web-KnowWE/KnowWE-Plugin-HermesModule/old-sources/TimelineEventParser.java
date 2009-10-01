package de.d3web.we.hermes.timeline;

import java.util.ArrayList;
import java.util.StringTokenizer;

import de.d3web.we.hermes.HermesSectioner;

/**
 * The Class TimelineEventParser reads a TimelineEvent from a text section in the wiki.
 */
public class TimelineEventParser {

	/**
	 * Parses the wiki2 database event.
	 * 
	 * @param topic the topic of this wiki article
	 * @param content the content of this section of the wiki article
	 * 
	 * @return the timeline event
	 */
	public static TimelineEvent parseWiki2DatabaseEvent(String topic, String content) {

		// remove start and end tag.
		content = content.substring(HermesSectioner.START_TAG.length(), content.length()- HermesSectioner.END_TAG.length());
		
		//initialize the list of historical sources describing this event
		ArrayList<String> sources = new ArrayList<String>();
		
		// Now tokenize it line by line:
		StringTokenizer tokenizer = new StringTokenizer(content, "\n");

		// the first Line contains header and relevance
		String firstLine = tokenizer.nextToken().trim();

		int indexOfLastOpeningBracket = firstLine.lastIndexOf("(");
		int indexOfLastClosingBracket = firstLine.lastIndexOf(")");

		// set defaults for error in this line
		int relevance = 4;
		String header = firstLine;

		if (indexOfLastOpeningBracket > -1 && indexOfLastClosingBracket > -1
				&& indexOfLastClosingBracket > indexOfLastOpeningBracket) {
			header = firstLine.substring(0, indexOfLastOpeningBracket);
			String relevString = firstLine.substring(indexOfLastOpeningBracket + 1, indexOfLastClosingBracket);
			try {
				relevance = Integer.parseInt(relevString);
			} catch (NumberFormatException nfe) {
				// do nothing => use default relevance
			}
		}
		
		//time String is in the next Line:
		String timeString = "";
		if (tokenizer.hasMoreTokens()) {
			timeString = tokenizer.nextToken().trim();
		}

		//next comes the abstract:
		String abstractText = "";
		while (tokenizer.hasMoreTokens()) {
			String line = tokenizer.nextToken().trim();
			
			//Here starts a source
			if (line.startsWith(TimelineEvent.SOURCE_INDICATOR)) {
				sources.add(line.substring(TimelineEvent.SOURCE_INDICATOR.length()));
				break;
			}
			abstractText += line + "\n";
		}
		return new TimelineEvent(topic, header, timeString, abstractText, relevance, sources);
	}

	/**
	 * Parses the timeline event to a text section
	 * 
	 * @param te the timelineEvent
	 * 
	 * @return the string
	 */
	public static String parseTimelineEvent2FileEntry(TimelineEvent te) {
		String s = "<<";
		s += te.getHeader() + " (" + te.getRelevance() + ")" + "\n";
		s += te.getTimeString() + "\n\n";
		s += te.getAbstractText() + "\n\n";
		for (String source : te.getSources()) {
			s += TimelineEvent.SOURCE_INDICATOR + " " + source + "\n";
		}
		s += ">>";
		
		return s;
	}

	
}
