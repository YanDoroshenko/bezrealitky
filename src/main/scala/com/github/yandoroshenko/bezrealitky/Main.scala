package com.github.yandoroshenko.bezrealitky

import java.io.{File, FileNotFoundException, FileWriter}

import com.typesafe.config.ConfigFactory
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Element

import scala.annotation.tailrec
import scala.io.Source
import scala.util.{Failure, Try}

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 31.05.2018.
  */
object Main extends App {

  /** Configure file locations **/
  lazy val oldFileName = ConfigFactory.load().getString("files.old")
  lazy val newFileName = ConfigFactory.load().getString("files.new")

  /** Configuration values **/
  lazy val location = ConfigFactory.load().getString("parameters.location")
  lazy val rooms = ConfigFactory.load().getString("parameters.rooms")
  lazy val maxPrice = ConfigFactory.load().getString("parameters.price.max")
  lazy val minSurface = ConfigFactory.load().getString("parameters.surface.min")
  lazy val token = ConfigFactory.load().getString("parameters.token")

  /** Create a browser to fetch data **/
  val browser = JsoupBrowser()

  /** Main work **/
  Try(Source.fromFile(oldFileName)) // Try to open the old file
    .recover {
    case _: FileNotFoundException => //Create a new one if failed
      new File(oldFileName).createNewFile()
      Source.fromFile(oldFileName)
  }
    .map(s => { // Proceed
      val old = // Read old values
        if (s.getLines().nonEmpty)
          s.getLines().toList
        else List()

      val all = load( // Recursively load all entries from page 1
        s"https://www.bezrealitky.cz/vypis/nabidka-pronajem/byt/$location/$rooms?priceTo=$maxPrice&surfaceFrom=$minSurface&_token=$token",
        1,
        List()
      ).map(_.attr("id").split("-")(1)).toList // Take only numeric IDs

      val n = all.filterNot(old.contains(_)) // Throw away old ones
      if (n.nonEmpty) { // If any new
        val newFile = new File(newFileName)
        newFile.createNewFile()
        val fw = new FileWriter(newFile) // Write them to the new file
        fw.write(n.mkString("\n"))
        fw.close()
      }
    }) match {
    case Failure(e) => println(e.getStackTrace().mkString("\n"))
    case _ => println("Done")
  }

  /** Recurse through pages until found duplicates (duplicate means that we're back to page 1) **/
  @tailrec
  def load(url: String, page: Int, docs: Seq[Element]): Seq[Element] = {
    val doc = browser.get(s"$url&page=$page")
    doc >> elementList("article") match {
      case c: List[Element] if c.nonEmpty && docs.map(_.attr("id")).intersect(c.map(_.attr("id"))).isEmpty =>
        load(url, page + 1, docs ++ c)
      case _ =>
        docs
    }
  }
}
