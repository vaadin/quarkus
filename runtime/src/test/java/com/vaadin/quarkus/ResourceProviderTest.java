package com.vaadin.quarkus;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.internal.JacksonUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

public class ResourceProviderTest {

    @Test
    public void quarkusResourceProvider_returnsExpectedResources()
            throws IOException {
        QuarkusResourceProvider resourceProvider = new QuarkusResourceProvider();
        // ======== resourceProvider.getApplicationResource(s)(String)
        URL applicationResource = resourceProvider
                .getApplicationResource("resource-provider/some-resource.json");

        Assertions.assertNotNull(applicationResource);

        List<URL> resources = resourceProvider.getApplicationResources(
                "resource-provider/some-resource.json");

        Assertions.assertEquals(1, resources.size());

        Assertions.assertNotNull(resources.get(0));

        URL nonExistent = resourceProvider
                .getApplicationResource("resource-provider/non-existent.txt");

        Assertions.assertNull(nonExistent);

        // =========== resourceProvider.getClientResource

        URL clientResource = resourceProvider
                .getClientResource("resource-provider/some-resource.json");

        Assertions.assertNotNull(clientResource);

        InputStream stream = resourceProvider.getClientResourceAsStream(
                "resource-provider/some-resource.json");

        String content = IOUtils.readLines(stream, StandardCharsets.UTF_8)
                .stream().collect(Collectors.joining("\n"));
        ObjectNode object = JacksonUtils.readTree(content);
        Assertions.assertTrue(object.get("client-resource").asBoolean());
    }
}
