package de.d3web.we.hermes.kdom.event;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.constraint.ConstraintSectionFinder;
import de.d3web.we.kdom.constraint.SingleChildConstraint;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
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
		}
	}

	private static class TitleType extends DefaultAbstractKnowWEObjectType {
		Pattern newline = Pattern.compile("\\r?\\n");

		public TitleType() {
			this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR1));
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
		}
	}

	private static class ImportanceType extends DefaultAbstractKnowWEObjectType {
		Pattern embracedNumbers = Pattern.compile("\\(\\s*\\d*\\s*\\)");
		//Pattern embracedNumbers = Pattern.compile("1");

		public ImportanceType() {
			this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR2));
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
									
									//of course a horrible way to access the last match, but seems like there is no other
									int lastMatchStart = -1;
									int lastMatchEnd = -1;
									while (matcher.find()) {
										lastMatchStart = matcher.start();
										lastMatchEnd = matcher.end();
									}
									if(lastMatchStart != -1) {
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
		}
	}

	private static class Description extends DefaultAbstractKnowWEObjectType {
		public Description() {
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
		}
	}
}
