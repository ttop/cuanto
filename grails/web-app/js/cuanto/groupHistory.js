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

YAHOO.cuanto.groupHistory = function() {

	var testRunTable;
	var timeParser = new YAHOO.cuanto.TimeParser();

	var onSelectTestRunRow = function(e) {
		this.onEventSelectRow(e);
		var currentRow = this.getSelectedRows()[0];
		var currentRecord = this.getRecord(currentRow);
		//var numTests = currentRecord.getData("tests");
		//if (numTests && numTests > 0)
		//{
			window.location = YAHOO.cuanto.urls.get('testRunLatest') + currentRecord.getData('projectKey');
		//}
	};


	function getTestRunDataSource() {
		var testRunDataSource = new YAHOO.util.XHRDataSource(YAHOO.cuanto.urls.get("groupHistory"));
		testRunDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
		testRunDataSource.connXhrMode = "queueRequests";
		testRunDataSource.responseSchema = {
			resultsList: 'testRuns',
			fields: ["projectName", "projectKey","dateExecuted", "note", "valid", "testProperties", "skipped",
				"tests", "passed", "failed", "quarantined", "totalDuration", "averageDuration",	"successRate",
				"successRateChange", "tests", "id", "numAnalyzed"],
			metaFields: { totalCount:"totalCount", offset:"offset" }
		};
		return testRunDataSource;
	}


	function getTestRunTableConfig() {
		return {
			sortedBy: {key:"dateExecuted", dir:YAHOO.widget.DataTable.CLASS_DESC}
		};
	}


	function getTestRunTableColumnDefs() {
		return [
			{key:"projectName", label: "Project Name", sortable: false},
			{key:"dateExecuted", label:"Test Run", sortable:true},
			{key:"tests", label:"Tests", sortable:true},
			{key:"passed", label:"Passed", sortable:true},
			{key:"failed", label: "Failed", sortable:true},
			{key:"skipped", label: "Skipped", sortable:true},
			{key:"quarantined", label:"Quarant'd", sortable:true},
			{key:"successRate", label: "Success", sortable:true, formatter: pctFormatter},
			{key:"successRateChange", label:"% Change", sortable:true, formatter: pctFormatter},
			{key:"numAnalyzed", label: "Analyzed", sortable:false},
			{key:"totalDuration", label: "Duration", sortable:true, formatter: formatTotalDuration},
			{key:"averageDuration", label: "Avg Duration", sortable:true, formatter: formatAvgDuration},
			{key:"testProperties", label: "Properties", sortable:false, formatter: propertyFormatter}
		];
	}


	function pctFormatter(elCell, oRecord, oColumn, oData) {
		if (oData == null) {
			$(elCell).html("N/A");
		} else {
			$(elCell).html(oData + " %");
		}
	}


	function propertyFormatter(elCell, oRecord, oColumn, oData) {
		var out = "";
		$.each(oData, function(indx, item) {
			out += item["name"] + ": " + item["value"];
			if (indx < oData.length - 1) {
				out += ", ";
			}
		});
		$(elCell).html(out);
	}


	function formatTotalDuration(elCell, oRecord, oColumn, oData) {
		$(elCell).html(timeParser.formatMs(oRecord.getData("totalDuration")));
	}

	function formatAvgDuration(elCell, oRecord, oColumn, oData) {
		$(elCell).html(timeParser.formatMs(oRecord.getData("averageDuration")));
	}

	function onProjectChange(e, data) {
		testRunTable.getDataSource().sendRequest("?rand=" + new Date().getTime(), {
			success: function(oRequest, oResponse, oPayload) {
				testRunTable.onDataReturnInitializeTable(oRequest, oResponse, oPayload);
			}
		});
	}

	return {
		initGroupHistoryTable: function() {
			var columnDefs = getTestRunTableColumnDefs();
			var dataSource = getTestRunDataSource();
			var tableConfig = getTestRunTableConfig();

			testRunTable = new YAHOO.widget.DataTable("testRunTableDiv", columnDefs,
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

			YAHOO.cuanto.events.projectChangeEvent.subscribe(onProjectChange);

			testRunTable.set("selectionMode", "single");
			testRunTable.subscribe("rowClickEvent", onSelectTestRunRow);
			testRunTable.subscribe("rowMouseoverEvent", testRunTable.onEventHighlightRow);
			testRunTable.subscribe("rowMouseoutEvent", testRunTable.onEventUnhighlightRow);
		}
	};
}();