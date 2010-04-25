package cuanto.queryprocessor

import cuanto.QueryFilter
import cuanto.TestOutcome

class FailureStatusChangedQueryModule implements QueryModule {

	def Map getQueryParts(QueryFilter queryFilter) {
		[where: " where t.isFailureStatusChanged = ? ", from: queryFilter]
	}

	def List<Class> getObjectTypes() {
		[TestOutcome.class]
	}
}
