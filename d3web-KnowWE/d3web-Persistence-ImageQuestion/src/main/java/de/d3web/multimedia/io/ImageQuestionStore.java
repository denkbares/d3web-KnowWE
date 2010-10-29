/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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

import java.util.List;

import de.d3web.core.knowledge.terminology.info.Property;

/**
 * Stored in the InfoStore of a question. Getter/Setter for the
 * ImageQuestionInfos used in {@link ImageQuestionPersistenceHandler}
 * 
 * @author Johannes Dienst
 * @created 11.10.2010
 */
public class ImageQuestionStore {

	/**
	 * used for: Storing infos about a QuestionImage and their AnswerRegions
	 * doc: Used to store a ImageQuestionStore of Image and Answer Regions
	 * 
	 * @return de.d3web.multimedia.io.ImageQuestionStore
	 */
	public static final Property<ImageQuestionStore> IMAGE_QUESTION_INFO =
			Property.getProperty("image_question_info", ImageQuestionStore.class);

	private String file;
	private String width;
	private String height;
	private List<List<String>> answerRegions;

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public List<List<String>> getAnswerRegions() {
		return answerRegions;
	}

	public void setAnswerRegions(List<List<String>> answerRegions) {
		this.answerRegions = answerRegions;
	}

}
