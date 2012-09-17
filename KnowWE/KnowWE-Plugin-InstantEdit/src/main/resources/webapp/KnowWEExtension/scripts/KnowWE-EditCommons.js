/**
 * The KNOWWE global namespace object. If KNOWWE is already defined, the
 * existing KNOWWE object will not be overwritten so that defined namespaces are
 * preserved.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
    var KNOWWE = {};
}

/**
 * Namespace: KNOWWE.core.plugin.instantedit The KNOWWE instant edit namespace.
 */
KNOWWE.editCommons = function() {

    return {

        mode: null,

        wikiText: new Object(),
        
        wrapHTML: function(id, locked, html) {
            var lockedHTML = "";
            if (locked) {
                lockedHTML = "<div class=\"error\">Another user has started to edit this page, but " + "hasn't yet saved it. You are allowed to further edit this page, but be " + "aware that the other user will not be pleased if you do so!</div>"
            }
            var openingDiv = "<div id='" + id + "' class='editarea'>";
            var closingDiv = "</div>\n";

            return openingDiv + lockedHTML + html + closingDiv;
        },

        hideTools: function() {
            $$('.markupTools').setStyle("display", "none");
        },

        showAjaxLoader: function(id) {
            var ajaxLoaderGif = new Element("img", {
                'id': 'instantedit_loader',
                'src': 'KnowWEExtension/images/ajax-100.gif',
                'class': 'ajaxloader'
            });
            $(id).appendChild(ajaxLoaderGif);
        },

        hideAjaxLoader: function() {
            var ajaxLoaderGif = $('instantedit_loader');
            if (ajaxLoaderGif) {
                ajaxLoaderGif.parentNode.removeChild(ajaxLoaderGif);
            }
        },

        reloadPage: function() {
            // reload page. remove version attribute if there
            var hrefSplit = window.location.href.split('?');
            if (hrefSplit.length == 1) {
                window.location.reload();
                return;
            }
            var path = hrefSplit[0];
            var args = hrefSplit[1].split('&');
            var newLocation = path;
            for (var i = 0; i < args.length; i++) {
                if (args[i].indexOf('version=') == 0) continue;
                newLocation += i == 0 ? '?' : '&';
                newLocation += args[i];
            }
            window.location = newLocation;
            window.location.reload();
        },

        // Maybe return given messages instead
        onErrorBehavior: function() {
            _EC.hideAjaxLoader();
            if (this.status == null) return;
            switch (this.status) {
            case 0:
                // server not running, do nothing.
                break;
            case 403:
                alert("You are not authorized to change this page.");
                break;
            case 404:
                alert("This page no longer exists. Please reload.");
                break;
            case 409:
                alert("This section has changed since you " + "loaded this page. Please reload the page.");
                break;
            default:
                alert("Error " + this.status + ". Please reload the page.");
                break;
            }
        },

        executeIfPrivileged: function(grantedFN, forbiddenFN) {
            var params = {
                action: 'CheckCanEditPageAction'
            }

            var options = {
                url: KNOWWE.core.util.getURL(params),
                response: {
                    action: 'none',
                    fn: function() {
                        var canedit = JSON.parse(this.responseText).canedit;

                        if (canedit) {
                            grantedFN();
                        } else if (forbiddenFN) {
                            forbiddenFN();
                        }
                    },
                    onError: _EC.onErrorBehavior,
                }
            }
            new _KA(options).send();
        },

        sendChanges: function(newWikiText, params, fn) {

            var options = {
                url: KNOWWE.core.util.getURL(params),
                data: newWikiText,
                response: {
                    action: 'none',
                    fn: function() {
                        // TODO: Remove?
                        window.onbeforeunload = null;
                        window.onunload = null;
                        $(window).removeEvents('beforeunload');
                        $(window).removeEvents('unload');
                        fn();
                    },
                    onError: _EC.onErrorBehavior
                }
            }
            new _KA(options).send();
        },

        getWikiText: function(id, actionName) {

            var tempWikiText = _EC.wikiText[id];

            if (tempWikiText != null) return tempWikiText;

            if (actionName == null) actionName = 'GetWikiTextAction';

            var params = {
                action: actionName,
                KdomNodeId: id
            };

            var options = {
                url: KNOWWE.core.util.getURL(params),
                async: false,
                response: {
                    action: 'none',
                    // for FF 3.6 compatibility, we can't use the function fn
                    // in synchronous call (no onreadystatechange event fired)
                    onError: _EC.onErrorBehavior
                }
            };
            var ajaxCall = new _KA(options);
            ajaxCall.send();
            _EC.wikiText[id] = ajaxCall.getResponse();
            return _EC.wikiText[id];
        }

    }
}();


KNOWWE.editCommons.elements = function() {

    return {

        getSaveButton: function(jsFunction) {
            return "<a class=\"action save\" " + "href=\"javascript:" + jsFunction + "\"" + ">Save</a>";
        },

        getCancelButton: function(jsFunction) {
            return "<a class=\"action cancel\" href=\"javascript:" + jsFunction + "\"" + ">Cancel</a>";
        },

        getDeleteSectionButton: function(jsFunction) {
            return "<a class=\"action delete\" href=\"javascript:" + jsFunction + "\"" + ">Delete</a>";
        },

        getSaveCancelDeleteButtons: function(id, additionalButtonArray) {
            var buttons = _EC.mode.getSaveCancelDeleteButtons(id, additionalButtonArray);
            return _EC.mode.getButtonsTable(buttons);
        }

    }

}();

var _EC = KNOWWE.editCommons;