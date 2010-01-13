YAHOO.namespace('cuanto');

YAHOO.cuanto.EditTestRun = function(testRunId) {

	$$('.deleteProp').each(function(link) {
		 YAHOO.util.Event.addListener(link, "click", deleteProperty);
		}
	);

	YAHOO.util.Event.addListener($('addProperty'), "click", addProperty);

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

	function addProperty(e) {
		YAHOO.util.Event.preventDefault(e);
		var propIndex = $$('.newProp').length;
		var newPropsNode = $('newProps');
		var propSpan = new Element('div', {'class': 'property newProp'});
		var propNameLabel = new Element('label', {'class':"narrowLabel"});
		propNameLabel.innerHTML = "Property name:";
		propSpan.appendChild(propNameLabel);
		var propName = new Element('input', {'class': '', name: 'newPropName[' + propIndex + "]", type: 'text'});
		propSpan.appendChild(propName);
		var propValueLabel = new Element('label', {'class':"newPropValue"});
		propValueLabel.innerHTML = "Property value:";
		propSpan.appendChild(propValueLabel);
		var propValue = new Element('input', {name: 'newPropValue[' + propIndex + ']', type:'text', size: "40"});
		newPropsNode.appendChild(propSpan);
		propSpan.appendChild(propValue);
		propSpan.appendChild(new Element('br'));

	}

};