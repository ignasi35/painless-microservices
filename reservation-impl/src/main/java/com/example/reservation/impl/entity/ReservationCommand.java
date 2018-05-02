package com.example.reservation.impl.entity;

import akka.Done;
import com.example.reservation.api.Reservation;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.List;

public interface ReservationCommand extends Jsonable {

    @Value
    class AddReservation implements ReservationCommand, PersistentEntity.ReplyType<ReservationEvent.ReservationAdded> {
        Reservation reservation;
    }

    enum GetCurrentReservations implements ReservationCommand, PersistentEntity.ReplyType<List<Reservation>> {
        INSTANCE
    }

}
