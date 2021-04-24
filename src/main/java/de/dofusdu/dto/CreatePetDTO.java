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
import de.dofusdu.entity.Pet;
import de.dofusdu.gateway.EffectRepository;
import de.dofusdu.gateway.ItemFinder;

import java.util.Collection;
import java.util.stream.Collectors;

public class CreatePetDTO extends CreateEquipmentDTO {
    public Collection<CharacteristicDTO> characteristics;

    public CreatePetDTO() {
        super();
    }

    public CreatePetDTO(Long ankamaId, String name, String description, String imageUrl, String ankamaUrl, String type, Integer level, Collection<EffectDTO> effects, String conditions, Collection<CharacteristicDTO> characteristics) {
        super(ankamaId, name, description, imageUrl, ankamaUrl, type, level, null, conditions, null);
        this.characteristics = characteristics;
    }

    public Pet toPet(String language, EffectRepository effectRepository, ItemFinder itemFinder, Branch branch) {
        Equipment equipment = super.toEquipment(language, effectRepository, itemFinder, branch);

        return new Pet(equipment.getAnkamaId(), equipment.getName(language), equipment.getDescription(language), equipment.getImageUrl(), equipment.getAnkamaUrl(language),
                language, equipment.getType(language), equipment.getConditions(language), equipment.getLevel(), equipment.getEffects(), equipment.getSet(),
                characteristics == null || characteristics.isEmpty() ? null : characteristics.stream().map(chara -> chara.toCharacteristic(language, effectRepository)).collect(Collectors.toList()), branch);
    }
}
