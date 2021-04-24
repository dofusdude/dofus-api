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
import de.dofusdu.dto.CreateSetDTO;
import de.dofusdu.dto.SetDTO;
import de.dofusdu.entity.Set;
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

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Optional;

@Path("/{language}/sets")
public class SetResource {

    @ConfigProperty(name = "admin.api.secret")
    String apiKey;

    @Inject
    BaseItemAllApi baseItemAllApi;

    @Inject
    SetRepository setRepository;

    @Inject
    WeaponRepository weaponRepository;

    @Inject
    BranchRepository branchRepository;

    @Inject
    EquipmentRepository equipmentRepository;

    @Inject
    PetRepository petRepository;

    @Context
    UriInfo uriInfo;

    @Inject
    EffectRepository effectRepository;

    @GET
    @Path("/{ankama_id}")
    @Tag(name = "Set")
    @Parameters({
            @Parameter(name = "language", in = ParameterIn.PATH, example = "en", description = "Language as code of length 2."),
            @Parameter(name = "ankama_id", in = ParameterIn.PATH, example = "252")
    })
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "OK",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(oneOf = de.dofusdu.dto.SetDTO.class)
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
    @Timed(name = "setsByIdTime")
    @Counted(name = "setsByIdCounter")
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public SetDTO byId(
            @PathParam("language") String language,
            @PathParam("ankama_id") Long ankamaId
    ) {
        LanguageHelper.checkLanguage(language);
        Optional<Set> set = setRepository.byIdCached(ankamaId);
        if (set.isEmpty()) {
            throw new NotFoundException();
        }

        return SetDTO.from(set.get(), language, uriInfo.getBaseUri());
    }

    @GET
    @Tag(name = "Set")
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
    @Timed(name = "setsAllTime")
    @Counted(name = "setsAllCounter")
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Response all(
            @PathParam("language") String language,
            @QueryParam("search[name]") @DefaultValue("") String query,
            @DefaultValue("-1") @QueryParam("page[number]") int pageNumber,
            @DefaultValue("-1") @QueryParam("page[size]") int pageSize
    ) {
        return baseItemAllApi.all(language, query, pageNumber, pageSize, setRepository, uriInfo.getAbsolutePath());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public SetDTO create(
            @HeaderParam("Authorization") String apiKey,
            @PathParam("language") String language,
            CreateSetDTO setDTO
    ) {
        LanguageHelper.checkLanguage(language);
        if (apiKey == null || !apiKey.equals("Bearer " + this.apiKey)) {
            throw new UnauthorizedException("Wrong secret.");
        }

        // skip ceremonial sets (champion set) as long as no ceremonial items implemented
        if (setDTO.ankamaId == 45) {
            return new SetDTO();
        }

        Optional<Set> set = setRepository.byId(setDTO.ankamaId);
        if (set.isPresent()) {
            throw new BadRequestException();
        }

        // persist entity
        Set persist = setRepository.persist(setDTO.toSet(language, weaponRepository, equipmentRepository, petRepository, effectRepository, branchRepository.main()));

        // convert back to show-DTO //
        return SetDTO.from(persist, language, uriInfo.getBaseUri());
    }

    @PUT
    @Path("/{id}/lang")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public SetDTO updateLanguage(
            @HeaderParam("Authorization") String apiKey,
            @PathParam("language") String language,
            @PathParam("id") Long id,
            CreateSetDTO createSetDTO
    ) {
        LanguageHelper.checkLanguage(language);
        if (apiKey == null || !apiKey.equals("Bearer " + this.apiKey)) {
            throw new UnauthorizedException("Wrong secret.");
        }

        // skip ceremonial sets (champion set) as long as no ceremonial items implemented
        if (createSetDTO.ankamaId == 45) {
            return new SetDTO();
        }

        Optional<Set> set = setRepository.byId(createSetDTO.ankamaId);
        if (set.isEmpty()) {
            throw new NotFoundException();
        }

        Set persisted = set.get();

        persisted.setName(createSetDTO.name, language);
        persisted.setAnkamaUrl(createSetDTO.ankamaUrl, language);

        Set update = setRepository.update(persisted);
        return SetDTO.from(update, language, uriInfo.getBaseUri());
    }

}
