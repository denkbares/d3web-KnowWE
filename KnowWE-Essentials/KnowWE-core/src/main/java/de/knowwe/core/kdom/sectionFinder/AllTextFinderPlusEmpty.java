package de.knowwe.core.kdom.sectionFinder;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

import java.util.List;

public final class AllTextFinderPlusEmpty implements SectionFinder {

    private static final AllTextFinderPlusEmpty instance = new AllTextFinderPlusEmpty();

    private AllTextFinderPlusEmpty() {
    }

    public static AllTextFinderPlusEmpty getInstance() {
        return instance;
    }

    @Override
    public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
        return SectionFinderResult.singleItemList(new SectionFinderResult(0, text.length()));
    }
}
