/*
 * CapellaHubBrowserPanelViewModel.java
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

import HubController.IHubController;
import Services.NavigationService.INavigationService;
import ViewModels.Interfaces.ICapellaHubBrowserPanelViewModel;
import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IHubBrowserContextMenuViewModel;
import ViewModels.Interfaces.IHubBrowserHeaderViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import ViewModels.Interfaces.ISessionControlPanelViewModel;

/**
 * The CapellaHubBrowserPanelViewModel is the view model for the {@linkplain CapellaHubBrowserPanel}
 */
public class CapellaHubBrowserPanelViewModel extends HubBrowserPanelViewModel implements ICapellaHubBrowserPanelViewModel
{
    /**
     * Backing field for {@linkplain #GetElementDefinitionBrowserContextMenuViewModel()}
     */
    private IHubBrowserContextMenuViewModel elementDefinitionBrowserContextMenuViewModel;
    
    /**
     * The {@linkplain IHubBrowserContextMenuViewModel} for the element definition browser
     * 
     * @return a {@linkplain IHubBrowserContextMenuViewModel}
     */
    @Override
    public IHubBrowserContextMenuViewModel GetElementDefinitionBrowserContextMenuViewModel()
    {
        return this.elementDefinitionBrowserContextMenuViewModel;
    }
    
    private IHubBrowserContextMenuViewModel requirementBrowserContextMenuViewModel;

    /**
     * The {@linkplain IHubBrowserContextMenuViewModel} for the requirement browser
     * 
     * @return a {@linkplain IHubBrowserContextMenuViewModel}
     */
    @Override
    public IHubBrowserContextMenuViewModel GetRequirementBrowserContextMenuViewModel()
    {
        return this.requirementBrowserContextMenuViewModel;
    }
    
    /**
     * Initializes a new {@link CapellaHubBrowserPanelViewModel}
     * 
     * @param navigationService the {@linkplain INavigationService}
     * @param hubController the {@linkplain IHubController}
     * @param hubBrowserHeaderViewModel the {@linkplain IHubBrowserHeaderViewModel}
     * @param requirementBrowserViewModel the {@linkplain IRequirementBrowserViewModel}
     * @param elementDefinitionBrowserViewModel the {@linkplain IElementDefinitionBrowserViewModel}
     * @param sessionControlViewModel the {@linkplain ISessionControlPanelViewModel}
     * @param elementDefinitionBrowserContextMenuViewModel the {@linkplain IHubBrowserContextMenuViewModel} for the element definition browser
     * @param requirementBrowserContextMenuViewModel the {@linkplain IHubBrowserContextMenuViewModel} for the requirement browser
     */
    public CapellaHubBrowserPanelViewModel(INavigationService navigationService, IHubController hubController,
            IHubBrowserHeaderViewModel hubBrowserHeaderViewModel, IRequirementBrowserViewModel requirementBrowserViewModel,
            IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel, ISessionControlPanelViewModel sessionControlViewModel, 
            IHubBrowserContextMenuViewModel elementDefinitionBrowserContextMenuViewModel, IHubBrowserContextMenuViewModel requirementBrowserContextMenuViewModel)
    {
        super(navigationService, hubController, hubBrowserHeaderViewModel, requirementBrowserViewModel,
                elementDefinitionBrowserViewModel, sessionControlViewModel, requirementBrowserContextMenuViewModel, requirementBrowserContextMenuViewModel);
        
        this.elementDefinitionBrowserContextMenuViewModel = elementDefinitionBrowserContextMenuViewModel;
        this.requirementBrowserContextMenuViewModel = requirementBrowserContextMenuViewModel;
        
    }
}
