/*
 * Copyright 2021 Christopher Sieh (stelzo@steado.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dofusdu.util;

import org.eclipse.microprofile.config.ConfigProvider;
import java.net.URI;
import java.util.Optional;

public class NgnixUriReplacer {
    public static URI replace(URI source) {
        Optional<String> optionalValue = ConfigProvider.getConfig().getOptionalValue("service.hostname", String.class);
        if (optionalValue.isEmpty()) {
            return source;
        }
        return URI.create(source.toString().replace("http://localhost:" + source.getPort(), optionalValue.get()));
    }
}
