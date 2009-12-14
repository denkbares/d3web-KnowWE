/**
 * Title: KnowWE-plugin-hermes Contains all javascript functions concerning the
 * KnowWE plugin Hermes.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
    /**
     * The KNOWWE global namespace object. If KNOWWE is already defined, the
     * existing KNOWWE object will not be overwritten so that defined namespaces
     * are preserved.
     */
    var KNOWWE = {};
}

/**
 * Namespace: KNOWWE.plugin.hermes The KNOWWE plugin d3web namespace.
 * Initialized empty to ensure existence.
 */
KNOWWE.plugin.hermes = function() {
    return {}
}();

/**
 * save new input.
 */
function sendTimeEventSearchRequest() {

    var searchFrom = document.getElementById("hermesSearchFrom");
    var searchTo = document.getElementById("hermesSearchTo");
    var startIndex = document.getElementById("startIndexTimeline");
    var resultCount = document.getElementById("hermesSearchResultCount");

    var params = {
        action : 'SearchTimeEventsAction',
        count : resultCount.value,
        from : searchFrom.value,
        to : searchTo.value,
        startIndex : startIndex.value

    }

    var options = {
        url : KNOWWE.core.util.getURL(params),
        response : {
            action : 'insert',
            ids : [ 'hermesSearchResult' ],
            fn : KNOWWE.core.util.addCollabsiblePluginHeader
        }
    }

    new _KA(options).send();

}

/**
 * save new input.
 */
function sendFilterLevel(level, user) {

    var params = {
        action : 'SetFilterLevelAction',
        user : user,
        level : level

    }

    var options = {
        url : KNOWWE.core.util.getURL(params),
        response : {
            action : 'insert',
            ids : [ '' ],
            fn : window.location.reload
        }
    }

    new _KA(options).send();

}

/**
 * starts the hermes quiz.
 */
function startQuiz(user) {

    var params = {
        action : 'StartQuizAction',
        user : user,
    }

    var options = {
        url : KNOWWE.core.util.getURL(params),
        response : {
            action : 'insert',
            ids : [ 'quiz-question' ],
        }
    }

    new _KA(options).send();

}


/**
 * starts the hermes quiz.
 */
function quizAnswer(user, answer) {

    var params = {
        action : 'SetAnswerAction',
        user : user,
        answer : answer,
    }

    var options = {
        url : KNOWWE.core.util.getURL(params),
        response : {
            action : 'insert',
            ids : [ 'quiz-question' ],
        }
    }

    new _KA(options).send();

}

/**
 * stops the hermes quiz.
 */
function stopQuiz(user) {

    var params = {
        action : 'StopQuizAction',
        user : user,
    }

    var options = {
        url : KNOWWE.core.util.getURL(params),
        response : {
            action : 'insert',
            ids : [ 'quiz-question' ],
        }
    }

    new _KA(options).send();

}

/**
 * Namespace: KNOWWE.plugin.hermes.conceptPopup The namespace of popup for
 * semi-automated formalization.
 */
KNOWWE.plugin.hermes.conceptPopup = function() {
    /**
     * Variable: sTimer Stores an timer object. Used to remove the overlay
     * question element after a certain amount of time with no user action.
     * 
     * Type: Object
     */
    var sTimer = null;

    return {
        /**
         * Function: init Initializes the semantic popups. Adds to every
         * question of the questionsheet a popup action.
         */
        init : function() {
            if (_KS('.conceptLink').length != 0) {
                var array = _KS('.conceptLink');
                array.each(function(element) {
                    _KE.add(
                        'click',
                        element,
                        KNOWWE.plugin.hermes.conceptPopup.showConceptOverlay);
                });
            }
        },
        /**
         * Function: overlayActions Contains all actions that can occur in an
         * question overlay element. Used to initialize the actions after the
         * overlay is created.
         */
        overlayActions : function() {
            if (_KS('.confirmOption').length != 0) {
                _KS('.confirmOption').each(
                    function(element) {
                        _KE.add('click', element,
                                KNOWWE.plugin.hermes.conceptPopup.sendConfirm);
                });
            }

            if (_KS('#o-lay')) {
                _KE.add('mouseout', _KS('#o-lay'), function(e) {
                    var father = _KE.target(e);
                    var e = father;
                    var id = e.getAttribute('id');
                    while (id != 'o-lay') {
                        e = e.parentNode;
                        id = e.getAttribute('id');
                    }
                    if (e.getAttribute('id') == 'o-lay' || father == e) {
                        clearTimeout(sTimer);
                        sTimer = setTimeout(function() {
                            _KS('#o-lay')._remove();
                        }, 4000);
                    }
                });
                _KE.add('click', _KS('#o-lay-close'), function() {
                    _KS('#o-lay')._remove();
                    clearTimeout(sTimer);
                });
            }
        },

        sendConfirm : function(e) {
            var element = _KE.target(e);

            var property = element.getAttribute('rel');
            var object = element.getAttribute('object');
            var kdomid = element.getAttribute('kdomid');
            var subject = element.getAttribute('subject');
            
            var params = {
                action : 'InsertRelationAction',
                subject : subject,
                property : property,
                object : object,
                kdomid : kdomid
            }

            var options = {
                url : KNOWWE.core.util.getURL(params),
                response : {
                    action : 'none',
                    ids : [ 'hermesSearchResult' ],
                    fn : function(){
                        var node, topic;
                        node = element.getAttribute('ancestor');
                        topic = KNOWWE.helper.gup('page');
                        KNOWWE.core.rerendercontent.updateNode(node, topic, "render");
                    }
                }
            }
            new _KA(options).send();
             _KS('#o-lay')._remove();
            clearTimeout(sTimer);
        },

        /**
         * Function: handleForm Handles the selection of checkboxes.
         * 
         * Parameters: e - The current occurred event.
         */
        handleForm : function(e) {
            var el = new _KN(_KE.target(e));
            var rel = eval("(" + el.getAttribute('rel') + ")");
            KNOWWE.plugin.d3web.semantic.send(rel.url, null);
        },
        /**
         * Function: handleNum Handles the input in an HTMLInput element
         * 
         * Parameters: e - The current occurred event
         */
        handleNum : function(e) {
            var bttn = (_KE.target(e).value == 'ok');
            var key = (e.keyCode == 13);
            if (!(key || bttn))
                return false;

            var rel = null, el = null;

            el = new _KN(_KE.target(e));
            if (el.value == "ok") {
                el = el.previousSibling;
            }
            rel = eval("(" + el.getAttribute('rel') + ")");

            if (!rel)
                return;
            KNOWWE.plugin.d3web.semantic.send(rel.url, {
                ValueNum : el.value
            });
        },
        /**
         * Function: handleOC Handles the selection within an one choice
         * question.
         * 
         * Parameters: e - The current occurred event
         */
        handleOC : function(e) {
            var el = _KE.target(e);
            var rel = eval("(" + el.getAttribute('rel') + ")");
            KNOWWE.plugin.d3web.semantic.send(rel.url, null);
        },
        /**
         * Function: send Sends the user selection and stores it. Used in the
         * other handleXXX functions to send an AJAX request in order to store
         * the users choice.
         * 
         * Parameters: url - The URL of the request values - The selected value
         */
        send : function(url, values) {

            var tokens = [];
            if (values) {
                for (keys in values) {
                    tokens.push(keys + "=" + encodeURIComponent(values[keys]));
                }
            }
            var options = {
                url : url + "&" + tokens.join('&'),
                action : 'none',
                fn : KNOWWE.plugin.d3web.actions.update
            }
            new _KA(options).send();
            _KS('#o-lay')._remove();
            clearTimeout(sTimer);
        },
        /**
         * Function: showOverlayQuestion Gets the data that is shown as an
         * overlay over the current question.
         * 
         * Parameters: e - The latest event object
         */
        showConceptOverlay : function(e) {
            var el = _KE.target(e);

            var kdomid = el.id;
            var contentElement = _KS('#' + kdomid + '_popupcontent');
            var contentT = contentElement.innerHTML;

            if (!el.getAttribute('rel'))
                return;

            var rel = eval("(" + el.getAttribute('rel') + ")");

            var mousePos = KNOWWE.helper.mouseCoords(e);
            var mouseOffset = KNOWWE.helper.getMouseOffset(el, e);

            var olay = new KNOWWE.helper.overlay( {
                cursor : {
                    top : mousePos.y - mouseOffset.y,
                    left : mousePos.x - mouseOffset.x
                },
                content : contentT,
                fn : KNOWWE.plugin.hermes.conceptPopup.overlayActions
            });
        }
    }
}();

(function init() {
    if (KNOWWE.helper.loadCheck( [ 'Wiki.jsp' ])) {
        window.addEvent('domready', function() {
            KNOWWE.plugin.hermes.conceptPopup.init();
        });
    }
}());