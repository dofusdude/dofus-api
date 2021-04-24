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

import de.dofusdu.entity.Resource;

import javax.transaction.Transactional;
import java.net.URI;
import java.util.Collection;
import java.util.stream.Collectors;

public class ResourceDTO extends ItemDTO {
    public String type;
    public Integer level;
    public Collection<RecipePositionDTO> recipe;

    public ResourceDTO(Long ankamaId, String name, String description, String imageUrl, String ankamaUrl, String type, Integer level, Collection<RecipePositionDTO> recipe) {
        super(ankamaId, name, description, imageUrl, ankamaUrl);
        this.type = type;
        this.level = level;
        this.recipe = recipe;
    }

    public ResourceDTO() {
        super();
    }

    @Transactional
    public static ResourceDTO from(Resource r, String language, URI baseUri) {
        return new ResourceDTO(r.getAnkamaId(), r.getName(language), r.getDescription(language), r.getImageUrl(), r.getAnkamaUrl(language), r.getType(language), r.getLevel(),
                r.getRecipe() == null ? null : r.getRecipe().getPositions().stream().map(el -> RecipePositionDTO.from(el, baseUri, language)).collect(Collectors.toList()));
    }


}
