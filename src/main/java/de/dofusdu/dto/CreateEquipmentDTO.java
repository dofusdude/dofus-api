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
import de.dofusdu.gateway.EffectRepository;
import de.dofusdu.gateway.ItemFinder;

import javax.ws.rs.NotFoundException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class CreateEquipmentDTO extends ItemDTO {
    public String type;
    public Integer level;

    public Collection<EffectDTO> effects;
    public String conditions;
    public Collection<CreateRecipePositionDTO> recipe;

    public CreateEquipmentDTO() {
    }

    public CreateEquipmentDTO(Long ankamaId, String name, String description, String imageUrl, String ankamaUrl, String type,
                              Integer level, Collection<EffectDTO> effects,
                              String conditions, Collection<CreateRecipePositionDTO> recipe) {
        super(ankamaId, name, description, imageUrl, ankamaUrl);
        this.type = type;
        this.level = level;
        this.effects = effects;
        this.conditions = conditions;
        this.recipe = recipe;
    }

    public static CreateEquipmentDTO fromEquipment(Equipment equipment, String language, Collection<EffectDTO> effects, Collection<CreateRecipePositionDTO> recipe) {
        return new CreateEquipmentDTO(equipment.getAnkamaId(), equipment.getName(language), equipment.getDescription(language), equipment.getImageUrl(), equipment.getAnkamaUrl(language), equipment.getType(language),
                equipment.getLevel(), effects, equipment.getConditions(language), recipe);
    }

    public Equipment toEquipment(String language, EffectRepository effectRepository, ItemFinder itemFinder, Branch branch) {
        Recipe recipe = null;
        if (this.recipe != null && !this.recipe.isEmpty()) {
            recipe = new Recipe();
            recipe.setPositions(this.recipe.stream().map(recipePositionDTO -> {
                Optional<Item> item = itemFinder.findItem(recipePositionDTO.itemId);
                if (item.isEmpty()) {
                    throw new NotFoundException();
                }
                return new RecipePosition(item.get(), recipePositionDTO.getQuantity());
            }).collect(Collectors.toList()));
        }


        return new Equipment(ankamaId, name, description, imageUrl, ankamaUrl, language, type, conditions, level, effects == null || effects.isEmpty() ? null : effects.stream().map(effectDTO -> {
            Optional<Attribute> effect = effectRepository.closestIdByName(effectDTO.name, language);
            if (effect.isEmpty()) {
                throw new NotFoundException();
            }
            return effectDTO.toEffect(effect.get(), language);
        }).collect(Collectors.toList()), recipe, null, branch);
    }
}
