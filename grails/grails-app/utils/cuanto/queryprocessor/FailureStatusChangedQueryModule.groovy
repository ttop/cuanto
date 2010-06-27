package cuanto.queryprocessor

import cuanto.QueryFilter
import cuanto.TestOutcome

class FailureStatusChangedQueryModule implements QueryModule {

	def Map getQueryParts(QueryFilter queryFilter) {
		(queryFilter.isFailureStatusChanged == null) ?
			[:] :
			[where: "t.isFailureStatusChanged = ?", params: [queryFilter.isFailureStatusChanged]]
	}

	def List<Class> getObjectTypes() {
		[TestOutcome.class]
	}
}
