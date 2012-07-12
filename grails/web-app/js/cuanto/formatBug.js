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

YAHOO.cuanto.format = function () {

	// number of characters to allow in note field before summarizing
	var NOTE_SUMMARIZATION_THRESHOLD = 50;
	var MORE = ' [more]';
	var LESS = ' [less]';

	var pixelsToCharLength = {};

	function getWidthForColumnText(text) {
		if ($("#columntester").length == 0) {
			var coltester = $("<div id='columntester'/>");
			document.body.appendChild(coltester[0]);
			$('#columntester').hide();
		}
		$('#columntester').html(text);
		return $('#columntester').width();
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

	function getNumCharactersForPixelLength(maxPixels) {
		var maxLength = pixelsToCharLength[maxPixels];
		if (!maxLength) {
			// close-enough algorithm for finding the number of characters in a string for the given pixel length
			var testString = "";
			var testStringPixels = 0;
			while (testStringPixels < maxPixels) {
				testString += 'M';
				testStringPixels = getWidthForColumnText(testString);
			}
			pixelsToCharLength[maxPixels] = testString.length;
			maxLength = testString.length;
		}
		return maxLength;
	}

	function findLastCommonTokenIndex(brokenString, maxLength) {
		var lastCommonToken = -1;
		for (var i = 0; i < brokenString.length && i < maxLength; ++i) {
			var c = brokenString[i];
			if (c == '.' || c == ' ' || c == ',' || c == ':')
				lastCommonToken = i;
		}
		return lastCommonToken;
	}

	var pub = {}; // public methods

	pub.showBugInNewWindow = function (e) {
		var newwindow = window.open(this.href, 'name', 'height=400,width=900, status=1, toolbar=1, resizable=1, scrollbars=1, menubar=1, location=1');
		if (window.focus) {
			newwindow.focus();
		}
		YAHOO.util.Event.preventDefault(e);
		return false;
	};

	pub.formatBug = function (elCell, oRecord, oColumn, oData) {
		if (oData) {
			var title = oData.title;
			var url = oData.url;
			if (title != null && url != null && url != "") {
				var cnt = $("<div></div>");
				cnt.html("<span>" + title + " </span>");
				var link = $("<a href='" + url + "'><img src='" + YAHOO.cuanto.urls.get("shortcutImg") +
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
				img.css({width:"13px", height:"13px"});
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

	pub.formatNote = function (elCell, oRecord, oColumn, oData) {
		if (!oData || oData.length <= NOTE_SUMMARIZATION_THRESHOLD) {
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

		YAHOO.util.Event.addListener(truncationToggler, 'click', function (e) {
			pub.toggleSummary(e, truncationToggler, noteContainer, oData);
			return false;
		});
	};

	pub.formatOutput = function (elCell, oRecord, oColumn, oData) {
		if (!oData) {
			$(elCell).html(oData);
			return;
		}
		var outputContainer = $("<span/>");

		var removed = oData.replace(/[\n|\r\n]/g, " ");
		var broken = pub.breakOnToken(removed, " ", 600);
		outputContainer.html(broken);
		$(elCell).html("");
		elCell.appendChild(outputContainer[0]);
	};

	pub.toggleSummary = function (e, truncationToggler, noteContainer, noteFieldValue) {
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

	pub.breakOnToken = function (str, token, suggestedWidthInPx) {
		var maxLength = getNumCharactersForPixelLength(suggestedWidthInPx);
		var tokens = str.split(token);
		var lines = new Array();

		var tokenIndex = 0;
		var brokenStrings = new Array();
		var brokenStrLength = 0;
		while (tokenIndex < tokens.length) {
			if (brokenStrLength < maxLength) {
				var s = tokens[tokenIndex++];
				brokenStrings.push(s);
				brokenStrLength += s.length;
			}
			else {
				var brokenString = brokenStrings.join(token);
				if (brokenString.length > maxLength) {
					var lastCommonTokenIndex = findLastCommonTokenIndex(brokenString, maxLength);
					if (lastCommonTokenIndex > -1) {
						lines.push(brokenString.substring(0, lastCommonTokenIndex + 1));
						var remainingString = brokenString.substring(lastCommonTokenIndex + 1);
						brokenStrings = [remainingString];
						brokenStrLength = remainingString.length;
					}
					else {
						lines.push(brokenString);
						brokenStrings = new Array();
						brokenStrLength = 0;
					}
				}
				else {
					lines.push(brokenString);
					brokenStrings = new Array();
					brokenStrLength = 0;
				}
			}
		}

		var brokenString = brokenStrings.join(token);
		while (brokenString.length > maxLength) {
			var lastCommonTokenIndex = findLastCommonTokenIndex(brokenString, maxLength);
			if (lastCommonTokenIndex > -1) {
				lines.push(brokenString.substring(0, lastCommonTokenIndex + 1));
				brokenString = brokenString.substring(lastCommonTokenIndex + 1);
			} else {
				// force line break, since no good token to break on was found
				lines.push(brokenString.substring(0, maxLength));
				brokenString = brokenString.substring(maxLength);
			}

		}
		if (brokenString.length > 0) {
			lines.push(brokenString);
		}

		return lines.join("<br />");
	};

	return pub;
}();