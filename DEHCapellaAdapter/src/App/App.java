/*
 * AppTestFixture.java
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

package App;

import static org.picocontainer.Characteristics.CACHE;
import static org.picocontainer.Characteristics.NO_CACHE;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.picocontainer.Characteristics;

import DstController.DstController;
import DstController.IDstController;
import HubController.IHubController;
import MappingRules.BinaryRelationshipToCapellaTraces;
import MappingRules.CapellaTracesToBinaryRelationship;
import MappingRules.ComponentToElementMappingRule;
import MappingRules.ElementToComponentMappingRule;
import MappingRules.RequirementToRequirementsSpecificationMappingRule;
import MappingRules.RequirementsSpecificationToRequirementMappingRule;
import Services.CapellaLog.CapellaLogService;
import Services.CapellaLog.ICapellaLogService;
import Services.CapellaSelection.CapellaSelectionService;
import Services.CapellaSelection.ICapellaSelectionService;
import Services.CapellaSession.CapellaSessionListenerService;
import Services.CapellaSession.CapellaSessionService;
import Services.CapellaSession.ICapellaSessionListenerService;
import Services.CapellaSession.ICapellaSessionService;
import Services.CapellaSession.ISiriusSessionManagerWrapper;
import Services.CapellaSession.SiriusSessionManagerWrapper;
import Services.CapellaTransaction.CapellaTransactionService;
import Services.CapellaTransaction.ICapellaTransactionService;
import Services.CapellaUserPreference.CapellaUserPreferenceService;
import Services.CapellaUserPreference.ICapellaUserPreferenceService;
import Services.Diagram.IPhysicalArchitectureDiagramGenerator;
import Services.Diagram.PhysicalArchitectureDiagramGenerator;
import Services.HistoryService.CapellaLocalExchangeHistoryService;
import Services.HistoryService.ICapellaLocalExchangeHistoryService;
import Services.Mapping.IMapCommandService;
import Services.Mapping.MapCommandService;
import Services.MappingConfiguration.CapellaMappingConfigurationService;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Services.MappingEngineService.IMappingEngineService;
import Services.MappingEngineService.MappingEngineService;
import Services.AdapterInfo.CapellaAdapterInfoService;
import Services.AdapterInfo.IAdapterInfoService;
import Utils.Stereotypes.CapellaTracedElementCollection;
import ViewModels.CapellaHubBrowserPanelViewModel;
import ViewModels.CapellaImpactViewPanelViewModel;
import ViewModels.CapellaImpactViewViewModel;
import ViewModels.ElementDefinitionImpactViewViewModel;
import ViewModels.HubBrowserPanelViewModel;
import ViewModels.RequirementImpactViewViewModel;
import ViewModels.TransferControlViewModel;
import ViewModels.CapellaObjectBrowser.CapellaObjectBrowserViewModel;
import ViewModels.CapellaObjectBrowser.Interfaces.ICapellaObjectBrowserViewModel;
import ViewModels.ContextMenu.HubBrowserContextMenuViewModel;
import ViewModels.Dialogs.AlertMoreThanOneCapellaModelOpenDialogViewModel;
import ViewModels.Dialogs.DstToHubMappingConfigurationDialogViewModel;
import ViewModels.Dialogs.HubToDstMappingConfigurationDialogViewModel;
import ViewModels.Dialogs.Interfaces.IAlertMoreThanOneCapellaModelOpenDialogViewModel;
import ViewModels.Dialogs.Interfaces.IDstToHubMappingConfigurationDialogViewModel;
import ViewModels.Dialogs.Interfaces.IHubToDstMappingConfigurationDialogViewModel;
import ViewModels.Interfaces.ICapellaHubBrowserPanelViewModel;
import ViewModels.Interfaces.ICapellaImpactViewPanelViewModel;
import ViewModels.Interfaces.ICapellaImpactViewViewModel;
import ViewModels.Interfaces.IElementDefinitionImpactViewViewModel;
import ViewModels.Interfaces.IHubBrowserContextMenuViewModel;
import ViewModels.Interfaces.IHubBrowserPanelViewModel;
import ViewModels.Interfaces.IRequirementImpactViewViewModel;
import ViewModels.Interfaces.ITransferControlViewModel;
import ViewModels.MappedElementListView.CapellaMappedElementListViewViewModel;
import ViewModels.MappedElementListView.MappedElementListViewViewModel;
import ViewModels.MappedElementListView.Interfaces.ICapellaMappedElementListViewViewModel;
import ViewModels.MappedElementListView.Interfaces.IMappedElementListViewViewModel;
import ViewModels.MappingListView.CapellaMappingListViewViewModel;
import ViewModels.MappingListView.Interfaces.IMappingListViewViewModel;

/**
 * The {@linkplain App} class is the main entry point for the DEH-Capella adapter
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class App extends AbstractUIPlugin
{
    /**
     * The current class logger
     */
    private Logger logger = LogManager.getLogger();
    
    /**
     * Starts the DEH Capella Adapter, initializing the IoC container and logs the initialization
     * 
     * @param context the {@linkplain BundleContext}
     * @throws Exception might throw {@linkplain Exception}
     */
    @Override
    public void start(BundleContext context) throws Exception
    {
        this.RegisterDependencies();
        super.start(context);
        AppContainer.Container.getComponent(IMapCommandService.class).Initialize();
        this.LogAdapterInitialization();
    }
    
    /**
     * Logs the initialization of the adapter
     */
    private void LogAdapterInitialization()
    {
        var version = AppContainer.Container.getComponent(IAdapterInfoService.class).GetVersion();
        var message = String.format("DEH-Capella adapter %s initialized with success", version);
        
        this.logger.info("----------------------------------------------------------------------------");
        this.logger.info(String.format("---------%s---------", message));
        this.logger.info("----------------------------------------------------------------------------");
        
        AppContainer.Container.getComponent(ICapellaLogService.class).Append(message);
    }

    /**
     * The stop method is executed when the plug-in gets unloaded, 
     * it stops the IoC container and gracefully closes open connections to the Hub
     * 
     * @param context the {@linkplain BundleContext}
     * @throws Exception might throw {@linkplain Exception}
     */
    @Override
    public void stop(BundleContext context) throws Exception
    {
        AppContainer.Container.getComponent(IHubController.class).Close();
        AppContainer.Container.stop();
        super.stop(context);
    }

    /**
     * Registers the dependencies of the DEH-Capella plugin into the {@linkplain AppContainer.Container}
     */
    public void RegisterDependencies()
    {
        try
        {
            AppContainer.Container.as(CACHE).addComponent(IDstController.class, DstController.class);
            AppContainer.Container.addConfig(MappingEngineService.AssemblyParameterName, ComponentToElementMappingRule.class.getPackage());
            AppContainer.Container.as(CACHE, Characteristics.USE_NAMES).addComponent(IMappingEngineService.class, MappingEngineService.class);
            AppContainer.Container.addComponent(ICapellaMappingConfigurationService.class, CapellaMappingConfigurationService.class);
            AppContainer.Container.as(CACHE).addComponent(ICapellaSessionService.class, CapellaSessionService.class);
            AppContainer.Container.addComponent(ICapellaSessionListenerService.class, CapellaSessionListenerService.class);
            AppContainer.Container.addConfig("platformLogger", Platform.getLog(FrameworkUtil.getBundle(CapellaLogService.class)));
            AppContainer.Container.as(Characteristics.USE_NAMES).addComponent(ICapellaLogService.class, CapellaLogService.class);
            AppContainer.Container.addComponent(ICapellaSelectionService.class, CapellaSelectionService.class);
            AppContainer.Container.as(CACHE).addComponent(IMapCommandService.class, MapCommandService.class);
            AppContainer.Container.addComponent(ISiriusSessionManagerWrapper.class, SiriusSessionManagerWrapper.class);
            AppContainer.Container.as(CACHE).addComponent(ICapellaTransactionService.class, CapellaTransactionService.class);
            AppContainer.Container.addComponent(IAdapterInfoService.class, CapellaAdapterInfoService.class);
            AppContainer.Container.as(CACHE).addComponent(ICapellaLocalExchangeHistoryService.class, CapellaLocalExchangeHistoryService.class);
            AppContainer.Container.addComponent(ICapellaUserPreferenceService.class, CapellaUserPreferenceService.class);
            AppContainer.Container.addComponent(IPhysicalArchitectureDiagramGenerator.class, PhysicalArchitectureDiagramGenerator.class);

            AppContainer.Container.addComponent(ComponentToElementMappingRule.class.getName(), ComponentToElementMappingRule.class);
            AppContainer.Container.addComponent(ElementToComponentMappingRule.class.getName(), ElementToComponentMappingRule.class);
            AppContainer.Container.addComponent(RequirementToRequirementsSpecificationMappingRule.class.getName(), RequirementToRequirementsSpecificationMappingRule.class);
            AppContainer.Container.addComponent(RequirementsSpecificationToRequirementMappingRule.class.getName(), RequirementsSpecificationToRequirementMappingRule.class);
            AppContainer.Container.addComponent(CapellaTracesToBinaryRelationship.class.getName(), CapellaTracesToBinaryRelationship.class);
            AppContainer.Container.addComponent(BinaryRelationshipToCapellaTraces.class.getName(), BinaryRelationshipToCapellaTraces.class);
            
            AppContainer.Container.addComponent(IElementDefinitionImpactViewViewModel.class, ElementDefinitionImpactViewViewModel.class);
            AppContainer.Container.addComponent(IRequirementImpactViewViewModel.class, RequirementImpactViewViewModel.class);
            AppContainer.Container.addComponent(IHubBrowserPanelViewModel.class, HubBrowserPanelViewModel.class);
            AppContainer.Container.addComponent(ICapellaImpactViewPanelViewModel.class, CapellaImpactViewPanelViewModel.class);
            AppContainer.Container.addComponent(ITransferControlViewModel.class, TransferControlViewModel.class);
            AppContainer.Container.addComponent(ICapellaObjectBrowserViewModel.class, CapellaObjectBrowserViewModel.class);
            AppContainer.Container.addComponent(ICapellaImpactViewViewModel.class, CapellaImpactViewViewModel.class);
            AppContainer.Container.addComponent(IDstToHubMappingConfigurationDialogViewModel.class, DstToHubMappingConfigurationDialogViewModel.class);
            AppContainer.Container.addComponent(IHubToDstMappingConfigurationDialogViewModel.class, HubToDstMappingConfigurationDialogViewModel.class);
            AppContainer.Container.as(NO_CACHE).addComponent(IHubBrowserContextMenuViewModel.class, HubBrowserContextMenuViewModel.class);
            AppContainer.Container.addComponent(ICapellaHubBrowserPanelViewModel.class, CapellaHubBrowserPanelViewModel.class);
            AppContainer.Container.addComponent(ICapellaMappedElementListViewViewModel.class, CapellaMappedElementListViewViewModel.class);
            AppContainer.Container.addComponent(IMappingListViewViewModel.class, CapellaMappingListViewViewModel.class);
            AppContainer.Container.addComponent(IAlertMoreThanOneCapellaModelOpenDialogViewModel.class.getSimpleName(), AlertMoreThanOneCapellaModelOpenDialogViewModel.class);
        }
        catch (Exception exception) 
        {
            this.logger.error(String.format("DEHCapella register dependencies has thrown an exception with %s %n %s", exception.toString(), exception.getStackTrace()));
            this.logger.catching(exception);
            throw exception;
        }
    }
}
