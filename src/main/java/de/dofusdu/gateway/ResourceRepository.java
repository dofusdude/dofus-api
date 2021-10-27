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
import de.dofusdu.entity.Resource;
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
public class ResourceRepository implements Repository {

    @Inject
    EntityManager em;

    @Transactional
    public Optional<Resource> byId(Long ankamaId) {
        TypedQuery<Resource> query = em.createQuery("select r from Resource r where r.ankamaId = :ankamaId", Resource.class);
        query.setParameter("ankamaId", ankamaId);
        Resource resource;
        try {
            resource = query.getSingleResult();
        } catch (NoResultException e) {
            return Optional.empty();
        }
        return Optional.of(resource);
    }

    @Transactional
    //@CacheResult(cacheName = "resourceId")
    public Optional<Resource> byIdCached(Long ankamaId) {
        return byId(ankamaId);
    }

    @Transactional
    //@CacheResult(cacheName = "resourceList")
    public List<Resource> allCached() {
        return all();
    }

    @Transactional
    public List<ItemBasicDTO> allBasic() {
        TypedQuery<ItemBasicDTO> query = em.createQuery("select new de.dofusdu.dto.ItemBasicDTO(r.ankamaId, 'resources') from Resource r", ItemBasicDTO.class);
        List<ItemBasicDTO> weapons;
        try {
            weapons = query.getResultList();
        } catch (NoResultException e) {
            return List.of();
        }
        return weapons;
    }

    @Transactional
    //@CacheResult(cacheName = "resourcesNames")
    public List<ItemNameDTO> allBasicName(String language, URI absoluteUri) {
        TypedQuery<ItemNameDTO> query;
        switch (language) {
            case "de": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'resources', r.nameDe) from Resource r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            case "fr": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'resources', r.nameFr) from Resource r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            case "es": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'resources', r.nameEs) from Resource r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            case "it": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'resources', r.nameIt) from Resource r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            case "pt": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'resources', r.namePt) from Resource r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            default: {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'resources', r.nameEn) from Resource r ORDER BY r.level ASC", ItemNameDTO.class);
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
    public List<Resource> all() {
        TypedQuery<Resource> query = em.createQuery("select r from Resource r", Resource.class);
        List<Resource> resources;
        try {
            resources = query.getResultList();
        } catch (NoResultException e) {
            return List.of();
        }
        return resources;
    }

    @Transactional
    public Resource persist(Resource resource) {
        em.persist(resource);
        return resource;
    }

    @Transactional
    public Resource update(Resource resource) {
        return em.merge(resource);
    }
}
