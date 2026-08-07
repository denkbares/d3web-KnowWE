<%--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
--%>
<%--
    KnowWE replaces the Haddock quick search with the section based one.

    Deliberately without the ".searchbox .dropdown-menu" structure: Haddock's Wiki.js binds Findpages and Recents to
    exactly that selector, so leaving it out means those never attach and there is nothing to unhook. The dropdown is
    filled by KnowWE-Plugin-Search.js.

    The form still posts to Search.jsp, so Enter without a selected suggestion lands on the full search page, and the
    box keeps working with JavaScript disabled.
--%>
<%@ taglib uri="http://jspwiki.apache.org/tags" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setLocale value="${prefs.Language}" />
<fmt:setBundle basename="templates.default"/>

<form action="<wiki:Link jsp='Search.jsp' format='url'/>"
	  class="searchbox knowwe-quicksearch"
	  id="searchForm" role="search"
	  accept-charset="<wiki:ContentEncoding />">
	<wiki:CsrfProtection/>
	<input type="text" name="query" id="query"
		   class="knowwe-quicksearch-input"
		   autocomplete="off" spellcheck="false"
		   accesskey="f"
		   placeholder="<fmt:message key='sbox.search.submit'/>"
		   aria-label="<fmt:message key='sbox.search.submit'/>" />
	<div class="knowwe-quicksearch-panel" id="knowwe-quicksearch-panel" hidden></div>
</form>
