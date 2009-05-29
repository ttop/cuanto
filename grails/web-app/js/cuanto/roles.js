/*
 Copyright (c) 2009 Todd Wells

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

YAHOO.cuanto.roles = function() {

	var pub = {};

	pub.initRoleList = function() {
		var columnDefs = [
			{ key: "id", label: "ID", sortable: true},
			{ key: "rolename", label: "Role Name", sortable: true },
			{ key: "description", label: "Description", sortable: true }
		];

		var myDataSource = new YAHOO.util.DataSource($('rolelist'));
		myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
		myDataSource.responseSchema = {
			fields: [{key:"id"},
				{key:"rolename"},
				{key:"description"}
			]
		};
		new YAHOO.widget.DataTable("rolelistdiv", columnDefs, myDataSource, {sortedBy: {key: "rolename", dir: "asc"}});
	};

	pub.initDelete = function() {
		function confirmDelete(e) {
			var mySimpleDialog = new YAHOO.widget.SimpleDialog("dlg", {
				width: "20em",
				fixedcenter:true,
				modal:true,
				visible:false,
				draggable:false });
			mySimpleDialog.setHeader("Warning!");
			mySimpleDialog.setBody("Are you sure you want to delete this role?");
			mySimpleDialog.cfg.setProperty("icon", YAHOO.widget.SimpleDialog.ICON_WARN);

			var handleYes = function() {
				$('deleteForm').submit();
				this.hide();
			}
			var handleNo = function() {
				this.hide();
			}
			var myButtons = [ { text:"Yes",
				handler:handleYes },
				{ text:"Cancel",
					handler:handleNo,
					isDefault:true } ];
			mySimpleDialog.cfg.queueProperty("buttons", myButtons);

			mySimpleDialog.render(document.body);
			mySimpleDialog.show();
			YAHOO.util.Event.preventDefault(e);
		}

		YAHOO.util.Event.addListener('deleteRole', "click", confirmDelete);
	};

	return pub;

};