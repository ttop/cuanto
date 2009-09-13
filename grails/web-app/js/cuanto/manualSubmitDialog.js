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

YAHOO.cuanto.ManualSubmitDialog = function(onSubmitSuccessful, scope) {
	var pub = {}; // public methods

	var msDialog = new YAHOO.widget.Dialog('manualSubmitDialog', {
		width: "400px",
		visible: false,
		modal: true,
		x: 100,
		y: 100
	});

	var handleCancel = function() {
		this.cancel();
	};

	var myButtons = [ { text:"Cancel", handler:handleCancel, isDefault:true } ];
	msDialog.cfg.queueProperty("buttons", myButtons);

	msDialog.callback.scope = msDialog;

	var escapeKey = new YAHOO.util.KeyListener(document, { keys:27 },
	{
		fn:handleCancel,
		scope:msDialog,
		correctScope:true
	});

	msDialog.cfg.queueProperty("keylisteners", [escapeKey]);
	msDialog.render();

	pub.show = function() {
		//$('dpdProjectId').value = projectId;
		//dpDialog.getButtons().each(function(b) {
		//	b.set('disabled', false);
		//});
		msDialog.show();
	};

	return pub;
};