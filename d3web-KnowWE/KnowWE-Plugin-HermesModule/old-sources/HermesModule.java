package de.d3web.we.hermes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.textParser.Utils.Message;
import de.d3web.textParser.Utils.Report;
import de.d3web.we.dom.KnowWEObjectType;
import de.d3web.we.hermes.timeline.TimelineDatabase;
import de.d3web.we.hermes.timeline.TimelineEvent;
import de.d3web.we.hermes.timeline.TimelineEventParser;
import de.d3web.we.hermes.timeline.TimelineRenderer;
import de.d3web.we.javaEnv.KnowWETopicLoader;
import de.d3web.we.module.DefaultKnowWEModule;
import de.d3web.we.module.KnowWEParseResult;
import de.d3web.we.module.SectionFinder;

/**
 * The Class HermesModule.
 */
public class HermesModule extends DefaultKnowWEModule {

	private static final String END_TIME = "endTime";

	private static final String START_TIME = "startTime";

	private static final String NUMBER_OF_ENTRIES = "numberOfEntries";

	private static final String START_INDEX_TIMELINE = "startIndexTimeline";

	/** The Constant REPARSE. */
	private static final String REPARSE = "REPARSE";
	
	private static final String IMPORTANT_EVENTS = "importantEvents";
	private static final String ALL_EVENTS = "allEvents";
	private static final String TIMELINE_FORM = "timelineForm";

	
	//dont show timelineEntries at while rendering.
	//They are displayed only via plugins!
	@Override
	public String renderPreTranslate(String output, String topic, String user, String web) {
		return "";
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.module.DefaultKnowWEModule#getSectioner()
	 */
	@Override
	/*
	 * Specifies, which sections of the wiki content are treated by HermesModule
	 */
	public SectionFinder getSectioner() {
		return new HermesSectioner();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.d3web.we.module.DefaultKnowWEModule#init(de.d3web.we.javaEnv.
	 * KnowWETopicLoader)
	 */
	@Override
	/*
	 * Actions taken on initialization of HermesModule
	 */
	public void init(KnowWETopicLoader loader) {
		Logger.getLogger("Hermes").info("HermesModule initialized.");
	}

	/**
	 * Is called, when a page is saved (once for the page).
	 * 
	 * Removes the timeline events from the TimelineDatabase.
	 * 
	 * @param topic
	 *            the topic
	 */
	public void onSave(String topic) {
		TimelineDatabase.getInstance().removeAllEventsFromTopic(topic);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.d3web.we.module.DefaultKnowWEModule#modifyAndInsert(java.lang.String,
	 * java.lang.String, java.lang.String,
	 * de.d3web.kernel.domainModel.KnowledgeBaseManagement)
	 */
	@Override
	/*
	 * Is called for every section treated by HermesModule on each page save.
	 * 
	 * Tries to parse a TimelineEvent out of each section and saves it to the
	 * database
	 */
	public KnowWEParseResult modifyAndInsert(String topic, String web, String text,
			KnowledgeBaseManagement kbm) {

		//extract the timeline event from the given text
		TimelineEvent te = TimelineEventParser.parseWiki2DatabaseEvent(topic, text);
		if (te != null)
			TimelineDatabase.getInstance().addTimelineEvent(te);

		//report to KnowWE, that we have no diagnostic knowledge
		Report r = new Report();
		r.add(new Message("No Knowledge inserted by HermesModule"));
		return new KnowWEParseResult(r, topic, text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.module.DefaultKnowWEModule#renderTags(java.util.Map,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	/*
	 * This is to handle JSPWiki "Plugins" ("execute"-method)
	 * 
	 * The action taken is determined by the parameters given.
	 */
	public String renderTags(Map<String, String> params, String topic, String user, String web) {
		if (params.get(REPARSE) != null) {
			
			return CompleteParser.reparseWeb();
		} else if (params.get(IMPORTANT_EVENTS) != null) {
				return TimelineRenderer.renderImportantEventsToList(topic, params.get(IMPORTANT_EVENTS));
		} else if (params.get(ALL_EVENTS) != null) {
				return TimelineRenderer.renderEventsToTabbedPane(topic);
		} else if (params.get(TIMELINE_FORM) != null) {
				return this.generateForms(topic);
		}else {
			
			String startIndexString = params.get(START_INDEX_TIMELINE);
			String numberOfEntriesString = params.get(NUMBER_OF_ENTRIES);
			String startTimeString = params.get(START_TIME);
			String endTimeString = params.get(END_TIME);

			if (startIndexString != null && numberOfEntriesString != null && startTimeString != null
					&& endTimeString != null) {
				
				return TimelineRenderer.generateTimeline(startIndexString, numberOfEntriesString, startTimeString,
						endTimeString);
			}
		}
		return "";
	}

	private String generateForms(String topic) {
		int startIndex = 0;
		int noEntries = 20;
		String startTimeString = "10000v";
		String endTimeString = "2008";
		String s = "";
		s += "<form action=\"Wiki.jsp?page=" + topic + "\" name=\"testform\" accept-charset=\"UTF-8\" method=\"post\" enctype=\"application/x-www-form-urlencoded\"><input type=\"hidden\" name=\"formname\" value=\"testform\"/>";
		s += "<p>Start Index: <input name='nbf_startIndexTimeline' type='text' value='" + startIndex + "'/> Anzahl Einträge: <input name='nbf_numberOfEntries' type='text' value='" + noEntries + "'/></p>";
		s += "<p>Von: <input name='nbf_startTime' type='text' value='" + startTimeString + "'/> Bis: <input name='nbf_endTime' type='text' value='" + endTimeString + "'/></p>";
		s += "<p><input name='nbf_Laden' type='submit' value='Anzeigen'/></p>";
		s += "</form>";
		return s;
	}

	@Override
	public List<? extends KnowWEObjectType> getAllowedChildrenTypes() {
		return Collections.EMPTY_LIST;
	}
}
