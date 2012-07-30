/*
 Copyright (c) 2008 thePlatform, Inc.

This file is part of Cuanto, a test results repository and analysis program.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/


YAHOO.namespace("cuanto");

YAHOO.cuanto.ProjectDialog = function(title) {
	var pub = {}; // public methods

	var projDialog = new YAHOO.widget.Dialog('projectDialog', {
		width: "430px",
		visible: false,
		modal: true,
		underlay: "none",
		hideaftersubmit: false,
		x: 100,
		y: 100
	});

	$('.pdInput').css("width", "180px");
	$('#pdPurge').css("width", "38px");

	if (title) {
		$('#pdHd').html(title);
	}

	var handleCancel = function() {
		this.cancel();
		this.hide();
		pub.clear();
	};

	var handleSubmit = function() {
		if (validate()) {
			this.submit();
		} else {
			$('#pdErrorDiv').show();
		}
	};

	var myButtons = [ { text:"Save", handler:handleSubmit, isDefault:true },
	                  { text:"Cancel", handler:handleCancel } ];
	projDialog.cfg.queueProperty("buttons", myButtons);

	projDialog.callback = {
		success: function (o) {
			this.hide();
			pub.clear();
			var project = YAHOO.lang.JSON.parse(o.responseText);
			YAHOO.cuanto.events.projectChangeEvent.fire({action: "edit", "project": project});
		},
		failure: function() {
			this.hide();
			pub.clear();
			YAHOO.cuanto.events.projectChangeEvent.fire();
		},
		scope: projDialog
	};

	var enterKey = new YAHOO.util.KeyListener(document, { keys:13 },
												  { fn:handleSubmit,
													scope:projDialog,
													correctScope:true } );
	var escapeKey = new YAHOO.util.KeyListener(document, { keys:27 },
												  { fn:handleCancel,
													scope:projDialog,
													correctScope:true } );

	projDialog.cfg.queueProperty("keylisteners", [enterKey, escapeKey]);
	projDialog.render();

	function initAutoComplete() {
		var dataSource = new YAHOO.util.XHRDataSource(YAHOO.cuanto.urls.get('groupNames'));
		dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
		dataSource.responseSchema = {
			resultsList: "groups",
			fields: ["name"]
		};

		$('#pdAutoComplete').css({width: $('#pdGroup').css('width')});

		var autoComplete = new YAHOO.widget.AutoComplete("pdGroup", "pdAutoComplete", dataSource);
		autoComplete.minQueryLength = 2;
		autoComplete.queryDelay = .4;
		autoComplete.generateRequest = function (qry) {
			return "?rand=" + new Date().getTime() + "&query=" + qry;
		};
		autoComplete.doBeforeExpandContainer = function () {
			var Dom = YAHOO.util.Dom;
			Dom.setXY("pdAutoComplete", [Dom.getX("pdGroup"), Dom.getY("pdGroup") + Dom.get("pdGroup").offsetHeight]);
			return true;
		};
		autoComplete.useIFrame = true;
	}

	function isBlank(val) {
		return $.trim(val).length == 0;
	}

	function validate() {
		var isValid = true;
		var error = "";
		var form = $('#pdForm');
		var name = $("#pdName");

		if (isBlank(name.val())) {
			isValid = false;
			error += "Project Name cannot be blank<br/>";
		}

		var pk = $("#pdProjectKey").val();
		if (isBlank(pk)) {
			isValid = false;
			error += "Project Key cannot be blank<br/>";
		}

		if (pk.length > 25) {
			isValid = false;
			error += "Project Key cannot exceed 25 characters<br/>";
		}

		if (pk.match(/\s/)) {
			isValid = false;
			error += "Project Key cannot contain whitespace";
		}

		$('#pdError').innerHTML = error;
		return isValid;
	}

	pub.setTitle = function(title) {
		$('#pdHd').html(title);
	};

	pub.show = function(title, projectId) {
		if (title) {
			setTitle(title);
		}

		if (projectId) {
			$('#pdProjectId').val(projectId);
		} else {
			$('#pdProjectId').val("");
		}
		projDialog.show();
		$("#pdType").replaceWith($('#pdType').clone()); // hack for Chrome display issue
		initAutoComplete();
	};

	pub.loadProject = function(projectId) {
	    $('#pdLoading').show();

		var myurl = YAHOO.cuanto.urls.get('projectInfo');
		$.ajax({
			url: myurl,
			data: {'id': projectId, format: 'json', rand: new Date().getTime()},
			dataType: "json",
			success: function(project, textStatus, httpReq) {
				$('#pdProjectId').val(project['id']);
				$('#pdName').val(project['name']);
				if (project['projectGroup'] && project['projectGroup']['name']) {
					$('#pdGroup').val(project['projectGroup']['name']);
				}
				$('#pdProjectKey').val(project['projectKey']);
				if (project['bugUrlPattern']) {
					$('#pdUrlPattern').val(project['bugUrlPattern']);
				}
				$('#pdType').val(project['testType']['name']);
				$('#pdPurge').val(project['purgeDays']);
				$('#pdLoading').hide();
			}
		});
	};

	pub.clear = function() {
		$('.pdInput').val("");
		$('#pdErrorDiv').hide();
		$('#pdError').html("");
	};
	return pub;
};
