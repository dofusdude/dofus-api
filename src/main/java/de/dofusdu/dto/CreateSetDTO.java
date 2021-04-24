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

import de.dofusdu.entity.*;
import de.dofusdu.gateway.EffectRepository;
import de.dofusdu.gateway.EquipmentRepository;
import de.dofusdu.gateway.PetRepository;
import de.dofusdu.gateway.WeaponRepository;

import javax.json.bind.annotation.JsonbProperty;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CreateSetDTO {
    public String name;
    public Collection<Long> items;
    @JsonbProperty("ankama_id")
    public Long ankamaId;
    @JsonbProperty("image_url")
    public String imageUrl;
    public Integer level;
    @JsonbProperty("ankama_url")
    public String ankamaUrl;
    public List<SetBonusPositionDTO> effects;

    public CreateSetDTO() {
    }

    public CreateSetDTO(String name, Collection<Long> items) {
        this.name = name;
        this.items = items;
    }

    public Set toSet(String language, WeaponRepository weaponRepository, EquipmentRepository equipmentRepository, PetRepository petRepository, EffectRepository effectRepository, Branch branch) {
        Optional<Weapon> w = Optional.empty();
        Collection<Item> equipment = new ArrayList<>();

        // get all equip, check if weapon
        for (Long ankaId : items) {
            Optional<Equipment> equipment2 = equipmentRepository.byId(ankaId);
            if (equipment2.isEmpty()) {
                // check if weapon
                Optional<Weapon> foundWeapon = weaponRepository.byId(ankaId);
                if (foundWeapon.isPresent()) {
                    if (w.isPresent()) {
                        throw new BadRequestException("Only one weapon per set allowed.");
                    } else {
                        w = foundWeapon;
                        equipment.add(w.get());
                    }
                } else {
                    Optional<Pet> foundPet = petRepository.byId(ankaId);
                    if (foundPet.isPresent()) {
                        equipment.add(foundPet.get());
                    } else {
                        throw new NotFoundException("Item with ID " + ankaId + " is not an equipment or a weapon.");
                    }
                }
            } else {
                equipment.add(equipment2.get());
            }
        }

        return new Set(name, language, ankamaId, imageUrl, level,
                effects == null ? null : effects.stream().map(bonusPositionDTO -> bonusPositionDTO.toBonusPosition(language, effectRepository)).collect(Collectors.toList()), equipment, branch, ankamaUrl);
    }
}
