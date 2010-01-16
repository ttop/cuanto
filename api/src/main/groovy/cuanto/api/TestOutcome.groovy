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

package cuanto.api

import cuanto.api.TestCase

class TestOutcome {
	TestCase testCase
	String testResult
    String testOutput
    BigDecimal duration
    String owner
    Bug bug
    String note
	Long id


	boolean equals(o) {
		if (this.is(o)) return true;

		if (!o || getClass() != o.class) return false;

		TestOutcome that = (TestOutcome) o;

		if (bug ? !bug.equals(that.bug) : that.bug != null) return false;
		if (duration ? !duration.equals(that.duration) : that.duration != null) return false;
		if (id ? !id.equals(that.id) : that.id != null) return false;
		if (note ? !note.equals(that.note) : that.note != null) return false;
		if (owner ? !owner.equals(that.owner) : that.owner != null) return false;
		if (testCase ? !testCase.equals(that.testCase) : that.testCase != null) return false;
		if (testOutput ? !testOutput.equals(that.testOutput) : that.testOutput != null) return false;
		if (testResult ? !testResult.equals(that.testResult) : that.testResult != null) return false;

		return true;
	}

	int hashCode() {
		int result;

		result = (testCase ? testCase.hashCode() : 0);
		result = 31 * result + (testResult ? testResult.hashCode() : 0);
		result = 31 * result + (testOutput ? testOutput.hashCode() : 0);
		result = 31 * result + (duration ? duration.hashCode() : 0);
		result = 31 * result + (owner ? owner.hashCode() : 0);
		result = 31 * result + (bug ? bug.hashCode() : 0);
		result = 31 * result + (note ? note.hashCode() : 0);
		result = 31 * result + (id ? id.hashCode() : 0);
		return result;
	}
}