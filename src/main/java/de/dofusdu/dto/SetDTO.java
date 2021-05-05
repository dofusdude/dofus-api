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

import de.dofusdu.entity.Set;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.json.bind.annotation.JsonbProperty;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class SetDTO {
    public String name;
    @JsonbProperty("ankama_id")
    public Long ankamaId;
    public Integer level;
    @JsonbProperty("image_url")
    public String imageUrl;
    public Collection<ItemBasicDTO> items;
    @JsonbProperty("ankama_url")
    public String ankamaUrl;
    @JsonbProperty("image_url_local")
    public String imageUrlLocal;
    @JsonbProperty("effects")
    public Collection<SetBonusPositionDTO> bonus;

    public SetDTO() {
    }

    public SetDTO(String name, Long ankamaId, Integer level, String imageUrl, Collection<ItemBasicDTO> items, String ankamaUrl, Collection<SetBonusPositionDTO> bonus) {
        this.name = name;
        this.ankamaId = ankamaId;
        this.level = level;
        this.imageUrl = imageUrl;
        this.items = items;
        this.ankamaUrl = ankamaUrl;
        this.bonus = bonus;
        Optional<String> service_hostname = ConfigProvider.getConfig().getOptionalValue("service.hostname", String.class);
        String service_host = "http://localhost";
        if (service_hostname.isPresent()) {
            service_host = service_hostname.get();
        }
        this.imageUrlLocal = UriBuilder.fromPath(service_host)
                .path("static")
                .path("sets")
                .path(ankamaId + ".png")
                .build().toString();
    }

    public static SetDTO from(Set set, String language, URI baseUri) {
        return new SetDTO(set.getName(language),
                set.getAnkamaId(),
                set.getLevel(),
                set.getImageUrl(),
                set.getItems().stream().map(e -> ItemBasicDTO.from(e, language, baseUri)).collect(Collectors.toList()),
                set.getAnkamaUrl(language),
                set.getBonus() == null ? null : set.getBonus().stream().map(bonus -> SetBonusPositionDTO.from(bonus, language)).collect(Collectors.toList())
        );
    }

}
