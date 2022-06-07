package pages
import controllers.routes
import play.api.mvc.Call

object IndexPage extends Page {

  override def route(waypoints: Waypoints): Call =
    routes.IndexController.onPageLoad
}
