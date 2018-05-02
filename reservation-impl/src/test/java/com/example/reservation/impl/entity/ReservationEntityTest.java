package com.example.reservation.impl.entity;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.example.reservation.api.Reservation;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ReservationEntityTest {
    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testMakeReservation() {
        String listingId = UUID.randomUUID().toString();

        PersistentEntityTestDriver<ReservationCommand, ReservationEvent, ReservationState> driver =
            new PersistentEntityTestDriver<>(system, new ReservationEntity(), listingId);

        Reservation r = new Reservation(LocalDate.now().plusDays(1L), LocalDate.now().plusDays(5L));

        PersistentEntityTestDriver.Outcome<ReservationEvent, ReservationState> outcome =
            driver.run(new ReservationCommand.AddReservation(r));


        // Assert what events are emitted
        assertEquals(outcome.events().size(), 1);
        ReservationEvent.ReservationAdded reservationAdded =
            (ReservationEvent.ReservationAdded) outcome.events().get(0);
        assertEquals(reservationAdded.getListingId().toString(), listingId);
        assertEquals(reservationAdded.getReservation(), r);

        // assert command gets a response
        assertEquals(outcome.getReplies().size(), 1);
        ReservationEvent.ReservationAdded reply = (ReservationEvent.ReservationAdded) outcome.getReplies().get(0);
        assertEquals(reply.getListingId().toString(), listingId);

        // assert there are no intetrnal issues (e.g. message serialization issues)
        List<PersistentEntityTestDriver.Issue> issues = driver.getAllIssues();
        if (issues.size() > 0) {
            issues.stream().forEach(System.out::println);
        }
        assertEquals(0, issues.size());

    }

    @Test
    public void testReservingListingADoesntShowOnGetAllListingB() {
        String listingIdA = UUID.randomUUID().toString();
        String listingIdB = UUID.randomUUID().toString();

        // reserve listing A
        PersistentEntityTestDriver<ReservationCommand, ReservationEvent, ReservationState> driverA =
            new PersistentEntityTestDriver<>(system, new ReservationEntity(), listingIdA);
        Reservation r = new Reservation(LocalDate.now().plusDays(1L), LocalDate.now().plusDays(5L));
        driverA.run(new ReservationCommand.AddReservation(r));

        // check reservations on listing B
        PersistentEntityTestDriver<ReservationCommand, ReservationEvent, ReservationState> driverB =
            new PersistentEntityTestDriver<>(system, new ReservationEntity(), listingIdB);
        PersistentEntityTestDriver.Outcome<ReservationEvent, ReservationState> outcome =
            driverB.run(ReservationCommand.GetCurrentReservations.INSTANCE);

        assertEquals(0, ((List<Reservation>) outcome.getReplies().get(0)).size());

    }

}
