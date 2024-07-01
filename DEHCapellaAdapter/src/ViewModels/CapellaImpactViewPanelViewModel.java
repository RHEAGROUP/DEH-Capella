/*
 * CapellaImpactViewPanelViewModel.java
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

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import DstController.DstController;
import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.CapellaLog.ICapellaLogService;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Utils.Tasks.Task;
import ViewModels.Interfaces.ICapellaImpactViewPanelViewModel;
import ViewModels.Interfaces.ICapellaImpactViewViewModel;
import ViewModels.Interfaces.IElementDefinitionImpactViewViewModel;
import ViewModels.Interfaces.IImpactViewContextMenuViewModel;
import ViewModels.Interfaces.IRequirementImpactViewViewModel;
import ViewModels.Interfaces.ITransferControlViewModel;
import cdp4common.engineeringmodeldata.ExternalIdentifierMap;
import io.reactivex.Observable;

/**
 * The {@linkplain CapellaImpactViewPanelViewModel} is 
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class CapellaImpactViewPanelViewModel extends ImpactViewPanelViewModel implements ICapellaImpactViewPanelViewModel
{
    /**
     * A value indicating whether the session to the hub is open
     */
    private Observable<Boolean> isSessionOpen;
    
    /**
     * The {@linkplain IDstController} instance
     */
    private IDstController dstController;    

    /**
     * The {@linkplain ICapellaLogService}
     */
    private ICapellaLogService logService;
    
    /**
     * The {@linkplain IMagicDrawMappingConfigurationService}
     */
    private ICapellaMappingConfigurationService mappingConfigurationService;

    /**
     * The {@linkplain IElementDefinitionImpactViewViewModel}
     */
    private IElementDefinitionImpactViewViewModel elementDefinitionImpactViewViewModel;

    /**
     * Gets the {@linkplain IElementDefinitionImpactViewViewModel} elementDefinitionImpactViewViewModel
     * 
     * @return the {@linkplain IElementDefinitionImpactViewViewModel}
     */
    @Override
    public IElementDefinitionImpactViewViewModel GetElementDefinitionImpactViewViewModel()
    {
        return elementDefinitionImpactViewViewModel;
    }

    /**
     * The {@linkplain ITransferControlViewModel}
     */
    private ITransferControlViewModel transferControlViewModel;

    /**
     * Gets the {@linkplain ITransferControlViewModel}
     * 
     * @return a {@linkplain ITransferControlViewModel}
     */
    @Override
    public ITransferControlViewModel GetTransferControlViewModel()
    {
        return this.transferControlViewModel;
    }

    /**
     * The {@linkplain IImpactViewContextMenuViewModel}
     */
    private IImpactViewContextMenuViewModel contextMenuViewModel;    
    
    /**
     * Gets the {@linkplain IImpactViewContextMenuViewModel} view model for the context menus
     * 
     * @return a {@linkplain IImpactViewContextMenuViewModel}
     */
    @Override
    public IImpactViewContextMenuViewModel GetContextMenuViewModel()
    {
        return this.contextMenuViewModel;
    }
    
    /**
     * The {@linkplain IRequirementImpactViewViewModel}
     */
    private IRequirementImpactViewViewModel requirementDefinitionImpactViewViewModel;

    /**
     * Gets the {@linkplain IRequirementImpactViewViewModel} requirementDefinitionImpactViewViewModel
     * 
     * @return the {@linkplain IRequirementImpactViewViewModel}
     */
    @Override
    public IRequirementImpactViewViewModel GetRequirementDefinitionImpactViewViewModel()
    {
        return requirementDefinitionImpactViewViewModel;
    }

    /**
     * The {@linkplain ICapellaImpactViewViewModel}
     */
    private ICapellaImpactViewViewModel capellaImpactViewViewModel;

    /**
     * Gets the {@linkplain ICapellaImpactViewViewModel} instance
     * 
     * @return the {@linkplain ICapellaImpactViewViewModel}
     */
    @Override
    public ICapellaImpactViewViewModel GetCapellaImpactViewViewModel()
    {
        return capellaImpactViewViewModel;
    }
    
    /**
     * Gets the {@linkplain Observable} of {@linkplain Boolean} indicating whether there is any Capella model open
     * 
     * @return the {@linkplain Observable} of {@linkplain Boolean}
     */
    @Override
    public Observable<Boolean> GetHasAnyCapellaModelOpenObservable()
    {
        return this.dstController.HasAnyOpenSessionObservable().startWith(this.dstController.HasAnyOpenSession());
    }

    /**
     * Gets a value {@linkplain Boolean} indicating whether there is any Capella model open
     * 
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean GetHasAnyCapellaModelOpen()
    {
        return this.dstController.HasAnyOpenSession();
    }

    /**
     * Gets the {@linkplain Observable} of {@linkplain Boolean} indicating whether the session to the hub is open 
     * 
     * @return the {@linkplain Observable} of {@linkplain Boolean}
     */
    @Override
    public Observable<Boolean> GetIsHubSessionOpen()
    {
        return this.isSessionOpen;
    }

    /**
     * Gets a value indicating whether the Impact view can load mapping configurations
     * 
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean CanLoadMappingConfiguration()
    {
        return this.hubController.GetIsSessionOpen() && this.dstController.HasAnyOpenSession();
    }
    
    /**
     * Initializes a new {@linkplain MagicDrawImpactViewPanelViewModel}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param dstController the {@linkplain IDstController}
     * @param elementDefinitionImpactViewModel the {@linkplain IElementDefinitionImpactViewViewModel}
     * @param requirementImpactViewModel the {@linkplain IRequirementImpactViewViewModel}
     * @param transferControlViewModel the {@linkplain ITransferControlViewModel}
     * @param contextMenuViewModel the {@linkplain IImpactViewContextMenuViewModel}
     * @param capellaImpactViewViewModel the {@linkplain ICapellaImpactViewViewModel}
     * @param mappingConfigurationService the {@linkplain ICapellaMappingConfigurationService}
     * @param logService the {@linkplain IMagicDrawUILogService}
     */
    public CapellaImpactViewPanelViewModel(IHubController hubController, IDstController dstController,
            IElementDefinitionImpactViewViewModel elementDefinitionImpactViewModel, IRequirementImpactViewViewModel requirementImpactViewModel,
            ITransferControlViewModel transferControlViewModel, IImpactViewContextMenuViewModel contextMenuViewModel,
            ICapellaImpactViewViewModel capellaImpactViewViewModel, ICapellaMappingConfigurationService mappingConfigurationService, 
            ICapellaLogService logService)
    {
        super(hubController);
        this.dstController = dstController;
        this.transferControlViewModel = transferControlViewModel;
        this.contextMenuViewModel = contextMenuViewModel;
        this.mappingConfigurationService = mappingConfigurationService;
        this.logService = logService;
        this.isSessionOpen = this.hubController.GetIsSessionOpenObservable();
        this.elementDefinitionImpactViewViewModel = elementDefinitionImpactViewModel;
        this.requirementDefinitionImpactViewViewModel = requirementImpactViewModel;
        this.capellaImpactViewViewModel = capellaImpactViewViewModel;
    }

    /**
     * Gets the the saved mapping configurations names from the open {@linkplain Iteration}
     * 
     * @return a {@linkplain String} collection of the names of the available {@linkplain ExternalIdentifierMap}
     */
    @Override
    public List<String> GetSavedMappingconfigurationCollection()
    {
        if(!this.hubController.GetIsSessionOpen())
        {
            return new ArrayList<>();
        }
        
        List<String> externalIdentifierMaps = this.hubController.GetAvailableExternalIdentifierMap(DstController.THISTOOLNAME)
                .stream()
                .map(x -> x.getName())
                .sorted()
                .collect(Collectors.toList());

        externalIdentifierMaps.add(0, "");
        return externalIdentifierMaps;
    }

    /**
     * Gets the {@linkplain Callable} of {@linkplain MappingDirection} to call when the user switch {@linkplain MappingDirection}
     * 
     * @return a {@linkplain Callable} of {@linkplain MappingDirection}
     */
    @Override
    public Callable<MappingDirection> GetOnChangeMappingDirectionCallable()
    {
        return () -> this.dstController.ChangeMappingDirection();
    }

    /**
     * Executes when the Save/Load configuration is pressed.
     * Creates a new configuration or loads an existing one based on its name
     * 
     * @param configurationName the name of the configuration to load or create
     * @return a {@linkplain boolean} indicating whether the configuration is a new one
     */
    @Override
    public boolean OnSaveLoadMappingConfiguration(String configurationName)
    {
        boolean isNew = !AreTheseEquals(configurationName, this.mappingConfigurationService.GetExternalIdentifierMap().getName());
        
        if(configurationName.isEmpty())
        {
            return false;
        }

        if(isNew)
        {
            this.mappingConfigurationService.SetExternalIdentifierMap(this.hubController.GetAvailableExternalIdentifierMap(DstController.THISTOOLNAME)
                    .stream().filter(x -> AreTheseEquals(x.getName(), configurationName))
                    .findFirst()
                    .orElse(this.CreateNewMappingConfiguration(configurationName)));
        }
        
        Task.Run(() -> this.dstController.LoadMapping());
        
        this.logService.Append("The configuration %s is %s ...", configurationName, !isNew ? "reloading" : "loading");
        
        return isNew && this.mappingConfigurationService.GetExternalIdentifierMap().getRevisionNumber() < 1;
    }

    /**
     * Creates a new ExternalIdentifierMap based on the specified name
     * 
     * @param configurationName the String name of the new configuration
     * @return the newly created {@linkplain ExternalIdentifierMap}
     */
    private ExternalIdentifierMap CreateNewMappingConfiguration(String configurationName)
    {
        return this.mappingConfigurationService
                .CreateExternalIdentifierMap(configurationName, "Capella Model"
                        , this.CheckForExistingTemporaryMapping());
    }

    /**
     * Verifies that there is some existing mapping defined in the possibly temporary {@linkplain ExternalIdentifierMap} and asks the user if it wants to keep it
     * 
     * @return a {@linkplain boolean}
     */
    private boolean CheckForExistingTemporaryMapping()
    {
        return this.mappingConfigurationService.IsTheCurrentIdentifierMapTemporary() 
                && !this.mappingConfigurationService.GetExternalIdentifierMap().getCorrespondence().isEmpty()
                && JOptionPane.showConfirmDialog(null, "You have some mapping defined already, do you want to keep it?", "", JOptionPane.YES_NO_OPTION) 
                    == JOptionPane.YES_OPTION;
    }
}