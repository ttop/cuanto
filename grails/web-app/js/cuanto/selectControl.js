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

YAHOO.cuanto.SelectControl = function() {

	var pub = {}; // public methods
	var selected = [];
	var dataTable;
	var columnIndex;


	function initTable(yuiDataTable, checkboxColumnIndex) {
		dataTable = yuiDataTable;
		columnIndex = checkboxColumnIndex;
		dataTable.subscribe("checkboxClickEvent", onSelectChange);
		$("#hideSelect").click(hideColumn);
		$("#selectPage").click(selectPageRecords);
		$("#deselectPage").click(deselectPageRecords);
		$("#deselectAll").click(deselectAllRecords);
	}

	function showColumn(e) {
		var selCol = dataTable.getColumn(columnIndex);
		if (selCol.hidden) {
			dataTable.showColumn(selCol);
			$('#showSelectOptions').show();
		}
		if (e) {
			YAHOO.util.Event.preventDefault(e);
		}
	}


	function hideColumn(e) {
		var selCol = dataTable.getColumn(columnIndex);
		if (!selCol.hidden) {
			dataTable.hideColumn(selCol);
			$('#showSelectOptions').hide();
		}
		if (e) {
			YAHOO.util.Event.preventDefault(e);
		}
	}


	function selectPageRecords(e) {
		doWithVisibleRecords(function(rec) {
			if (rec) {
				var recId = rec.getData("id");
				rec.setData("checked", true);
				if (selected.indexOf(recId) == -1) {
					selected.push(recId);
				}
			}
		});
		dataTable.render();

		updateSelectedTotal();
		if (e) {
			e.preventDefault();
		}
	}


	function doWithVisibleRecords(callback) {
		var start = dataTable.get("paginator").getStartIndex();
		var rowsPerPage = dataTable.get("paginator").getRowsPerPage();

		for (var i=start; i < start + rowsPerPage; i++) {
			var rec = dataTable.getRecordSet().getRecord(i);
			if (rec) {
				callback(rec);
			}
		}
	}


	function deselectPageRecords(e) {
		doWithVisibleRecords(function(rec){
			rec.setData("checked", false);

			var idToRemove = rec.getData("id");
			selected = $.grep(selected, function(i) {
				return i != idToRemove;
			});
		});
		dataTable.render();
		updateSelectedTotal();

		if (e) {
			YAHOO.util.Event.preventDefault(e);
		}
	}


	function deselectAllRecords(e) {
		selected.length = 0;
		$.each(dataTable.getRecordSet().getRecords(), function(idx, rec) {
			if (rec) {
				rec.setData("checked", false);
			}
		});
		dataTable.render();
		updateSelectedTotal();
		if (e) {
			YAHOO.util.Event.preventDefault(e);
		}
	}


	function updateSelectedTotal() {
		$('#currentSelected').html(selected.length + " Selected");
	}


	function onSelectChange(oArgs) {
		var record = this.getRecord(oArgs.target);
		var id = record.getData("id");
		var indxInSelected = selected.indexOf(id);
		if (record.getData("checked")) {
			record.setData("checked", false);
			selected.splice(indxInSelected, 1);
		} else {
			record.setData("checked", true);
			if (indxInSelected == -1){
				selected.push(id);
			}
		}
		updateSelectedTotal();
	}

	function formatSelect(elCell, oRecord, oColumn, oData) {
		var indxInSelected = selected.indexOf(oRecord.getData("id"));
		if (indxInSelected == -1) {
			oRecord.setData("checked", false);
			oData = false;
		} else {
			oRecord.setData("checked", true);
			oData = true;
		}
		YAHOO.widget.DataTable.formatCheckbox(elCell, oRecord, oColumn, oData);
	}

	pub.formatSelect = formatSelect;
	pub.onSelectChange = onSelectChange;
	pub.showColumn = showColumn;
	pub.hideColumn = hideColumn;
	pub.deselectAllRecords = deselectAllRecords;
	pub.initTable = initTable;

	pub.getSelected = function() {
		return selected;
	};
	return pub;
};