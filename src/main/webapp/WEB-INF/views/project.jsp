<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/styles.css"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.8.1.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.jstree.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/tiny_mce/tiny_mce.js"></script>
<script language="javascript" type="text/javascript">
$(document).ready(function(){
	$('#docnavwrapper').jstree({
		"plugins": ["themes", "html_data", "search", "ui"],
		"core": {
			"animation": 0,
			"initially_open": [<c:forEach var="vol" items="${topVolumes}">"${vol}-nav",</c:forEach>]
		},
		"ui": {
			"select_limit": 1
		},
		"themes": {
			"theme": "apple",
			"dots": false,
			"icons": true
		}
	});

   $('#docnavsearch').keyup(function() {
        $('#docnavwrapper').jstree("search",$(this).val());
    });

// don't want the initial select to cause the page reload
   var initialSelect = true;
   $("#docnavwrapper").bind("select_node.jstree", function(event, data) {
       var selectedObj = data.rslt.obj;
       var href = selectedObj.find('a')[0];
       if (initialSelect) {
             initialSelect = false;    
       } else {
       	// load the new location
           window.location = href;
       };
   });

  $('#docnavsearch').keyup(function() {
       $('#docnavwrapper').jstree("search",$(this).val());
   });
});
</script>

<title>${projectName}</title>
</head>
<body>

<div id="page_title">
	<h1><a href="${pageContext.request.contextPath}/ui/">View Editor</a></h1>
</div>
    
<div id="user">
    <p><sec:authentication property="principal.username"/> - <a href="<c:url value='/j_spring_security_logout'/>">Logout</a> </p>
</div>
   
<div id="projects">
  	<c:forEach var="proj" items="${projects}">
    	<span class="${projectId == proj['mdid'] ? 'tabHighlight' : 'normal'}"><a href="${pageContext.request.contextPath}/ui/projects/${proj['mdid']}?name=${proj['name']}">${proj['name']}</a></span>
	</c:forEach>
</div>

<div id="nav">
<div>
Search: <input id="docnavsearch" type="text" size="15"/>
</div><br/>
<div id="docnavwrapper">
	<ul id="docnav">
	<c:forEach var="vol" items="${topVolumes}">
		<template:voltree node="${volumeTree[vol]}"/>
	</c:forEach>
	</ul>
</div>
</div>

<div class="clear"></div>

</body>
</html>