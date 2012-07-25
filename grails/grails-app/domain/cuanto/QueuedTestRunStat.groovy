/*
	Copyright (c) 2010 Todd E. Wells

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

package cuanto

public class QueuedTestRunStat {
	// This class represents a test run that is queued for stat calculation -- we'll just use the ID rather than
	// a reference to the actual TestRun object. If the TestRun is not present at the time the calculation is
	// attempted, it will just be ignored. This way we don't have to manage the case where TestRuns are deleted
	// while in the queue
	Long testRunId
	Date dateCreated

	static mapping = {
		version false
	}
}