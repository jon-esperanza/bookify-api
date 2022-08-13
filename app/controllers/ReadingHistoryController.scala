package controllers

import models.{ReadBook, User, BookPageCount, GenreGroup, RatingTotal, GenreAvg, AuthorAvg, Insights, Totals}
import java.util.Calendar
import play.api.libs.json.JsResult.Exception
import play.api.libs.json._
import play.api.mvc._
import repositories.ReadingRepository
import scala.collection.immutable.ListMap

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.mutable.ListBuffer

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

  /* 
     grab History -> clean and analyze
     
     Books and Pages over last year -> line graph  [X]
     Top 5 Books by Rating 
     Top 5 Genres by Aggregated Rating
     Top 5 Authors by Aggregated Rating
     Total: Books, Pages, Unique Genres, Unique Authors
  */
  def getInsightData(id: String): Action[AnyContent] = Action.async { implicit request => 
    readingRepository.findOne(id).map(
      user => {
        if (user == None) {
            NotFound("User with id " + id + " not found.")
          } else {
            val history = user.get.readingHistory
            val insights = generateInsights(history)
            Ok(Json.toJson(insights))
          }
      })
  }

  def generateInsights(history: List[ReadBook]) = {
            val bookPages = generateBookAndPage(history)
            val booksByRating = generateTop5Books(history)
            val genresByRating = generateTop5Genres(history)
            val authorsByRating = generateTop5Authors(history)
            val totalPages = generateTotalPages(history)
            val totals = Totals(history.size, totalPages, genresByRating.size, authorsByRating.size)
            Insights(bookPages, booksByRating.take(5), genresByRating.take(5), authorsByRating.take(5), totals)
  }

  def generateTotalPages(history: List[ReadBook]) = { 
    var pages = 0
    history.map(book => {
      pages = pages + book.pages
    });
    pages
  }

  def generateTop5Books(history: List[ReadBook]) = {
    val sorted = history.sortWith(_.rating > _.rating);
    sorted.take(5)
  }

  def generateTop5Genres(history: List[ReadBook]) = {
    var genres = scala.collection.mutable.Map[String, RatingTotal]()
    var genresBuffer = new ListBuffer[GenreAvg]()
    history.map(book => {
      if (!genres.contains(book.genre)) {
        genres.put(book.genre, RatingTotal());
      }
      var counts = genres.get(book.genre).get
      counts.rating = counts.rating + book.rating
      counts.total = counts.total + 1
    });
    for ((k, v) <- genres) {
      val avg: Float = v.rating.toFloat / v.total
      genresBuffer += GenreAvg(k, avg)
    }
    genresBuffer.toList.sortWith(_.avgRating > _.avgRating)
  }

  def generateTop5Authors(history: List[ReadBook]) = {
    var authors = scala.collection.mutable.Map[String, RatingTotal]()
    var authorsBuffer = new ListBuffer[AuthorAvg]()
    history.map(book => {
      if (!authors.contains(book.author)) {
        authors.put(book.author, RatingTotal());
      }
      var counts = authors.get(book.author).get
      counts.rating = counts.rating + book.rating
      counts.total = counts.total + 1
    });
    for ((k, v) <- authors) {
      val avg: Float = v.rating.toFloat / v.total
      authorsBuffer += AuthorAvg(k, avg)
    }
    authorsBuffer.toList.sortWith(_.avgRating > _.avgRating)
  }

  def generateBookAndPage(history: List[ReadBook]) = {
    var months = scala.collection.mutable.Map[String, BookPageCount]()
    history.map(book => {
      var parsed = book.dateCompleted.split("-")
      var formatted = formatMonthYear(parsed(1), parsed(0))
      if (!months.contains(formatted)) {
        months.put(formatted, BookPageCount());
      }
      var count = months.get(formatted).get
      count.books = count.books + 1
      count.pages = count.pages + book.pages
    })
    var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    var index = 12
    var current = formatMonthYear(currentMonth.toString(), currentYear.toString());
    if (!months.contains(current)) {
        months.put(current, BookPageCount())
    }
    var obj = months.get(current).get
    obj.index = Option(index)
    index = 11
    var i = currentMonth - 1
    while (i != currentMonth) {
      if (i <= 0) {
        i = 12
        currentYear -= 1
      }
      var check = formatMonthYear(i.toString(), currentYear.toString());
      if (!months.contains(check)) {
          months.put(check, BookPageCount());
      }
      var obj = months.get(check).get
      obj.index = Option(index)
      i -= 1
      index -= 1
    }
    ListMap(months.toSeq.sortWith(_._2.index.get > _._2.index.get): _*)
  }

  def formatMonthYear(month: String, year: String): String = {
    var short = month match {
      case "01" | "1" => "Jan "
      case "02" | "2" => "Feb "
      case "03" | "3" => "Mar "
      case "04" | "4" => "Apr "
      case "05" | "5" => "May "
      case "06" | "6" => "Jun "
      case "07" | "7" => "Jul "
      case "08" | "8" => "Aug "
      case "09" | "9" => "Sep "
      case "10" => "Oct "
      case "11" => "Nov "
      case "12" => "Dec "
    }
    short.concat(year);
  }

}
