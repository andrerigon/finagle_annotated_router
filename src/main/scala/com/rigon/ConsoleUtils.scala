package com.rigon

object ConsoleUtils {
  def colored(s: String, color: String, options: String*) = color + options.mkString("") + s + Console.RESET
}
