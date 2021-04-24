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

import de.dofusdu.entity.*;

import javax.transaction.Transactional;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class EquipmentDTO extends ItemDTO {
    public String type;
    public Integer level;

    public Collection<EffectDTO> effects;
    public String conditions;
    public Collection<RecipePositionDTO> recipe;

    public EquipmentDTO() {
    }

    public EquipmentDTO(Long ankamaId, String name, String description, String imageUrl, String ankamaUrl, String type,
                        Integer level, Collection<EffectDTO> effects,
                        String conditions, Collection<RecipePositionDTO> recipe) {
        super(ankamaId, name, description, imageUrl, ankamaUrl);
        this.type = type;
        this.level = level;
        this.effects = effects;
        this.conditions = conditions;
        this.recipe = recipe;
    }

    @Transactional
    public static EquipmentDTO from(Equipment equipment, String language, URI baseUri) {
        return new EquipmentDTO(equipment.getAnkamaId(), equipment.getName(language), equipment.getDescription(language), equipment.getImageUrl(), equipment.getAnkamaUrl(language), equipment.getType(language),
                equipment.getLevel(), equipment.getEffects() == null || equipment.getEffects().isEmpty() ? null : equipment.getEffects().stream().map(entityPos -> EffectDTO.from(entityPos, language)).collect(Collectors.toList()),
                equipment.getConditions(language),
                equipment.getRecipe() == null || (equipment.getRecipe().getPositions() == null || equipment.getRecipe().getPositions().isEmpty()) ? null : equipment.getRecipe().getPositions().stream().map(entityPos -> RecipePositionDTO.from(
                        entityPos, baseUri, language
                ))
                        .collect(Collectors.toList()));
    }

    public Equipment toEquipment(String language, List<Effect> conEffects, Collection<RecipePosition> recipePositions, Branch branch) {
        Recipe recipe = null;
        if (recipePositions != null && !recipePositions.isEmpty()) {
            recipe = new Recipe();
            recipe.setPositions(recipePositions);
        }

        return new Equipment(ankamaId, name, description, imageUrl, ankamaUrl, language, type, conditions, level, conEffects, recipe, null, branch);
    }
}
