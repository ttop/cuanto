import cuanto.parsers.ParsableTestOutcome
import cuanto.parsers.NUnitParser
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



public class NUnitParserTests extends GroovyTestCase {

	void testBasicNUnitParsing() {
		def parser = new NUnitParser()
		List<ParsableTestOutcome> outcomes = parser.parseFile(getFile("NUnit-TestResultNet.xml"))
		assertEquals "Wrong number of outcomes found", 164, outcomes.size()

		ParsableTestOutcome f8 = outcomes.find {
			it.testCase?.fullName == "NETTests.Tests.Attachmate.Reflection.Emulation.IbmHosts.ControlKeys.F8"
		} as ParsableTestOutcome
		assertNotNull "didn't find test", f8
		assertEquals "package name", "NETTests.Tests.Attachmate.Reflection.Emulation.IbmHosts.ControlKeys", f8.testCase?.packageName
		assertEquals "test name", "F8", f8.testCase?.testName
		assertEquals "result", "Pass", f8.testResult
		assertEquals "duration", 984, f8.duration
		assertNull "output", f8.testOutput
        assertEquals "tag", 1, f8.tags?.size
        assertEquals "tag name", "ABAT", f8.tags[0]

		ParsableTestOutcome aFailure = outcomes.find {
			it.testCase?.fullName == "NETTests.Tests.Attachmate.Reflection.Framework.MyReflectionTests.CreateApplication_IPAddress_DomainCredentials_Tcpip"
		} as ParsableTestOutcome
		assertNotNull "didn't find test", aFailure
		assertEquals "package name", "NETTests.Tests.Attachmate.Reflection.Framework.MyReflectionTests", aFailure.testCase?.packageName
		assertEquals "test name", "CreateApplication_IPAddress_DomainCredentials_Tcpip", aFailure.testCase?.testName
		assertEquals "result", "Fail", aFailure.testResult
		assertEquals "duration", 3656, aFailure.duration
		assertFalse "output has CDATA", aFailure.testOutput?.startsWith("<![CDATA")
		assertTrue "output start", aFailure.testOutput.startsWith("System.Exception : ")
		assertTrue "output end", aFailure.testOutput.endsWith("line 48")
	}


	void testArgosFrench() {
		def parser = new NUnitParser()
		List<ParsableTestOutcome> outcomes = parser.parseFile(getFile("NUnit-Compagny.Argos-Test-Result-French.xml"))
		assertEquals "Wrong number of outcomes found", 14, outcomes.size()

		ParsableTestOutcome badIdTest = outcomes.find {
			it.testCase?.fullName == "Compagny.Argos.Test.Service.DebiteurManagerTest.LoadDebiteurBadId"
		} as ParsableTestOutcome
		assertNotNull "didn't find test", badIdTest
		assertEquals "package name", "Compagny.Argos.Test.Service.DebiteurManagerTest", badIdTest.testCase?.packageName
		assertEquals "test name", "LoadDebiteurBadId", badIdTest.testCase?.testName
		assertEquals "result", "Pass", badIdTest.testResult
		assertEquals "duration", 16, badIdTest.duration
		assertNull "output", badIdTest.testOutput

		ParsableTestOutcome aFailure = outcomes.find {
			it.testCase?.fullName == "Compagny.Argos.Test.DAO.DAOFactoryTest.Error"
		} as ParsableTestOutcome
		assertNotNull "didn't find test", aFailure
		assertEquals "package name", "Compagny.Argos.Test.DAO.DAOFactoryTest", aFailure.testCase?.packageName
		assertEquals "test name", "Error", aFailure.testCase?.testName
		assertEquals "result", "Fail", aFailure.testResult
		assertEquals "duration", 16, aFailure.duration
		assertFalse "output has CDATA", aFailure.testOutput?.startsWith("<![CDATA")
		assertTrue "output start", aFailure.testOutput.startsWith("Test Error")
		assertTrue "output end", aFailure.testOutput.endsWith("line 66")
	}


	void testMockResult() {
		def parser = new NUnitParser()
		List<ParsableTestOutcome> outcomes = parser.parseFile(getFile("NUnit-Mock-Test-Result.xml"))
		assertEquals "Wrong number of outcomes found", 7, outcomes.size()

		ParsableTestOutcome notExecuted = outcomes.find {
			it.testCase?.fullName == "NUnit.Tests.Assemblies.MockTestFixture.MockTest5"
		} as ParsableTestOutcome
		assertNotNull "didn't find test", notExecuted
		assertEquals "package name", "NUnit.Tests.Assemblies.MockTestFixture", notExecuted.testCase?.packageName
		assertEquals "test name", "MockTest5", notExecuted.testCase?.testName
		assertEquals "result", "Skip", notExecuted.testResult
		assertNull "duration", notExecuted.duration
		assertEquals "output start", "Method MockTest5's signature is not correct: it must be a public method.", notExecuted.testOutput
	}


	File getFile(fileName) {
		File testFile = new File("test/resources/${fileName}")
		if (!testFile.exists()) {
			testFile = new File("grails/test/resources/${fileName}")
		}
		assertTrue "Couldn't find ${fileName} at ${testFile.absolutePath}", testFile.exists()
		return testFile
	}


	void assertTestOutcomeEquals(ParsableTestOutcome a, ParsableTestOutcome b) {
		assertEquals "Wrong note", a.note, b.note
		assertEquals "Wrong owner", a.owner, b.owner
		assertEquals "Wrong result", a.testResult, b.testResult
		assertEquals "Wrong duration", a.duration, b.duration
		assertTrue "Test cases are unequal", a.testCase.equals(b.testCase)
	}

}