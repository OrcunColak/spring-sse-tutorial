# Read Me First

The original idea is from  
https://medium.com/globant/server-sent-events-in-spring-boot-6a2c512a5475

# home.html

The original idea is from  
https://medium.com/@cagataygokcel/building-a-real-time-todo-list-with-reactive-streams-in-spring-884bb640dee1

# How SSE Works Under the Hood

https://medium.com/@ShantKhayalian/server-sent-events-sse-and-long-polling-in-spring-boot-real-time-data-without-websockets-da3b3a4bb4f5

SSE leverages the HTTP/1.1 protocol to maintain an open connection between the server and the client. Here’s how the
flow works:

- The client establishes an HTTP connection to the server using the EventSource API.
- The server keeps this connection open and streams data to the client using a text-based format, where each message is
  separated by two line breaks (\n\n).
- The client continuously listens for incoming data and processes it in real-time.

SSE uses a special Content-Type header (text/event-stream) to signal that the response will be a stream of events
rather than a regular HTTP response.

Example output

```
HTTP/1.1 200 OK
Content-Type: text/event-stream
Cache-Control: no-cache
Connection: keep-alive

id: 1
event: update
data: {"message": "SSE event sent!"}

id: 2
data: {"message": "Another event"}
```

# Client Side Automatic Reconnection

The EventSource object automatically tries to reconnect when a connection is lost. However, you can customize the
reconnection behavior by sending the retry field from the server, which tells the client how many milliseconds to wait
before trying again.

# Event ID for Resuming After a Disconnect

To improve resilience, you can implement event IDs. The client sends the last received event ID via the Last-Event-ID
header during reconnection, allowing the server to resend missed events. This prevents data loss during reconnections:

```
@GetMapping("/events")
public SseEmitter streamEvents(@RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
SseEmitter emitter = new SseEmitter();

  // If the client sends a Last-Event-ID, handle retransmission of missed events
  if (lastEventId != null) {
    // Logic to fetch and resend missed events
  }
    
  // Rest of the event streaming logic
  return emitter;
}
```

# SSE Scalability

Key Scalability Considerations:

- Threading Model: Each open SSE connection ties up a server thread, which may limit scalability. To avoid this
  bottleneck, Spring uses SseEmitter, which is asynchronous and prevents blocking.
- Load Balancing: SSE works best in sticky session setups because the client holds a long-lived connection with the
  server. If your infrastructure is distributed (e.g., multiple instances behind a load balancer), it’s crucial to
  maintain session affinity.
- Connection Timeouts: Keeping connections open for a long time can lead to stale connections. Configuring proper
  timeouts
  on your load balancer and server is essential.

```
@GetMapping("/stocks")
public SseEmitter streamStockPrices() {
    SseEmitter emitter = new SseEmitter(0L); // No timeout
    
    Executors.newSingleThreadExecutor().execute(() -> {
        try {
            while (true) {
                String stockData = fetchStockPrices(); // Fetch real-time stock data
                emitter.send(stockData);
                Thread.sleep(1000);
            }
        } catch (Exception ex) {
            emitter.completeWithError(ex);
        }
    });
    
    return emitter;
}
```