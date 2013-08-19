/**
 * editor_plugin_src.js
 *
 * Copyright 2009, Moxiecode Systems AB
 * Released under LGPL License.
 *
 * License: http://tinymce.moxiecode.com/license
 * Contributing: http://tinymce.moxiecode.com/contributing
 */

(function() {
	tinymce.create('tinymce.plugins.TemplateEuropaPlugin', {
		init : function(ed, url) {
			var t = this;

			ed.addCommand('insertTemplate', function() {
				var str = "<h1>Concept Description:</h1>" +
						"<h1>Theory and Principles:</h1>" +
						"<h1>Related Scenarios:</h1>" +
						"<h1>Element:</h1>" +
						"<h1>Functions:</h1>" +
						"<h1>Functional Allocation:</h1>" +
						"<h1>Relationship Definition:</h1>" +
						"<h1>Element Property Definition:</h1>" +
						"<h1>Extracted Requirements:</h1>"; 

				ed.execCommand('mceInsertContent', false, str);
			});


			ed.addButton('template_europa', {
				title : 'Insert Template', 
				cmd : 'insertTemplate', 
				image: url + '/img/template_europa.gif'});
		},
		
		getInfo : function () {
			return {
				longname : 'Europa Template Plugin',
				author : 'JPL',
				authorurl : '',
				infourl : '',
				version : tinymce.majorVersion + "." + tinymce.minorVersion
			};
		}

	});

	// Register plugin
	tinymce.PluginManager.add('template_europa', tinymce.plugins.TemplateEuropaPlugin);
})();