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

YAHOO.cuanto.format = function() {

	// number of characters to allow in note field before summarizing
	var NOTE_SUMMARIZATION_THRESHOLD = 50;
	var MORE = ' [more]';
	var LESS = ' [less]';

	function getSplitPoint(str, max, token) {
		var tokens = str.split(token);
		var wkStr = new String();
		var i = 0;
		var splitPoint = -1;
		while (getWidthForColumnText(wkStr + tokens[i]) < max && i < tokens.length)
		{
			wkStr += tokens[i];
			if (i < tokens.length - 1) {
				wkStr += token;
			}
			splitPoint = wkStr.length;
			i++;
		}
		if (splitPoint < 0) {
			var j = 1;
			while (getWidthForColumnText(str.substr(0, j)) < max) {
				j++;
			}
			splitPoint = j;
		}
		return splitPoint;
	}


	function getWidthForColumnText(text) {
		if (!$('#columntester').length > 0) {
			var elem = createElem("columntester");
			elem.id = "columntester";
			document.body.appendChild(elem);
			$('#columntester').hide();
		}
		$('#columntester').html(text);
		//return $('#columntester').css("width");
		return $('#columntester').width();
	}

	function createElem(elem) {
		return document.createElementNS ?
		       document.createElementNS('http://www.w3.org/1999/xhtml', elem) :
		       document.createElement(elem);
	}

	function showBugInNewWindow(e) {
		var newwindow = window.open(this.href, 'name',
			'height=400,width=900, status=1, toolbar=1, resizable=1, scrollbars=1, menubar=1, location=1');
		if (window.focus) {
			newwindow.focus();
		}
		YAHOO.util.Event.preventDefault(e);
		return false;
	}

	var pub = {}; // public methods

	pub.showBugInNewWindow = function(e)
	{
		var newwindow = window.open(this.href, 'name', 'height=400,width=900, status=1, toolbar=1, resizable=1, scrollbars=1, menubar=1, location=1');
		if (window.focus)
		{
			newwindow.focus();
		}
		YAHOO.util.Event.preventDefault(e);
		return false;
	};

	pub.formatBug = function(elCell, oRecord, oColumn, oData) {
		if (oData) {
			var title = oData.title;
			var url = oData.url;
			if (title != null && url != null && url != "") {
				var cnt = $("<div></div>");
				cnt.html("<span>" + title + " </span>");
				
				var link = $("<a href=''" + url + "'<img src='" + YAHOO.cuanto.urls.get("shortcutImg") +
					"' style='width:13px; height:13px;'/></a>");
				cnt.append(link);
				$(elCell).html("");
				$(elCell).append(cnt);
				YAHOO.util.Event.addListener(link, "click", showBugInNewWindow);
			}
			else if (title != null) {
				$(elCell).html(title);
			}
			else if (url != null && url != "") {
				var cnt = $("<div></div>");
				var titleSpan = $("<span/>");
				titleSpan.html(title + " ");
				cnt.appendChild(titleSpan);
				var link = $("<a href='" + url + "'></a>");
				var img = $("<img src='" + YAHOO.cuanto.urls.get("shortcutImg") + "'/>");
				img.css({width: "13px", height: "13px"});
				link.appendChild(img);
				cnt.appendChild(link);
				$(elCell).html("");
				elCell.appendChild(cnt);
				YAHOO.util.Event.addListener(link, "click", pub.showBugInNewWindow);
			}
			else {
				$(elCell).html("");
			}
		} else {
			$(elCell).html("");
		}
	};

	pub.formatNote = function(elCell, oRecord, oColumn, oData)
    {
	    if (!oData || oData.length <= NOTE_SUMMARIZATION_THRESHOLD)
	    {
		    $(elCell).html(oData);
		    return;
	    }

        var noteContainer = $("<span/>");
        noteContainer.html(oData.substr(0, NOTE_SUMMARIZATION_THRESHOLD - MORE.length));
        var truncationToggler = $("<a></a>");
        truncationToggler.addClass("truncationToggler");
        truncationToggler.html(MORE);
        truncationToggler.isTruncated = true;
        $(elCell).html('');
        elCell.appendChild(noteContainer[0]);
        elCell.appendChild(truncationToggler[0]);

	    YAHOO.util.Event.addListener(truncationToggler, 'click', function(e) {
	        pub.toggleSummary(e, truncationToggler, noteContainer, oData);
		    return false;
        });
    };
	
	pub.formatOutput = function(elCell, oRecord, oColumn, oData)
	{
		if (!oData)
		{
			$(elCell).html(oData);
			return;
		}
		var outputContainer = $("<span/>");

		var removed = oData.replace(/[\n|\r\n]/g, " ");
		var broken = pub.breakOnToken(removed, " ", 400);
		outputContainer.html(broken.replace(/[\n|\r\n]/g, "<br/>"));
		$(elCell).html("");
		elCell.appendChild(outputContainer[0]);
	};

	pub.toggleSummary = function(e, truncationToggler, noteContainer, noteFieldValue) {
		YAHOO.util.Event.preventDefault(e);
		if (truncationToggler.isTruncated) {
			$(truncationToggler).html(LESS);
			$(noteContainer).html(noteFieldValue);
		}
		else {
			$(truncationToggler).html(MORE);
			$(noteContainer).html(noteFieldValue.substr(0, NOTE_SUMMARIZATION_THRESHOLD - MORE.length));
		}
		truncationToggler.isTruncated = !truncationToggler.isTruncated;
		return false;
	};

	pub.breakOnToken = function(str, token, maxLinePxls)
	{
		var dispStrs = new Array();
		var splitPoint;
		do {
			splitPoint = getSplitPoint(str, maxLinePxls, token);
			if (splitPoint > 0)
			{
				dispStrs.push(str.slice(0, splitPoint));
			}
			else
			{
				splitPoint = getSplitPoint(str, maxLinePxls, token, "");
			}
			str = str.substr(splitPoint);

		}
		while (getWidthForColumnText(str) > maxLinePxls && splitPoint > 0);

		dispStrs.push(str);

		var displayStr = new String();
		for (var x = 0; x < dispStrs.length; x++)
		{
			displayStr += dispStrs[x];
			if (x < dispStrs.length - 1)
			{
				displayStr += "<br/>";
			}
		}
		return displayStr;
	};

	return pub;
}();