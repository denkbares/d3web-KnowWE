/**
 * The KNOWWE global namespace object. If KNOWWE is already defined, the
 * existing KNOWWE object will not be overwritten so that defined namespaces are
 * preserved.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
    var KNOWWE = {};
}

var toSelect;
/**
 * The KNOWWE.plugin global namespace object. If KNOWWE.plugin is already
 * defined, the existing KNOWWE.plugin object will not be overwritten so that
 * defined namespaces are preserved.
 */
if (typeof KNOWWE.plugin == "undefined" || !KNOWWE.plugin) {
    KNOWWE.plugin = function () {
        return {}
    }
}

/**
 * The KNOWWE.plugin.ontology global namespace object. If KNOWWE.plugin.ontology
 * is already defined, the existing KNOWWE.plugin.ontology object will not be
 * overwritten so that defined namespaces are preserved.
 */
KNOWWE.plugin.ontology = function () {
    return {
        commitOntology: function (sectionID) {
            new _KA({
                url: KNOWWE.core.util.getURL({
                    action: 'CommitOntologyAction',
                    SectionID: sectionID
                }),
                fn: function () {
                    KNOWWE.core.util.hideProcessingIndicator();
                    location.reload();
                }
            }).send();
            KNOWWE.core.util.showProcessingIndicator();
        },
        expandLazyReference: function (sectionID, newReferenceText, rerenderID) {
            var params = {
                action: 'ReplaceKDOMNodeAction',
                TargetNamespace: sectionID,
                KWikitext: newReferenceText,
            };
            var options = {
                url: KNOWWE.core.util.getURL(params),
                response: {
                    fn: function () {
                        // todo: rerender markup block
                        location.reload();
                    }
                }
            }
            new _KA(options).send();
        }
    }
}();


KNOWWE.plugin.sparql.editTool = KNOWWE.plugin.defaultEditTool;
KNOWWE.plugin.sparql.editTool.generateButtons = function (id) {
    return _EC.elements.getSaveCancelDeleteButtons(id,
        ["<a class='action format' onclick='KNOWWE.plugin.sparql.editTool.format(\"" + id + "\")'>" +
        "Format</a>"]);
};

// todo move to Java and use methods of Strings.java
/*
 *   Formats Sparql markups
 *   current:
 *   * deletes unnecessary Whitespaces (e.g. 2+ empty lines --> 1 empty line, spaces at the beginning of a line),
 *   * adds a \n after "%%Sparql" if its missing,
 *   * adds tabs at the beginning of a line if the depth is 1 or higher // todo one tab less before a "}"
 *   * adds new lines after "{" and "." // todo add no new line if "}" is in the same line
 */
KNOWWE.plugin.sparql.editTool.format = function (id) {
    var unnecessaryWhitespaces = new RegExp("(\\n(\\s)*){3,}", "g");
    var replaceUnnecessaryWhitspacesWith = "\n\n";
    var missingWhitespaces = new RegExp("(%%Sparql)(?=[^\\n])", "g");
    var replaceMissingWhitespacesWith = "$&\n";

    var textarea = jq$("#defaultEdit" + id);
    var wikiText = textarea.val();

    for (var h = 0; h < 5; h++) {

        wikiText = wikiText.replace(missingWhitespaces, replaceMissingWhitespacesWith);
        wikiText = wikiText.replace(unnecessaryWhitespaces, replaceUnnecessaryWhitspacesWith);

        var quoted = false; // "
        var comment = false; // #
        var atLineStart = true; // \n
        var startNewLine = false;
        var spacesAtLineStart = false;
        var defaultdepth = 0;
        var depth = defaultdepth;
        var numberOfTabs = depth;
        var operationAtIndex = [];

        for (var i = 0; i < wikiText.length; i++) {
            if (wikiText[i] == "\n") {
                atLineStart = true;
                comment = false;
                startNewLine = false;
                continue;
            }
            if (!comment && wikiText[i] == "\"") {
                quoted = !quoted;
            }
            if (wikiText[i] == "#") {
                comment = true;
            }
            if (atLineStart) {
                numberOfTabs = depth;
                atLineStart = false;
                if (wikiText[i] == " " || wikiText[i] == "\t") {
                    spacesAtLineStart = true;
                }
            }
            if (numberOfTabs > 0 && wikiText[i] != "\t") {
                operationAtIndex.push({index: i, action: "addTab"});
                numberOfTabs--;
            }
            if (numberOfTabs > 0 && wikiText[i] == "\t") {
                numberOfTabs--;
            }
            if (!comment && !quoted) {
                if (startNewLine && wikiText[i] != "\n") {
                    operationAtIndex.push({index: i, action: "addNewLine"});
                    startNewLine = false;
                }
                if (wikiText[i] == "{") {
                    depth++;
                    startNewLine = true;
                }
                if (wikiText[i] == "}") {
                    depth--;

                }
                if (wikiText[i] == ".") {
                    startNewLine = true;
                }
            }
            if (spacesAtLineStart) {
                if (wikiText[i] == " ") {
                    operationAtIndex.push({index: i, action: "removeWhitespace"});
                } else if (wikiText[i] != "\t") {
                    spacesAtLineStart = false;
                }
            }
        }

        if (operationAtIndex.length == 0) {
            break;
        }

        for (var i = operationAtIndex.length - 1; i >= 0; i--) {
            if (operationAtIndex[i].action == "addNewLine") {
                wikiText = wikiText.splice(operationAtIndex[i].index, 0, "\n");
            } else if (operationAtIndex[i].action == "addTab") {
                wikiText = wikiText.splice(operationAtIndex[i].index, 0, "\t");
            } else if (operationAtIndex[i].action == "removeWhitespace") {
                wikiText = wikiText.slice(0, operationAtIndex[i].index) + wikiText[operationAtIndex[i].index].replace(/ /, "") + wikiText.slice(operationAtIndex[i].index + 1);
            }
            console.log(wikiText);
        }
    }

    textarea.val(wikiText);


}

String.prototype.splice = function (idx, rem, s) {
    return (this.slice(0, idx) + s + this.slice(idx + Math.abs(rem)));
};

jq$(document)
    .ready(
    function () {
        // Prepare for instant table editor with custom
        // auto-complete
        KNOWWE.plugin.ontology.tableEditTool = KNOWWE.plugin.tableEditTool
            .create(function (callback, prefix, spreadsheet, row, col) {
                AutoComplete.sendCompletionAction(function (byAjax) {
                    AutoComplete.unquoteTermIdentifiers(byAjax);
                    callback(byAjax);
                }, prefix, "OntologyTableMarkup");
            });
    });
