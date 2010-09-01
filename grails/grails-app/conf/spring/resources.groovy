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


import cuanto.formatter.FullPackageFormatter
import cuanto.formatter.ClassnameFormatter
import cuanto.formatter.ParentPackageFormatter
import cuanto.formatter.MethodOnlyFormatter
import cuanto.formatter.ManualFormatter
import cuanto.parsers.JunitReportParser
import cuanto.parsers.CuantoManualParser
import cuanto.parsers.TestNgParser
import cuanto.queryprocessor.TestRunQueryModule
import cuanto.queryprocessor.TestResultIsFailureQueryModule
import cuanto.queryprocessor.TestResultQueryModule
import cuanto.queryprocessor.TestCaseFullNameQueryModule
import cuanto.queryprocessor.TestCaseParametersQueryModule
import cuanto.queryprocessor.TestCasePackageQueryModule
import cuanto.queryprocessor.ProjectQueryModule
import cuanto.queryprocessor.TestResultIncludedInCalculationsQueryModule
import cuanto.queryprocessor.IsAnalyzedQueryModule
import cuanto.queryprocessor.AnalysisStateQueryModule
import cuanto.queryprocessor.BugQueryModule
import cuanto.queryprocessor.OwnerQueryModule
import cuanto.queryprocessor.TestCaseQueryModule
import cuanto.queryprocessor.NoteQueryModule
import cuanto.queryprocessor.TestOutputQueryModule
import cuanto.queryprocessor.TagNameQueryModule
import cuanto.queryprocessor.HasTagsQueryModule
import cuanto.queryprocessor.DateExecutedQueryModule
import cuanto.parsers.NUnitParser
import cuanto.queryprocessor.FailureStatusChangedQueryModule
import cuanto.queryprocessor.TestResultIsSkipQueryModule
import cuanto.queryprocessor.TestOutcomeHasAllPropertiesQueryModule

beans = {
    testParserRegistry(cuanto.TestParserRegistry) {
	    parsers = [
		    new JunitReportParser(),
		    new CuantoManualParser(),
		    new TestNgParser(),
		    new NUnitParser()
	    ]
    }

	testCaseFormatterRegistry(cuanto.formatter.TestCaseFormatterRegistry) {
		formatterList = [
			new FullPackageFormatter(),
			new ClassnameFormatter(),
			new ParentPackageFormatter(),
			new MethodOnlyFormatter(),
			new ManualFormatter()
		]
	}

	queryBuilder(cuanto.QueryBuilder) {
		queryModules = [
			new TestRunQueryModule(),
			new TestResultIsFailureQueryModule(),
			new TestResultIsSkipQueryModule(),
			new TestResultQueryModule(),
			new TestCaseFullNameQueryModule(),
			new TestCaseParametersQueryModule(),
			new TestCasePackageQueryModule(),
			new ProjectQueryModule(),
			new TestResultIncludedInCalculationsQueryModule(),
			new IsAnalyzedQueryModule(),
			new AnalysisStateQueryModule(),
			new BugQueryModule(),
			new OwnerQueryModule(),
			new TestCaseQueryModule(),
			new NoteQueryModule(),
			new TestOutputQueryModule(),
            new TagNameQueryModule(),
            new HasTagsQueryModule(),
			new DateExecutedQueryModule(),
			new FailureStatusChangedQueryModule(),
			new TestOutcomeHasAllPropertiesQueryModule()
		]
	}
}