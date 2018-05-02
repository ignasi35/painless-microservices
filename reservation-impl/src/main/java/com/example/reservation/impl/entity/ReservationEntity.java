package com.example.reservation.impl.entity;

import com.example.reservation.api.Reservation;
import com.example.reservation.impl.entity.ReservationCommand.AddReservation;
import com.example.reservation.impl.entity.ReservationCommand.GetCurrentReservations;
import com.example.reservation.impl.entity.ReservationEvent.ReservationAdded;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import org.pcollections.PSequence;

import java.util.Optional;
import java.util.UUID;

public class ReservationEntity extends PersistentEntity<ReservationCommand, ReservationEvent, ReservationState> {
    @Override
    public Behavior initialBehavior(Optional<ReservationState> snapshotState) {

        BehaviorBuilder behaviorBuilder = newBehaviorBuilder(snapshotState.orElse(ReservationState.EMPTY));

        behaviorBuilder
            .setReadOnlyCommandHandler(
                GetCurrentReservations.class,
                this::getCurrentReservations
            );

        behaviorBuilder
            .setCommandHandler(
                AddReservation.class,
                this::addReservation
            );

        behaviorBuilder
            .setEventHandler(
                ReservationAdded.class,
                this::reservationAdded
            );

        return behaviorBuilder.build();
    }


    // NOTE: the code from this line to the EOF is buggy on purpose since the talk this repo
    // accompanies covers the bug resolution.
    private void getCurrentReservations(GetCurrentReservations cmd, ReadOnlyCommandContext ctx) {
        ctx.reply(state().getReservations());
    }

    private Persist addReservation(AddReservation cmd, CommandContext ctx) {
        ReservationAdded added = new ReservationAdded(
            UUID.randomUUID(),
            UUID.fromString(entityId()),
            cmd.getReservation()
        );
        return ctx.thenPersist(added, (evt) -> ctx.reply(evt));
    }

    private ReservationState reservationAdded(ReservationAdded event) {
        PSequence<Reservation> reservations = state().getReservations();
        return new ReservationState(reservations.plus(event.getReservation()));
    }


}
