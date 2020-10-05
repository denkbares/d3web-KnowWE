/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.core;

public class Attributes {

	public static final String TOPIC = "KWiki_Topic";
	public static final String TITLE = TOPIC;
	public static final String REDIRECT_PAGE = "RedirectPage";
	public static final String SECTION_ID = "SectionID";
	public static final String LOCAL_SECTION_STORAGE = "localSectionStorage";
	public static final String PACKAGE = "package";
	public static final String JSON_DATA = "data";
	public static final String ENCODING = "KWiki_Encoding";
	public static final String WEB = "KWikiWeb";
	public static final String CHANGE_NOTE = "KWikiChangeNote";
	public static final String TERM = "KWikiTerm";
	public static final String SESSIONPROVIDER = "KWikiSessionProvider";
	public static final String NOTIFICATIONMANAGER = "KWikiNotificationManager";
	public static final String NAMESPACE = "KWikiNamespace";
	public static final String TARGET = "TargetNamespace";
	public static final String TEXT = "KWikitext";
	public static final String KNOWLEDGEBASE = "KWikiknowledgebase";
	public static final String KNOWLEDGEBASE_ID = "KWikiknowledgebaseID";
	public static final String CLUSTERID = "KWikiClusterID";
	public static final String UPLOAD_KB_URL = "UploadKBUrl";
	public static final String UPLOAD_KB_ID = "UploadKBID";
	public static final String COMPLETION_TEXT = "CompletionText";
	public static final String SCL_DATA = "SCLData";
	public static final String PARENT_NODE_ID = "ParentNodeID";

	public static final String NEWKB_NAME = "NewKBName";
	public static final String ATTACHMENT_NAME = "AttachmentName";
	public static final String ATTACHMENT_PARENT = "AttachmentParent";

	public static final String NEW_PAGE_NAME = "NewPageName";
	public static final String TEMPLATE_NAME = "TemplateName";

	public static final String TOPIC_FOR_XCL = "TopicForXCL";

	public static final String TYPE_BROWSER_QUERY = "TypeBrowserQuery";

	public static final String PARSE_REPORT = "KWikiParseReport";
	public static final String SCLIST_EDITOR = "KWikiSCListEditor";
	public static final String SUMMARIZER = "KWikiSummarizer";

	public static final String SESSION_FILE = "KWikiSessionFile";
	public static final String KWIKI_CONFIG = "KWikiconfigproperties";
	public static final String SESSION_ID = "KWikisessionid";
	public static final String USER = "KWikiUser";
	public static final String LATESTN = "KWikiLatestN";
	public static final String TREETABLE = "KWikiTreeTable";
	public static final String TREESTATUSTABLE = "KWikiTreeStatusTable";
	public static final String TERMINOLOGYTYPE = "KWikiTerminologyType";
	public static final String FOCUSED_TERM = "KWikiFocusedTerm";
	public static final String JUMP_ID = "KWikiJumpId";
	public static final String EXPLAIN = "KWikiExplain";

	public static final String STEP_ACTION = "stepAction";
	public static final String CONFIRMED_ACTION = "confirmedAction";
	public static final String NOT_CONFIRMED_ACTION = "notConfirmedAction";
	public static final String USER_INTERVENTION_TEXT = "userConfirm";
	public static final String STEP_RENDERER = "stepRenderer";

	public static final String CONTEXT_PREVIOUS = "ContextPrevious";
	public static final String CONTEXT_AFTER = "ContextAfter";
	public static final String CASE_SENSITIVE = "CaseSensitive";
	public static final String ATM_URL = "ATMUrl";

	public static final String ACTION_SWITCH_CASE = "KWiki_switchCase";

	public static final String RENDERER_USER_INTERVENTION = "KWiki_userIntervention";
	public static final String LINK_ACTION = "KWikiLinkAction";
	public static final String KNOWWE_DIALOG = "KnowWE-Dialog";
	public static final String PROBLEM_SOLVER_TYPE = "ProblemSolverType";

	public static final String SEMANO_NAMESPACE = "namespace";
	public static final String SEMANO_TERM_NAME = "TermName";
	public static final String SEMANO_OBJECT_ID = "ObjectID";
	public static final String SEMANO_VALUE_ID = "ValueID";
	public static final String SEMANO_VALUE_IDS = "ValueIDS";
	public static final String SEMANO_VALUE_NUM = "ValueNum";
	public static final String SEMANO_VALUE_DATE = "ValueDate";
	public static final String SEMANO_VALUE_TEXT = "ValueText";
	public static final String SEMANO_TERM_TYPE = "TermType";

	public static final String TAGGING_ACTION = "tagaction";
	public static final String TAGGING_TAG = "tagtag";
	public static final String TAGGING_QUERY = "tagquery";

	public static String getBrokerConstant(String web) {
		if (web != null) {
			return SESSIONPROVIDER + web;
		}
		else {
			// [TODO]
			return null;
		}
	}

}
