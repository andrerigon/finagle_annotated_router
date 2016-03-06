package org.finagle.annotated.router

import com.twitter.finagle.http.path.{Root, Path}
import com.twitter.finagle.http.{Method => HttpMethod}
import scala.annotation.StaticAnnotation
import scala.reflect.runtime.currentMirror
import scala.reflect.runtime.universe._

case class Route(path: Path, methods: HttpMethod*) extends StaticAnnotation

object PathImplicits {
  implicit def String2Path(v : String): Path = Path(v)
}

object Route {

  private def extractMethod(s: String) = s.toLowerCase match {
    case "post"     => HttpMethod.Post
    case "put"      => HttpMethod.Put
    case "head"     => HttpMethod.Head
    case "patch"    => HttpMethod.Patch
    case "delete"   => HttpMethod.Delete
    case "trace"    => HttpMethod.Trace
    case "connect"  => HttpMethod.Connect
    case _          => HttpMethod.Get
  }

   def getFrom[T](clazz: Class[T]) = {
     implicit val unliftableHttpMethod: Unliftable[HttpMethod] = Unliftable[HttpMethod] {
       case q"com.twitter.finagle.http.Method.${x: NameApi}" => extractMethod(x.toString)
     }

     implicit val unliftablePath: Unliftable[Path] = Unliftable[Path] {
       case q"com.twitter.finagle.http.path.Path.apply(${x: String})" => Path(x.toString)
       case q"com.twitter.finagle.http.path.Root./(${x: String})" =>  Path(x)
       case q"com.twitter.finagle.http.path.Root" => Root
       case q"org.finagle.annotated.router.PathImplicits.String2Path(${x: String})" => Path(x.toString)
     }

     val annotations = currentMirror.classSymbol(clazz).asClass.annotations

     val annotation = annotations.find(_.tree.children.head.toString contains classOf[Route].getSimpleName)

     if(annotation.isEmpty)
       throw new IllegalArgumentException(s"class ${clazz.getSimpleName} does not have @Route")

     val params = annotation.get.tree.children.tail

     val methods = params.tail map { x =>
       val q"${method: HttpMethod}" = q"$x"
       method
     }
     val y = params.head


     val q"${path: Path}" = q"$y"
     Route(path, methods:_*)
   }
 }
