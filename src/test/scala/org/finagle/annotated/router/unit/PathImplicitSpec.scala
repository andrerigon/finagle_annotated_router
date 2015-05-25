package org.finagle.annotated.router.unit

import com.twitter.finagle.http.path.Path
import org.finagle.annotated.router.PathImplicits
import org.scalatest.{FlatSpec, Matchers, BeforeAndAfter}
import org.scalatest.mock.MockitoSugar

class PathImplicitSpec extends FlatSpec with Matchers with MockitoSugar with BeforeAndAfter {
  it should "create correct Path" in {

    import PathImplicits._

    val path: Path = "/api/foo"

    path.toList shouldBe List("api", "foo")
  }
}
