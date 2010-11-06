/*
 Copyright (c) 2010 thePlatform, Inc.

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

YAHOO.cuanto.GroupedOutput = function() {
    var goTable;

    main();
    
    function main() {
        goTable = new YAHOO.widget.DataTable("trOutputTable", getColumnDefs(), getDataSource(), getTableConfig());
        goTable.handleDataReturnPayload = processPayload;
        goTable.set("selectionMode", "single");
        goTable.subscribe("rowClickEvent", handleRowClick);
        goTable.subscribe("rowMouseoverEvent", goTable.onEventHighlightRow);
        goTable.subscribe("rowMouseoutEvent", goTable.onEventUnhighlightRow);
        goTable.render();
	    $("#trOutputFilter").change(onFilterChange);
	    YAHOO.cuanto.events.outcomeFilterChangeEvent.subscribe(onFilterChangeEvent);
    }

    function getColumnDefs() {
        return [
            {key:"failures", label: "Failures", resizeable:false, width: 55, sortable: true},
            {key:"output", label: "Output Summary", resizeable: true, minWidth: 350, width: 800, sortable: true,
	            formatter: formatOutput}
        ];
    }
    
    function getDataSource() {
        var dataSource = new YAHOO.util.XHRDataSource(YAHOO.cuanto.urls.get('groupedOutput'));
        dataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
        dataSource.connXhrMode = "allowAll";
        dataSource.maxCacheEntries = 0;
        dataSource.responseSchema = {
            resultsList: 'groupedOutput',
            fields: ["failures", "output"],
            metaFields: {
                offset: "offset",
                totalCount: "totalCount"
            }
        };
        return dataSource;
    }

    function getTableConfig() {
        return {
            initialRequest: "offset=0&max=10&sort=failures&order=desc&cb=" + new Date().getTime(),
            renderLoopSize:10,
            generateRequest: buildGroupedOutputQuery,
            paginator: getDataTablePaginator(0, Number.MAX_VALUE, 0, 10),
            sortedBy: {key:"failures", dir:YAHOO.widget.DataTable.CLASS_DESC},
            dynamicData: true
        };
    }

    function buildGroupedOutputQuery(state){
	    if (!state) {
		    state = goTable.getState()
	    }
        var order = state.sortedBy.dir == YAHOO.widget.DataTable.CLASS_ASC ? "asc" : "desc";
        var qry = "offset=" + state.pagination.recordOffset +
                  "&max=" + state.pagination.rowsPerPage +
                  "&sort=" + state.sortedBy.key +
                  "&order=" + order +
	              "&filter=" + $('#trOutputFilter').val() +
                  "&cb=" + new Date().getTime(); // cache buster
        return qry;
    }


    function processPayload(oRequest, oResponse, oPayload) {
	    if (!oPayload) {
			    oPayload = {};
	    }

        oPayload.totalRecords = oResponse.meta.totalCount;
	    $("#grpOutputTotalRows").html(oPayload.totalRecords)
        return oPayload;
    }


    function handleRowClick(e) {
        this.onEventSelectRow(e);
        var record = this.getRecord(e.target);
        var output = record.getData("output");
        tabView.set('activeIndex', 0);
        YAHOO.cuanto.events.outcomeFilterChangeEvent.fire({search: "Output", qry: output,
	        results: $('#trOutputFilter').val(), context: "groupedOutput"});
    }


    function getDataTablePaginator(page, totalRecs, offset, rows) {
        var config = {
            containers         : ['trOutputPaging'],
            pageLinks          : 25,
            rowsPerPage        : rows,
            rowsPerPageOptions : [5,10,15,20,30,50,100],
            template       : "{FirstPageLink} {PreviousPageLink} {PageLinks} {NextPageLink} {LastPageLink}<br/>Show {RowsPerPageDropdown} per page"
        };
        config.initialPage = page;
        config.totalRecords = totalRecs;
        config.offset = offset;
        return new YAHOO.widget.Paginator(config);
    }


	function sendNewRequest() {
		var newRequest = buildGroupedOutputQuery(null);

		goTable.getDataSource().sendRequest(newRequest, {
			success : initDataTablePageOne,
			failure : function() {
				alert("Failed loading table data");
			}
		});
	}


	function onFilterChange(e) {
		sendNewRequest();
		YAHOO.cuanto.events.outcomeFilterChangeEvent.fire({results: $('#trOutputFilter').val(), context: 'groupedOutput'});
	}

	function onFilterChangeEvent(e, arg) {
		var filter = arg[0];
		if (filter.context != 'groupedOutput') {
			if (filter.results) {
				$('#trOutputFilter').val(filter.results);
			}
			sendNewRequest();
		}
	}

	function initDataTablePageOne(oRequest, oResponse, oPayload) {
		var origSort = goTable.get('sortedBy');  // keep the sort indicator
		goTable.onDataReturnInitializeTable(oRequest, oResponse, oPayload);
		goTable.set('sortedBy', origSort);
		goTable.get('paginator').setPage(1);
	}

	function formatOutput(elCell, oRecord, oColumn, oData) {
		var output = YAHOO.cuanto.format.breakOnToken(oData, ' ', 800) + " ";
		$(elCell).html(output);
	}
};