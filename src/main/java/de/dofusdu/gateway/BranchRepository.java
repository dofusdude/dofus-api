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

import de.dofusdu.entity.Branch;
import io.quarkus.cache.CacheResult;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Optional;

@RequestScoped
public class BranchRepository {
    @Inject
    EntityManager em;

    @CacheResult(cacheName = "branchId")
    public Optional<Branch> byId(Long id) {
        return Optional.of(em.find(Branch.class, id));
    }

    @CacheResult(cacheName = "branchMain")
    public Branch main() {
        return byName("main").get();
    }

    public Optional<Branch> byName(String name) {
        TypedQuery<Branch> query = em.createQuery("select r from Branch r where r.name = :name", Branch.class);
        query.setParameter("name", name);
        Branch branch;
        try {
            branch = query.getSingleResult();
        } catch (NoResultException e) {
            return Optional.empty();
        }
        return Optional.of(branch);
    }
}
