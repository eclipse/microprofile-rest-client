/*
 * Copyright 2017 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.microprofile.rest.client.tck.providers;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class MultiTypedProvider implements ClientRequestFilter, ClientResponseFilter, MessageBodyReader, MessageBodyWriter,
    ReaderInterceptor, WriterInterceptor, ResponseExceptionMapper, ParamConverterProvider{
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {

    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {

    }

    @Override
    public boolean isReadable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return false;
    }

    @Override
    public Object readFrom(Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders,
                           InputStream entityStream) throws IOException, WebApplicationException {
        return null;
    }

    @Override
    public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return false;
    }

    @Override
    public long getSize(Object o, Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(Object o, Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {

    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        return null;
    }

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        return null;
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {

    }

    @Override
    public Throwable toThrowable(Response response) {
        return null;
    }
}
