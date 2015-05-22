package com.rigon

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import mesosphere.jackson.CaseClassModule


trait JsonSupport {
  private val mapper = new ObjectMapper with ScalaObjectMapper
  mapper.disable(DeserializationFeature.WRAP_EXCEPTIONS)
  mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
  mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
  mapper.setSerializationInclusion(Include.NON_NULL)
  mapper.registerModule(DefaultScalaModule)
  mapper.registerModule(CaseClassModule)

  def addDeserializer[T](clazz: Class[T], deserializer: StdDeserializer[T]) = {
    val module = new SimpleModule(s"Module[${clazz.getSimpleName}}]").addDeserializer(clazz, deserializer)
    mapper.registerModule(module)
  }

  def toBytes(obj: Any) = mapper.writeValueAsBytes(obj)

  def toJson(obj: Any) = mapper.writeValueAsString(obj)

  def fromJson[T](json: String)(implicit m: Manifest[T]): T = {
    mapper.readValue[T](json)
  }

  def fromJson[T](json: Array[Byte])(implicit m: Manifest[T]): T = {
    mapper.readValue[T](json)
  }
}
