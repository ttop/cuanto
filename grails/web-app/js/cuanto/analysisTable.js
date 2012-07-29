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

YAHOO.cuanto.AnalysisTable = function(testResultNames, analysisStateNames, propNames) {
	var analysisCookieName = "cuantoAnalysis";
	var prefTcFormat = "tcFormat";
	var prefHiddenColumns = "hiddenColumns";

	var dataTable;
	var analysisDialog;
	var bugColumnLabel = "Bug";
	var noteColumnLabel = "Note";
	var targetOutcomes = new Array();
	var ovrlyMgr = new YAHOO.widget.OverlayManager();
	var outputProxy = new YAHOO.cuanto.OutputProxy();
	var outputPanel = new YAHOO.cuanto.OutputPanel(outputProxy);
	var timeParser = new YAHOO.cuanto.TimeParser();
	var cacheOutputTimer;
	var historyImgUrl =  YAHOO.cuanto.urls.get('historyImg');
	var outputImgUrl = YAHOO.cuanto.urls.get('outputImg');
	var anlzImgUrl = YAHOO.cuanto.urls.get('analysisImg');
	var alertImgUrl = YAHOO.cuanto.urls.get('alertImg');
	var columnDialog;
    var tagButtons = [];

	var dataTableEventOverrides = {
		"cellClickEvent": [
			{
				columnLabel: bugColumnLabel,
				overriddenTarget: 'IMG'
			},
			{
				columnLabel: noteColumnLabel,
				overriddenTarget: 'A'
			}
		]
	};

	YAHOO.cuanto.events.outcomeChangeEvent.subscribe(function(e, args) {
		YAHOO.cuanto.tables.updateDataTableRecord(e, args, dataTable);
	});

	YAHOO.cuanto.events.recordSelectedEvent.subscribe(function(e, args) {
		addRecord(args[0]);
	});

	YAHOO.cuanto.events.recordsUpdatedEvent.subscribe(updateTable);
	YAHOO.cuanto.events.bulkAnalysisEvent.subscribe(applyAnalysis);
	YAHOO.cuanto.events.testRunChangedEvent.subscribe(onTestRunChanged);
    YAHOO.cuanto.events.outcomeFilterChangeEvent.subscribe(onFilterChangeEvent);

	hideCurrentSearchSpan();
	$("#searchQry").val("");

	if (!dataTable) {
		YAHOO.util.Event.addListener('trDetailsFilter', "change", onFilterChange);
		YAHOO.util.Event.addListener('tcFormat', "change", onTcFormatChange);
		YAHOO.util.Event.addListener('searchTerm', "change", onSearchTermChange);
		onSearchTermChange(null);
		dataTable = new YAHOO.widget.DataTable("trDetailsTable", getDataTableColumnDefs(),
			getAnalysisDataSource(), getDataTableConfig());

		var hiddenCols = getHiddenColumns();

		$.each(dataTable.getColumnSet().flat, function(idx, column) {
			if (hiddenCols[column.key] != undefined && hiddenCols[column.key]) {
				dataTable.hideColumn(column.key);
			}
		});

		dataTable.handleDataReturnPayload = processPayload;
		dataTable.set("selectionMode", "single");

		// iterate over dataTableEventOverrides.cellClickEvent for any overridden events
		dataTable.subscribe("cellClickEvent",
			function(o) {
				var eventTarget = getEventTargetTagname(o.event);
				var eventOverriden = false;
				var cellClickEventOverrides = dataTableEventOverrides.cellClickEvent;

				for (var i = 0; i < cellClickEventOverrides.length; ++i)
				{
					var overrideInfo = cellClickEventOverrides[i];
					if (dataTable.getColumn(o.target).label == overrideInfo.columnLabel &&
					    eventTarget == overrideInfo.overriddenTarget)
					{
						eventOverriden = true;
						break;
					}
				}

				// invoke the cell editor only if the event is not overridden.
				if (!eventOverriden){
					dataTable.onEventShowCellEditor(o);
				}

				var col = dataTable.getColumn(o.target);
				if (col.getKeyIndex() != 0)
				{
					highlightRowForEvent(o);
				}

			}, this);
		dataTable.subscribe("editorSaveEvent", inlineEditHandler);
		dataTable.subscribe("editorBlurEvent", function(oArgs) {
			this.cancelCellEditor();
		});
		analysisDialog = new YAHOO.cuanto.analysisDialog(ovrlyMgr, outputProxy);

        initTagButtons();

		YAHOO.util.Event.addListener("showSelectCol", "click", showSelectColumn);
		$('#showSelectOptions').hide();
		YAHOO.util.Event.addListener("hideSelectCol", "click", hideSelectColumn);
		YAHOO.util.Event.addListener("selectPage", "click", selectPageRecords);
		YAHOO.util.Event.addListener("deselectPage", "click", deselectPageRecords);
		YAHOO.util.Event.addListener("deselectAll", "click", deselectAllRecords);
		YAHOO.util.Event.addListener("searchSubmit", "click", onTableStateChange);
		YAHOO.util.Event.addListener("editNote", "click", onEditNote);
		YAHOO.util.Event.addListener("saveNote", "click", onSaveNote);
		YAHOO.util.Event.addListener("cancelNote", "click", onCancelNote);
		YAHOO.util.Event.addListener("deleteTestRun", "click", deleteTestRun);
		YAHOO.util.Event.addListener("recalcStats", "click", recalcStats);
		YAHOO.util.Event.addListener("chooseColumns", "click", chooseColumns);


		new YAHOO.widget.Tooltip("feedtt", {context:"feedImg"});
		new YAHOO.widget.Tooltip("selectTip", { context:"showSelectCol", text:"Show/Hide Select Column",
			showDelay:500 });
	} else {
		dataTable.render();
	}


	function getAnalysisDataSource() {
		var dataSource = new YAHOO.util.XHRDataSource(YAHOO.cuanto.urls.get('testRunOutcomes'));
		dataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
		dataSource.connXhrMode = "allowAll";
		dataSource.maxCacheEntries = 0;
		dataSource.responseSchema = {
			resultsList: 'testOutcomes',
			fields: ["testCase", "result", "streak", "successRate", "testCase.analysisCount", "analysisState", "duration",
				"bug", "owner", "note", "id", "testOutput",	"startedAt", "finishedAt", "tags", "isFailureStatusChanged",
				"testProperties", "links"],
			metaFields: {
				offset: "offset",
				totalCount: "totalCount"
			}
		};
		return dataSource;
	}


	function getDataTableConfig() {
		var tableWidth;
		var minWidth = 1024;
		if ($(window).width() > minWidth) {
			tableWidth = $(window).width() * .95;
		} else {
			tableWidth = minWidth;
		}

		var initReq = getDefaultTableState();
		return {
			initialRequest: initReq,
			width: tableWidth + "px",
			renderLoopSize:10,
			generateRequest: buildOutcomeQueryString,
			paginator: getDataTablePaginator(0, Number.MAX_VALUE, 0, 10),
			sortedBy: {key:"testCase", dir:YAHOO.widget.DataTable.CLASS_ASC},
			dynamicData: true
		};
	}


	function getDataTablePaginator(page, totalRecs, offset, rows) {
		var config = {
			containers         : ['trDetailsPaging'],
			pageLinks          : 25,
			rowsPerPage        : rows,
			rowsPerPageOptions : [5,10,15,20,30,50,100],
			template       : "{FirstPageLink} {PreviousPageLink} {PageLinks} {NextPageLink} {LastPageLink}<br/>Show {RowsPerPageDropdown} rows per page"
		};
		config.initialPage = page;
		config.totalRecords = totalRecs;
		config.offset = offset;
		return new YAHOO.widget.Paginator(config);
	}

	function processPayload(oRequest, oResponse, oPayload) {
		if (!oPayload) {
			oPayload = {};
		}

		if (!oRequest || !oRequest.match("outcome=")) {
			oPayload.totalRecords = parseInt(oResponse.meta.totalCount);
		}

		processProperties(oResponse);
		updateTotalRows(oResponse);
		cacheOutput();

		return oPayload;
	}


	function processProperties(oResponse) {
		var propNames = [];
		$.each(oResponse.results, function(idx, item) {
			for (var key in item.testProperties) {
				if ($.inArray(key, propNames) == -1) {
					propNames.push(key);
				}
			}
		});

		if (propNames.length > 0) {
			var colNames = $.map(dataTable.getColumnSet().keys, function(k, i) {
				return k.key;
			});
			for (var p = 0; p < propNames.length; p++) {
				var prop = propNames[p];
				var matchingNames = $.grep(colNames, function(name) {
					return name.toLowerCase() == prop.toLowerCase();
				});
				if (matchingNames.length == 0) {
					var col = new YAHOO.widget.Column({key: prop, label: prop, resizeable: true, width: 100, sortable:true,
						formatter: propertyFormatter});
					var hiddenCols = getHiddenColumns();
					dataTable.insertColumn(col);
					if (hiddenCols[prop]) {
						dataTable.hideColumn(prop);
					}
					colNames.push(prop);
				}
			}
		}
	}

	function updateTotalRows(oResponse) {
		$('#trTotalRows').html(oResponse.meta.totalCount);
	}

	function hasColumnNamed(dt, colName) {
		$.each(dt.getColumnSet().keys, function(idx, col) {
			if (col.key == colName) {
				return true;
			}
		});
		return false;
	}

	function initDataTablePageOne(oRequest, oResponse, oPayload) {
		var origSort = dataTable.get('sortedBy');  // keep the sort indicator
		dataTable.onDataReturnInitializeTable(oRequest, oResponse, oPayload);
		dataTable.set('sortedBy', origSort);
		dataTable.get('paginator').setPage(1);
		cacheOutput();
	}


	function onFilterChange(e) {
		var newRequest = generateNewRequest();

		dataTable.getDataSource().sendRequest(newRequest, {
			success : initDataTablePageOne,
			failure : function() {
				alert("Failed loading table data");
			}
		});
		YAHOO.cuanto.events.outcomeFilterChangeEvent.fire({results:getCurrentFilter(), context: 'analysisTable'});
	// getCurrentFilter()
	}


	function generateNewRequest() {
        //todo: can this logic be combined with buildOutcomeQueryString?
		var order;
		if (dataTable.get("sortedBy").dir == YAHOO.widget.DataTable.CLASS_DESC) {
			order = "desc";
		} else {
			order = "asc";
		}
		var newRequest = "format=json" +
		                 "&filter=" + getCurrentFilter() +
		                 "&offset=0" +
		                 "&max=" + dataTable.get('paginator').getRowsPerPage() +
		                 "&order=" + order +
		                 "&sort=" + dataTable.get("sortedBy").key +
		                 "&tcFormat=" + getCurrentTcFormat() +
		                 getSearchQuery() +
                         getTagsQuery() +
                         "&rand=" + new Date().getTime();

		return newRequest;
	}


    function onFilterChangeEvent(e, arg) {
	    var filter = arg[0];

	    if (filter.context != 'analysisTable') {
		    var resultFilterNode = $('#trDetailsFilter')[0];
		    var resultsFilter = resultFilterNode.length - 1;
		    if (filter.results) {
			    var targResult = filter.results.toUpperCase();
			    for (var i = 0; i < resultFilterNode.options.length; i++) {
				    var item = resultFilterNode.options[i];
				    if (item.text.toUpperCase() == targResult || $(item).val().toUpperCase() == targResult) {
					    resultsFilter = i;
					    break;
				    }
			    }
		    }

		    resultFilterNode.selectedIndex = resultsFilter;

		    if (filter.search) {
			    var searchIndex;
			    var searchTermNode = $('#searchTerm')[0];
			    var targSearch = filter.search.toUpperCase();
			    for (var s = 0; s < searchTermNode.options.length; s++) {
				    var st = searchTermNode.options[s];
				    if (st.text.toUpperCase() == targSearch || $(item).val().toUpperCase() == targSearch) {
					    searchIndex = s;
					    break;
				    }
			    }
			    if (!(searchIndex == undefined)) {
				    searchTermNode.selectedIndex = searchIndex;
			    }
		    }

		    if (filter.qry) {
			    $('#searchQry').val(filter.qry);
		    }
		    onFilterChange(null);
		    showCurrentSearchSpan();
	    }
    }


	function getSearchQuery() {
		if (searchQueryIsSpecified()) {
			var term = $("#searchTerm").val();
			if (term == "Properties") {
				return "&qry=" + term + "|" + $("#propName").val() + "|" + $("#searchQry").val();
			} else {
				return "&qry=" + term + "|" + $("#searchQry").val();
			}
		} else {
			return "";
		}
	}

	function searchQueryIsSpecified() {
		return $("#searchQry").val().search(/\S/) != -1;
	}


    function getTagsQuery() {
        var query = "";
        if (tagButtons.length > 0) {
            if (tagButtons[0].get("checked")) {
                query += "&hasTags=true";
            } else if (tagButtons[1].get("checked")){
                query+= "&hasTags=false";
            } else {
                for (var i = 2; i < tagButtons.length; i++) {
                    var btn = tagButtons[i];
                    if (btn.get("checked")) {
                        query += "&tag=" + btn.get("label");
                    }
                }
            }
        }
        return query;
    }

	function getEventTargetTagname(e) {
		if ($.browser.msie) {
			return e.srcElement.tagName;
		} else {
			return e.target.tagName;
		}
	}


	function inlineEditHandler(oArgs) {
		YAHOO.cuanto.tables.saveRecord(oArgs.editor.getRecord());
	}


	function getDefaultTableState() {
		var tcFormat = YAHOO.util.Cookie.getSub(analysisCookieName, prefTcFormat);
		if (tcFormat) {
			$('#tcFormat').val(tcFormat);
		} else {
			setTcFormatPref();
		}
		return "format=json&offset=0&max=10&order=asc&sort=testCase&filter=" + getCurrentFilter() +
		       "&tcFormat=" + tcFormat + "&rand=" + new Date().getTime();
	}


	function buildOutcomeQueryString(state) {
        //todo: can this logic be combined with generateNewRequest?
		var qry = "format=json" +
		          "&filter=" + getCurrentFilter() +
		          "&offset=" + state.pagination.recordOffset +
		          "&max=" + state.pagination.rowsPerPage +
		          "&order=" + ((state.sortedBy.dir === YAHOO.widget.DataTable.CLASS_DESC) ? "desc" : "asc") +
		          "&sort=" + state.sortedBy.key +
		          "&tcFormat=" + getCurrentTcFormat() +
		          getSearchQuery() +
                  getTagsQuery() +
		          "&rand=" + new Date().getTime();
		showCurrentSearchSpan();
		return qry;
	}


	function getCurrentFilter() {
		return $('#trDetailsFilter').val();
	}


	function getCurrentTcFormat() {
		return $('#tcFormat').val();
	}


	function getDataTableColumnDefs() {
		var bugEditor = new YAHOO.widget.TextboxCellEditor();
		bugEditor.subscribe("showEvent", showBugEditor);

		var noteEditor = new YAHOO.widget.TextareaCellEditor();
		noteEditor.subscribe("showEvent", showNoteEditor);

		var toolWidth = $.browser.msie ? 70 : 50;

		var columns = [
			{label:"Sel.", resizeable:false, formatter: formatSelect, hidden:true},
			{label:"Tools", resizeable:false, formatter: formatActionCol, width:toolWidth},
			{key:"testCase", label:"Name", resizeable:true, className:"wrapColumn",
				formatter: formatTestCase, sortable:true},
			{key:"parameters", label:"Parameters", resizeable:true, formatter:formatParameters, sortable: false},
            {key:"tags", label:"Tags", resizeable:true, formatter: formatTags, sortable: false, hidden: ($('.tagspan').length == 0)},
			{key:"result", label:"Result", sortable:true,
				editor:new YAHOO.widget.DropdownCellEditor({dropdownOptions:testResultNames})},
            {key:"streak", label:"Streak", sortable:true },
            {key:"successRate", label:"Pass Rate", sortable:true, formatter: formatPercentage },
			{key:"analysisState", label:"Reason", sortable:true,
				editor:new YAHOO.widget.DropdownCellEditor({dropdownOptions:analysisStateNames, disableBtns:true})},
			{key:"startedAt", label: "Started At", sortable:true},
			{key:"finishedAt", label: "Finished At", sortable:true},
			{key:"duration", label:"Duration", sortable:true, formatter: formatDuration},
			{key:"bug", label: bugColumnLabel, formatter:YAHOO.cuanto.format.formatBug, sortable:true,
				editor: bugEditor},
			{key:"owner", label:"Owner", width:90, sortable:true, editor:new YAHOO.widget.TextboxCellEditor()},
			{key:"note", label:"Note", formatter:YAHOO.cuanto.format.formatNote, minWidth:150, resizeable:true, sortable:true,
                editor: noteEditor},
			{key: "testOutput", label: "Output", minWidth: 150, resizeable: true, sortable: true,
				formatter:YAHOO.cuanto.format.formatOutput},
			{key: "links", label: "Links", minWidth: 100, resizeable:true, sortable: false, formatter: linkFormatter }
		];

		return columns;
	}


	function showBugEditor(o) {
		var title = o.editor.getRecord().getData("bug")["title"];
		var url = o.editor.getRecord().getData("bug")["url"];
		if (title != null) {
			o.editor.textbox.value = title;
		} else if (url != null) {
			o.editor.textbox.value = url;
		} else {
			o.editor.textbox.value = "";
		}
	}


	function showNoteEditor(o) {
		var note = o.editor.getRecord().getData("note");
		if (note == null) {
			o.editor.textarea.value = "";
		} else {
			o.editor.textarea.value = unescapeHtmlEntities(note);
		}
	}


	function formatSelect(elCell, oRecord, oColumn, oData) {
		elCell.innerHTML = "";
		var rid = oRecord.getData("id");
		var boxid = "chk" + rid;
		var div = $("<div></div>");
		var chkbox = $("<input type='checkbox' value='" + rid + "' class='batch' id='" + boxid + "'/>");
		elCell.appendChild(div[0]).appendChild(chkbox[0]);

		if (targetOutcomes.indexOf(boxid) > -1) {
			chkbox.attr("checked", true);
		}
		YAHOO.util.Event.addListener(boxid, "click", selectRecord);
	}

    function formatPercentage(elCell, oRecord, oColumn, oData) {
        elCell.style.textAlign = "right";
        $(elCell).html(oData + '%');
    }

	function formatTestCase(elCell, oRecord, oColumn, oData) {
		$(elCell).html("");

		var result = oRecord.getData().result.toLowerCase();
		var isNewFailure = oRecord.getData().isFailureStatusChanged && (result == "fail" || result == "error");

		var testCaseString = YAHOO.cuanto.format.breakOnToken(oData.name, '.', 400) + " ";
		if (isNewFailure)
		{
			var alertImg = $("<img src='" + alertImgUrl +"'/>");
			setImgTitleAndAlt(alertImg, "New Failure");
			elCell.appendChild(alertImg[0]);
			$(elCell).html($(elCell).html() + " ");
		}

		var oldHtml = $(elCell).html();

		var newHtml = oldHtml += testCaseString;
		$(elCell).html(newHtml);
	}

	function formatParameters(elCell, oRecord, oColumn, oData) {
		var tc = oRecord.getData('testCase');
		var out = "";
		if (tc && tc.parameters) {
			out = tc.parameters;
		}
		$(elCell).html(out);
	}

	function formatActionCol(elCell, oRecord, oColumn, oData) {
		$(elCell).html("");
		var toId = oRecord.getData('id');
		var tcId = oRecord.getData("testCase").id;

		var outputLinkId = 'to' + toId;
		$(elCell).append("<a href=''><img src='" + outputImgUrl + "' id='" + outputLinkId + "' class='outLink'/></a>");

		var anlzLinkId = 'an' + toId;
		$(elCell).append("<a href='' id='" + anlzLinkId + "' class='anlzLink'><img src='" + anlzImgUrl + "'/></a>");

		var historyLinkId = 'history' + tcId;
		var analysisCount = oRecord.getData("testCase.analysisCount");
		if (analysisCount > 9) {
			analysisCount = "+";
		}
		$(elCell).append("<a class='maglink' href='' id='" + historyLinkId + "'><img class='mag' src='" + historyImgUrl + "'/><div class='acnt'>" + analysisCount + "</div></a>");

		YAHOO.util.Event.addListener(outputLinkId, "click", handleOutputIcon);
		YAHOO.util.Event.addListener(historyLinkId, "click", showHistoryForLink, tcId);
		YAHOO.util.Event.addListener(anlzLinkId, "click", showAnalysisDialog, null,
			analysisDialog);
		setTimeout(function() {
			setImgTitleAndAlt($("img", $('#' + historyLinkId)), "Test History (new window)");
			setImgTitleAndAlt($("#" + outputLinkId), 'Test Output');
			setImgTitleAndAlt($("img", $("#" + anlzLinkId)), 'Batch Analysis');
		}, 2000);
	}

    function formatTags(elCell, oRecord, oColumn, oData) {
        if (oData && oData.length > 0) {
            $(elCell).html(oData.join(", "));
        }
    }

	function setImgTitleAndAlt(imgElem, title) {
		imgElem.attr('title', title);
		imgElem.attr('alt', title);
	}

	function showAnalysisDialog(e) {
		analysisDialog.showAnalysisDialog(e);
	}


	function handleOutputIcon(e) {
		var currentlyDisplayed = outputPanel.getCurrentTargetId();
		var currentlySelected = YAHOO.util.Event.getTarget(e).id ;
		if (currentlyDisplayed == currentlySelected ) {
			outputPanel.closePanel();
		} else {
			outputPanel.showOutputForLink(e, ovrlyMgr);
		}
	}

	function highlightRowForEvent(e) {
		dataTable.unselectAllRows();
		var row = getRowFromEvent(e);
		dataTable.selectRow(row);
	}

	function getRowFromEvent(e) {
		var targ = YAHOO.util.Event.getTarget(e);
		return dataTable.getTrEl(targ);
	}

	function showHistoryForLink(e, oid) {
		var newwindow = window.open(YAHOO.cuanto.urls.get('testCaseHistory') + oid, 'name',
			'height=400,width=900, status=1, toolbar=1, resizable=1, scrollbars=1, menubar=1, location=1');
		if (window.focus) {
			newwindow.focus();
		}
		return false;
	}


	function updateTable(e, data) {
		var updatedData = data[0];
		if (updatedData.updatedRecords.length > 0) {
			var recordsToUpdate = $.grep(dataTable.getRecordSet().getRecords(), function(record, idx) {
				var found = $.grep(updatedData.updatedRecords, function(id) {
					return record.getData('id') == id;
				});
				return found[0];
			});

			$.each(recordsToUpdate, function(idx, record) {
				if (updatedData.fields.hasOwnProperty("testResult")) {
					record.setData('result', updatedData.fields.testResult.name);
				}

				if (updatedData.fields.hasOwnProperty("analysisState")) {
					record.setData('analysisState', updatedData.fields.analysisState);
				}

				if (updatedData.fields.hasOwnProperty("bug")) {
					if (updatedData.fields.bug == null) {
						record.setData('bug', {title: "", url: ""});
					} else {
						var bugData = {title: updatedData.fields.bug.title,  url: updatedData.fields.bug.url };
						record.setData('bug', bugData);
					}
				}

				if (updatedData.fields.hasOwnProperty("owner")) {
					record.setData('owner', updatedData.fields.owner);
				}

				if (updatedData.fields.hasOwnProperty("note")) {
					var note = updatedData.fields.note;
					if (note == null) {
						note = "";
					}
					record.setData('note', updatedData.fields.note);
				}
				dataTable.updateRow(record, record.getData());
			});
		}
	}


	function selectRecord(e) {
		var target = YAHOO.util.Event.getTarget(e);
		if (target.checked) {
			addRecord(target.id);
		} else {
			removeRecord(target.id);
		}
	}

	function addRecord(recordId) {
		showSelectColumn();
		if (targetOutcomes.indexOf(recordId) == -1) {
			targetOutcomes.push(recordId);
			updateSelectedTotal();
		}
	}

	function removeRecord(recordId) {
		//targetOutcomes = targetOutcomes.without(recordId);
		targetOutcomes = $.grep(targetOutcomes, function(item) {
			return item != recordId;
		});
		updateSelectedTotal();
	}

	function showSelectColumn(e) {
		var selCol = dataTable.getColumn(0);
		if (selCol.hidden) {
			dataTable.showColumn(selCol);
			$('#showSelectCol').hide();
			$('#showSelectOptions').show();
		}
		if (e) {
			YAHOO.util.Event.preventDefault(e);
		}
	}


	function hideSelectColumn(e) {
		var selCol = dataTable.getColumn(0);
		if (!selCol.hidden) {
			dataTable.hideColumn(selCol);
			$('#showSelectOptions').hide();
			$('#showSelectCol').show();
		}
		if (e) {
			YAHOO.util.Event.preventDefault(e);
		}
	}


	function selectPageRecords(e) {
		$.each($('.batch'), function(idx, box) {
			box.checked = true;
			if (targetOutcomes.indexOf(box.id) == -1) {
				targetOutcomes.push(box.id);
			}
		});
		updateSelectedTotal();
		if (e) {
			YAHOO.util.Event.preventDefault(e);
		}
	}

	function deselectPageRecords(e) {
		$.each($('.batch'), function(idx, box) {
			box.checked = false;
			targetOutcomes = targetOutcomes.without(box.id);
		});
		updateSelectedTotal();
		if (e) {
			YAHOO.util.Event.preventDefault(e);
		}
	}


	function deselectAllRecords(e) {
		targetOutcomes.length = 0;
		$.each($('.batch'), function(idx, box) {
			box.checked = false;
		});
		updateSelectedTotal();
		if (e) {
			YAHOO.util.Event.preventDefault(e);
		}
	}


	function updateSelectedTotal() {
		$('#currentSelected').html(targetOutcomes.length + " Selected");
	}


	function applyAnalysis(e, args) {
		var sourceOutcome = args[0].sourceOutcome;
		var postBdy = "src=" + sourceOutcome;
		var fieldStr = "";

		if (args[0].fields) {
			$.each(args[0].fields, function(idx, field) {
				fieldStr += "&field=" + field;
			});
			postBdy += fieldStr;
		}

		var idRegEx = /\d+/;
		$.each(targetOutcomes, function(idx, outc) {
			var oid = idRegEx.exec(outc);
			if (oid && parseInt(oid[0]) != sourceOutcome) {
				postBdy += "&target=" + oid;
			}
		});


		var callback = {
			success: function(o) {
				var updatedData = YAHOO.lang.JSON.parse(o.responseText);
				YAHOO.cuanto.events.recordsUpdatedEvent.fire(updatedData);
				deselectAllRecords();
				YAHOO.cuanto.events.analysisAppliedEvent.fire();
			},
			failure: function() {
				alert("failed applying analysis");
				YAHOO.cuanto.events.analysisAppliedEvent.fire();
			}
		};

		YAHOO.util.Connect.asyncRequest('POST', YAHOO.cuanto.urls.get('applyAnalysis'), callback, postBdy);
		YAHOO.util.Event.preventDefault(e);
	}


	function hideCurrentSearchSpan() {
		$('#currentSearchSpan').hide();
	}


	function showCurrentSearchSpan() {
		if (searchQueryIsSpecified()) {
			$('#currentSearchSpan').show();
			$('#currentSearch').html("By " + $("#searchTerm").val() + ":  " + $("#searchQry").val());
		} else {
			hideCurrentSearchSpan();
		}
	}


	function onTcFormatChange(e) {
		YAHOO.util.Cookie.setSub(analysisCookieName, prefTcFormat, getCurrentTcFormat(), {path: "/", expires: new Date().getDate() + 30});
		setTcFormatPref();
		onTableStateChange(e);
	}

	function setTcFormatPref() {
		var tcFormat = getCurrentTcFormat();
		var expDate = new Date();
		expDate.setDate(expDate.getDate() + 30);
		YAHOO.util.Cookie.setSub(analysisCookieName, prefTcFormat, tcFormat, {path: "/", expires: expDate});
		return tcFormat;
	}


	function onTableStateChange(e) {
		var newRequest = generateNewRequest();
		showCurrentSearchSpan();
		dataTable.getDataSource().sendRequest(newRequest, {
			success : function(oRequest, oResponse, oPayload) {
				var origSort = dataTable.get('sortedBy');  // keep the sort indicator
				dataTable.onDataReturnInitializeTable(oRequest, oResponse, oPayload);
				dataTable.set('sortedBy', origSort);
			},
			failure : function() {
				alert("Failed loading table data");
			},
			argument:{}
		});

		YAHOO.util.Event.preventDefault(e);
		return false;
	}


	function onEditNote(e) {
		var editField = $("<input id='editNoteField' type='text' size='100'/>");
		editField.val($('#trhNote').html());
		$('#editNote').hide();
		$('#noteOps').show();
		$('#trhNote').hide();
		$('#noteContainer')[0].appendChild(editField[0]);
		var kl = new YAHOO.util.KeyListener(editField[0], {keys: 13}, onSaveNote);
		kl.enable();
		editField.focus().select();
		YAHOO.util.Event.preventDefault(e);
		return false;
	}

	//todo: still needed?
	function onTestRunChanged(e, args) {
		var testRun = args[0];
		if (testRun["testProperties"]) {
			var out = "";
			$.each(testRun["testProperties"], function(indx, item) {
				out += item["name"] + ": " + item["value"];
				if (indx < oData.length - 1) {
					out += ", ";
				}
			});
			$('#trhTestProperties').innerHTML = out;
		}
		if (testRun["note"]) {
			$('#trhNote').innerHTML = testRun["note"];
		}
		if (testRun["valid"]) {
			$('#trhIsInvalid').hide();
		} else {
			$('#trhIsInvalid').show();
		}
	}


	function clearEditNoteField(){
		var editLink = $('#editNoteField');
        if (editLink) {
	        editLink.remove();
	        $('#noteOps').hide();
	        $('#editNote').show();
	        $('#trhNote').show();
        }
	}


	function onCancelNote(e) {
		clearEditNoteField();
		YAHOO.util.Event.preventDefault(e);
	}


	function onSaveNote(e) {
		var editLink = $('#editNoteField');
		var note = editLink.val();
		$('#trhNote').html(note);
		clearEditNoteField();

		$.ajax({
			url: YAHOO.cuanto.urls.get('testRunUpdateNote'),
			data: {'id': $('#testRunId').val(), 'note': note, 'format': 'json'},
			dataType: "json",
			error: function(req, textStatus, errorThrown) {
				var msg;
				if (errorThrown) {
					msg = errorThrown;
				} else {
					msg = "Unknown error while updating note";
				}
				showFlashMsg(msg);
			}
		});

		YAHOO.util.Event.preventDefault(e);
		return false;
	}


	function showFlashMsg(msg) {
		$('#flashMsg').html(msg);
		$('#flashMsg').show();
	}

	function cacheOutput() {
		clearTimeout(cacheOutputTimer);
		cacheOutputTimer = setTimeout(function() {
			outputPanel.prefetchNextOutputs();
		}, 5000);
	}

	function chooseColumns(e) {
		YAHOO.util.Event.preventDefault(e);
		var columnDialog = getColumnDialog();
		columnDialog.show();
	}

	function getColumnDialog() {
		if (!columnDialog)
		{
			columnDialog = new YAHOO.cuanto.ColumnDialog(dataTable, ovrlyMgr, prefHiddenColumns);
		}
		return columnDialog;
	}

	function deleteTestRun(e) {
		YAHOO.util.Event.preventDefault(e);
		var delDialog = new YAHOO.widget.SimpleDialog("trDeleteDialog", {
			width: "20em",
			fixedcenter:true,
			modal:true,
			visible:false,
			draggable:false,
			zIndex:900});
		delDialog.setHeader("Warning!");
		delDialog.setBody("Are you sure you want to permanently delete this test run?");
		delDialog.cfg.setProperty("icon", YAHOO.widget.SimpleDialog.ICON_WARN);
		var handleYes = function() {
			$.each(delDialog.getButtons(), function(idx, b){
				b.destroy();
			});
			delDialog.setBody("Deleting...");
			$.ajax({
				url: YAHOO.cuanto.urls.get('testRunDelete'),
				type: "POST",
				data: {'id': $('#testRunId').val()},
				dataType: "json",
				success: function(response, textStatus, req) {
					if (response.error) {
						delDialog.setBody("Failed deleting: " + response.error);
					} else {
						delDialog.setBody("Deleted this test run!<br/>Redirecting to test run history...");
						setTimeout(function(){
							window.location = YAHOO.cuanto.urls.get('projectHistory');
						}, 3000);
					}					
				},
				error: function(req, status, error) {
					alert("Error: " + req.statusText);
					delDialog.hide();
				}
			});
		};
		var handleNo = function() {
			this.hide();
		};
		var myButtons = [
			{ text:"Cancel",
				handler:handleNo,
				isDefault:true },
			{ text:"Delete",
				handler:handleYes } ];
		delDialog.cfg.queueProperty("buttons", myButtons);
		delDialog.render(document.body);
		delDialog.show();
	}

	function recalcStats(e) {
		YAHOO.util.Event.preventDefault(e);
		$.ajax({
			url: YAHOO.cuanto.urls.get('testRunRecalcStats'),
			data: {'id': $('#testRunId').val()},
			success: function(o) {
				showFlashMsg("Queued for recalculating statistics.")
			}
		});
	}

	function unescapeHtmlEntities(s) {
		return s.replace(/&lt;/g, '<').replace(/&gt;/g, '>');
	}

	function getHiddenColumns() {
		var dialog = getColumnDialog();
		var hiddenCols = dialog.getHiddenColumns();
		if (hiddenCols) {
			return hiddenCols;
		} else {
			var cols = {};
			$.each(["startedAt", "finishedAt", "output", "parameters"],
				function(idx, item) {
				cols[item] = true;
			});
			return cols;
		}
	}

	function formatDuration(elCell, oRecord, oColumn, oData) {
		$(elCell).html(timeParser.formatMs(oRecord.getData("duration")));
	}

	function propertyFormatter(elCell, oRecord, oColumn, oData) {
		var out = "";
		var propName = oColumn.label.toLowerCase();
		if (oRecord) {
			var props = oRecord.getData("testProperties");
			for (var prop in props) {
				if (prop.toLowerCase() == propName) {
					out = props[prop];
				}
			}
			$(elCell).html(out);
		}
	}

	function linkFormatter(elCell, oRecord, oColumn, oData) {
		var links = oRecord.getData("links");

		var linkDesc = [];
		var descMap = {};
		for (var d in links) {
			linkDesc.push(links[d]);
			descMap[links[d]] = d;
		}
		var sortedDesc = linkDesc.sort();
		var output = "";

		for (var i = 0; i < sortedDesc.length; i++) {
			var desc = sortedDesc[i];
			output += "<a href='" + descMap[desc] + "'>" + desc + "</a>";
			if (i != sortedDesc.length - 1) {
				output += "<br/>";
			}
		}
		
		$(elCell).html(output);
	}


    function initTagButtons() {
        $.each($('.tagspan'), function(idx, tagspan) {
            tagButtons.push(new YAHOO.widget.Button(tagspan.id, {type: "checkbox", checked: false, onclick: { fn: onTagClick } }));
        });
    }

    function onTagClick(e) {
        if (this == tagButtons[0]) {
            // if All Tags, select all tag buttons except Untagged
            var numOn = 0;
            tagButtons[1].set("checked", false);
            for (var i = 2; i < tagButtons.length; i++) {
                if (tagButtons[i].get("checked")) {
                    numOn++;
                }
            }
            for (var j = 2; j < tagButtons.length; j++) {
                var chkstate = (numOn != tagButtons.length - 2);
                tagButtons[j].set("checked", chkstate);
            }
        } else if (this == tagButtons[1]) {
            // if Untagged, deselect all buttons except Untagged
            tagButtons[0].set("checked", false);
            for (var k = 2; k < tagButtons.length; k++) {
                tagButtons[k].set("checked", false);
            }

        } else {
            var numOn = 0;
            for (var i = 2; i < tagButtons.length; i++) {
                if (tagButtons[i].get("checked")) {
                    numOn++;
                }
            }
            tagButtons[0].set("checked", numOn == tagButtons.length - 2);
            tagButtons[1].set("checked", false);
        }

        onFilterChange(e);
    }

	function onSearchTermChange(e) {
		var searchTerm = $('#searchTerm');
		if ($('#searchTerm').val() == "Properties") {
			$.each($('.propSearch'), function(idx, item) {
				$(item).show();
			});
		} else {
			$.each($('.propSearch'), function(idx, item) {
				$(item).hide();
			});
		}
	}
};

