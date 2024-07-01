/*
 * RequirementImpactViewViewModelTestFixture.java
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
package ViewModels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;

import DstController.IDstController;
import HubController.IHubController;
import Reactive.ObservableCollection;
import Utils.Ref;
import ViewModels.Interfaces.IMappedElementRowViewModel;
import ViewModels.ObjectBrowser.ElementDefinitionTree.Rows.ElementDefinitionRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;
import ViewModels.ObjectBrowser.RequirementTree.Rows.IterationRequirementRowViewModel;
import ViewModels.ObjectBrowser.RequirementTree.Rows.RequirementSpecificationRowViewModel;
import ViewModels.ObjectBrowser.Rows.IterationRowViewModel;
import ViewModels.ObjectBrowser.Rows.ThingRowViewModel;
import ViewModels.Rows.MappedDstRequirementRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.DomainOfExpertise;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.Requirement;
import io.reactivex.Observable;

class RequirementImpactViewViewModelTestFixture
{  
    private IDstController dstController;
    private IHubController hubController;
    private RequirementImpactViewViewModel viewModel;
    private ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>> elements;
    private Iteration iteration;
    private ObservableCollection<Thing> selectedDstMapResultForTransfer;
    private ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>> dstMapResult;
    private cdp4common.engineeringmodeldata.Requirement requirement0;
    private cdp4common.engineeringmodeldata.Requirement requirement1;
    private cdp4common.engineeringmodeldata.Requirement requirement2;
    private MappedElementRowViewModel<DefinedThing, ? extends NamedElement> mappedElement2;
    private MappedElementRowViewModel<DefinedThing, ? extends NamedElement> mappedElement1;
    private MappedElementRowViewModel<DefinedThing, ? extends NamedElement> mappedElement0;
    private RequirementsSpecification requirementsSpecification;

    @BeforeEach
    public void setUp() throws Exception
    {
        this.hubController = mock(IHubController.class);
        this.dstController = mock(IDstController.class);
        
        this.SetupMockedHubModel();
        
        when(this.hubController.GetIsSessionOpenObservable()).thenReturn(Observable.fromArray(true, true));
        when(this.hubController.GetSessionEventObservable()).thenReturn(Observable.fromArray(false, false));
        when(this.hubController.GetIsSessionOpen()).thenReturn(true);
        when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
        
        this.dstMapResult = new ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>>();
        when(this.dstController.GetDstMapResult()).thenReturn(this.dstMapResult);
        
        this.selectedDstMapResultForTransfer = new ObservableCollection<Thing>();
        when(this.dstController.GetSelectedDstMapResultForTransfer()).thenReturn(this.selectedDstMapResultForTransfer);
        
        this.viewModel = new RequirementImpactViewViewModel(this.hubController, this.dstController);
    }
    
    private void SetupMockedHubModel()
    {
        this.iteration = new Iteration();
    }
    
    @Test
    public void VerifyComputeDifferences() throws Exception
    {
        var timesTheBrowserTreeModelHasBeenUpdated = new Ref<Integer>(Integer.class, 0);
        
        this.viewModel.browserTreeModel.Observable().subscribe(x -> 
            timesTheBrowserTreeModelHasBeenUpdated.Set(timesTheBrowserTreeModelHasBeenUpdated.Get() + 1));
        
        this.SetupModelElements();
        
        assertEquals(1, this.viewModel.GetBrowserTreeModel().getRowCount());
                
        var elements = new ArrayList<MappedElementRowViewModel<DefinedThing, NamedElement>>();
        elements.add((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedElement0);
        elements.add((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedElement1);
        elements.add((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedElement2);
        
        this.dstMapResult.addAll(elements);
        
        Callable<Integer> treeRows = () -> ((IHaveContainedRows<?>)((IterationRowViewModel)(this.viewModel.GetBrowserTreeModel().getRoot()))
                                            .GetContainedRows().get(0)).GetContainedRows().size();
        assertEquals(3, treeRows.call());

        this.iteration.getRequirementsSpecification().add(this.requirementsSpecification);
        when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
        this.dstMapResult.addAll(elements);
        assertEquals(3, treeRows.call());
        this.dstMapResult.clear();
        assertEquals(3, treeRows.call());
        
        assertEquals(3, timesTheBrowserTreeModelHasBeenUpdated.Get());
    }

    @Test
    public void VerifyOnSelectionChanged()
    {
        this.SetupModelElements();

        this.viewModel = new RequirementImpactViewViewModel(this.hubController, this.dstController);
        this.iteration.getRequirementsSpecification().add(this.requirementsSpecification);
        when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
        this.viewModel.ComputeDifferences();
        ThingRowViewModel<? extends Thing> requirementSpecificationRow = ((IterationRequirementRowViewModel)this.viewModel.GetBrowserTreeModel().getRoot()).GetContainedRows().get(0);
        ((IHaveContainedRows<?extends ThingRowViewModel<?>>)requirementSpecificationRow).GetContainedRows().forEach(x -> x.SetIsHighlighted(true));
        requirementSpecificationRow.SetIsHighlighted(true);
        assertFalse(requirementSpecificationRow.GetIsSelected());
        assertDoesNotThrow(() -> this.viewModel.OnSelectionChanged((ThingRowViewModel<Thing>)requirementSpecificationRow));
        assertFalse(requirementSpecificationRow.GetIsSelected());
        this.dstMapResult.add((MappedElementRowViewModel<DefinedThing, NamedElement>) this.mappedElement0);
        this.dstMapResult.add((MappedElementRowViewModel<DefinedThing, NamedElement>) this.mappedElement1);
        this.dstMapResult.add((MappedElementRowViewModel<DefinedThing, NamedElement>) this.mappedElement2);
        when(this.dstController.GetDstMapResult()).thenReturn(this.dstMapResult);
        assertDoesNotThrow(() -> this.viewModel.OnSelectionChanged((ThingRowViewModel<Thing>) requirementSpecificationRow));
        
        assertTrue(((IHaveContainedRows<?extends ThingRowViewModel<?>>)requirementSpecificationRow).GetContainedRows().stream().allMatch(x -> x.GetIsSelected()));
        
        assertDoesNotThrow(() -> this.viewModel.OnSelectionChanged((ThingRowViewModel<Thing>) 
                ((IHaveContainedRows<?extends ThingRowViewModel<?>>)requirementSpecificationRow).GetContainedRows().get(0)));
        
        assertTrue(((IHaveContainedRows<?extends ThingRowViewModel<?>>)requirementSpecificationRow).GetContainedRows().stream().anyMatch(x -> !x.GetIsSelected()));
        verify(this.dstController, times(13)).GetSelectedDstMapResultForTransfer();
    }
    
    private void SetupModelElements()
    {
        var owner = new DomainOfExpertise();
        owner.setName("Owner");
        owner.setShortName("o");
        
        this.requirement0 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement0.setIid(UUID.randomUUID());
        this.requirement0.setOwner(owner);
        this.requirement0.setName("requirement0");
        this.requirement1 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement1.setIid(UUID.randomUUID());
        this.requirement1.setOwner(owner);
        this.requirement1.setName("requirement1");
        this.requirement2 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement2.setIid(UUID.randomUUID());
        this.requirement2.setOwner(owner);
        this.requirement2.setName("requirement2");
        
        this.requirementsSpecification = new RequirementsSpecification();
        this.requirementsSpecification.setOwner(owner);
        this.requirementsSpecification.setName("requirementsSpecification");
        this.requirementsSpecification.setShortName("requirementsSpecification");
        this.requirementsSpecification.getRequirement().add(this.requirement0);
        this.requirementsSpecification.getRequirement().add(this.requirement1);
        this.requirementsSpecification.getRequirement().add(this.requirement2);
        
        this.mappedElement0 = (MappedElementRowViewModel<DefinedThing, ? extends NamedElement>)mock(MappedElementRowViewModel.class);
        when(this.mappedElement0.GetHubElement()).thenReturn(this.requirement0);
        this.mappedElement1 = (MappedElementRowViewModel<DefinedThing, ? extends NamedElement>)mock(MappedElementRowViewModel.class);
        when(this.mappedElement1.GetHubElement()).thenReturn(this.requirement1);
        this.mappedElement2 = (MappedElementRowViewModel<DefinedThing, ? extends NamedElement>)mock(MappedElementRowViewModel.class);
        when(this.mappedElement2.GetHubElement()).thenReturn(this.requirement2);
    }    
}
