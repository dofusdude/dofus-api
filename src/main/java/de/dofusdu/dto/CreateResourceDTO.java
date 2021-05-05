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

import de.dofusdu.entity.Branch;
import de.dofusdu.entity.Recipe;
import de.dofusdu.entity.RecipePosition;
import de.dofusdu.entity.Resource;

import javax.transaction.Transactional;
import java.util.Collection;

public class CreateResourceDTO extends ItemDTO {
    public String type;
    public Integer level;

    public Collection<CreateRecipePositionDTO> recipe;

    public CreateResourceDTO(Long ankamaId, String name, String description, String imageUrl, String ankamaUrl, String type, Integer level, Collection<String> effects, String conditions, Collection<CreateRecipePositionDTO> recipe) {
        super(ankamaId, name, description, imageUrl, ankamaUrl, null);
        this.type = type;
        this.level = level;
        this.recipe = recipe;
    }

    public CreateResourceDTO() {
        super();
    }

    @Transactional
    public Resource toResource(String language, Collection<RecipePosition> recipePositions, Branch branch) {
        Recipe recipe = new Recipe();
        recipe.setPositions(recipePositions);
        return new Resource(ankamaId, name, description, imageUrl, ankamaUrl, language, type, level,
                recipePositions == null ? null : recipe, branch);
    }
}
