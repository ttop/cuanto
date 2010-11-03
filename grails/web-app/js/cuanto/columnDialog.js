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

YAHOO.cuanto.ColumnDialog = function (datatable, overlayManager, subCookieName, unpickableColKeys) {
	var panel;
	var pub = {}; // public methods
	var analysisCookieName = "cuantoAnalysis";
	var newCols = true;


	function getColumnPanel() {

		if (!panel) {
			panel = new YAHOO.widget.Panel("columnPanel", {dragOnly: true, x: 150,
				width: "170px", visible: true, iframe: false, underlay: "none"});
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

		var colHtml = "";

		for (var i = 0, l = allColumns.length; i < l; i++) {
			var oColumn = allColumns[i];

			if (!isUnpickable(oColumn)) {
				colHtml += "<input type='checkbox' class='colChk' id='col-" + oColumn.getKey() + "' ";
				if (!oColumn.hidden) {
					colHtml += "checked ";
				}
				colHtml += "value='" + oColumn.getKey() + "'/> " + "<label class='colLabel' for='col-" + oColumn.getKey() + "'>" + oColumn.label + "</label><br/>";
			}
		}
		elPicker.html(colHtml);
		$('.colChk').click(handleClick);
	};


	function isUnpickable(column) {
		var unpick = false;
		var targetKey = column.getKey();
		if (targetKey.match(/^yui/)) {
			return true;
		}

		if (unpickableColKeys) {
			$.each(unpickableColKeys, function(idx, colkey) {
				if (colkey == targetKey) {
					unpick = true;
					return false;
				}
			});
		}
		return unpick;
	}

	function handleClick(e) {
		var colKey = $(e.target).val();
		var column = datatable.getColumn(colKey);
		if (column.hidden) {
			datatable.showColumn(column);
		} else {
			datatable.hideColumn(column);
		}
		setAnalysisColumnPref();
	}

	
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