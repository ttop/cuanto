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

	var testRunTable;
	var columnDialog;
	var projectDialog;
	var projectDeleteDialog;
	var deleteButton;
	var deleteCancelButton;
	var timeParser = new YAHOO.cuanto.TimeParser();
	var testRunProperties;
	var selectControl = new YAHOO.cuanto.SelectControl();


	var onSelectTestRunRow = function(e) {
		this.onEventSelectRow(e);
		var currentRow = this.getSelectedRows()[0];
		var currentRecord = this.getRecord(currentRow);
		//var numTests = currentRecord.getData("tests");
		//if (numTests && numTests > 0)
		//{
			window.location = YAHOO.cuanto.urls.get('testRunResults') + currentRecord.getData('id');
		//}
	};


	function getTestRunDataSource() {
		var testRunDataSource = new YAHOO.util.XHRDataSource(YAHOO.cuanto.urls.get("projectHistory"));
		testRunDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
		testRunDataSource.connXhrMode = "queueRequests";
		testRunDataSource.responseSchema = {
			resultsList: 'testRuns',
			fields: ["dateExecuted", "note", "valid", "tests", "passed", "failed", "skipped", "quarantined",
				"totalDuration", "averageDuration",	"successRate", "tests", "id", "numAnalyzed", "testProperties", "tags"],
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


	function getTestRunTableColumnDefs(propertyNames) {
		var columns = [
			{key: "checked", label: "Sel.", width: 20, formatter: selectControl.formatSelect, hidden: true},
			{key:"dateExecuted", label:"Test Run", sortable:true, width: 125},
			{key:"tests", label:"Tests", sortable:true},
			{key:"passed", label:"Passed", sortable:true},
			{key:"failed", label: "Failed", sortable:true},
			{key:"skipped", label: "Skipped", sortable:true},
			{key:"quarantined", label:"Quarant'd", sortable:true},
			{key:"successRate", label: "Success", sortable:true, formatter: pctFormatter},
			{key:"numAnalyzed", label: "Analyzed", sortable:false},
			{key:"totalDuration", label: "Duration", sortable:true, formatter: formatTotalDuration},
			{key:"averageDuration", label: "Avg Duration", sortable:true, formatter: formatAverageDuration},
			{key:"tags", label: "Tags", sortable:false, formatter: formatTags}
		];

		for (var i=0; i < propertyNames.length; i++) {
			columns.push({key: "prop|" + propertyNames[i], label: propertyNames[i], sortable: true, isProp: true,
				formatter: propertyFormatter});
		}

		return columns;
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

	function chooseColumns(e) {
		e.preventDefault();
		$("#columnPanel").show();
		var columnDialog = getColumnDialog();
		columnDialog.show();
	}
	

	function buildTestRunTableQueryString(state) {
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
		       "&sort=" + state.sortedBy.key +
		       "&cb=" + new Date().getTime()
			;
	}

	function pctFormatter(elCell, oRecord, oColumn, oData) {
		$(elCell).html(oData + " %");
	}


	function propertyFormatter(elCell, oRecord, oColumn, oData) {
		var out = "";
		var propName = oColumn.label;
		if (oRecord && oRecord.getData("testProperties")) {

			var prop;
			$.each(oRecord.getData("testProperties"), function(idx, pr) {
				if (pr.name.toLowerCase() == propName.toLowerCase()) {
					prop = pr;
					out = prop["value"];
					return false;
				}
			});

			$(elCell).html(out);
		}
	}


	function formatTotalDuration(elCell, oRecord, oColumn, oData) {
		$(elCell).html(timeParser.formatMs(oRecord.getData("totalDuration")));
	}

	function formatAverageDuration(elCell, oRecord, oColumn, oData) {
		$(elCell).html(timeParser.formatMs(oRecord.getData("averageDuration")));
	}

    function formatTags(elCell, oRecord, oColumn, oData) {
        if (oData && oData.length > 0) {
            $(elCell).html(oData.join(", "));
        }
    }

	function getColumnDialog() {
		if (!columnDialog)
		{
			var columns = $.map(testRunTable.getColumnSet().keys, function(item, idx) {
				return item.key;
			});
			columnDialog = new YAHOO.cuanto.ColumnDialog(testRunTable, null, "ph-" + $('#projectId').val(), ["checked"]);
		}
		return columnDialog;
	}

	function getHiddenColumns() {
		var dialog = getColumnDialog();
		var hiddenCols = dialog.getHiddenColumns();
		if (hiddenCols && hiddenCols[0]) {
			return hiddenCols;
		} else {
			return { "quarantined": true};
		}
	}

	function onEditProject(event) {
		event.preventDefault();
		projectDialog.setTitle("Edit Project");
		projectDialog.show();
		var projectId = event.target.id.match(/.+?(\d+)/)[1];
		projectDialog.loadProject(projectId);
	}


	function onProjectChange(e, data) {
		var eventData = data[0];
		if (eventData.action == "edit") {
			var project = eventData.project;
			$.ajax({
				url: YAHOO.cuanto.urls.get('projectHeader') + "?rand=" + new Date().getTime(),
				data: {id: project.id},
				dataType: "html",
				success: function(response, textStatus, httpReq) {
					$('#phHeader').html(response);
					initHeader();
				}
			});
		} else if (eventData.action == "delete") {
			window.location = YAHOO.cuanto.urls.get('mason');
		}
	}


	function onBulkDelete(e) {
		if (confirm("Are you sure you wish to delete all of the selected test runs?")) {
			hideBulkOperations(null);

			var actionTxt = $("#deleteText");
			actionTxt.show();

			$.ajax({
				url: YAHOO.cuanto.urls.get("testRunBulkDelete"),
				data: YAHOO.lang.JSON.stringify(selectControl.getSelected()),
				type: "POST",
				contentType: "application/json",
				dataType: "json",
				success: function(response, textStatus, httpReq) {
					testRunTable.destroy();
					testRunTable = null;
					initTable();
					addTableListeners();
					hideTestRunDelete();
				}
			});
			selectControl.deselectAllRecords();
		}
	}

	function hideTestRunDelete() {
		$("#deleteText").hide();
		$("#selectTrText").show();
	}

	function showDeleteProject(e) {
		var target = YAHOO.util.Event.getTarget(e);
		YAHOO.util.Event.preventDefault(e);
		var projectId = target.id.match(/.+?(\d+)/)[1];
		var projectName = $('#pName' + projectId).html();
		projectDeleteDialog.show(projectId, projectName);
	}


	function showBulkOperations(e) {
		e.preventDefault();
		$("#showSelectOptions").show();

		selectControl.showColumn();
		testRunTable.showColumn(testRunTable.getColumnSet().getColumn(0));

		$("#bulkButtons").show();
		$("#selectTrText").hide();

		deleteButton = new YAHOO.widget.Button("deleteBtn", {
			onclick: {fn: onBulkDelete},
			disabled: false
		});

		deleteCancelButton = new YAHOO.widget.Button("cancelDeleteBtn", {
			onclick: {fn: closeBulk},
			disabled: false
		});


		testRunTable.removeListener("rowClickEvent", onSelectTestRunRow);
		testRunTable.removeListener("rowMouseoverEvent", testRunTable.onEventHighlightRow);
		testRunTable.removeListener("rowMouseoutEvent", testRunTable.onEventUnhighlightRow);
	}

	function hideBulkOperations(e) {
		if (e) {
			e.preventDefault();
		}
		$("#bulkButtons").hide();
		$("#showSelectOptions").hide();
		selectControl.hideColumn();
	}

	function initHeader() {
		$('.editProj').click(onEditProject);
		$("#chooseColumns").click(chooseColumns);
		$('.deleteProj').click(showDeleteProject);
		$('.bulk').click(showBulkOperations);
		$('#closeBulk').click(closeBulk);
	}

	function closeBulk(e){
			hideBulkOperations(e);
			selectControl.deselectAllRecords();
			$("#selectTrText").show();
			addTableListeners();
		}


	function addTableListeners() {
		testRunTable.subscribe("rowClickEvent", onSelectTestRunRow);
		testRunTable.subscribe("rowMouseoverEvent", testRunTable.onEventHighlightRow);
		testRunTable.subscribe("rowMouseoutEvent", testRunTable.onEventUnhighlightRow);
	}


	function initTable() {
		var columnDefs = getTestRunTableColumnDefs(testRunProperties);
		var dataSource = getTestRunDataSource();
		var tableConfig = getTestRunTableConfig();

		testRunTable = new YAHOO.widget.DataTable("testRunTableDiv", columnDefs,
			dataSource, tableConfig);

		var hiddenCols = getHiddenColumns();

		$.each(testRunTable.getColumnSet().flat, function(idx, column) {
			if (hiddenCols[column.key] != undefined && hiddenCols[column.key]) {
				testRunTable.hideColumn(column.key);
			}
		});

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
		addTableListeners();
	}

	return {
		initHistoryTable: function(testRunProps) {
			testRunProperties = testRunProps;
			$("#showSelectOptions").hide();
			$("#hideSelectCol").hide();
			projectDialog = new YAHOO.cuanto.ProjectDialog();
			projectDeleteDialog = new YAHOO.cuanto.DeleteProjectDialog();
			YAHOO.cuanto.events.projectChangeEvent.subscribe(onProjectChange);
			initHeader();
			initTable();
			selectControl.initTable(testRunTable, 0);
		}
	};

}();