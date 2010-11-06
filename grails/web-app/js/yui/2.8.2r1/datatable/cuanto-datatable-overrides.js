var lang = YAHOO.lang,
	util = YAHOO.util,
	widget = YAHOO.widget,

	Dom = util.Dom,
	Ev = util.Event,
	DT = widget.DataTable;

YAHOO.widget.DropdownCellEditor.prototype.focus = function() {
	this.getDataTable()._focusEl(this.dropdown);
	this.dropdown.size = this.dropdown.length;
};

YAHOO.widget.DataTable.prototype.updateRow = function(row, oData) {
	var index = row;
	if (!lang.isNumber(index)) {
		index = this.getRecordIndex(row);
	}

	// Update the Record
	if (lang.isNumber(index) && (index >= 0)) {
		var oRecordSet = this._oRecordSet,
			oldRecord = oRecordSet.getRecord(index);


		if (oldRecord) {
			var updatedRecord = this._oRecordSet.setRecord(oData, index),
				elRow = this.getTrEl(oldRecord),
				// Copy data from the Record for the event that gets fired later
				oldData = oldRecord ? oldRecord.getData() : null;

			if (updatedRecord) {
				// Update selected rows as necessary
				var tracker = this._aSelections || [],
					i = 0,
					oldId = oldRecord.getId(),
					newId = updatedRecord.getId();
				for (; i < tracker.length; i++) {
					if ((tracker[i] === oldId)) {
						tracker[i] = newId;
					}
					else if (tracker[i].recordId === oldId) {
						tracker[i].recordId = newId;
					}
				}

				// Update the TR only if row is on current page
				this._oChainRender.add({
					method: function() {
						if ((this instanceof DT) && this._sId) {
							// code replacement here per Satyam at http://yuilibrary.com/forum/viewtopic.php?f=90&t=5437&start=0
							if (elRow) {
								this._updateTrEl(elRow, updatedRecord);
							}
							else if (!this.get('paginator')) {
								this.getTbodyEl().appendChild(this._addTrEl(updatedRecord));
							}
							this.fireEvent("rowUpdateEvent", {record:updatedRecord, oldData:oldData});
							YAHOO.log("DataTable row updated: Record ID = " + updatedRecord.getId() +
								", Record index = " + this.getRecordIndex(updatedRecord) +
								", page row index = " + this.getTrIndex(updatedRecord), "info", this.toString());
						}
					},
					scope: this,
					timeout: (this.get("renderLoopSize") > 0) ? 0 : -1
				});
				this._runRenderChain();
				return;
			}
		}
	}
	YAHOO.log("Could not update row " + row + " with the data : " + lang.dump(oData), "warn", this.toString());
	return;
}