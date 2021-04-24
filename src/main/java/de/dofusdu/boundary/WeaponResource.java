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

package de.dofusdu.boundary;

import de.dofusdu.dto.BaseItemAllApi;
import de.dofusdu.dto.CreateWeaponDTO;
import de.dofusdu.dto.WeaponDTO;
import de.dofusdu.entity.Weapon;
import de.dofusdu.gateway.BranchRepository;
import de.dofusdu.gateway.EffectRepository;
import de.dofusdu.gateway.ItemFinder;
import de.dofusdu.gateway.WeaponRepository;
import de.dofusdu.util.LanguageHelper;
import io.quarkus.security.UnauthorizedException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestScoped
@Path("/{language}/weapons")
public class WeaponResource {
    @ConfigProperty(name = "admin.api.secret")
    String apiKey;

    @Inject
    BaseItemAllApi baseItemAllApi;

    @Inject
    WeaponRepository weaponRepository;

    @Inject
    EffectRepository effectRepository;

    @Inject
    ItemFinder itemFinder;

    @Context
    UriInfo uriInfo;

    @Inject
    BranchRepository branchRepository;

    @GET
    @Path("/{ankama_id}")
    @Tag(name = "Weapon")
    @Parameters({
            @Parameter(name = "language", in = ParameterIn.PATH, example = "en", description = "Language as code of length 2."),
            @Parameter(name = "ankama_id", in = ParameterIn.PATH, example = "6492")
    })
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "OK",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = de.dofusdu.dto.WeaponDTO.class)
                            )
                    }
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Not Found"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Bad Request - unknown language"
            )
    })
    @Produces(MediaType.APPLICATION_JSON)
    @Timeout
    @CircuitBreaker
    @Retry
    @Timed(name = "weaponsByIdTime")
    @Counted(name = "weaponsByIdCounter")
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public WeaponDTO byId(
            @PathParam("language") String language,
            @PathParam("ankama_id") Long ankamaId
    ) {
        LanguageHelper.checkLanguage(language);
        Optional<Weapon> weapon = weaponRepository.byIdCached(ankamaId);
        if (weapon.isEmpty()) {
            throw new NotFoundException();
        }

        return WeaponDTO.from(weapon.get(), language, uriInfo.getBaseUri());
    }

    @GET
    @Tag(name = "Weapon")
    @Parameters({
            @Parameter(name = "language", in = ParameterIn.PATH, example = "en", description = "Language as code of length 2."),
            @Parameter(name = "search[name]", in = ParameterIn.QUERY, example = ""),
            @Parameter(name = "page[number]", in = ParameterIn.QUERY, example = "1"),
            @Parameter(name = "page[size]", in = ParameterIn.QUERY, example = "96")
    })
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "OK",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = de.dofusdu.dto.ItemListResponse.class)
                            )
                    }
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Bad Request - unknown language"
            )
    })
    @Produces(MediaType.APPLICATION_JSON)
    @Timeout
    @CircuitBreaker
    @Retry
    @Timed(name = "weaponsAllTime")
    @Counted(name = "weaponsAllCounter")
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Response all(
            @PathParam("language") String language,
            @QueryParam("search[name]") @DefaultValue("") String query,
            @DefaultValue("-1") @QueryParam("page[number]") int pageNumber,
            @DefaultValue("-1") @QueryParam("page[size]") int pageSize
    ) {
        return baseItemAllApi.all(language, query, pageNumber, pageSize, weaponRepository, uriInfo.getAbsolutePath());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public WeaponDTO create(
            @HeaderParam("Authorization") String apiKey,
            @PathParam("language") String language,
            CreateWeaponDTO weaponDTO
    ) {
        LanguageHelper.checkLanguage(language);
        if (apiKey == null || !apiKey.equals("Bearer " + this.apiKey)) {
            throw new UnauthorizedException("Wrong secret.");
        }

        Optional<Weapon> weapon = weaponRepository.byId(weaponDTO.ankamaId);
        if (weapon.isPresent()) {
            throw new BadRequestException();
        }

        // persist entity
        Weapon persist = weaponRepository.persist(weaponDTO.toWeapon(language, effectRepository, itemFinder, branchRepository.main()));

        // convert back to show-DTO //
        return WeaponDTO.from(persist, language, uriInfo.getBaseUri());
    }

    @PUT
    @Path("/{id}/lang")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public WeaponDTO updateLanguage(
            @HeaderParam("Authorization") String apiKey,
            @PathParam("language") String language,
            @PathParam("id") Long id,
            CreateWeaponDTO weaponDTO
    ) {
        LanguageHelper.checkLanguage(language);
        Optional<Weapon> weapon = weaponRepository.byId(weaponDTO.ankamaId);
        if (weapon.isEmpty()) {
            throw new NotFoundException();
        }

        // base equipment
        //equipmentResource.updateLanguage(apiKey, language, id, weaponDTO);

        Weapon persisted = weapon.get();
        persisted.setName(weaponDTO.name, language);
        persisted.setType(weaponDTO.type, language);
        persisted.setAnkamaUrl(weaponDTO.ankamaUrl, language);
        persisted.setDescription(weaponDTO.description, language);
        persisted.setConditions(weaponDTO.conditions, language);

        persisted.setCharacteristics(weaponDTO.characteristics.stream().map(el -> el.toCharacteristic(language, effectRepository)).collect(Collectors.toList()));
        Weapon update = weaponRepository.update(persisted);

        return WeaponDTO.from(update, language, uriInfo.getBaseUri());
    }

}
