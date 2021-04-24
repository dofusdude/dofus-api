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

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(indexes = {
        @Index(columnList = "ankamaId", name = "idx_ankamaId", unique = true)
})
public class Item extends MultilingualEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    public Branch branch;
    private Long ankamaId;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MultilingualEntity description;

    private String imageUrl;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MultilingualEntity ankamaUrl;

    public Item() {
        this.description = new MultilingualEntity();
    }

    public Item(Long ankamaId, String name, String description, String imageUrl, String ankamaUrl, String language, Branch branch) {
        this.ankamaId = ankamaId;
        super.setName(name, language);
        this.description = new MultilingualEntity();
        this.description.setName(description, language);
        this.imageUrl = imageUrl;
        this.ankamaUrl = new MultilingualEntity();
        this.ankamaUrl.setName(ankamaUrl, language);
        this.branch = branch;
    }

    public Long getAnkamaId() {
        return ankamaId;
    }

    public void setAnkamaId(Long ankamaId) {
        this.ankamaId = ankamaId;
    }

    public String getDescription(String language) {
        return description.getName(language);
    }

    public void setDescription(String description, String language) {
        this.description.setName(description, language);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAnkamaUrl(String language) {
        return ankamaUrl.getName(language);
    }

    public void setAnkamaUrl(String ankamaUrl, String language) {
        this.ankamaUrl.setName(ankamaUrl, language);
    }

    public String getItemType() {
        return "item";
    }
}
