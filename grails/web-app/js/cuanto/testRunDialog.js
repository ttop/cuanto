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

YAHOO.cuanto.TestRunDialog = function() {

	var trDialog = new YAHOO.widget.Dialog('testRunDialog', {
		width: "400px",
		visible: false,
		underlay: "none",
		context: ['editTestRun', 'tl', 'tl']
	});

	var pub = {}; // public methods

	var handleCancel = function() {
		this.cancel();
	};

	var handleSubmit = function() {
		this.submit();
	};

	var myButtons = [ { text:"Save", handler:handleSubmit, isDefault:true },
	                  { text:"Cancel", handler:handleCancel } ];
	trDialog.cfg.queueProperty("buttons", myButtons);

	trDialog.callback.success = function () {
		reloadTestRunInfo();
	};

	var enterKey = new YAHOO.util.KeyListener(document, { keys:13 },
												  { fn:handleSubmit,
													scope:trDialog,
													correctScope:true } );
	var escapeKey = new YAHOO.util.KeyListener(document, { keys:27 },
												  { fn:handleCancel,
													scope:trDialog,
													correctScope:true } );

	trDialog.cfg.queueProperty("keylisteners", [enterKey, escapeKey]);
	trDialog.render();

	function refreshTestRunInfo() {
		$('trdLoading').show();
		reloadTestRunInfo(function(testRun) {
			if (testRun["milestone"]){
				$('trdMilestone').value = testRun["milestone"];
			}
			if (testRun["build"]) {
				$('trdBuild').value = testRun["build"];
			}
			if (testRun["targetEnv"]) {
				$('trdTargetEnv').value = testRun["targetEnv"];
			}
			if (testRun["note"]) {
				$('trdNote').value = testRun["note"];
			}
			if (testRun["valid"] != null) {
				$('trdValid').checked = testRun["valid"];
			}
			$('trdLoading').hide();
		});
	}

	function reloadTestRunInfo(fnAfter) {
		new Ajax.Request(YAHOO.cuanto.urls.get('testRunInfo') + $('trDialogId').innerHTML +
		                 "?rand=" + new Date().getTime(), {
			method: 'get',
			parameters: {'format' : 'json'},
			onSuccess: function(o) {
				var testRun = o.responseJSON;
				if (fnAfter) {
					fnAfter(testRun);
				}
				YAHOO.cuanto.events.testRunChangedEvent.fire(testRun);
			}
		});
	}

	pub.show = function() {
		trDialog.show();
		refreshTestRunInfo();
	};
	return pub;
};
