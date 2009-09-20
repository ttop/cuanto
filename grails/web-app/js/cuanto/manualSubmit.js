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

YAHOO.cuanto.ManualSubmit = function() {

	var testRunId = -1;
	var msDialog = null;
	var fileList;
	var uploader;

	var pub = {}; // public methods

	function showManualSubmitDialog(e) {
		msDialog.show();
		YAHOO.util.Event.preventDefault(e);
	}


	/* Event handlers for the flash object. */

	function handleRollOver() {
		YAHOO.util.Dom.setStyle(YAHOO.util.Dom.get('selectLink'), 'color', "#FFFFFF");
		YAHOO.util.Dom.setStyle(YAHOO.util.Dom.get('selectLink'), 'background-color', "#000000");
	}


	function handleRollOut() {
		YAHOO.util.Dom.setStyle(YAHOO.util.Dom.get('selectLink'), 'color', "#0000CC");
		YAHOO.util.Dom.setStyle(YAHOO.util.Dom.get('selectLink'), 'background-color', "#FFFFFF");
	}


	function handleMouseDown() {
	}


	function handleMouseUp() {
	}


	function handleClick() {
	}


	function handleContentReady() {
		// Allows the uploader to send log messages to trace, as well as to YAHOO.log
		uploader.setAllowLogging(true);

		// Allows multiple file selection in "Browse" dialog.
		uploader.setAllowMultipleFiles(true);

		// New set of file filters.
		var ff = new Array({description:"All", extensions:"*.*"},
		{description:"Videos", extensions:"*.avi;*.mov;*.mpg"});

		// Apply new set of file filters to the uploader.
		uploader.setFileFilters(ff);
	}


	function onFileSelect(event) {
		if ('fileList' in event && event.fileList != null) {
			fileList = event.fileList;
			createDataTable(fileList);
		}
	}


	function createDataTable(entries) {
		this.fileIdHash = {};
		this.dataArr = [];
		for (var i in entries) {
			var entry = entries[i];
			entry["progress"] = "<div style='height:5px;width:100px;background-color:#CCC;'></div>";
			this.dataArr.unshift(entry);
		}

		for (var j = 0; j < this.dataArr.length; j++) {
			this.fileIdHash[this.dataArr[j].id] = j;
		}

		var myColumnDefs = [
			{key:"name", label: "File Name", sortable:false},
			{key:"size", label: "Size", sortable:false},
			{key:"progress", label: "Upload progress", sortable:false}
		];

		this.myDataSource = new YAHOO.util.DataSource(this.dataArr);
		this.myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
		this.myDataSource.responseSchema = {
			fields: ["id","name","created","modified","type", "size", "progress"]
		};

		this.singleSelectDataTable = new YAHOO.widget.DataTable("dataTableContainer",
			myColumnDefs, this.myDataSource, {
			caption:"Files To Upload",
			selectionMode:"single"
		});
	}


	function upload() {
		if (fileList != null) {
			uploader.setSimUploadLimit(1); // avoid race condition
			uploader.uploadAll(YAHOO.cuanto.urls.get('testRunSubmitFile') + '?testRunId=' + testRunId);
		}
	}


	function onUploadProgress(event) {
		rowNum = fileIdHash[event["id"]];
		prog = Math.round(100 * (event["bytesLoaded"] / event["bytesTotal"]));
		progbar = "<div style='height:5px;width:100px;background-color:#CCC;'><div style='height:5px;background-color:#F00;width:" + prog + "px;'></div></div>";
		singleSelectDataTable.updateRow(rowNum, {name: dataArr[rowNum]["name"], size: dataArr[rowNum]["size"], progress: progbar});
	}


	function onUploadComplete(event) {
		rowNum = fileIdHash[event["id"]];
		prog = Math.round(100 * (event["bytesLoaded"] / event["bytesTotal"]));
		progbar = "<div style='height:5px;width:100px;background-color:#CCC;'><div style='height:5px;background-color:#F00;width:100px;'></div></div>";
		singleSelectDataTable.updateRow(rowNum, {name: dataArr[rowNum]["name"], size: dataArr[rowNum]["size"], progress: progbar});
		
		// if we reach the completion of the last row, we're done, so refresh.
		if (rowNum == dataArr.length - 1)
			location.reload(true);
	}


	function onUploadStart(event) {
	}


	function onUploadError(event) {
		alert("Error encountered while uploading.");
		location.reload(true);
	}


	function onUploadCancel(event) {
		alert("Upload canceled by user.");
		location.reload(true);
	}


	function onUploadResponse(event) {
		alert(event);
	}


	function attachEventHanders() {
		uploader.addListener('contentReady', handleContentReady);
		uploader.addListener('fileSelect', onFileSelect)
		uploader.addListener('uploadStart', onUploadStart);
		uploader.addListener('uploadProgress', onUploadProgress);
		uploader.addListener('uploadCancel', onUploadCancel);
		uploader.addListener('uploadComplete', onUploadComplete);
		uploader.addListener('uploadCompleteData', onUploadResponse);
		uploader.addListener('uploadError', onUploadError);
		uploader.addListener('rollOver', handleRollOver);
		uploader.addListener('rollOut', handleRollOut);
		uploader.addListener('click', handleClick);
		uploader.addListener('mouseDown', handleMouseDown);
		uploader.addListener('mouseUp', handleMouseUp);
	}


	pub.initManualSubmitDialog = function(currentTestRunId) {
		testRunId = currentTestRunId;

		YAHOO.util.Event.onAvailable("manualSubmit", function() {
			msDialog = new YAHOO.widget.Dialog('manualSubmitDialog', {
				width: "430px",
				visible: true,
				modal: true,
				underlay: "none",
				hideaftersubmit: false,
				draggable: true,
				x: 100,
				y: 100
			});

			YAHOO.util.Event.addListener("manualSubmit", "click", showManualSubmitDialog);
			//var target = YAHOO.util.Event.getTarget(e);
			//var projectName = $('msdProjectId' + projectId).innerHTML;
		});

		YAHOO.util.Event.onAvailable("uiElements", function () {
			var uiLayer = YAHOO.util.Dom.getRegion('selectLink');
			var overlay = YAHOO.util.Dom.get('uploaderOverlay');
			YAHOO.util.Dom.setStyle(overlay, 'width', uiLayer.right - uiLayer.left + "px");
			YAHOO.util.Dom.setStyle(overlay, 'height', uiLayer.bottom - uiLayer.top + "px");
		});

		YAHOO.util.Event.onAvailable("uploadLink", function() {
			$('uploadLink').onclick = upload;
		});

		YAHOO.widget.Uploader.SWFURL = YAHOO.cuanto.urls.get('uploaderSwf');
		uploader = new YAHOO.widget.Uploader("uploaderOverlay");

		attachEventHanders();

	};

	return pub;
};