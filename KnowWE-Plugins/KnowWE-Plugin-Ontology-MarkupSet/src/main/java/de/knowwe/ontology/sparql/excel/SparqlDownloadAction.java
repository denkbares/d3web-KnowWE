/*
 * Copyright (C) 2021 denkbares GmbH, Germany
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
package de.knowwe.ontology.sparql.excel;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import com.denkbares.semanticcore.utils.ResultTableModel;
import com.denkbares.utils.Files;
import com.denkbares.utils.Streams;
import de.knowwe.kdom.renderer.PaginationRenderer;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;

import com.denkbares.semanticcore.CachedTupleQueryResult;
import com.denkbares.semanticcore.utils.IndexedResultTableModel;
import com.denkbares.semanticcore.utils.TableRow;
import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.ontology.sparql.SparqlContentType;
import de.knowwe.ontology.sparql.SparqlMarkupType;
import de.knowwe.ontology.sparql.SparqlResultRenderer;
import de.knowwe.ontology.sparql.SparqlType;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.utils.RenderOptions;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Act
 *
 * @author Stefan Plehn
 * @created 22.03.2013
 */
public class SparqlDownloadAction extends AbstractAction {

	public static final String PARAM_FILENAME = "filename";
	public static final double MAX_COLUMN_WIDTH = 100;

	@Override
	public void execute(UserActionContext context) throws IOException {
		// find query
		Section<?> rootSection = getSection(context);
		Section<SparqlContentType> querySection = Sections.successor(rootSection, SparqlContentType.class);
		if (querySection == null) {
			context.sendError(410, "Query not found, probably the page has been edited while you visiting it. Please reload the page and try again, or contact the administrator if the error persists.");
			return;
		}
		Map<String, Set<Pattern>> filter = PaginationRenderer.getFilter(rootSection, context);
		Set<String> hiddenColumns = PaginationRenderer.getHiddenColumns(rootSection, context);
		Section<SparqlMarkupType> markupSection = Sections.ancestor(querySection, SparqlMarkupType.class);
		if (markupSection == null) {
			context.sendError(404, "Markup section not found, please reload page.");
			return;
		}
		RenderOptions opts = $(markupSection).successor(SparqlType.class)

				.mapFirst(s -> s.get().getRenderOptions(s, context));
		Collection<Rdf2GoCompiler> compilers = Compilers.getCompilers(context, markupSection, Rdf2GoCompiler.class);
		if (!compilers.isEmpty()) {
			Rdf2GoCore core = compilers.iterator().next().getRdf2GoCore();
			String sparql = Rdf2GoUtils.createSparqlString(core, querySection.getText());
			CachedTupleQueryResult resultSet = core.sparqlSelect(sparql);
			File file = getExcelOutputFile(context, filter, hiddenColumns, opts, core, resultSet);
			//no download - 1. request (post)
			if (!Boolean.parseBoolean(context.getParameter("download"))) {
				prepareDownload(context, file);
			}

			//download - 2. request (get)
			if (Boolean.parseBoolean(context.getParameter("download"))) {
				download(context);
			}
		}
	}

	@NotNull
	private static File getExcelOutputFile(UserActionContext context, Map<String, Set<Pattern>> filter, Set<String> hiddenColumns, RenderOptions opts, Rdf2GoCore core, CachedTupleQueryResult resultSet) throws IOException {
		File file = new File(Files.getSystemTempDir(), UUID.randomUUID() + ".xlsx");
		file.deleteOnExit();
		try (OutputStream outputStream = new FileOutputStream(file)) {
			try (XSSFWorkbook workbook = new XSSFWorkbook()) {
				if (Boolean.parseBoolean(context.getParameter("filtered"))) {
					addSparqlResultAsSheet(workbook, resultSet, context, opts, filter, hiddenColumns);
				}
				else {
					addSparqlResultAsSheet(workbook, resultSet, context, opts, Collections.emptyMap(), Collections.emptySet());
				}
				workbook.write(outputStream);
			}
		}
		return file;
	}

	private static void prepareDownload(UserActionContext context, File file) throws IOException {
		if (context.getWriter() != null) {
			context.setContentType(JSON);
			JSONObject response = new JSONObject();
			try {
				response.put("downloadFile", file.getName());
				response.write(context.getWriter());
			}
			catch (JSONException e) {
				throw new IOException(e);
			}
		}
	}

	private static void download(UserActionContext context) throws IOException {
		context.setContentType("application/vnd.ms-excel");
		String fileName = context.getParameter(PARAM_FILENAME);
		if (Boolean.parseBoolean(context.getParameter("filtered"))) {
			fileName = fileName.replace(".xlsx", "_filtered.xlsx");
		}
		context.setHeader("Content-Disposition", "attachment; filename=\""
				+ fileName + "\"");

		File file1 = new File(Files.getSystemTempDir(), context.getParameter("downloadFile"));
		try (FileInputStream inputStream = new FileInputStream(file1); OutputStream outputStream = context.getOutputStream()) {
			Streams.stream(inputStream, outputStream);
		}
		finally {
			file1.delete();
		}
	}

	protected static void addSparqlResultAsSheet(XSSFWorkbook wb, CachedTupleQueryResult qrt, UserContext user, RenderOptions opts, Map<String, Set<Pattern>> filter, Set<String> hiddenColumns) {

		XSSFSheet sheet = wb.createSheet("Result");

		XSSFCellStyle headerStyle = getHeaderStyle(wb);
		XSSFCellStyle cellStyle = getResultCellStyle(wb);

		IndexedResultTableModel tableRows = IndexedResultTableModel.create(qrt);
		ResultTableModel filteredTable = tableRows.filter(filter);
		filteredTable = filteredTable.hideColumns(hiddenColumns);

		List<String> variables = filteredTable.getVariables();
		// create header
		XSSFRow headerRow = sheet.createRow(0);
		for (int i = 0; i < variables.size(); i++) {
			XSSFCell cell = headerRow.createCell(i);
			cell.setCellValue(variables.get(i).replace("_", " "));
			cell.setCellStyle(headerStyle);
		}
		Iterator<TableRow> iterator = filteredTable.iterator();
		int rowNum = 1;
		while (iterator.hasNext()) {
			XSSFRow row = sheet.createRow(rowNum);
			TableRow tableRow = iterator.next();
			for (int i = 0; i < variables.size(); i++) {
				String variable = variables.get(i);
				String result = renderCell(user, opts, variable, tableRow);
				XSSFCell cell = row.createCell(i);
				try {
					cell.setCellValue(Double.parseDouble(result));
				}
				catch (NumberFormatException e) {
					cell.setCellValue(result);
				}
				cell.setCellStyle(cellStyle);
			}
			rowNum++;
		}
		adjustColumnWidth(sheet, variables);
		sheet.createFreezePane(0, 1);
	}

	@NotNull
	private static String renderCell(UserContext user, RenderOptions opts, String variable, TableRow node) {
		RenderResult renderResult = new RenderResult(user);
		SparqlResultRenderer.getInstance().renderNode(node, variable, user, opts, renderResult);
		String result = renderResult.toString();
		// some node renderers my already produce html, so we remove it
		result = Strings.htmlToPlain(result);

		// get rid of all JSPWiki-Markup, just render it to HTML and the remove the html
		result = Environment.getInstance().getWikiConnector().renderWikiSyntax(result, user.getRequest());
		result = Strings.htmlToPlain(result);
		// translate html emojis
		result = StringEscapeUtils.unescapeHtml4(result);
		// other clenaup
		result = result.replace("&nbsp;", " ");
		return result;
	}

	private static void adjustColumnWidth(XSSFSheet sheet, List<String> variables) {
		int maxWidth = (int) (MAX_COLUMN_WIDTH * 256);
		for (int i = 0; i < variables.size(); i++) {
			sheet.autoSizeColumn(i);
			int columnWidth = sheet.getColumnWidth(i);
			if (columnWidth > maxWidth) {
				sheet.setColumnWidth(i, maxWidth);
			}
			else if (columnWidth < 5 * 256) {
				sheet.setColumnWidth(i, 5 * 256);
			}
		}
	}

	private static XSSFCellStyle getResultCellStyle(XSSFWorkbook wb) {
		XSSFCellStyle style = wb.createCellStyle();
		XSSFFont font = wb.createFont();
		font.setFontHeightInPoints((short) 10);
		font.setFontName("Arial");
		font.setBold(false);
		font.setItalic(false);
		style.setFont(font);
//		style.setWrapText(true);
		return style;
	}

	@NotNull
	private static XSSFCellStyle getHeaderStyle(XSSFWorkbook wb) {
		XSSFCellStyle headerStyle = wb.createCellStyle();
		XSSFFont headerFont = wb.createFont();
		headerFont.setBold(true);
		headerFont.setItalic(false);
		headerFont.setFontHeightInPoints((short) 10);
		headerFont.setFontName("Arial");
		headerStyle.setFont(headerFont);
		return headerStyle;
	}
}
