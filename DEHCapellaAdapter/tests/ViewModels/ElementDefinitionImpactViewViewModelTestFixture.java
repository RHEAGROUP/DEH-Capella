/*
 * ElementDefinitionImpactViewViewModelTestFixture.java
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

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.eclipse.emf.ecore.EObject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;

import Reactive.ObservableCollection;
import Utils.Ref;
import ViewModels.ObjectBrowser.ElementDefinitionTree.Rows.ElementDefinitionRowViewModel;
import ViewModels.ObjectBrowser.ElementDefinitionTree.Rows.IterationElementDefinitionRowViewModel;
import ViewModels.ObjectBrowser.RequirementTree.Rows.IterationRequirementRowViewModel;
import ViewModels.ObjectBrowser.Rows.IterationRowViewModel;
import ViewModels.ObjectBrowser.Rows.ThingRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.sitedirectorydata.DomainOfExpertise;
import io.reactivex.Observable;
import DstController.IDstController;
import HubController.IHubController;

class ElementDefinitionImpactViewViewModelTestFixture
{
    private IDstController dstController;
    private IHubController hubController;
    private ElementDefinitionImpactViewViewModel viewModel;
    private Collection<MappedElementRowViewModel<DefinedThing, NamedElement>> elements;
    private Iteration iteration;
    private ObservableCollection<Thing> selectedDstMapResultForTransfer;
    private ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>> dstMapResult;
    private ElementDefinition elementDefinition0;
    private ElementDefinition elementDefinition1;
    private ElementDefinition elementDefinition2;
    private MappedElementRowViewModel<DefinedThing, NamedElement> mappedElement0;
    private MappedElementRowViewModel<DefinedThing, NamedElement> mappedElement1;
    private MappedElementRowViewModel<DefinedThing, NamedElement> mappedElement2;

    @BeforeEach
    public void setUp() throws Exception
    {
        this.hubController = mock(IHubController.class);
        this.dstController = mock(IDstController.class);
        
        this.SetupMockedHubModel();
        
        when(this.hubController.GetIsSessionOpenObservable()).thenReturn(Observable.fromArray(false, true));
        when(this.hubController.GetSessionEventObservable()).thenReturn(Observable.fromArray(false, false));
        when(this.hubController.GetIsSessionOpen()).thenReturn(true);
        when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
        
        this.dstMapResult = new ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>>();
        when(this.dstController.GetDstMapResult()).thenReturn(this.dstMapResult);
        
        this.selectedDstMapResultForTransfer = new ObservableCollection<Thing>();
        when(this.dstController.GetSelectedDstMapResultForTransfer()).thenReturn(this.selectedDstMapResultForTransfer);
        
        this.viewModel = new ElementDefinitionImpactViewViewModel(this.hubController, this.dstController);
    }

    private void SetupMockedHubModel()
    {
        this.iteration = new Iteration();
    }

    @Test
    public void VerifyComputeDifferences() throws Exception
    {
        this.SetupModelElements();
        
        var timesTheBrowserTreeModelHasBeenUpdated = new Ref<Integer>(Integer.class, 0);
        
        this.viewModel.browserTreeModel.Observable().subscribe(x -> 
            timesTheBrowserTreeModelHasBeenUpdated.Set(timesTheBrowserTreeModelHasBeenUpdated.Get() + 1));
        
        this.dstMapResult.addAll(this.elements);
        Callable<Integer> treeRows = () -> ((IterationRowViewModel)(this.viewModel.GetBrowserTreeModel().getRoot()))
                                            .GetContainedRows().size();
        assertEquals(3, treeRows.call());
        this.dstMapResult.clear();
        assertEquals(0, treeRows.call());
        this.iteration.getElement().add(elementDefinition0);
        when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
        this.dstMapResult.addAll(elements);
        assertEquals(3, treeRows.call());
        
        assertEquals(3, timesTheBrowserTreeModelHasBeenUpdated.Get());
    }
    
    @Test
    public void VerifySwitchIsSelected()
    {
        this.SetupModelElements();
        this.iteration.getElement().addAll(Arrays.asList(this.elementDefinition0, this.elementDefinition1, this.elementDefinition2));
        var dstMapResultToTransfer = new ObservableCollection<Thing>();
        when(this.dstController.GetSelectedDstMapResultForTransfer()).thenReturn(dstMapResultToTransfer);
        when(this.hubController.GetIsSessionOpenObservable()).thenReturn(Observable.fromArray(false, true));
        when(this.hubController.GetIsSessionOpen()).thenReturn(false);
        this.viewModel = new ElementDefinitionImpactViewViewModel(this.hubController, this.dstController);
        assertDoesNotThrow(() -> dstMapResultToTransfer.addAll(Arrays.asList(this.elementDefinition0)));
        assertDoesNotThrow(() -> dstMapResultToTransfer.RemoveOne(this.elementDefinition0));
    }

    @Test
    public void VerifyOnSelectionChanged()
    {
        this.SetupModelElements();

        this.viewModel = new ElementDefinitionImpactViewViewModel(this.hubController, this.dstController);
        this.iteration.getElement().add(this.elementDefinition0);
        this.iteration.getElement().add(this.elementDefinition1);
        this.iteration.getElement().add(this.elementDefinition2);
        when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
        this.viewModel.ComputeDifferences();
        var rootRowViewModel = (IterationElementDefinitionRowViewModel)this.viewModel.GetBrowserTreeModel().getRoot();
        rootRowViewModel.GetContainedRows().forEach(x -> x.SetIsHighlighted(true));
                
        ThingRowViewModel<? extends Thing> elementDefinitionRow = rootRowViewModel.GetContainedRows().get(0);
        ThingRowViewModel<Thing> thingRowViewModelBeautifullyCasted = (ThingRowViewModel<Thing>) elementDefinitionRow;
        
        assertFalse(elementDefinitionRow.GetIsSelected());
        assertDoesNotThrow(() -> this.viewModel.OnSelectionChanged(thingRowViewModelBeautifullyCasted));
        assertFalse(elementDefinitionRow.GetIsSelected());
        this.dstMapResult.add(this.mappedElement0);
        assertDoesNotThrow(() -> this.viewModel.OnSelectionChanged(thingRowViewModelBeautifullyCasted));
        assertTrue(elementDefinitionRow.GetIsSelected());
        assertDoesNotThrow(() -> this.viewModel.OnSelectionChanged(thingRowViewModelBeautifullyCasted));
        assertFalse(elementDefinitionRow.GetIsSelected());
        verify(this.dstController, times(9)).GetSelectedDstMapResultForTransfer();
    }
    
    private void SetupModelElements()
    {
        var owner = new DomainOfExpertise();
        owner.setName("Owner");
        owner.setShortName("o");
        this.elementDefinition0 = new ElementDefinition();
        this.elementDefinition0.setIid(UUID.randomUUID());
        this.elementDefinition0.setOwner(owner);
        this.elementDefinition0.setName("elementDefinition0");
        this.elementDefinition1 = new ElementDefinition();
        this.elementDefinition1.setIid(UUID.randomUUID());
        this.elementDefinition1.setOwner(owner);
        this.elementDefinition1.setName("elementDefinition1");
        this.elementDefinition2 = new ElementDefinition();
        this.elementDefinition2.setIid(UUID.randomUUID());
        this.elementDefinition2.setOwner(owner);
        this.elementDefinition2.setName("elementDefinition2");
        
        this.mappedElement0 = (MappedElementRowViewModel<DefinedThing, NamedElement>)mock(MappedElementRowViewModel.class);
        when(mappedElement0.GetHubElement()).thenReturn(elementDefinition0.clone(false));
        this.mappedElement1 = (MappedElementRowViewModel<DefinedThing, NamedElement>)mock(MappedElementRowViewModel.class);
        when(mappedElement1.GetHubElement()).thenReturn(elementDefinition1);
        this.mappedElement2 = (MappedElementRowViewModel<DefinedThing, NamedElement>)mock(MappedElementRowViewModel.class);
        when(mappedElement2.GetHubElement()).thenReturn(elementDefinition2);

        assertEquals(1, this.viewModel.GetBrowserTreeModel().getRowCount());
                
        this.elements = new ArrayList<MappedElementRowViewModel<DefinedThing, NamedElement>>();
        elements.add(this.mappedElement0);
        elements.add(this.mappedElement1);
        elements.add(this.mappedElement2);
    }
}
