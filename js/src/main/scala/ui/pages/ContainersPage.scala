package ui.pages

import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import model.{Container, ContainersInfo}
import ui.WorkbenchRef
import ui.widgets.Alert
import util.logger._

import scala.concurrent.ExecutionContext.Implicits.global

object ContainersPage extends Page {

  val id = "Containers"

  case class State(info: ContainersInfo = ContainersInfo(), error: Option[String] = None)

  case class Props(ref: WorkbenchRef)

  case class Backend(t: BackendScope[Props, State]) {
    def willStart(): Unit = {
      t.props.ref.client.map { client =>
        client.containersInfo().map { info =>
          t.modState(s => State(info))
        }.onFailure {
          case ex: Exception =>
            log.error("Unable to get Metadata", ex)
            t.modState(s => s.copy(error = Some("Unable to get data: " + ex.getMessage)))
        }
      }
    }
  }

  def component(ref: WorkbenchRef) = {
    val props = Props(ref)
    ContainersPageRender.component(props)
  }
}

object ContainersPageRender {

  import ui.pages.ContainersPage._

  val component = ReactComponentB[Props]("ContainerPage")
    .initialState(State())
    .backend(new Backend(_))
    .render((P, S, B) => {
    vdom(S, P)
  }).componentWillMount(_.backend.willStart())
    .build

  def vdom(S: State, P: Props) = <.div(
    S.error.map(Alert(_, Some(P.ref.link(SettingsPage)))),
    table(true, "Container Running", S.info.running, P),
    table(false, "History", S.info.history, P)
  )

  def table(showLinks: Boolean, title: String, containers: Seq[Container], props: Props) =
    <.div(^.className := "container  col-sm-12",
      <.div(^.className := "panel panel-default  bootcards-summary",
        <.div(^.className := "panel-heading clearfix",
          <.h3(^.className := "panel-title pull-left")(title),
          <.a(^.className := "btn pull-right glyphicon glyphicon-refresh", ^.href := "#")
        ),
        <.table(^.className := "table table-hover",
          <.thead(
            <.tr(
              Some(<.th("Id")).filter(_ => showLinks),
              <.th("Image"),
              <.th("Command"),
              <.th("Ports"),
              <.th("Created"),
              <.th("Status")
            )
          ),
          <.tbody(
            containers.map { c =>
              <.tr(
                Some(<.td(
                  props.ref.link(ContainerPage(c.Id, props.ref))(c.id)))
                  .filter(_ => showLinks),
                <.td(c.Image),
                <.td(c.Command),
                <.td(c.ports.map(<.div(_))),
                <.td(c.created),
                <.td(c.Status)
              )
            }
          )
        )
      )
    )


}