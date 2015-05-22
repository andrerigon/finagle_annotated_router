package org.finagle.annotated.router

object ConsoleUtils {
  def colored(s: String, color: String, options: String*) = color + options.mkString("") + s + Console.RESET
}
