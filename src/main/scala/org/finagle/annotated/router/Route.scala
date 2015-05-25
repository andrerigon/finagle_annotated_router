package org.finagle.annotated.router

import com.twitter.finagle.http.path.{Root, Path}
import org.jboss.netty.handler.codec.http.HttpMethod

import scala.annotation.StaticAnnotation
import scala.reflect.runtime.currentMirror
import scala.reflect.runtime.universe._

case class Route(path: Path, methods: HttpMethod*) extends StaticAnnotation

object PathImplicits {
  implicit def String2Path(v : String): Path = Path(v)
}

object Route {

   def getFrom[T](clazz: Class[T]) = {
     implicit val unliftableHttpMethod: Unliftable[HttpMethod] = Unliftable[HttpMethod] {
       case q"org.jboss.netty.handler.codec.http.HttpMethod.${x: NameApi}" => HttpMethod.valueOf(x.toString)
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
