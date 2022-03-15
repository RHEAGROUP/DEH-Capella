/*
 * DstMappingConfigurationDialogViewModelTestFixture.java
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

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
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
import ViewModels.ObjectBrowser.ElementDefinitionTree.Rows.ElementDefinitionRowViewModel;
import ViewModels.ObjectBrowser.RequirementTree.Rows.RequirementSpecificationRowViewModel;
import ViewModels.ObjectBrowser.Rows.ThingRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedDstRequirementRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.DomainOfExpertise;

class DstMappingConfigurationDialogViewModelTestFixture
{
    private IDstController dstController;
    private IHubController hubController;
    private IElementDefinitionBrowserViewModel elementDefinitionBrowser;
    private IRequirementBrowserViewModel requirementBrowserViewModel;
    private ICapellaObjectBrowserViewModel capellaObjectBrowser;
    private DstMappingConfigurationDialogViewModel viewModel;
    private Iteration iteration;
    private ObservableCollection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> dstMapResult;
    private ObservableValue<ThingRowViewModel<? extends Thing>> selectedElementDefinitionObservable;
    private ObservableValue<ThingRowViewModel<? extends Thing>> selectedRequirementObservable;
    private ObservableValue<ElementRowViewModel<? extends CapellaElement>> selectedCapellaElementObservable;

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
        
        this.dstMapResult = new ObservableCollection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>>();
        when(this.dstController.GetDstMapResult()).thenReturn(this.dstMapResult);
        
        this.selectedCapellaElementObservable = new ObservableValue<ElementRowViewModel<? extends CapellaElement>>();
        when(this.capellaObjectBrowser.GetSelectedElement()).thenReturn(this.selectedCapellaElementObservable.Observable());
        this.selectedElementDefinitionObservable = new ObservableValue<ThingRowViewModel<? extends Thing>>();
        when(this.elementDefinitionBrowser.GetSelectedElement()).thenReturn(this.selectedElementDefinitionObservable.Observable());
        this.selectedRequirementObservable = new ObservableValue<ThingRowViewModel<? extends Thing>>();
        when(this.requirementBrowserViewModel.GetSelectedElement()).thenReturn(this.selectedRequirementObservable.Observable());
        
        this.iteration = new Iteration();
        when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);

        when(this.hubController.TryGetThingById(any(UUID.class), any(Ref.class))).thenReturn(true);

        this.viewModel = new DstMappingConfigurationDialogViewModel(this.dstController, this.hubController, 
                this.elementDefinitionBrowser, this.requirementBrowserViewModel, this.capellaObjectBrowser);
    }

    @Test
    public void VerifyProperties()
    {
        assertNotNull(this.viewModel.GetElementDefinitionBrowserViewModel());
        assertNotNull(this.viewModel.GetRequirementBrowserViewModel());
        assertNotNull(this.viewModel.GetCapellaObjectBrowserViewModel());
        assertNotNull(this.viewModel.GetMappedElementCollection());
        assertNotNull(this.viewModel.GetSelectedMappedElement());
        assertDoesNotThrow(() -> this.viewModel.SetSelectedMappedElement(mock(MappedElementRowViewModel.class)));
        assertNotNull(this.viewModel.GetShouldMapToNewHubElementCheckBoxBeEnabled());
    }
    
    @Test
    public void VerifySetMappedElement()
    {
        var mappedRequirement = new MappedDstRequirementRowViewModel(mock(Requirement.class), MappingDirection.FromDstToHub);
        mappedRequirement.SetShouldCreateNewTargetElement(false);
        this.dstMapResult.add(mappedRequirement);
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
        var mappedElementDefinition = new MappedElementDefinitionRowViewModel(mock(PhysicalComponent.class), MappingDirection.FromDstToHub);
        mappedElementDefinition.SetShouldCreateNewTargetElement(false);

        var mappedRequirement = new MappedDstRequirementRowViewModel(mock(Requirement.class), MappingDirection.FromDstToHub);
        mappedElementDefinition.SetShouldCreateNewTargetElement(false);
        
        assertDoesNotThrow(() -> this.viewModel.WhenMapToNewHubElementCheckBoxChanged(true));
        
        this.viewModel.SetSelectedMappedElement(mappedElementDefinition);
        assertDoesNotThrow(() -> this.viewModel.WhenMapToNewHubElementCheckBoxChanged(true));
        assertNull(mappedElementDefinition.GetHubElement());
        
        assertDoesNotThrow(() -> this.viewModel.WhenMapToNewHubElementCheckBoxChanged(false));
        mappedElementDefinition.SetShouldCreateNewTargetElement(true);
        assertDoesNotThrow(() -> this.viewModel.WhenMapToNewHubElementCheckBoxChanged(false));
        assertFalse(mappedElementDefinition.GetShouldCreateNewTargetElementValue());

        this.viewModel.SetSelectedMappedElement(mappedRequirement);
        mappedRequirement.SetShouldCreateNewTargetElement(false);
        assertDoesNotThrow(() -> this.viewModel.WhenMapToNewHubElementCheckBoxChanged(true));
        assertFalse(mappedElementDefinition.GetShouldCreateNewTargetElementValue());
        
        mappedElementDefinition.SetHubElement(new ElementDefinition());
        this.viewModel.SetSelectedMappedElement(mappedElementDefinition);
        mappedElementDefinition.SetShouldCreateNewTargetElement(true);
        assertDoesNotThrow(() -> this.viewModel.WhenMapToNewHubElementCheckBoxChanged(false));
        mappedElementDefinition.SetShouldCreateNewTargetElement(false);
        assertDoesNotThrow(() -> this.viewModel.WhenMapToNewHubElementCheckBoxChanged(false));
        
        var requirementsSpecification = new RequirementsSpecification();
        
        mappedRequirement.SetHubElement(requirementsSpecification);
        mappedRequirement.SetShouldCreateNewTargetElement(false);
        this.dstMapResult.add(mappedRequirement);
        this.viewModel.SetSelectedMappedElement(mappedRequirement);
        assertDoesNotThrow(() -> this.viewModel.WhenMapToNewHubElementCheckBoxChanged(true));
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
        
        
        assertDoesNotThrow(() -> this.selectedElementDefinitionObservable.Value(new ElementDefinitionRowViewModel(elementDefinition, null)));
        assertDoesNotThrow(() -> this.selectedRequirementObservable.Value(new RequirementSpecificationRowViewModel(requirementSpecification, null)));
        var mappedElementDefinition = new MappedElementDefinitionRowViewModel(mock(PhysicalComponent.class), MappingDirection.FromDstToHub);
        mappedElementDefinition.SetShouldCreateNewTargetElement(false);
        mappedElementDefinition.SetRowStatus(MappedElementRowStatus.None);
        this.viewModel.SetSelectedMappedElement(mappedElementDefinition);
        assertDoesNotThrow(() -> this.selectedElementDefinitionObservable.Value(new ElementDefinitionRowViewModel(elementDefinition, null)));
        var mappedRequirement = new MappedDstRequirementRowViewModel(mock(Requirement.class), MappingDirection.FromDstToHub);
        mappedRequirement.SetShouldCreateNewTargetElement(false);
        mappedRequirement.SetRowStatus(MappedElementRowStatus.None);
        this.viewModel.SetSelectedMappedElement(mappedRequirement);
        assertDoesNotThrow(() -> this.selectedRequirementObservable.Value(new RequirementSpecificationRowViewModel(requirementSpecification, null)));   
    }
    
    @Test
    public void VerifyUpdateMappedElement()
    {
        this.viewModel.SetMappedElement(this.SetupCapellaElements());
        var firstRowIsSelected = new ArrayList<Boolean>();
        this.viewModel.GetMappedElementCollection().get(0).GetIsSelected().subscribe(x -> firstRowIsSelected.add(x));
        
        this.viewModel.SetSelectedMappedElement(this.viewModel.GetMappedElementCollection().get(0));
        assertTrue(firstRowIsSelected.get(firstRowIsSelected.size() - 1));
        assertEquals(1, firstRowIsSelected.size());
        

        var mappedElement = new MappedElementDefinitionRowViewModel(mock(PhysicalComponent.class), MappingDirection.FromDstToHub);
        mappedElement.SetRowStatus(MappedElementRowStatus.NewElement);
        assertDoesNotThrow(() -> this.viewModel.SetSelectedMappedElement(mappedElement));
        
        var mappedRequirement = new MappedDstRequirementRowViewModel(mock(Requirement.class), MappingDirection.FromDstToHub);
        mappedRequirement.SetRowStatus(MappedElementRowStatus.ExisitingElement);
        assertDoesNotThrow(() -> this.viewModel.SetSelectedMappedElement(mappedRequirement));
        
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
