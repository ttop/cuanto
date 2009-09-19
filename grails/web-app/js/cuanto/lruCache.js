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

YAHOO.cuanto.LruCache = function(maxItems) {
	var cache = new Object();
	var cacheCount = 0;
	var cacheMaxItems = maxItems;

	var publicMethods = {};

	publicMethods.addItem = function(key, value) {
		cacheCount++;
		if (cacheCount > cacheMaxItems) {
			removeOldestItem();
		}
		cache[key] = {'value': value, date: new Date().getTime()};
	};

	function removeOldestItem() {
		var oldest;

		for (var prop in cache) {
			if (!oldest || cache[prop].date < cache[oldest].date) {
				oldest = prop;
			}
		}

		if (oldest) {
			delete cache[oldest];
			cacheCount--;
		}
	}

	publicMethods.getItem = function(key) {
		if (cache[key]) {
			return cache[key]['value'];
		} else {
			return undefined;
		}
	};

	publicMethods.removeItem = function(key) {
		if (cache[key]) {
			var item = cache[key]['value'];
			delete cache[key];
			cacheCount--;
			return item;
		} else {
			return undefined;
		}
	};

	publicMethods.getCache = function(){
		return cache;
	};

	publicMethods.getCount = function() {
		return cacheCount;
	};

	publicMethods.getRawItem = function(key) {
		return cache[key];
	};

	publicMethods.flush = function() {
		cache = null;
		cache = new Object();
		cacheCount = 0;
	};
	
	return publicMethods;
};

