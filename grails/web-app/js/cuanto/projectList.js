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

YAHOO.cuanto.ProjectList = function() {
	var pub = {};
	var projectDialog = new YAHOO.cuanto.ProjectDialog();
	var projectDeleteDialog = new YAHOO.cuanto.DeleteProjectDialog();

	pub.init = function() {
		initHeight();
		initAccordion();
		initProjectDialog();
		initDeleteProjectDialog();
		var initialAccordianMenuWidth = parseInt($('accordionMenu').getStyle('width'));
		var scrollBarCompensatedWidth = initialAccordianMenuWidth + 20 + "px";
		$('accordionMenu').setStyle({width : scrollBarCompensatedWidth});

		YAHOO.cuanto.events.projectChangeEvent.subscribe(onProjectChange);
		window.onresize = initHeight;
	};

	function initHeight() {
		var newHt = document.viewport.getHeight() - 136;
		$('accordionMenu').setStyle({height: newHt + "px"});
		$('rightColProjects').clonePosition($('accordionMenu'), {setLeft: false, setWidth: false});
		$('projColInner').setStyle({height: newHt - 60 + "px", width: $('rightColProjects').getWidth() - 60 + "px"});
	}

	function initAccordion() {
		$$('.tog').each(function(toggler) {
			YAHOO.util.Event.addListener(toggler, 'mouseover', showProjectList);
		});

		var stretchers = $$(".accordion");
		var togglers = $$(".inactiveToggler");
		new fx.Accordion(togglers, stretchers, {
			trigger: 'hover',
			triggerThreshold: 0,
			opacity: true,
			start: false,
			duration: 0
		});
	}

	function showProjectList() {
		$('rightColProjects').show();
		$$('.tog').each(function(toggler) {
			YAHOO.util.Event.removeListener(toggler, 'mouseover', showProjectList);
		});
	}

	function initProjectDialog() {
		YAHOO.util.Event.onAvailable("addProject", function() {
		 	YAHOO.util.Event.addListener("addProject", "click", showAddProject);
			$$('.editProj').each(function(lnk) {
				YAHOO.util.Event.addListener(lnk, "click", showEditProject);
			});
		 });
	}

	function initDeleteProjectDialog() {
		YAHOO.util.Event.onAvailable("addProject", function() {
			$$('.deleteProj').each(function(lnk) {
				YAHOO.util.Event.addListener(lnk, "click", showDeleteProject);
			});
		});
	}


	function onProjectChange(e) {
		var url = YAHOO.cuanto.urls.get('projectList') + "?rand=" + new Date().getTime();
		new Ajax.Request(url, {
			method: 'get',
			onSuccess: function(o) {
				$('projectBody').innerHTML = o.responseText;
				initAccordion();
				initProjectDialog();
				initDeleteProjectDialog();
				initHeight();
			}
		});
		YAHOO.util.Event.preventDefault(e);
	}

	function showAddProject(e) {
		projectDialog.setTitle("Add Project");
		projectDialog.show();
		YAHOO.util.Event.preventDefault(e);
	}

	function showEditProject(e) {
		projectDialog.setTitle("Edit Project");
		projectDialog.show();
		var target = YAHOO.util.Event.getTarget(e);
		var projectId = target.id.match(/.+?(\d+)/)[1];
		projectDialog.loadProject(projectId);
		YAHOO.util.Event.preventDefault(e);
	}

	function showDeleteProject(e) {
		var target = YAHOO.util.Event.getTarget(e);
		var projectId = target.id.match(/.+?(\d+)/)[1];
		var projectName = $('pName' + projectId).innerHTML;
		projectDeleteDialog.show(projectId, projectName);
		YAHOO.util.Event.preventDefault(e);
	}



	return pub;
};
