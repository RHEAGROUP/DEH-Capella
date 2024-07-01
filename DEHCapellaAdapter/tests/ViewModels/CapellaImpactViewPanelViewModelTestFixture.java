/*
 * CapellaImpactViewPanelViewModelTestFixture.java
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

import org.apache.logging.log4j.core.util.Assert;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import DstController.DstController;
import DstController.IDstController;
import HubController.IHubController;
import Services.CapellaLog.ICapellaLogService;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import ViewModels.Interfaces.ICapellaImpactViewPanelViewModel;
import ViewModels.Interfaces.ICapellaImpactViewViewModel;
import ViewModels.Interfaces.IElementDefinitionImpactViewViewModel;
import ViewModels.Interfaces.IImpactViewContextMenuViewModel;
import ViewModels.Interfaces.IRequirementImpactViewViewModel;
import ViewModels.Interfaces.ITransferControlViewModel;
import cdp4common.engineeringmodeldata.ExternalIdentifierMap;

class CapellaImpactViewPanelViewModelTestFixture
{
    private IHubController hubController;
    private IDstController dstController;
    private IElementDefinitionImpactViewViewModel elementDefinitionImpactViewViewModel;
    private IRequirementImpactViewViewModel requirementImpactViewViewModel;
    private ITransferControlViewModel transferControlViewModel;
    private IImpactViewContextMenuViewModel impactViewContextMenuViewModel;
    private ICapellaLogService capellaLogService;
    private ICapellaMappingConfigurationService capellaMappingConfigurationService;
    private ICapellaImpactViewPanelViewModel viewModel;
    private ICapellaImpactViewViewModel capellaImpactViewViewModel;

    @BeforeEach
    public void setUp() throws Exception
    {
        this.hubController = mock(IHubController.class);
        this.dstController = mock(IDstController.class);
        this.elementDefinitionImpactViewViewModel = mock(IElementDefinitionImpactViewViewModel.class);
        this.requirementImpactViewViewModel = mock(IRequirementImpactViewViewModel.class);
        this.transferControlViewModel = mock(ITransferControlViewModel.class);
        this.impactViewContextMenuViewModel = mock(IImpactViewContextMenuViewModel.class);
        this.capellaImpactViewViewModel = mock(ICapellaImpactViewViewModel.class);
        this.capellaMappingConfigurationService = mock(ICapellaMappingConfigurationService.class);
        this.capellaLogService = mock(ICapellaLogService.class);
        
        this.viewModel = new CapellaImpactViewPanelViewModel(this.hubController, this.dstController, 
                this.elementDefinitionImpactViewViewModel, this.requirementImpactViewViewModel,
                this.transferControlViewModel, this.impactViewContextMenuViewModel, this.capellaImpactViewViewModel,
                this.capellaMappingConfigurationService, this.capellaLogService);
    }
    
    @Test
    public void VerifyGetSavedMappingconfigurationCollection()
    {
        var availableExternalIdentifierMap = new ArrayList<ExternalIdentifierMap>();
        ExternalIdentifierMap externalIdentifierMap = new ExternalIdentifierMap();
        externalIdentifierMap.setName("name");
        availableExternalIdentifierMap.add(externalIdentifierMap);
        when(this.hubController.GetAvailableExternalIdentifierMap(DstController.THISTOOLNAME)).thenReturn(availableExternalIdentifierMap);
        when(this.hubController.GetIsSessionOpen()).thenReturn(false);        
        Assert.isEmpty(this.viewModel.GetSavedMappingconfigurationCollection());
        when(this.hubController.GetIsSessionOpen()).thenReturn(true);        
        assertEquals(2, this.viewModel.GetSavedMappingconfigurationCollection().size());
    }
    
    @Test
    public void VerifyGetOnChangeMappingDirectionCallable()
    {
        assertDoesNotThrow(() -> this.viewModel.GetOnChangeMappingDirectionCallable().call());
    }

    @Test
    public void VerifyOnSaveLoadMappingConfiguration()
    {
        when(this.capellaMappingConfigurationService.GetExternalIdentifierMap()).thenReturn(new ExternalIdentifierMap());
        assertDoesNotThrow(() -> this.viewModel.OnSaveLoadMappingConfiguration("name"));
        
        var availableExternalIdentifierMap = new ArrayList<ExternalIdentifierMap>();
        ExternalIdentifierMap externalIdentifierMap = new ExternalIdentifierMap();
        when(this.hubController.GetAvailableExternalIdentifierMap(DstController.THISTOOLNAME)).thenReturn(availableExternalIdentifierMap);
        
        assertDoesNotThrow(() -> this.viewModel.OnSaveLoadMappingConfiguration("newName"));
        externalIdentifierMap.setName("name");
        availableExternalIdentifierMap.add(externalIdentifierMap);
        assertDoesNotThrow(() -> this.viewModel.OnSaveLoadMappingConfiguration("name"));
        
        verify(this.capellaLogService, times(3)).Append(any(String.class), anyString(), anyString());
        verify(this.dstController, times(3)).LoadMapping();
        verify(this.capellaMappingConfigurationService, times(1)).CreateExternalIdentifierMap("newName", "Capella Model", false);
    }
}
