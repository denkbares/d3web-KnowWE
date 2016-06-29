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
package org.apache.poi.xwpf.usermodel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlToken;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlip;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObjectData;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualPictureProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;
import org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture;
import org.openxmlformats.schemas.drawingml.x2006.picture.CTPictureNonVisual;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDrawing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;

/**
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 10.02.2014
 */
public class MyXWPFRun extends XWPFRun {

	private MyXWPFRun(CTR r, IRunBody p) {
		super(r, p);
	}

	public static XWPFPicture addPicture(XWPFRun other, InputStream pictureData, int pictureType, String filename, int width, int height)
			throws InvalidFormatException, IOException {
		XWPFDocument doc = other.getDocument();

		// Add the picture + relationship
		String relationId = doc.addPictureData(pictureData, pictureType);
		XWPFPictureData picData = (XWPFPictureData) doc.getRelationById(relationId);

		// Create the drawing entry for it
		try {
			CTDrawing drawing = other.getCTR().addNewDrawing();
			CTInline inline = drawing.addNewInline();

			// Do the fiddly namespace bits on the inline
			// (We need full control of what goes where and as what)
			String xml =
					"<a:graphic xmlns:a=\"" + CTGraphicalObject.type.getName().getNamespaceURI()
							+ "\">" +
							"<a:graphicData uri=\"" + CTPicture.type.getName().getNamespaceURI()
							+ "\">" +
							"<pic:pic xmlns:pic=\"" + CTPicture.type.getName().getNamespaceURI()
							+ "\" />" +
							"</a:graphicData>" +
							"</a:graphic>";
			inline.set(XmlToken.Factory.parse(xml));

			// Setup the inline
			inline.setDistT(0);
			inline.setDistR(0);
			inline.setDistB(0);
			inline.setDistL(0);

			CTNonVisualDrawingProps docPr = inline.addNewDocPr();
			long id = other.getParent().getDocument().getDrawingIdManager().reserveNew();
			docPr.setId(id);
			/* This name is not visible in Word 2010 anywhere. */
			docPr.setName("Drawing " + id);
			docPr.setDescr(filename);

			CTPositiveSize2D extent = inline.addNewExtent();
			extent.setCx(width);
			extent.setCy(height);

			// Grab the picture object
			CTGraphicalObject graphic = inline.getGraphic();
			CTGraphicalObjectData graphicData = graphic.getGraphicData();
			CTPicture pic = getCTPictures(graphicData).get(0);

			// Set it up
			CTPictureNonVisual nvPicPr = pic.addNewNvPicPr();

			CTNonVisualDrawingProps cNvPr = nvPicPr.addNewCNvPr();
			/* use "0" for the id. See ECM-576, 20.2.2.3 */
			cNvPr.setId(0L);
			/* This name is not visible in Word 2010 anywhere */
			cNvPr.setName("Picture " + id);
			cNvPr.setDescr(filename);

			CTNonVisualPictureProperties cNvPicPr = nvPicPr.addNewCNvPicPr();
			cNvPicPr.addNewPicLocks().setNoChangeAspect(true);

			CTBlipFillProperties blipFill = pic.addNewBlipFill();
			CTBlip blip = blipFill.addNewBlip();
			blip.setEmbed(picData.getPackageRelationship().getId());
			blipFill.addNewStretch().addNewFillRect();

			CTShapeProperties spPr = pic.addNewSpPr();
			CTTransform2D xfrm = spPr.addNewXfrm();

			CTPoint2D off = xfrm.addNewOff();
			off.setX(0);
			off.setY(0);

			CTPositiveSize2D ext = xfrm.addNewExt();
			ext.setCx(width);
			ext.setCy(height);

			CTPresetGeometry2D prstGeom = spPr.addNewPrstGeom();
			prstGeom.setPrst(STShapeType.RECT);
			prstGeom.addNewAvLst();

			XmlObject[] pics = graphicData.selectChildren(new QName(
					CTPicture.type.getName().getNamespaceURI(), "pic"));
			pics[0].set(pic);

			// Finish up
			XWPFPicture xwpfPicture = new XWPFPicture(pic, other);
			other.getEmbeddedPictures().add(xwpfPicture);
			return xwpfPicture;
		}
		catch (XmlException e) {
			throw new IllegalStateException(e);
		}
	}

	private static List<CTPicture> getCTPictures(XmlObject o) {
		List<CTPicture> pictures = new ArrayList<>();
		XmlObject[] picts = o.selectPath("declare namespace pic='"
				+ CTPicture.type.getName().getNamespaceURI() + "' .//pic:pic");
		for (XmlObject pict : picts) {
			if (pict instanceof XmlAnyTypeImpl) {
				// Pesky XmlBeans bug - see Bugzilla #49934
				try {
					pict = CTPicture.Factory.parse(pict.toString());
				}
				catch (XmlException e) {
					throw new POIXMLException(e);
				}
			}
			if (pict instanceof CTPicture) {
				pictures.add((CTPicture) pict);
			}
		}
		return pictures;
	}
}
