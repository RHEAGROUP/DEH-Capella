/*
 * CapellaImpactViewViewModelTestFixture.java
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
package ViewModels;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.la.LogicalComponent;

import DstController.IDstController;
import Enumerations.MappingDirection;
import Reactive.ObservableCollection;
import Services.CapellaSession.CapellaSessionRelatedBaseTestFixture;
import Services.CapellaSession.ICapellaSessionService;
import Services.CapellaTransaction.ICapellaTransactionService;
import ViewModels.CapellaObjectBrowser.Rows.ComponentRowViewModel;
import ViewModels.CapellaObjectBrowser.Rows.RootRowViewModel;import ViewModels.ObjectBrowser.RequirementTree.Rows.RequirementSpecificationRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.Thing;
import io.reactivex.Observable;

public class CapellaImpactViewViewModelTestFixture extends CapellaSessionRelatedBaseTestFixture
{
    private ICapellaSessionService sessionService;
    private IDstController dstController;
    private CapellaImpactViewViewModel viewModel;
    private ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>> hubMapResult;
    private ICapellaTransactionService transactionService;

    @BeforeEach
    public void Setup()
    {
        this.dstController = mock(IDstController.class);
        this.sessionService = mock(ICapellaSessionService.class);
        this.transactionService = mock(ICapellaTransactionService.class);
        
        this.hubMapResult = new ObservableCollection<>();
        when(this.dstController.GetHubMapResult()).thenReturn(this.hubMapResult);
        when(this.dstController.HasAnyOpenSessionObservable()).thenReturn(Observable.fromArray(false, true, true));
        when(this.dstController.GetSelectedHubMapResultForTransfer()).thenReturn(new ObservableCollection());
        when(this.transactionService.IsClonedOrNew(any())).thenAnswer(x -> new Random().nextBoolean());

        var session = this.GetSession(URI.createURI("test"));
        
        RootRowViewModel rootRowViewModel = new RootRowViewModel("", CapellaSessionRelatedBaseTestFixture.GetSessionElements(session, Notifier.class));
        when(this.sessionService.GetModels()).thenReturn(rootRowViewModel);
        this.viewModel = new CapellaImpactViewViewModel(this.dstController, this.sessionService, this.transactionService);
    }
    
    @Test
    public void VerifyOnSelectionChanged()
    {
        var logicalComponent = mock(LogicalComponent.class);
        when(logicalComponent.getId()).thenReturn(UUID.randomUUID().toString());
        when(logicalComponent.eContents()).thenReturn(new BasicEList<>());
        var rowViewModel = new ComponentRowViewModel(null, logicalComponent);
        assertDoesNotThrow(() -> this.viewModel.OnSelectionChanged(rowViewModel));
        assertFalse(rowViewModel.GetIsSelected());
        assertDoesNotThrow(() -> this.viewModel.OnSelectionChanged(rowViewModel));
        assertFalse(rowViewModel.GetIsSelected());
        
        MappedElementRowViewModel<? extends DefinedThing, ? extends NamedElement> mappedElementDefinitionRowViewModel = 
                new MappedElementDefinitionRowViewModel(null, logicalComponent, MappingDirection.FromHubToDst);
        
        this.hubMapResult.add((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedElementDefinitionRowViewModel);
        assertDoesNotThrow(() -> this.viewModel.OnSelectionChanged(rowViewModel));
        assertTrue(rowViewModel.GetIsSelected());
        assertDoesNotThrow(() -> this.viewModel.OnSelectionChanged(rowViewModel));
        assertFalse(rowViewModel.GetIsSelected());
        verify(this.dstController, times(6)).GetSelectedHubMapResultForTransfer();
    }
    
    @Test
    public void VerifyComputeDifferences()
    {
        var newHubMapResult = new ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>>();
        when(this.dstController.GetHubMapResult()).thenReturn(newHubMapResult);
        
        MappedElementRowViewModel<? extends DefinedThing, ? extends NamedElement> mappedElementDefinitionRowViewModel0 = 
                new MappedElementDefinitionRowViewModel(this.LogicalComponent, MappingDirection.FromHubToDst);
        
        newHubMapResult.add((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedElementDefinitionRowViewModel0);
        when(this.sessionService.HasAnyOpenSession()).thenReturn(true);
        this.hubMapResult.clear();
        when(this.sessionService.HasAnyOpenSession()).thenReturn(false);
        this.hubMapResult.clear();
        when(this.sessionService.HasAnyOpenSession()).thenReturn(true);
        var newNewHubMapResult = new ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>>();
        when(this.dstController.GetHubMapResult()).thenReturn(newNewHubMapResult);
        this.viewModel.UpdateBrowserTrees(true);
        
        MappedElementRowViewModel<? extends DefinedThing, ? extends NamedElement> mappedElementDefinitionRowViewModel1 = 
                new MappedElementDefinitionRowViewModel(this.LogicalComponent, MappingDirection.FromHubToDst);
        
        newNewHubMapResult.add((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedElementDefinitionRowViewModel1);
    }
}
