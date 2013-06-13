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
	function upload() {
 		var url = $('#filename');

		if (url.val() && url.val().length > 0) {
			var filename = url.val().replace('C:\\fakepath\\', '');
			var path = $('#path').val();// + '/';
			var baseURL = document.URL.replace('artifact\/upload', 'artifacts');
			var save = true;

			if ($('a[rel="' + path + filename + '"]').length > 0) {
				if (!confirm("File already exists. Are you sure you want to overwrite?")) {
					save = false;
				};
			}
 		
			if (save) {
				window.opener.document.getElementById('src').value = baseURL + path + filename;
			
	 			var formData = new FormData($('form')[0]);
				$.ajax({
					url : '/editor/artifact/upload/',
					type : 'POST',
					data : formData,
					cache : false,
					contentType : false,
					processData : false,
					async : false,
					enctype: 'multipart/form-data',
				});
	 
				close();
			}
		} else {
			alert('File to upload not specified');
		}
	};
	 
	function createfolder() {
		var path = $('#path').val();
		if (!path || /^\s*$/.test(path)) {
			path = 'artifacts/';
		};
		$('#ui-id-1').html('Create in: ' + path);
		$('#dialog-form').dialog('open');
	};
</script>
<script type="text/javascript">
$(document).ready( function() {
	var foldername = $('#foldername'),
		allFields = $([]).add(foldername);
	
    $('#container_id').fileTree({
        root: 'artifacts',
        script: '/editor/js/jqueryFileTree/connectors/jqueryFileTree.jsp',
        expandSpeed: 1,
        collapseSpeed: 1,
        multiFolder: true,
        showFiles: false
    }, function(file) {
        $('#path').val(file);
    });
    
    $( '#dialog-form' ).dialog({
        autoOpen: false,
        height: 150,
        width: 300,
        modal: true,
        buttons: {
          'Create folder': function() {
            if (foldername.val() && foldername.val().length >0) {
	        	allFields.removeClass( 'ui-state-error' );
	   
	            $.post('/editor/artifact/folder/', {path: $('#path').val() + foldername.val()});
	            $( this ).dialog( 'close' );
	            location.reload(true);
            } else {
            	alert('Invalid foldername');
            }
          },
          Cancel: function() {
            $( this ).dialog( 'close' );
          }
        },
        close: function() {
          allFields.val( '' ).removeClass( 'ui-state-error' );
        }
      });
});
</script>

<title>Upload artifact</title>
</head>
<body>
	<div id="upload">
		<form id="uploadForm" action="" method="post" enctype="multipart/form-data">
		<table>
				<tr>
					<td><label for="filename">File to upload</label></td>
				</tr>
				<tr>
					<td><input id="filename" name="fileUpload" type="file" /></td>
				</tr>
				<tr>
					<td><label for="path">Directory to upload to (root is artifacts)</label></td>
				</tr>
				<tr>
					<td>
					<input id="path" name="path" type="text" size="40" value="artifacts/" readonly/>
					<div id="container_id" class="jqueryFileTreeDiv"></div>
					</td>
				</tr>
				<tr>
					<td>
						<input type="button" value="Upload" onclick="upload();" />
						<input type="button" value="New Folder..." onclick="createfolder();" />
						<input type="button" name="cancel" id="cancel" value="Cancel" onclick="tinyMCEPopup.close();" />
					</td>
				</tr>
			</table>
		</form>
	</div>

	<div id="dialog-form" title="Create new folder">
		<form>
			<fieldset>
				<label for="foldername">Folder Name</label>
				<input type="text" name="foldername" id="foldername" class="text ui-widget-content ui-corner-all" />
			</fieldset>
		</form>
	</div>

</body>
</html>