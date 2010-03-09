/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.tirex.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import de.d3web.core.io.PersistenceManager;
import de.d3web.core.io.progress.ConsoleProgressListener;
import de.d3web.core.knowledge.KnowledgeBase;

/**
 * This singleton class contains the data and methods needed for TiRex to
 * function. A knowledgebase, a wiki-file, a file with the synonym-sets and one
 * with regular expressions can be set and loaded along with the TiRex-Settings
 * saved in a ResourceBundle.
 * 
 * @author Dmitrij Frese
 * @date 02/2008
 */
public class TiRexFileReader {

	/**
	 * The wiki-file (has to be explicitely set by another class just as the
	 * other files)
	 */
	private String wikiFile;

	/**
	 * The file that contains the knowledgebase
	 */
	private KnowledgeBase knowledgeBase;

	/**
	 * The file with the synonym sets
	 */
	private String synonymSetsFile;

	/**
	 * The file containing the regex-knoffice pairs
	 */
	private String regexKnofficePairsFile;

	/**
	 * The file containing the TiRex settings
	 */
	private String tiRexSettingsFile;

	/**
	 * The unique instance.
	 */
	private static TiRexFileReader instance;

	private TiRexFileReader() {
		// empty
	}

	/**
	 * @return The unique instance of the TiRexFileReader.
	 */
	public static TiRexFileReader getInstance() {
		if (instance == null) {
			instance = new TiRexFileReader();
		}

		return instance;
	}

	/**
	 * @param file
	 *            The File, from which the knowledgebase is to be read.
	 * @return The loaded knowledgebase.
	 * @throws IOException 
	 */
	public KnowledgeBase loadKnowledgebase(File file)
			throws IOException {
		return loadKnowledgebase(file.toURI().toURL());
	}

	/**
	 * @param url
	 *            The link to the location of the knowledgebase.
	 * @return The loaded knowledgebase.
	 * @throws IOException 
	 */
	public KnowledgeBase loadKnowledgebase(URL url) throws IOException {
		PersistenceManager mgr = PersistenceManager.getInstance();
		return mgr.load(new File(url.getFile()), new ConsoleProgressListener());
	}

	/**
	 * @param path
	 *            The path to the location of the knowledgebase.
	 * @return The loaded knowledgebase.
	 * @throws IOException 
	 */
	public KnowledgeBase loadKnowledgebase(String path)
			throws IOException {
		URL url = new URL(path);

		return loadKnowledgebase(url);
	}

	public String getWikiFile() {
		return wikiFile;
	}

	public void setWikiFile(String wikiFile) {
		this.wikiFile = wikiFile;
	}

	public KnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}

	public void setKnowledgeBase(KnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	public String getSynonymSetsFile() {
		return synonymSetsFile;
	}

	public void setSynonymSetsFile(String synonymSetsFile) {
		this.synonymSetsFile = synonymSetsFile;
	}

	public String getRegexKnofficePairsFile() {
		return regexKnofficePairsFile;
	}

	public void setRegexKnofficePairsFile(String regexKnofficePairsFile) {
		this.regexKnofficePairsFile = regexKnofficePairsFile;
	}

	public String getTiRexSettingsFile() {
		return regexKnofficePairsFile;
	}

	public void setTiRexSettingsFile(String tiRexSettingsFile) {
		this.tiRexSettingsFile = tiRexSettingsFile;
	}
}
