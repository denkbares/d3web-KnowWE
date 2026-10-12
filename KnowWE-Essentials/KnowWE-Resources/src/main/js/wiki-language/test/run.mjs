// Runs the syntax tree tests in test/*.txt against the ES build. Run through "pnpm test".
//
// A test is a "# name" line, the input, a "==>" line and the expected tree. Unlike the lezer generator, the input is
// taken verbatim up to the line break before "==>", since leading blanks and empty lines matter in wiki markup. A
// JSON object after the name configures the test, {"markups": null} parses without markup definitions.
import {readdirSync, readFileSync} from "node:fs";
import {testTree} from "@lezer/generator/test";
import {EditorState} from "@codemirror/state";
import {foldable} from "@codemirror/language";
import {defineMarkups, parser, registerLanguage, unregisterLanguage, wiki} from "../dist/wiki-language.mjs";
import {buildParser} from "@lezer/generator";
import {checkCoverage, checkIncremental, checkNesting, errorOffsets} from "./checks.mjs";
import {MARKUPS} from "./markups.mjs";

let failures = 0, passed = 0;

function report(name, error) {
    if (error) {
        failures++;
        console.log("FAIL " + name + "\n  " + String(error.message ?? error).replace(/\n/g, "\n  "));
    } else {
        passed++;
    }
}

function readTests(file) {
    const tests = [];
    const lines = file.split("\n");
    for (let i = 0; i < lines.length; i++) {
        const header = /^# (.*?)\s*(\{.*\})?$/.exec(lines[i]);
        if (!header) continue;
        const start = ++i;
        while (i < lines.length && lines[i] !== "==>") i++;
        const text = lines.slice(start, i).join("\n");
        const expected = [];
        while (++i < lines.length && !lines[i].startsWith("# ")) expected.push(lines[i]);
        i--;
        const config = header[2] ? JSON.parse(header[2]) : {};
        tests.push({name: header[1], config, text, expected: expected.join("\n").trim()});
    }
    return tests;
}

// a stand-in for an embedded language that parses words
const words = buildParser("@top Words { Word* } @skip { space } "
    + "@tokens { Word { ![ \\t\\n]+ } space { $[ \\t\\n]+ } }");

const dir = new URL(".", import.meta.url).pathname;
const strict = parser.configure({strict: true});
for (const file of readdirSync(dir).filter(name => name.endsWith(".txt")).sort()) {
    for (const {name, config, text, expected} of readTests(readFileSync(dir + file, "utf8"))) {
        defineMarkups("markups" in config ? config.markups : MARKUPS);
        if (config.language) registerLanguage(config.language, words);
        try {
            // every input is valid wiki markup, so the strict parser must not fail
            const tree = strict.parse(text);
            testTree(tree, expected);
            checkNesting(tree);
            checkCoverage(tree, text);
            if (errorOffsets(tree).length) throw new Error("error nodes in tree");
            // typing at every position must give the same tree as a fresh parse
            for (let pos = 0; pos <= text.length; pos += Math.max(1, text.length >> 3)) {
                checkIncremental(parser, text, tree, pos, pos, "x");
                checkIncremental(parser, text, tree, pos, Math.min(text.length, pos + 2), "");
            }
            report(file + ": " + name);
        } catch (error) {
            report(file + ": " + name, error);
        } finally {
            if (config.language) unregisterLanguage(config.language);
        }
    }
}

defineMarkups(MARKUPS);
const lf = "!! T\n* a\n continued\n| x | y\n%%Rule\nIF a THEN b\n/%\ntext __bold\nmore__\n";
report("CRLF line breaks", parser.parse(lf.replace(/\n/g, "\r\n")).toString() === parser.parse(lf).toString()
    ? null : new Error("CRLF input parses differently"));

// nesting deeper than the parse stack allows turns the innermost marks into text instead of failing
for (const unit of ["%%a ", "{{", "%%a\n", "__a ''b {{c %%d "]) {
    try {
        strict.parse(unit.repeat(5000));
        report("deep nesting of " + JSON.stringify(unit));
    } catch (error) {
        report("deep nesting of " + JSON.stringify(unit), error);
    }
}

// a heading folds its section up to the next heading of the same or a larger level, the folded text is marked by < >
const FOLDS = [
    ["!! A<\ntext\n! B\nmore>\n\n!! C", 1],
    ["! A<\ntext>\n!! B\nmore", 1],
    ["!!! A<\n!! B\ntext>\n!!! C", 1],
    ["!! B<\ntext>\n\n\n!!! C", 1],
    ["! A\n!! B", 1],
    ["!! A<\ntext\n%%Rule\nIF a THEN b\n/%>\n", 1],
    ["%%style\n!! A<\ntext>\n/%\n!! B", 2],
    ["! [Link|\nPage]<\ntext>", 1],
    ["text\n!! A", 1],
    ["text\n!! A", 2],
    // the editor state parses only the start of a long document, the rest is searched for headings
    ["!! A<\n" + "text\n\n".repeat(5000) + "! B\nmore>\n!!! C", 1],
];
for (const [marked, line] of FOLDS) {
    const text = marked.replace(/[<>]/g, "");
    const from = marked.indexOf("<");
    const expected = from < 0 ? null : {from, to: marked.indexOf(">") - 1};
    const state = EditorState.create({doc: text, extensions: wiki()});
    const {from: start, to: end} = state.doc.line(line);
    const actual = foldable(state, start, end);
    report("fold " + JSON.stringify(marked), JSON.stringify(actual) === JSON.stringify(expected)
        ? null : new Error(`expected ${JSON.stringify(expected)} but got ${JSON.stringify(actual)}`));
}

console.log(failures ? `${failures} of ${failures + passed} tests failed` : `all ${passed} tests passed`);
process.exit(failures ? 1 : 0);
