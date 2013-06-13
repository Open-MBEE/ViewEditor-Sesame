<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jqueryFileTree/jqueryFileTree.css"/>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery-ui-1.10.3.min.css"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.8.1.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-ui-1.10.3.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/tiny_mce/tiny_mce_popup.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jqueryFileTree/jqueryFileTree.js"></script>
<script type="text/javascript">
	function selectFile() {
		var baseURL = document.URL.replace('artifact\/browse', '');
		var path = document.getElementById("path").value + "/";
		window.opener.document.getElementById("src").value = baseURL + path;
		close();
	};
</script>
<script type="text/javascript">
$(document).ready( function() {
    $('#container_id').fileTree({
        root: 'artifacts',
        script: '/editor/js/jqueryFileTree/connectors/jqueryFileTree.jsp',
        expandSpeed: 1,
        collapseSpeed: 1,
        multiFolder: true,
        showFiles: true
    }, function(file) {
        $('#path').val(file);
    });
});
</script>

<title>Browse artifacts</title>
</head>
<body>
	<div id="browse">
		<form id="browseForm" action="" method="post" enctype="multipart/form-data">
		<table>
				<tr>
					<td><label for="path">File to embed</label></td>
				</tr>
				<tr>
					<td>
					<input id="path" name="path" type="text" size="40" readonly/>
					<div id="container_id" class="jqueryFileTreeDiv"></div>
					</td>
				</tr>
				<tr>
					<td>
						<input type="button" value="Select" onclick="selectFile();" />
						<input type="button" name="cancel" id="cancel" value="Cancel" onclick="tinyMCEPopup.close();" />
					</td>
				</tr>
			</table>
		</form>
	</div>

</body>
</html>