/*
 * DstToHubMappingConfigurationDialogViewModel.java
 *
 * Copyright (c) 2020-2022 RHEA System S.A.
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
package ViewModels.Dialogs;

import static Utils.Operators.Operators.AreTheseEquals;
import static Utils.Stereotypes.StereotypeUtils.GetChildren;
import static Utils.Stereotypes.StereotypeUtils.GetShortName;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.emf.ecore.EObject;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.Structure;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.requirement.Requirement;
import org.polarsys.capella.core.data.requirement.RequirementsPkg;

import DstController.IDstController;
import Enumerations.MappedElementRowStatus;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Utils.Ref;
import Utils.Stereotypes.StereotypeUtils;
import ViewModels.CapellaObjectBrowser.Interfaces.ICapellaObjectBrowserViewModel;
import ViewModels.CapellaObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.CapellaObjectBrowser.Rows.ElementRowViewModel;
import ViewModels.Dialogs.Interfaces.IDstToHubMappingConfigurationDialogViewModel;
import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import ViewModels.MappedElementListView.Interfaces.ICapellaMappedElementListViewViewModel;
import ViewModels.MappedElementListView.Interfaces.IMappedElementListViewViewModel;
import ViewModels.Rows.MappedDstRequirementRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import Views.Dialogs.CapellaDstToHubMappingConfigurationDialog;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import io.reactivex.Observable;

/**
 * The {@linkplain DstToHubMappingConfigurationDialogViewModel} is the main view model for the {@linkplain CapellaDstToHubMappingConfigurationDialog}
 */
public class DstToHubMappingConfigurationDialogViewModel extends MappingConfigurationDialogViewModel<EObject> implements IDstToHubMappingConfigurationDialogViewModel
{    
    /**
     * Initializes a new {@linkplain DstToHubMappingConfigurationDialogViewModel}
     * 
     * @param dstController the {@linkplain IDstController}
     * @param hubController the {@linkplain IHubController}
     * @param elementDefinitionBrowserViewModel the {@linkplain IElementDefinitionBrowserViewModel}
     * @param requirementBrowserViewModel the {@linkplain IRequirementBrowserViewModel}
     * @param capellaObjectBrowserViewModel the {@linkplain ICapellaObjectBrowserViewModel}
     * @param mappedElementListViewViewModel the {@linkplain IMappedElementListViewViewModel}
     */
    public DstToHubMappingConfigurationDialogViewModel(IDstController dstController, IHubController hubController, 
            IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel, IRequirementBrowserViewModel requirementBrowserViewModel,
            ICapellaObjectBrowserViewModel capellaObjectBrowserViewModel, IMappedElementListViewViewModel mappedElementListViewViewModel)
    {
        super(dstController, hubController, elementDefinitionBrowserViewModel, requirementBrowserViewModel, 
                capellaObjectBrowserViewModel, mappedElementListViewViewModel);
        
        this.InitializeObservables();
    }
    
    /**
     * Initializes the {@linkplain Observable}s of this view model
     */
    protected void InitializeObservables()
    {
        super.InitializeObservables();
        
        this.capellaObjectBrowserViewModel.GetSelectedElement()
            .subscribe(x -> this.UpdateMappedElements(x));
    }
    
    /**
     * Updates the mapped element collection 
     * 
     * @param rowViewModel the {@linkplain IElementRowViewModel}
     */
    private void UpdateMappedElements(ElementRowViewModel<? extends CapellaElement> rowViewModel)
    {
        var optionalMappedElement = this.mappedElements.stream()
            .filter(x -> AreTheseEquals(x.GetDstElement().getId(), rowViewModel.GetElement().getId()))
            .findFirst();
        
        if(!optionalMappedElement.isPresent())
        {
            MappedElementRowViewModel<? extends Thing, ? extends CapellaElement> mappedElement;
            
            if(rowViewModel.GetElement() instanceof Component)
            {
                mappedElement = new MappedElementDefinitionRowViewModel((Component) rowViewModel.GetElement(), MappingDirection.FromDstToHub);
            }
            else
            {
                mappedElement = new MappedDstRequirementRowViewModel((Requirement) rowViewModel.GetElement(), MappingDirection.FromDstToHub);
            }

            this.mappedElements.add(mappedElement);
            this.SetSelectedMappedElement(mappedElement);
        }
        else
        {
            this.SetSelectedMappedElement(optionalMappedElement.get());
        }
    }

    /**
     * Updates this view model properties
     */
    @Override
    protected void UpdateProperties()
    {
        this.UpdateProperties(this.dstController.GetDstMapResult());
        this.capellaObjectBrowserViewModel.BuildTree(this.originalSelection);
        ((ICapellaMappedElementListViewViewModel)this.mappedElementListViewViewModel).SetShouldDisplayTargetArchitectureColumn(false);
    }

    /**
     * Pre-map the selected elements
     * 
     * @param selectedElement the collection of {@linkplain #TElement}
     */
    @Override
    protected void PreMap(Collection<EObject> selectedElements)
    {
        for (var element : selectedElements)
        {
            if(element instanceof Structure)
            {
                this.PreMap((Collection<EObject>) GetChildren((Structure)element));
            }
            else if (element instanceof CapellaElement)
            {
                var mappedElement = this.GetMappedElementRowViewModel((CapellaElement)element);

                if(mappedElement != null)
                {
                    this.mappedElements.add(mappedElement);
                }
            }
        }
    }
    
    /**
     * Get a {@linkplain MappedElementRowViewModel} that represents a pre-mapped {@linkplain Class}
     * 
     * @param capellaElement the {@linkplain Class} element
     * @return a {@linkplain MappedElementRowViewModel}
     */
    protected MappedElementRowViewModel<? extends Thing, ? extends CapellaElement> GetMappedElementRowViewModel(CapellaElement capellaElement)
    {
        Ref<Boolean> refShouldCreateNewTargetElement = new Ref<>(Boolean.class, false);
        MappedElementRowViewModel<? extends Thing, ? extends CapellaElement> mappedElementRowViewModel = null;
        
        if(capellaElement instanceof Component)
        {
            Ref<ElementDefinition> refElementDefinition = new Ref<>(ElementDefinition.class);
            
            if(this.TryGetElementDefinition((Component)capellaElement, refElementDefinition, refShouldCreateNewTargetElement))
            {
                mappedElementRowViewModel = 
                        new MappedElementDefinitionRowViewModel(refElementDefinition.Get(), (Component)capellaElement, MappingDirection.FromDstToHub);
            }
        }
        else if(capellaElement instanceof Requirement)
        {
            Ref<RequirementsSpecification> refRequirementSpecification = new Ref<>(RequirementsSpecification.class);
            
            if(this.TryGetRequirementSpecification((Requirement)capellaElement, refRequirementSpecification, refShouldCreateNewTargetElement))
            {
                mappedElementRowViewModel = 
                        new MappedDstRequirementRowViewModel(refRequirementSpecification.Get(), (Requirement)capellaElement, MappingDirection.FromDstToHub);
            }
        }
        
        if(mappedElementRowViewModel != null)
        {
            mappedElementRowViewModel.SetShouldCreateNewTargetElement(refShouldCreateNewTargetElement.Get());
            mappedElementRowViewModel.SetRowStatus(Boolean.TRUE.equals(refShouldCreateNewTargetElement.Get()) ? MappedElementRowStatus.NewElement : MappedElementRowStatus.ExisitingElement);
            return mappedElementRowViewModel;
        }
        
        return null;
    }

    /**
     * Gets or create an {@linkplain ElementDefinition} that can be mapped to the provided {@linkplain Class},
     * In the case the provided {@linkplain Class} is already represented in the {@linkplain mappedElements} returns false
     * 
     * @param element the {@linkplain Component} element
     * @param refElementDefinition the {@linkplain Ref} of {@linkplain ElementDefinition}
     * @param refShouldCreateNewTargetElement the {@linkplain Ref} of {@linkplain Boolean} indicating whether the target Hub element will be created
     * @return a value indicating whether the method execution was successful in getting a {@linkplain ElementDefinition}
     */
    private boolean TryGetElementDefinition(Component element, Ref<ElementDefinition> refElementDefinition, Ref<Boolean> refShouldCreateNewTargetElement)
    {
        if(this.mappedElements.stream().noneMatch(x-> AreTheseEquals(x.GetDstElement().getId(), element.getId())))
        {
            Optional<ElementDefinition> optionalElementDefinition =
                    this.hubController.GetOpenIteration().getElement().stream()
                    .filter(x -> AreTheseEquals(x.getName(), element.getName())).findFirst();
            
            if(optionalElementDefinition.isPresent())
            {
                refElementDefinition.Set(optionalElementDefinition.get().clone(true));
            }
            else
            {
                ElementDefinition elementDefinition = new ElementDefinition();
                elementDefinition.setIid(UUID.randomUUID());
                elementDefinition.setName(element.getName());
                elementDefinition.setShortName(GetShortName(element));
                elementDefinition.setOwner(this.hubController.GetCurrentDomainOfExpertise());

                refElementDefinition.Set(elementDefinition);
                refShouldCreateNewTargetElement.Set(true);
            }
        }
        
        return refElementDefinition.HasValue();
    }

    /**
     * Gets or create an {@linkplain RequirementsSpecification} that can contained a {@linkplain Requirement} that will represent the provided {@linkplain Class},
     * In the case the provided {@linkplain Class} is already represented in the {@linkplain mappedElements} returns false
     * 
     * @param requirement the {@linkplain Class} element
     * @param refRequirementSpecification the {@linkplain Ref} of {@linkplain RequirementsSecification}
     * @param refShouldCreateNewTargetElement the {@linkplain Ref} of {@linkplain Boolean} indicating whether the target Hub element will be created
     * @return a value indicating whether the method execution was successful in getting a {@linkplain RequirementSpecification}
     */
    private boolean TryGetRequirementSpecification(Requirement requirement, Ref<RequirementsSpecification> refRequirementSpecification, Ref<Boolean> refShouldCreateNewTargetElement)
    {
        Optional<RequirementsSpecification> optionalRequirementsSpecification = 
              this.hubController.GetOpenIteration().getRequirementsSpecification().stream()
              .flatMap(x -> x.getRequirement().stream())
              .filter(x -> AreTheseEquals(x.getName(), requirement.getName()))
              .map(x -> x.getContainerOfType(RequirementsSpecification.class))
              .findFirst();

        if(optionalRequirementsSpecification.isPresent())
        {
            UUID optionalRequirementsSpecificationIid = optionalRequirementsSpecification.get().getIid();
            
            if(this.mappedElements.stream().anyMatch(x -> AreTheseEquals(x.GetHubElement().getIid(), optionalRequirementsSpecificationIid)
                    && AreTheseEquals(x.GetDstElement().getId(), requirement.getId())))
            {
                return false;
            }
            
            refRequirementSpecification.Set(optionalRequirementsSpecification.get().clone(true));
        }
        else
        {
            refShouldCreateNewTargetElement.Set(true);
            
            var possibleParent = new Ref<>(RequirementsPkg.class);
            
            if(StereotypeUtils.TryGetPossibleRequirementsSpecificationElement(requirement, possibleParent))
            {
                optionalRequirementsSpecification = this.hubController.GetOpenIteration().getRequirementsSpecification().stream()
                    .filter(x -> AreTheseEquals(possibleParent, x.getName()))
                    .findFirst();
                
                if(optionalRequirementsSpecification.isPresent())
                {
                    refRequirementSpecification.Set(optionalRequirementsSpecification.get().clone(true));
                    return true;
                }
            }
            
            RequirementsSpecification requirementsSpecification = new RequirementsSpecification();
            requirementsSpecification.setName(possibleParent.HasValue() ? possibleParent.Get().getName() : "new RequirementsSpecification");
            requirementsSpecification.setShortName(GetShortName(possibleParent.HasValue() ? possibleParent.Get().getName() : requirementsSpecification.getName()));
            requirementsSpecification.setIid(UUID.randomUUID());
            requirementsSpecification.setOwner(this.hubController.GetCurrentDomainOfExpertise());
            refRequirementSpecification.Set(requirementsSpecification);            
        }

        return refRequirementSpecification.HasValue();
    }

    /**
     * Occurs when the user sets the target element of the current mapped element to be a
     * 
     * @param selected the new {@linkplain boolean} value
     */
    @Override
    public void WhenMapToNewElementCheckBoxChanged(boolean selected)
    {
        super.WhenMapToNewElementCheckBoxChanged(selected);
        
        if(selected && !this.selectedMappedElement.Value().GetShouldCreateNewTargetElementValue())
        {
            this.selectedMappedElement.Value().SetHubElement(null);
        }
                
        if(this.selectedMappedElement.Value().GetHubElement() instanceof ElementDefinition)
        {
            this.UpdateRowStatus(this.selectedMappedElement.Value(), ElementDefinition.class);
        }
        else if(this.selectedMappedElement.Value().GetHubElement() instanceof RequirementsSpecification)
        {
            this.UpdateRowStatus(this.selectedMappedElement.Value(), RequirementsSpecification.class);
        }
    }
}