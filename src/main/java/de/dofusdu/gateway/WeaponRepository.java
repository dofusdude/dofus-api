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

package de.dofusdu.gateway;

import de.dofusdu.dto.ItemBasicDTO;
import de.dofusdu.dto.ItemNameDTO;
import de.dofusdu.entity.Weapon;
import io.quarkus.cache.CacheResult;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@RequestScoped
public class WeaponRepository implements Repository {

    @Inject
    EntityManager em;

    @Transactional
    public Weapon persist(Weapon weapon) {
        em.persist(weapon);
        return weapon;
    }

    @Transactional
    public Weapon update(Weapon weapon) {
        return em.merge(weapon);
    }

    @Transactional
    //@CacheResult(cacheName = "weaponId")
    public Optional<Weapon> byIdCached(Long ankamaId) {
        return byId(ankamaId);
    }

    @Transactional
    //@CacheResult(cacheName = "weaponList")
    public List<Weapon> allCached() {
        return all();
    }


    @Transactional
    public Optional<Weapon> byId(Long ankamaId) {
        TypedQuery<Weapon> query = em.createQuery("select r from Weapon r where r.ankamaId = :ankamaId", Weapon.class);
        query.setParameter("ankamaId", ankamaId);
        Weapon weapon;
        try {
            weapon = query.getSingleResult();
        } catch (NoResultException e) {
            return Optional.empty();
        }
        return Optional.of(weapon);
    }

    @Transactional
    public List<ItemBasicDTO> allBasic() {
        TypedQuery<ItemBasicDTO> query = em.createQuery("select new de.dofusdu.dto.ItemBasicDTO(r.ankamaId, 'weapons') from Weapon r", ItemBasicDTO.class);
        List<ItemBasicDTO> weapons;
        try {
            weapons = query.getResultList();
        } catch (NoResultException e) {
            return List.of();
        }
        return weapons;
    }

    @Transactional
    //@CacheResult(cacheName = "weaponsNames")
    public List<ItemNameDTO> allBasicName(String language, URI absoluteUri) {
        TypedQuery<ItemNameDTO> query;
        switch (language) {
            case "de": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'weapons', r.nameDe) from Weapon r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            case "fr": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'weapons', r.nameFr) from Weapon r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            case "es": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'weapons', r.nameEs) from Weapon r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            case "it": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'weapons', r.nameIt) from Weapon r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            case "pt": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'weapons', r.namePt) from Weapon r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            default: {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'weapons', r.nameEn) from Weapon r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
        }

        List<ItemNameDTO> weapons;
        try {
            weapons = query.getResultList();
        } catch (NoResultException e) {
            return List.of();
        }
        weapons.forEach(el -> el.url =
                URLDecoder.decode(
                        UriBuilder.fromUri(absoluteUri)
                        .path(el.ankamaId.toString())
                        .build()
                        .toString(),
                StandardCharsets.UTF_8));

        return weapons;
    }

    @Transactional
    public List<Weapon> all() {
        TypedQuery<Weapon> query = em.createQuery("select r from Weapon r", Weapon.class);
        List<Weapon> weapons;
        try {
            weapons = query.getResultList();
        } catch (NoResultException e) {
            return List.of();
        }
        return weapons;
    }

}
