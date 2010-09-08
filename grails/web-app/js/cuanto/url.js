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

YAHOO.cuanto.urls = function() {
	var urls = new Object();

	return { // public methods
		set: function(attr, url) {
			var tmp = url;
			tmp = tmp.replace(/;jsessionid=.*(?=[\/|$])/, ""); // workaround for CUANTO-34
			urls[attr] = tmp;
		},

		get: function(attr) {
			return urls[attr];
		}
	};
}();
