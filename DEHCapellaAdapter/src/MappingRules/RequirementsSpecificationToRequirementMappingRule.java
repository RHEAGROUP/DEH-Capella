/*
 * RequirementsSpecificationToRequirementMappingRule.java
 *
 * Copyright (c) 2020-2024 Starion Group S.A.
 *
 * Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski 
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
package MappingRules;

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.ArrayList;

import org.eclipse.emf.common.util.EList;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.requirement.Requirement;
import org.polarsys.capella.core.data.requirement.RequirementsPkg;

import App.AppContainer;
import DstController.IDstController;
import Enumerations.CapellaArchitecture;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.CapellaTransaction.ICapellaTransactionService;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Utils.Ref;
import Utils.Stereotypes.CapellaRequirementCollection;
import Utils.Stereotypes.CapellaTypeEnumerationUtility;
import Utils.Stereotypes.HubRequirementCollection;
import Utils.Stereotypes.RequirementType;
import ViewModels.Rows.MappedHubRequirementRowViewModel;
import ViewModels.Rows.MappedDstRequirementRowViewModel;
import cdp4common.commondata.NamedThing;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.Category;

/**
 * The {@linkplain RequirementsSpecificationToRequirementMappingRule} is the mapping rule implementation for transforming {@linkplain RequirementsSpecification} into {@linkplain CapellaRequirementCollection}
 */
public class RequirementsSpecificationToRequirementMappingRule extends HubToDstBaseMappingRule<HubRequirementCollection, ArrayList<MappedHubRequirementRowViewModel>>
{
    /**
     * The collection of {@linkplain RequirementsSpecification} that are being mapped
     */
    private ArrayList<RequirementsSpecification> requirementsSpecifications = new ArrayList<RequirementsSpecification>();

    /**
     * The temporary collection of {@linkplain RequirementsPkg} that were created during this mapping
     */
    private ArrayList<RequirementsPkg> temporaryRequirementsContainer = new ArrayList<>();

    /**
     * Initializes a new {@linkplain RequirementToRequirementsSpecificationMappingRule}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain ICapellaMappingConfigurationService}
     * @param transactionService the {@linkplain ICapellaTransactionService}
     */
    public RequirementsSpecificationToRequirementMappingRule(IHubController hubController, 
            ICapellaMappingConfigurationService mappingConfiguration, ICapellaTransactionService transactionService)
    {
        super(hubController, mappingConfiguration, transactionService);
    }    
    
    /**
     * Transforms an {@linkplain CapellaRequirementCollection} of type {@linkplain Requirement} to an {@linkplain ArrayList} of {@linkplain RequirementsSpecification}
     * 
     * @param input the {@linkplain CapellaRequirementCollection} to transform
     * @return the {@linkplain ArrayList} of {@linkplain MappedDstRequirementRowViewModel}
     */
    @Override
    public ArrayList<MappedHubRequirementRowViewModel> Transform(Object input)
    {
        try
        {
            if(this.dstController == null)
            {
                this.dstController = AppContainer.Container.getComponent(IDstController.class);
            }
            
            var mappedElements = this.CastInput(input);
            this.Map(mappedElements);
            
            this.SaveMappingConfiguration(mappedElements, MappingDirection.FromHubToDst);
            return new ArrayList<MappedHubRequirementRowViewModel>(mappedElements);
        }
        catch (Exception exception)
        {
            this.logger.catching(exception);
            return new ArrayList<MappedHubRequirementRowViewModel>();
        }
        finally
        {
            this.requirementsSpecifications.clear();
            this.temporaryRequirementsContainer.clear();
        }
    }
    
    /**
     * Maps the provided collection of requirements
     * 
     * @param mappedRequirements the collection of {@linkplain Requirement} to map
     */
    private void Map(HubRequirementCollection mappedRequirements)
    {
        for (var mappedRequirementRowViewModel : mappedRequirements)
        {
            if(mappedRequirementRowViewModel.GetDstElement() == null)
            {
                mappedRequirementRowViewModel.SetDstElement(this.GetOrCreateRequirement(mappedRequirementRowViewModel));
            }
            else
            {
                this.transactionService.RegisterTargetArchitecture(mappedRequirementRowViewModel.GetDstElement(), mappedRequirementRowViewModel.GetTargetArchitecture());
            }
            
            this.UpdateProperties(mappedRequirementRowViewModel.GetHubElement(), mappedRequirementRowViewModel.GetDstElement());
            
            this.UpdateOrCreateRequirementPackages(mappedRequirementRowViewModel.GetHubElement(), mappedRequirementRowViewModel.GetDstElement(), mappedRequirementRowViewModel.GetTargetArchitecture());
        }
    }

    /**
     * Updates the target requirement properties
     * 
     * @param <TRequirement> the type of {@linkplain Requirement}
     * @param hubRequirement the {@linkplain cdp4common.engineeringmodeldata.Requirement} element that represents the requirement in the Hub
     * @param requirement the {@linkplain Requirement} to update
     */
    private <TRequirement extends Requirement> void UpdateProperties(cdp4common.engineeringmodeldata.Requirement hubRequirement, TRequirement dstRequirement)
    {
        dstRequirement.setRequirementId(hubRequirement.getShortName());
        dstRequirement.setName(hubRequirement.getName());
        this.UpdateOrCreateDefinition(hubRequirement, dstRequirement);
    }

    /**
     * Gets or creates the {@linkplain RequirementsPkg} that can represent the {@linkplain RequirementsSpecification}
     * 
     * @param mappedRequirementRowViewModel the {@linkplain MappedDstRequirementRowViewModel}
     * @return a {@linkplain RequirementsPkg}
     */
    private Requirement GetOrCreateRequirement(MappedHubRequirementRowViewModel mappedRequirementRowViewModel)
    {
        var refRequirementType = new Ref<RequirementType>(RequirementType.class);
        
        if(!this.TryGetRequirementClass(mappedRequirementRowViewModel.GetHubElement(), refRequirementType))
        {
            refRequirementType.Set(RequirementType.User);
        }
        
        return this.GetOrCreateRequirement(mappedRequirementRowViewModel.GetHubElement(), mappedRequirementRowViewModel.GetTargetArchitecture(), refRequirementType.Get().ClassType());
    }

    /**
     * Gets or creates the {@linkplain RequirementsPkg} that can represent the {@linkplain RequirementsSpecification}
     * 
     * @param <TRequirement> the type of {@linkplain Requirement} to get
     * @param hubRequirement the {@linkplain cdp4common.engineeringmodeldata.Requirement}
     * @param targetArchitecture the {@linkplain CapellaArchitecture}
     * @param requirementType the {@linkplain Class} of {@linkplain Requirement}
     * @return a {@linkplain RequirementsPkg}
     */
    private <TRequirement extends Requirement> TRequirement GetOrCreateRequirement(cdp4common.engineeringmodeldata.Requirement hubRequirement, CapellaArchitecture targetArchitecture, Class<TRequirement> requirementType)
    {
        var refElement = new Ref<>(requirementType);
        
        if(!this.dstController.TryGetElementBy(x -> x instanceof NamedElement && 
                AreTheseEquals(((NamedElement) x).getName(), hubRequirement.getName(), true) && targetArchitecture.AreSameArchitecture(x), refElement))
        {        
            var newRequirement = this.transactionService.Create(requirementType, hubRequirement.getName(), targetArchitecture);
            refElement.Set(newRequirement);
        }
        else
        {
            refElement.Set(this.transactionService.Clone(refElement.Get()));
        }
        
        return refElement.Get();
    }
    
    /**
     * Updates or creates the definition according to the provided {@linkplain Requirement} assignable to the {@linkplain Requirement}  
     * 
     * @param <TRequirement> the type of {@linkplain Requirement}
     * @param hubRequirement the {@linkplain cdp4common.engineeringmodeldata.Requirement} element that represents the requirement in the Hub
     * @param requirement the {@linkplain Requirement} to update
     */
    private <TRequirement extends Requirement> void UpdateOrCreateDefinition(cdp4common.engineeringmodeldata.Requirement hubRequirement, TRequirement requirement)
    {
        if(!hubRequirement.getDefinition().isEmpty())
        {
            var definition = hubRequirement.getDefinition()
                    .stream()
                    .filter(x -> x.getLanguageCode().equalsIgnoreCase("en"))
                    .findFirst()
                    .orElse(hubRequirement.getDefinition().get(0));
            
            requirement.setDescription(definition.getContent());
        }
        else
        {
            requirement.setDescription("");
        }
    }
    
    /**
     * Gets the {@linkplain RequirementType} based on the {@linkplain Category} applied to the provided {@linkplain cdp4common.engineeringmodeldata.Requirement}
     * 
     * @param requirement the {@linkplain cdp4common.engineeringmodeldata.Requirement}
     * @param refRequirementType the {@linkplain Ref} of {@linkplain RequirementType}
     * @return a {@linkplain boolean} indicating whether the {@linkplain RequirementType} is different than the default value
     */
    private boolean TryGetRequirementClass(cdp4common.engineeringmodeldata.Requirement requirement, Ref<RequirementType> refRequirementType)
    {
        for (var category : requirement.getCategory())
        {
            var requirementType = CapellaTypeEnumerationUtility.RequirementTypeFrom(category.getName());
            
            if(requirementType != null)
            {
                refRequirementType.Set(requirementType);
                break;
            }
        }
        
        return refRequirementType.HasValue();
    }
    
    /**
     * Gets or creates the {@linkplain RequirementsPkg} that can represent the {@linkplain RequirementsSpecification} or one of its {@linkplain RequirementsGroup}
     * 
     * @param hubRequirement the {@linkplain cdp4common.engineeringmodeldata.Requirement} element that represents the requirement in the Hub
     * @param requirement the {@linkplain Requirement} to update
     * @param targetArchitecture the {@linkplain CapellaArchitecture}
     */
    private void UpdateOrCreateRequirementPackages(cdp4common.engineeringmodeldata.Requirement hubRequirement, Requirement requirement, CapellaArchitecture targetArchitecture)
    {
        var hubRequirementSpecification = hubRequirement.getContainerOfType(RequirementsSpecification.class);
        
        var requirementsSpecification = this.GetOrCreateRequirementContainer(hubRequirementSpecification, targetArchitecture);
                
        var container = hubRequirement.getGroup();
        CapellaElement lastChild = requirement;
        
        while(container != null && !AreTheseEquals(container.getIid(), hubRequirementSpecification.getIid()))
        {
            var newGroup = this.GetOrCreateRequirementContainer(container, targetArchitecture);

            this.AddOrUpdateContainement(lastChild, newGroup);
            
            var upperContainer = container.getContainer();            
            container = upperContainer instanceof RequirementsGroup ? (RequirementsGroup)upperContainer : null;
            lastChild = newGroup;
        }

        this.AddOrUpdateContainement(lastChild, requirementsSpecification);
    }

    /**
     * Adds or update the containedElement to the container
     * 
     * @param containedElement the {@linkplain CapellaElement} contained element
     * @param container the {@linkplain RequirementsPkg} container
     */
    @SuppressWarnings("unchecked")
    private <TElement extends CapellaElement> void AddOrUpdateContainement(TElement containedElement, RequirementsPkg container)
    {
        var collection = (EList<TElement>)container.getOwnedRequirementPkgs();
        
        if(containedElement instanceof Requirement)
        {
            collection = (EList<TElement>)container.getOwnedRequirements();
        }
        
        collection.removeIf(x -> AreTheseEquals(x.getId(), containedElement.getId()));
        collection.add(containedElement);
    }
    
    /**
     * Gets or creates the {@linkplain RequirementsPkg} that can represent the {@linkplain RequirementsSpecification} or one of its {@linkplain RequirementsGroup} 
     * 
     * @param thingContainer the {@linkplain cdp4common.engineeringmodeldata.Requirement} element that represents the requirement in the Hub
     * @param targetArchitecture the {@linkplain CapellaArchitecture}
     * @return a {@linkplain RequirementsPkg}
     */
    private <TThing extends NamedThing> RequirementsPkg GetOrCreateRequirementContainer(TThing thingContainer, CapellaArchitecture targetArchitecture)
    {
        var refElement = new Ref<>(RequirementsPkg.class);
        
        var existingContainer = this.temporaryRequirementsContainer.stream()
                .filter(x -> AreTheseEquals(((NamedElement) x).getName(), thingContainer.getName(), true))
                .findFirst();
        
        if(existingContainer.isPresent())
        {
            refElement.Set(existingContainer.get());
        }
        else
        {
            if(!this.dstController.TryGetElementBy(x -> x instanceof NamedElement && 
                    AreTheseEquals(((NamedElement) x).getName(), thingContainer.getName(), true) && targetArchitecture.AreSameArchitecture(x), refElement))
            {        
                var newRequirementsPackage = this.transactionService.Create(RequirementsPkg.class, thingContainer.getName(), targetArchitecture);
                this.temporaryRequirementsContainer.add(newRequirementsPackage);
                refElement.Set(newRequirementsPackage);
            }
            else
            {
                refElement.Set(this.transactionService.Clone(refElement.Get()));
            }
        }
        
        return refElement.Get();
    }    
}
