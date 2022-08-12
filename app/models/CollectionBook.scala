package models

import play.api.libs.json.{Format, Json}
import reactivemongo.bson.{BSONArray, BSONDocument, BSONDocumentReader, BSONDocumentWriter}

case class CollectionBook(
                     _id: String,
                     title: String,
                     author: String,
                     genre: String,
                     image: String,
                   )

object CollectionBook {
  implicit val fmt: Format[CollectionBook] = Json.format[CollectionBook]

  implicit object CollectionBookBSONReader extends BSONDocumentReader[CollectionBook] {
    def read(doc: BSONDocument): CollectionBook = {
      CollectionBook(
        doc.getAs[String]("_id").get,
        doc.getAs[String]("title").get,
        doc.getAs[String]("author").get,
        doc.getAs[String]("genre").get,
        doc.getAs[String]("image").get
      )
    }
  }

  implicit object CollectionBookBSONWriter extends BSONDocumentWriter[CollectionBook] {
    def write(book: CollectionBook): BSONDocument = {
      BSONDocument(
        "_id" -> book._id,
        "title" -> book.title,
        "author" -> book.author,
        "genre" -> book.genre,
        "image" -> book.image
      )
    }
  }
}