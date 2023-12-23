package fi.iki.elonen;

public enum HttpStatusCode implements NanoHTTPD.Response.IStatus {
    OK(200),
    CREATED(201),
    ACCEPTED(202),
    NO_CONTENT(204),
    PARTIAL_CONTENT(206),

    REDIRECT_SEE_OTHER(303),
    NOT_MODIFIED(304),
    TEMPORARY_REDIRECT(307),

    BAD_REQUEST(400),
    FORBIDDEN(403),
    NOT_FOUND(404),
    NOT_ACCEPTABLE(406),
    LENGTH_REQUIRED(411),
    PAYLOAD_TOO_LARGE(413),
    RANGE_NOT_SATISFIABLE(416),

    INTERNAL_ERROR(500),
    NOT_IMPLEMENTED(501),
    SERVICE_UNAVAILABLE(503),
    INSUFFICIENT_STORAGE(507);

    private final int value;

    HttpStatusCode(int requestStatus) {
        value = requestStatus;
    }

    @Override
    public int getStatusCode() {
        return value;
    }

    @Override
    public String getDescription() {
        return value + " CSS";
    }
}
