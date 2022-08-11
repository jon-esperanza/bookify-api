package repositories

import models.{ReadBook, User}

import javax.inject._
import reactivemongo.api.bson.collection.BSONCollection
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReadingRepository @Inject()(
                                   implicit executionContext: ExecutionContext,
                                   reactiveMongoApi: ReactiveMongoApi
                                 ) {
  def collection: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("users"))

  def findOne(id: String): Future[Option[User]] = {
    collection.flatMap(_.find(BSONDocument("_id" -> id), Option.empty[User]).one[User])
  }

  def addBook(id: String, book: ReadBook): Future[WriteResult] = {
    collection.flatMap(_.update(ordered = false).one(BSONDocument("_id" -> id), BSONDocument("$push" -> BSONDocument("readingHistory" -> book))))
  }

  def removeBook(id: String, bookId: String): Future[WriteResult] = {
    collection.flatMap(_.update(ordered = false).one(BSONDocument("_id" -> id), BSONDocument("$pull" -> BSONDocument("readingHistory" -> BSONDocument("_id" -> bookId)))))
  }
}
