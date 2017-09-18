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

package uk.gov.gchq.gaffer.doc.dev.aggregator;

import uk.gov.gchq.gaffer.data.element.comparison.ComparableOrToStringComparator;
import uk.gov.gchq.koryphe.binaryoperator.KorypheBinaryOperator;
import uk.gov.gchq.koryphe.tuple.n.Tuple2;

import java.util.Comparator;

public class ExampleTuple2BinaryOperator extends KorypheBinaryOperator<Tuple2<Comparable, Object>> {
    private static final Comparator<Object> COMPARATOR = new ComparableOrToStringComparator();

    @Override
    protected Tuple2<Comparable, Object> _apply(final Tuple2<Comparable, Object> tuple1, final Tuple2<Comparable, Object> tuple2) {
        if (COMPARATOR.compare(tuple1.get0(), tuple2.get0()) >= 0) {
            return tuple1;
        }
        return tuple2;
    }
}