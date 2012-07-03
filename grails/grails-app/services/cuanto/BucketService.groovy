package cuanto

import groovy.xml.MarkupBuilder

class BucketService {

    static transactional = true

    def serviceMethod() {

    }

    String getTestNgSuiteForPassFailBuckets(TestRun testRun) {
        def passedTestOutcomes = TestOutcome.executeQuery(
                """select t.testCase.packageName, t.testCase.testName from cuanto.TestOutcome t
                       where t.testRun = ? and t.testResult.isFailure = ? and t.testResult.isSkip = ?
                       order by t.testCase.fullName""",
                [testRun, false, false])
        def failedTestOutcomes = TestOutcome.executeQuery(
                """select t.testCase.packageName, t.testCase.testName from cuanto.TestOutcome t
                       where t.testRun = ? and (t.testResult.isFailure = ? or t.testResult.isSkip = ?)
                       order by t.testCase.fullName""",
                [testRun, true, true])

        Map<String, List<String>> passedTestsByClasses = createBucketByTestClass(passedTestOutcomes)
        Map<String, List<String>> failedTestsByClasses = createBucketByTestClass(failedTestOutcomes)

        def testSuite = "Bucketed Test Suite for [$testRun.project.name]"
        def buckets = ['Pass Bucket': passedTestsByClasses, 'Fail Bucket': failedTestsByClasses]
        return generateTestNgXmlForBuckets(testSuite, buckets)
    }

    private Map<String, List<String>> createBucketByTestClass(List<TestOutcome> testOutcomes) {
        def testsByClasses = [:]

        for (testOutcome in testOutcomes) {
            def testClass = testOutcome[0]
            def testName = testOutcome[1]
            if (!testsByClasses.containsKey(testClass))
                testsByClasses[testClass] = []
            testsByClasses[testClass] << testName
        }
        return testsByClasses
    }

    private String generateTestNgXmlForBuckets(
            String testSuite, Map<String, Map<String, List<String>>> buckets) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        xml.suite(name: testSuite) {
            buckets.each { bucketName, testsByClasses ->
                test(name: bucketName) {
                    classes {
                        testsByClasses.each { className, testNames ->
                            "class"(name: className) {
                                methods {
                                    testNames.each { testName ->
                                        xml.include(name: testName)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return writer.toString()
    }
}
