
class TestRunStatsJob {
	def statisticService
	def concurrent = false

	static triggers = {
		simple name: 'CalculateTestRunStats', startDelay: 5000, repeatInterval: 15000
	}

    def execute() {
	    if (!statisticService.processingTestRunStats) {
		    statisticService.processTestRunStats()
	    }
    }
}
