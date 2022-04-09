import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class AssertionsTest {
    @Test
    fun onFailureContainsMessageIsCorrect() = setup(object {
        val actual = listOf("A", "B", "C")
        val expected = "Z"
    }) exercise {
        runCatching { actual.assertContains(expected) }
    } verify { result ->
        result.exceptionOrNull()
            ?.message
            ?.assertIsEqualTo("[A, B, C] did not contain Z")
    }
}
