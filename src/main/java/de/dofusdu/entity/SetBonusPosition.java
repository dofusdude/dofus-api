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
public class SetBonusPosition {
    @Id
    @GeneratedValue
    private Long id;

    private Integer quantity; // e.g. set bonus with 2 items

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "bonus_position")
    private Collection<Characteristic> bonus;

    public SetBonusPosition(Integer quantity, Collection<Characteristic> bonus) {
        this.quantity = quantity;
        this.bonus = bonus;
    }

    public SetBonusPosition() {

    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Collection<Characteristic> getBonus() {
        return bonus;
    }

    public void setBonus(Collection<Characteristic> bonus) {
        this.bonus = bonus;
    }
}
