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

YAHOO.cuanto.ListGroup = function() {
	var projectDialog = new YAHOO.cuanto.ProjectDialog();
	var projectDeleteDialog = new YAHOO.cuanto.DeleteProjectDialog();
	
	YAHOO.cuanto.events.projectChangeEvent.subscribe(onProjectChange);
	initListeners();

	function initListeners() {
		$$('.editProj').each(function(lnk) {
			YAHOO.util.Event.addListener(lnk, "click", showEditProject);
		});

		$$('.deleteProj').each(function(lnk) {
			YAHOO.util.Event.addListener(lnk, "click", showDeleteProject);
		});
	}

	function showEditProject(e) {
		projectDialog.setTitle("Edit Project");
		projectDialog.show();
		var target = YAHOO.util.Event.getTarget(e);
		var projectId = target.id.match(/.+?(\d+)/)[1];
		projectDialog.loadProject(projectId);
		YAHOO.util.Event.preventDefault(e);
	}

	function onProjectChange(e) {
		new Ajax.Request(YAHOO.cuanto.urls.get('groupTable'), {
			method: 'get',
			parameters: {id: $('groupId').getValue()},
			onSuccess: function(transport){
				$('listGroupTableDiv').innerHTML = transport.responseText;
				initListeners();
			}
		});
		YAHOO.util.Event.preventDefault(e);
	}

	function showDeleteProject(e) {
		var target = YAHOO.util.Event.getTarget(e);
		var projectId = target.id.match(/.+?(\d+)/)[1];
		var projectName = $('pName' + projectId).innerHTML;
		projectDeleteDialog.show(projectId, projectName);
		YAHOO.util.Event.preventDefault(e);
	}
};