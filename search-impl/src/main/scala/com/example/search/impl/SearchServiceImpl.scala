package com.example.search.impl

import java.time.LocalDate
import java.util.UUID

import akka.Done
import akka.actor.{ Actor, ActorSystem, Props, Status }
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.example.reservation.api.{ ReservationAdded, ReservationService }
import com.example.search.api.{ ListingSearchResult, SearchService }
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import play.api.libs.json.{ Format, Json }

import scala.concurrent.duration._

/**
  * Implementation of the SearchService.
  */
class SearchServiceImpl(reservationService: ReservationService, actorSystem: ActorSystem) extends SearchService {

  import SearchActor._

  private val searchActor = actorSystem.actorOf(Props[SearchActor])
  implicit val searchActorTimeout = Timeout(10.seconds)

  reservationService
    .reservationEvents
    .subscribe
    .withGroupId(UUID.randomUUID().toString)
    .atLeastOnce(Flow[ReservationAdded].mapAsync(1) { reservation =>
      (searchActor ? reservation).mapTo[Done]
    })

  override def searchListings(checkin: LocalDate, checkout: LocalDate) = ServiceCall { _ =>
    (searchActor ? Search(checkin, checkout)).mapTo[Seq[ListingSearchResult]]
  }

  override def listingName(listingId: UUID) = ServiceCall { _ =>
    (searchActor ? ListingName(listingId)).mapTo[String]
  }
}


private object SearchActor {
  case class Search(checkin: LocalDate, checkout: LocalDate)
  case class ListingName(listingId: UUID)
}

private class SearchActor extends Actor {

  import SearchActor._

  val repo = new SearchRepository

  override def receive = {

    case reservation: ReservationAdded =>
      sender() ! repo.add(reservation)

    case Search(checkin, checkout) =>
      sender() ! repo.search(checkin, checkout)

    case ListingName(listingId) =>
      repo.name(listingId) match {
        case Some(name) => sender() ! name
        case None => sender() ! Status.Failure(NotFound(s"Listing $listingId not found"))
      }
  }
}

/**
  * Not at all an efficient index, but this is a demo and this code isn't the subject of the demo
  */
private class SearchRepository {


  private var reservations: Map[UUID, ListingIndex] =
    Seq(
      ListingSearchResult(
        UUID.fromString("673500f8-1068-4866-bb86-02a9f0011296"),
        "Beach house with wonderful views", "beachhouse.jpeg",
        280),
      ListingSearchResult(
        UUID.fromString("673500f8-1068-4866-bb86-02a9f0011297"),
        "Villa by the water", "villa.jpeg",
        350),
      ListingSearchResult(
        UUID.fromString("673500f8-1068-4866-bb86-02a9f0011298"),
        "Budget hotel convenient to town centre", "hotel.jpeg",
        120),
      ListingSearchResult(
        UUID.fromString("673500f8-1068-4866-bb86-02a9f0011299"),
        "Quaint country B&B", "bnb.jpeg"
        , 180)
    ).map { listing =>
      listing.listingId -> ListingIndex(listing, Set.empty)
    }.toMap

  def add(reservation: ReservationAdded): Done = {
    reservations.get(reservation.listingId) match {
      case Some(ListingIndex(listing, res)) =>
        if (res.forall(_.reservationId != reservation.reservationId)) {
          reservations += (listing.listingId -> ListingIndex(listing, res + reservation))
        }
        Done
      case None =>
        // Ignore
        Done
    }
  }

  def search(checkin: LocalDate, checkout: LocalDate): List[ListingSearchResult] = {
    reservations.values.collect {
      case ListingIndex(listing, res) if res.forall(reservationDoesNotConflict(checkin, checkout)) => listing
    }.toList
  }

  def name(listingId: UUID): Option[String] = {
    reservations.get(listingId).map(_.listing.listingName)
  }

  private def reservationDoesNotConflict(checkin: LocalDate, checkout: LocalDate)(reservationAdded: ReservationAdded): Boolean = {
    val rCheckin = reservationAdded.reservation.checkin
    val rCheckout = reservationAdded.reservation.checkout

    if (checkout.isBefore(rCheckin) || checkout == rCheckin) {
      true
    } else if (checkin.isAfter(rCheckout) || checkin == rCheckout) {
      true
    } else {
      false
    }
  }
}

private case class ListingIndex(listing: ListingSearchResult, reservations: Set[ReservationAdded])

private object ListingIndex {
  implicit val format: Format[ListingIndex] = Json.format
}