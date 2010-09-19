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

YAHOO.cuanto.testCases = function() {
	var project;
	var testCaseTable;

	function getColumnDefs() {
		return [
			{ key:"fullName", label:"Test Name", formatter:formatTestCase },
			{ key: "descr", label: "Description" },
			{ label: "Operation", formatter: formatOperations }
			
		];
	}

	function getDataSource() {
		var tcDataSrc = new YAHOO.util.XHRDataSource(YAHOO.cuanto.urls.get('testCaseList') + project + "?");
		tcDataSrc.responseType = YAHOO.util.DataSource.TYPE_JSON;
		tcDataSrc.connXhrMode = "queueRequests";

		tcDataSrc.responseSchema = {
			resultsList: 'testCases',
			fields: ["fullName", "pkg", "name", "descr", "type", "id"],
			metaFields: {
				offset: "recordStartIndex",
				totalCount: "totalCases"
			}
		};
		return tcDataSrc;
	}

	function getTableConfig() {
		return {
			initialRequest: buildTcRequest(),
			height:"600px",
			renderLoopSize: 10,
			generateRequest: buildTcRequest,
			paginator: getTcPaginator(),
			dynamicData: true
		};
	}

	function buildTcRequest(state) {
		if (!state) {
			state = getDefaultTcState();
		}
		var req = "format=json" +
	              "&offset=" + state.pagination.recordOffset +
	              "&max=" + state.pagination.rowsPerPage;
		return req;
	}

	function getDefaultTcState() {
		return {
			startIndex: 0,
			pagination: {
				recordOffset: 0,
				rowsPerPage: 10
			}
		};
	}

	function getTcPaginator() {
		return new YAHOO.widget.Paginator({
			containers : ['tcPaging'],
			pageLinks : 5,
			rowsPerPage : 10,
			template : "{FirstPageLink} {PageLinks} {LastPageLink}",
			pageLabelBuilder : function (page, paginator) {
				var recs = paginator.getPageRecords(page);
				return (recs[0] + 1) + ' - ' + (recs[1] + 1);
			}
		});
	}

	function formatTestCase(elCell, oRecord, oColumn, oData) {
		elCell.innerHTML = YAHOO.cuanto.format.breakOnToken(oData, '.',400);
	}

	function formatOperations(elCell, oRecord, oColumn, oData) {
		var tcId = oRecord.getData().id;
		var editLink = YAHOO.cuanto.urls.get('testCaseEdit') + tcId;
		var historyLink = YAHOO.cuanto.urls.get('testCaseHistory') + tcId;
		var deleteLink = YAHOO.cuanto.urls.get('testCaseDelete')+ tcId; // todo: implement
		elCell.innerHTML = "<a href='" + editLink + "'>Edit</a> | <a href='" + deleteLink +
	                       "'>Delete</a> | <a href='" + historyLink + "'>History</a>";
	}


	function onSaveRecordFailure(o) {
		alert("Failed saving test case " + o.argument.getData("id") +
		"\n" + o.responseText);
	}


	function onSaveRecordSuccess(o) {
		//YAHOO.cuanto.events.outcomeChangeEvent.fire(o.argument.getData("id"));
	}

	function saveRecord (record) {
		var postBody = getPostBodyForRecord(record);
		var callback = {
			success: onSaveRecordSuccess,
			failure: onSaveRecordFailure,
			argument: record
		};
		YAHOO.util.Connect.asyncRequest('POST', YAHOO.cuanto.urls.get('testCaseUpdate'), callback, postBody);
	}

	function getPostBodyForRecord(record) {
		var body = "id=" + record.getData("id");
		body += "&packageName=" + record.getData("pkg");
		body += "&testName=" + record.getData("name");
		body += "&description=" + record.getData("descr");		
		body += "&project=" + project;
		return body;
	}

	return { // public methods
		initTestCaseTable:function(projectId) {
			project = projectId;
			testCaseTable = new YAHOO.widget.DataTable("tcTable", getColumnDefs(), getDataSource(), getTableConfig());

			testCaseTable.handleDataReturnPayload = function (oRequest, oResponse, oPayload) {
				if (!oPayload) {
					oPayload = {};
				}

				oPayload.totalRecords = oResponse.meta.totalCount;

				if (oPayload.pagination) {
					oPayload.pagination.totalRecords = oResponse.meta.totalCount;
					oPayload.pagination.recordOffset = oResponse.meta.offset;
				}

				return oPayload;
			};
		}
	};
}();