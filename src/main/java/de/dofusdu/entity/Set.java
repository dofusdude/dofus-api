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
public class Set extends MultilingualEntity {

    private Long ankamaId;

    @Column(name = "imageUrl", length = 1024)
    private String imageUrl;

    private Integer level;

    @ManyToOne(fetch = FetchType.LAZY)
    private Branch branch;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "set")
    private Collection<SetBonusPosition> bonus;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "set")
    private Collection<Item> items;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MultilingualEntity ankamaUrl;

    public Set() {
        super();
    }

    public Set(String name, String lang, Long ankamaId, String imageUrl, Integer level, Collection<SetBonusPosition> bonus, Collection<Item> items, Branch branch, String ankamaUrl) {
        super(name, lang);
        this.ankamaId = ankamaId;
        this.imageUrl = imageUrl;
        this.level = level;
        this.bonus = bonus;
        this.items = items;
        this.ankamaUrl = new MultilingualEntity(ankamaUrl, lang);
        this.branch = branch;
    }

    public Long getAnkamaId() {
        return ankamaId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Integer getLevel() {
        return level;
    }

    public Collection<SetBonusPosition> getBonus() {
        return bonus;
    }

    public Collection<Item> getItems() {
        return items;
    }

    public String getAnkamaUrl(String language) {
        return ankamaUrl.getName(language);
    }

    public void setAnkamaUrl(String ankamaUrl, String language) {
        this.ankamaUrl.setName(ankamaUrl, language);
    }

    public String getItemType() {
        return "sets";
    }
}
