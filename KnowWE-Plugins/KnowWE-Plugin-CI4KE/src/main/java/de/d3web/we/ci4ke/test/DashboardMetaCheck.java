package de.d3web.we.ci4ke.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.denkbares.strings.NumberAwareComparator;
import de.d3web.testing.AbstractTest;
import de.d3web.testing.Message;
import de.d3web.testing.TestResult;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.CIDashboardManager;
import de.d3web.we.ci4ke.dashboard.type.CIDashboardType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * This test Checks all other Dashboards for failed tests and lists them
 *
 * @author Nikolai Reed (Olyro GmbH)
 * @created 24.10.2018
 */

public class DashboardMetaCheck extends AbstractTest<Article> {

    @Override
    public Message execute(Article article, String[] args, String[]... ignores) throws InterruptedException {
        List<Section<CIDashboardType>> successors = Sections.successors(article, CIDashboardType.class);
        Set<String> failedTests = new HashSet<>();
        Set<String> warningTests = new HashSet<>();
        Set<String> errorTests = new HashSet<>();
        for (Section<CIDashboardType> successor : successors) {
            CIDashboard dashboard = CIDashboardManager.getDashboard(successor);
            if (dashboard == null  || dashboard.getLatestBuild() == null)
                continue;
            Message.Type overallResult = dashboard.getLatestBuild().getOverallResult();
            if (overallResult != Message.Type.SUCCESS)
            {
                for (TestResult result : dashboard.getLatestBuild().getResults()) {
                    if (result.getTestName() != this.getName()) {
                            Message.Type resultType = result.getSummary().getType();
                            if (resultType == Message.Type.FAILURE)
                                failedTests.add(result.getTestName());
                            if (resultType == Message.Type.WARNING){
                                warningTests.add(result.getTestName());
                            }
                            if (resultType == Message.Type.ERROR){
                                errorTests.add(result.getTestName());
                            }
                    }
                }
                return createDashboardError(failedTests, warningTests, errorTests, dashboard.getDashboardName());
            }
        }
        return new Message(Message.Type.SUCCESS, null);
    }

    protected Message createDashboardError(Set<String> failedTests, Set<String> warnedTests, Set<String> errorTests, String dashboard){
        StringBuilder result = new StringBuilder();
        Message.Type type = Message.Type.SUCCESS;
        result.append("Dashboard ")
                .append(KnowWEUtils.maskJSPWikiMarkup(dashboard));
        if (!warnedTests.isEmpty()){
            result.append("\nThe following tests have warnings:");
            warnedTests.stream().sorted(NumberAwareComparator.CASE_INSENSITIVE)
                    .forEach(name -> result.append("\n* ").append(KnowWEUtils.maskJSPWikiMarkup(name)));
            if (type != Message.Type.ERROR && type != Message.Type.FAILURE)
                type = Message.Type.WARNING;
        }
        if (!failedTests.isEmpty()){
            result.append("\nThe following tests have failed:");
            failedTests.stream().sorted(NumberAwareComparator.CASE_INSENSITIVE)
                    .forEach(name -> result.append("\n* ").append(KnowWEUtils.maskJSPWikiMarkup(name)));
            if (type != Message.Type.ERROR)
                type = Message.Type.FAILURE;
        }
        if (!errorTests.isEmpty()){
            result.append("\nThe following tests did not run due to error:");
            errorTests.stream().sorted(NumberAwareComparator.CASE_INSENSITIVE)
                    .forEach(name -> result.append("\n* ").append(KnowWEUtils.maskJSPWikiMarkup(name)));
            type = Message.Type.ERROR;
        }
        return new Message(type, result.toString());

    }


    @Override
    public Class<Article> getTestObjectClass() {
        return Article.class;
    }

    @Override
    public String getDescription() {
        return "Checks all other Dashboards for failed tests.";
    }
}
