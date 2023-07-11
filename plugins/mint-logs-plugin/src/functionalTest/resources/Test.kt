import kotlin.test.Test

class Test {

    @Test
    fun example() = com.zegreatrob.testmints.setup(object {
    }) {
        println("setup")
    } exercise {
        println("exercise")
    } verify {
        println("verify")
    }

}