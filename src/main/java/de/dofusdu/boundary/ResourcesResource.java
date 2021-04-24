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
import de.dofusdu.dto.CreateRecipePositionDTO;
import de.dofusdu.dto.CreateResourceDTO;
import de.dofusdu.dto.ResourceDTO;
import de.dofusdu.entity.Consumable;
import de.dofusdu.entity.Item;
import de.dofusdu.entity.RecipePosition;
import de.dofusdu.entity.Resource;
import de.dofusdu.gateway.BranchRepository;
import de.dofusdu.gateway.ConsumableRepository;
import de.dofusdu.gateway.ItemFinder;
import de.dofusdu.gateway.ResourceRepository;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestScoped
@Path("/{language}/resources")
public class ResourcesResource {

    @Inject
    BaseItemAllApi baseItemAllApi;

    @Inject
    ResourceRepository resourceRepository;

    @Inject
    ConsumableRepository consumableRepository;

    @Inject
    BranchRepository branchRepository;

    @ConfigProperty(name = "admin.api.secret")
    String apiKey;

    @Inject
    ItemFinder itemFinder;

    @Context
    UriInfo uriInfo;

    @GET
    @Path("/{ankama_id}")
    @Tag(name = "Resource")
    @Parameters({
            @Parameter(name = "language", in = ParameterIn.PATH, example = "en", description = "Language as code of length 2."),
            @Parameter(name = "ankama_id", in = ParameterIn.PATH, example = "21968")
    })
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "OK",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = de.dofusdu.dto.ResourceDTO.class)
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
    @Timed(name = "resourcesByIdTime")
    @Counted(name = "resourcesByIdCounter")
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public ResourceDTO byId(@PathParam("language") String language,
                            @PathParam("ankama_id") Long ankamaId) {
        LanguageHelper.checkLanguage(language);
        Optional<Resource> resource = resourceRepository.byIdCached(ankamaId);
        if (resource.isEmpty()) {
            throw new NotFoundException();
        }
        return ResourceDTO.from(resource.get(), language, uriInfo.getBaseUri());
    }

    @GET
    @Tag(name = "Resource")
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
    @Timed(name = "resourcesAllTime")
    @Counted(name = "resourcesAllCounter")
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Response all(@PathParam("language") String language,
                        @QueryParam("search[name]") @DefaultValue("") String query,
                        @DefaultValue("-1") @QueryParam("page[number]") int pageNumber,
                        @DefaultValue("-1") @QueryParam("page[size]") int pageSize
    ) {
        return baseItemAllApi.all(language, query, pageNumber, pageSize, resourceRepository, uriInfo.getAbsolutePath());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public ResourceDTO create(@HeaderParam("Authorization") String apiKey,
                              @PathParam("language") String language,
                              CreateResourceDTO resource) {
        LanguageHelper.checkLanguage(language);
        if (apiKey == null || !apiKey.equals("Bearer " + this.apiKey)) {
            throw new UnauthorizedException("Wrong secret.");
        }

        Optional<Resource> resource1 = resourceRepository.byId(resource.ankamaId);
        if (resource1.isPresent()) {
            throw new BadRequestException();
        }

        // resources includes consumables, so skip if already in consumable
        Optional<Consumable> consumable = consumableRepository.byId(resource.ankamaId);
        if (consumable.isPresent()) {
            throw new BadRequestException();
        }

        Collection<CreateRecipePositionDTO> recipe = resource.recipe;
        List<RecipePosition> conRecipe = null;
        if (recipe != null && !recipe.isEmpty()) {
            conRecipe = recipe.stream().map(el -> {
                Optional<Item> item = itemFinder.findItem(el.itemId);
                if (item.isPresent()) {
                    return new RecipePosition(item.get(), el.quantity);
                }

                throw new NotFoundException("AnkaID from Recipe not found!");
            })
                    .collect(Collectors.toList());
        }

        Resource r = resource.toResource(language, conRecipe, branchRepository.main());
        Resource persist = resourceRepository.persist(r);

        return ResourceDTO.from(persist, language, uriInfo.getBaseUri());
    }

    @PUT
    @Path("/{id}/lang")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public ResourceDTO updateLanguage(@HeaderParam("Authorization") String apiKey,
                                      @PathParam("language") String language,
                                      @PathParam("id") Long id,
                                      ResourceDTO resource) {
        LanguageHelper.checkLanguage(language);
        if (apiKey == null || !apiKey.equals("Bearer " + this.apiKey)) {
            throw new UnauthorizedException("Wrong secret.");
        }

        Optional<Resource> resource1 = resourceRepository.byId(id);
        if (resource1.isEmpty()) {
            throw new NotFoundException();
        }

        Resource persisted = resource1.get();

        // base item
        persisted.setName(resource.name, language);
        persisted.setType(resource.type, language);
        persisted.setAnkamaUrl(resource.ankamaUrl, language);
        persisted.setDescription(resource.description, language);

        // update new languages
        persisted.setType(resource.type, language);
        Resource updated = resourceRepository.update(persisted);

        return ResourceDTO.from(updated, language, uriInfo.getBaseUri());
    }
}
