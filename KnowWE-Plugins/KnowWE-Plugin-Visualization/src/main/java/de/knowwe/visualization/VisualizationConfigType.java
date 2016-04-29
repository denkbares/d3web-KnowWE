/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * @author: Johanna Latt
 * @created 13.07.2014.
 */
public class VisualizationConfigType extends DefaultMarkupType {

	public static final String ANNOTATION_NAME = "name";



	protected String getMarkupName() {
		return "VisualizationConfig";
	}

	public VisualizationConfigType() {
		applyMarkup(createMarkup());
		this.setRenderer(new DefaultMarkupRenderer());
	}

	public  DefaultMarkup createMarkup() {
		DefaultMarkup markup = new DefaultMarkup(getMarkupName());
		markup.addAnnotation(ANNOTATION_NAME, true);
		markup.addAnnotation(Config.COLORS, false);
		markup.addAnnotation(Config.SUCCESSORS, false);
		markup.addAnnotation(Config.PREDECESSORS, false);
		markup.addAnnotation(Config.EXCLUDE_NODES, false);
		markup.addAnnotation(Config.EXCLUDE_RELATIONS, false);
		markup.addAnnotation(Config.FILTER_RELATIONS, false);
		markup.addAnnotation(Config.SIZE, false);
		markup.addAnnotation(Config.HEIGHT, false);
		markup.addAnnotation(Config.WIDTH, false);
		markup.addAnnotation(Config.FORMAT, false);
		markup.addAnnotation(Config.SHOW_CLASSES, false, "true", "false");
		markup.addAnnotation(Config.SHOW_PROPERTIES, false, "true", "false");
		markup.addAnnotation(PackageManager.MASTER_ATTRIBUTE_NAME, false);
		markup.addAnnotation(Config.LANGUAGE, false);
		markup.addAnnotation(Config.DOT_APP, false);
		markup.addAnnotation(Config.ADD_TO_DOT, false);
		markup.addAnnotation(Config.SHOW_OUTGOING_EDGES, false, "true", "false");
		markup.addAnnotation(Config.SHOW_INVERSE, false, "true", "false");
		markup.addAnnotation(Config.VISUALIZATION, false, Config.Visualization.values());
		markup.addAnnotation(Config.LINK_MODE, false, Config.LinkMode.values());
		markup.addAnnotation(Config.RANK_DIR, false, Config.RankDir.values());
		markup.addAnnotation(Config.SHOW_LABELS, false);
		markup.addAnnotation(Config.DESIGN, false);
		markup.addAnnotation(Config.OVERLAP, false, Config.Overlap.values() );
		markup.addAnnotation(Config.LAYOUT, false, Config.Layout.values());
		markup.addAnnotation(Config.RANK_SAME, false);
		return markup;
	}
}
