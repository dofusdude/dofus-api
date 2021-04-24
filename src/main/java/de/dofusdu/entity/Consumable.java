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

import javax.persistence.*;
import java.util.Collection;

@Entity
public class Consumable extends Item {
    @OneToOne(cascade = CascadeType.ALL)
    private MultilingualEntity type;

    private Integer level;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "effect")
    private Collection<ConsumableEffect> effects;

    @OneToOne(cascade = CascadeType.ALL)
    private MultilingualEntity conditions;

    @OneToOne(cascade = CascadeType.ALL)
    private Recipe recipe;

    public Consumable(Long ankamaId, String name, String description, String imageUrl, String ankamaUrl, String language, String type, Integer level, Collection<ConsumableEffect> effects, String conditions, Recipe recipe, Branch branch) {
        super(ankamaId, name, description, imageUrl, ankamaUrl, language, branch);
        this.type = new MultilingualEntity(type, language);
        this.level = level;
        this.effects = effects;
        this.conditions = new MultilingualEntity(conditions, language);
        this.recipe = recipe;
    }

    public Consumable() {

    }

    public String getType(String language) {
        return type.getName(language);
    }

    public void setType(String type, String language) {
        this.type.getName(language);
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Collection<ConsumableEffect> getEffects() {
        return effects;
    }

    public void setEffects(Collection<ConsumableEffect> effects) {
        this.effects = effects;
    }

    public String getConditions(String language) {
        return conditions.getName(language);
    }

    public void setConditions(String conditions, String language) {
        this.conditions.setName(conditions, language);
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public String getItemType() {
        return "consumables";
    }
}
