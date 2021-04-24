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

package de.dofusdu.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class Resource extends Item {
    @OneToOne(cascade = CascadeType.ALL)
    private MultilingualEntity type;

    private Integer level;

    @OneToOne(cascade = CascadeType.ALL)
    private Recipe recipe;

    public Integer getLevel() {
        return level;
    }

    public String getType(String language) {
        return type.getName(language);
    }

    public void setType(String type, String language) {
        this.type.setName(type, language);
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public Resource(String type, String language, Integer level, Recipe recipe) {
        this.type = new MultilingualEntity(type, language);
        this.level = level;
        this.recipe = recipe;
    }

    public Resource(Long ankamaId, String name, String description, String imageUrl, String ankamaUrl, String language, String type, Integer level, Recipe recipe, Branch branch) {
        super(ankamaId, name, description, imageUrl, ankamaUrl, language, branch);
        this.type = new MultilingualEntity(type, language);
        this.level = level;
        this.recipe = recipe;
    }

    public Resource() {
    }

    public String getItemType() {
        return "resources";
    }
}
