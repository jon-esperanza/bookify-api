package models


import play.api.libs.json.{Format, Json}
import models.ReadBook

case class BookPageCount(
                var books: Int = 0,
                var pages: Int = 0,
                var index: Option[Int] = Option.empty,
            )
object BookPageCount {
  implicit val fmt: Format[BookPageCount] = Json.format[BookPageCount]
}

case class RatingTotal(
                var rating: Int = 0,
                var total: Int = 0,
            )
object RatingTotal {
  implicit val fmt: Format[RatingTotal] = Json.format[RatingTotal]
}

case class GenreAvg(
                var genre: String,
                var avgRating: Float
)
object GenreAvg {
  implicit val fmt: Format[GenreAvg] = Json.format[GenreAvg]
}

case class AuthorAvg(
                var author: String,
                var avgRating: Float
)
object AuthorAvg {
  implicit val fmt: Format[AuthorAvg] = Json.format[AuthorAvg]
}

case class Totals(
                var books: Int = 0,
                var pages: Int = 0,
                var genres: Int = 0,
                var authors: Int = 0
)
object Totals {
  implicit val fmt: Format[Totals] = Json.format[Totals]
}


case class Insights(
                var bookPageYTD: Map[String, BookPageCount] = Map[String,BookPageCount](),
                var top5Books: List[ReadBook] = List(),
                var top5Genres: List[GenreAvg] = List(),
                var top5Authors: List[AuthorAvg] = List(),
                var totals: Totals = Totals()
)
object Insights {
  implicit val fmt: Format[Insights] = Json.format[Insights]
}
