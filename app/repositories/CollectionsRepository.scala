package repositories

import models.{Collection, CollectionBook, User}

import javax.inject._
import reactivemongo.api.bson.collection.BSONCollection
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CollectionsRepository @Inject()(
                                   implicit executionContext: ExecutionContext,
                                   reactiveMongoApi: ReactiveMongoApi
                                 ) {
  def collectionDB: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("users"))

  def findOne(id: String): Future[Option[User]] = {
    collectionDB.flatMap(_.find(BSONDocument("_id" -> id), Option.empty[User]).one[User])
  }

  def createCollection(id: String, collection: Collection): Future[WriteResult] = {
    collectionDB.flatMap(_.update(ordered = false).one(BSONDocument("_id" -> id), BSONDocument("$push" -> BSONDocument("collections" -> collection))))
  }

  def editCollection(id: String, collectionId: String, name: String): Future[WriteResult] = {
    collectionDB.flatMap(_.update(ordered = false).one(BSONDocument("_id" -> id, "collections" -> BSONDocument("$elemMatch" -> BSONDocument("_id" -> collectionId))), BSONDocument("$set" -> BSONDocument("collections.$.name" -> name))))
  }

  def removeCollection(id: String, collectionId: String): Future[WriteResult] = {
    collectionDB.flatMap(_.update(ordered = false).one(BSONDocument("_id" -> id), BSONDocument("$pull" -> BSONDocument("collections" -> BSONDocument("_id" -> collectionId)))))
  }

  def addBook(id: String, collectionId: String, book: CollectionBook): Future[WriteResult] ={
    collectionDB.flatMap(_.update(ordered = false).one(BSONDocument("_id" -> id, "collections" -> BSONDocument("$elemMatch" -> BSONDocument("_id" -> collectionId))), BSONDocument("$push" -> BSONDocument("collections.$.books" -> book))))
  }

  def removeBook(id: String, collectionId: String, bookId: String): Future[WriteResult] = {
    collectionDB.flatMap(_.update(ordered = false).one(BSONDocument("_id" -> id, "collections" -> BSONDocument("$elemMatch" -> BSONDocument("_id" -> collectionId))), BSONDocument("$pull" -> BSONDocument("collections.$.books" -> BSONDocument("_id" -> bookId)))))
  }
}