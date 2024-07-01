/*
 * TransferControlViewModelTestFixture.java
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

import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.emf.ecore.EObject;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.polarsys.capella.core.data.capellacore.CapellaElement;

import DstController.IDstController;
import Enumerations.CapellaArchitecture;
import Enumerations.MappingDirection;
import Reactive.ObservableCollection;
import Reactive.ObservableValue;
import Services.CapellaLog.ICapellaLogService;
import Services.HistoryService.ICapellaLocalExchangeHistoryService;
import Services.LocalExchangeHistory.ILocalExchangeHistoryService;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import io.reactivex.Observable;

class TransferControlViewModelTestFixture
{

    private IDstController dstController;
    private ICapellaLogService logService;
    private TransferControlViewModel viewModel;
    private ObservableValue<MappingDirection> mappingDirections;
    private ICapellaLocalExchangeHistoryService transferHistory;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        this.logService = mock(ICapellaLogService.class);
        this.dstController = mock(IDstController.class);
        this.transferHistory = mock(ICapellaLocalExchangeHistoryService.class);
        
        var dstMapResult = new ObservableCollection<Thing>();
        dstMapResult.add(new ElementDefinition());
        dstMapResult.add(new ElementDefinition());
        dstMapResult.add(new ElementDefinition());
        
        this.mappingDirections = new ObservableValue<MappingDirection>(MappingDirection.FromDstToHub, MappingDirection.class);
        
        when(this.dstController.GetMappingDirection()).thenReturn(this.mappingDirections.Observable());
        
        when(this.dstController.GetSelectedDstMapResultForTransfer()).thenReturn(dstMapResult);
        when(this.dstController.GetSelectedHubMapResultForTransfer()).thenReturn(new ObservableCollection<CapellaElement>());
                
        this.viewModel = new TransferControlViewModel(this.dstController, this.logService, this.transferHistory);
    }

    @Test
    public void VerifyNumberOfSelectedThingGetsUpdatedOnChangeTransferDirection() throws InterruptedException
    {
        var result = new ArrayList<Integer>();
        
        this.viewModel.GetNumberOfSelectedThing().subscribe(x -> result.add(x));
        
        this.mappingDirections.Value(MappingDirection.FromDstToHub);
        this.mappingDirections.Value(MappingDirection.FromHubToDst);
        
        Thread.sleep(50);
        
        assertEquals(3, result.get(0));
        assertEquals(0, result.get(1));
    }
    
    @Test
    public void VerifyGetOnTransferCallable()
    {
        var transferCallable = this.viewModel.GetOnTransferCallable();
        assertNotNull(transferCallable);
        
        assertDoesNotThrow(() -> transferCallable.call());
        
        verify(this.logService, times(1)).Append(any(String.class), any(boolean.class));
    }
}
