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

import de.dofusdu.entity.Consumable;

import java.net.URI;
import java.util.Collection;
import java.util.stream.Collectors;

public class ConsumableDTO extends ItemDTO {

    public String type;
    public Integer level;

    public Collection<ConsumableEffectDTO> effects;
    public String conditions;
    public Collection<RecipePositionDTO> recipe;

    public ConsumableDTO(Long ankamaId, String name, String description, String imageUrl, String ankamaUrl, String type, Integer level, Collection<ConsumableEffectDTO> effects, String conditions, Collection<RecipePositionDTO> recipe) {
        super(ankamaId, name, description, imageUrl, ankamaUrl);
        this.type = type;
        this.level = level;
        this.effects = effects;
        this.conditions = conditions;
        this.recipe = recipe;
    }


    public ConsumableDTO() {
        super();
    }

    public static ConsumableDTO from(Consumable consumable, String language, URI baseUri) {
        return new ConsumableDTO(consumable.getAnkamaId(), consumable.getName(language), consumable.getDescription(language), consumable.getImageUrl(), consumable.getAnkamaUrl(language), consumable.getType(language), consumable.getLevel(),
                consumable.getEffects() == null || consumable.getEffects().isEmpty() ? null : consumable.getEffects().stream().map(eff -> ConsumableEffectDTO.from(eff, language)).collect(Collectors.toList()),
                consumable.getConditions(language),
                consumable.getRecipe() == null || consumable.getRecipe().getPositions() == null || consumable.getRecipe().getPositions().isEmpty() ? null : consumable.getRecipe().getPositions().stream().map(el -> RecipePositionDTO.from(el, baseUri, language)).collect(Collectors.toList())
        );
    }
}
