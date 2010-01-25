YAHOO.namespace('cuanto');

YAHOO.cuanto.TimeParser = function() {
	var pub = {};

	pub.formatMs = function(ms) {
		var workingMs = ms;
		var SECOND = 1000;
		var MINUTE = 60 * SECOND;
		var HOUR = 60 * MINUTE;

		var hrs = Math.floor(workingMs / HOUR);
		workingMs -= hrs * HOUR;

		var minutes = Math.floor(workingMs / MINUTE);
		workingMs -= minutes * MINUTE;

		var seconds = Math.floor(workingMs / SECOND);
		workingMs -= seconds * SECOND;

		var str = "";
		if (hrs > 0) {
			str = hrs + ":";
		}
		str += pad(minutes, 2) + ":" + pad(seconds, 2) + "." + pad(workingMs, 3);
		return str;
	};

	function pad(n, len) {
		s = n.toString();
		if (s.length < len) {
			s = ('0000000000' + n.toString()).slice(-len);
		}

		return s;
	}

	return pub;
};