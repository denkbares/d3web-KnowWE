/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg denkbares GmbH
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

package de.d3web.multimedia.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.d3web.core.io.KnowledgeReader;
import de.d3web.core.io.KnowledgeWriter;
import de.d3web.core.io.progress.ProgressListener;
import de.d3web.core.io.utilities.Util;
import de.d3web.core.io.utilities.XMLUtil;
import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.info.BasicProperties;

/**
 * PersistenceHandler for PictureQuestion used in the
 * {@link ImageQuestionHandler}.
 * 
 * 
 * @author Johannes Dienst
 * 
 */
public class ImageQuestionPersistenceHandler implements KnowledgeReader, KnowledgeWriter {

	@Override
	public void read(KnowledgeBase knowledgeBase, InputStream stream,
			ProgressListener listener) throws IOException {

		listener.updateProgress(0, "Starting to load picture questions");

		Document doc = Util.streamToDocument(stream);
		List<Element> childNodes = XMLUtil.getElementList(doc.getChildNodes());

		// Check for right DocumentStructure
		if (!(childNodes.size() == 0) && !(childNodes.size() > 1)) {
			if (childNodes.get(0).getNodeName().equals("Questions")) {

				List<Element> questions = XMLUtil.getElementList(childNodes
						.get(0).getChildNodes());

				// Load properties for every ImageQuestion
				for (int i = 0; i < questions.size(); i++) {

					Element questionElement = questions.get(i);
					String id = questionElement.getAttribute("ID");
					Question q = knowledgeBase.searchQuestion(id);

					InfoStore infoStore = q.getInfoStore();
					ImageQuestionStore store = new ImageQuestionStore();

					List<Element> atts = XMLUtil.getElementList(questionElement
							.getChildNodes());

					// Filename
					String file = atts.get(0).getAttribute("file");
					store.setFile(file);

					// width and height of image
					store.setWidth(atts.get(0).getAttribute("width"));
					store.setHeight(atts.get(0).getAttribute("height"));

					// answerRegions
					store.setAnswerRegions(readAnswerRegions(atts.get(0)));

					infoStore.addValue(BasicProperties.IMAGE_QUESTION_INFO, store);
				}
			}
		}
		listener.updateProgress(1, "Loading Picture Questions finished");

	}

	@Override
	public int getEstimatedSize(KnowledgeBase knowledgeBase) {
		int count = 0;
		for (Question q : knowledgeBase.getQuestions()) {
			if (q.getInfoStore().getValue(BasicProperties.IMAGE_QUESTION_INFO) != null) count++;
		}
		return count;
	}

	@Override
	public void write(KnowledgeBase knowledgeBase, OutputStream stream,
			ProgressListener listener) throws IOException {
		listener.updateProgress(0, "Starting to save Image Questions");
		int maxvalue = getEstimatedSize(knowledgeBase);
		float aktvalue = 0;

		Document doc = Util.createEmptyDocument();
		Element root = doc.createElement("Questions");
		List<Question> questions = knowledgeBase.getQuestions();

		for (Question q : questions) {
			ImageQuestionStore store = (ImageQuestionStore) q.getInfoStore().getValue(
					BasicProperties.IMAGE_QUESTION_INFO);

			if (store != null) {
				Element question = doc.createElement("Question");
				question.setAttribute("ID", q.getId());
				Element questionImage = doc.createElement("QuestionImage");
				questionImage.setAttribute("file", store.getFile());
				questionImage.setAttribute("width", store.getWidth());
				questionImage.setAttribute("height", store.getHeight());

				// Answer Regions
				writeAnswerRegions(doc, questionImage, store.getAnswerRegions());

				listener.updateProgress(aktvalue++ / maxvalue, "Saving Image Question "
						+ Math.round(aktvalue) + " of " + maxvalue);
				question.appendChild(questionImage);
				root.appendChild(question);
			}
		}
		doc.appendChild(root);

		listener.updateProgress(1, "Image Question saved");
		Util.writeDocumentToOutputStream(doc, stream);

	}

	/**
	 * Writes the Answer Regions to a Document.
	 * 
	 * @created 12.10.2010
	 * @param doc
	 * @param questionImage
	 * @param answerRegions
	 */
	private static void writeAnswerRegions(Document doc, Element questionImage, List<List<String>> answerRegions) {
		for (List<String> ar : answerRegions) {
			String answerID = ar.get(0);
			Element answerEl = doc.createElement("AnswerRegion");
			answerEl.setAttribute("answerID", answerID);
			answerEl.setAttribute("xStart", ar.get(1));
			answerEl.setAttribute("xEnd", ar.get(2));
			answerEl.setAttribute("yStart", ar.get(3));
			answerEl.setAttribute("yEnd", ar.get(4));
			questionImage.appendChild(answerEl);
		}
	}

	/**
	 * Reads out the Answer Regions from a given Element.
	 * 
	 * @created 12.10.2010
	 * @param element
	 * @return
	 */
	private static List<List<String>> readAnswerRegions(Element element) {
		ArrayList<List<String>> ret = new ArrayList<List<String>>();
		List<Element> list = XMLUtil.getElementList(element.getChildNodes());
		for (int i = 0; i < list.size(); i++) {
			Element aR = list.get(i);
			ArrayList<String> regionsInfo = new ArrayList<String>();
			regionsInfo.add(aR.getAttribute("answerID"));
			regionsInfo.add(aR.getAttribute("xStart"));
			regionsInfo.add(aR.getAttribute("xEnd"));
			regionsInfo.add(aR.getAttribute("yStart"));
			regionsInfo.add(aR.getAttribute("yEnd"));
			ret.add(regionsInfo);
		}
		return ret;
	}
}
