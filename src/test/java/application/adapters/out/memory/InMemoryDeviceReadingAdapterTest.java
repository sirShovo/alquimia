package application.adapters.out.memory;

import static org.assertj.core.api.Assertions.assertThat;

import application.domain.models.DeviceReading;
import application.domain.support.Fakes;
import application.domain.valueobjects.Distance;
import application.domain.valueobjects.MessageFragment;
import application.domain.valueobjects.SurveillanceDevice;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class InMemoryDeviceReadingAdapterTest {

    private final InMemoryDeviceReadingAdapter adapter = new InMemoryDeviceReadingAdapter();

    @Test
    void storesReadingsOrderedByCatalog() {
        assertThat(adapter.saveIfAbsent(Fakes.starSatellite())).isTrue();
        assertThat(adapter.saveIfAbsent(Fakes.starLabs())).isTrue();
        assertThat(adapter.saveIfAbsent(Fakes.starLabs())).isFalse();

        assertThat(adapter.findAll()).extracting(DeviceReading::device)
                .containsExactly(SurveillanceDevice.STAR_LABS, SurveillanceDevice.STAR_SATELLITE);
        assertThat(adapter.findBy(SurveillanceDevice.CCPD)).isEmpty();
    }

    @Test
    void replaceUpdateAndDeleteOnlyWorkOnExistingReadings() {
        assertThat(adapter.replaceIfPresent(Fakes.ccpd())).isEmpty();
        assertThat(adapter.updateIfPresent(SurveillanceDevice.CCPD, r -> r)).isEmpty();
        assertThat(adapter.deleteIfPresent(SurveillanceDevice.CCPD)).isFalse();

        adapter.saveIfAbsent(Fakes.ccpd());
        DeviceReading replacement = Fakes.ccpd().withDistance(new Distance(1));
        assertThat(adapter.replaceIfPresent(replacement)).contains(replacement);
        assertThat(adapter.updateIfPresent(SurveillanceDevice.CCPD, r -> r.withDistance(new Distance(2))))
                .map(DeviceReading::distance).contains(new Distance(2));
        assertThat(adapter.deleteIfPresent(SurveillanceDevice.CCPD)).isTrue();
        assertThat(adapter.findAll()).isEmpty();
    }

    @Test
    void concurrentRegistrationsOfTheSameDeviceStoreExactlyOne() throws InterruptedException {
        AtomicInteger stored = new AtomicInteger();
        runConcurrently(50, () -> {
            if (adapter.saveIfAbsent(Fakes.ccpd())) {
                stored.incrementAndGet();
            }
        });
        assertThat(stored.get()).isEqualTo(1);
    }

    @Test
    void concurrentPartialUpdatesDoNotLoseFields() throws InterruptedException {
        adapter.saveIfAbsent(new DeviceReading(SurveillanceDevice.CCPD, new Distance(0),
                new MessageFragment(List.of())));
        runConcurrently(200, () -> adapter.updateIfPresent(SurveillanceDevice.CCPD,
                r -> r.withDistance(new Distance(r.distance().value() + 1))));

        assertThat(adapter.findBy(SurveillanceDevice.CCPD)).map(r -> r.distance().value()).contains(200.0);
    }

    private static void runConcurrently(int tasks, Runnable action) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        for (int i = 0; i < tasks; i++) {
            executor.submit(() -> {
                start.await();
                action.run();
                return null;
            });
        }
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
    }
}
