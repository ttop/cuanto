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

YAHOO.cuanto.OutputPanel = function(outputProxy) {

	var pub = {}; // public methods
	if (!outputProxy) {
		outputProxy = new YAHOO.cuanto.OutputProxy();
	}

	var NUM_TO_PREFETCH = 2;
	var PANEL_X_POSITION = 150;
	var PANEL_Y_POSITION = 100;
	var outputPanel;

	function closeOutPanel() {
		if (outputPanel) {
			outputPanel.setBody(""); //workaround for Firefox 2 rendering bug
			outputPanel.hide("");
		}
	}

	function getDesiredOutPanelXY() {
		var vp = document.viewport.getScrollOffsets();
		var pX = vp[0] + PANEL_X_POSITION;
		var pY = vp[1] + PANEL_Y_POSITION;
		return [pX, pY];
	}

	function getLoadingDiv() {
		return "<div>Loading... " + getProgressImg() + "</div>"
	}

	function getProgressImg() {
		var img = YAHOO.cuanto.urls.get('progressImg');
		return "<img src ='" + img + "'/>";
	}

	function populateContents(output, testName) {
		var outDiv = new Element('div', {'class':"testOutput", 'readOnly': true});
		outDiv.innerHTML = output;
		var wdth = ($('outPanel').getWidth() * .98) + "px";
		outDiv.setStyle({'width': wdth});
		outputPanel.setBody(outDiv);
		outputPanel.setHeader("Output for " + testName);
		outDiv.focus();
	}

	function prefetchNextOutputs(currentOutputId) {
		var allOutputIds = $$('.outLink').collect(function(link) {
			return link.id.match(/.+?(\d+)/)[1];
		});

		if (allOutputIds.length < 1) {
			return;
		}

		var firstIndexToCache = currentOutputId ? allOutputIds.indexOf(currentOutputId) + 1 : 0;
		var fetchCandidates = allOutputIds.slice(firstIndexToCache, firstIndexToCache + NUM_TO_PREFETCH);
		outputProxy.prefetchOutputs(fetchCandidates);
	}

	pub.showOutputForLink = function(e, overlayMgr) {
		var outcomeId = YAHOO.util.Event.getTarget(e).id.match(/.+?(\d+)/)[1];
		if (outputPanel == null || outputPanel == undefined) {
			outputPanel = new YAHOO.widget.Panel("outPanel", {
				width: document.viewport.getWidth() * .95 - PANEL_X_POSITION + "px",
				visible: true, xy: getDesiredOutPanelXY(), zIndex: 100, underlay: "none", autofillheight: "body"});
			if (overlayMgr) {
				overlayMgr.register(outputPanel);
			}
			var resize = new YAHOO.util.Resize('outPanel');
			resize.on('resize', function(args) {
			      var panelHeight = args.height;
			      this.cfg.setProperty("height", panelHeight + "px");
			  }, outputPanel, true);

		}
		outputPanel.setHeader("Output for Test " + outcomeId);
		outputPanel.setBody(getLoadingDiv());

		outputPanel.cfg.setProperty('xy', getDesiredOutPanelXY());
		outputPanel.render("outContainer");

		var escapeKey = new YAHOO.util.KeyListener($('outPanel'), { keys:27 }, { fn:closeOutPanel});
		escapeKey.enable();
		outputPanel.cfg.queueProperty('keylisteners', escapeKey);


		var closeElm = $$("#outPanel > .container-close");
		YAHOO.util.Event.purgeElement(closeElm);
		YAHOO.util.Event.addListener(closeElm, "click", closeOutPanel);

		outputPanel.show();

		var onOutputResponse = function(outputItem) {
			populateContents(outputItem.output, outputItem['shortName']);
			prefetchNextOutputs(outcomeId);
		};

		outputProxy.getOutputForId(outcomeId, onOutputResponse);
		YAHOO.util.Event.preventDefault(e);
	};

	pub.prefetchNextOutputs = function (outcomeId) {
		return prefetchNextOutputs(outcomeId);
	};

	pub.getOutputProxy = function() {
		return outputProxy;
	};

	return pub;
};