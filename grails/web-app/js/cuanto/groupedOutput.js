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
        goTable.set("selectionMode", "single");
        goTable.subscribe("rowClickEvent", handleRowClick);
        goTable.subscribe("rowMouseoverEvent", goTable.onEventHighlightRow);
        goTable.subscribe("rowMouseoutEvent", goTable.onEventUnhighlightRow);
        goTable.render();

    }

    function getColumnDefs() {
        return [
            {key:"failures", label: "Failures", resizeable:false, width: 40},
            {key:"output", label: "Output Summary", resizeable: true, minWidth: 350}    
        ];
    }
    
    function getDataSource() {
        var dataSource = new YAHOO.util.XHRDataSource(YAHOO.cuanto.urls.get('groupedOutput'));
        dataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
        dataSource.connXhrMode = "allowAll";
        dataSource.maxCacheEntries = 0;
        dataSource.responseSchema = {
            resultsList: 'groupedOutput',
            fields: ["failures", "output"]
          /*  metaFields: {
                offset: "offset",
                totalCount: "totalCount"
            }*/
        };
        return dataSource;

    }

    function getTableConfig() {
        var tableWidth;
        var minWidth = 1024;
        if (document.viewport.getWidth() > minWidth) {
            tableWidth = document.viewport.getWidth();
        } else {
            tableWidth = minWidth;
        }

        return {
            initialRequest: "offset=0&max=10",
            //width: tableWidth + "px",
            renderLoopSize:10,
            //generateRequest: buildOutcomeQueryString,
            //paginator: getDataTablePaginator(0, Number.MAX_VALUE, 0, 10),
            //sortedBy: {key:"testCase", dir:YAHOO.widget.DataTable.CLASS_ASC},
            dynamicData: true
        };
    }

    function handleRowClick(e) {
        this.onEventSelectRow(e);
        
    }
};