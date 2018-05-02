package com.example.reservation.impl.entity;

import com.example.reservation.api.Reservation;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.UUID;

/**
 *
 */
public interface ReservationEvent extends Jsonable , AggregateEvent<ReservationEvent> {


    int NUM_SHARDS = 4;
    AggregateEventShards<ReservationEvent> TAG = AggregateEventTag.sharded(ReservationEvent.class, NUM_SHARDS);

    @Override
    default AggregateEventTagger<ReservationEvent> aggregateTag() {
        return TAG;
    }



    @Value
    class ReservationAdded implements ReservationEvent {
        UUID uuid;
        UUID listingId;
        Reservation reservation;
    }
}

