# Changelog

## 1.0.0 (2026-11-02)

First public release.

- Halts the JVM with exit code 1 when the server tick counter stands still for 300 seconds, with a warning at half time.
- Halts the JVM with exit code 1 when the server thread has ended and the JVM is still alive after 90 seconds.
- Both limits adjustable with `-Dtickfreezeexit.freezeSeconds` and `-Dtickfreezeexit.deadSeconds`.
- Mod icon.
