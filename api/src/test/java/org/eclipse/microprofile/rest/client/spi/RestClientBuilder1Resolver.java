package org.eclipse.microprofile.rest.client.spi;

import org.eclipse.microprofile.rest.client.BuilderImpl1;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

/**
 * A resolver that always picks BuilderImpl1 builder
 * @author Ondrej Mihalyi
 */
public class RestClientBuilder1Resolver extends RestClientBuilderResolver {

    @Override
    public RestClientBuilder newBuilder() {
        return new BuilderImpl1();
    }
    
}

