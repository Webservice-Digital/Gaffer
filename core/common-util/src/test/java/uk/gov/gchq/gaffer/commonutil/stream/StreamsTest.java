/*
 * Copyright 2016-2020 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.gaffer.commonutil.stream;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.Closeable;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class StreamsTest {

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCloseIteratorWhenStreamIsClosed() throws Throwable {
        // Given
        final Iterator<String> iterator = mock(Iterator.class, Mockito.withSettings().extraInterfaces(Closeable.class));
        given(iterator.hasNext()).willReturn(true, false);
        final String first = "first item";
        given(iterator.next()).willReturn(first, null, null);

        // When
        final Object firstResult;
        try (final Stream<?> stream = Streams.toStream(iterator)) {
            firstResult = stream.findFirst().orElseThrow(RuntimeException::new);
        }

        // Then
        assertEquals(first, firstResult);
        verify((Closeable) iterator).close();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCloseIterableWhenStreamIsClosed() throws Throwable {
        // Given
        final Iterable<String> iterable = mock(Iterable.class, Mockito.withSettings().extraInterfaces(Closeable.class));
        final Iterator<String> iterator = mock(Iterator.class, Mockito.withSettings().extraInterfaces(Closeable.class));

        given(iterable.spliterator()).willReturn(Spliterators.spliteratorUnknownSize(iterator, 0));
        given(iterator.hasNext()).willReturn(true, false);
        final String first = "first item";
        given(iterator.next()).willReturn(first, null, null);

        // When
        final Object firstResult;
        try (final Stream<?> stream = Streams.toStream(iterable)) {
            firstResult = stream.findFirst().orElseThrow(RuntimeException::new);
        }

        // Then
        assertEquals("first item", firstResult);
        verify((Closeable) iterable).close();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCloseIteratorWhenParallelStreamIsClosed() throws Throwable {
        // Given
        final Iterator<String> iterator = mock(Iterator.class, Mockito.withSettings().extraInterfaces(Closeable.class));
        given(iterator.hasNext()).willReturn(true, false);
        final String first = "first item";
        given(iterator.next()).willReturn(first, null, null);

        // When
        final Object firstResult;
        try (final Stream<?> stream = Streams.toParallelStream(iterator)) {
            firstResult = stream.findFirst().orElseThrow(RuntimeException::new);
        }

        // Then
        assertEquals("first item", firstResult);
        verify((Closeable) iterator).close();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCloseIterableWhenParallelStreamIsClosed() throws Throwable {
        // Given
        final Iterable<String> iterable = mock(Iterable.class, Mockito.withSettings().extraInterfaces(Closeable.class));
        final Iterator<String> iterator = mock(Iterator.class, Mockito.withSettings().extraInterfaces(Closeable.class));
        given(iterable.spliterator()).willReturn(Spliterators.spliteratorUnknownSize(iterator, 0));
        given(iterator.hasNext()).willReturn(true, false);
        final String first = "first item";
        given(iterator.next()).willReturn(first, null, null);

        // When
        final Object firstResult;
        try (final Stream<?> stream = Streams.toParallelStream(iterable)) {
            firstResult = stream.findFirst().orElseThrow(RuntimeException::new);
        }

        // Then
        assertEquals("first item", firstResult);
        verify((Closeable) iterable).close();
    }
}
