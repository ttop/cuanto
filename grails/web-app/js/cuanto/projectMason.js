/*
 Copyright (c) 2010 Todd Wells

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

YAHOO.cuanto.ProjectMason = function() {
	var pub = {};
	var projectDialog = new YAHOO.cuanto.ProjectDialog();
	var cookieName = "project";

	pub.init = function() {
		initMasonry();
		initProjectDialog();
		initExpandAndCollapse();
		YAHOO.cuanto.events.projectChangeEvent.subscribe(onProjectChange);
	};

	function initProjectDialog() {
		YAHOO.util.Event.onAvailable("addProject", function() {
			YAHOO.util.Event.addListener("addProject", "click", showAddProject);
		});
	}

	function initExpandAndCollapse() {
		YAHOO.util.Event.onAvailable("expandAll", function() {
			YAHOO.util.Event.addListener("expandAll", "click", expandAll);
		});
		YAHOO.util.Event.onAvailable("collapseAll", function() {
			YAHOO.util.Event.addListener("collapseAll", "click", collapseAll);
		});

	}


	function initMasonry() {
		openPrevious();

		$('#groups').masonry({
			singleMode: true,
			itemSelector: '.projBox',
			animate: true,
			animationOptions: {easing: 'linear', duration:200},
			saveOptions: true
		});

		$(".projBox").unbind();
		$(".projBox").click(function(event) {
			if ($(event.target).attr("tagName") != 'A') {
				event.preventDefault();
				var projList = $(".projList", this);
				if (projList.css("display") == "none") {
					showProjectGroup(this);
				} else {
					hideProjectGroup(this);
				}
				$('#groups').masonry();
				storeVisibleGroups()
			}
		});
	}

	function showProjectGroup(grp) {
		var projList = $(".projList", grp);
		var projBox = $(grp).closest('.projBox');
		projBox.addClass("shown");
		$(".projList", grp).show();
		$(grp).closest('.projBox').find(".groupLink").show();
	}

	function hideProjectGroup(grp) {
		var projList = $(".projList", grp);
		$(".projList", grp).hide();
		$(grp).closest('.projBox').removeClass("shown");
		$(grp).closest('.projBox').find(".groupLink").hide();
	}

	function expandAll() {
		$.each($(".projBox"), function(idx, grp) {
			showProjectGroup(grp);
		});
		$('#groups').masonry();
		storeVisibleGroups();
	}


	function collapseAll() {
		$.each($(".projBox"), function(idx, grp) {
			hideProjectGroup(grp);
		});
		$('#groups').masonry();
		storeVisibleGroups();
	}


	function storeVisibleGroups() {
		var visible = $(".shown").children(".projGroup");
		var visibleIds = $.map(visible, function(grp) {
			return grp.id;
		});
		var expDate = new Date();
		expDate.setDate(expDate.getDate() + 30);
		var text = visibleIds.join(",");
		YAHOO.util.Cookie.set(cookieName, text, {path:"/", expires: expDate});
	}

	function openPrevious() {
		var text = YAHOO.util.Cookie.get(cookieName);
		if (text) {
			var visibleIds = text.split(",");
			$.each(visibleIds, function(idx, grp) {
				showGroup(grp);
			});
		}
	}

	function showGroup(grp) {
		var grpId = "#" + grp;
		var box = $(grpId).closest(".projBox");
		box.addClass("shown");
		$(grpId).children(".groupLink").show();
		var projList = $(grpId).nextAll(".projList");
		projList.show();
	}

	function showAddProject(e) {
		projectDialog.setTitle("Add Project");
		projectDialog.show();
		YAHOO.util.Event.preventDefault(e);
	}

	function onProjectChange(e, proj) {
		YAHOO.util.Event.preventDefault(e);
		var project = proj[0].project;
		var shortGrpName = project.projectGroup.name.replace(/\s/, "");
		var grpId = "#grp-" + shortGrpName;
		if ($(grpId).length > 0) {
			$.ajax({
				url: YAHOO.cuanto.urls.get('masonProjList') + "?rand=" + new Date().getTime(),
				dataType: "html",
				data: {"id": project.projectGroup.id},
				success: function(data, status, req) {
					var grpId = "#grp-" + project.projectGroup.name.replace("\\s+", "");
					$(grpId).nextAll(".projList").replaceWith(data);
					$(grpId).nextAll(".projList").show();
					initMasonry();
				}
			});
		} else {
			$.ajax({
				url: YAHOO.cuanto.urls.get('mason') + "?rand=" + new Date().getTime(),
				dataType: "html",
				data: {"id": project.projectGroup.id, "refresh": "t"},
				success: function(data, status, req) {
					$('#projectMason').replaceWith(data);
					initMasonry();
					showGroup("grp-" + shortGrpName);
					storeVisibleGroups();
					initMasonry();
				}
			});
		}
	}

	return pub;
};
