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

YAHOO.cuanto.analysisDialog = function(overlayManager, outputProxy) {
	var panel;
	var anButton;
	var analyses;

	var PANEL_X_POSITION = 150;
	var PANEL_Y_POSITION = 100;

	if (!outputProxy) {
		outputProxy = new YAHOO.cuanto.OutputProxy();
	}

	YAHOO.cuanto.events.analysisAppliedEvent.subscribe(function() {
		stopProgressIcon();
	});

	YAHOO.cuanto.events.recordsUpdatedEvent.subscribe(updateHistory);

	return {    // public method
		showAnalysisDialog: function (e) {
			var outcomeId = getOutcomeIdFromEvent(e);

			var chkBoxId = 'chk' + outcomeId;
			var chkBox = $(chkBoxId);
			if (!chkBox.checked) { 
				chkBox.checked = true;
				YAHOO.cuanto.events.recordSelectedEvent.fire(chkBoxId);
			}

			var anlzPanel = getAnlzPanel();
			anlzPanel.cfg.setProperty('xy', getDesiredAnlzPanelXY());
			anlzPanel.render();

			var escapeKey = new YAHOO.util.KeyListener($('anPanel'), { keys:27 }, { fn:closeAnlzPanel});
			escapeKey.enable();
			anlzPanel.cfg.queueProperty('keylisteners', escapeKey);

			$('anPanel').removeClassName("hidden");
			$('anPanel').setStyle({visibility: 'visible'});
			$('anContainer').show();
			anlzPanel.show();

			var closeElm = $$("#anPanel > .container-close");
			YAHOO.util.Event.removeListener(closeElm, "click", closeAnlzPanel);
			YAHOO.util.Event.addListener(closeElm, "click", closeAnlzPanel);
			fetchAnalysisHistory(outcomeId);
			YAHOO.util.Event.preventDefault(e);
		}
	};


	function fetchAnalysisHistory(outcomeId) {
		var callback = {
			cache: false,
			success: function(o) {
				analyses = YAHOO.lang.JSON.parse(o.responseText);
				showAnalysis(0);
				populateAnalysisHistory();
			},
			failure: function(o) {
				getAnlzPanel().setBody("Failed retrieving analysis of test case " + outcomeId + "<p><br/>" + o.responseText);
			}
		};
		var url = YAHOO.cuanto.urls.get('analysis') + outcomeId;
		YAHOO.util.Connect.asyncRequest('GET', url, callback);
	}

	function populateAnalysisHistory() {
		var historyElem = $('anlzHistory');
		historyElem.childElements().each(function(child) {
			child.remove();
		});
		var outcomes = analyses['outcomes'];

		YAHOO.util.Event.addListener(historyElem, "change", function(e) {
			analyses.offset = this.options[this.selectedIndex].getAttribute('value');
			showAnalysis(analyses.offset);
			YAHOO.util.Event.preventDefault(e);
		});

		for (var i = 0; i < outcomes.length; i++) {
			var option = getOptionForOutcome(outcomes[i], i);
			historyElem.appendChild(option);
		}

		var maxSize = 9;
		historyElem.size = outcomes.length < maxSize ? outcomes.length : maxSize;
	}


	function updateHistory(e, updated) {
		var displayedOutcome =  analyses['outcomes'][0]['id'];
		if (updated[0]['updatedRecords'].any(function(outcomeId){
			return outcomeId == displayedOutcome;
		})) {
			fetchAnalysisHistory(displayedOutcome);
		}
	}


	function getOptionForOutcome(outcome, val) {
		// val is the value to assign the option element
		var option = new Element('option', {value: val});
		var text = outcome['testRun'] + ": ";
		text += getStateText(outcome) + " ";

		var note = getShortNote(outcome, text.length);
		if (!note.blank()) {
			text += "- <i>" + note + "</i>";
		}
		option.innerHTML = text;
		return option;
	}


	function getStateText(outcome) {
		var text = outcome['analysisState'];
		if (outcome['bug']) {
			if (outcome['bug']['title']) {
				text += " (" + outcome['bug']['title'] + ") ";
			} else if (outcome['bug']['url']) {
				text += " (" + outcome['bug']['url'] + ") ";
			}
		}
		return text;
	}

	function getShortNote(outcome, currentLength) {
		var maxLineLength = 65;
		var maxNoteLength = maxLineLength - currentLength;
		var shortNote = "";
		var note = outcome['note'];
		if (note && note.strip().length > 0) {
			shortNote = note.truncate(maxNoteLength);
		}
		return shortNote;
	}

	function getOutcomeIdFromEvent(e) {
		var targ = YAHOO.util.Event.getTarget(e);
		var idToGrep;
		if (targ.id) {
			idToGrep = targ.id;
		} else {
			if (Prototype.Browser.IE) {
				idToGrep = targ.parentElement.id;
			} else {
				idToGrep = targ.up('.anlzLink').id;
			}
		}
		return idToGrep.match(/.+?(\d+)/)[1];
	}

	function getDesiredAnlzPanelXY() {
		var vp = document.viewport.getScrollOffsets();
		var pX = vp[0] + PANEL_X_POSITION;
		var pY = vp[1] + PANEL_Y_POSITION;
		return [pX, pY];
	}

	function showAnalysis(offset) {
		var analysis = analyses['outcomes'][offset];
		$('testCase').innerHTML = analyses['testCase']['shortName'];
		for (var field in analysis) {
			if (field == "bug") {
				$('bug').setAttribute('href', analysis['bug'].url);
				if (!analysis['bug'].title) {
					analysis['bug'].title = analysis['bug'].url;
				}
				$('bug').innerHTML = analysis['bug'].title ? analysis['bug'].title : "";
				YAHOO.util.Event.addListener('bug', "click", YAHOO.cuanto.format.showBugInNewWindow);
			} else if (field == "id") {
				// do nothing
			} else {
				$(field).innerHTML = analysis[field] ? analysis[field] : "";
			}
		}

		$('anOutputDiv').innerHTML = "Loading... " + getProgressImg();

		var processOutput = function(outputItem) {
			populateOutputField(outputItem.output);
			primeAnalysesOutputCache(outputItem);
		};

		outputProxy.getOutputForId(analysis.id, processOutput);
		$('anApply').focus();
	}


	function primeAnalysesOutputCache(currentItem) {
		var NUM_TO_PREFETCH = 2;
		var allOutputIds = analyses['outcomes'].collect(function(anlys) {
			return anlys.id;
		});

		var indexOfCurrentOutput = allOutputIds.indexOf(currentItem.id);
		var firstIndx = indexOfCurrentOutput == -1 ? 0 : indexOfCurrentOutput + 1;
		var prefetchIds = allOutputIds.slice(firstIndx, firstIndx + NUM_TO_PREFETCH);
		outputProxy.prefetchOutputs(prefetchIds);
	}


	function populateOutputField(outputText) {
		$('anOutputDiv').innerHTML = outputText.truncate(1000);
	}


	function getAnlzPanel() {
		if (!panel) {
			panel = new YAHOO.widget.Panel("anPanel", {dragOnly: true,
				width: "850px",
				visible: true, xy: getDesiredAnlzPanelXY(), zIndex: 50, iframe: false, underlay: "none"});
			initDialogButtons();
			panel.render();
			overlayManager.register(panel);
			YAHOO.cuanto.events.outcomeChangeEvent.subscribe(function(e, args) {
				var changedId = args[0];
				if (changedId == analyses['outcomes'][0]['id']) {
					fetchAnalysisHistory(changedId);
				}
			});

		}
		return panel;
	}


	function initDialogButtons() {
		anButton = new YAHOO.widget.Button('anApply');
		anButton.addListener("click", applyAnalysis);
	}

	
	function applyAnalysis(e) {
		var sourceOutcome = analyses['outcomes'][analyses.offset].id;
		var boxes = $('anData').getInputs('checkbox');
		var fields = new Array();
		boxes.each(function(chkbox) {
			if (chkbox.checked) {
				fields.push(chkbox.readAttribute('value'));
			}
		});

		startProgressIcon();
		YAHOO.cuanto.events.bulkAnalysisEvent.fire({'sourceOutcome': sourceOutcome, 'fields': fields});
		$('anContainer').hide();
		getAnlzPanel.hide();
		YAHOO.util.Event.preventDefault(e);
	}


	function startProgressIcon() {
		$('applyStatus').innerHTML = " " + getProgressImg();
	}


	function stopProgressIcon() {
		$('applyStatus').innerHTML = "";
	}


	function getProgressImg() {
		var img = YAHOO.cuanto.urls.get('progressImg');
		return "<img src ='" + img + "'/>";
	}


	function closeAnlzPanel() {
		$('anPanel').setStyle({'visibility': 'hidden'});
		getAnlzPanel().hide();
		clearAnlzFields();
	}


	function clearAnlzFields() {
		var fields = ['testRun', 'currentRecord', 'totalRecords', 'testResult', 'analysisState', 'bug', 'owner', 'note',
			'anOutputDiv'];
		fields.each(function(item) {
			var field = $(item);
			if (field) {
				field.innerHTML = "";
			}
		});
	}


};
