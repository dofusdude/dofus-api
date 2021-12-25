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

import java.util.Collection;
import java.util.List;

public class CreateConsumableDTO extends ItemDTO {
    public String type;
    public Integer level;

    public List<String> effects;
    public String conditions;
    public Collection<CreateRecipePositionDTO> recipe;

    public CreateConsumableDTO(Long ankamaId, String name, String description, String imageUrl, String ankamaUrl, String type, Integer level, List<String> effects, String conditions, Collection<CreateRecipePositionDTO> recipe) {
        super(ankamaId, name, description, imageUrl, ankamaUrl, null);
        this.type = type;
        this.level = level;
        this.effects = effects;
        this.conditions = conditions;
        this.recipe = recipe;
    }

    public CreateConsumableDTO() {
        super();
    }

    public Consumable toConsumable(String language, Collection<RecipePosition> recipePositions, Branch branch) {
        Recipe recipe = null;
        if (recipePositions != null && !recipePositions.isEmpty()) {
            recipe = new Recipe();
            recipe.setPositions(recipePositions);
        }
        return new Consumable(ankamaId, name, description, imageUrl, ankamaUrl, language, type, level,
                effects == null || effects.isEmpty() ? null : effects, conditions, recipe, branch);
    }
}
