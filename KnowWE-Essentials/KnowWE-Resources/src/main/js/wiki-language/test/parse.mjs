// Parses sample documents with the ES build and checks the resulting trees. Run through "pnpm test".
import {parser, registerLanguage} from "../dist/wiki-language.mjs";
import {buildParser} from "@lezer/generator";

let failures = 0;

function tree(text) {
    return parser.parse(text).toString();
}

function check(name, text, expected) {
    const actual = tree(text);
    const ok = expected instanceof RegExp ? expected.test(actual) : actual === expected;
    console.log((ok ? "ok   " : "FAIL ") + name);
    if (!ok) {
        failures++;
        console.log("  input:    " + JSON.stringify(text));
        console.log("  expected: " + expected);
        console.log("  actual:   " + actual);
    }
}

check("heading", "!!! Title\n", "Document(Heading(HeadingMark,Text))");
check("heading mark mid line is text", "a ! b\n", "Document(Paragraph(Text))");
check("mark after inline element", "''x''!b", "Document(Paragraph(Italic(ItalicMark,Text,ItalicMark),SpecialChar,Text))");
check("dash at line start", "-1 degree", "Document(Paragraph(SpecialChar,Text))");
check("unclosed link recovers", "x [y\nz", /^Document\(Paragraph\(.*\),Paragraph\(Text\)\)$/);
check("list", "* one\n** two\n# three", "Document(ListItem(ListMark,Text),ListItem(ListMark,Text),ListItem(ListMark,Text))");
check("bold and italic", "__bold__ and ''it''",
    "Document(Paragraph(Bold(BoldMark,Text,BoldMark),Text,Italic(ItalicMark,Text,ItalicMark)))");
check("nested italic in bold", "__a ''b'' c__",
    "Document(Paragraph(Bold(BoldMark,Text,Italic(ItalicMark,Text,ItalicMark),Text,BoldMark)))");
check("link", "see [Main|Main page|target='_blank'] now",
    "Document(Paragraph(Text,Link(LinkOpen,LinkContent,LinkSeparator,LinkContent,LinkSeparator,LinkContent,LinkClose),Text))");
check("plugin", "[{TableOfContents title='x'}]", "Document(Paragraph(Plugin))");
check("multi line plugin", "[{Foo\n  bar='1'}]", "Document(Paragraph(Plugin))");
check("monospace and preformatted", "{{code}} {{{\npre\n}}}", "Document(Paragraph(Monospace,Text,Preformatted))");
check("table", "|| h1 || h2\n| a | b", "Document(TableRow(TableMark,Text,CellSeparator,Text),TableRow(TableMark,Text,CellSeparator,Text))");
check("pipe in paragraph", "a | b", "Document(Paragraph(Text,Pipe,Text))");
check("horizontal rule", "----\ntext", "Document(HorizontalRule,Paragraph(Text))");
check("escape", "~[not a link] [[literal", "Document(Paragraph(Escape,Text,SpecialChar,Text,Escape,Text))");
check("line break", "a\\\\b", "Document(Paragraph(Text,LineBreak,Text))");
check("markup block", "%%COOM:DSL\nproduct x\n/%\n",
    "Document(Markup(MarkupOpen,MarkupBody(Line(Text)),MarkupClose))");
check("markup block with annotations", "%%Off:CIDashboard\n@test: Foo\n@name = bar\ntext\n%",
    "Document(Markup(MarkupOpen,MarkupBody(Annotation(AnnotationName,AnnotationValue),Annotation(AnnotationName,AnnotationValue),Line(Text)),MarkupClose))");
check("markup block with comment and colon", "%%Rule: // comment\n  a --> b\n/%",
    "Document(Markup(MarkupOpen,MarkupBody(Line(Text),Line(Text)),MarkupClose))");
check("single line markup", "%%Tag: a, b\nnext", "Document(Markup(MarkupOpen,MarkupBody(Line(Text))),Paragraph(Text))");
check("indented markup", "  %%Foo\nx\n  /%", "Document(Markup(MarkupOpen,MarkupBody(Line(Text)),MarkupClose))");
check("unterminated markup runs to end", "%%Foo\na\nb", "Document(Markup(MarkupOpen,MarkupBody(Line(Text),Line(Text))))");
check("percent mid line is text", "100%% sure", "Document(Paragraph(Text,SpecialChar,SpecialChar,Text))");
check("empty lines", "a\n\n\nb", "Document(Paragraph(Text),Paragraph(Text))");
check("crlf", "!! T\r\n* x\r\n", "Document(Heading(HeadingMark,Text),ListItem(ListMark,Text))");
check("unclosed bold recovers", "__oops\nnext", /^Document\(Paragraph\(.*\),Paragraph\(Text\)\)$/);

// a registered language replaces the default body parser
const coom = buildParser('@top CoomModel { Word* } @tokens { Word { $[a-z]+ } space { $[ \\n]+ } } @skip { space }');
registerLanguage("COOM:DSL", coom);
check("registered language", "%%coom:dsl\nproduct x\n/%", "Document(Markup(MarkupOpen,CoomModel(Word,Word),MarkupClose))");
check("unregistered keeps default", "%%Other\nproduct x\n/%", "Document(Markup(MarkupOpen,MarkupBody(Line(Text)),MarkupClose))");

// positions of the embedded body
const t = parser.parse("%%Foo\nbody\n/%");
const inner = t.topNode.getChild("Markup").getChild("MarkupBody");
console.log((inner && inner.name === "MarkupBody" && inner.from === 5 && inner.to === 11 ? "ok   " : "FAIL ") + "embedded range " + inner.from + "-" + inner.to);
if (!(inner && inner.from === 5 && inner.to === 11)) failures++;

console.log(failures ? failures + " failure(s)" : "all passed");
process.exit(failures ? 1 : 0);
