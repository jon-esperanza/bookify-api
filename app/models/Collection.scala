package models

import play.api.libs.json.{Format, Json}
import models.CollectionBook
import reactivemongo.bson.{BSONArray, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

case class Collection(
                 name: String,
                 books: List[CollectionBook] = List(),
                  _id: String = ""
               )
object Collection {
  implicit val fmt: Format[Collection] = Json.format[Collection]

  implicit object CollectionBSONReader extends BSONDocumentReader[Collection] {
    def read(doc: BSONDocument): Collection = {
      Collection(
        doc.getAs[String]("name").get,
        doc.getAs[BSONArray]("books").get.toMap.toList.map(book => {
          CollectionBook.CollectionBookBSONReader.read(book._2.asInstanceOf[BSONDocument])
        }),
        doc.getAs[String]("_id").get,
      )
    }
  }

  implicit object CollectionBSONWriter extends BSONDocumentWriter[Collection] {
    def write(collection: Collection): BSONDocument = {
      BSONDocument(
        "_id" -> BSONObjectID.generate().stringify,
        "name" -> collection.name,
        "books" -> BSONArray(collection.books.map {
          book => CollectionBook.CollectionBookBSONWriter.write(book)
        })
      )
    }
  }
}
