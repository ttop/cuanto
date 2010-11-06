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


YAHOO.namespace('cuanto');

YAHOO.cuanto.DeleteProjectDialog = function(onDeleteSuccessful, scope) {
	var pub = {}; // public methods

	Function.prototype.bindFunc = function(){
		// http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Functions:arguments
		var _$A = function(a){return Array.prototype.slice.call(a);};

		if(arguments.length < 2 && (typeof arguments[0] == "undefined")) return this;

		var __method = this, args = _$A(arguments), object = args.shift();

		return function() {
		  return __method.apply(object, args.concat(_$A(arguments)));
		}
	};

	var dpDialog = new YAHOO.widget.Dialog('deleteProjectDialog', {
		width: "400px",
		visible: false,
		modal: true,
		x: 100,
		y: 100
	});

	var handleCancel = function() {
		this.cancel();
	};
	var handleDelete = function() {
		if ($('#dpdConfirmDelete').val() == "YES") {
			$.each(dpDialog.getButtons(), function(idx, b) {
				b.set('disabled', true);
			});
			$('#dpdMessage').html("");
			this.submit();
		} else {
			$('#dpdMessage').html("Deletion is <b>not</b> confirmed.");
		}
	};

	var myButtons = [ { text:"Cancel", handler:handleCancel, isDefault:true },
		{ text:"Delete", handler:handleDelete } ];
	dpDialog.cfg.queueProperty("buttons", myButtons);

	dpDialog.callback.success = onDeleteSuccessfulLocal;
	dpDialog.callback.scope = dpDialog;

	var escapeKey = new YAHOO.util.KeyListener(document, { keys:27 },
	{
		fn:handleCancel,
		scope:dpDialog,
		correctScope:true
	});

	dpDialog.cfg.queueProperty("keylisteners", [escapeKey]);
	dpDialog.render();

	function onDeleteSuccessfulLocal() {
		var bd = "Deleted " + $('#dpdProjectName').html() + "!";
		var deleteDoneDialog = new YAHOO.widget.SimpleDialog("dlg", {
			width: "20em",
			context: ['deleteProjectDialog', 'br', 'br'],
			modal:true,
			visible:false,
			draggable:false });
		deleteDoneDialog.setHeader("Deleted!");
		deleteDoneDialog.setBody(bd);
		deleteDoneDialog.cfg.setProperty("icon", YAHOO.widget.SimpleDialog.ICON_INFO);
		var myButtons = [ { text:"OK", handler: function() {
			this.hide();
			if (onDeleteSuccessful) {
				var toCall;
				if (scope) {
					toCall = onDeleteSuccessful.bindFunc(scope);
				} else {
					toCall = onDeleteSuccessful;
				}
				toCall();
			}
			YAHOO.cuanto.events.projectChangeEvent.fire({action: "delete"});
		}}];
		deleteDoneDialog.cfg.queueProperty("buttons", myButtons);
		deleteDoneDialog.render(document.body);
		deleteDoneDialog.show();
	}


	pub.show = function(projectId, projectName) {
		$('#dpdConfirmDelete').val("");
		$('#dpdProjectId').val(projectId);
		if (projectName) {
			$('#dpdProjectName').html(projectName);
		}

		$.each(dpDialog.getButtons(), function(idx, b) {
			b.set('disabled', false);
		});

		dpDialog.show();
	};

	return pub;
};