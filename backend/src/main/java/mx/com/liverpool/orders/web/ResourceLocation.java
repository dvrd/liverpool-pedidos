package mx.com.liverpool.orders.web;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

public final class ResourceLocation {

    private ResourceLocation() {
    }

    public static URI forId(String id) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
