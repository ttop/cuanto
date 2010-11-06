YAHOO.namespace('cuanto');

YAHOO.cuanto.EditTestRun = function(testRunId) {

	$.each($('.deleteProp'), function(idx, link) {
		 YAHOO.util.Event.addListener(link, "click", deleteProperty);
		}
	);

	$.each($('.deleteLink'), function(idx, link) {
		 YAHOO.util.Event.addListener(link, "click", deleteLink);
		}
	);

	YAHOO.util.Event.addListener('addProperty', "click", addProperty);
	YAHOO.util.Event.addListener('addLink', "click", addLink);

	function deleteProperty(e) {
		YAHOO.util.Event.preventDefault(e);
		var name = $(e.target).prevAll('.propName').val();
		if (confirm("Delete property '" + name + "'?")) {
			var id = $(e.target).prevAll('.propId').attr("value");
			$.ajax({
				url: YAHOO.cuanto.urls.get('propertyDelete') + id + "?testRun=" + testRunId,
				type: "POST",
				success: function(data, status, req) {
					$(e.target).closest(".property").remove();
					$("#flashMsg").html("Property " + name + " deleted.");
					$("#flashMsg").show();
				},
				error: function(req, status, error) {
					alert("Error: " + status);
				}
			});
		}
	}

	function deleteLink(e) {
		YAHOO.util.Event.preventDefault(e);
		var descr = $(e.target).prevAll('.linkDescr').attr('value');
		if (confirm("Delete link '" + descr + "'?")) {
			var id = $(e.target).prevAll('.linkId').attr("value");
			$.ajax({
				url: YAHOO.cuanto.urls.get('linkDelete') + id + "?testRun=" + testRunId,
				type: "POST",
				success: function(data, status, req) {
					$(e.target).closest('.link').remove();
					$("#flashMsg").html("Link " + descr + " deleted.");
					$("#flashMsg").show();
				},
				error: function(req, status, error) {
					alert("Error: " + status);
				}
			});
		}

	}

	function addProperty(e) {
		YAHOO.util.Event.preventDefault(e);
		var propIndex = $('.newProp').length;
		var newPropsNode = $('#newProps');
		var propDiv = $("<div class='property newProp'><label class='narrowLabel'>Property name:</div>");
		var propName = $("<input type='text'/>");
		propName.attr(name, 'newPropName[' + propIndex + ']');
		propDiv[0].appendChild(propName[0]);
		var propValueLabel = $("<label class='nonClearLabel'>Property value:</label>");
		propDiv[0].appendChild(propValueLabel[0]);
		var propValue = $("<input type='text' size='40'/>");
		propValue.attr("name", 'newPropValue[' + propIndex + ']');
		newPropsNode[0].appendChild(propDiv[0]);
		propDiv[0].appendChild(propValue[0]);
		propDiv.append($("<br/>"));
	}

	function addLink(e) {
		YAHOO.util.Event.preventDefault(e);
		var linkIndex = $('.newLink').length;
		var newLinksNode = $('#newLinks');
		var linkDiv = $("<div class='link newLink'><label class='narrowLabel'>Link description:</label></div>");
		var linkDescr = $("<input type='text'/>");
		linkDescr.attr("name", 'newLinkDescr[' + linkIndex + "]");
		linkDiv.append(linkDescr);
		var linkUrlLabel = $("<label class='nonClearLabel'>Link URL:</label>");
		linkDiv.append(linkUrlLabel);
		var propValue = $("<input type='text' size='40'/>");
		propValue.attr("name", 'newLinkUrl[' + linkIndex + ']');
		newLinksNode.append(linkDiv);
		linkDiv.append(propValue);
		linkDiv.append($("<br/>"));
	}

};