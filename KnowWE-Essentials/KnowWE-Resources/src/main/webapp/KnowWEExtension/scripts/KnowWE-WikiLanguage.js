var KnowWEWikiLanguage = (function(exports, common, highlight, language, lr) {
  "use strict";
  const MarkupOpen = 1, MarkupContent = 2, MarkupClose = 3, HeadingMark = 4, ListMark = 5, DefinitionMark = 6, HorizontalRule = 7, TableMark = 8;
  const NEWLINE = 10, RETURN = 13, SPACE = 32, TAB = 9, PERCENT = 37, SLASH = 47, COLON = 58, EQUALS = 61;
  const EXCLAMATION = 33, STAR = 42, HASH = 35, SEMICOLON = 59, DASH = 45, PIPE = 124;
  function isBlank(ch) {
    return ch === SPACE || ch === TAB;
  }
  function isNameStart(ch) {
    return ch >= 65 && ch <= 90 || ch >= 97 && ch <= 122;
  }
  function isNameChar(ch) {
    return isNameStart(ch) || ch >= 48 && ch <= 57 || ch === 95 || ch === 45 || ch === 46 || ch === COLON;
  }
  function isLineEnd(ch) {
    return ch === NEWLINE || ch === RETURN || ch < 0;
  }
  function atLineStart(input) {
    return input.pos === 0 || input.peek(-1) === NEWLINE;
  }
  function closingLineLength(input, offset) {
    let i = offset;
    while (isBlank(input.peek(i))) i++;
    if (input.peek(i) === SLASH) i++;
    if (input.peek(i) !== PERCENT) return -1;
    i++;
    while (isBlank(input.peek(i))) i++;
    return isLineEnd(input.peek(i)) ? i - offset : -1;
  }
  function declarationLength(input, offset) {
    let i = offset;
    while (isBlank(input.peek(i))) i++;
    if (input.peek(i) !== PERCENT || input.peek(i + 1) !== PERCENT) return -1;
    i += 2;
    if (!isNameStart(input.peek(i))) return -1;
    while (isNameChar(input.peek(i))) i++;
    let j = i;
    while (isBlank(input.peek(j))) j++;
    if (input.peek(j) === COLON || input.peek(j) === EQUALS) i = j + 1;
    return i - offset;
  }
  function isBlockDeclaration(input, offset) {
    let i = offset;
    while (isBlank(input.peek(i))) i++;
    const ch = input.peek(i);
    return isLineEnd(ch) || ch === SLASH && input.peek(i + 1) === SLASH;
  }
  function countWhile(input, predicate, max) {
    let i = 0;
    while (i < max && predicate(input.peek(i))) i++;
    return i;
  }
  const lineStart = new lr.ExternalTokenizer((input, stack) => {
    if (stack.canShift(MarkupContent)) {
      let length = 0;
      if (isBlockDeclaration(input, 0)) {
        for (; ; ) {
          let ch2 = input.peek(length);
          while (!isLineEnd(ch2)) ch2 = input.peek(++length);
          if (ch2 < 0) break;
          if (ch2 === RETURN) length++;
          if (input.peek(length) === NEWLINE) length++;
          if (closingLineLength(input, length) >= 0 || input.peek(length) < 0) break;
        }
      } else {
        while (!isLineEnd(input.peek(length))) length++;
      }
      if (length > 0) input.acceptToken(MarkupContent, length);
      return;
    }
    if (!atLineStart(input)) return;
    if (stack.canShift(MarkupClose)) {
      const length = closingLineLength(input, 0);
      if (length > 0) {
        input.acceptToken(MarkupClose, length);
        return;
      }
    }
    if (stack.canShift(MarkupOpen)) {
      const length = declarationLength(input, 0);
      if (length > 0) {
        input.acceptToken(MarkupOpen, length);
        return;
      }
    }
    const ch = input.next;
    if (ch === EXCLAMATION && stack.canShift(HeadingMark)) {
      input.acceptToken(HeadingMark, countWhile(input, (c) => c === EXCLAMATION, 3));
    } else if ((ch === STAR || ch === HASH) && stack.canShift(ListMark)) {
      input.acceptToken(ListMark, countWhile(input, (c) => c === STAR || c === HASH, 20));
    } else if (ch === SEMICOLON && stack.canShift(DefinitionMark)) {
      input.acceptToken(DefinitionMark, 1);
    } else if (ch === DASH && stack.canShift(HorizontalRule)) {
      const length = countWhile(input, (c) => c === DASH, 1e3);
      if (length >= 4) input.acceptToken(HorizontalRule, length);
    } else if (ch === PIPE && stack.canShift(TableMark)) {
      input.acceptToken(TableMark, input.peek(1) === PIPE ? 2 : 1);
    }
  }, { contextual: true });
  const parser$2 = lr.LRParser.deserialize({
    version: 14,
    states: "'hQ!UOROOO!]ORO'#CfO!kOSO'#CnO#hOQO'#CjO$aOQO'#ChOOOO'#DW'#DWO%]OQO'#CgO%gOQO'#CyO%qOQO'#CzO&pOWO'#C{O&zOQO'#C}OOOO'#DV'#DVO'UORO'#DTQ'`OQOOQ'`OQOOOOOO,59Q,59QO'eORO,59QO'pOSO'#DRO'{OSO,59YOOOO,59Y,59YO'{OSO,59YOOOO'#DY'#DYOOOO'#DQ'#DQO(TOQO,59UOOOO,59U,59UOOOO'#DX'#DXOOOO'#DP'#DPO([OQO,59SOOOO,59S,59SOOOO'#DO'#DOO(cOQO,59RO(mOQO,59eO(wOQO,59fOOOO'#DS'#DSO)ROWO,59gO)]OQO,59iOOOO,59o,59oOOOO-E7R-E7ROOOO1G.l1G.lOOOO,59m,59mOOOO-E7P-E7POOOO1G.t1G.tO)gOSO1G.tOOOO-E7O-E7OOOOO1G.p1G.pOOOO-E6}-E6}OOOO1G.n1G.nOOOO-E6|-E6|OOOO-E7Q-E7QOOOO7+$`7+$`",
    stateData: ")y~OPPOSUOTVOUWOVZOWXO]SO_RO`TOaTOcQOgTOhTOiTOjTOkTO~O}[O~PQOQ`OR_OxYX}YX~OddOeaOfcO~O]SO`eOaeOcQOgeOheOieOjeOkeOleO~O_hO~P!vO_RO`iOaiOcQOgiOhiOiiOjiOkiOliO~O]lO~P#oO]SO_RO`TOaTOcQOgTOhTOiTOjTOkTOlmO~OxZX}ZX~P$hOxmX}mX~P$hOxnX}nX~P$hO]SO_RO`TOaTOcQOgTOhTOiTOjTOkTOpqO~OxoX}oX~P%{OxqX}qX~P$hOxwX}wX~PQO}[O~ORvOxYa}Ya~OdwOeuXfuX~OeaOfyO~O_|O~P!vO]!OO~P#oOxZa}Za~P$hOxma}ma~P$hOxna}na~P$hOxoa}oa~P%{Oxqa}qa~P$hOeaOf!RO~Oa`ghi]_ki~",
    goto: "%X}PPPPPPPPPP!O!O!SP!fPPP!xPPPPPPPPPP!O!O!OP!O#`#r#x$O$Y$`P$g$m%P%TTZO[hTOUVWXY[noprsTeRghTOUVWXY[noprsTiSkhTOUVWXY[noprsSeRgTiSkQnUQoVQpWQsYX!PnopsQkSR}kQgRR{gQbQSxbzRzdQrXR!QrS]O^Ru]Q^ORt[SYO[`mUVWYnopsTqXrTjSkTfRg",
    nodeNames: "⚠ MarkupOpen MarkupContent MarkupClose HeadingMark ListMark DefinitionMark HorizontalRule TableMark Document Markup Heading Bold BoldMark Italic ItalicMark Monospace Preformatted Link LinkOpen LinkContent LinkSeparator LinkClose Plugin Escape LineBreak Text SpecialChar Pipe ListItem Definition TableRow CellSeparator Paragraph",
    maxTerm: 45,
    skippedNodes: [0],
    repeatNodeCount: 6,
    tokenData: ";rVRpOY#VYZ%WZ]#V]^%]^q#Vqr%wrs#Vst%wtu#Vuv%wvw#Vwx&bxz#Vz{%w{}#V}!O%w!O!]#V!]!^%w!^!}#V!}#O'l#O#P+g#P#Q,n#Q#R#V#R#S,u#S#o#V#o#p.P#p#q:e#q#r%w#r#s:v#s;'S#V;'S;=`%Q<%lO#VV#^adQjTOY#VZ]#V]^$c^u#Vuv$cvw#Vwx$cx!}#V!}#P$c#Q#R#V#R#S$c#S#o#V#o#p$c#q#s$c#s;'S#V;'S;=`%Q<%lO#VQ$hUdQOY$cZ#P$c#Q#p$c#q;'S$c;'S;=`$z<%lO$cQ$}P;=`<%l$cV%TP;=`<%l#VT%]O}TV%bVdQOY$cYZ%WZ#P$c#Q#p$c#q;'S$c;'S;=`$z<%lO$cV&OUdQkTOY$cZ#P$c#Q#p$c#q;'S$c;'S;=`$z<%lO$cV&iWdQkTOY$cZw$cwx'Rx#P$c#Q#p$c#q;'S$c;'S;=`$z<%lO$cV'YUdQ_TOY$cZ#P$c#Q#p$c#q;'S$c;'S;=`$z<%lO$cV'sXdQcTOY$cZ!}$c!}#O(`#O#P$c#Q#o$c#o#p(y#q;'S$c;'S;=`$z<%lO$cV(gUdQhTOY$cZ#P$c#Q#p$c#q;'S$c;'S;=`$z<%lO$cV)OYdQOY(yYZ)nZ#P(y#P#Q)n#Q#p(y#p#q)n#q#r*o#r;'S(y;'S;=`+a<%lO(yT)qTO#q)n#q#r*Q#r;'S)n;'S;=`*i<%lO)nT*TTO#P)n#P#Q*d#Q;'S)n;'S;=`*i<%lO)nT*iOgTT*lP;=`<%l)nV*tXdQOY(yYZ)nZ#P(y#P#Q*d#Q#p(y#p#q)n#q;'S(y;'S;=`+a<%lO(yV+dP;=`<%l(yV+nVdQkTOY$cZ#O$c#O#P,T#Q#p$c#q;'S$c;'S;=`$z<%lO$cV,[UdQiTOY$cZ#P$c#Q#p$c#q;'S$c;'S;=`$z<%lO$cV,uOfQkTV,|WdQkTOY$cZ#P$c#Q#R$c#R#S-f#S#p$c#q;'S$c;'S;=`$z<%lO$cV-mUdQ]TOY$cZ#P$c#Q#p$c#q;'S$c;'S;=`$z<%lO$cV.WVdQkTOY$cZ#P$c#Q#o$c#o#p.m#q;'S$c;'S;=`$z<%lO$cV.rYdQOY/bZ#P/b#P#Q0S#Q#o/b#o#p2V#p#q0S#q#r0z#r;'S/b;'S;=`2P<%lO/bV/gXdQOY/bZ#P/b#P#Q0S#Q#p/b#p#q0S#q#r0z#r;'S/b;'S;=`2P<%lO/bT0VUOY0SZ#q0S#q#r0i#r;'S0S;'S;=`0t<%lO0ST0lP#q#r0oT0tO`TT0wP;=`<%l0SV1PVdQOY$cZ#P$c#Q#p$c#q#r1f#r;'S$c;'S;=`$z<%lO$cV1mUdQ`TOY$cZ#P$c#Q#p$c#q;'S$c;'S;=`$z<%lO$cV2SP;=`<%l/bV2[YdQOY2VYZ2zZ#P2V#P#Q4_#Q#p2V#p#q4_#q#r5u#r;'S2V;'S;=`:_<%lO2VT2}TO#q2z#q#r3^#r;'S2z;'S;=`4X<%lO2zT3aTO#q2z#q#r3p#r;'S2z;'S;=`4X<%lO2zT3sTO#q2z#q#r4S#r;'S2z;'S;=`4X<%lO2zT4XOaTT4[P;=`<%l2zT4bVOY4_YZ2zZ#q4_#q#r4w#r;'S4_;'S;=`5o<%lO4_T4zTO#q2z#q#r5Z#r;'S2z;'S;=`4X<%lO2zT5`T`TO#q2z#q#r4S#r;'S2z;'S;=`4X<%lO2zT5rP;=`<%l4_V5zYdQOY6jYZ2zZ#P6j#P#Q2z#Q#p6j#p#q2z#q#r9h#r;'S6j;'S;=`9b<%lO6jV6oYdQOY6jYZ2zZ#P6j#P#Q2z#Q#p6j#p#q2z#q#r7_#r;'S6j;'S;=`9b<%lO6jV7dYdQOY6jYZ2zZ#P6j#P#Q2z#Q#p6j#p#q2z#q#r8S#r;'S6j;'S;=`9b<%lO6jV8XYdQOY6jYZ2zZ#P6j#P#Q2z#Q#p6j#p#q2z#q#r8w#r;'S6j;'S;=`9b<%lO6jV9OUdQaTOY$cZ#P$c#Q#p$c#q;'S$c;'S;=`$z<%lO$cV9eP;=`<%l6jV9oYdQ`TOY6jYZ2zZ#P6j#P#Q2z#Q#p6j#p#q2z#q#r8w#r;'S6j;'S;=`9b<%lO6jV:bP;=`<%l2VV:nPpSeQlP#p#q:qS:vOpSV:}WdQkTOY(`Z#P(`#P#Q;g#Q#p(`#p#q;g#q;'S(`;'S;=`;l<%lO(`T;lOhTV;oP;=`<%l(`",
    tokenizers: [lineStart, 0, 1, 2],
    topRules: { "Document": [0, 9] },
    tokenPrec: 399
  });
  const parser$1 = lr.LRParser.deserialize({
    version: 14,
    states: "!jQQOPOOO`OQO'#C^OOOO'#Cd'#CdOkOPO'#CaOOOO'#Cg'#CgOyOPO'#CeQZOPOOQZOPOOOOOO,58x,58xOOOO-E6b-E6bOOOO,59P,59POOOO-E6c-E6c",
    stateData: "!a~ORPOUQOVQO[TO~OSWOYQX[QX~OUQOVQOYTX[TX~ORPOUQOVQOYXX[XX~ORUVU~",
    goto: "u[PP]PP]PPahPoTSOTSROTRXRSUOVRZUQVORYT",
    nodeNames: "⚠ MarkupBody Annotation AnnotationName AnnotationValue Line Text At",
    maxTerm: 12,
    skippedNodes: [0],
    repeatNodeCount: 2,
    tokenData: ",_~R[OXwXY!eYZ'eZ]w]^'j^pwpq!eq!bw!b!c'p!c;'Sw;'S;=`!_<%lOwR!OTSQUPOYwZ]w^;'Sw;'S;=`!_<%lOwR!bP;=`<%lwR!lYSQUPOXwXY!eZ]w^pwpq!eq!bw!b!c#[!c;'Sw;'S;=`!_<%lOwR#c_SQUPOYwZ]w^}w}!O$b!O!Qw!Q![$b![!cw!c!}$b!}#Rw#R#S$b#S#Tw#T#o$b#o;'Sw;'S;=`!_<%lOwR$keSQRPUPOXwXY%|Z]w^pwpq%|q}w}!O$b!O!Qw!Q![$b![!]&{!]!_w!_!`&{!`!cw!c!}$b!}#Rw#R#S$b#S#Tw#T#o$b#o;'Sw;'S;=`!_<%lOwR&V[SQRPUPOXwXY%|Z]w^pwpq%|q![w![!]&{!]!_w!_!`&{!`;'Sw;'S;=`!_<%lOwR'UTSQRPUPOYwZ]w^;'Sw;'S;=`!_<%lOw~'jO[~~'mPYZ'eR'w_SQVPOY(vZ](v^}(v}!O)b!O!Q(v!Q![)b![!c(v!c!})b!}#R(v#R#S)b#S#T(v#T#o)b#o;'S(v;'S;=`)[<%lO(vQ({TSQOY(vZ](v^;'S(v;'S;=`)[<%lO(vQ)_P;=`<%l(vR)ieSQRPOX(vXY*zZ](v^p(vpq*zq}(v}!O)b!O!Q(v!Q![)b![!]+w!]!_(v!_!`+w!`!c(v!c!})b!}#R(v#R#S)b#S#T(v#T#o)b#o;'S(v;'S;=`)[<%lO(vR+R[SQRPOX(vXY*zZ](v^p(vpq*zq![(v![!]+w!]!_(v!_!`+w!`;'S(v;'S;=`)[<%lO(vR,OTSQRPOY(vZ](v^;'S(v;'S;=`)[<%lO(v",
    tokenizers: [0, 1],
    topRules: { "MarkupBody": [0, 1] },
    tokenPrec: 58
  });
  const registry = /* @__PURE__ */ new Map();
  function registerLanguage(markupName2, parser2) {
    registry.set(markupName2.toLowerCase(), parser2);
  }
  function unregisterLanguage(markupName2) {
    registry.delete(markupName2.toLowerCase());
  }
  function getLanguage(markupName2) {
    return registry.get(markupName2.toLowerCase()) ?? null;
  }
  function markupName(open, input) {
    return input.read(open.from, open.to).trim().replace(/^%%/, "").replace(/\s*[:=]$/, "").trim();
  }
  const markupContentParser = parser$1.configure({
    props: [
      highlight.styleTags({
        AnnotationName: highlight.tags.annotation,
        AnnotationValue: highlight.tags.attributeValue
      })
    ]
  });
  const parser = parser$2.configure({
    props: [
      highlight.styleTags({
        "Heading/...": highlight.tags.heading,
        HeadingMark: highlight.tags.processingInstruction,
        "Bold/...": highlight.tags.strong,
        "Italic/...": highlight.tags.emphasis,
        Monospace: highlight.tags.monospace,
        Preformatted: highlight.tags.monospace,
        Link: highlight.tags.link,
        LinkContent: highlight.tags.link,
        "LinkOpen LinkClose LinkSeparator": highlight.tags.punctuation,
        Plugin: highlight.tags.processingInstruction,
        Escape: highlight.tags.escape,
        LineBreak: highlight.tags.contentSeparator,
        HorizontalRule: highlight.tags.contentSeparator,
        ListMark: highlight.tags.list,
        DefinitionMark: highlight.tags.list,
        TableMark: highlight.tags.punctuation,
        MarkupOpen: highlight.tags.keyword,
        MarkupClose: highlight.tags.keyword
      }),
      language.foldNodeProp.add({
        Markup(node) {
          const open = node.firstChild;
          const close = node.lastChild;
          if (!open || !close || close.name !== "MarkupClose") return null;
          return { from: open.to, to: close.from };
        }
      })
    ],
    wrap: common.parseMixed((node, input) => {
      if (node.name !== "MarkupContent") return null;
      const open = node.node.prevSibling;
      if (!open || open.name !== "MarkupOpen") return null;
      return { parser: getLanguage(markupName(open, input)) ?? markupContentParser };
    })
  });
  const wikiLanguage = language.LRLanguage.define({
    name: "jspwiki",
    parser
  });
  function wiki() {
    return new language.LanguageSupport(wikiLanguage);
  }
  exports.getLanguage = getLanguage;
  exports.markupContentParser = markupContentParser;
  exports.parser = parser;
  exports.registerLanguage = registerLanguage;
  exports.unregisterLanguage = unregisterLanguage;
  exports.wiki = wiki;
  exports.wikiLanguage = wikiLanguage;
  Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
  return exports;
})({}, CodeMirror.lezer.common, CodeMirror.lezer.highlight, CodeMirror.language, CodeMirror.lezer.lr);
