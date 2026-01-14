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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.denkbares.utils.Files;
import com.denkbares.utils.Streams;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.jspwiki.JSPWikiMarkupUtils;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableHeadStart;
import de.knowwe.kdom.table.TableLine;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Action to convert Table markup into XLSX Excel file and download it
 *
 * @author Philipp Sehne
 */
public class TableToExcelAction extends AbstractAction {

	private static XSSFFont normalFont;
	private static XSSFCellStyle bodyStyle;
	private static XSSFFont boldFont;
	private static XSSFFont italicFont;
	private static XSSFFont boldItalicFont;

	@Override
	public void execute(UserActionContext context) throws IOException {
		Section<?> section = getSection(context);

		context.setContentType(
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
		);

		try (OutputStream contextOutputStream = context.getOutputStream()) {
			File file = new File(Files.getSystemTempDir(), UUID.randomUUID() + ".xlsx");
			file.deleteOnExit();
			try (OutputStream outputStream = new FileOutputStream(file)) {
				try (XSSFWorkbook workbook = new XSSFWorkbook()) {
					writeTableToXlsx(workbook, $(section).successor(TableLine.class));
					workbook.write(outputStream);
				}
			}
			context.setHeader("Content-Disposition", "attachment; filename=\"" + JSPWikiMarkupUtils.generateFileNameForSection(section) + ".xlsx\"");
			try {
				try (InputStream inputStream = new FileInputStream(file)) {
					Streams.stream(inputStream, contextOutputStream);
				}
			}
			finally {
				file.delete();
			}
		}

	}




	public static void writeTableToXlsx(XSSFWorkbook workbook, Sections<TableLine> tableLines) {
		XSSFSheet sheet = workbook.createSheet("WikiTable");

		XSSFCellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setWrapText(true);

		// Light grey background
		headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		// Thin borders
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);

		normalFont = createFont(workbook);

		bodyStyle = createBodyStyle(workbook);

		boldFont = createFont(workbook);
		boldFont.setBold(true);

		italicFont = createFont(workbook);
		italicFont.setItalic(true);

		boldItalicFont = createFont(workbook);
		boldItalicFont.setBold(true);
		boldItalicFont.setItalic(true);

		List<Section<TableLine>> tableLineList = tableLines.stream().toList();

		for (int rowIndex = 0; rowIndex < tableLineList.size(); rowIndex++) {
			XSSFRow row = sheet.createRow(rowIndex);
			List<Section<TableCellContent>> cells = $(tableLineList.get(rowIndex)).successor(TableCellContent.class).stream().toList();


			for (int colIndex = 0; colIndex < cells.size(); colIndex++) {
				XSSFCell cell = row.createCell(colIndex);
				String cellText = cells.get(colIndex).getText();
				Section<TableHeadStart> tableHead = $(cells.get(colIndex)).ancestor(Type.class).successor(TableHeadStart.class).getFirst();
				if (tableHead != null) {
					// Header row
					applyWikiRichText(
							cell,
							cellText,
							true
					);
					cell.setCellStyle(headerStyle);
				} else {
					applyWikiRichText(
							cell,
							cellText,
							false
					);
					cell.setCellStyle(bodyStyle); // borders, wrap, background
				}
			}
		}


		// Auto-size columns
		if (!tableLineList.isEmpty()) {
			int columnCount = $(tableLineList.get(0)).successor(TableCellContent.class).stream().toList().size();
			for (int i = 0; i < columnCount; i++) {
				sheet.autoSizeColumn(i);
			}
		}

		sheet.createFreezePane(0, 1);
	}

	private static void applyWikiRichText(
			XSSFCell cell,
			String raw,
			boolean isHeader) {

		raw = normalizeWikiLineBreaks(raw);

		if (raw.isEmpty()) {
			cell.setCellValue("");
			return;
		}

		XSSFRichTextString rich = new XSSFRichTextString();

		boolean bold = isHeader;
		boolean italic = false;

		StringBuilder buffer = new StringBuilder();
		XSSFFont currentFont = resolveFont(bold, italic);

		for (int i = 0; i < raw.length(); ) {

			// Bold toggle
			if (raw.startsWith("__", i)) {
				flush(buffer, rich, currentFont);
				bold = !bold;
				currentFont = resolveFont(isHeader || bold, italic);
				i += 2;
				continue;
			}

			// Italic toggle
			if (raw.startsWith("''", i)) {
				flush(buffer, rich, currentFont);
				italic = !italic;
				currentFont = resolveFont(isHeader || bold, italic);
				i += 2;
				continue;
			}

			buffer.append(raw.charAt(i++));
		}

		flush(buffer, rich, currentFont);
		cell.setCellValue(rich);
	}


	private static void flush(StringBuilder buffer, XSSFRichTextString rich, XSSFFont font) {
		if (!buffer.isEmpty()) {
			rich.append(buffer.toString(), font);
			buffer.setLength(0);
		}
	}

	private static XSSFFont resolveFont(
			boolean bold,
			boolean italic) {

		if (bold && italic) return boldItalicFont;
		if (bold) return boldFont;
		if (italic) return italicFont;
		return normalFont;
	}



	private static XSSFCellStyle createBodyStyle(XSSFWorkbook workbook) {
		XSSFCellStyle boldBodyStyle = workbook.createCellStyle();
		boldBodyStyle.setWrapText(true);

		boldBodyStyle.setBorderTop(BorderStyle.THIN);
		boldBodyStyle.setBorderBottom(BorderStyle.THIN);
		boldBodyStyle.setBorderLeft(BorderStyle.THIN);
		boldBodyStyle.setBorderRight(BorderStyle.THIN);
		return boldBodyStyle;
	}

	private static XSSFFont createFont(XSSFWorkbook workbook) {
		XSSFFont font = workbook.createFont();
		font.setFontHeightInPoints((short) 11);
		return font;
	}

	private static String normalizeWikiLineBreaks(String raw) {
		if (raw == null) return "";

		String trimmed = raw.trim();

		if (trimmed.equals("\\\\")) {
			return "";
		}

		return raw.replace("\\\\", "\n");
	}

}
