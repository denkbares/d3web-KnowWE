/*
 * Copyright (C) 2021 denkbares GmbH. All rights reserved.
 *
 */

package de.d3web.we.ci4ke.test;

import com.denkbares.strings.NumberAwareComparator;
import com.denkbares.strings.Strings;
import de.d3web.testing.*;
import de.d3web.we.ci4ke.build.CIRenderer;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * CI Test that tests whether an article consist of markup(s) with errors in it. This encompasses missing '%' at the
 * beginning and end as well as nested markups
 *
 * @author Tobias Schmee (denkbares GmbH)
 * @created 17.03.21
 */
public class ArticleHasMarkupErrorsTest extends AbstractTest<Article> implements ResultRenderer {

    public ArticleHasMarkupErrorsTest() {
        this.addIgnoreParameter("article-title", TestParameter.Type.String, TestParameter.Mode.Optional,
                "Specify articles that are ignored by this test");
    }

    @Override
    public Message execute(Article article, String[] args2, String[]... ignores) throws InterruptedException {

        // list all ignored articles
        List<String> ignoredArticles = Stream.of(ignores)
                .flatMap(Stream::of)
                .map(Strings::unquote)
                .toList();
        // when this one is ignored -> go on
        if (ignoredArticles.contains(article.getTitle())) return new Message(Message.Type.SUCCESS);

        // collect messages for erroneous markups
        Set<String> messages = new TreeSet<>(NumberAwareComparator.CASE_INSENSITIVE);
        List<MessageObject> messageObjects = new ArrayList<>();
        List<Section<?>> sections = article.getRootSection().getChildren();
        for (Section<?> sec : sections) {
            // Check for nested markups
            messageObjects.add(new MessageObject(sec.getTitle(), sec.get().getClass()));
            String text = sec.getText().trim();
            text = text.replaceAll("%%\\(color:.+%%", "");
            if (text.matches("%%[\\s\\S]+%%[\\s\\S]+")) {
                messages.add("In article [" + article.getTitle() + "] the section '" +
                        sec.get().getName() +
                        "' seems to have a nested markup.");
                continue;
            }
            // section is actually perceived as a markup -> it is correct and we can go to the next
            if (sec.get() instanceof DefaultMarkupType) continue;
            // text is only a % -> this might be a mistake ?
            if ("%".equals(text)) {
                messages.add("In article [" + article.getTitle() + "]  a lonely % is floating around. This may be a mistake");
                continue;
            }
            // text is not a markup -> we can go to the next
            if (!text.startsWith("%%") && !text.endsWith("\n%")) continue;
            if (text.startsWith(("@progress"))) continue;
            // Markup is in one line, then no closing % is needed -> we can go to the next
            if (text.startsWith("%%") && !text.contains(System.getProperty("line.separator"))) continue;
            // Check for missing % at the beginning or end of the markup
            if (!text.matches("%%[\\s\\S]+%")) {
                messages.add("In article [" + article.getTitle() + "] the section '" +
                        sec.get().getName() +
                        "' seems to have a wrong %-Markup. Did you start with '%%' and close the Markup with '%'?");
            }
        }
        return getMessage(messageObjects, messages, Message.Type.ERROR);
    }

    @Override
    public Class<Article> getTestObjectClass() {
        return Article.class;
    }

    @Override
    public String getDescription() {
        return "CI Test that tests whether an article consist of markup(s) with errors in it. This encompasses missing '%' at the beginning and end as well as nested markups";
    }

    @Override
    public void renderResultMessage(UserContext context, String testObjectName, Message message, TestResult testResult, RenderResult renderResult) {
        Class<?> testObjectClass = CIRenderer.renderResultMessageHeader(context, message, testResult, renderResult);
        renderResult.append(message.getText());
        CIRenderer.renderResultMessageFooter(context, testObjectName, testObjectClass, message, renderResult);
    }
}
