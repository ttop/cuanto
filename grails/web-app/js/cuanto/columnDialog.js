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

YAHOO.cuanto.ColumnDialog = function (datatable, overlayManager, subCookieName) {
	var panel;
	var pub = {}; // public methods
	var analysisCookieName = "cuantoAnalysis";
	var newCols = true;


	function getColumnPanel() {

		if (!panel) {
			panel = new YAHOO.widget.Panel("columnPanel", {dragOnly: true, x: 150,
				width: "330px", visible: true, iframe: false, underlay: "none"});
			panel.render();
			if (overlayManager)
			{
				overlayManager.register(panel);
			}
		}
		return panel;
	}


	var handleButtonClick = function(e, oSelf) {
		var sKey = this.get("name");
		if (this.get("value") === "Hide") {
			datatable.hideColumn(sKey);
		}
		else {
			datatable.showColumn(sKey);
		}
		setAnalysisColumnPref();
	};


	function setAnalysisColumnPref() {
		var expDate = new Date();
		expDate.setDate(expDate.getDate() + 30);

		var colHidden = [];
		$.each(datatable.getColumnSet().flat, function(idx, column) {
			var isYuiCol = column.key.match(/^yui/);
			if (!isYuiCol) {
				colHidden.push(column.key + ":" + column.hidden);
			}
		});
		var colStr = colHidden.join(",");
		YAHOO.util.Cookie.setSub(analysisCookieName, subCookieName, colStr, {path: "/", expires: expDate});
	}


	pub.show = function(e) {
		getColumnPanel().show();

		if (e) {
			YAHOO.util.Event.stopEvent(e);
		}

		var elPicker = $("#columnPanel-picker");
		$(elPicker.children()).remove();

		var columnKeys = $.map(datatable.getColumnSet().keys, function(item, idx) {
			return item.key;
		});

		var allColumns = $.map(columnKeys, function(colKey, idx) {
			return datatable.getColumn(colKey);
		});

		var elTemplateCol = document.createElement("div");
		YAHOO.util.Dom.addClass(elTemplateCol, "columnPanel-pickercol");
		var elTemplateKey = elTemplateCol.appendChild(document.createElement("span"));
		YAHOO.util.Dom.addClass(elTemplateKey, "columnPanel-pickerkey");
		var elTemplateBtns = elTemplateCol.appendChild(document.createElement("span"));
		YAHOO.util.Dom.addClass(elTemplateBtns, "columnPanel-pickerbtns");
		var onclickObj = {fn:handleButtonClick, obj:this, scope:false };

		var elColumn, elKey, elButton, oButtonGrp;
		for (var i = 0,l = allColumns.length; i < l; i++) {
			var oColumn = allColumns[i];
			elColumn = elTemplateCol.cloneNode(true);
			elKey = elColumn.firstChild;
			elKey.innerHTML = oColumn.label;

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

			elPicker[0].appendChild(elColumn);
		}
	};
	
	pub.getHiddenColumns = function() {
		var cookie = YAHOO.util.Cookie.getSub(analysisCookieName, subCookieName);
		if (cookie) {
			var cols = {};
			var pairs = cookie.split(",");
			$.each(pairs, function(idx, pair) {
				var items = pair.split(":");
				cols[items[0]] = (/^true$/i).test(items[1]);
			});
			return cols;
		}
		else {
			return null;
		}
	};

	return pub;
};