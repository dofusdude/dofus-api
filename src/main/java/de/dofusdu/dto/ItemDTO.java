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
import org.eclipse.microprofile.config.ConfigProvider;

import javax.json.bind.annotation.JsonbProperty;
import javax.ws.rs.core.UriBuilder;
import java.util.Optional;

public class ItemDTO {
    @JsonbProperty("ankama_id")
    public Long ankamaId;
    public String name;
    public String description;
    @JsonbProperty("image_url")
    public String imageUrl;
    @JsonbProperty("image_url_local")
    public String imageUrlLocal;
    @JsonbProperty("ankama_url")
    public String ankamaUrl;

    public ItemDTO(Long ankamaId, String name, String description, String imageUrl, String ankamaUrl, String itemType) {
        this.ankamaId = ankamaId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.ankamaUrl = ankamaUrl;
        Optional<String> service_hostname = ConfigProvider.getConfig().getOptionalValue("service.hostname", String.class);
        String service_host = "http://localhost";
        if (service_hostname.isPresent()) {
            service_host = service_hostname.get();
        }
        this.imageUrlLocal = UriBuilder.fromPath(service_host)
                .path("static")
                .path(itemType)
                .path(ankamaId + ".png")
                .build().toString();
    }

    public ItemDTO() {
    }

    public static ItemDTO from(Item item, String language) {
        return new ItemDTO(item.getAnkamaId(), item.getName(language), item.getDescription(language), item.getImageUrl(), item.getAnkamaUrl(language), item.getItemType());
    }
}
