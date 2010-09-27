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


YAHOO.cuanto.SummaryTab = function() {

	tabView.getTab(2).addListener('click',
		function() {
			refreshSummaryTab();
		},
		null, this);

	tabView.set('activeIndex', 0);

	function refreshSummaryTab() {
		$.each($(".progress"), function(idx, item) {
			item.html(" " + getProgressImg());
		});
		update(YAHOO.cuanto.urls.get('bugSummary') + "?rand=" + new Date().getTime(), $('#bugSummary'));
		update(YAHOO.cuanto.urls.get('failureChart') + "?rand=" + new Date().getTime(), $("#failureChart"));
		update(YAHOO.cuanto.urls.get('summaryTable') + "?rand=" + new Date().getTime(), $("#summaryTable"));
	}

	function update(url, elem) {
		$.get(url, null, function(data, status, request) {
			elem.html(data);
		}, "html");
	}

	function getProgressImg() {
		return "<img src ='" + YAHOO.cuanto.urls.get('progressImg') + "'/>";
	}
};
