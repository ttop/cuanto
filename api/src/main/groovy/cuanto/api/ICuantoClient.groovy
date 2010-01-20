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

	public void deleteTestRun(Long testRunId)

	public TestOutcome getTestOutcome(Long testOutcomeId)

	public void submitFile(File file, Long testRunId)

	public void submitFiles(List<File> files, Long testRunId)

	public Long createTestOutcomeForTestRun(TestOutcome testOutcome, Long testRunId)

	public Long createTestOutcomeForProject(TestOutcome testOutcome, Long projectId)

	public void updateTestOutcome(TestOutcome testOutcome)

	public void deleteTestOutcome(Long testOutcomeId)

	public List<TestRun> getTestRunsWithProperties(Long projectId, List<TestProperty> testProperties)

    public TestCase getTestCase(Long projectId, String testPackage, String testName, String parameters)

}