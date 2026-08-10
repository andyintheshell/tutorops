import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class Healthcheck {
    private Healthcheck() {
    }

    public static void main(String[] args) throws Exception {
        URI endpoint = URI.create("http://127.0.0.1:8080/actuator/health");
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();
        int status = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.discarding())
                .statusCode();
        if (status != 200) {
            throw new IllegalStateException("Health endpoint returned HTTP " + status);
        }
    }
}
