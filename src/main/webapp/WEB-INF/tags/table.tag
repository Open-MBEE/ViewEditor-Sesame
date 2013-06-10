<%@ tag description="display the table header or body" pageEncoding="UTF-8" %>
<%@ attribute name="celltag" type="java.lang.String" required="true" %>
<%@ attribute name="contents" type="java.util.List" required="true" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:forEach var="bodyRow" items="${contents}">
	<tr>
		<c:forEach var="bodyEntry" items="${bodyRow}">
			<${celltag} rowspan="${bodyEntry['rowspan']}" colspan="${bodyEntry['colspan']}">
				<c:choose>
					<c:when test="${bodyEntry['type'] == 'doc'}">
						<div class="editable">
						<div class="docinput" id="${bodyEntry['mdid']}-doc">
							${bodyEntry["documentation"]}
						</div>
						</div>
					</c:when>
					<c:when test="${bodyEntry['type'] == 'name'}">
						<div class="editable">
						<span class="display ${bodyEntry['mdid']}-name_display">${bodyEntry["name"]}</span>
						<c:if test="${bodyEntry['edit'] == 'true'}">
						<input class="hidden textinput" id="${bodyEntry['mdid']}-name" type="text" value="${bodyEntry['name']}" size="15"/>
						</c:if>
						</div>
					</c:when>
					<c:when test="${bodyEntry['type'] == 'text'}">
						${bodyEntry["text"]}
					</c:when>
					<c:when test="${bodyEntry['type'] == 'dvalue'}">
						<div class="editable">
						<span class="display ${bodyEntry['mdid']}-dvalue_display">${bodyEntry["dvalue"]}</span>
						<c:if test="${bodyEntry['edit'] == 'true'}">
						<input class="hidden textinput" id="${bodyEntry['mdid']}-dvalue" type="text" value="${bodyEntry['dvalue']}" size="10"/>
						</c:if>
						</div>
					</c:when>
				</c:choose>
			</${celltag}>
		</c:forEach>
	</tr>
</c:forEach>