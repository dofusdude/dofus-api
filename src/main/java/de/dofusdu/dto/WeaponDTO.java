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

import de.dofusdu.entity.Weapon;

import javax.transaction.Transactional;
import java.net.URI;
import java.util.Collection;
import java.util.stream.Collectors;

public class WeaponDTO extends EquipmentDTO {

    public Collection<CharacteristicDTO> characteristics;

    public WeaponDTO() {
        super();
    }

    public WeaponDTO(Long ankamaId, String name, String description, String imageUrl, String ankamaUrl, String type, Integer level, Collection<EffectDTO> effects, String conditions, Collection<RecipePositionDTO> recipe) {
        super(ankamaId, name, description, imageUrl, ankamaUrl, type, level, effects, conditions, recipe);
    }

    @Transactional
    public static WeaponDTO from(Weapon weapon, String language, URI baseUri) {
        EquipmentDTO equipmentDTO = EquipmentDTO.from(weapon, language, baseUri);

        WeaponDTO weaponDTO = new WeaponDTO(equipmentDTO.ankamaId, equipmentDTO.name, equipmentDTO.description, equipmentDTO.imageUrl, equipmentDTO.ankamaUrl, equipmentDTO.type, equipmentDTO.level,
                equipmentDTO.effects, equipmentDTO.conditions, equipmentDTO.recipe);
        weaponDTO.characteristics = weapon.getCharacteristics().stream().map(el -> CharacteristicDTO.from(el, language)).collect(Collectors.toList());
        return weaponDTO;
    }
}
