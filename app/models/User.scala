package models

import play.api.libs.json.{Format, Json}
import models.{ReadBook, Collection}
import reactivemongo.bson.{BSONArray, BSONDocument, BSONDocumentReader, BSONDocumentWriter}

case class User(
                 _id: String,
                 readingHistory: List[ReadBook] = List(),
                 collections: List[Collection] = List(Collection("Future Reads"))
               )
object User {
  implicit val fmt: Format[User] = Json.format[User]

  implicit object UserBSONReader extends BSONDocumentReader[User] {
    def read(doc: BSONDocument): User = {
      User(
        doc.getAs[String]("_id").get,
        doc.getAs[BSONArray]("readingHistory").get.toMap.toList.map(book => {
          ReadBook.ReadBookBSONReader.read(book._2.asInstanceOf[BSONDocument])
        }),
        doc.getAs[BSONArray]("collections").get.toMap.toList.map(collection => {
          Collection.CollectionBSONReader.read(collection._2.asInstanceOf[BSONDocument])
        })
      )
    }
  }

  implicit object UserBSONWriter extends BSONDocumentWriter[User] {
    def write(user: User): BSONDocument = {
      BSONDocument(
        "_id" -> user._id,
        "readingHistory" -> BSONArray(user.readingHistory.map {
          book => ReadBook.ReadBookBSONWriter.write(book)
        }),
        "collections" -> BSONArray(user.collections.map {
          collection => Collection.CollectionBSONWriter.write(collection)
        })
      )
    }
  }
}
