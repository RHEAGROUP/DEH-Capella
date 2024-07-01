/*
 * HubToDstMappingConfigurationDialogViewModel.java
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
package ViewModels.Dialogs;

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.data.requirement.Requirement;
import org.polarsys.capella.core.data.requirement.SystemUserRequirement;

import DstController.IDstController;
import Enumerations.CapellaArchitecture;
import Enumerations.MappedElementRowStatus;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.CapellaTransaction.ICapellaTransactionService;
import Utils.Ref;
import ViewModels.CapellaObjectBrowser.Interfaces.ICapellaObjectBrowserViewModel;
import ViewModels.CapellaObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.CapellaObjectBrowser.Rows.ElementRowViewModel;
import ViewModels.Dialogs.Interfaces.IHubToDstMappingConfigurationDialogViewModel;
import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IHaveTargetArchitecture;
import ViewModels.Interfaces.IObjectBrowserBaseViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import ViewModels.MappedElementListView.Interfaces.ICapellaMappedElementListViewViewModel;
import ViewModels.Rows.MappedDstRequirementRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedHubRequirementRowViewModel;
import Views.Dialogs.CapellaHubToDstMappingConfigurationDialog;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * The {@linkplain HubToDstMappingConfigurationDialogViewModel} is the main view model for the {@linkplain CapellaHubToDstMappingConfigurationDialog}
 */
public class HubToDstMappingConfigurationDialogViewModel extends MappingConfigurationDialogViewModel<Thing, NamedElement, ElementRowViewModel<? extends CapellaElement>> implements IHubToDstMappingConfigurationDialogViewModel
{
    /**
     * The {@linkplain IDstController}
     */
    private final IDstController dstController;

    /**
     * The {@linkplain ICapellaTransactionService}
     */
    private final ICapellaTransactionService transactionService;
    
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
     * The collection of {@linkplain Disposable}
     */
    private List<Disposable> disposables = new ArrayList<>();

    /**
     * Initializes a new {@linkplain HubToDstMappingConfigurationDialogViewModel}
     * 
     * @param dstController the {@linkplain IDstController}
     * @param hubController the {@linkplain IHubController}
     * @param elementDefinitionBrowserViewModel the {@linkplain IElementDefinitionBrowserViewModel}
     * @param requirementBrowserViewModel the {@linkplain IRequirementBrowserViewModel}
     * @param capellaObjectBrowserViewModel the {@linkplain ICapellaObjectBrowserViewModel}
     * @param transactionService the {@linkplain ICapellaTransactionService}
     * @param mappedElementListViewViewModel the {@linkplain ICapellaMappedElementListViewViewModel}
     */
    public HubToDstMappingConfigurationDialogViewModel(IDstController dstController, IHubController hubController, 
            IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel, IRequirementBrowserViewModel requirementBrowserViewModel,
            ICapellaObjectBrowserViewModel capellaObjectBrowserViewModel, ICapellaTransactionService transactionService,
            ICapellaMappedElementListViewViewModel mappedElementListViewViewModel)
    {
        super(dstController, hubController, elementDefinitionBrowserViewModel, requirementBrowserViewModel, 
                mappedElementListViewViewModel);
        
        this.dstController = dstController;
        this.transactionService = transactionService;
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
    private void UpdateMappedElements(ElementRowViewModel<? extends CapellaElement> rowViewModel)
    {
        if(this.selectedMappedElement.Value() == null)
        {
            return;
        }
        
        if(AreTheseEquals(this.selectedMappedElement.Value().GetDstElement().getId(), rowViewModel.GetElement().getId()))
        {
            return;
        }
        
        if(rowViewModel.GetElement() instanceof Component)
        {
            this.selectedMappedElement.Value().SetDstElement((Component)rowViewModel.GetElement());
        }
        else if(rowViewModel.GetElement() instanceof Requirement)
        {
            this.selectedMappedElement.Value().SetDstElement((Requirement)rowViewModel.GetElement());
        }
        
        this.UpdateTargetArchitecture(rowViewModel.GetElement());
    }

    /**
     * Updates the target architecture {@linkplain CapellaArchitecture} for the currently selected {@linkplain MappedElementRowViewModel}
     * 
     * @param capellaElement
     */
    private void UpdateTargetArchitecture(CapellaElement capellaElement)
    {
        if(!(this.selectedMappedElement.Value() instanceof IHaveTargetArchitecture))
        {
            return;
        }
        
        ((IHaveTargetArchitecture)this.selectedMappedElement.Value()).SetTargetArchitecture(CapellaArchitecture.From(capellaElement));
    }

    /**
     * Updates this view model properties
     */
    @Override
    protected void UpdateProperties()
    {
        this.UpdateProperties(this.dstController.GetHubMapResult());
        this.dstObjectBrowser.BuildTree(null);
        ((ICapellaMappedElementListViewViewModel)this.mappedElementListViewViewModel).SetShouldDisplayTargetArchitectureColumn(true);
    }

    /**
     * Pre-map the selected elements
     * 
     * @param selectedElement the collection of {@linkplain #TElement}
     */
    @Override
    protected void PreMap(Collection<Thing> selectedElements)
    {
        this.disposables.forEach(x -> x.dispose());
        this.disposables.clear();
        
        for (var thing : selectedElements)
        {            
            var mappedRowViewModel = this.GetMappedElementRowViewModel(thing);
            
            if(mappedRowViewModel != null)
            {
                this.mappedElements.add(mappedRowViewModel);
            }
        }
        
        for (var mappedElementRowViewModel : mappedElements.stream()
                .filter(x -> x instanceof IHaveTargetArchitecture && x.GetHubElement() instanceof ElementDefinition)
                .collect(Collectors.toList()))
        {
            var subscription = ((IHaveTargetArchitecture)mappedElementRowViewModel).GetTargetArchitectureObservable()
                    .subscribe(x -> this.UpdateComponent((MappedElementRowViewModel<? extends DefinedThing, NamedElement>)mappedElementRowViewModel));
            
            this.disposables.add(subscription);
        }
    }
    
    /**
     * Get a {@linkplain MappedElementRowViewModel} that represents a pre-mapped {@linkplain Class}
     * 
     * @param thing the {@linkplain Class} element
     * @return a {@linkplain MappedElementRowViewModel}
     */
    @SuppressWarnings("unchecked")
    protected MappedElementRowViewModel<DefinedThing, NamedElement> GetMappedElementRowViewModel(Thing thing)
    {
        Ref<Boolean> refShouldCreateNewTargetElement = new Ref<>(Boolean.class, false);
        MappedElementRowViewModel<? extends DefinedThing, ? extends NamedElement> mappedElementRowViewModel = null;
        
        if(thing instanceof ElementDefinition)
        {
            var refComponent = new Ref<>(Component.class);
            
            if(this.TryGetComponent((ElementDefinition)thing, PhysicalComponent.class, refComponent, refShouldCreateNewTargetElement))
            {
                var mappedElementDefinition = new MappedElementDefinitionRowViewModel((ElementDefinition)thing, refComponent.Get(), MappingDirection.FromHubToDst);
                
                mappedElementDefinition.SetTargetArchitecture(mappedElementDefinition.GetDstElement() instanceof PhysicalComponent 
                        ? CapellaArchitecture.PhysicalArchitecture
                        : CapellaArchitecture.LogicalArchitecture);
                
                mappedElementRowViewModel = mappedElementDefinition;
            }
        }
        else if(thing instanceof cdp4common.engineeringmodeldata.Requirement)
        {
            var refRequirement = new Ref<>(Requirement.class);
            var refTargetArchitecture = new Ref<>(CapellaArchitecture.class);
            
            if(this.TryGetRequirement((cdp4common.engineeringmodeldata.Requirement)thing, refRequirement, refShouldCreateNewTargetElement, refTargetArchitecture))
            {
                mappedElementRowViewModel = 
                        new MappedHubRequirementRowViewModel((cdp4common.engineeringmodeldata.Requirement)thing, 
                                refRequirement.Get(), MappingDirection.FromHubToDst);
            }
        }
        
        if(mappedElementRowViewModel != null)
        {
            mappedElementRowViewModel.SetShouldCreateNewTargetElement(refShouldCreateNewTargetElement.Get());
            
            mappedElementRowViewModel.SetRowStatus(Boolean.TRUE.equals(refShouldCreateNewTargetElement.Get()) 
                    ? MappedElementRowStatus.NewElement 
                    : MappedElementRowStatus.ExisitingElement);
            
            return (MappedElementRowViewModel<DefinedThing, NamedElement>) mappedElementRowViewModel;
        }
        
        return null;
    }

    /**
     * Gets or create an {@linkplain Component} that can be mapped to the provided {@linkplain ElementDefinition},
     * In the case the provided {@linkplain ElementDefinition} is already represented in the {@linkplain mappedElements} returns false
     * 
     * @param <TComponent> the type of {@linkplain Component} to get
     * @param elementDefinition the {@linkplain ElementDefinition} element
     * @param componentType the {@linkplain Class} type of the {@linkplain Component} to get
     * @param refComponent the {@linkplain Ref} of {@linkplain Component}
     * @param refShouldCreateNewTargetElement the {@linkplain Ref} of {@linkplain Boolean} indicating whether the target DST element will be created
     * @return a value indicating whether the method execution was successful in getting a {@linkplain Component}
     */
    private <TComponent extends Component> boolean TryGetComponent(ElementDefinition elementDefinition, Class<TComponent> componentType,
            Ref<Component> refComponent, Ref<Boolean> refShouldCreateNewTargetElement)
    {
        if(this.mappedElements.stream()
                .noneMatch(x-> AreTheseEquals(x.GetHubElement().getIid(), elementDefinition.getIid()) && 
                        x.GetDstElement() != null && componentType.isInstance(x.GetDstElement())))
        {
            if(this.dstController.TryGetElementByName(elementDefinition, refComponent) && componentType.isInstance(refComponent.Get()))
            {
                refComponent.Set(this.transactionService.Clone(refComponent.Get()));
            }
            else
            {
                var component = this.transactionService.Create(componentType, elementDefinition.getName());
                refComponent.Set((TComponent) component);
                refShouldCreateNewTargetElement.Set(true);
            }
        }
        
        return refComponent.HasValue();
    }

    /**
     * Updates the target dst element when the target architecture changes
     * 
     * @param mappedElementRowViewModel the {@linkplain MappedElementDefinitionRowViewModel}
     */
    private void UpdateComponent(MappedElementRowViewModel<? extends DefinedThing, NamedElement> mappedElementRowViewModel)
    {
        var refComponent = new Ref<>(Component.class);
        
        Ref<Boolean> refShouldCreateNewTargetElement = new Ref<>(Boolean.class, 
                mappedElementRowViewModel.GetShouldCreateNewTargetElementValue());
        
        var componentType = ((IHaveTargetArchitecture) mappedElementRowViewModel).GetTargetArchitecture() == CapellaArchitecture.PhysicalArchitecture
                ? PhysicalComponent.class
                : LogicalComponent.class;
        
        if(TryGetComponent((ElementDefinition)mappedElementRowViewModel.GetHubElement(), componentType, refComponent, refShouldCreateNewTargetElement))
        {
            mappedElementRowViewModel.SetDstElement(refComponent.Get());
        }
    }
    
    /**
     * Gets or create an {@linkplain Requirement} that can be mapped from the specified {@linkplain cdp4common.engineeringmodeldata.Requirement},
     * In the case the provided {@linkplain cdp4common.engineeringmodeldata.Requirement} is already represented in the {@linkplain mappedElements} returns false
     * 
     * @param requirement the {@linkplain cdp4common.engineeringmodeldata.Requirement} element
     * @param refRequirement the {@linkplain Ref} of {@linkplain Requirement}
     * @param refShouldCreateNewTargetElement the {@linkplain Ref} of {@linkplain Boolean} indicating whether the target DST element will be created
     * @param refArchitecture the {@linkplain Ref} of {@linkplain CapellaArchitecture}
     * @return a value indicating whether the method execution was successful in getting a {@linkplain Requirement}
     */
    private boolean TryGetRequirement(cdp4common.engineeringmodeldata.Requirement requirement, Ref<Requirement> refRequirement, 
            Ref<Boolean> refShouldCreateNewTargetElement, Ref<CapellaArchitecture> refArchitecture)
    {
        if(this.mappedElements.stream().noneMatch(x-> AreTheseEquals(x.GetHubElement().getIid(), requirement.getIid())))
        {
            if(this.dstController.TryGetElementByName(requirement, refRequirement))
            {
                refArchitecture.Set(Utils.Stereotypes.StereotypeUtils.GetArchitecture(refRequirement.Get()));
                refRequirement.Set(this.transactionService.Clone(refRequirement.Get()));
            }
            else
            {
                var component = this.transactionService.Create(SystemUserRequirement.class, requirement.getName());
                refRequirement.Set(component);
                refArchitecture.Set(CapellaArchitecture.SystemAnalysis);
                refShouldCreateNewTargetElement.Set(true);
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
