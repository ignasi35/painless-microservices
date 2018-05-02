package com.example.reservation.impl;

import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.javadsl.Source;
import com.example.reservation.api.Reservation;
import com.example.reservation.api.ReservationAdded;
import com.example.reservation.api.ReservationService;
import com.example.reservation.impl.entity.ReservationCommand;
import com.example.reservation.impl.entity.ReservationEntity;
import com.example.reservation.impl.entity.ReservationEvent;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

public class ReservationServiceImpl implements ReservationService {


    private PersistentEntityRegistry persistentEntityRegistry;

    @Inject
    public ReservationServiceImpl(PersistentEntityRegistry persistentEntityRegistry) {
        this.persistentEntityRegistry = persistentEntityRegistry;
        persistentEntityRegistry.register(ReservationEntity.class);
    }

    @Override
    public ServiceCall<Reservation, ReservationAdded> reserve(UUID listingId) {
        return reservation ->
            getReservationEntity(listingId)
                .ask(new ReservationCommand.AddReservation(reservation))
                .thenApply(this::mapToApi);
    }

    @Override
    public ServiceCall<NotUsed, List<Reservation>> getCurrentReservations(UUID listingId) {
        return notUsed ->
            getReservationEntity(listingId)
                .ask(ReservationCommand.GetCurrentReservations.INSTANCE);
    }

    @Override
    public Topic<ReservationAdded> reservationEvents() {
        return TopicProducer.taggedStreamWithOffset(ReservationEvent.TAG.allTags(), this::publishingStream);
    }

    //--------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------

    private Source<Pair<ReservationAdded, Offset>, ?> publishingStream(AggregateEventTag<ReservationEvent> tag, Offset offset) {
        return persistentEntityRegistry
            .eventStream(tag, offset)
            .map(
                eventWithOffset ->
                {
                    ReservationAdded first = mapToApi(eventWithOffset.first());
                    return new Pair<>(first, eventWithOffset.second());
                });
    }


    private ReservationAdded mapToApi(ReservationEvent event) {
        ReservationEvent.ReservationAdded added = (ReservationEvent.ReservationAdded) event;
        return new ReservationAdded(added.getUuid(), added.getListingId(), added.getReservation());
    }

    private PersistentEntityRef<ReservationCommand> getReservationEntity(UUID listing) {
        return persistentEntityRegistry.refFor(ReservationEntity.class, listing.toString());
    }
}
