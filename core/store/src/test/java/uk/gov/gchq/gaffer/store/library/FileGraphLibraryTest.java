/*
 * Copyright 2017-2021 Crown Copyright
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

package uk.gov.gchq.gaffer.store.library;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class FileGraphLibraryTest extends AbstractGraphLibraryTest {

    private static final String TEST_FILE_PATH = "src/test/resources/graphLibrary";
    private static final String TEST_INVALID_FINAL_PATH = "inv@lidP@th";

    @Override
    public GraphLibrary createGraphLibraryInstance() {
        return new FileGraphLibrary(TEST_FILE_PATH);
    }

    @AfterEach
    public void cleanUp() throws IOException {
        if (new File(TEST_FILE_PATH).exists()) {
            FileUtils.forceDelete(new File(TEST_FILE_PATH));
        }
    }

    @Test
    public void shouldThrowExceptionWithInvalidPath() {
        // When / Then
        assertThatIllegalArgumentException().isThrownBy(() -> new FileGraphLibrary(TEST_INVALID_FINAL_PATH)).extracting("message").isNotNull();
    }
}
