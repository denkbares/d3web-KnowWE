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

    var textarea = jq$("#defaultEdit" + id);
    var wikiText = textarea.val();

    var ajax = jq$.ajax("action/SparqlFormatAction", {
        data: {
            sectionID: id,
            wikiText: wikiText
        },
        cache: false,
        success: function (json) {
            textarea.val(json.wikiText);
        }
    });

    return;

}
