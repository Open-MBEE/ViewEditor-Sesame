<%@ tag description="display a tree as unordered list" pageEncoding="UTF-8" %>
<%@ attribute name="node" type="java.util.Map" required="true" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<li id="${node['mdid']}-nav"><a class="jstree-view" href="${pageContext.request.contextPath}/ui/views/${node['mdid']}?name=${node['name']}">${node['section']}${node['name']}</a>
<c:if test="${fn:length(node['children']) > 0}">
    <ul>
    <c:forEach var="child" items="${node['children']}">
        <template:tree node="${viewTree[child]}"/>
    </c:forEach>
    </ul>
</c:if>
</li>