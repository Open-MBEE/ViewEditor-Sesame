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


<title>View Editor</title>
</head>
<body>

<div id="page_title">
	<h1><a href="${pageContext.request.contextPath}/ui/">View Editor</a></h1>
</div>
    
   
<div id="projects">
  	<c:forEach var="proj" items="${projects}">
    	<span><a href="${pageContext.request.contextPath}/ui/projects/${proj['mdid']}?name=${proj['name']}">${proj['name']}</a></span>
	</c:forEach>
</div>
<div>
<p>
To use the view editor currently, your document must satisfy the following conditions:</p>
<ul>
<li>
You are using Viewpoint and Views in your document - sections generated using actions in activities will not show up here
</li>
<li>
The views in your document must be unique to that document - at least, no two documents exported to the editor should have a shared view (the view package element)
</li>
<li>
Your queries have no pure docbook tags - for example, you're not using docbook tags in your documentations, and any user scripts used will not give back verbatim docbook text.
</li>
</ul>

<p>
Things the view editor doesn't do right now:</p>
<ul>
<li>
edit elements that don't come from some magicdraw element's name, documentation, or property default value
</li>
<li>
have role/ldap group based permissions - right now any jpl us person can access this site and its views
</li>
</ul>
<p>Color codes:</p>
<p class="editable-name">Editable element names will be surrounded with a blue border</p>
<p class="editable-doc">Editable element documentation will be surrounded with a red border</p>
<p class="editable-dvalue">Editable property defualt values will be surrounded with a green border</p>

<p>Some advice on workflow:</p>
<ul>
<li>The import/export functions from DocGen plugin work at the magicdraw element level - if some view references an element, that element's name/documentation/default value will all be exported/imported together. 
Therefore, it's not advisable to have two people editing the same element or view from different tools. For example, If person A is editing the documentation of an element online, person B should not be editing its name in magicdraw. It's ok to edit the name online.</li>
<li>Two people should not be editing the same view online at the same time. Whoever saves last will win.</li>
<li>If you only want to change the view hierarchy order, export view hierarchy only and not the whole view. It's also best to do it at the document level.</li>
<li>In general, always do an import first before export. Doing an export without the "force" option will also check for available imports first</li>
<li>If you export anyway when there's some changes online that hasn't been imported, conflicting element names and documentation will be merged online and you'll have to resolve it.</li>
<li>If your view and the elements it references are in different modules, you may need to lock across shares to do imports...</li>
<li>As soon as you successfully import, you should commit. The view editor side will mark it as imported and assume it's committed. If you decide you don't want what's online, do an export and it'll wipe out the online contents. If an element is not successfully imported (it wasn't locked or something), you can still import until the model is changed - at which time the element online will be marked as imported.</li>
<li>Doing an import dry run will show you what elements will be changed if you did a real import.</li>
</ul>

</div>
<div class="clear"></div>

</body>
</html>