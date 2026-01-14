/*
 * Copyright (C) 2025 denkbares GmbH, Germany
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

package de.knowwe.kdom.table.excel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
/**
 * Action to convert XLSX Excel files into valid Table markup text
 *
 * @author Philipp Sehne
 */
public class ExcelToTableAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
		try {
			// find file among all fields of the multipart form request
			List<FileItem> potentialFiles = upload.parseRequest(context.getRequest());
			var fileOptional = potentialFiles.stream().filter(field -> !field.isFormField()).findFirst();
			fileOptional.ifPresent(file -> {
				// check file
				boolean hasXLSXExtension = file.getName() != null && file.getName().toLowerCase().endsWith(".xlsx");
				if (!hasXLSXExtension) throw new RuntimeException("File has an invalid format");
				if (file == null || file.getSize() == 0) {
					throw new IllegalArgumentException("Uploaded file is empty or null");
				}

				try (InputStream is = file.getInputStream()) {
					// Apache POI automatically detects XLSX format for XSSFWorkbook
					XSSFWorkbook workbook = new XSSFWorkbook(is);
					String markupText = xlsxToWikiTable(workbook);
					Section<?> section = getSection(context);
					Sections.replace(context, section.getID(), markupText);

				} catch (Exception e) {
					throw new RuntimeException(e);
				}

			});
		}
		catch (Exception e) {
			context.sendError(500, "Error while uploading a snapshot: " + e.getMessage());
		}
	}

	public static String xlsxToWikiTable(XSSFWorkbook workbook) {
		XSSFSheet sheet = workbook.getSheetAt(0);
		StringBuilder wiki = new StringBuilder();

		wiki.append("%%Table\n");

		// 1️⃣ Determine true max column count (unchanged, but safe)
		int columnCount = 0;
		for (Row row : sheet) {
			if (row != null && row.getLastCellNum() > columnCount) {
				columnCount = row.getLastCellNum();
			}
		}

		if (columnCount <= 0) {
			return ""; // empty sheet
		}

		// 2️⃣ Iterate by row INDEX (this is the critical fix)
		int firstRow = sheet.getFirstRowNum();
		int lastRow = sheet.getLastRowNum();

		for (int r = firstRow; r <= lastRow; r++) {
			Row row = sheet.getRow(r);
			boolean headerRow = (r == 0);

			// 3️⃣ Handle completely empty rows
			if (row == null || row.getLastCellNum() == -1) {
				for (int c = 0; c < columnCount; c++) {
					wiki.append("|  ");
				}
				wiki.append("\n");
				continue;
			}

			// 4️⃣ Normal row
			for (int col = 0; col < columnCount; col++) {
				Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

				boolean headerCell = headerRow || hasBackground(cell);
				wiki.append(headerCell ? "|| " : "| ");

				String cellText = (cell instanceof XSSFCell x)
						? cellToWiki(x)
						: "";
				wiki.append(cellText).append(" ");
			}

			wiki.append("\n");
		}

		wiki.append("%\n");
		return wiki.toString();
	}

	private static String cellToWiki(XSSFCell cell) {
		if (cell == null) return "";

		if (cell.getCellType() != CellType.STRING) {
			return cell.toString();
		}

		XSSFRichTextString rich = cell.getRichStringCellValue();
		if (rich == null) {
			return cell.getStringCellValue();
		}

		StringBuilder wiki = new StringBuilder();

		int runCount = rich.numFormattingRuns();
		if (runCount == 0) {
			// Convert Excel line breaks back to wiki markup
			return rich.getString().replace("\r\n", "\n").replace("\n", "\\\\");
		}

		for (int i = 0; i < runCount; i++) {
			int start = rich.getIndexOfFormattingRun(i);
			int end = (i + 1 < runCount)
					? rich.getIndexOfFormattingRun(i + 1)
					: rich.length();

			String text = rich.getString().substring(start, end);

			XSSFFont font = rich.getFontOfFormattingRun(i);
			boolean bold = font != null && font.getBold();
			boolean italic = font != null && font.getItalic();

			// Normalize line endings
			text = text.replace("\r\n", "\n");

			// Split by Excel line breaks
			String[] lines = text.split("\n", -1);

			for (int l = 0; l < lines.length; l++) {
				if (!lines[l].isEmpty()) {
					wiki.append(applyWikiMarkup(lines[l], bold, italic));
				}

				// Re-add wiki line break between lines
				if (l < lines.length - 1) {
					wiki.append("\\\\");
				}
			}
		}

		return wiki.toString();
	}


	private static boolean hasBackground(Cell cell) {
		if (cell == null) return false;

		CellStyle style = cell.getCellStyle();
		if (style == null) return false;

		return style.getFillPattern() != FillPatternType.NO_FILL;
	}


	private static String applyWikiMarkup(String text, boolean bold, boolean italic) {
		if (text.isEmpty()) return "";

		if (bold && italic) {
			return "__''" + text + "''__";
		}
		if (bold) {
			return "__" + text + "__";
		}
		if (italic) {
			return "''" + text + "''";
		}
		return text;
	}

}
