/*
 * ICapellaHubBrowserPanelViewModel.java
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
package ViewModels.Interfaces;

/**
 * The ICapellaHubBrowserPanelViewModel is the interface definition for the {@linkplain CapellaHubBrowserPanelViewModel}
 */
public interface ICapellaHubBrowserPanelViewModel extends IHubBrowserPanelViewModel
{
    /**
     * The {@linkplain IHubBrowserContextMenuViewModel} for the requirement browser
     * 
     * @return a {@linkplain IHubBrowserContextMenuViewModel}
     */
    IHubBrowserContextMenuViewModel GetRequirementBrowserContextMenuViewModel();

    /**
     * The {@linkplain IHubBrowserContextMenuViewModel} for the element definition browser
     * 
     * @return a {@linkplain IHubBrowserContextMenuViewModel}
     */
    IHubBrowserContextMenuViewModel GetElementDefinitionBrowserContextMenuViewModel();
}
