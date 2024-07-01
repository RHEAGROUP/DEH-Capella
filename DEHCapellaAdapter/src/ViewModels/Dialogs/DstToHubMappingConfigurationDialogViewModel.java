/*
 * DstToHubMappingConfigurationDialogViewModel.java
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
package ViewModels.Dialogs;

import static Utils.Operators.Operators.AreTheseEquals;
import static Utils.Stereotypes.StereotypeUtils.GetChildren;
import static Utils.Stereotypes.StereotypeUtils.GetShortName;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.emf.ecore.EObject;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;
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
import ViewModels.Interfaces.IObjectBrowserBaseViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import ViewModels.MappedElementListView.Interfaces.ICapellaMappedElementListViewViewModel;
import ViewModels.MappedElementListView.Interfaces.IMappedElementListViewViewModel;
import ViewModels.Rows.MappedDstRequirementRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import Views.Dialogs.CapellaDstToHubMappingConfigurationDialog;
import cdp4common.commondata.DefinedThing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import io.reactivex.Observable;

/**
 * The {@linkplain DstToHubMappingConfigurationDialogViewModel} is the main view model for the {@linkplain CapellaDstToHubMappingConfigurationDialog}
 */
public class DstToHubMappingConfigurationDialogViewModel extends MappingConfigurationDialogViewModel<EObject, NamedElement, ElementRowViewModel<? extends CapellaElement>> implements IDstToHubMappingConfigurationDialogViewModel
{    
    /**
     * The {@linkplain IMagicDrawObjectBrowserViewModel}
     */
    private final ICapellaObjectBrowserViewModel dstObjectBrowser;

    /**
     * Gets the DST {@linkplain IObjectBrowserBaseViewModel}
     * 
     * @return an {@linkplain IObjectBrowserViewModel}
     */
    @Override
    public IObjectBrowserBaseViewModel<ElementRowViewModel<? extends CapellaElement>> GetDstObjectBrowserViewModel()
    {
        return this.dstObjectBrowser;
    }
    
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
            ICapellaObjectBrowserViewModel capellaObjectBrowserViewModel, ICapellaMappedElementListViewViewModel mappedElementListViewViewModel)
    {
        super(dstController, hubController, elementDefinitionBrowserViewModel, requirementBrowserViewModel, 
                mappedElementListViewViewModel);
        
        this.dstObjectBrowser = capellaObjectBrowserViewModel;
        this.InitializeObservables();
    }
    
    /**
     * Initializes the {@linkplain Observable}s of this view model
     */
    protected void InitializeObservables()
    {
        super.InitializeObservables();
        
        this.dstObjectBrowser.GetSelectedElement()
            .subscribe(x -> this.UpdateMappedElements(x));
    }
    
    /**
     * Updates the mapped element collection 
     * 
     * @param rowViewModel the {@linkplain IElementRowViewModel}
     */
    @SuppressWarnings("unchecked")
    private void UpdateMappedElements(ElementRowViewModel<? extends CapellaElement> rowViewModel)
    {
        var optionalMappedElement = this.mappedElements.stream()
            .filter(x -> AreTheseEquals(x.GetDstElement().getId(), rowViewModel.GetElement().getId()))
            .findFirst();
        
        if(!optionalMappedElement.isPresent())
        {
            MappedElementRowViewModel<? extends DefinedThing, ? extends NamedElement> mappedElement;
            
            if(rowViewModel.GetElement() instanceof Component)
            {
                mappedElement = new MappedElementDefinitionRowViewModel((Component) rowViewModel.GetElement(), MappingDirection.FromDstToHub);
            }
            else
            {
                mappedElement = new MappedDstRequirementRowViewModel((Requirement) rowViewModel.GetElement(), MappingDirection.FromDstToHub);
            }

            this.mappedElements.add((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedElement);
            this.SetSelectedMappedElement((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedElement);
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
        this.dstObjectBrowser.BuildTree(this.originalSelection);
        ((ICapellaMappedElementListViewViewModel)this.mappedElementListViewViewModel).SetShouldDisplayTargetArchitectureColumn(false);
    }

    /**
     * Pre-map the selected elements
     * 
     * @param selectedElement the collection of {@linkplain #TElement}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void PreMap(Collection<EObject> selectedElements)
    {
        for (var element : selectedElements)
        {
            if(element instanceof Structure)
            {
                this.PreMap((Collection<EObject>) GetChildren((Structure)element));
            }
            else if (element instanceof NamedElement)
            {
                var mappedElement = this.GetMappedElementRowViewModel((NamedElement)element);

                if(mappedElement != null)
                {
                    this.mappedElements.add((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedElement);
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
    protected MappedElementRowViewModel<? extends DefinedThing, ? extends NamedElement> GetMappedElementRowViewModel(NamedElement capellaElement)
    {
        Ref<Boolean> refShouldCreateNewTargetElement = new Ref<>(Boolean.class, false);
        MappedElementRowViewModel<? extends DefinedThing, ? extends NamedElement> mappedElementRowViewModel = null;
        
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
            var refRequirement = new Ref<>(cdp4common.engineeringmodeldata.Requirement.class);
            
            if(this.TryGetRequirement((Requirement)capellaElement, refRequirement, refShouldCreateNewTargetElement))
            {
                mappedElementRowViewModel = 
                        new MappedDstRequirementRowViewModel(refRequirement.Get(), (Requirement)capellaElement, MappingDirection.FromDstToHub);
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
     * Gets or create an {@linkplain cdp4common.engineeringmodeldata.Requirement} that can be mapped from the specified {@linkplain Requirement},
     * In the case the provided {@linkplain Requirement} is already represented in the {@linkplain mappedElements} returns false
     * 
     * @param requirement the {@linkplain Requirement} element
     * @param refRequirement the {@linkplain Ref} of {@linkplain cdp4common.engineeringmodeldata.Requirement}
     * @param refShouldCreateNewTargetElement the {@linkplain Ref} of {@linkplain Boolean} indicating whether the target Hub element will be created
     * @return a value indicating whether the method execution was successful in getting a {@linkplain RequirementSpecification}
     */
    private boolean TryGetRequirement(Requirement requirement, Ref<cdp4common.engineeringmodeldata.Requirement> refRequirement, Ref<Boolean> refShouldCreateNewTargetElement)
    {
        Optional<cdp4common.engineeringmodeldata.Requirement> optionalRequirement = 
              this.hubController.GetOpenIteration().getRequirementsSpecification().stream()
              .flatMap(x -> x.getRequirement().stream())
              .filter(x -> AreTheseEquals(x.getName(), requirement.getName()) && !x.isDeprecated())
              .findFirst();

        if(optionalRequirement.isPresent())
        {
            if(this.mappedElements.stream().anyMatch(x -> AreTheseEquals(x.GetHubElement().getIid(), optionalRequirement.get().getIid())
                    && AreTheseEquals(x.GetDstElement().getId(), requirement.getId())))
            {
                return false;
            }
            
            var requirementSpecification = optionalRequirement.get().getContainerOfType(RequirementsSpecification.class).clone(true);            
            refRequirement.Set(requirementSpecification.getRequirement().stream()
                    .filter(x -> AreTheseEquals(x.getIid(), optionalRequirement.get().getIid()))
                    .findFirst()
                    .orElseThrow());
        }
        else
        {
            refShouldCreateNewTargetElement.Set(true);
            
            var possibleParent = new Ref<>(RequirementsPkg.class);
            
            if(StereotypeUtils.TryGetPossibleRequirementsSpecificationElement(requirement, possibleParent))
            {
                var requirementSpecification = this.hubController.GetOpenIteration().getRequirementsSpecification().stream()
                    .filter(x -> AreTheseEquals(possibleParent, x.getName()))
                    .map(x -> x.clone(true))
                    .findFirst()
                    .orElseGet(() ->
                {
                    var newRequirementsSpecification = new RequirementsSpecification();
                    newRequirementsSpecification.setName(possibleParent.HasValue() ? possibleParent.Get().getName() : "new RequirementsSpecification");
                    newRequirementsSpecification.setShortName(GetShortName(possibleParent.HasValue() ? possibleParent.Get().getName() : newRequirementsSpecification.getName()));
                    newRequirementsSpecification.setIid(UUID.randomUUID());
                    newRequirementsSpecification.setOwner(this.hubController.GetCurrentDomainOfExpertise());
                    return newRequirementsSpecification;
                });
                
                var newRequirement = new cdp4common.engineeringmodeldata.Requirement();
                newRequirement.setName(requirement.getName());
                requirementSpecification.getRequirement().add(newRequirement);
                
                refRequirement.Set(newRequirement);
            }
        }

        return refRequirement.HasValue();
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
