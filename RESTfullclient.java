package serverpackage;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public class RESTfullclient {

  private WebTarget webTarget;
  private static Client client;
  private static final String BASE_URI = "http://34.236.3.111:8080/mavenproject2/webresources";

  static int NumberOfThreads =0;   //Variable to calculate total number of threads.
  static AtomicInteger NumberOfRequestSent = new AtomicInteger(0);    //Variable to calculate Number of request sent to the server.
  static AtomicInteger SuccessfullRequest = new AtomicInteger(0);     //Variable to Calculate Number of Successfull request.

  /**
   * Syncronized method to calculate total number of threads.
   * Increment by everytime a thread gets created.
   *
   */
  static synchronized void totalNumberOfThreads(){
    NumberOfThreads++;
  }

  /**
   * Syncronized method to calculate total number of request
   * sent to the server.
   * Increment by 1 everytime a request is sent.
   */
  static synchronized void totalNumberOfRequestSent(){
    NumberOfRequestSent.addAndGet(1);
  }

  /**
   * Syncronized method to calculate total number of successfull
   * request.
   * Increment by 1 at every successfull request.
   */
  static synchronized void totalSuccessfullRequest(){
    SuccessfullRequest.addAndGet(1);
  }

  static final CopyOnWriteArrayList<Thread> ThreadList = new CopyOnWriteArrayList<>();
  static final CopyOnWriteArrayList<Long> Latency = new CopyOnWriteArrayList<>();  //CopyOnWriteArrayList to store Latency for each request.
  static final CopyOnWriteArrayList<Long> RequestTime = new CopyOnWriteArrayList<>();    //CopyOnWriteArrayList to store requesttime for each request.

  public static void main(String[] args) throws InterruptedException {
    System.out.println(args[0]);      //Command line arugument representing number of threads.
    System.out.println(args[1]);      //Command line arugument representing number of iterations.
    System.out.println(args[2]);      //Command line arugument representing IP address.
    System.out.println(args[3]);      //Command line arugument representing port used on server.

    long clientStartTime = System.currentTimeMillis();
    final RESTfullclient client =  new RESTfullclient();      //Client Creation.

    long startTimestamp = System.currentTimeMillis();
    for (int i = 0; i < Integer.parseInt(args[0]); i++) {
        Thread ti = new Thread(new Runnable() {             //Thread Creation.

          @Override
          public void run() {
            totalNumberOfThreads();
            for (int i = 0; i < Integer.parseInt(args[1]); i++) {

              long firstRequestStartTime = System.currentTimeMillis();
              RequestTime.add(firstRequestStartTime);
              if(client.getStatus().equals("alive")){
                totalSuccessfullRequest();
              }
              long firstRequestEndTime = System.currentTimeMillis();
              Latency.add(firstRequestEndTime - firstRequestStartTime);
              totalNumberOfRequestSent();

              long secondRequestStartTime = System.currentTimeMillis();
              RequestTime.add(secondRequestStartTime);
              if(client.getStatus().equals("alive")){
                totalSuccessfullRequest();
              }
              long secondRequestEndTime = System.currentTimeMillis();
              Latency.add(secondRequestEndTime - secondRequestStartTime);
              totalNumberOfRequestSent();
            }
          }
        });
        ThreadList.add(ti);
        ti.start();
      }
      long threadEndTime = System.currentTimeMillis();

    for(Thread t: ThreadList) {
        t.join();
    }

    long endTimestamp =  System.currentTimeMillis();
    long totalTimestamp = endTimestamp - startTimestamp;

    client.close();
    
    System.out.println("Client Starting Time"+clientStartTime);
    System.out.println("Threads Complete Time :"+ threadEndTime);
    System.out.println("Number of Threads :"+ NumberOfThreads);
    System.out.println("Total number of request Sent:"+NumberOfRequestSent);
    System.out.println("Toatl number of Successfull Request:"+SuccessfullRequest);
    System.out.println("Wall Time :"+totalTimestamp/1000+"sec");

    OptionalDouble meanLetency = mean();
    sortLatency();
    Long medianLetancy = median();
    int nintynith = nintyNinthPercentile();
    int nintyfifth = nintyFifthPercentile();

    System.out.println("Mean latency for for all request :"+meanLetency.getAsDouble());
    System.out.println("Median latency for all request:"+medianLetancy);
    System.out.println("99th percentile latency:"+Latency.get(nintynith));
    System.out.println("95th percentile latency:"+Latency.get(nintyfifth));

    // Please, do not remove this line from file template, here invocation of web service will be inserted
}

  /**
   * Method to calculate mean latency.
   */
  public static OptionalDouble mean(){
    OptionalDouble mean = Latency.stream().mapToDouble(a->a).average();
    return mean;
  }

  /**
   * Method to sort the latency
   */
  public static void sortLatency(){
    Object[] a = Latency.toArray();
    Arrays.sort(a);
    for (int i = 0; i < a.length; i++) {
      Latency.set(i, (Long) a[i]);
    }
  }

  /**
   *Method to calculate median latency.
   */
  public static long median(){
    long medianLetancy;
    if (Latency.size() % 2 == 0) {
      medianLetancy = ( Latency.get(Latency.size() / 2) + Latency.get(Latency.size() / 2 - 1)) / 2;
    }
    else {
      medianLetancy =  Latency.get(Latency.size() / 2);
    }
    return medianLetancy;
  }

  /**
   * Method to calculate 99th percentile latency
   */
  public static int nintyNinthPercentile(){
    int nintynine = (Latency.size()/100)*(100-99);
    return nintynine;
  }

  /**
   *Methos to calculate 95th percentile.
   */
  public static int nintyFifthPercentile(){
    int nintyFive = (Latency.size()/100)*(100-95);
    return nintyFive;
  }

  public RESTfullclient() {
    client = javax.ws.rs.client.ClientBuilder.newClient();
    webTarget = client.target(BASE_URI).path("myresource");
  }

  public <T> T postText(Object requestEntity, Class<T> responseType) throws
          ClientErrorException {
    return
            webTarget.request(javax.ws.rs.core.MediaType.TEXT_PLAIN)
                    .post(javax.ws.rs.client.Entity.entity(requestEntity,
                            javax.ws.rs.core.MediaType.TEXT_PLAIN),
                            responseType);
  }

  public String getStatus() throws ClientErrorException {
  // WebTarget resource = webTarget;
    return webTarget.request(javax.ws.rs.core.MediaType.TEXT_PLAIN).get(String.class);
  }

 public void close() {
   client.close();
  }

}

