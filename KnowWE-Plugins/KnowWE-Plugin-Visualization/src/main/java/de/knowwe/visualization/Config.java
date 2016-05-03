/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

package de.knowwe.visualization;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Data structure for visualization configuration.
 * <p/>
 * Created by Albrecht Striffler (denkbares GmbH) on 07.05.2015.
 */
public class Config {

	public static final String CONFIG = "config";
	public static final String COLORS = "colors";
	public static final String SUCCESSORS = "successors";
	public static final String PREDECESSORS = "predecessors";
	public static final String EXCLUDE_NODES = "excludeNodes";
	public static final String EXCLUDE_RELATIONS = "excludeRelations";
	public static final String FILTER_RELATIONS = "filterRelations";
	public static final String SHOW_CLASSES = "showClasses";
	public static final String SHOW_PROPERTIES = "showProperties";
	public static final String SHOW_OUTGOING_EDGES = "showOutgoingEdges";
	public static final String SHOW_INVERSE = "showInverse";
	public static final String SHOW_LABELS = "showLabels";
	public static final String CONCEPT = "concept";
	public static final String SIZE = "size";
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String FORMAT = "format";
	public static final String LANGUAGE = "language";
	public static final String LINK_MODE = "linkMode";
	public static final String RANK_DIR = "rankDir";
	public static final String RANK_SAME = "rankSame";
	public static final String DOT_APP = "dotApp";
	public static final String ADD_TO_DOT = "dotAddLine";
	public static final String RENDERER = "renderer";
	public static final String VISUALIZATION = "visualization";
	public static final String DESIGN = "design";
	public static final String PRERENDER = "prerender";
	public static final String TITLE = "title";
	public static final String LAYOUT = "layout";
	public static final String OVERLAP = "overlap";

	public Config() {
	}

	public Config(Section<? extends DefaultMarkupType> section) {
		this();
		readFromSection(section);
	}

	public String getForceVisualizationStyle() {
		return forceVisualizationStyle;
	}

	public void setForceVisualizationStyle(String forceVisualizationStyle) {
		this.forceVisualizationStyle = forceVisualizationStyle;
	}

	public enum Renderer {
		DOT, D3
	}

	public enum LinkMode {
		JUMP, BROWSE
	}

	public enum Visualization {
		WHEEL, FORCE
	}

	public enum RankDir {
		LR, RL, TB, BT
	}

	public enum Layout {
		DOT, NEATO, FDP, CIRCO , TWOPI
	}

	public enum Overlap {
		TRUE, FALSE, SCALEXY, SCALE, COMPRESS, VPSC, VORONOI, ORTHO, ORTHOXY, ORTHOYX, PORTHO, PORTHOXY, PORTHOYX
	}

	public String getCacheDirectoryPath() {
		if (cacheDirectoryPath == null) {
			return Environment.getInstance().getWikiConnector().getApplicationRootPath();
		}
		return cacheDirectoryPath;
	}

	public String getTitle() {
		return title;
	}

	public String getSectionId() {
		return sectionId;
	}

	private String colors = null;
	private Map<String, String> relationColors = new HashMap<String, String>();
	private Map<String, String> classColors = new HashMap<String, String>();
	private int successors = 1;
	private int predecessors = 1;
	private Collection<String> excludeNodes = new HashSet<>();
	private Collection<String> excludeRelations = new HashSet<>();
	private Collection<String> filterRelations = new HashSet<>();
	private Collection<String> concepts = new HashSet<>();
	private boolean showClasses = true;
	private boolean showProperties = true;
	private boolean showInverse = false;
	private boolean showOutgoingEdges = false;
	private String showLabels = null;
	private String size = null;
	private String width = null;
	private String height = null;
	private String format = "svg";
	private String language = null;
	private LinkMode linkMode = LinkMode.JUMP;
	private RankDir rankDir = RankDir.LR;
	private String rankSame = null;
	private String dotApp = "dot";
	private String dotAddLine = null;
	private Renderer renderer = Renderer.DOT;
	private Visualization visualization = Visualization.FORCE;
	private String forceVisualizationStyle = null;
	private String design = null;
	private String config = null;
	private String prerender = null;
	private String cacheFileID = null;
	private String sectionId = null;
	private String title = null;
	private String layout = null;
	private String overlap = null;
	private String cacheDirectoryPath = null;

	public void readFromSection(Section<? extends DefaultMarkupType> section) {

		String[] configNames = DefaultMarkupType.getAnnotations(section, CONFIG);
		if (configNames.length > 0) {
			Collection<Section<VisualizationConfigType>> configTypeSections = Sections.successors(section.getArticleManager(), VisualizationConfigType.class);
			HashSet<String> names = new HashSet<>();
			Collections.addAll(names, configNames);
			for (Section<VisualizationConfigType> configTypeSection : configTypeSections) {
				String name = DefaultMarkupType.getAnnotation(configTypeSection, VisualizationConfigType.ANNOTATION_NAME);
				if (names.contains(name)) {
					readFromSection(configTypeSection);
				}
			}
		}

		setColors(DefaultMarkupType.getAnnotation(section, COLORS));
		parseAndSetInt(section, SUCCESSORS, this::setSuccessors);
		parseAndSetInt(section, PREDECESSORS, this::setPredecessors);
		parseAndSetCSV(section, EXCLUDE_NODES, this::addExcludeNodes);
		parseAndSetCSV(section, EXCLUDE_RELATIONS, this::addExcludeRelations);
		parseAndSetCSV(section, FILTER_RELATIONS, this::addFilterRelations);
		parseAndSetBoolean(section, SHOW_CLASSES, this::setShowClasses);
		parseAndSetBoolean(section, SHOW_PROPERTIES, this::setShowProperties);
		parseAndSetBoolean(section, SHOW_OUTGOING_EDGES, this::setShowOutgoingEdges);
		parseAndSetBoolean(section, SHOW_INVERSE, this::setShowInverse);
		setShowLabels(DefaultMarkupType.getAnnotation(section, SHOW_LABELS));
		parseAndSetCSV(section, CONCEPT, this::addConcept);
		setSize(DefaultMarkupType.getAnnotation(section, SIZE));
		setWidth(DefaultMarkupType.getAnnotation(section, WIDTH));
		setHeight(DefaultMarkupType.getAnnotation(section, HEIGHT));
		setFormat(DefaultMarkupType.getAnnotation(section, FORMAT));
		setLanguage(DefaultMarkupType.getAnnotation(section, LANGUAGE));
		parseAndSetEnum(section, RANK_DIR, RankDir.class, this::setRankDir);
		setDotApp(DefaultMarkupType.getAnnotation(section, DOT_APP));
		setDotAddLine(DefaultMarkupType.getAnnotation(section, ADD_TO_DOT));
		parseAndSetEnum(section, LINK_MODE, LinkMode.class, this::setLinkMode);
		parseAndSetEnum(section, RENDERER, Renderer.class, this::setRenderer);
		parseAndSetEnum(section, VISUALIZATION, Visualization.class, this::setVisualization);
		setDesign(DefaultMarkupType.getAnnotation(section, DESIGN));
		setPrerender(DefaultMarkupType.getAnnotation(section, PRERENDER));
		setSectionId(section.getID());
		setTitle(DefaultMarkupType.getAnnotation(section, TITLE));
		setRankSame(DefaultMarkupType.getAnnotation(section, RANK_SAME));
		parseAndSetEnum(section, OVERLAP, Overlap.class, this::setOverlap);
		parseAndSetEnum(section, LAYOUT, Layout.class, this::setLayout);
	}

	private <E extends Enum<E>> void parseAndSetEnum(Section<? extends DefaultMarkupType> section, String annotationName, Class<E> enumType, Consumer<E> setter) {
		String annotation = DefaultMarkupType.getAnnotation(section, annotationName);
		if (annotation != null) {
			try {
				E value = Enum.valueOf(enumType, annotation.toUpperCase());
				setter.accept(value);
			}
			catch (IllegalArgumentException e) {
				Log.warning("Annotation '" + annotationName + "' expects on of the following values: "
						+ enumType.toString() + ". '" + annotation + "' is not one of them.");
			}
		}
	}

	private void parseAndSetBoolean(Section<? extends DefaultMarkupType> section, String annotationName, Consumer<Boolean> setter) {
		String annotation = DefaultMarkupType.getAnnotation(section, annotationName);
		if (annotation != null) {
			setter.accept(annotation.equalsIgnoreCase("true"));
		}
	}

	public String getRankSame() {
		return rankSame;
	}

	public void setRankSame(String rankSame) {
		this.rankSame = rankSame;
	}

	public String getOverlap() {
		return overlap;
	}

	public void setOverlap(Overlap overlap) {
		this.overlap = overlap.toString();
	}

	public String getLayout() {
		return layout;
	}

	public void setLayout(Layout layout) {
		this.layout = layout.toString();
	}


	private void parseAndSetInt(Section<? extends DefaultMarkupType> section, String annotationName, Consumer<Integer> setter) {
		String annotation = DefaultMarkupType.getAnnotation(section, annotationName);
		if (annotation != null) {
			try {
				setter.accept(Integer.parseInt(annotation));
			}
			catch (NumberFormatException e) {
				Log.warning("Annotation '" + annotationName + "' expects an integer, '" + annotation + "' is not and integer.");
			}
		}
	}

	private void parseAndSetCSV(Section<? extends DefaultMarkupType> section, String annotationName, Consumer<String[]> setter) {
		String[] annotations = DefaultMarkupType.getAnnotations(section, annotationName);
		for (String annotation : annotations) {
			for (String csv : annotation.split(",")) {
				setter.accept(new String[] { Strings.trim(csv) });
			}
		}
	}

	public void setColors(String colors) {
		if (colors == null) return;
		this.colors = colors;
	}

	public void setRelationColors(Map<String, String> relationColors) {
		// TODO: refactor this to be a map of color assignments
		if (relationColors == null) return;
		this.relationColors = relationColors;
	}

	public void setClassColors(Map<String, String> classColors) {
		// TODO: refactor this to be a map of color assignments
		if (classColors == null) return;
		this.classColors = classColors;
	}

	public void setSuccessors(int successors) {
		this.successors = successors;
	}

	public void setPredecessors(int predecessors) {
		this.predecessors = predecessors;
	}

	public void addExcludeNodes(String... excludeNodes) {
		Collections.addAll(this.excludeNodes, excludeNodes);
	}

	public void addExcludeRelations(String... excludeRelations) {
		Collections.addAll(this.excludeRelations, excludeRelations);
	}

	public void addFilterRelations(String... filterRelations) {
		Collections.addAll(this.filterRelations, filterRelations);
	}

	public void addConcept(String... concept) {
		Collections.addAll(this.concepts, concept);
	}

	public void setConcept(String... concepts) {
		this.concepts = new HashSet<>();
		addConcept(concepts);
	}

	public void setShowClasses(boolean showClasses) {
		this.showClasses = showClasses;
	}

	public void setShowProperties(boolean showProperties) {
		this.showProperties = showProperties;
	}

	public void setShowOutgoingEdges(boolean showOutgoingEdges) {
		this.showOutgoingEdges = showOutgoingEdges;
	}

	public void setShowInverse(boolean showInverse) {
		this.showInverse = showInverse;
	}

	public void setSize(String size) {
		if (size == null) return;
		this.size = size;
	}

	public void setWidth(String width) {
		if (width == null) return;
		this.width = width;
	}

	public void setHeight(String height) {
		if (height == null) return;
		this.height = height;
	}

	public void setFormat(String format) {
		if (format == null) return;
		format = format.toLowerCase();
		this.format = format;
	}

	public void setLanguage(String language) {
		if (language == null) return;
		this.language = language;
	}

	public void setLinkMode(LinkMode linkMode) {
		if (linkMode == null) return;
		this.linkMode = linkMode;
	}

	public void setRankDir(RankDir rankDir) {
		if (rankDir == null) return;
		this.rankDir = rankDir;
	}

	public void setDotApp(String dotApp) {
		if (dotApp == null) return;
		this.dotApp = dotApp;
	}

	public void setDotAddLine(String dotAddLine) {
		if (dotAddLine == null) return;
		if (!dotAddLine.matches("^.*?\r?\n$")) {
			dotAddLine = dotAddLine + "\n";
		}
		this.dotAddLine = dotAddLine;
	}

	public void setRenderer(Renderer renderer) {
		if (renderer == null) return;
		this.renderer = renderer;
	}

	public void setVisualization(Visualization visualization) {
		if (visualization == null) return;
		this.visualization = visualization;
	}

	public void setDesign(String design) {
		if (design == null) return;
		this.design = design;
	}

	public void setShowLabels(String showLabels) {
		this.showLabels = showLabels;
	}

	public void setConfig(String config) {
		if (config == null) return;
		this.config = config;
	}

	public void setPrerender(String prerender) {
		if (prerender == null) return;
		this.prerender = prerender;
	}

	public void setCacheFileID(String fileID) {
		if (fileID == null) return;
		this.cacheFileID = fileID;
	}

	public String getColors() {
		return colors;
	}

	public String getConfig() {
		return config;
	}

	public Map<String, String> getRelationColors() {
		return relationColors;
	}

	public Map<String, String> getClassColors() {
		return classColors;
	}

	public int getSuccessors() {
		return successors;
	}

	public int getPredecessors() {
		return predecessors;
	}

	public Collection<String> getExcludeNodes() {
		return Collections.unmodifiableCollection(excludeNodes);
	}

	public Collection<String> getExcludeRelations() {
		return Collections.unmodifiableCollection(excludeRelations);
	}

	public Collection<String> getFilterRelations() {
		return Collections.unmodifiableCollection(filterRelations);
	}

	public boolean isShowClasses() {
		return showClasses;
	}

	public boolean isShowProperties() {
		return showProperties;
	}

	public boolean isShowOutgoingEdges() {
		return showOutgoingEdges;
	}

	public boolean isShowInverse() {
		return showInverse;
	}

	public String getShowLabels() {
		return showLabels;
	}

	public Collection<String> getConcepts() {
		return concepts;
	}

	public String getSize() {
		return size;
	}

	public String getWidth() {
		return width;
	}

	public String getHeight() {
		return height;
	}

	public String getFormat() {
		return format;
	}

	public String getLanguage() {
		return language;
	}

	public LinkMode getLinkMode() {
		return linkMode;
	}

	public RankDir getRankDir() {
		return rankDir;
	}

	public String getDotApp() {
		return dotApp;
	}

	public String getDotAddLine() {
		return dotAddLine;
	}

	public Renderer getRenderer() {
		return renderer;
	}

	public Visualization getVisualization() {
		return visualization;
	}

	public String getDesign() {
		return design;
	}

	public String getPrerender() {
		return prerender;
	}

	public String getCacheFileID() {
		return cacheFileID;
	}

	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setCacheDirectoryPath(String cachePath) {
		this.cacheDirectoryPath = cachePath;
	}
}
