package net.andreaskluth.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

public class SampleApplication {

  private static final Logger LOG = LoggerFactory.getLogger(SampleApplication.class);

  public static void main(String[] args) throws InterruptedException {
    List<String> responses = sampleData();

    Observable<String> observables =
        Observable.from(responses).flatMap(res -> new DemoCommand(res).observe());

    List<String> listBlockingObservable = observables.toList().toBlocking().single();
    LOG.info("Got results: {}", listBlockingObservable);

    LOG.info("Let the interrupted stuff time to log.");
    Thread.sleep(1000);
  }

  private static List<String> sampleData() {
    List<String> responses = new ArrayList<>();
    responses.add("AAA");
    responses.add("A");
    responses.add("B");
    responses.add("C");
    responses.add("CCC");
    responses.add("E");
    return responses;
  }
}

class DemoCommand extends HystrixCommand<String> {

  private static final Logger LOG = LoggerFactory.getLogger(DemoCommand.class);

  private final String response;

  protected DemoCommand(String response) {
    super(HystrixCommandGroupKey.Factory.asKey("demoCommand"));
    this.response = response;
  }

  protected String run() {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    LOG.info("Command {} is running on Thread: {}", response, Thread.currentThread().getName());
    if (response.length() > 2) {
      try {
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        // Just ignore the interrupt and don't set it, intentionally bad behaviour.
        LOG.warn("Interrupted after {}.", stopWatch.toString());
      }
    }
    LOG.info(
        "Command {} finished running on Thread: {}", response, Thread.currentThread().getName());
    return response;
  }

  @Override
  protected String getFallback() {
    return "Fallback";
  }
}
