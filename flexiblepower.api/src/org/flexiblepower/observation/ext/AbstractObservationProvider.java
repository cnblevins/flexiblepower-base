package org.flexiblepower.observation.ext;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import org.flexiblepower.observation.Observation;
import org.flexiblepower.observation.ObservationConsumer;
import org.flexiblepower.observation.ObservationProvider;

/**
 * Gives a basic implementation of an {@link ObservationProvider} where the {@link #subscribe(ObservationConsumer)} and
 * {@link #unsubscribe(ObservationConsumer)} methods are implemented. To publish a new observation, the
 * {@link #publish(Observation)} method should be used.
 * 
 * @param <T>
 *            The type of the value
 */
public abstract class AbstractObservationProvider<T> implements ObservationProvider<T> {

    private final Set<ObservationConsumer<? super T>> consumers = new CopyOnWriteArraySet<ObservationConsumer<? super T>>();
    private final AtomicReference<Observation<? extends T>> lastObservation = new AtomicReference<Observation<? extends T>>(null);

    @Override
    public void subscribe(ObservationConsumer<? super T> consumer) {
        consumers.add(consumer);
    }

    @Override
    public void unsubscribe(ObservationConsumer<? super T> consumer) {
        consumers.remove(consumer);
    }

    @Override
    public Observation<? extends T> getLastObservation() {
        return lastObservation.get();
    }

    /**
     * Publishes an observation to all the subscribed consumers.
     * 
     * @param observation
     *            The observation that will be sent.
     */
    protected void publish(Observation<? extends T> observation) {
        lastObservation.set(observation);
        for (ObservationConsumer<? super T> consumer : consumers) {
            consumer.consume(this, observation);
        }
    }

}
