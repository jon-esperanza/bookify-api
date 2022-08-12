package models

import play.api.libs.json.{Format, Json}
import reactivemongo.bson.{BSONArray, BSONDocument, BSONDocumentReader, BSONDocumentWriter}

case class ReadBook(
                     _id: String,
                     title: String,
                     author: String,
                     genre: String,
                     pages: Int,
                     image: String,
                     rating: Int,
                     dateCompleted: String
                   )

object ReadBook {
  implicit val fmt: Format[ReadBook] = Json.format[ReadBook]

  implicit object ReadBookBSONReader extends BSONDocumentReader[ReadBook] {
    def read(doc: BSONDocument): ReadBook = {
      ReadBook(
        doc.getAs[String]("_id").get,
        doc.getAs[String]("title").get,
        doc.getAs[String]("author").get,
        doc.getAs[String]("genre").get,
        doc.getAs[Int]("pages").get,
        doc.getAs[String]("image").get,
        doc.getAs[Int]("rating").get,
        doc.getAs[String]("dateCompleted").get
      )
    }
  }

  implicit object ReadBookBSONWriter extends BSONDocumentWriter[ReadBook] {
    def write(book: ReadBook): BSONDocument = {
      BSONDocument(
        "_id" -> book._id,
        "title" -> book.title,
        "author" -> book.author,
        "genre" -> book.genre,
        "pages" -> book.pages,
        "image" -> book.image,
        "rating" -> book.rating,
        "dateCompleted" -> book.dateCompleted
      )
    }
  }
}