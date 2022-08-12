package controllers

import models.{Collection, User, CollectionBook}
import play.api.libs.json.JsResult.Exception
import play.api.libs.json._
import play.api.mvc._
import repositories.CollectionsRepository

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

/* 
  Controller for CRUD requests for User Collections

  GET  getCollections(user_id): 
      returns user collections
  POST createCollection(user_id, name): 
      creates a collection for user
  PUT  editCollection(user_id, collection_id, name):
      edit a collection's name
  DEL  removeCollection(user_id, collection_id): 
      removes collection for user
  POST addBook(user_id, collection_id, book(book_id, book_title, book_author, book_image)):
      adds a book to collection
  DEL  removeBook(collection_id, book_id):
      removes a book from colleciton
*/

@Singleton
class CollectionsController @Inject()(
                                implicit executionContext: ExecutionContext,
                                val collectionsRepository: CollectionsRepository,
                                val controllerComponents: ControllerComponents) extends BaseController {
    def getCollections(id: String): Action[AnyContent] = Action.async { implicit request =>
        collectionsRepository.findOne(id).map(
            user => {
            if (user == None) {
                NotFound("User with id " + id + " not found.")
            } else {
                Ok(Json.toJson(user.get.collections))
            }
        })
    }

    def createCollection(id: String): Action[JsValue] = Action.async(parse.json) { implicit request => 
        request.body.validate[Collection].fold(
            _ => Future.successful(BadRequest("Cannot parse request body")),
            collection => collectionsRepository.createCollection(id, collection).map {
                result => {
                    result.n match {
                        case 0 => NotFound("User with id " + id + " not found.")
                        case 1 => Ok("Successfully created collection.")
                    }
                }
            }
        )
    }

    def editCollection(id: String): Action[JsValue] = Action.async(parse.json) { implicit request => 
        request.body.validate[Collection].fold(
            _ => Future.successful(BadRequest("Cannot parse request body")),
            collection => collectionsRepository.editCollection(id, collection._id, collection.name).map {
                result => {
                    result.n match {
                        case 0 => NotFound("User with id " + id + " not found.")
                        case 1 => Ok("Successfully updated collection.")
                    }
                }
            }
        )
    }

    def removeCollection(id: String, collectionId: String): Action[AnyContent] = Action.async { implicit request => 
        collectionsRepository.removeCollection(id, collectionId).map(
            result => Ok(Json.toJson(result.ok))
        )
    }

    def addBook(id: String, collectionId: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
        request.body.validate[CollectionBook].fold(
            _ => Future.successful(BadRequest("Cannot parse request body")),
            book => collectionsRepository.addBook(id, collectionId, book).map {
                result => {
                    result.n match {
                        case 0 => NotFound("User with id " + id + " not found.")
                        case 1 => Ok("Successfully added book to collection.")
                    }
                }
            }
        )
    }

    def removeBook(id: String, collectionId: String, bookId: String): Action[AnyContent] = Action.async { implicit request => 
        collectionsRepository.removeBook(id, collectionId, bookId).map(
            result => Ok(Json.toJson(result.ok))
        )
    }
}
