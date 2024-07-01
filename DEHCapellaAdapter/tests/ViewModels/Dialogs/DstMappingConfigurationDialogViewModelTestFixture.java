/*
 * DstMappingConfigurationDialogViewModelTestFixture.java
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

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.cs.ComponentPkg;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.data.requirement.Requirement;

import DstController.IDstController;
import Enumerations.MappedElementRowStatus;
import Enumerations.MappingDirection;
import HubController.IHubController;
import MappingRules.RequirementToRequirementsSpecificationMappingRule;
import Reactive.ObservableCollection;
import Reactive.ObservableValue;
import Utils.Ref;
import ViewModels.CapellaObjectBrowser.Interfaces.ICapellaObjectBrowserViewModel;
import ViewModels.CapellaObjectBrowser.Rows.ComponentRowViewModel;
import ViewModels.CapellaObjectBrowser.Rows.ElementRowViewModel;
import ViewModels.CapellaObjectBrowser.Rows.RequirementRowViewModel;
import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import ViewModels.MappedElementListView.Interfaces.ICapellaMappedElementListViewViewModel;
import ViewModels.MappedElementListView.Interfaces.IMappedElementListViewViewModel;
import ViewModels.ObjectBrowser.ElementDefinitionTree.Rows.ElementDefinitionRowViewModel;
import ViewModels.ObjectBrowser.RequirementTree.Rows.RequirementSpecificationRowViewModel;
import ViewModels.ObjectBrowser.Rows.ThingRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedDstRequirementRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.DomainOfExpertise;
import io.reactivex.Observable;

class DstMappingConfigurationDialogViewModelTestFixture
{
    private IDstController dstController;
    private IHubController hubController;
    private IElementDefinitionBrowserViewModel elementDefinitionBrowser;
    private IRequirementBrowserViewModel requirementBrowserViewModel;
    private ICapellaObjectBrowserViewModel capellaObjectBrowser;
    private DstToHubMappingConfigurationDialogViewModel viewModel;
    private Iteration iteration;
    private ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>> dstMapResult;
    private ObservableValue<ThingRowViewModel<Thing>> selectedElementDefinitionObservable;
    private ObservableValue<ThingRowViewModel<Thing>> selectedRequirementObservable;
    private ObservableValue<ElementRowViewModel<? extends CapellaElement>> selectedCapellaElementObservable;
    private ICapellaMappedElementListViewViewModel mappedElementListViewViewModel;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        this.dstController = mock(IDstController.class);
        this.hubController = mock(IHubController.class);
        this.elementDefinitionBrowser = mock(IElementDefinitionBrowserViewModel.class);
        this.requirementBrowserViewModel = mock(IRequirementBrowserViewModel.class);
        this.capellaObjectBrowser = mock(ICapellaObjectBrowserViewModel.class);
        this.mappedElementListViewViewModel = mock(ICapellaMappedElementListViewViewModel.class);
        when(this.mappedElementListViewViewModel.GetSelectedElement()).thenReturn(Observable.empty());
        
        this.dstMapResult = new ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>>();
        when(this.dstController.GetDstMapResult()).thenReturn(this.dstMapResult);
        
        this.selectedCapellaElementObservable = new ObservableValue<ElementRowViewModel<? extends CapellaElement>>();
        when(this.capellaObjectBrowser.GetSelectedElement()).thenReturn(this.selectedCapellaElementObservable.Observable());
        this.selectedElementDefinitionObservable = new ObservableValue<ThingRowViewModel<Thing>>();
        when(this.elementDefinitionBrowser.GetSelectedElement()).thenReturn(this.selectedElementDefinitionObservable.Observable());
        this.selectedRequirementObservable = new ObservableValue<ThingRowViewModel<Thing>>();
        when(this.requirementBrowserViewModel.GetSelectedElement()).thenReturn(this.selectedRequirementObservable.Observable());
        
        this.iteration = new Iteration();
        when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);

        when(this.hubController.TryGetThingById(any(UUID.class), any(Ref.class))).thenReturn(true);

        this.viewModel = new DstToHubMappingConfigurationDialogViewModel(this.dstController, this.hubController, 
                this.elementDefinitionBrowser, this.requirementBrowserViewModel, this.capellaObjectBrowser, this.mappedElementListViewViewModel);
    }

    @Test
    public void VerifyProperties()
    {
        assertNotNull(this.viewModel.GetElementDefinitionBrowserViewModel());
        assertNotNull(this.viewModel.GetRequirementBrowserViewModel());
        assertNotNull(this.viewModel.GetDstObjectBrowserViewModel());
        assertNotNull(this.viewModel.GetMappedElementCollection());
        assertNotNull(this.viewModel.GetSelectedMappedElement());
        assertDoesNotThrow(() -> this.viewModel.SetSelectedMappedElement(mock(MappedElementRowViewModel.class)));
        assertNotNull(this.viewModel.GetShouldMapToNewElementCheckBoxBeEnabled());
    }
    
    @Test
    public void VerifySetMappedElement()
    {
        MappedElementRowViewModel<? extends DefinedThing,? extends NamedElement> mappedRequirement = new MappedDstRequirementRowViewModel(mock(Requirement.class), MappingDirection.FromDstToHub);
        mappedRequirement.SetShouldCreateNewTargetElement(false);
        this.dstMapResult.add((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedRequirement);
        var elements = SetupCapellaElements();
        assertDoesNotThrow(() -> this.viewModel.SetMappedElement(elements));
    }

    @Test
    public void VerifyResetPremappedThings()
    {
        var elements = new ArrayList<EObject>();
        assertDoesNotThrow(() -> this.viewModel.SetMappedElement(elements));
        assertDoesNotThrow(() -> this.viewModel.ResetPreMappedThings());
    }
    
    @Test
    public void VerifyWhenMapToNewHubElementCheckBoxChanged()
    {
        MappedElementRowViewModel<? extends DefinedThing, ? extends NamedElement> mappedElementDefinition = 
                new MappedElementDefinitionRowViewModel(mock(PhysicalComponent.class), MappingDirection.FromDstToHub);
        
        mappedElementDefinition.SetShouldCreateNewTargetElement(false);

        MappedElementRowViewModel<? extends DefinedThing, ? extends NamedElement> mappedRequirement = 
                new MappedDstRequirementRowViewModel(mock(Requirement.class), MappingDirection.FromDstToHub);
        
        mappedElementDefinition.SetShouldCreateNewTargetElement(false);
        this.viewModel.SetSelectedMappedElement((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedElementDefinition);
        
        assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));
        
        this.viewModel.SetSelectedMappedElement((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedElementDefinition);
        assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));
        assertNull(mappedElementDefinition.GetHubElement());
        
        assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(false));
        mappedElementDefinition.SetShouldCreateNewTargetElement(true);
        assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(false));
        assertFalse(mappedElementDefinition.GetShouldCreateNewTargetElementValue());

        this.viewModel.SetSelectedMappedElement((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedRequirement);
        mappedRequirement.SetShouldCreateNewTargetElement(false);
        assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));
        assertFalse(mappedElementDefinition.GetShouldCreateNewTargetElementValue());
        
        ((MappedElementDefinitionRowViewModel)mappedElementDefinition).SetHubElement(new ElementDefinition());
        this.viewModel.SetSelectedMappedElement((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedElementDefinition);
        mappedElementDefinition.SetShouldCreateNewTargetElement(true);
        assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(false));
        mappedElementDefinition.SetShouldCreateNewTargetElement(false);
        assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(false));
        
        var requirement = new cdp4common.engineeringmodeldata.Requirement();
        
        ((MappedDstRequirementRowViewModel)mappedRequirement).SetHubElement(requirement);
        mappedRequirement.SetShouldCreateNewTargetElement(false);
        this.dstMapResult.add((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedRequirement);
        this.viewModel.SetSelectedMappedElement((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedRequirement);
        assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));
    }
    
    @Test
    public void VerifySetHubElement()
    {
        var owner = new DomainOfExpertise();
        owner.setName("Owner");
        owner.setShortName("owner");
        var elementDefinition = new ElementDefinition();
        elementDefinition.setOwner(owner);
        elementDefinition.setName("elementDefinition");
        elementDefinition.setShortName("elementDefinition");
        var requirementSpecification = new RequirementsSpecification();
        requirementSpecification.setOwner(owner);
        requirementSpecification.setName("requirementSpecification");
        requirementSpecification.setShortName("requirementSpecification");
        
        ThingRowViewModel<? extends Thing> elementDefinitionRowViewModel = new ElementDefinitionRowViewModel(elementDefinition, null);
        
        assertDoesNotThrow(() -> this.selectedElementDefinitionObservable.Value((ThingRowViewModel<Thing>) elementDefinitionRowViewModel));

        ThingRowViewModel<? extends Thing> requirementSpecificationRowViewModel = new RequirementSpecificationRowViewModel(requirementSpecification, null);
        
        assertDoesNotThrow(() -> this.selectedRequirementObservable.Value((ThingRowViewModel<Thing>) requirementSpecificationRowViewModel));
        
        MappedElementRowViewModel<? extends DefinedThing, ? extends NamedElement> mappedElementDefinition = new MappedElementDefinitionRowViewModel(mock(PhysicalComponent.class), MappingDirection.FromDstToHub);
        mappedElementDefinition.SetShouldCreateNewTargetElement(false);
        mappedElementDefinition.SetRowStatus(MappedElementRowStatus.None);
        this.viewModel.SetSelectedMappedElement((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedElementDefinition);
        assertDoesNotThrow(() -> this.selectedElementDefinitionObservable.Value((ThingRowViewModel<Thing>) elementDefinitionRowViewModel));
        MappedElementRowViewModel<? extends DefinedThing, ? extends NamedElement> mappedRequirement = new MappedDstRequirementRowViewModel(mock(Requirement.class), MappingDirection.FromDstToHub);
        mappedRequirement.SetShouldCreateNewTargetElement(false);
        mappedRequirement.SetRowStatus(MappedElementRowStatus.None);
        this.viewModel.SetSelectedMappedElement((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedRequirement);
        assertDoesNotThrow(() -> this.selectedRequirementObservable.Value((ThingRowViewModel<Thing>) requirementSpecificationRowViewModel));   
    }
    
    @Test
    public void VerifyUpdateMappedElement()
    {
        this.viewModel.SetMappedElement(this.SetupCapellaElements());
        var firstRowIsSelected = new ArrayList<Boolean>();
        this.viewModel.GetMappedElementCollection().get(0).GetIsSelectedObservable().subscribe(x -> firstRowIsSelected.add(x));
        
        this.viewModel.SetSelectedMappedElement(this.viewModel.GetMappedElementCollection().get(0));
        assertTrue(firstRowIsSelected.get(firstRowIsSelected.size() - 1));
        assertEquals(1, firstRowIsSelected.size());
        

        MappedElementRowViewModel<? extends DefinedThing, ? extends NamedElement> mappedElement = new MappedElementDefinitionRowViewModel(mock(PhysicalComponent.class), MappingDirection.FromDstToHub);
        mappedElement.SetRowStatus(MappedElementRowStatus.NewElement);
        assertDoesNotThrow(() -> this.viewModel.SetSelectedMappedElement((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedElement));
        
        MappedElementRowViewModel<? extends DefinedThing, ? extends NamedElement> mappedRequirement = new MappedDstRequirementRowViewModel(mock(Requirement.class), MappingDirection.FromDstToHub);
        mappedRequirement.SetRowStatus(MappedElementRowStatus.ExisitingElement);
        assertDoesNotThrow(() -> this.viewModel.SetSelectedMappedElement((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedRequirement));
        
        var physicalComponent = mock(PhysicalComponent.class);
        when(physicalComponent.getId()).thenReturn(UUID.randomUUID().toString());
        when(physicalComponent.getName()).thenReturn("physicalComponent");
        when(physicalComponent.eContents()).thenReturn(new BasicEList<EObject>());
        
        var requirement0 = mock(Requirement.class);
        var requirement1 = mock(Requirement.class);
        when(requirement1.getName()).thenReturn("requirement");
        
        assertDoesNotThrow(() -> this.selectedCapellaElementObservable.Value(new ComponentRowViewModel(null, physicalComponent)));
        assertDoesNotThrow(() -> this.selectedCapellaElementObservable.Value(new RequirementRowViewModel(null, requirement0)));   
        assertDoesNotThrow(() -> this.selectedCapellaElementObservable.Value(new RequirementRowViewModel(null, requirement1)));   
    }
    
    private ArrayList<EObject> SetupCapellaElements()
    {
        var elements = new ArrayList<EObject>();
        var physicalComponent = mock(PhysicalComponent.class);
        when(physicalComponent.getName()).thenReturn("physicalComponent");
        elements.add(physicalComponent);
        var requirement = mock(Requirement.class);
        when(requirement.getId()).thenReturn(UUID.randomUUID().toString());
        elements.add(requirement);
        var componentPkg = mock(ComponentPkg.class);
        when(componentPkg.getName()).thenReturn("componentPkg");
        elements.add(componentPkg);
        return elements;
    }
}
