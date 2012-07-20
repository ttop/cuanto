package cuanto

class TestOutcomeStats {

    static constraints = {
        successRate(nullable: false)
        streak(nullable: false)
    }

    BigDecimal successRate = 0
    Integer streak = 0
}
