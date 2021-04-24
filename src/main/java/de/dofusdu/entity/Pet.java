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
public class Pet extends Equipment {

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "pet")
    private Collection<Characteristic> characteristics;

    public Pet() {
        super();
    }

    public Pet(Long ankamaId, String name, String description, String imageUrl, String ankamaUrl, String language, String type, String conditions, Integer level, Collection<Effect> effects, Set set, Collection<Characteristic> characteristics, Branch branch) {
        super(ankamaId, name, description, imageUrl, ankamaUrl, language, type, conditions, level, effects, null, set, branch);
        setCharacteristics(characteristics);
    }

    public Collection<Characteristic> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(Collection<Characteristic> characteristics) {
        this.characteristics = characteristics;
    }

    @Override
    public String getItemType() {
        return "pets";
    }
}
