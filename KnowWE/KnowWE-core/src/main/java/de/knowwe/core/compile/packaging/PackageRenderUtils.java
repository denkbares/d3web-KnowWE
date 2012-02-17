// package de.knowwe.core.compile.packaging;
//
// import java.util.Set;
//
// import de.knowwe.core.KnowWEEnvironment;
// import de.knowwe.core.kdom.KnowWEArticle;
// import de.knowwe.core.kdom.parsing.Section;
// import de.knowwe.core.utils.KnowWEUtils;
//
// public class PackageRenderUtils {
//
// public static KnowWEArticle checkArticlesCompiling(KnowWEArticle article,
// Section<?> s, StringBuilder string) {
//
// if (s.get().isIgnoringPackageCompile()) return article;
//
// // check and handle, if the section is compiled in other articles
// KnowWEArticle compilingArticle = article;
// if (article.getTitle().equals(s.getTitle())) {
// Set<String> compilingArticles =
// KnowWEEnvironment.getInstance().getPackageManager(
// article.getWeb()).getArticlesReferringTo(s);
//
// if (compilingArticles.size() > 1) {
// // string.append("Articles compiling this Section: " +
// // compilingArticles);
// string.append(KnowWEUtils.maskHTML("<span class=\"info\">Articles compiling this Section: "
// + compilingArticles + "</span><p/>"));
// }
// else if (compilingArticles.size() == 1) {
// String title = compilingArticles.iterator().next();
// if (!title.equals(compilingArticle.getTitle())) {
// // string.append("Compiled and rendered for Article: " +
// // title + "\n\n");
// compilingArticle =
// KnowWEEnvironment.getInstance().getArticle(article.getWeb(),
// title);
// }
// }
// else {
// // string.append("This Section is not compiled in any article!\n\n");
// string.append(KnowWEUtils.maskHTML("<span class=\"warning\">This Section is not compiled in any article!</span><p/>"));
// }
// }
// return compilingArticle;
// }
//
// public static KnowWEArticle checkArticlesCompiling(KnowWEArticle article,
// Section<?> s) {
//
// if (s.get().isIgnoringPackageCompile()) return article;
//
// KnowWEArticle compilingArticle = article;
// if (article.getTitle().equals(s.getTitle())) {
// Set<String> compilingArticles =
// KnowWEEnvironment.getInstance().getPackageManager(
// article.getWeb()).getArticlesReferringTo(s);
// if (compilingArticles.size() == 1) {
// String title = compilingArticles.iterator().next();
// if (!title.equals(compilingArticle.getTitle())) {
// compilingArticle =
// KnowWEEnvironment.getInstance().getArticle(article.getWeb(),
// title);
// }
// }
// }
// return compilingArticle;
// }
//
// }
