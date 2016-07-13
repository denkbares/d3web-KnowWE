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
package poitest;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.openxml4j.opc.PackageProperties;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import com.denkbares.utils.Exec;
import de.knowwe.include.export.DefaultBuilder;
import de.knowwe.include.export.DocumentBuilder.Style;
import de.knowwe.include.export.ExportManager;
import de.knowwe.include.export.ExportModel;

/**
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 08.02.2014
 */
public class TemplateConvert {

	public static void main(String[] args) throws Exception {
		ExportManager export = new ExportManager(null);
		ExportModel model = new ExportModel(export, ExportManager.createDefaultTemplateStream());
		DefaultBuilder builder = new DefaultBuilder(model);

		XWPFRun run = builder.getParagraph(Style.text).createRun();
		run.setText("Hallo Welt!\n");

		run = builder.getNewParagraph(Style.heading1).createRun();
		run.setText("Hallo Überschrift\n");

		run = builder.getParagraph(Style.text).createRun();
		run.setText("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.\n");

		run = builder.getNewParagraph(Style.heading2).createRun();
		run.setText("Und etwas kleiner\n");

		run = builder.getNewParagraph(Style.heading3).createRun();
		run.setText("Und noch kleiner\n");

		run = builder.getNewParagraph(Style.heading2).createRun();
		run.setText("Und etwas kleiner\n");

		PackageProperties properties = builder.getDocument().getPackage().getPackageProperties();
		properties.setRevisionProperty("13");
		properties.setCreatorProperty("Volker POI");
		properties.setTitleProperty("MMP Qatar Test Document");

		File folder = new File("target/result");
		folder.mkdirs();
		try (FileOutputStream stream = new FileOutputStream(new File(folder, "Test.docx"))) {
			builder.getDocument().write(stream);
		}
		System.out.println("Done.");
		Exec.runSimpleCommand("open Test.docx", folder);
	}
}
