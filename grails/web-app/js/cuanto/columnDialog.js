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


YAHOO.namespace('cuanto');

YAHOO.cuanto.ColumnDialog = function (datatable, overlayManager, columnKeys) {
	var panel;
	var pub = {}; // public methods


	function getColumnPanel() {
		if (!panel) {
			panel = new YAHOO.widget.Panel("columnPanel", {dragOnly: true, fixedCenter: true,
				width: "270px", visible: true, iframe: false, underlay: "none"});
			panel.render();
			if (overlayManager)
			{
				overlayManager.register(panel);
			}
		}
		return panel;
	}


	// Shows dialog, creating one when necessary
	var newCols = true;

	var handleButtonClick = function(e, oSelf) {
		var sKey = this.get("name");
		if (this.get("value") === "Hide") {
			// Hides a Column
			datatable.hideColumn(sKey);
		}
		else {
			// Shows a Column
			datatable.showColumn(sKey);
		}
	};


	pub.show = function(e) {
		getColumnPanel().show();

		if (e) {
			YAHOO.util.Event.stopEvent(e);
		}

		if (newCols) {
			// Populate Dialog
			// Using a template to create elements for the SimpleDialog

			var allColumns = [];
			columnKeys.each(function(colKey) {
				var col = datatable.getColumn(colKey);
				allColumns.push(col);
			});

			var elPicker = YAHOO.util.Dom.get("columnPanel-picker");
			var elTemplateCol = document.createElement("div");
			YAHOO.util.Dom.addClass(elTemplateCol, "columnPanel-pickercol");
			var elTemplateKey = elTemplateCol.appendChild(document.createElement("span"));
			YAHOO.util.Dom.addClass(elTemplateKey, "columnPanel-pickerkey");
			var elTemplateBtns = elTemplateCol.appendChild(document.createElement("span"));
			YAHOO.util.Dom.addClass(elTemplateBtns, "columnPanel-pickerbtns");
			var onclickObj = {fn:handleButtonClick, obj:this, scope:false };

			// Create one section in the SimpleDialog for each Column
			var elColumn, elKey, elButton, oButtonGrp;
			for (var i = 0,l = allColumns.length; i < l; i++) {
				var oColumn = allColumns[i];

				// Use the template
				elColumn = elTemplateCol.cloneNode(true);

				// Write the Column key
				elKey = elColumn.firstChild;
				elKey.innerHTML = oColumn.label;

				// Create a ButtonGroup
				oButtonGrp = new YAHOO.widget.ButtonGroup({
					id: "buttongrp" + i,
					name: oColumn.getKey(),
					container: elKey.nextSibling
				});
				oButtonGrp.addButtons([
					{
						label: "Show",
						value: "Show",
						checked: ((!oColumn.hidden)),
						onclick: onclickObj
					},
					{
						label: "Hide",
						value: "Hide",
						checked: ((oColumn.hidden)),
						onclick: onclickObj
					}
				]);

				elPicker.appendChild(elColumn);
			}
			newCols = false;
		}

	};
	return pub;
};