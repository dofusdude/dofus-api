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
import de.dofusdu.entity.Equipment;
import de.dofusdu.entity.Weapon;
import de.dofusdu.gateway.EffectRepository;
import de.dofusdu.gateway.ItemFinder;

import java.util.Collection;
import java.util.stream.Collectors;

public class CreateWeaponDTO extends CreateEquipmentDTO {

    public Collection<CharacteristicDTO> characteristics;

    public CreateWeaponDTO() {
    }

    public CreateWeaponDTO(Long ankamaId, String name, String description, String imageUrl, String ankamaUrl, String type, Integer level, Collection<EffectDTO> effects, String conditions, Collection<CreateRecipePositionDTO> recipe, Collection<CharacteristicDTO> characteristics) {
        super(ankamaId, name, description, imageUrl, ankamaUrl, type, level, effects, conditions, recipe);
        this.characteristics = characteristics;
    }

    public Weapon toWeapon(String language, EffectRepository effectRepository, ItemFinder itemFinder, Branch branch) {
        Equipment equipment = super.toEquipment(language, effectRepository, itemFinder, branch);

        return new Weapon(equipment.getAnkamaId(), equipment.getName(language), equipment.getDescription(language), equipment.getImageUrl(), equipment.getAnkamaUrl(language),
                language, equipment.getType(language), equipment.getConditions(language), equipment.getLevel(), equipment.getEffects(), equipment.getRecipe(), equipment.getSet(),
                characteristics.stream().map(chara -> chara.toCharacteristic(language, effectRepository)).collect(Collectors.toList()), branch);
    }
}
