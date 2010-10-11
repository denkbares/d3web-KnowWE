package de.d3web.we.hermes.kdom.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.hermes.TimeEvent;
import de.d3web.we.hermes.TimeStamp;
import de.d3web.we.hermes.kdom.conceptMining.LocationOccurrence;
import de.d3web.we.hermes.kdom.conceptMining.PersonOccurrence;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.constraint.ConstraintSectionFinder;
import de.d3web.we.kdom.constraint.SingleChildConstraint;
import de.d3web.we.kdom.objects.TermDefinition;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.InvalidNumberError;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotation;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.kdom.type.AnonymousType;
import de.d3web.we.kdom.type.AnonymousTypeInvisible;
import de.d3web.we.kdom.util.SplitUtility;
import de.d3web.we.kdom.util.StringFragment;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TimeEventNew extends DefaultAbstractKnowWEObjectType {

	private static final Description DescriptionType = new Description();
	private static final Source SourceType = new Source();
	public static final String START_TAG = "<<";
	public static final String END_TAG = ">>";

	public TimeEventNew() {

		this.sectionFinder = new RegexSectionFinder("\\r?\\n(" + START_TAG
				+ ".*?" + END_TAG + ")\\r?\\n", Pattern.DOTALL, 1);

		AnonymousType opening = new AnonymousTypeInvisible("EventStart");
		opening.setSectionFinder(new RegexSectionFinder(START_TAG));
		this.childrenTypes.add(opening);

		AnonymousType closing = new AnonymousTypeInvisible("EventEnd");
		closing.setSectionFinder(new RegexSectionFinder(END_TAG));
		this.childrenTypes.add(closing);

		ImportanceType imp = new ImportanceType();
		this.addChildType(imp);

		TitleType title = new TitleType();
		this.addChildType(title);

		DateType date = new DateType();
		this.addChildType(date);

		this.childrenTypes.add(SourceType);

		this.childrenTypes.add(DescriptionType);

		this.addSubtreeHandler(Priority.LOW, new TimeEventOWLCompiler());
	}

	/**
	 * 
	 * Allows for quick and simple access of the TimeEvent-object which is
	 * created and stored by the TitleType
	 * 
	 * @created 08.10.2010
	 * @param s
	 * @return
	 */
	public static TimeEvent getEvent(Section<TimeEventNew> s) {
		Section<TitleType> titleSection = s.findSuccessor(TitleType.class);
		if (titleSection != null) {
			return titleSection.get().getTermObject(s.getArticle(), titleSection);
		}
		return null;
	}

	private static class DateType extends DefaultAbstractKnowWEObjectType {

		public DateType() {
			this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR6));
			ConstraintSectionFinder cf = new ConstraintSectionFinder(
					new ISectionFinder() {

						@Override
						public List<SectionFinderResult> lookForSections(String text, Section<?> father, KnowWEObjectType type) {
							StringFragment firstNonEmptyLineContent = SplitUtility.getFirstNonEmptyLineContent(text);
							if (firstNonEmptyLineContent != null) {
								return SectionFinderResult.createSingleItemResultList(
										firstNonEmptyLineContent.getStart(),
										firstNonEmptyLineContent.getEnd());
							}
							return null;
						}
					});
			cf.addConstraint(SingleChildConstraint.getInstance());
			this.sectionFinder = cf;

			// check for correct importance value
			this.addSubtreeHandler(new TimeEventAttributeHandler<DateType>() {

				@Override
				public Collection<KDOMReportMessage> createAttribute(KnowWEArticle article, Section<DateType> s) {
					TimeStamp t = DateType.getTimeStamp(s);
					if (false /* t is invalid */) { // TODO: set appropriate error
						return Arrays.asList((KDOMReportMessage) new InvalidNumberError(
								s.get().getName()
										+ ": " + s.getOriginalText()));
					}
					else {
						Section<TimeEventNew> teSection = s.findAncestorOfType(TimeEventNew.class);
						TimeEvent te = TimeEventNew.getEvent(teSection);
						if (te != null) te.setTime(t);
					}
					return new ArrayList<KDOMReportMessage>(0);
				}

			});

		}

		static TimeStamp getTimeStamp(Section<DateType> s) {
			return new TimeStamp(s.getOriginalText()); // one could
			// possibly
			// cache the
			// object in
			// sectionInfoStore..
		}
	}

	private static class TitleType extends TermDefinition<TimeEvent> {
		Pattern newline = Pattern.compile("\\r?\\n");

		public TitleType() {
			// true says that this name is registered as globally unique term
			super(TimeEvent.class, true);

			// renderer (for testing only)
			this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR1));

			// SectionFinder for Title
			ConstraintSectionFinder cf = new ConstraintSectionFinder(
					new ISectionFinder() {

						@Override
						public List<SectionFinderResult> lookForSections(String text, Section<?> father, KnowWEObjectType type) {
							Matcher matcher = newline.matcher(text);
							if (matcher.find()) { // if there is a linebreak in
								// the passed fragment take
								// everything before
								return SectionFinderResult.createSingleItemResultList(0,
										matcher.start());
							}
							else { // take everything
								return new AllTextFinderTrimmed().lookForSections(text,
										father, type);
							}

						}
					});
			cf.addConstraint(SingleChildConstraint.getInstance());
			this.sectionFinder = cf;

			// check for correct importance value
			this.addSubtreeHandler(new SubtreeHandler<TitleType>() {

				@Override
				public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<TitleType> s) {
					TimeEvent e = new TimeEvent(s.get().getTermName(s), s.getID(),
							article.getTitle());
					s.get().storeTermObject(article, s, e);
					return new ArrayList<KDOMReportMessage>(0);
				}
			});
		}

		@Override
		public String getTermName(Section s) {
			return s.getOriginalText();
		}
	}

	private static class ImportanceType extends DefaultAbstractKnowWEObjectType {
		Pattern embracedNumbers = Pattern.compile("\\(\\s*\\d*\\s*\\)");

		// Pattern embracedNumbers = Pattern.compile("1");

		public ImportanceType() {
			this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR2));

			// SectionFinder taking the last number in brackets
			ConstraintSectionFinder cf = new ConstraintSectionFinder(
					new ISectionFinder() {

						@Override
						public List<SectionFinderResult> lookForSections(String text, Section<?> father, KnowWEObjectType type) {
							String[] split = text.split("\\r?\\n");
							for (int i = 0; i < split.length; i++) {
								String line = split[i];
								if (line.trim().length() > 0) {
									// take the last match of that line
									Matcher matcher = embracedNumbers.matcher(line);

									// of course a horrible way to access the
									// last match, but seems like there is no
									// other
									int lastMatchStart = -1;
									int lastMatchEnd = -1;
									while (matcher.find()) {
										lastMatchStart = matcher.start();
										lastMatchEnd = matcher.end();
									}
									if (lastMatchStart != -1) {
										return SectionFinderResult.createSingleItemResultList(
														lastMatchStart,
														lastMatchEnd);
									}
								}
							}

							return null;

						}
					});
			cf.addConstraint(SingleChildConstraint.getInstance());
			this.sectionFinder = cf;

			// check for correct importance value
			this.addSubtreeHandler(new TimeEventAttributeHandler<ImportanceType>() {

				@Override
				protected Collection<KDOMReportMessage> createAttribute(KnowWEArticle article, Section<ImportanceType> s) {
					Integer i = ImportanceType.getImportance(s);
					if (i == null || i < 1 || i > 3) {
						return Arrays.asList((KDOMReportMessage) new InvalidNumberError(
								s.get().getName()
										+ ": " + s.getOriginalText()));
					}
					else {
						Section<TimeEventNew> teSection = s.findAncestorOfType(TimeEventNew.class);
						TimeEvent te = TimeEventNew.getEvent(teSection);
						if (te != null) te.setImportance(i);
						return new ArrayList<KDOMReportMessage>(0);
					}

				}
			});

		}

		static Integer getImportance(Section<ImportanceType> s) {
			String number = s.getOriginalText().replaceAll("\\(", "").replaceAll("\\)",
					"").trim();
			Integer i = null;
			try {
				i = Integer.parseInt(number);
			}
			catch (Exception e) {
				// is not a valid number
			}

			return i;
		}
	}

	private static class Source extends DefaultAbstractKnowWEObjectType {
		public Source() {
			this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR3));
			this.sectionFinder = new RegexSectionFinder("(QUELLE:.*)\\r?\\n",
					9999, 1);

			// add source value
			this.addSubtreeHandler(new TimeEventAttributeHandler<Source>() {

				@Override
				public Collection<KDOMReportMessage> createAttribute(KnowWEArticle article, Section<Source> s) {
					String name = Source.getSourceName(s);

					Section<TimeEventNew> teSection = s.findAncestorOfType(TimeEventNew.class);
					TimeEvent te = TimeEventNew.getEvent(teSection);
					te.addSource(name);

					return new ArrayList<KDOMReportMessage>(0);
				}
			});
		}

		static String getSourceName(Section<Source> s) {
			return s.getOriginalText().substring(s.getOriginalText().indexOf(":") + 1);
		}
	}

	private static class Description extends DefaultAbstractKnowWEObjectType {
		public Description() {
			SemanticAnnotation semanticAnnotation = new SemanticAnnotation();

			// first grab annotated concepts
			this.childrenTypes.add(semanticAnnotation);

			// then search for un-annotated concepts
			this.childrenTypes.add(new PersonOccurrence());
			this.childrenTypes.add(new LocationOccurrence());

			// for testing only
			this.setCustomRenderer(new KnowWEDomRenderer<KnowWEObjectType>() {

				@Override
				public void render(KnowWEArticle article, Section<KnowWEObjectType> sec, KnowWEUserContext user, StringBuilder string) {
					string.append(KnowWEUtils.maskHTML("<b>"));
					string.append(sec.getOriginalText());
					string.append(KnowWEUtils.maskHTML("</b>"));
					string.append(KnowWEUtils.maskHTML("<br>"));
					string.append(KnowWEUtils.maskHTML("<br>"));

				}
			});
			ConstraintSectionFinder f = new ConstraintSectionFinder(
					new AllTextFinderTrimmed());
			f.addConstraint(SingleChildConstraint.getInstance());
			this.sectionFinder = f;

			// check for correct importance value
			this.addSubtreeHandler(new TimeEventAttributeHandler<Description>() {

				@Override
				public Collection<KDOMReportMessage> createAttribute(KnowWEArticle article, Section<Description> s) {
					Section<TimeEventNew> teSection = s.findAncestorOfType(TimeEventNew.class);
					TimeEvent event = TimeEventNew.getEvent(teSection);
					if (event != null) event.setDescription(s.getOriginalText());
					return new ArrayList<KDOMReportMessage>(0);
				}
			});
		}
	}
}
