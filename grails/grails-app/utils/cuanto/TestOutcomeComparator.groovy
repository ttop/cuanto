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


package cuanto

class TestOutcomeComparator implements Comparator{

	def sortBy = "testOutcome"
	def desc = false

	public int compare(Object to1, Object to2) {
		int result = 0
		TestOutcome outcomeOne = (TestOutcome)to1
		TestOutcome outcomeTwo = (TestOutcome)to2
		if (sortBy.equalsIgnoreCase("testOutcome")) {
			if (outcomeOne.fullName < outcomeTwo.fullName) {
				result = -1
			} else if (outcomeTwo > outcomeOne) {
				result = 1
			} 
		}
		if (desc) {
			result = result * -1
		}
		return result
	}
}