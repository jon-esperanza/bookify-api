package repositories

import models.User
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.commands.WriteResult

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject()(
                                   implicit executionContext: ExecutionContext,
                                   reactiveMongoApi: ReactiveMongoApi
                                 ) {
  def collection: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("users"))

  def create(user: User): Future[WriteResult] = {
    collection.flatMap(_.insert(ordered = false)
      .one(user.copy()))
  }
}
