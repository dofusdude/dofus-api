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
import de.dofusdu.dto.CreateEquipmentDTO;
import de.dofusdu.dto.EquipmentDTO;
import de.dofusdu.entity.Equipment;
import de.dofusdu.entity.Weapon;
import de.dofusdu.gateway.*;
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

@RequestScoped
@Path("/dofus/{language}/equipment")
public class EquipmentResource {
    @ConfigProperty(name = "admin.api.secret")
    String apiKey;

    @Inject
    EquipmentRepository equipmentRepository;

    @Inject
    WeaponRepository weaponRepository;

    @Inject
    EffectRepository effectRepository;

    @Inject
    ItemFinder itemFinder;

    @Inject
    BranchRepository branchRepository;

    @Context
    UriInfo uriInfo;

    @Inject
    BaseItemAllApi baseItemAllApi;

    @GET
    @Path("/{ankama_id}")
    @Tag(name = "Equipment")
    @Parameters({
            @Parameter(name = "language", in = ParameterIn.PATH, example = "en", description = "Language as code of length 2."),
            @Parameter(name = "ankama_id", in = ParameterIn.PATH, example = "7336")
    })
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "OK",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = de.dofusdu.dto.EquipmentDTO.class)
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
    @Timed(name = "equipmentByIdTime")
    @Counted(name = "equipmentByIdCounter")
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public EquipmentDTO byId(
            @PathParam("language") String language,
            @PathParam("ankama_id") Long ankamaId
    ) {
        LanguageHelper.checkLanguage(language);
        Optional<Equipment> equipment = equipmentRepository.byIdCached(ankamaId);
        if (equipment.isEmpty()) {
            throw new NotFoundException();
        }

        return EquipmentDTO.from(equipment.get(), language, uriInfo.getBaseUri());
    }

    @GET
    @Tag(name = "Equipment")
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
    @Timed(name = "equipmentAllTime")
    @Counted(name = "equipmentAllCounter")
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Response all(
            @PathParam("language") String language,
            @QueryParam("search[name]") @DefaultValue("") String query,
            @DefaultValue("-1") @QueryParam("page[number]") int pageNumber,
            @DefaultValue("-1") @QueryParam("page[size]") int pageSize
    ) {
        return baseItemAllApi.all(language, query, pageNumber, pageSize, equipmentRepository, uriInfo.getAbsolutePath());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public EquipmentDTO create(
            @HeaderParam("Authorization") String apiKey,
            @PathParam("language") String language,
            CreateEquipmentDTO equipmentDTO
    ) {
        LanguageHelper.checkLanguage(language);
        if (apiKey == null || !apiKey.equals("Bearer " + this.apiKey)) {
            throw new UnauthorizedException("Wrong secret.");
        }


        Optional<Equipment> equipment = equipmentRepository.byId(equipmentDTO.ankamaId);
        if (equipment.isPresent()) {
            throw new BadRequestException();
        }

        // all weapons are equipment in the encyclopedia, so check if already in weapons, if so, skip
        Optional<Weapon> inWeapon = weaponRepository.byId(equipmentDTO.ankamaId);
        if (inWeapon.isPresent()) {
            throw new BadRequestException();
        }

        // persist entity
        Equipment persist = equipmentRepository.persist(equipmentDTO.toEquipment(language, effectRepository, itemFinder, branchRepository.main()));

        // convert back to show-DTO //
        return EquipmentDTO.from(persist, language, uriInfo.getBaseUri());
    }

    @PUT
    @Path("/{id}/lang")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public EquipmentDTO updateLanguage(
            @HeaderParam("Authorization") String apiKey,
            @PathParam("language") String language,
            @PathParam("id") Long id,
            EquipmentDTO equipmentDTO
    ) {
        LanguageHelper.checkLanguage(language);
        if (apiKey == null || !apiKey.equals("Bearer " + this.apiKey)) {
            throw new UnauthorizedException("Wrong secret.");
        }

        Optional<Equipment> equipment = equipmentRepository.byId(equipmentDTO.ankamaId);
        if (equipment.isEmpty()) {
            throw new NotFoundException();
        }

        Equipment persisted = equipment.get();

        // base item
        persisted.setName(equipmentDTO.name, language);
        persisted.setType(equipmentDTO.type, language);
        persisted.setAnkamaUrl(equipmentDTO.ankamaUrl, language);
        persisted.setDescription(equipmentDTO.description, language);

        // update language specifics for equipment
        persisted.setConditions(equipmentDTO.conditions, language);
        persisted.setType(equipmentDTO.type, language);
        Equipment update = equipmentRepository.update(persisted);

        return EquipmentDTO.from(update, language, uriInfo.getBaseUri());
    }
}
