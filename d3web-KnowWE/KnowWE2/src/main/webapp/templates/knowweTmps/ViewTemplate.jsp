<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<fmt:setBundle basename="templates.default"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html id="top" xmlns="http://www.w3.org/1999/xhtml">

<head>
  <title>
    <fmt:message key="view.title.view">
      <fmt:param><wiki:Variable var="ApplicationName" /></fmt:param>
      <fmt:param><wiki:PageName /></fmt:param>
    </fmt:message>
  </title>
  <wiki:Include page="commonheader.jsp"/>
  <wiki:CheckVersion mode="notlatest">
    <meta name="robots" content="noindex,nofollow" />
  </wiki:CheckVersion>
  <wiki:CheckRequestContext context="diff|info">
    <meta name="robots" content="noindex,nofollow" />
  </wiki:CheckRequestContext>
  <wiki:CheckRequestContext context="!view">
    <meta name="robots" content="noindex,follow" />
  </wiki:CheckRequestContext>
     
  <link rel="stylesheet" media="screen, projection" type="text/css"
     href="KnowWEExtension/css/general.css"/>     
</head>

<body class="view" onload="kwikiOnLoad();">

<div id="wikibody" class="${prefs.Orientation}">
 
  <wiki:Include page="Header.jsp" />

  <div id="content">

    <div id="page">
    <table>
	<tr>
	<td>
      <wiki:Include page="PageActionsTop.jsp"/>
      <wiki:Content/>
      <wiki:Include page="PageActionsBottom.jsp"/>
      </td>
<td>
<div id="knowwerightbar">
	<div id="patternRightBarContents" style="min-height:200px;width:200px">
<p />
<div class="patternTop">
<div class="patternToolBar">
<div class="patternRightToolBarButtons">
<span class="patternButton"><a id="solutionsButton" href="javascript:void(0)" onclick="showSolutions();updateSolutions()" rel="nofollow" title='Show Solutions'>Solutions</a></span>
<span class="patternButton"><a id="dialogsButton" href="javascript:void(0)" onclick="showDialogs();updateDialogs()" rel="nofollow" title='Show Dialogs'>Dialog</a></span>		
<span class="patternButton"><a href="javascript:kwiki_call('KnowWE.jsp?action=KWiki_dpsClear&amp;KWikiWeb=default_web');showSolutions();updateSolutions()" rel="nofollow" title='Clear Session'>Cl</a></span>
<span class="patternButton"><a href="javascript:doShowHideOptionsMenu()" rel="nofollow" title='Show additional Options'>Opt</a></span>

<div id="kwikiOptionsMenu" style="visibility:hidden; z-index:99">
	<div onclick="doShowHideOptionsMenu()" class="patternButton" style="clear:left; text-align:right; width:13em"><a href="javascript:void(0)" onclick="updateSolutions();updateDialogs()" rel="nofollow" title='Update'>Update</a></div>
	<div onclick="doShowHideOptionsMenu()" class="patternButton" style="clear:left; text-align:right; width:13em"><a href="javascript:kwiki_window('KnowWE.jsp?renderer=KWiki_userFindings&amp;KWikiWeb=default_web')" rel="nofollow" title='Show all Findings'>Show all Findings</a></div>				
	<div onclick="doShowHideOptionsMenu()" class="patternButton" style="clear:left; text-align:right; width:13em"><a href="javascript:kwiki_window('KnowWE.jsp?renderer=KWiki_allSolutions&amp;KWikiWeb=default_web')" rel="nofollow" title='Show all Solutions'>Show all Solutions</a></div>	
	<div onclick="doShowHideOptionsMenu()" class="patternButton" style="clear:left; text-align:right; width:13em"><a href="javascript:kwiki_window('KnowWE.jsp?renderer=KWiki_viewKSSHistory&amp;KWikiWeb=default_web')" rel="nofollow" title='Show Dialog History'>Show Dialog History</a></div>
	<div onclick="doShowHideOptionsMenu()" class="patternButton" style="clear:left; text-align:right; width:13em"><a href="javascript:kwiki_call('KnowWE.jsp?action=KWiki_dpsClear&amp;KWikiWeb=default_web')" rel="nofollow" title='Clear Session'>Clear Session</a></div>				
<p />
</div>
<div id="kwikiSessions" style="position: absolute; left:3px; top:75px; visibility:hidden;overflow:visible; z-index:200">
MUSHHAHAHSAds
</div>
</div>
</div><!-- /patternToolBar-->
<div class="patternToolBarBottom"></div>
</div><!-- /patternTop-->
<p />
<div id="kwikiRightBarContents">
<div id="KnowWESolutions" >
</div>
<div id="KnowWEDialogs" style="visibility:hidden; max-width:15em; overflow:hidden">
</div>
</div><!-- /kwikiRightBarContents-->
</div><!-- /patternRightBarContents-->
       </div>
</td>
</tr>
</table>
    </div>


    <wiki:Include page="Favorites.jsp"/>

	<div class="clearbox"></div>
  </div>

  <wiki:Include page="Footer.jsp" />

</div>

</body>
</html>