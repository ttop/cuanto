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

YAHOO.cuanto.projectHistory = function() {
	var onSelectTestRunRow = function(e) {
		this.onEventSelectRow(e);
		var currentRow = this.getSelectedRows()[0];
		var currentRecord = this.getRecord(currentRow);
		window.location = YAHOO.cuanto.urls.get('testRunResults') + currentRecord.getData('id');
	};


	function getTestRunDataSource() {
		var testRunDataSource = new YAHOO.util.XHRDataSource(YAHOO.cuanto.urls.get("projectHistory"));
		testRunDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
		testRunDataSource.connXhrMode = "queueRequests";
		testRunDataSource.responseSchema = {
			resultsList: 'testRuns',
			fields: ["dateExecuted", "note", "valid", "tests", "passed", "failed",
				"totalDuration", "averageDuration",	"successRate", "tests", "id", "numAnalyzed", "testProperties"],
			metaFields: { totalCount:"totalCount", offset:"offset" }
		};
		return testRunDataSource;
	}


	function getTestRunTableConfig() {
		return {
			initialRequest:buildTestRunTableQueryString(),
			generateRequest: buildTestRunTableQueryString,
			paginator: getTestRunTablePaginator(),
			sortedBy: {key:"dateExecuted", dir:YAHOO.widget.DataTable.CLASS_DESC},
			dynamicData: true
		};
	}


	function getTestRunTableColumnDefs() {
		return [
			{key:"dateExecuted", label:"Test Run", sortable:true},
			{key:"tests", label:"Tests", sortable:true},
			{key:"passed", label:"Passed", sortable:true},
			{key:"failed", label: "Failed", sortable:true},
			{key:"successRate", label: "Success", sortable:true, formatter: pctFormatter},
			{key:"numAnalyzed", label: "Analyzed", sortable:false},
			{key:"totalDuration", label: "Duration", sortable:true},
			{key:"averageDuration", label: "Avg Duration", sortable:true},
			{key:"testProperties", label: "Properties", sortable:false, formatter: propertyFormatter}
		];
	}


	function getTestRunTablePaginator() {
		return new YAHOO.widget.Paginator({
			containers         : ['trTablePaging'],
			pageLinks          : 5,
			rowsPerPage        : 10,
			rowsPerPageOptions : [10,15,20,30,50],
			template       : "{FirstPageLink} {PageLinks} {LastPageLink}<br/>Show {RowsPerPageDropdown} per page",
			pageLabelBuilder : function (page, paginator) {
				var recs = paginator.getPageRecords(page);
				return (recs[0] + 1) + ' - ' + (recs[1] + 1);
			}
		});
	}


	function buildTestRunTableQueryString(state, self) {
		if (!state) {
			state = {
				startIndex: 0,
				sortedBy: {
					key: "dateExecuted",
					dir: YAHOO.widget.DataTable.CLASS_DESC
				},
				pagination : {
					recordOffset: 0,
					rowsPerPage: 10
				}
			};
		}
		return "format=json" +
		       "&offset=" + state.pagination.recordOffset +
		       "&max=" + state.pagination.rowsPerPage +
		       "&order=" + ((state.sortedBy.dir === YAHOO.widget.DataTable.CLASS_DESC) ? "desc" : "asc") +
		       "&sort=" + state.sortedBy.key;
	}

	function pctFormatter(elCell, oRecord, oColumn, oData) {
		elCell.innerHTML = oData + " %";
	}


	function propertyFormatter(elCell, oRecord, oColumn, oData) {
		var out = "";
		oData.each(function(item, indx) {
			out += item["name"] + ": " + item["value"];
			if (indx < oData.length - 1) {
				out += ", ";
			}
		});
		elCell.innerHTML = out;
	}


	return {
		initHistoryTable: function() {
			var columnDefs = getTestRunTableColumnDefs();
			var dataSource = getTestRunDataSource();
			var tableConfig = getTestRunTableConfig();

			var testRunTable = new YAHOO.widget.DataTable("testRunTableDiv", columnDefs,
				dataSource, tableConfig);

			testRunTable.handleDataReturnPayload = function(oRequest, oResponse, oPayload) {
				if (!oPayload) {
					oPayload = {};
				}
				oPayload.totalRecords = oResponse.meta.totalCount;
				if (oResponse.meta.offset) {
					oPayload.pagination.totalRecords = oResponse.meta.totalCount;
				}
				return oPayload;
			};

			testRunTable.set("selectionMode", "single");
			testRunTable.subscribe("rowClickEvent", onSelectTestRunRow);
			testRunTable.subscribe("rowMouseoverEvent", testRunTable.onEventHighlightRow);
			testRunTable.subscribe("rowMouseoutEvent", testRunTable.onEventUnhighlightRow);
		}
	};
}();