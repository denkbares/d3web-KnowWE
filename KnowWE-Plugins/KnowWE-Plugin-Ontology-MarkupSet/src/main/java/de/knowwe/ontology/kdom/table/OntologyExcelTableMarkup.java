package de.knowwe.ontology.kdom.table;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.QuoteSet;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import com.denkbares.utils.Pair;
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Types;
import de.knowwe.core.kdom.basicType.AttachmentType;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.objects.SimpleReferenceRegistrationScript;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.sectionFinder.SplitSectionFinderUnquoted;
import de.knowwe.kdom.table.Table;
import de.knowwe.ontology.compile.OntologyCompileScript;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.kdom.OntologyUtils;
import de.knowwe.ontology.kdom.resource.AbbreviatedResourceReference;
import de.knowwe.ontology.turtle.TurtleLiteralType;
import de.knowwe.rdf2go.Rdf2GoCore;

public class OntologyExcelTableMarkup extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	private static final String CONFIG_KEY = "readLinesConfig";
	private static final String WORKBOOK_CACHE_KEY = "workbookCacheKey";
	private static final String COUNT_KEY = "countKey";
	private static final String ANNOTATION_CONFIG = "config";
	private static final String ANNOTATION_XLSX = "xlsx";
	private static final Pattern COLUMN_PATTERN = Pattern.compile("\\$column:?\\s*(\\w+)|\\$\\{column:?\\s*(\\w+)}", Pattern.CASE_INSENSITIVE);
	private static final String ROW_VARIABLE = "$row";
	private static final String ROW_VARIABLE2 = "${row}";
	private static final String URL_ROW_VARIABLE = Strings.encodeURL(ROW_VARIABLE);
	private static final String URL_ROW_VARIABLE2 = Strings.encodeURL(ROW_VARIABLE2);

	static {
		MARKUP = new DefaultMarkup("OntologyExcelTable");
		Table content = new Table();
		MARKUP.addContentType(content);
		PackageManager.addPackageAnnotation(MARKUP);

		MARKUP.addAnnotation(ANNOTATION_CONFIG, true);
		MARKUP.addAnnotationContentType(ANNOTATION_CONFIG, new ConfigAnnotationType());
		String documentation = "Some examples:\n"
				+ "@config: Sheet 1-3, Rows 5+, Subject $columnA, Predicate rdf:type, Object lns:ImportedRow\n"
				+ "@config: Sheet 1-3, Rows 5+, Subject $columnA, Predicate lns:hasRowNumber, Object \"$row\"^^xsd:int\n"
				+ "@config: Sheet 1-3, Rows 5+, Subject ${column A}, Predicate skos:prefLabel, Object \"${column C}\"@en\n"
				+ "@config: Sheet 4, Rows 5-120, Subject $column1, Predicate $column4, Object $column5";
		MARKUP.getAnnotation(ANNOTATION_CONFIG).setDocumentation(documentation);
		MARKUP.addAnnotation(ANNOTATION_XLSX, true);
		MARKUP.addAnnotationContentType(ANNOTATION_XLSX, new AttachmentType());
	}

	public OntologyExcelTableMarkup() {
		super(MARKUP);
	}

	@NotNull
	private static String getMessage(int statementCounter, int rowCounter) {
		return "Generated " + Strings.pluralOf(statementCounter, "statement")
				+ " from " + Strings.pluralOf(rowCounter, "row");
	}

	private static class ConfigAnnotationType extends AbstractType {

		public ConfigAnnotationType() {
			setSectionFinder(AllTextFinderTrimmed.getInstance());
			addCompileScript(new ConfigParseScript());
			SplitSectionFinderUnquoted splitSectionFinderUnquoted = new SplitSectionFinderUnquoted(",", QuoteSet.TRIPLE_QUOTES, new QuoteSet('"'));
			AnonymousType split = new AnonymousType("Split", splitSectionFinderUnquoted);
			addChildType(split);

			split.addChildType(new SheetType());
			split.addChildType(new RowsType());
			split.addChildType(new SkipType());
			split.addChildType(new XlsxSubjectType());
			split.addChildType(new XlsxPredicateType());
			split.addChildType(new XlsxObjectType());

			addCompileScript(new StatementCompileScript());

			setRenderer((section, user, result) -> {
				OntologyCompiler compiler = Compilers.getCompiler(section, OntologyCompiler.class);
				DelegateRenderer.getInstance().render(section, user, result);
				Pair<Integer, Integer> counts = getCounts(section, compiler);
				if (counts != null) {
					result.appendHtmlElement("span",
							" // " + getMessage(counts.getA(), counts.getB()),
							"style", "color: grey");
				}
			});
		}

		private Pair<Integer, Integer> getCounts(Section<?> section, OntologyCompiler compiler) {
			//noinspection unchecked
			return (Pair<Integer, Integer>) section.getObject(compiler, COUNT_KEY);
		}
	}

	private static class StatementCompileScript extends OntologyCompileScript<ConfigAnnotationType> {

		@Override
		public void compile(OntologyCompiler compiler, Section<ConfigAnnotationType> section) throws CompilerMessage {

			Stopwatch stopwatch = new Stopwatch();
			if (Messages.hasMessagesInSubtree(section, Message.Type.ERROR)) return;

			XSSFWorkbook xlsx = getXlsx(section);
			Config config = getConfig(section);
			Rdf2GoCore core = compiler.getRdf2GoCore();

			// get static IRIs
			final IRI subjectIriFromConfig = getUriFromConfig(core, config.subject);
			IRI subjectIRI = subjectIriFromConfig;
			final IRI predicateIriFromConfig = getUriFromConfig(core, config.predicate);
			IRI predicateIRI = predicateIriFromConfig;

			List<IRI> objectFromConfigIris = new ArrayList<>();
			List<String> objectFromConfigIriStrings = new ArrayList<>();
			List<Literal> literalsFromConfig = new ArrayList<>();
			for (ObjectConfig objectConfig : config.objectConfigs) {
				final IRI uriFromConfig = getUriFromConfig(core, objectConfig.object);
				objectFromConfigIris.add(uriFromConfig);
				objectFromConfigIriStrings.add(uriFromConfig == null ? null : uriFromConfig.toString());
				literalsFromConfig.add(objectConfig.objectLiteral == null ?
						null : objectConfig.objectLiteral.get().getLiteral(core, objectConfig.objectLiteral));
			}

			int statementCounter = 0;
			int rowCounter = 0;

			// fill variable IRIs and Literals from excel
			for (XSSFSheet sheet : getSheets(xlsx, config)) {
				// config indexes are 1-based indexes, so subtract 1 to get to 0-based indexes for poi
				for (int i = config.startRow - 1; i <= Math.min(config.endRow - 1, sheet.getLastRowNum()); i++) {
					try {
						XSSFRow row = sheet.getRow(i);

						//cell which is tested when using skipRowType
						if (config.skipColumn != -1) {
							String skipTestCell = getCellValue(row.getCell(config.skipColumn - 1));
							if (config.skipPattern != null && skipTestCell != null && skipTestCell.matches(config.skipPattern)) {
								continue;
							}
						}

						// subject
						if (config.subjectColumn > 0) { // read from excel
							subjectIRI = getUriFromCell(core, row, config.subjectColumn);
						}
						else if (subjectIriFromConfig != null) { // read from config
							subjectIRI = core.createIRI(replaceRowInUri(i, subjectIriFromConfig.toString()));
						}

						// predicate
						if (config.predicateColumn > 0) { // read from excel
							predicateIRI = getUriFromCell(core, row, config.predicateColumn);
						}
						else if (predicateIriFromConfig != null) { // read from config
							predicateIRI = core.createIRI(replaceRowInUri(i, predicateIriFromConfig.toString()));
						}

						// objects
						for (int j = 0; j < config.objectConfigs.size(); j++) {
							ObjectConfig objectConfig = config.objectConfigs.get(j);
							IRI objectIRI = objectFromConfigIris.get(j);
							String objectIriString = objectFromConfigIriStrings.get(j);
							Literal literal = literalsFromConfig.get(j);
							if (objectConfig.objectColumn > 0) { // read from excel
								if (literal == null) {
									objectIRI = getUriFromCell(core, row, objectConfig.objectColumn);
								}
								else {
									literal = getLiteralFromCell(core, row, objectConfig, literal);
								}
							}
							else { // read from config
								if (objectIriString != null) {
									objectIRI = core.createIRI(replaceRowInUri(i, objectIriString));
								}
								else if (literal != null) {
									final Optional<String> language = literal.getLanguage();
									if (language.isPresent()) {
										literal = core.createLanguageTaggedLiteral(
												replaceRow(i, literal.getLabel()),
												language.get());
									}
									else {
										literal = core.createDatatypeLiteral(
												replaceRow(i, literal.getLabel()),
												literal.getDatatype());
									}
								}
							}

							// create and add statements
							Statement statement = null;
							if (literal != null) {
								statement = core.createStatement(subjectIRI, predicateIRI, literal);
							}
							else if (objectIRI != null) {
								statement = core.createStatement(subjectIRI, predicateIRI, objectIRI);
							}
							if (statement != null) {
								core.addStatements(section, statement);
								statementCounter++;
							}
						}
					}
					catch (Exception e) {
						String message = "Exception in row " + (i + 1) + ": " + e.getMessage();
						Log.severe(message, e);
						throw CompilerMessage.error(message);
					}
					rowCounter++;
				}
			}
			section.storeObject(compiler, COUNT_KEY, new Pair<>(statementCounter, rowCounter));
			stopwatch.log(getMessage(statementCounter, rowCounter));
		}

		private static XSSFWorkbook getXlsx(Section<ConfigAnnotationType> section) throws CompilerMessage {
			Section<OntologyExcelTableMarkup> markupSection = Sections.ancestor(section, OntologyExcelTableMarkup.class);
			if (markupSection == null) throw CompilerMessage.error("Unable to get markup section"); // should not happen

			//noinspection unchecked
			SoftReference<XSSFWorkbook> workbookSoftReference = (SoftReference<XSSFWorkbook>) markupSection.getObject(WORKBOOK_CACHE_KEY);
			if (workbookSoftReference != null) {
				XSSFWorkbook workbook = workbookSoftReference.get();
				if (workbook != null) return workbook;
			}

			Section<? extends AnnotationContentType> contentSection = DefaultMarkupType.getAnnotationContentSection(markupSection, ANNOTATION_XLSX);
			try {
				WikiAttachment attachment = AttachmentType.getAttachment(Sections.successor(contentSection, AttachmentType.class));
				if (attachment == null) throw CompilerMessage.error("Attachment specified at " + ANNOTATION_XLSX + " not found");
				XSSFWorkbook workbook = new XSSFWorkbook(attachment.getInputStream());
				markupSection.storeObject(WORKBOOK_CACHE_KEY, new SoftReference<>(workbook));
				return workbook;
			}
			catch (Exception e) {
				String message = "Exception while trying to access attached XLSX file";
				throw CompilerMessage.error(message);
			}
		}

		@Override
		public void destroy(OntologyCompiler compiler, Section<ConfigAnnotationType> section) {
			compiler.getRdf2GoCore().removeStatements(section);
		}

		private Literal getLiteralFromCell(Rdf2GoCore core, XSSFRow row, ObjectConfig config, Literal literal) {
			XSSFCell cell = row.getCell(config.objectColumn - 1);
			if (cell == null) return null;
			if (literal.getLanguage().isPresent()) {
				literal = core.createLanguageTaggedLiteral(getCellValue(cell), literal.getLanguage().get());
			}
			else if (literal.getDatatype() != null) {
				literal = core.createDatatypeLiteral(getCellValue(cell), literal.getDatatype());
			}
			return literal;
		}

		@Nullable
		private IRI getUriFromConfig(Rdf2GoCore core, Section<AbbreviatedResourceReference> resource) {
			IRI uri = null;
			if (resource != null) {
				uri = resource.get().getResourceIRI(core, resource);
			}
			return uri;
		}

		private IRI getUriFromCell(Rdf2GoCore core, XSSFRow row, int subjectColumn) {
			XSSFCell cell = row.getCell(subjectColumn - 1);
			if (cell == null) return null;
			return core.createIRI(getCellValue(cell));
		}

		@NotNull
		private List<XSSFSheet> getSheets(XSSFWorkbook xlsx, Config config) {
			List<XSSFSheet> sheets = new ArrayList<>();
			if (config.sheets == null) {
				for (int i = 0; i < xlsx.getNumberOfSheets(); i++) {
					sheets.add(xlsx.getSheetAt(i));
				}
			}
			else {
				for (Integer sheetIndex : config.sheets) {
					sheets.add(xlsx.getSheetAt(sheetIndex - 1));
				}
			}
			return sheets;
		}

		private Config getConfig(Section<ConfigAnnotationType> section) {
			return (Config) section.getObject(CONFIG_KEY);
		}
	}

	@NotNull
	private static String replaceRowInUri(int i, String objectIriString) {
		return objectIriString.replace(URL_ROW_VARIABLE, String.valueOf(i))
				.replace(URL_ROW_VARIABLE2, String.valueOf(i));
	}

	@NotNull
	private static String replaceRow(int i, String label) {
		return label.replace(ROW_VARIABLE, String.valueOf(i)).replace(ROW_VARIABLE2, String.valueOf(i));
	}

	/**
	 * returns the value of a XSSFCell as String no matter what Type the cell is, null if the given cell is null
	 */
	private static String getCellValue(XSSFCell cell) {
		if (cell != null) {
			switch (cell.getCellType()) {
				case STRING:
					return cell.getStringCellValue();

				case NUMERIC:
					if (DateUtil.isCellDateFormatted(cell)) {
						return cell.getDateCellValue().toString();
					}
					else {
						return Double.toString(cell.getNumericCellValue());
					}

				case BOOLEAN:
					return Boolean.toString(cell.getBooleanCellValue());

				case FORMULA:
					return cell.getCellFormula();

				default:
					return cell.toString();
			}
		}
		else {
			return null;
		}
	}

	private static class ConfigParseScript extends DefaultGlobalCompiler.DefaultGlobalScript<ConfigAnnotationType> {

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<ConfigAnnotationType> section) throws CompilerMessage {
			Config config = new Config();
			addSheets(section, config);
			addRows(section, config);
			skipRows(section, config);
			addSubject(section, config);
			addPredicate(section, config);
			addObjects(section, config);
			section.storeObject(CONFIG_KEY, config);
		}

		private void skipRows(Section<ConfigAnnotationType> section, Config config) {
			Section<SkipType> rows = Sections.successor(section, SkipType.class);
			if (rows == null) {
				config.skipPattern = null;
			}
			else {
				rows.get().skipRows(rows, config);
			}
		}

		private void addRows(Section<ConfigAnnotationType> section, Config config) throws CompilerMessage {
			Section<RowsType> rows = Sections.successor(section, RowsType.class);
			if (rows == null) {
				config.startRow = 2;
				config.endRow = Integer.MAX_VALUE;
			}
			else {
				rows.get().addRows(rows, config);
			}
		}

		private void addSheets(Section<ConfigAnnotationType> section, Config config) {
			Section<SheetType> sheets = Sections.successor(section, SheetType.class);
			if (sheets == null) {
				config.sheets = null;
			}
			else {
				sheets.get().addSheets(sheets, config);
			}
		}

		private void addSubject(Section<ConfigAnnotationType> section, Config config) throws CompilerMessage {
			Section<XlsxSubjectType> xlsxSubject = Sections.successor(section, XlsxSubjectType.class);
			if (xlsxSubject == null) {
				throw CompilerMessage.error("Subject not defined. Please specify subject, e.g. 'Subject $Column2' or 'Subject ns:mySubject'");
			}
			else {
				xlsxSubject.get().addSubject(xlsxSubject, config);
			}
		}

		private void addPredicate(Section<ConfigAnnotationType> section, Config config) throws CompilerMessage {
			Section<XlsxPredicateType> xlsxPredicate = Sections.successor(section, XlsxPredicateType.class);
			if (xlsxPredicate == null) {
				throw CompilerMessage.error("Predicate not defined. Please specify predicate, e.g. 'Predicate $Column3' or 'Predicate ns:myPredicate'");
			}
			else {
				xlsxPredicate.get().addPredicate(xlsxPredicate, config);
			}
		}

		private void addObjects(Section<ConfigAnnotationType> section, Config config) throws CompilerMessage {
			List<Section<XlsxObjectType>> xlsxObjects = Sections.successors(section, XlsxObjectType.class);
			if (xlsxObjects.isEmpty()) {
				throw CompilerMessage.error("Object not defined. Please specify one or more objects, " +
						"e.g. 'Object $Column3' or 'Object ns:myPredicate' or 'Object \"$Column5\"@en'.");
			}
			else {
				for (Section<XlsxObjectType> object : xlsxObjects) {
					object.get().addObjects(object, config);
				}
			}
		}
	}

	private static class SheetType extends AbstractType {

		public static final String SHEET_PATTERN = "(\\d+)(?:-(\\d+))?";

		public SheetType() {
			setSectionFinder(new RegexSectionFinder(
					"\\s*(?:sheets?|tabellen?|arbeitsblatt|arbeitsblätter):?\\s*(" + SHEET_PATTERN + ")\\s*",
					Pattern.CASE_INSENSITIVE, 1));
		}

		public void addSheets(Section<SheetType> section, Config config) {
			Pattern sheetsPattern = Pattern.compile(SHEET_PATTERN);
			Matcher sheetsMatcher = sheetsPattern.matcher(section.getText());
			if (sheetsMatcher.find()) {
				int start = Integer.parseInt(sheetsMatcher.group(1));
				String endGroup = sheetsMatcher.group(2);
				int end = endGroup == null ? start + 1 : Integer.parseInt(endGroup);
				for (int i = start; i < end; i++) {
					config.sheets.add(i);
				}
			}
		}
	}

	private static class SkipType extends AbstractType {
		private static final Pattern MATCHING_PATTERN = Pattern.compile("(?:matching):?\\s*(\"?.*\"?)");

		public SkipType() {
			setSectionFinder(new RegexSectionFinder(
					"\\s*skip:?\\s+(.+)", Pattern.CASE_INSENSITIVE, 0));
		}

		public void skipRows(Section<SkipType> section, Config config) {
			Matcher columnMatcher = COLUMN_PATTERN.matcher(section.getText());
			Matcher matchingMatcher = MATCHING_PATTERN.matcher(section.getText());

			if (columnMatcher.find()) {
				config.skipColumn = getColumnNumber(columnMatcher);
			}
			if (matchingMatcher.find()) {
				config.skipPattern = Strings.unquote(matchingMatcher.group(1));
			}
		}
	}

	private static class RowsType extends AbstractType {

		public static final String ROW_PATTERN = "(\\d+)(\\+)?(?:-(\\d+))?";

		public RowsType() {
			setSectionFinder(new RegexSectionFinder(
					"\\s*(?:rows?|reihen?|zeilen?):?\\s*(" + ROW_PATTERN + ")\\s*",
					Pattern.CASE_INSENSITIVE, 1));
		}

		private void addRows(Section<RowsType> section, Config config) throws CompilerMessage {
			Pattern rowsPattern = Pattern.compile(ROW_PATTERN);
			Matcher rowsMatcher = rowsPattern.matcher(section.getText());
			if (rowsMatcher.find()) {
				config.startRow = Integer.parseInt(rowsMatcher.group(1));
				boolean all = rowsMatcher.group(2) != null;
				String endGroup = rowsMatcher.group(3);
				if (all && endGroup == null) {
					config.endRow = Integer.MAX_VALUE;
				}
				else if (all) {
					throw CompilerMessage.error("Invalid row pattern. Please use either e.g. 3+ for all available "
							+ "rows starting with row 3, or e.g. 3-100 for all rows from 3 to 100, both including");
				}
				else if (endGroup != null) {
					config.endRow = Integer.parseInt(endGroup);
				}
			}
		}
	}

	private static class XlsxSubjectType extends AbstractType {

		public XlsxSubjectType() {
			setSectionFinder(new RegexSectionFinder(
					"\\s*subje[ck]t:?\\s+(.+)",
					Pattern.CASE_INSENSITIVE, 1));
			addChildType(new XlsxTableResourceReference());
		}

		public void addSubject(Section<XlsxSubjectType> xlsxSubject, Config config) throws CompilerMessage {
			Section<AbbreviatedResourceReference> subject = Sections.child(xlsxSubject, AbbreviatedResourceReference.class);
			if (subject != null) {
				config.subject = subject;
			}
			else {
				Matcher matcher = COLUMN_PATTERN.matcher(xlsxSubject.getText());
				if (matcher.find()) {
					config.subjectColumn = getColumnNumber(matcher);
				}
				else {
					throw CompilerMessage.error("Invalid subject. Either give an abbreviated uri, " +
							"e.g. ns:mySubject or an column, e.g. $column1");
				}
			}
		}
	}

	private static class XlsxPredicateType extends AbstractType {

		public XlsxPredicateType() {
			setSectionFinder(new RegexSectionFinder(
					"\\s*predi[ck]ate?:?\\s+(.+)*",
					Pattern.CASE_INSENSITIVE, 1));
			addChildType(new XlsxTableResourceReference());
		}

		public void addPredicate(Section<XlsxPredicateType> xlsxSubject, Config config) throws CompilerMessage {
			Section<AbbreviatedResourceReference> predicate = Sections.child(xlsxSubject, AbbreviatedResourceReference.class);
			if (predicate != null) {
				config.predicate = predicate;
			}
			else {
				Matcher matcher = COLUMN_PATTERN.matcher(xlsxSubject.getText());
				if (matcher.find()) {
					config.predicateColumn = getColumnNumber(matcher);
				}
				else {
					throw CompilerMessage.error("Invalid predicate. Either give an abbreviated uri, e.g. ns:myPredicate or an column, e.g. $column1");
				}
			}
		}
	}

	private static class XlsxObjectType extends AbstractType {

		public XlsxObjectType() {
			setSectionFinder(new RegexSectionFinder(
					"\\s*obje[ck]t:?\\s+(.+)",
					Pattern.CASE_INSENSITIVE, 1));
			addChildType(new TurtleLiteralType());
			addChildType(new XlsxTableResourceReference());
		}

		public void addObjects(Section<XlsxObjectType> xlsxObject, Config config) {
			ObjectConfig objectConfig = new ObjectConfig();
			Section<AbbreviatedResourceReference> object = Sections.child(xlsxObject, AbbreviatedResourceReference.class);
			Section<TurtleLiteralType> literal = Sections.child(xlsxObject, TurtleLiteralType.class);
			if (object != null) {
				objectConfig.object = object;
			}
			else if (literal != null) {
				objectConfig.objectLiteral = literal;
				Section<TurtleLiteralType.LiteralPart> content = Sections.child(literal, TurtleLiteralType.LiteralPart.class);
				if (content != null) {
					setObjectColumn(content.getText(), objectConfig);
				}
			}
			else {
				String text = xlsxObject.getText();
				setObjectColumn(text, objectConfig);
			}
			config.objectConfigs.add(objectConfig);
		}

		private void setObjectColumn(String text, ObjectConfig config) {
			Matcher matcher = COLUMN_PATTERN.matcher(text);
			if (matcher.find()) {
				config.objectColumn = getColumnNumber(matcher);
			}
		}
	}

	private static int getColumnNumber(Matcher columnMatcher) {
		String columnIdentifier = columnMatcher.group(1);
		if (columnIdentifier == null) {
			columnIdentifier = columnMatcher.group(2);
		}
		try {
			return Integer.parseInt(columnIdentifier);
		}
		catch (NumberFormatException e) {
			return CellReference.convertColStringToIndex(columnIdentifier) + 1;
		}
	}

	private static class XlsxTableResourceReference extends AbbreviatedResourceReference {

		public XlsxTableResourceReference() {
			for (SimpleReference reference : Types.successors(this, SimpleReference.class)) {
				//noinspection unchecked
				reference.removeCompileScript(OntologyCompiler.class, SimpleReferenceRegistrationScript.class);
				reference.addCompileScript(Priority.LOW, new SimpleReferenceRegistrationScript<>(OntologyCompiler.class, false));
			}
			setSectionFinder(OntologyUtils.ABBREVIATED_NS_RESOURCE_FINDER);
		}
	}

	private static class Config {

		public List<Integer> sheets = new ArrayList<>();
		public int startRow;
		public int endRow;
		public int subjectColumn;
		public int predicateColumn;
		public String skipPattern;
		public int skipColumn = -1;
		public Section<AbbreviatedResourceReference> subject;
		public Section<AbbreviatedResourceReference> predicate;
		public List<ObjectConfig> objectConfigs = new ArrayList<>();
	}

	private static class ObjectConfig {
		public int objectColumn;
		public Section<AbbreviatedResourceReference> object;
		public Section<TurtleLiteralType> objectLiteral;
	}
}
