<%@ tag description="display a tree as unordered list" pageEncoding="UTF-8" %>
<%@ attribute name="node" type="java.util.Map" required="true" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<li id="${node['mdid']}-nav"><a class="jstree-vol" href="#">${node['name']}</a>
<c:if test="${fn:length(node['childDocuments']) > 0 || fn:length(node['childVolumes']) > 0}">
    <ul>
    <c:forEach var="childDocInfo" items="${node['childDocuments']}">
    	<li id="${childDocInfo['mdid']}-nav"><a class="jstree-doc" href="${pageContext.request.contextPath}/ui/views/${childDocInfo['mdid']}?name=${childDocInfo['name']}">${childDocInfo['name']}</a>
    </c:forEach>
    <c:forEach var="volid" items="${node['childVolumes']}">
    	<template:voltree node="${volumeTree[volid]}"/>
    </c:forEach>
    </ul>
</c:if>
</li>