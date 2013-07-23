<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/styles.css"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.8.1.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.jstree.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/tiny_mce/tiny_mce.js"></script>
<script language="javascript" type="text/javascript">
var curedit = false;
var addingComment = false;
var env = window.location.pathname.split('/')[1];

tinyMCE.init({
	theme : "advanced",
    mode : "none",
    content_css : "${pageContext.request.contextPath}/css/tinymce_content.css",
    theme_advanced_font_sizes: "10px,12px,13px,14px,16px,18px,20px",
    font_size_style_values : "10px,12px,13px,14px,16px,18px,20px",
    theme_advanced_toolbar_location : "top",
    theme_advanced_toolbar_align : "left",
    theme_advanced_buttons1 : "undo,redo,|,bold,italic,underline,strikethrough,|,bullist,numlist,outdent,indent,sup,sub,|,link,unlink,|,cleanup,removeformat,help,|,code,spellchecker,pastetext,pasteword,|,search,replace,preview",
    theme_advanced_buttons2 : "styleselect,fontsizeselect,justifyleft,justifycenter,justifyright,justifyfull,|,tablecontrols,|,forecolor,backcolor,charmap,|,image,selectall,",
    theme_advanced_resizing : true,
    entity_encoding:"named",
    entities: "",
    paste_auto_cleanup_on_paste : true, // for word paste cleanup
    paste_convert_middot_lists: true,
    paste_retain_style_properties: "all",
	paste_remove_styles : false,
	paste_remove_styles_if_webkit : false,
    paste_strip_class_attributes: "all",
    plugins : "autolink,autoresize,paste,table,spellchecker,searchreplace,preview",
    spellchecker_languages : "+English=en-us",
    spellchecker_rpc_url : "${pageContext.request.contextPath}/spellchecker",
    browser_spellcheck: true,
    valid_elements: "a[href],p[style],ul,ol,li,sup,sub,table[class|border|cellspacing|cellpadding|frame],tr,td[colspan|rowspan],span[style],th,caption,strong,b,s,i,u,strike,pre,img[src|alt|height|width|style],h1,h2,h3,h4,h5,h6",
    width: '600',
    relative_urls : false,
    convert_urls : false,
    formats: {
    	italic: {inline: 'i'},
    	bold: {inline: 'strong'},
    	underline: {inline: 'u'},
    	strikethrough: {inline: 's'}
    },
    style_formats : [
                     {title : 'Header 1', block : 'h1'},
                     {title : 'Header 2', block : 'h2'},
                     {title : 'Header 3', block : 'h3'},
                     {title : 'Header 4', block : 'h4'},
                     {title : 'Header 5', block : 'h5'},
                     {title : 'Header 6', block : 'h6'},
                     {title : 'Paragraph', block : 'p'},
             ] ,
});

function inlineImg() {
	$('.docinput').each(function(index, el) {
		var eid = $(this).attr('id').split('-')[0];
		var content = $(this).html();
		var imglink = "img[src='/" + env + "/images/docgen/" + eid + "_latest.svg']";
		var imglinks = $(this).find(imglink);
		if (imglinks.length != 0) {
			$(imglink).not(imglinks).remove();
		}
		if (content.indexOf("[image]") != -1) {
			$(imglink).remove();
			$(this).html(content.replace('[image]', '<img src="/' + env + '/images/docgen/' + eid + '_latest.svg"/>'));
		}
	});
}

$(document).ready(function(){
	$(window).bind('beforeunload', function() {
		if (curedit) {
			return "You're currently editing the view!";
		} else if (addingComment) {
			return "You're currently editing a comment!";
		}
	});
	$("img[src*='/editor/images/docgen/']").each(function() {
		var src = $(this).attr('src').replace('editor', env);
		$(this).attr('src', src);
	});
	inlineImg();
	
	$('#toggleEdit').click(function() {
		if (addingComment) {
			alert("You're currently adding a comment! Save the comment first!");
			return;
		}
		$(".docinput").each(function(index, el) {
			tinyMCE.execCommand("mceToggleEditor", true, el.id);
		});
		$('.textinput').toggleClass('hidden');
		$('.display').toggleClass('hidden');
		
		if (curedit) {
			$('.textinput').each(function(index, el) {
				$('.' + el.id + "_display").html($(el).val());
			});
			$(this).html("Edit");
			inlineImg();
		} else {
		    $(this).html("Preview");
		}
		curedit = curedit ? false : true;
	});
	$('.mer-instance-table').after('<br/><button id="instance">Instance Process</button>');
	$('#instance').click(function() {
		if (curedit)
			$('#toggleEdit').click();
		var post = [];
		var mapping = {};
		$('.mer-instance-table .textinput').each(function(index, el) {
			var jel = $(el);
			var id = jel.attr('id').split('-')[0]
			var type = jel.attr('id').split('-')[1]
			if (!(id in mapping)) {
				mapping[id] = {};
				post.push(mapping[id]);
			}
			var content = mapping[id];
			content['mdid'] = id;
			if (type == 'name')
				content['name'] = jel.val();
			if (type == 'doc')
				content['documentation'] = jel.html();
			if (type == 'dvalue')
				content['dvalue'] = jel.val();
		});
		var postString = JSON.stringify(post);
		$.ajax({
			url: window.location.pathname + '?type=instance', 
			type: 'POST',
			data: postString, 
			contentType: "application/json",
			success: function(data) { alert(data)},
			error: function(jqXHR, textStatus, errorThrown) {alert("not saved: " + errorThrown);}
		});
	});
	$('#save').click(function() {
		if (curedit)
			$('#toggleEdit').click();
		var post = [];
		var mapping = {};
		$('.textinput,.docinput').each(function(index, el) {
			var jel = $(el);
			var id = jel.attr('id').split('-')[0]
			var type = jel.attr('id').split('-')[1]
			if (!(id in mapping)) {
				mapping[id] = {};
				post.push(mapping[id]);
			}
			var content = mapping[id];
			content['mdid'] = id;
			if (type == 'name')
				content['name'] = jel.val();
			if (type == 'doc') {
				content['documentation'] = jel.html();
			}
			if (type == 'dvalue')
				content['dvalue'] = jel.val();
		});
		var postString = JSON.stringify(post);
/* 		var str = '';
		for (var i=0; i < postString.length; i++) {
			str += "  " + postString.charCodeAt(i)+ ":" + postString.charAt(i);
		}
		alert(str);
 */		$.ajax({
			url: window.location.pathname, 
			type: 'POST',
			data: postString, 
			contentType: "application/json;charset=utf-8;",
			success: function(data) { window.location.reload(true);},
			error: function(jqXHR, textStatus, errorThrown) {alert("not saved: " + errorThrown);}
		});
	});
	$('#cancel').click(function() {curedit = false; addingComment = false; location.reload();});
	$('#docnavwrapper').jstree({
		"plugins": ["themes", "html_data", "search", "ui"],
		"core": {
			"animation": 0,
			"initially_open": ["${viewId}-nav"<c:forEach var="viewParentId" items="${viewParentIds}">,"${viewParentId}-nav"</c:forEach>]
		},
		"ui": {
			"select_limit": 1,
			"initially_select": ["${viewId}-nav"]
		},
		"themes": {
			"theme": "apple",
			"dots": false,
			"icons": true
		}
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

	$('#addComment').click(function() {
		if (curedit) {
			alert("You're currently editing the view! Save your view first!");
			return;
		}
		$('#addCommentForm').toggleClass("hidden");
		tinyMCE.execCommand("mceToggleEditor", true, "addCommentTextArea");
		$('#addComment').toggleClass("hidden");
		addingComment = true;
	});
	$('#addCommentForm').submit(function() {addingComment = false; return true;});
	$('.comment-remove').click(function() {
		var id = $(this).attr('id').split('-')[0];
		$.ajax({
			url: "${pageContext.request.contextPath}/rest/comments/" + id,
			type: "DELETE",
			dataType: "text",
			success: function(data) { $('#'+data +"-comment").remove();}
		});
	});
	$('.comment-edit-submit').click(function() {
		var id = $(this).attr('id').split('-')[0];
		tinyMCE.execCommand("mceToggleEditor", true, id + "-body");
		$('#'+ id + '-edit-submit').toggleClass('hidden');
		$.ajax({
			url: "${pageContext.request.contextPath}/rest/comments/" + id,
			type: "POST",
			data: $('#' + id + '-body').html(),
			contentType: 'text/plain',
			processData: false,
			success: function(data) {}
		});
	});
		
	$('.comment-edit').click(function() {
		var id = $(this).attr('id').split('-')[0];
		tinyMCE.execCommand("mceToggleEditor", true, id + "-body");
		$('#'+ id + '-edit-submit').toggleClass('hidden');
	});
	
});
</script>
<title>${viewName}</title>
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
Search View Name: <input id="docnavsearch" type="text" size="15"/>
</div>
<br/>
<div id="docnavwrapper">
	<ul id="docnav">
        <template:tree node="${viewTree[documentId]}"/>
	</ul>
</div>
</div>
<div id="view">
<h1>
<span class="display ${viewId}-name_display"}>${viewName}</span>
<input class="hidden textinput" id="${viewId}-name" type="text" value="${viewName}" size="80"/>
</h1>
<br/>
<button id="toggleEdit">Edit</button><button id="save">Save</button><button id="cancel">Cancel</button><span id="lastModified">  Last saved/exported by ${lastUser} at ${lastModified}</span><br/>
<br/>
<c:if test="${fn:length(viewDetail) == 0}">
		<div class="editable editable-doc">
		<div class="docinput" id="${viewId}-doc"> 
		</div>
		</div>
</c:if>
<c:forEach var="element" items="${viewDetail}">
	<c:choose>
	<c:when test="${element['type'] == 'doc'}">
		<div class="editable editable-doc">
		<div class="docinput" id="${element['mdid']}-doc">
		${element["documentation"]}
		</div>
		</div>
	</c:when>
	<c:when test="${element['type'] == 'name'}">
		<div class="editable editable-name">
		<span class="display ${element['mdid']}-name_display">${element["name"]}</span>
		<c:if test="${element['edit'] == 'true'}">
		<input class="hidden textinput" id="${element['mdid']}-name" type="text" value="${element['name']}"/>
		</c:if>
		</div>
	</c:when>
	<c:when test="${element['type'] == 'text'}">
		<div>
		${element["text"]}
		</div>
	</c:when>
	<c:when test="${element['type'] == 'table'}">
		<table class="${element['style']}">
			<caption>${element['title']}</caption>
			<template:table celltag="th" contents="${element['header']}"/>
			<template:table celltag="td" contents="${element['body']}"/>
		</table>
	</c:when>
	</c:choose>
	<br/>
</c:forEach>

<div id="comments">
<h3>Comments</h3><br/>
<button id="addComment">Add Comment</button>
<form class="hidden" action="${pageContext.request.contextPath}/ui/views/${viewId}/comment" method="post" id="addCommentForm">
<textarea id="addCommentTextArea" name="body" cols="40" rows="5"></textarea><br/>
<input type="submit" value="Submit Comment"/>
</form>
<c:forEach var="comment" items="${comments}">
	<div id="${comment['id']}-comment" class="comment-detail">
	<span id="${comment['id']}-author" class="comment-author">by ${comment['author']} </span>
	<span id="${comment['id']}-timestamp" class="comment-timestamp">at ${comment['timestamp']}</span>
	<c:if test="${currentUser == comment['author']}">
	<span id="${comment['id']}-remove" class="comment-remove"><a href="#">X</a></span>
	<span id="${comment['id']}-edit" class="comment-edit"><a href="#">Edit</a>&nbsp;</span>
	</c:if>
	<div id="${comment['id']}-body" class="comment-body">
			${comment['body']}
	</div>
	<button id="${comment['id']}-edit-submit" class="hidden comment-edit-submit">Submit</button>
	</div>
</c:forEach>

</div>
</div>
<div class="clear"></div>

</body>
</html>
