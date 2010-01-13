YAHOO.namespace('cuanto');

YAHOO.cuanto.EditTestRun = function(testRunId) {

	$$('.deleteProp').each(function(link) {
		 YAHOO.util.Event.addListener(link, "click", deleteProperty);
		}
	);

	$$('.deleteLink').each(function(link) {
		 YAHOO.util.Event.addListener(link, "click", deleteLink);
		}
	);

	YAHOO.util.Event.addListener($('addProperty'), "click", addProperty);
	YAHOO.util.Event.addListener($('addLink'), "click", addLink);

	function deleteProperty(e) {
		YAHOO.util.Event.preventDefault(e);
		var nameNode = $(e.target).previous('.propName');
		var name = nameNode.readAttribute('value');
		if (confirm("Delete property '" + name + "'?")) {
			var id = $(e.target).previous('.propId').readAttribute("value");
			new Ajax.Request(YAHOO.cuanto.urls.get('propertyDelete') + id + "?testRun=" + testRunId, {
				method:'post',
				onSuccess: function(){
					$(e.target).up('.property').remove();
					$('flashMsg').innerHTML = "Property " + name + " deleted."
					$('flashMsg').show();
				},
				onFailure: function(transport) {
					alert(transport.responseText);
				}
			});
		}
	}

	function deleteLink(e) {
		YAHOO.util.Event.preventDefault(e);
		var descrNode = $(e.target).previous('.linkDescr');
		var descr = descrNode.readAttribute('value');
		if (confirm("Delete link '" + descr + "'?")) {
			var id = $(e.target).previous('.linkId').readAttribute("value");
			new Ajax.Request(YAHOO.cuanto.urls.get('linkDelete') + id + "?testRun=" + testRunId, {
				method:'post',
				onSuccess: function(){
					$(e.target).up('.link').remove();
					$('flashMsg').innerHTML = "Link " + descr + " deleted."
					$('flashMsg').show();
				},
				onFailure: function(transport) {
					alert(transport.responseText);
				}
			});
		}

	}

	function addProperty(e) {
		YAHOO.util.Event.preventDefault(e);
		var propIndex = $$('.newProp').length;
		var newPropsNode = $('newProps');
		var propDiv = new Element('div', {'class': 'property newProp'});
		var propNameLabel = new Element('label', {'class':"narrowLabel"});
		propNameLabel.innerHTML = "Property name:";
		propDiv.appendChild(propNameLabel);
		var propName = new Element('input', {'class': '', name: 'newPropName[' + propIndex + "]", type: 'text'});
		propDiv.appendChild(propName);
		var propValueLabel = new Element('label', {'class':"nonClearLabel"});
		propValueLabel.innerHTML = "Property value:";
		propDiv.appendChild(propValueLabel);
		var propValue = new Element('input', {name: 'newPropValue[' + propIndex + ']', type:'text', size: "40"});
		newPropsNode.appendChild(propDiv);
		propDiv.appendChild(propValue);
		propDiv.appendChild(new Element('br'));

	}

	function addLink(e) {
		YAHOO.util.Event.preventDefault(e);
		var linkIndex = $$('.newLink').length;
		var newLinksNode = $('newLinks');
		var linkDiv = new Element('div', {'class': 'link newLink'});
		var linkDescrLabel = new Element('label', {'class':"narrowLabel"});
		linkDescrLabel.innerHTML = "Link description:";
		linkDiv.appendChild(linkDescrLabel);
		var linkDescr = new Element('input', {'class': '', name: 'newLinkDescr[' + linkIndex + "]", type: 'text'});
		linkDiv.appendChild(linkDescr);
		var linkUrlLabel = new Element('label', {'class':"nonClearLabel"});
		linkUrlLabel.innerHTML = "Link URL:";
		linkDiv.appendChild(linkUrlLabel);
		var propValue = new Element('input', {name: 'newLinkUrl[' + linkIndex + ']', type:'text', size: "40"});
		newLinksNode.appendChild(linkDiv);
		linkDiv.appendChild(propValue);
		linkDiv.appendChild(new Element('br'));

	}

};