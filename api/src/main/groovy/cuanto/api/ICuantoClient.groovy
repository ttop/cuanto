/*
 Copyright (c) 2010 Todd Wells

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

public interface ICuantoClient {

	String cuantoUrl
	String proxyHost
	Integer proxyPort

	public Long createProject(Project project)

	public void deleteProject(Long id)

	public Project getProject(Long projectId)

	public TestRun getTestRun(Long testRunId)

	public Long createTestRun(TestRun testRun)

	public TestOutcome getTestOutcome(Long testOutcomeId)

	public void submit(File file, Long testRunId)

	public void submit(List<File> files, Long testRunId)

	public Long submit(TestOutcome testOutcome, Long testRunId)

	public Long submit(TestOutcome testOutcome)

	public Long update(TestOutcome testOutcome)
}