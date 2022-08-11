package controllers

import models.User
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import repositories.{ReadingRepository, UserRepository}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject()(
                                          implicit executionContext: ExecutionContext,
                                          val userRepository: UserRepository,
                                          val controllerComponents: ControllerComponents) extends BaseController {
  def createUser(id: String): Action[AnyContent] = Action.async { implicit request =>
    val user = User(id)
    userRepository.create(user).map({
      result => Ok(Json.toJson(result.ok))
    })
  }
}

