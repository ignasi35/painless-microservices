package com.example.reservation.impl.entity;

import com.example.reservation.api.Reservation;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;


@Value
public class ReservationState implements Jsonable {

    public static final ReservationState EMPTY = new ReservationState(TreePVector.empty());

    PSequence<Reservation> reservations;

}
