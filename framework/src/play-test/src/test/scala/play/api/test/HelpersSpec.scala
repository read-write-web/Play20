/*
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package play.api.test

import play.api.test._
import play.api.test.Helpers._
import play.api.mvc._
import play.api.mvc.Results._
import play.twirl.api.Content
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.specs2.mutable._

class HelpersSpec extends Specification {

  "inMemoryDatabase" should {

    "change database with a name argument" in {
      val inMemoryDatabaseConfiguration = inMemoryDatabase("test")
      inMemoryDatabaseConfiguration.get("db.test.driver") must beSome("org.h2.Driver")
      inMemoryDatabaseConfiguration.get("db.test.url") must beSome.which { url =>
        url.startsWith("jdbc:h2:mem:play-test-")
      }
    }

    "add options" in {
      val inMemoryDatabaseConfiguration = inMemoryDatabase("test", Map("MODE" -> "PostgreSQL", "DB_CLOSE_DELAY" -> "-1"))
      inMemoryDatabaseConfiguration.get("db.test.driver") must beSome("org.h2.Driver")
      inMemoryDatabaseConfiguration.get("db.test.url") must beSome.which { url =>
        """^jdbc:h2:mem:play-test([0-9-]+);MODE=PostgreSQL;DB_CLOSE_DELAY=-1$""".r.findFirstIn(url).isDefined
      }
    }
  }
  
  "charset" should {
  
    "extract a charset without whitespace from a Result as Some[String]" in {
      charset(Future.successful(Ok.withHeaders(CONTENT_TYPE -> "text/html;charset=utf-8"))) must beSome("utf-8")
    }
  
    "extract a charset with whitespace from a Result as Some[String]" in {
      charset(Future.successful(Ok.withHeaders(CONTENT_TYPE -> "text/html;\t charset=utf-8"))) must beSome("utf-8")
    }
  
    "extract a missing charset from a Result as None" in {
      charset(Future.successful(Ok.withHeaders(CONTENT_TYPE -> "text/html"))) must beNone
    }
  
  }

  "contentAsString" should {

    "extract the content from Result as String" in {
      contentAsString(Future.successful(Ok("abc"))) must_== "abc"
    }

    "extract the content from Content as String" in {
      val content = new Content {
        val body: String = "abc"
        val contentType: String = "text/plain"
      }
      contentAsString(content) must_== "abc"
    }

  }

  "contentAsBytes" should {

    "extract the content from Result as Bytes" in {
      contentAsBytes(Future.successful(Ok("abc"))) must_== Array(97, 98, 99)
    }

    "extract the content from Content as Bytes" in {
      val content = new Content {
        val body: String = "abc"
        val contentType: String = "text/plain"
      }
      contentAsBytes(content) must_== Array(97, 98, 99)
    }

  }

  "contentAsJson" should {

    "extract the content from Result as Json" in {
      val jsonResult = Ok("""{"play":["java","scala"]}""").as("application/json")
      (contentAsJson(Future.successful(jsonResult)) \ "play").as[List[String]] must_== List("java", "scala")
    }

    "extract the content from Content as Json" in {
      val jsonContent = new Content {
        val body: String = """{"play":["java","scala"]}"""
        val contentType: String = "application/json"
      }
      (contentAsJson(jsonContent) \ "play").as[List[String]] must_== List("java", "scala")
    }

  }


}
