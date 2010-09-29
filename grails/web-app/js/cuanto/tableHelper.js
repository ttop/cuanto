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

YAHOO.cuanto.tables = function() {
	var pub = {};  // public methods

	// get the start index for the current page widened by one record
	function getWideStartIndex(table) {
		var paginator = table.get("paginator");
		var startIdx;
		if (paginator.getStartIndex() > 0) {
			startIdx = paginator.getStartIndex() - 1;
		} else {
			startIdx = 0;
		}
		return startIdx;
	}


	// get the end index for the current page widened by one record
	function getWideEndIndex(table) {
		var paginator = table.get("paginator");
		var endIdx;
		var startIdx = getWideStartIndex(table);
		if (startIdx + paginator.getRowsPerPage() + 1 < paginator.getTotalRecords() - 1) {
			endIdx = startIdx + paginator.getRowsPerPage() + 1;
		} else {
			endIdx = paginator.getTotalRecords() - 1;
		}
		return endIdx;
	}

	function getCurrentTcFormat() {
		//return $F('tcFormat', 'value');
		return $("#tcFormat").val();
	}

	function onSaveRecordFailure(o) {
		var failPanel =	new YAHOO.widget.Panel("failPanel", {dragOnly: true, width: $(window).width() * .8 + "px",
				visible: true, xy: [100,100], zIndex: 10,
				effect:{effect:YAHOO.widget.ContainerEffect.FADE,duration:0.15}});
		failPanel.setHeader("Failed saving record " + o.argument.getData("id"));
		failPanel.setBody(o.responseText);
		failPanel.render("failContainer");
		failPanel.show();
		YAHOO.cuanto.events.outcomeChangeEvent.fire(o.argument.getData("id"));
	}


	function onSaveRecordSuccess(o) {
		YAHOO.cuanto.events.outcomeChangeEvent.fire(o.argument.getData("id"));
	}


	function getPostBodyForRecord(record) {
		var body = "id=" + record.getData("id");
		var bug = record.getData("bug");

		if (bug != null) {
			if (typeof bug == "string") {
				body += "&bug=" + bug;
			} else {
				if (bug.title) {
					body += "&bugTitle=" + encodeURIComponent(bug.title);
				}
				if (bug.url) {
					body += "&bugUrl=" + encodeURIComponent(bug.url);
				}
			}
		}

		var analysisState = record.getData("analysisState");
		if (analysisState != null) {
			if (typeof analysisState == "string") {
				body += "&analysisStateName=" + analysisState;
			} else {
				body += "&analysisStateName=" + analysisState.name;
			}
		}

		var fields = [{key: 'result', param:'testResult'},{key: 'note', param: 'note'}, {key: 'owner', param: 'owner'}];

		for (var f = 0; f < fields.length; f++) {
			var field = fields[f];
			if (f) {
				var data = record.getData(field['key']);
				if (data) {
					body += "&" + field['param'] + "=" + encodeURIComponent(data);
				}
			}
		}
		return body;
	}


	function getIndexOfRecord(record, table) {
		var indx = table.getRecordIndex(record);
		if (indx == null) {
			// relocate record in recordset
			var rec = pub.getRecordForOutcome(record.getData("id"), table);
			indx = table.getRecordIndex(rec);
		}
		return indx;
	}


	pub.getRecordForOutcome = function(outcomeId, table) {
		// didn't find a selected record, so locate this outcome ID in the recordset

		var recordToReturn;
		var recordSet = table.getRecordSet();
		var paginator = table.get("paginator");

	// widen the current page by one record on either side and search for the outcome
		for (var i = getWideStartIndex(table); i < getWideEndIndex(table) + 1; i++) {
			var record = recordSet.getRecord(i);
			if (record && record.getData("id") == outcomeId) {
				recordToReturn = record;
				break;
			}
		}

		if (!recordToReturn) { // check the records prior to the current page
			for (var i = 0; i < getWideStartIndex(table); i ++) {
				var record = recordSet.getRecord(i);
				if (record && record.getData("id") == outcomeId) {
					recordToReturn = record;
					break;
				}
			}
		}

		if (!recordToReturn) { // check the records following the current page
			for (var i = getWideEndIndex(table) + 1; i < paginator.getTotalRecords(); i++) {
				var record = recordSet.getRecord(i);
				if (record && record.getData("id") == outcomeId) {
					recordToReturn = record;
					break;
				}
			}
		}
		if (!recordToReturn) {
			alert("test outcome record " + outcomeId + " not found in the local recordset!");
		}
		return recordToReturn;
	};


	pub.saveRecord = function(record) {
		var postBody = getPostBodyForRecord(record);
		var callback = {
			success: onSaveRecordSuccess,
			failure: onSaveRecordFailure,
			argument: record
		};
		YAHOO.util.Connect.asyncRequest('POST', YAHOO.cuanto.urls.get('saveOutcome'), callback, postBody);
	};


	pub.selectRecord = function(record, table) {
		// select the record if not already selected
		var refresh = true;
		var selectedRows = table.getSelectedTrEls();
		if (selectedRows.length == 1) {
			var currentRecord = table.getRecord(selectedRows[0]);
			if (currentRecord.getData("id") == record.getData("id")) {
				refresh = false;
			}
		}
		if (refresh) {
			table.unselectAllRows();
			table.selectRow(record);
		}
	};


	pub.updateDataTableRecord = function(e, args, table) {
		// Update the record in the datatable with the specified ID by querying the server for new data
		var selectedId = args[0];
		var selectedRecord = this.getRecordForOutcome(selectedId, table);
		var refreshRecord = {
			success : function(oRequest, oResponse, oPayload) {
				oPayload = {pagination: {recordOffset: oResponse.meta.offset}};
				if (oResponse.results && oResponse.results.length > 0) {
					var recs = this.getRecordSet().getRecords();
					var rec = recs[oResponse.meta.offset];
					if (rec) {
						this.updateRow(rec, oResponse.results[0]);
					}
				}
			},
			failure : function(oRequest, oResponse, oPayload) {
				alert("Failed loading table data");
			},
			scope: table
		};

		var resultsPaginator = table.get('paginator');
		var req = "format=json&outcome=" + selectedRecord.getData()["id"] +
		          "&recordStartIndex=" + getIndexOfRecord(selectedRecord, table) +
		          "&totalCount=" + resultsPaginator.getTotalRecords() + "&tcFormat=" + getCurrentTcFormat() +
		          "&rand=" + new Date().getTime();
		// must pass along the totalCount so that it is returned in the response meta fields and
		// so the paginator shows the correct total
		table.getDataSource().sendRequest(req, refreshRecord);
	};

	return pub;
}();

