/*
 * Copyright (C) 2014 denkbares GmbH
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
package de.knowwe.include.export;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.MyXWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.include.export.DocumentBuilder.Style;
import de.knowwe.jspwiki.types.PluginType;

/**
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class ImageExporter implements Exporter<PluginType> {

	@Override
	public boolean canExport(Section<PluginType> section) {
		return Strings.startsWithIgnoreCase(section.getText(), "[{Image");
	}

	@Override
	public Class<PluginType> getSectionType() {
		return PluginType.class;
	}

	@Override
	public void export(Section<PluginType> section, DocumentBuilder manager) throws ExportException {
		String file = Strings.trim(attr(section, "src"));
		if (Strings.isBlank(file)) {
			throw new ExportException("cannot export image: " +
					"'src' does not contain a file name in " + section.getText());
		}
		try {
			String title = section.getTitle();
			String path;
			if (file.startsWith("attach/")) {
				// image tag uses verbose image url: attach/article/image
				path = file.substring(file.indexOf('/') + 1);
			}
			else if (file.contains("/")) {
				// image tag uses article/image
				path = file;
			}
			else {
				// image tag uses only image name, relative to current article
				path = title + "/" + file;
			}
			WikiConnector connector = Environment.getInstance().getWikiConnector();
			WikiAttachment attachment = connector.getAttachment(path);

			Dimension dim = getImageDimension(section, attachment);
			try (InputStream stream = attachment.getInputStream()) {
				XWPFRun run = manager.getNewParagraph(Style.image).createRun();
				MyXWPFRun.addPicture(
						run, stream, getFormat(path), file,
						Units.toEMU(dim.width),
						Units.toEMU(dim.height));
			}

			String caption = attr(section, "caption");
			if (!Strings.isBlank(caption)) {
				XWPFRun run = manager.getNewParagraph(Style.caption).createRun();
				run.setText(caption);
			}
		}
		catch (InvalidFormatException e) {
			throw new ExportException("Image has invalid format: " + file, e);
		}
		catch (Exception e) {
			throw new ExportException("Could not load image: " + file, e);
		}
	}

	private Dimension getImageDimension(Section<PluginType> section, WikiAttachment attachment) {
		// use approx. document client width / height
		final int maxW = 450;
		final int maxH = 600;

		boolean hasW = attr(section, "width") != null;
		boolean hasH = attr(section, "height") != null;

		int w = hasW ? intAttr(section, "width", -1000) : 0;
		int h = hasH ? intAttr(section, "height", -1000) : 0;

		// read dimension or use default dimension in 4:3 ratio
		Dimension dim = readImageDimension(attachment);
		if (dim == null) dim = new Dimension(640, 480);

		// if size is negative, scale image as per-mille
		// if size is not specified, leave unchanged
		// is size is positive, scale compared to real size
		float fx = (w < 0) ? -w / 1000f : (w == 0) ? 1f : w / (float) dim.width;
		float fy = (h < 0) ? -h / 1000f : (h == 0) ? 1f : h / (float) dim.height;

		// if only one attribute has been specified,
		// use same scale factor for both directions
		if (!hasW) fx = fy;
		if (!hasH) fy = fx;

		// scale image by detected scale
		w = Math.round(dim.width * fx);
		h = Math.round(dim.height * fy);

		// make image smaller if exceeds page width or height
		// but keep the aspect ratio!
		if (w > maxW) {
			h = Math.round(h / (w / (float) maxW));
			w = maxW;
		}
		if (h > maxH) {
			w = Math.round(w / (h / (float) maxH));
			h = maxH;
		}
		return new Dimension(w, h);
	}

	private int getFormat(String path) throws InvalidFormatException {
		path = path.toLowerCase();
		if (path.endsWith(".emf")) {
			return XWPFDocument.PICTURE_TYPE_EMF;
		}
		else if (path.endsWith(".wmf")) {
			return XWPFDocument.PICTURE_TYPE_WMF;
		}
		else if (path.endsWith(".pict")) {
			return XWPFDocument.PICTURE_TYPE_PICT;
		}
		else if (path.endsWith(".jpeg") || path.endsWith(".jpg")) {
			return XWPFDocument.PICTURE_TYPE_JPEG;
		}
		else if (path.endsWith(".png")) {
			return XWPFDocument.PICTURE_TYPE_PNG;
		}
		else if (path.endsWith(".dib")) {
			return XWPFDocument.PICTURE_TYPE_DIB;
		}
		else if (path.endsWith(".gif")) {
			return XWPFDocument.PICTURE_TYPE_GIF;
		}
		else if (path.endsWith(".tiff")) {
			return XWPFDocument.PICTURE_TYPE_TIFF;
		}
		else if (path.endsWith(".eps")) {
			return XWPFDocument.PICTURE_TYPE_EPS;
		}
		else if (path.endsWith(".bmp")) {
			return XWPFDocument.PICTURE_TYPE_BMP;
		}
		else if (path.endsWith(".wpg")) return XWPFDocument.PICTURE_TYPE_WPG;
		throw new InvalidFormatException("unrecognized format of " + path);
	}

	private int intAttr(Section<PluginType> section, String attribute, int defaultValue) {
		String text = Strings.trim(attr(section, attribute));
		if (Strings.isBlank(text)) return defaultValue;
		boolean percent = text.endsWith("%");
		text = text.replace("%", "").trim();
		float d = Float.valueOf(text);
		return Math.round(percent ? (defaultValue * d / 100) : d);
	}

	private String attr(Section<PluginType> section, String attribute) {
		String text = section.getText();

		// single quotes
		Pattern pattern = Pattern.compile(
				Pattern.quote(attribute) + "\\s*=\\s*'([^']*)'",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) return matcher.group(1);

		// double quotes
		pattern = Pattern.compile(
				Pattern.quote(attribute) + "\\s*=\\s*\"([^\"]*)\"",
				Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(text);
		if (matcher.find()) return matcher.group(1);

		// no quotes
		pattern = Pattern.compile(
				Pattern.quote(attribute) + "\\s*=\\s*([^\\s]*)",
				Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(text);
		if (matcher.find()) return matcher.group(1);

		return null;
	}

	private Dimension readImageDimension(WikiAttachment attachment) {
		try {
			InputStream stream = attachment.getInputStream();
			try {
				BufferedImage bimg = ImageIO.read(stream);
				return new Dimension(bimg.getWidth(), bimg.getHeight());
			}
			finally {
				stream.close();
			}
		}
		catch (IOException e) {
			Log.warning("cannot read image size, using default size", e);
			return null;
		}
	}
}
