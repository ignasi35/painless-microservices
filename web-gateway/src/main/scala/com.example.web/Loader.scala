package com.example.web

import com.example.reservation.api.ReservationService
import com.example.search.api.SearchService
import _root_.controllers.AssetsComponents
import com.example.web.controllers.Main
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.api.{ LagomConfigComponent, ServiceAcl, ServiceInfo }
import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.rp.servicediscovery.lagom.scaladsl.LagomServiceLocatorComponents
import com.softwaremill.macwire._
import play.api.ApplicationLoader.Context
import play.api.i18n.I18nComponents
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.{ ApplicationLoader, BuiltInComponentsFromContext, Mode }
import play.filters.HttpFiltersComponents

import scala.collection.immutable
import scala.concurrent.ExecutionContext

abstract class WebGateway(context: Context) extends BuiltInComponentsFromContext(context)
  with I18nComponents
  with AhcWSComponents
  with AssetsComponents
  with HttpFiltersComponents
  with LagomServiceClientComponents
  with LagomConfigComponent {

  override lazy val serviceInfo: ServiceInfo = ServiceInfo(
    "web-gateway",
    Map(
      "web-gateway" -> immutable.Seq(ServiceAcl.forPathRegex("(?!/api/).*"))
    )
  )
  override implicit lazy val executionContext: ExecutionContext = actorSystem.dispatcher
  override lazy val router = {
    val prefix = "/"
    wire[_root_.router.Routes]
  }

  lazy val searchService = serviceClient.implement[SearchService]
  lazy val reservationService = serviceClient.implement[ReservationService]

  lazy val main = wire[Main]
}

class WebGatewayLoader extends ApplicationLoader {
  override def load(context: Context) = context.environment.mode match {
    case Mode.Dev =>
      new WebGateway(context) with LagomDevModeComponents {}.application
    case _ =>
      new WebGateway(context) with LagomServiceLocatorComponents {}.application
  }
}
