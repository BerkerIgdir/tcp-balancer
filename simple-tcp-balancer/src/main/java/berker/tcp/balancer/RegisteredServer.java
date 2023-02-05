package berker.tcp.balancer;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegisteredServer(@JsonProperty("url") String url,
                               @JsonProperty("port") int port) {
}