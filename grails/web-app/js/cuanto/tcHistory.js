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


// cuanto/events.js needs to be included in order for this file to work
YAHOO.namespace('cuanto');

YAHOO.cuanto.testCaseHistory = function() {

	var tcHistoryDataTable;
	var tcHistoryId;
	var cacheOutputTimer;
	var outputPanel = new YAHOO.cuanto.OutputPanel();
	var timeParser = new YAHOO.cuanto.TimeParser();

	function getCurrentFilter() {
		return  $('#tcHistoryFilter').val();
	}

	function getTcHistoryColumnDefs() {
		return [
			{label:"Output", width: 35, resizeable:false, formatter: formatActionCol},
			{key:"date", label:"Date", width:130, sortable:true},
			{key:"result", label:"Result", width: 70, sortable:true},
			{key:"analysisState", label:"Reason", width: 100, sortable:true},
			{key:"duration", label:"Duration", sortable:true, formatter: formatDuration},
			{key:"bug", label:"Bug", formatter: YAHOO.cuanto.format.formatBug, sortable:false},
			{key:"owner", label:"Owner", sortable:true},
			{key:"note", label:"Note", width:250, resizeable:true, sortable:true}
		];
	}


	function getTcHistoryDataSource() {
		var tcDataSrc = new YAHOO.util.XHRDataSource(YAHOO.cuanto.urls.get('testCaseHistory') + tcHistoryId + "?");
		tcDataSrc.responseType = YAHOO.util.DataSource.TYPE_JSON;
		tcDataSrc.connXhrMode = "queueRequests";

		tcDataSrc.responseSchema = {
			resultsList: 'testOutcomes',
			fields: ["date", "result", "analysisState", "duration", "bug", "owner", "note", "id"],
			metaFields: {
				recordStartIndex: "recordStartIndex",
				startIndex: "recordStartIndex",
				offset: "recordStartIndex",
				totalRecords: "totalOutcomes",
				totalCount: "totalOutcomes",
				testCase: 'testCase'
			}
		};
		return tcDataSrc;
	}

	function getTcHistoryTableConfig() {
		return {
			initialRequest:buildTcHistoryRequest(),
			height:"600px",
			renderLoopSize:10,
			generateRequest: buildTcHistoryRequest,
			paginator: getTcHistoryPaginator(),
			sortedBy: {key:"date", dir:YAHOO.widget.DataTable.CLASS_DESC},
			dynamicData: true
		};
	}


	function getTcHistoryPaginator() {
		return new YAHOO.widget.Paginator({
			containers         : ['tcHistoryPaging'],
			pageLinks          : 5,
			rowsPerPage        : 10,
			rowsPerPageOptions : [5,10,15,20,30,50,100],
			template       : "{FirstPageLink} {PreviousPageLink} {PageLinks} {NextPageLink} {LastPageLink}<br/>Show {RowsPerPageDropdown} per page"
		});
	}


	function onTcHistoryFilterChange() {
		var order;
		if (tcHistoryDataTable.get("sortedBy").dir == YAHOO.widget.DataTable.CLASS_DESC) {
			order = "desc";
		} else {
			order = "asc";
		}

		var newRequest = "format=json" +
		                 "&filter=" + getCurrentFilter() +
		                 "&offset=0" +
		                 "&max=" + tcHistoryDataTable.get('paginator').getRowsPerPage() +
		                 "&order=" + order +
		                 "&sort=" + tcHistoryDataTable.get("sortedBy").key;

		tcHistoryDataTable.getDataSource().sendRequest(newRequest, {
			success : initTcHistoryTablePageOne,
			failure : function() {
				alert("Failed loading table data");
			}
		});
	}

	function initTcHistoryTablePageOne(oRequest, oResponse, oPayload) {
		var origSort = tcHistoryDataTable.get('sortedBy');  // keep the sort indicator
		tcHistoryDataTable.onDataReturnInitializeTable(oRequest, oResponse, oPayload);
		tcHistoryDataTable.set('sortedBy', origSort);
		tcHistoryDataTable.get('paginator').setPage(1);
	}

	function buildTcHistoryRequest(state) {
		if (!state) {
			state = getDefaultTcHistoryState();
		}
		return "format=json" +
		          "&filter=" + getCurrentFilter() +
		          "&offset=" + state.pagination.recordOffset +
		          "&max=" + state.pagination.rowsPerPage +
		          "&order=" + ((state.sortedBy.dir === YAHOO.widget.DataTable.CLASS_ASC) ? "asc" : "desc") +
		          "&sort=" + state.sortedBy.key +
		          "&id=" + tcHistoryId;
	}

	function getDefaultTcHistoryState() {
		return {
			startIndex: 0,
			sortedBy: {
				key: "date",
				dir: YAHOO.widget.DataTable.CLASS_DESC
			},
			pagination: {
				recordOffset: 0,
				rowsPerPage: 10
			}
		};
	}
	
	function sortTcHistoryColumn(oColumn) {
		var sortOrder = "desc";

	// If already sorted, sort in opposite direction
		if (oColumn.key === this.get("sortedBy").key) {
			sortOrder = (this.get("sortedBy").dir === YAHOO.widget.DataTable.CLASS_ASC) ?
			            "desc" : "asc";
		}
		var newRequest = "format=json" +
		                 "&filter=" + getCurrentFilter() + 
		                 "&offset=0" +
		                 "&max=" + tcHistoryDataTable.get("paginator").getRowsPerPage() +
		                 "&order=" + sortOrder +
		                 "&sort=" + oColumn.key +
		                 "&id=" + tcHistoryId;

		// Create callback for data request
		var onSortedDataLoad = {
			success: function(oRequest, oResponse, oPayload) {
				tcHistoryDataTable.onDataReturnInitializeTable(oRequest, oResponse, oPayload);
				tcHistoryDataTable.get('paginator').setPage(1);
			},
			failure: this.onDataReturnInitializeTable,
			scope: this,
			argument: {
				// Pass in sort values so UI can be updated in callback function
				sorting: {
					key: oColumn.key,
					dir: (sortOrder === "asc") ? YAHOO.widget.DataTable.CLASS_ASC : YAHOO.widget.DataTable.CLASS_DESC
				}
			}
		};

		tcHistoryDataTable.getDataSource().sendRequest(newRequest, onSortedDataLoad);
	}

	function formatActionCol(elCell, oRecord, oColumn, oData) {
		$(elCell).html("");
		var outLinkDiv = $('<div class="outputLink"/>');
		var toId = oRecord.getData('id');
		var outputImg = $('<img alt="Test Output" title="Test Output" class="outLink"/>');
		var outputLinkId = 'to' + toId;
		outputImg.attr("id", outputLinkId);
		outputImg.attr("src", YAHOO.cuanto.urls.get('outputImg'));
		outLinkDiv[0].appendChild(outputImg[0]);
		elCell.appendChild(outLinkDiv[0]);
		YAHOO.util.Event.addListener(outputLinkId, "click", outputPanel.showOutputForLink);
	}

	function formatDuration(elCell, oRecord, oColumn, oData) {
		$(elCell).html(timeParser.formatMs(oRecord.getData("duration")));
	}

	function cacheOutput(){
		clearTimeout(cacheOutputTimer);
		cacheOutputTimer = setTimeout(function() {
			outputPanel.prefetchNextOutputs();
		}, 5000);
	}


	return { // public methods
		initTcHistoryTable:function(testCaseId) {
			tcHistoryId = testCaseId;

			if (!tcHistoryDataTable) {
				YAHOO.util.Event.addListener('tcHistoryFilter', "change", onTcHistoryFilterChange);

				var columnDefs = getTcHistoryColumnDefs();
				var dataSrc = getTcHistoryDataSource();
				var tableConfig = getTcHistoryTableConfig();
				tcHistoryDataTable = new YAHOO.widget.DataTable("tcHistoryTable", columnDefs,
					dataSrc, tableConfig);

				tcHistoryDataTable.handleDataReturnPayload = function (oRequest, oResponse, oPayload) {
					cacheOutput();
					if (!oPayload) {
						oPayload = {};
					}
					if (!oRequest.match("outcome=")) {
						oPayload.totalRecords = oResponse.meta.totalCount;

						if (oPayload.pagination) {
							oPayload.pagination.totalRecords = oResponse.meta.totalCount;
							oPayload.pagination.recordOffset = oResponse.meta.offset;
						}
					}
					return oPayload;
				};

				tcHistoryDataTable.sortColumn = sortTcHistoryColumn;
			}
		}
	};
}();