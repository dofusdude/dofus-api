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
import de.dofusdu.entity.RecipePosition;
import de.dofusdu.util.NgnixUriReplacer;

import javax.json.bind.annotation.JsonbProperty;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class RecipePositionDTO {
    @JsonbProperty("item_url")
    public String itemUrl;
    public Integer quantity;
    @JsonbProperty("ankama_id")
    public Long ankamaId;

    public RecipePositionDTO() {
    }

    public RecipePositionDTO(String itemUrl, Integer quantity, Long ankamaId) {
        this.itemUrl = itemUrl;
        this.quantity = quantity;
        this.ankamaId = ankamaId;
    }

    public static RecipePositionDTO from(RecipePosition recipePosition, URI baseUri, String language) {
        Item item = recipePosition.getItem();
        URI uri = NgnixUriReplacer.replace(UriBuilder.fromUri(baseUri)
                .path("dofus")
                .path(language)
                .path(item.getItemType())
                .path(item.getAnkamaId().toString())
                .build());
        return new RecipePositionDTO(uri.toString(), recipePosition.getQuantity(), recipePosition.getItem().getAnkamaId());
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
