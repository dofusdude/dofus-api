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

package de.dofusdu.dto;

import de.dofusdu.entity.Item;

import javax.json.bind.annotation.JsonbProperty;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class ItemBasicDTO {
    @JsonbProperty("ankama_id")
    public Long ankamaId;
    @JsonbProperty("item_url")
    public String url;

    public ItemBasicDTO() {
    }

    public ItemBasicDTO(Long ankamaId, String url) {
        this.ankamaId = ankamaId;
        this.url = url;
    }

    public static ItemBasicDTO from(Item item, String language, URI baseUri) {
        URI uri = UriBuilder.fromUri(baseUri)
                .path(language)
                .path(item.getItemType())
                .path(item.getAnkamaId().toString())
                .build();
        return new ItemBasicDTO(item.getAnkamaId(), uri.toString());
    }
}
