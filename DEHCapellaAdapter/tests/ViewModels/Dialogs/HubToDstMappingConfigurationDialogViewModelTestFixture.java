/*
 * HubToDstMappingConfigurationDialogViewModelTestFixture.java
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.Feature;
import org.polarsys.capella.core.data.capellacore.NamedElement;

import DstController.IDstController;
import HubController.IHubController;
import Reactive.ObservableCollection;
import Reactive.ObservableValue;
import Services.CapellaTransaction.ClonedReferenceElement;
import Services.CapellaTransaction.ICapellaTransactionService;
import Utils.Ref;
import ViewModels.CapellaObjectBrowser.Interfaces.ICapellaObjectBrowserViewModel;
import ViewModels.CapellaObjectBrowser.Rows.ElementRowViewModel;
import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import ViewModels.MappedElementListView.Interfaces.ICapellaMappedElementListViewViewModel;
import ViewModels.ObjectBrowser.Rows.ThingRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.Iteration;
import io.reactivex.Observable;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.ComponentPkg;
import org.polarsys.capella.core.data.information.Property;
import org.polarsys.capella.core.data.information.datatype.Enumeration;
import org.polarsys.capella.core.data.la.LogicalComponent;
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
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.DomainOfExpertise;
import io.reactivex.Observable;

class HubToDstMappingConfigurationDialogViewModelTestFixture
{
    private IDstController dstController;
    private IHubController hubController;
    private IElementDefinitionBrowserViewModel elementDefinitionBrowser;
    private IRequirementBrowserViewModel requirementBrowserViewModel;
    private ICapellaObjectBrowserViewModel capellaObjectBrowser;
    private HubToDstMappingConfigurationDialogViewModel viewModel;
    private Iteration iteration;
    private ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>> hubMapResult;
    private ObservableValue<ThingRowViewModel<Thing>> selectedElementDefinitionObservable;
    private ObservableValue<ThingRowViewModel<Thing>> selectedRequirementObservable;
    private ObservableValue<ElementRowViewModel<? extends CapellaElement>> selectedCapellaElementObservable;
    private ICapellaMappedElementListViewViewModel mappedElementListViewViewModel;
    private ICapellaTransactionService transactionService;
    private Collection<Thing> elements;
    
    @BeforeEach
    public void Setup()
    {
        this.dstController = mock(IDstController.class);
        this.hubController = mock(IHubController.class);
        this.elementDefinitionBrowser = mock(IElementDefinitionBrowserViewModel.class);
        this.requirementBrowserViewModel = mock(IRequirementBrowserViewModel.class);
        this.capellaObjectBrowser = mock(ICapellaObjectBrowserViewModel.class);
        this.mappedElementListViewViewModel = mock(ICapellaMappedElementListViewViewModel.class);
        this.transactionService = mock(ICapellaTransactionService.class);
        when(this.mappedElementListViewViewModel.GetSelectedElement()).thenReturn(Observable.empty());
        
        this.hubMapResult = new ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>>();
        when(this.dstController.GetHubMapResult()).thenReturn(this.hubMapResult);
        
        this.selectedCapellaElementObservable = new ObservableValue<ElementRowViewModel<? extends CapellaElement>>();
        when(this.capellaObjectBrowser.GetSelectedElement()).thenReturn(this.selectedCapellaElementObservable.Observable());
        this.selectedElementDefinitionObservable = new ObservableValue<ThingRowViewModel<Thing>>();
        when(this.elementDefinitionBrowser.GetSelectedElement()).thenReturn(this.selectedElementDefinitionObservable.Observable());
        this.selectedRequirementObservable = new ObservableValue<ThingRowViewModel<Thing>>();
        when(this.requirementBrowserViewModel.GetSelectedElement()).thenReturn(this.selectedRequirementObservable.Observable());
        
        this.iteration = new Iteration();
        when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
        when(this.hubController.TryGetThingById(any(UUID.class), any(Ref.class))).thenReturn(true);
        
        when(this.transactionService.Clone(any())).thenAnswer(x -> 
        {
            var clonedReference = mock(ClonedReferenceElement.class);
            when(clonedReference.GetClone()).thenReturn(x.getArgument(0));
            when(clonedReference.GetOriginal()).thenReturn(x.getArgument(0));
            return clonedReference;
        });


        when(this.transactionService.Create(any(Class.class), any(String.class)))
            .thenAnswer(x -> this.AnswerToTransactionServiceCreate(x));

        when(this.transactionService.Create(any(Class.class)))
            .thenAnswer(x -> this.AnswerToTransactionServiceCreate(x));
        
        
        this.SetupElements();
        
        this.viewModel = new HubToDstMappingConfigurationDialogViewModel(this.dstController, this.hubController, 
                this.elementDefinitionBrowser, this.requirementBrowserViewModel, this.capellaObjectBrowser, this.transactionService, this.mappedElementListViewViewModel);
    }

    private Object AnswerToTransactionServiceCreate(InvocationOnMock invocationData)
    {
        var type = invocationData.getArgument(0, Class.class);
        
        if(invocationData.getArguments().length == 2)
        {
            return MockElement(invocationData.getArgument(1, String.class), type);
        }
        
        return this.MockElement("", type);
    }

    private Object MockElement(String elementName, Class<? extends NamedElement> type)
    {
        var mock = mock(type);
        when(mock.getName()).thenReturn(elementName);
        
        if(Component.class.isAssignableFrom(type))
        {
            when(((Component)mock).getContainedProperties()).thenReturn(new BasicEList<Property>());
            when(((Component)mock).getOwnedFeatures()).thenReturn(new BasicEList<Feature>());
            when(mock.eContents()).thenReturn(new BasicEList<EObject>());
            
            if(PhysicalComponent.class.isAssignableFrom(type))
            {
                when(((PhysicalComponent)mock).getOwnedPhysicalComponents()).thenReturn(new BasicEList<PhysicalComponent>());
            }
            if(LogicalComponent.class.isAssignableFrom(type))
            {
                when(((LogicalComponent)mock).getOwnedLogicalComponents()).thenReturn(new BasicEList<LogicalComponent>());
            }
        }
        
        return mock;
    }

    private void SetupElements()
    {
        this.elements = new ArrayList<>();
        var elementDefinition = new ElementDefinition();
        elementDefinition.setName("elementDefinition");
        
        var requirement = new cdp4common.engineeringmodeldata.Requirement();
        var requirementSpecification = new RequirementsSpecification();
        requirementSpecification.getRequirement().add(requirement);
        
        this.elements.add(requirement);
        this.elements.add(elementDefinition);
        
    }
    
    @Test
    public void VerifyUpdateElement()
    {
        var physicalComponent = mock(PhysicalComponent.class);
        when(physicalComponent.getId()).thenReturn(UUID.randomUUID().toString());
        when(physicalComponent.eContents()).thenReturn(new BasicEList<EObject>());
        this.viewModel.SetMappedElement(this.elements);
        this.viewModel.SetSelectedMappedElement(this.viewModel.GetMappedElementCollection().get(0));
        
        ElementRowViewModel<? extends NamedElement> componentRow = new ComponentRowViewModel(null, physicalComponent);
        assertDoesNotThrow(() -> this.selectedCapellaElementObservable.Value((ElementRowViewModel<NamedElement>)componentRow));
        
        this.viewModel.SetSelectedMappedElement(this.viewModel.GetMappedElementCollection().get(0));
        var requirement = mock(Requirement.class);
        when(requirement.getId()).thenReturn(UUID.randomUUID().toString());
        
        ElementRowViewModel<? extends NamedElement> requirementRow = new RequirementRowViewModel(null, requirement);
        assertDoesNotThrow(() -> this.selectedCapellaElementObservable.Value((ElementRowViewModel<NamedElement>) requirementRow));
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
        assertDoesNotThrow(() -> this.viewModel.SetMappedElement(this.elements));
    }

    @Test
    public void VerifyResetPremappedThings()
    {
        var elements = new ArrayList<EObject>();
        assertDoesNotThrow(() -> this.viewModel.SetMappedElement(this.elements));
        assertDoesNotThrow(() -> this.viewModel.ResetPreMappedThings());
    }
}
