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
		while (getWidthForColumnText(wkStr + tokens[i]) < max)
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
		if (!$('columntester')) {
			var elem = createElem("columntester");
			elem.id = "columntester";
			document.body.appendChild(elem);
			$('columntester').hide();
		}
		$('columntester').innerHTML = text;
		return $('columntester').getWidth();
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

	pub.formatBug = function(elCell, oRecord, oColumn, oData)
	{
		var title = oData.title;
		var url = oData.url;
		if (title != null && url != null && url != "")
		{
			var cnt = new Element('div');
			var titleSpan = new Element('span');
			titleSpan.innerHTML = title + " ";
			cnt.appendChild(titleSpan);
			var link = new Element('a', {'href': url});
			var img = new Element('img', {'src': YAHOO.cuanto.urls.get("shortcutImg"), width:13, height:13});
			link.appendChild(img);
			cnt.appendChild(link);
			elCell.innerHTML = "";
			elCell.appendChild(cnt);
			YAHOO.util.Event.addListener(link, "click", showBugInNewWindow);
		}
		else if (title != null)
		{
			elCell.innerHTML = title;
		}
		else if (url != null && url != "")
		{
			var cnt = new Element('div');
			var titleSpan = new Element('span');
			titleSpan.innerHTML = title + " ";
			cnt.appendChild(titleSpan);
			var link = new Element('a', {'href': url});
			var img = new Element('img', {'src': YAHOO.cuanto.urls.get("shortcutImg"), width:13, height:13});
			link.appendChild(img);
			cnt.appendChild(link);
			elCell.innerHTML = "";
			elCell.appendChild(cnt);
			YAHOO.util.Event.addListener(link, "click", pub.showBugInNewWindow);
		}
		else
		{
			elCell.innerHTML = "";
		}
	};

	pub.formatNote = function(elCell, oRecord, oColumn, oData)
    {
	    if (!oData || oData.length <= NOTE_SUMMARIZATION_THRESHOLD)
	    {
		    elCell.innerHTML = oData;
		    return;
	    }

        var noteContainer = new Element('span');
        noteContainer.innerHTML = oData.truncate(NOTE_SUMMARIZATION_THRESHOLD - MORE.length);
        var truncationToggler = new Element('a');
        truncationToggler.className = 'truncationToggler';
        truncationToggler.innerHTML = MORE;
        truncationToggler.isTruncated = true;
        elCell.innerHTML = '';
        elCell.appendChild(noteContainer);
        elCell.appendChild(truncationToggler);

	    YAHOO.util.Event.addListener(truncationToggler, 'click', function(e) {
	        pub.toggleSummary(e, truncationToggler, noteContainer, oData);
		    return false;
        });
    };

	pub.toggleSummary = function(e, truncationToggler, noteContainer, noteFieldValue) {
		YAHOO.util.Event.preventDefault(e);
		if (truncationToggler.isTruncated) {
			truncationToggler.innerHTML = LESS;
			noteContainer.innerHTML = noteFieldValue;
		}
		else {
			truncationToggler.innerHTML = MORE;
			noteContainer.innerHTML = noteFieldValue.truncate(NOTE_SUMMARIZATION_THRESHOLD - MORE.length);
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