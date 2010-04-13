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

package cuanto.parsers


/**
* A TestOutcome contains information about one execution of a test case. Valid <b>TestResults</b> are determined by the Cuanto
* database. The testResults available in a default installation are <b>Pass, Fail, Error, Ignore, Skip,</b> and <b>Unexecuted</b>.
* <b>dateCreated</b> and <b>lastUpdated</b> are not settable, the Cuanto server maintains these values.   
*/
class ParsableTestOutcome {
	ParsableTestCase testCase
	String testResult
    String testOutput
    Long duration
    String owner
    String note
	Long id
	Date startedAt
	Date finishedAt
	Date dateCreated
	Date lastUpdated
    List tags


	boolean equals(o) {
		if (this.is(o)) return true;

		if (!o || getClass() != o.class) return false;

		ParsableTestOutcome that = (ParsableTestOutcome) o;

		if (duration ? !duration.equals(that.duration) : that.duration != null) return false;
		if (id ? !id.equals(that.id) : that.id != null) return false;
		if (note ? !note.equals(that.note) : that.note != null) return false;
		if (owner ? !owner.equals(that.owner) : that.owner != null) return false;
		if (testCase ? !testCase.equals(that.testCase) : that.testCase != null) return false;
		if (testOutput ? !testOutput.equals(that.testOutput) : that.testOutput != null) return false;
		if (testResult ? !testResult.equals(that.testResult) : that.testResult != null) return false;
		if (startedAt ? ! startedAt.equals(that.startedAt) : that.startedAt != null) return false;
		if (finishedAt ? ! finishedAt.equals(that.finishedAt) : that.finishedAt != null) return false;
		if (dateCreated ? ! dateCreated.equals(that.dateCreated) : that.dateCreated != null) return false;
		if (lastUpdated ? ! lastUpdated.equals(that.lastUpdated) : that.lastUpdated != null) return false;

		return true;
	}

	int hashCode() {
		int result;

		result = (testCase ? testCase.hashCode() : 0);
		result = 31 * result + (testResult ? testResult.hashCode() : 0);
		result = 31 * result + (testOutput ? testOutput.hashCode() : 0);
		result = 31 * result + (duration ? duration.hashCode() : 0);
		result = 31 * result + (owner ? owner.hashCode() : 0);
		result = 31 * result + (note ? note.hashCode() : 0);
		result = 31 * result + (id ? id.hashCode() : 0);
		result = 31 * result + (startedAt ? startedAt.hashCode() : 0);
		result = 31 * result + (finishedAt ? finishedAt.hashCode() : 0);
		result = 31 * result + (dateCreated ? dateCreated.hashCode() : 0);
		result = 31 * result + (lastUpdated ? lastUpdated.hashCode() : 0);
		return result;
	}
}