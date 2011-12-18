import org.specs2.mutable._

class AnalyzerSpec extends Specification {

  "This test" should {
	"pass with flying colours" in {
		val stack = 2
    	stack must equalTo(2)
	}
  }
}
