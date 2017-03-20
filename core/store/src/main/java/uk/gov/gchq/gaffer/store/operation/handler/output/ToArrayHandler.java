/*
 * Copyright 2017 Crown Copyright
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
package uk.gov.gchq.gaffer.store.operation.handler.output;

import com.google.common.collect.Lists;
import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.operation.impl.output.ToArray;
import uk.gov.gchq.gaffer.store.Context;
import uk.gov.gchq.gaffer.store.Store;
import uk.gov.gchq.gaffer.store.operation.handler.OutputOperationHandler;
import java.lang.reflect.Array;
import java.util.Collection;

public class ToArrayHandler<T> implements OutputOperationHandler<ToArray<T>, T[]> {
    @Override
    public T[] doOperation(final ToArray<T> operation, final Context context, final Store store) throws OperationException {
        return toArray(operation.getInput());
    }

    private <T> T[] toArray(final Collection<T> collection, final Class<?> clazz) {
        return toArray(collection, (T[]) Array.newInstance(clazz, collection.size()));
    }

    private <T> T[] toArray(final Iterable<T> iterable) {
        return toArray(Lists.newArrayList(iterable), iterable.iterator().next().getClass());
    }

    private <T> T[] toArray(final Collection<T> collection, final T[] array) {
        return collection.size() > array.length
                ? collection.toArray((T[]) Array.newInstance(array.getClass().getComponentType(), collection.size()))
                : collection.toArray(array);
    }

}
