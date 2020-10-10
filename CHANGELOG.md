# Vertx Logging Interceptor Changelog

## 0.1.0
* Initial version:
  - Supports for arbitrary log interceptors
  - Built-in interceptor that produces a metric (counter `vertx_thread_blocked`) when Vert.x detects that a thread
    is "blocked" - Based on the output of `BlockedThreadChecker`.
