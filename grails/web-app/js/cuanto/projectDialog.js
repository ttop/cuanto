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

	$$('.pdInput').each(function(inp) {
		inp.setStyle({width: "180px"});
	});

	if (title) {
		$('pdHd').innerHTML = title;
	}

	var handleCancel = function() {
		this.cancel();
		this.hide();
		pub.clear();
	}

	var handleSubmit = function() {
		if (validate()) {
			this.submit();
		} else {
			$('pdErrorDiv').show();
		}
	}

	var myButtons = [ { text:"Save", handler:handleSubmit, isDefault:true },
	                  { text:"Cancel", handler:handleCancel } ];
	projDialog.cfg.queueProperty("buttons", myButtons);

	projDialog.callback = {
		success: function () {
			this.hide();
			pub.clear();
			YAHOO.cuanto.events.projectChangeEvent.fire();
		},
		failure: function() {
			this.hide();
			pub.clear();
			YAHOO.cuanto.events.projectChangeEvent.fire();
		},
		scope: projDialog
	}

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

		$('pdAutoComplete').setStyle({width: $('pdGroup').getStyle('width')});
		var autoComplete = new YAHOO.widget.AutoComplete("pdGroup", "pdAutoComplete", dataSource);
		autoComplete.minQueryLength = 2;
		autoComplete.queryDelay = .4;
		autoComplete.generateRequest = function (qry) {
			return "?rand=" + new Date().getTime() + "&query=" + qry;
		}
		autoComplete.doBeforeExpandContainer = function () {
			var Dom = YAHOO.util.Dom;
			Dom.setXY("pdAutoComplete", [Dom.getX("pdGroup"), Dom.getY("pdGroup") + Dom.get("pdGroup").offsetHeight]);
			return true;
		}
		autoComplete.useIFrame = true;
	}

	function validate() {
		var isValid = true;
		var error = "";
		var form = $('pdForm');

		var name = form["name"];

		if ($F(name).blank()) {
			isValid = false;
			error += "Project Name cannot be blank<br/>";
		}

		var pk = $F(form["projectKey"]);
		if (pk.blank()) {
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

		$('pdError').innerHTML = error;
		return isValid;
	}

	pub.setTitle = function(title) {
		$('pdHd').innerHTML = title;
	}

	pub.show = function(title) {
		if (title) {
			setTitle(title);
		}
		projDialog.show();
		initAutoComplete();
	}

	pub.loadProject = function(projectId) {
	    $('pdLoading').show();
		new Ajax.Request(YAHOO.cuanto.urls.get('projectInfo'), {
			parameters: {'id': projectId, format: 'json', rand: new Date().getTime()},
			method: "get",
			onSuccess: function(transport) {
				var project = transport.responseJSON;
				$('pdProjectId').setValue(project['id']);
				$('pdName').setValue(project['name']);
				if (project['projectGroup'] && project['projectGroup']['name']) {
					$('pdGroup').setValue(project['projectGroup']['name']);
				}
				$('pdProjectKey').setValue(project['projectKey']);
				if (project['bugUrlPattern']) {
					$('pdUrlPattern').setValue(project['bugUrlPattern']);
				}
				$('pdType').setValue(project['testType']['name']);
				$('pdLoading').hide();
			}
		})
	}

	pub.clear = function() {
		$('pdForm').reset();
		$('pdErrorDiv').hide();
		$('pdError').innerHTML = "";
	}
	return pub;
};
