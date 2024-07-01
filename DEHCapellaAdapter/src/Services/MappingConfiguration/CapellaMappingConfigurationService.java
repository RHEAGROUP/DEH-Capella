/*
 * CapellaMappingConfigurationService.java
 *
 * Copyright (c) 2020-2024 Starion Group S.A.
 *
 * Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski, Antoine Théate
 *
 * This file is part of DEH-Capella
 *
 * The DEH-Capella is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * The DEH-Capella is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package Services.MappingConfiguration;

import static Utils.Operators.Operators.AreTheseEquals;
import static Utils.Stereotypes.StereotypeUtils.GetChildren;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.polarsys.capella.common.data.modellingcore.AbstractNamedElement;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.requirement.Requirement;

import Enumerations.CapellaArchitecture;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.CapellaSession.ICapellaSessionService;
import Services.CapellaTransaction.ICapellaTransactionService;
import Utils.Ref;
import Utils.StreamExtensions;
import ViewModels.Interfaces.IHaveTargetArchitecture;
import ViewModels.Interfaces.IMappedElementRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedHubRequirementRowViewModel;
import ViewModels.Rows.MappedRequirementBaseRowViewModel;
import ViewModels.Rows.MappedDstRequirementRowViewModel;
import cdp4common.commondata.NamedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ElementUsage;
import cdp4common.engineeringmodeldata.ExternalIdentifierMap;
import cdp4common.engineeringmodeldata.RequirementsSpecification;

/**
 * The {@linkplain CapellaMappingConfigurationService} is the implementation of {@linkplain MappingConfigurationService} for the Capella adapter
 */
public class CapellaMappingConfigurationService extends MappingConfigurationService<CapellaElement, CapellaExternalIdentifier> implements ICapellaMappingConfigurationService
{
    /**
     * The {@linkplain ICapellaSessionService} instance
     */
    private final ICapellaSessionService sessionService;
    
    /**
     * The {@linkplain ICapellaTransactionService}
     */
    private final ICapellaTransactionService transactionService;

    /**
     * Initializes a new {@linkplain MagicDrawMappingConfigurationService}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param sessionService the {@linkplain ICapellaSessionService}
     * @param transactionService the {@linkplain ICapellaTransactionService}
     */
    public CapellaMappingConfigurationService(IHubController hubController, ICapellaSessionService sessionService, ICapellaTransactionService transactionService)
    {
        super(hubController, CapellaExternalIdentifier.class);
        this.sessionService = sessionService;
        this.transactionService = transactionService;
        
        this.hubController.GetIsSessionOpenObservable()
        .subscribe(x -> 
        {
            if(!x)
            {
                this.correspondences.clear();
                this.SetExternalIdentifierMap(new ExternalIdentifierMap());
            }
        });
    }
    
    /**
     * Loads the mapping configuration and generates the map result respectively
     * 
     * @return a {@linkplain Collection} of {@linkplain IMappedElementRowViewModel}
     */
    @Override
    public Collection<IMappedElementRowViewModel> LoadMapping()
    {
        var mappedElements = new ArrayList<IMappedElementRowViewModel>();    
        var sessionsAndElementsMap = this.sessionService.GetAllCapellaElementsFromOpenSessions();
        
        for (var sessionUri : sessionsAndElementsMap.keySet())
        {
            mappedElements.addAll(this.LoadMapping(sessionsAndElementsMap.get(sessionUri)));
        }
                
        return mappedElements;
    }

    /**
     * Loading all mapped HubElement that misses their target on the currently loaded Capella Model 
     * 
     * @param mappedElements the {@linkplain Collection} of currently {@linkplain IMappedElementRowViewModel}   
     */
    private void LoadMappingForMissingCapellaElement(ArrayList<IMappedElementRowViewModel> mappedElements)
    {
        var mappedElementRowViewModels = StreamExtensions.OfType(mappedElements, MappedElementRowViewModel.class);
        
        var correspondencesNotLoaded = this.correspondences.stream()
                .filter(x -> x.middle.MappingDirection == MappingDirection.FromHubToDst && mappedElementRowViewModels.stream()
                        .noneMatch(p -> p.GetHubElement() != null && AreTheseEquals(x.right, p.GetHubElement().getIid())))
                .collect(Collectors.toList());
        
        for (MutableTriple<UUID, CapellaExternalIdentifier, UUID> correspondence : correspondencesNotLoaded)
        {
            var refMappedElement = new Ref<IMappedElementRowViewModel>(IMappedElementRowViewModel.class);
            
            try
            {
                if(this.TryGetMappedElement(correspondence, MappedHubRequirementRowViewModel.class, cdp4common.engineeringmodeldata.Requirement.class, refMappedElement)
                        || this.TryGetMappedElement(correspondence, MappedElementDefinitionRowViewModel.class, ElementDefinition.class, refMappedElement))
                {
                    mappedElements.add((IMappedElementRowViewModel) refMappedElement.Get());
                }
                else
                {
                    this.logger.warn(String.format("Could not initialize the IMappedElementRowViewModel for the internalIid [%s] and externalId [%s]", 
                            correspondence.getRight(), correspondence.getMiddle().Identifier));
                }
            }
            catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException exception)
            {
                this.logger.catching(exception);
            }
        }
    }

    /**
     * Tries to get the {@linkplain IMappedElementRowViewModel} based on the provided correspondence represented by a 
     * {@linkplain Triple} of {@linkplain UUID}, {@linkplain CapellaExternalIdentifier}, {@linkplain UUID}
     * 
     * if the represented thing is of the provided {@linkplain Class} thingType, a rowViewModelType is initialized and 
     * sets as the value carried by the provided {@linkplain Ref} of {@linkplain IMappedElementRowViewModel}
     * 
     * @param <TThing> the type of {@linkplain Thing} the internalId of the correspondence could represent
     * @param <TRowViewModel> the type of {@linkplain IMappedElementRowViewModel} & {@linkplain IHaveTargetArchitecture} that should be initialized if the Thing is found
     * @param correspondence a {@linkplain Triple} of {@linkplain UUID}, {@linkplain CapellaExternalIdentifier}, {@linkplain UUID} representing an id correspondence
     * @param rowViewModelType the {@linkplain Class} of {@linkplain #TRowViewModel}
     * @param thingType the {@linkplain Class} of {@linkplain #TThing}
     * @param refMappedElement the {@linkplain Ref} of {@linkplain IMappedElementRowViewModel}
     * @return a value indicating whether the {@linkplain IMappedElementRowViewModel} could be initialized
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    private <TThing extends Thing, TRowViewModel extends IMappedElementRowViewModel & IHaveTargetArchitecture> boolean TryGetMappedElement(
            MutableTriple<UUID, CapellaExternalIdentifier, UUID> correspondence, Class<TRowViewModel> rowViewModelType, Class<TThing> thingType, Ref<IMappedElementRowViewModel> refMappedElement) 
                    throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
    {
        var refHubElement = new Ref<TThing>(thingType);
        
        if(this.hubController.TryGetThingById(correspondence.getRight(), refHubElement))
        {
            TRowViewModel newMappedElement = rowViewModelType.getDeclaredConstructor(thingType, MappingDirection.class).newInstance(refHubElement.Get(), MappingDirection.FromHubToDst);
                
            newMappedElement.SetTargetArchitecture(correspondence.getMiddle().TargetArchitecture);
            refMappedElement.Set(newMappedElement);
        }
        
        return refMappedElement.HasValue();
    }

    /**
     * Loads the mapping configuration and generates the map result respectively
     * 
     * @param elements a {@linkplain Collection} of {@code TDstElement}
     * @return a {@linkplain Collection} of {@linkplain IMappedElementRowViewModel}
     */
    @Override
    public Collection<IMappedElementRowViewModel> LoadMapping(Collection<CapellaElement> elements)
    {
        var mappedElements = new ArrayList<IMappedElementRowViewModel>();
        
        for (var element : elements)
        {
            mappedElements.addAll(this.GetMappedElements(element));
        }
        
        return mappedElements;
    }
    
    /**
     * Retrieves the mapped elements for the given CapellaElement.
     *
     * @param element The CapellaElement for which mapped elements are retrieved.
     * @return A collection of IMappedElementRowViewModel representing the mapped elements.
     */
    private Collection<IMappedElementRowViewModel> GetMappedElements(CapellaElement element)
    {
        var correspondences = this.correspondences.stream()
                .filter(x -> AreTheseEquals(x.middle.Identifier, element.getId()))
                .collect(Collectors.toList());
        
        var result = new ArrayList<IMappedElementRowViewModel>();
        
        for(var correspondence : correspondences)
        {
            var mappingDirection = correspondence.middle.MappingDirection;
            var targetArchitecture = correspondence.middle.TargetArchitecture;
            var internalId = correspondence.right;
            
            if(!(targetArchitecture == null || targetArchitecture.AreSameArchitecture(element)))
            {
                continue;
            }
            
            if(element instanceof Part)
            {
                var refElementUsage = new Ref<>(ElementUsage.class);
                
                var mappedElement = new MappedElementDefinitionRowViewModel(
                        mappingDirection == MappingDirection.FromDstToHub 
                        ? (Part)element 
                        : this.transactionService.Clone((Part)element), mappingDirection);
                
                mappedElement.SetTargetArchitecture(targetArchitecture);
                
                if(this.hubController.TryGetThingById(internalId, refElementUsage))
                {
                    mappedElement.SetHubElement(refElementUsage.Get().clone(false));
                }
                else
                {
                    this.logger.error(String.format("ElementUsage for %s not found", ((NamedElement) element).getName()));
                    continue;
                }
                            
                result.add(mappedElement);
            }
            
            if(element instanceof Component)
            {
                var refElementDefinition = new Ref<>(ElementDefinition.class);
                
                var mappedElement = new MappedElementDefinitionRowViewModel(
                        mappingDirection == MappingDirection.FromDstToHub 
                        ? (Component)element 
                        : this.transactionService.Clone((Component)element), mappingDirection);
                
                mappedElement.SetTargetArchitecture(targetArchitecture);
                
                if(this.hubController.TryGetThingById(internalId, refElementDefinition))
                {
                    mappedElement.SetHubElement(refElementDefinition.Get().clone(false));
                }
                            
                result.add(mappedElement);
            }
            else if(element instanceof Requirement)
            {
                if(mappingDirection == MappingDirection.FromHubToDst)
                {
                    var mappedElement = new MappedHubRequirementRowViewModel(this.transactionService.Clone((Requirement)element), mappingDirection);
                    this.GetMappedRequirement(mappedElement, internalId);
                    mappedElement.SetTargetArchitecture(targetArchitecture);
                    
                    result.add(mappedElement);
                }
                else
                {
                    var mappedElement = new MappedDstRequirementRowViewModel((Requirement)element, mappingDirection);
                    this.GetMappedRequirement(mappedElement, internalId);
                    result.add(mappedElement);
                }            
            }
        }
        
        return result;
    }

    /**
     * Gets the mapped {@linkplain cdp4common.engineeringmodeldata.Requirement}
     * 
     * @param mappedElement the {@linkplain MappedRequirementBaseRowViewModel}
     * @param internalId the internal id of the queried Thing
     * @param clazz the {@linkplain Class} of the queried Thing
     */
    private void GetMappedRequirement(MappedRequirementBaseRowViewModel mappedElement, UUID internalId)
    {
        var refHubRequirement = new Ref<>(cdp4common.engineeringmodeldata.Requirement.class);
        
        if(this.hubController.TryGetThingById(internalId, refHubRequirement))
        {
            var requirementSpecification = refHubRequirement.Get().getContainerOfType(RequirementsSpecification.class).clone(true);
            
            mappedElement.SetHubElement(requirementSpecification.getRequirement().stream()
                    .filter(x -> AreTheseEquals(x.getIid(), refHubRequirement.Get().getIid()) && !x.isDeprecated())
                    .findFirst()
                    .orElse(null));
        }
    }

    /**
     * Creates a new {@linkplain ExternalIdentifierMap} and sets the current as the new one
     * 
     * @param newName the {@linkplain String} name of the new configuration
     * @param addTheTemporyMapping a value indicating whether the current temporary {@linkplain ExternalIdentifierMap} 
     * contained correspondence should be transfered the new one
     * 
     * @return the new configuration {@linkplain ExternalIdentifierMap}
     */
    @Override
    public ExternalIdentifierMap CreateExternalIdentifierMap(String newName, String modelName, boolean addTheTemporyMapping)
    {
        return super.CreateExternalIdentifierMap(newName, modelName, DstController.DstController.THISTOOLNAME, addTheTemporyMapping);
    }

    /**
     * Adds one correspondence to the {@linkplain ExternalIdentifierMap}
     * 
     * @param internalId the {@linkplain UUID} that identifies the thing to correspond to
     * @param externalId the {@linkplain Object} that identifies the object to correspond to
     * @param mappingDirection the {@linkplain MappingDirection} the mapping belongs to
     * @param targetArchitecture the target {@linkplain CapellaArchitecture}
     */
    @Override
    public void AddToExternalIdentifierMap(UUID internalId, String externalId, CapellaArchitecture targetArchitecture,
            MappingDirection mappingDirection)
    {
        var externalIdentifier = new CapellaExternalIdentifier();
        externalIdentifier.MappingDirection = mappingDirection;
        externalIdentifier.Identifier = externalId;
        externalIdentifier.TargetArchitecture = targetArchitecture;
        
        this.AddToExternalIdentifierMap(internalId, externalIdentifier, 
                x -> x.getMiddle().TargetArchitecture == targetArchitecture
                    && AreTheseEquals(x.getMiddle().MappingDirection, mappingDirection)
                    && AreTheseEquals(x.getRight(), internalId));
    }
}
