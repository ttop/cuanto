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

YAHOO.cuanto.users = function() {

	var pub = {};

	pub.initUserList = function() {
		var columnDefs = [
			{ key: "id", label: "ID", sortable: true},
			{ key: "username", label: "Username", sortable: true },
			{ key: "realname", label: "Real Name", sortable: true },
			{ key: "enabled", label: "Enabled", sortable: true },
			{ key: "roles", label: "Roles", sortable: true }
		];

		var myDataSource = new YAHOO.util.DataSource($('userlist'));
		myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
		myDataSource.responseSchema = {
			fields: [{key:"id"},
				{key:"username"},
				{key:"realname"},
				{key:"enabled"},
				{key:"roles"}
			]
		};
		new YAHOO.widget.DataTable("userlistdiv", columnDefs, myDataSource, {sortedBy: {key: "username", dir: "asc"}});
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
			mySimpleDialog.setBody("Are you sure you want to delete this user?");
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

		YAHOO.util.Event.addListener('deleteUser', "click", confirmDelete);
	};

	return pub;

};