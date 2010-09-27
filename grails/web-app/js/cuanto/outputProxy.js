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

YAHOO.cuanto.OutputProxy = function() {


	var pub = {}; // public methods
	var outputCache = new YAHOO.cuanto.LruCache(50);

	Function.prototype.bindFunc = function(){
		// http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Functions:arguments
		var _$A = function(a){return Array.prototype.slice.call(a);};

		if(arguments.length < 2 && (typeof arguments[0] == "undefined")) return this;

		var __method = this, args = _$A(arguments), object = args.shift();

		return function() {
		  return __method.apply(object, args.concat(_$A(arguments)));
		}
	};

	pub.prefetchOutputs = function(outputIdArray){
		// only fetches outputs that aren't already cached

		var toFetch = $.grep(outputIdArray, function(outId, idx) {
			return outputCache.getItem(outId) == undefined;
		});

		if (toFetch.length > 0) {
			var fetchString = "";
			$.each(toFetch, function(idx, idToFetch) {
				fetchString += "&id=" + idToFetch;
			});
			var url = YAHOO.cuanto.urls.get('testOutputDataSource') + "?format=json" + fetchString;

			var callback = {
				success: function(o) {
					var response = YAHOO.lang.JSON.parse(o.responseText);
					$.each(response["testOutputs"], function(idx, out) {
						outputCache.addItem(out.id, {'output': out.output, 'testName': out.testName,
							'shortName': out.shortName, 'id': out.id});
					});
				}
			};
			YAHOO.util.Connect.asyncRequest('GET', url, callback);
		}
	};

	/*
	    Gets the output ID and calls the function userCallback with the item as an argument. If scope is provided,
	    callback will be bound to that scope ('this' will be set to 'scope' for userCallback). Item has id, output
	    and testName properties.
	 */
	pub.getOutputForId = function(outcomeId, userCallback, scope) {
		var funcToCall;
		if (scope) {
			funcToCall = userCallback.bindFunc(scope);
		} else {
			funcToCall = userCallback;
		}

		var cachedValue = outputCache.getItem(outcomeId);
		if (cachedValue) {
			funcToCall(cachedValue);
		} else {
			var processOutputJson = {
				success: function(o) {
					var response = YAHOO.lang.JSON.parse(o.responseText);

					if (response["testOutputs"].length > 0) {
						var item = response["testOutputs"][0];
						var output = item.output;
						var testName = item.testName;
						outputCache.addItem(outcomeId, { 'output': output, 'testName': testName, 'id': outcomeId,
							'shortName': item.shortName});
						funcToCall(item);
					}
				}
			};
			var url = YAHOO.cuanto.urls.get('testOutputDataSource') + outcomeId + "?format=json";
			YAHOO.util.Connect.asyncRequest('GET', url, processOutputJson);
		}
	};
	return pub;
};