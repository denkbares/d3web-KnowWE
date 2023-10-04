/*
 * Copyright (C) 2023 denkbares GmbH, Germany
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

package de.knowwe.jspwiki.recentChanges;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.wiki.api.core.Page;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Pair;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.jspwiki.PageComparator;
import de.knowwe.kdom.renderer.PaginationRenderer;

import static de.knowwe.jspwiki.recentChanges.RecentChangesUtils.*;

@SuppressWarnings("rawtypes")
public class RecentChangesPaginationRenderer extends PaginationRenderer {

    public RecentChangesPaginationRenderer(Renderer decoratedRenderer, SortingMode sorting, boolean supportFiltering) {
        super(decoratedRenderer, sorting, supportFiltering);
    }

    @Override
    protected void renderFilterTools(Section<?> section, UserContext user, RenderResult result) {
        boolean tickedPage = showPages(user);
        boolean tickedAtt = isShowAttachments(user);
        boolean tickedInterm = showIntermediates(user);
        if (tickedPage) {
            result.appendHtmlTag("input", "type", "checkbox", "class", "filter-style show-pages", "id", "showPages", "name", "showPages", "onclick", "KNOWWE.plugin.jspwikiConnector.setPageFilter(this, 'page')", "checked", "checked");
        } else {
            result.appendHtmlTag("input", "type", "checkbox", "class", "filter-style show-pages", "id", "showPages", "name", "showPages", "onclick", "KNOWWE.plugin.jspwikiConnector.setPageFilter(this, 'page')");
        }
        result.appendHtmlElement("label", "Pages", "class", "fillText", "for", "showPages");
        if (tickedAtt) {
            result.appendHtmlTag("input", "type", "checkbox", "class", "filter-style show-attachments", "id", "showAttachments", "name", "showAttachments", "onclick", "KNOWWE.plugin.jspwikiConnector.setPageFilter(this, 'attachment')", "checked", "checked");
        } else {
            result.appendHtmlTag("input", "type", "checkbox", "class", "filter-style show-attachments", "id", "showAttachments", "name", "showAttachments", "onclick", "KNOWWE.plugin.jspwikiConnector.setPageFilter(this, 'attachment')");
        }
        result.appendHtmlElement("label", "Attachments", "class", "fillText", "for", "showAttachments");
        if (tickedInterm) {
            result.appendHtmlTag("input", "type", "checkbox", "class", "filter-style show-intermediate", "id", "showIntermediate", "name", "showIntermediate", "onclick", "KNOWWE.plugin.jspwikiConnector.setPageFilter(this, 'intermediate')", "checked", "checked");
        } else {
            result.appendHtmlTag("input", "type", "checkbox", "class", "filter-style show-intermediate", "id", "showIntermediate", "name", "showIntermediate", "onclick", "KNOWWE.plugin.jspwikiConnector.setPageFilter(this, 'intermediate')");
        }
        result.appendHtmlElement("label", "Intermediate Versions", "class", "fillText", "for", "showIntermediate");

        super.renderFilterTools(section, user, result);
    }

    @Override
	protected String getResultSizeTag(Section<?> sec, UserContext user) {
        String resultSize = String.valueOf(getRecentChanges(sec, user).size());
        String tag = "";
        if (resultSize.equals(UNKNOWN_RESULT_SIZE)) {
            tag += "<span class=fillText> rows (overall number unknown)</span>";
        } else {
            int resultSizeInt = Integer.parseInt(resultSize);
            tag += "<input class='resultSize' style='display:none' value='" + resultSize + "'/>";
            tag += "<span class=fillText>" + (getCount(sec, user) == Integer.MAX_VALUE ? "" : " of");
            tag += " " + resultSize + " " + Strings.pluralOf(resultSizeInt, "row", false) + "</span>";
        }
        return tag;
    }

    private static final RecentChangesUtils util = new RecentChangesUtils();
    private static final Logger LOGGER = LoggerFactory.getLogger(RecentChangesRenderer.class);

    public List<Page> getRecentChanges(Section<?> sec, UserContext user) {
        Map<String, Set<Pattern>> filter = PaginationRenderer.getFilter(sec, user);
        Set<Page> filteredRecentChanges = getRecentChangesFiltered(user, filter);
        PaginationRenderer.setResultSize(user, filteredRecentChanges.size());
        return sortPages(sec, user, filteredRecentChanges);
    }


	@NotNull
    private List<Page> sortPages(Section<?> sec, UserContext user, Set<Page> filteredRecentChanges) {
        List<Pair<String, Comparator>> columnComparators = PaginationRenderer.getMultiColumnSorting(sec, user).stream()
                .map(p -> new Pair<>(p.getA(), createComparator(p.getA(), p.getB())))
                .collect(Collectors.toList());
        if (columnComparators.isEmpty()) {
            columnComparators.add(new Pair<>(LAST_MODIFIED, createComparator(LAST_MODIFIED, false)));
        }
        PageComparator pageComparator = new PageComparator(columnComparators);
        List<Page> sortedFilteredRecentChanges = new ArrayList<>(filteredRecentChanges);
        sortedFilteredRecentChanges.sort(pageComparator);
        return sortedFilteredRecentChanges;
    }

    private Comparator createComparator(String columnName, boolean ascending) {
        if (columnName.equals(LAST_MODIFIED)) {
            Comparator<Date> comparator = Date::compareTo;
            return ascending ? comparator : comparator.reversed();
        } else {
            Comparator<String> comparator = String::compareToIgnoreCase;
            return ascending ? comparator : comparator.reversed();
        }
    }

}
