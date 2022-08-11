package controllers

import models.{ReadBook, User}
import play.api.libs.json.JsResult.Exception
import play.api.libs.json._
import play.api.mvc._
import repositories.ReadingRepository

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

/* 
  Controller for CRUD requests for User Reading History

  GET  getHistory(user_id): 
      returns user reading history
  POST addBook({user_id, book_id, date_completed, rating, genre, author}): 
      adds book to user reading history
  DEL  removeBook(user_id, book_id): 
      removes book from user reading history
*/

@Singleton
class ReadingHistoryController @Inject()(
                                implicit executionContext: ExecutionContext,
                                val readingRepository: ReadingRepository,
                                val controllerComponents: ControllerComponents) extends BaseController {

  def getHistory(id: String): Action[AnyContent] = Action.async { implicit request =>
    readingRepository.findOne(id).map(
        user => {
          if (user == None) {
            NotFound("User with id " + id + " not found.")
          } else {
            Ok(Json.toJson(user.get.readingHistory))
          }
        })
  }

  def addBook(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[ReadBook].fold(
      _ => Future.successful(BadRequest("Cannot parse request body")),
      book => readingRepository.addBook(id, book).map {
        result => {
          result.n match {
            case 0 => NotFound("User with id " + id + " not found.")
            case 1 => Ok("Successfully added book.")
          }
        }
      }
    )
  }

  def removeBook(id: String, bookId: String): Action[AnyContent] = Action.async { implicit request =>
    readingRepository.removeBook(id, bookId).map {
      result => Ok(Json.toJson(result.ok))
    }
  }
}
