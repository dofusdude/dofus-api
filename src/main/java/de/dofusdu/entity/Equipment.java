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
@Inheritance(strategy = InheritanceType.JOINED)
public class Equipment extends Item {

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    protected MultilingualEntity type;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    protected MultilingualEntity conditions;

    protected Integer level;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment")
    protected Collection<Effect> effects;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    protected Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    protected Set set;

    public Equipment() {

    }

    public void setType(String type, String language) {
        this.type.setName(type, language);
    }

    public void setConditions(String conditions, String language) {
        this.conditions.setName(conditions, language);
    }

    public String getType(String language) {
        return type.getName(language);
    }

    public String getConditions(String language) {
        return conditions.getName(language);
    }

    public Integer getLevel() {
        return level;
    }

    public Collection<Effect> getEffects() {
        return effects;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public Set getSet() {
        return set;
    }

    public Equipment(String language, String type, String conditions, Integer level, Collection<Effect> effects, Recipe recipe, Set set) {
        this.type = new MultilingualEntity(type, language);
        this.conditions = new MultilingualEntity(conditions, language);
        this.level = level;
        this.effects = effects;
        this.recipe = recipe;
        this.set = set;
    }

    public Equipment(Long ankamaId, String name, String description, String imageUrl, String ankamaUrl, String language, String type, String conditions, Integer level, Collection<Effect> effects, Recipe recipe, Set set, Branch branch) {
        super(ankamaId, name, description, imageUrl, ankamaUrl, language, branch);
        this.type = new MultilingualEntity(type, language);
        this.conditions = new MultilingualEntity(conditions, language);
        this.level = level;
        this.effects = effects;
        this.recipe = recipe;
        this.set = set;
    }

    @Override
    public String getItemType() {
        return "equipment";
    }
}
