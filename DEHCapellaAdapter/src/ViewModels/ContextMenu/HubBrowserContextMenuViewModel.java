/*
 * HubBrowserContextMenuViewModel.java
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
package ViewModels.ContextMenu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Reactive.ObservableValue;
import Services.Mapping.IMapCommandService;
import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IHubBrowserContextMenuViewModel;
import ViewModels.Interfaces.IObjectBrowserViewModel;
import io.reactivex.Observable;

/**
 * The HubBrowserContextMenuViewModel is the implementation of the {@linkplain IHubBrowserContextMenuViewModel} for the Hub browsers context menu
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class HubBrowserContextMenuViewModel implements IHubBrowserContextMenuViewModel
{
    /**
     * The current class logger
     */
    protected final Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain IHubController}
     */
    private final IHubController hubController;

    /**
     * The {@linkplain IMapCommandService}
     */
    private final IMapCommandService mapCommandService;

    /**
     * Maps the top element towards the DST
     */
    @Override
    public void MapTopElement()
    {
        this.mapCommandService.MapTopElement(this.hubController.GetOpenIteration().getTopElement(), MappingDirection.FromHubToDst);
    }
    
    /**
     * Maps the selection towards the DST
     */
    @Override
    public void MapSelection()
    {
        this.mapCommandService.MapSelection(MappingDirection.FromHubToDst);
    }

    /**
     * Backing field for {@linkplain #CanMapTopElement()}
     */
    private ObservableValue<Boolean> canMapTopElement = new ObservableValue<>();
        
    /**
     * Gets an {@linkplain Observable} of {@linkplain Boolean} indicating whether the {@linkplain #MapTopElement()} can execute
     * 
     * @return an {@linkplain Observable} of {@linkplain Boolean}
     */
    @Override
    public Observable<Boolean> CanMapTopElement()
    {
        return this.canMapTopElement.Observable();
    }

    /**
     * Gets an {@linkplain Observable} of {@linkplain Boolean} indicating whether the {@linkplain #MapSelection()} can execute
     * 
     * @return an {@linkplain Observable} of {@linkplain Boolean}
     */
    @Override
    public Observable<Boolean> CanMapSelection()
    {
        return this.mapCommandService.CanExecuteObservable();
    }

    /**
     * Backing field for {@linkplain #SetBrowserType()}
     */
    private Class<? extends IObjectBrowserViewModel> browserType;

    /**
     * Sets the browser type with the specified {@linkplain Class} of {@linkplain IObjectBrowserViewModel}
     * 
     * @param browserType the {@linkplain Class} {@linkplain IObjectBrowserViewModel} that identifies the type of browser
     */
    @Override
    public void SetBrowserType(Class<? extends IObjectBrowserViewModel> implementingBrowser)
    {
        this.browserType = implementingBrowser;
    }

    /**
     * Initializes a new {@linkplain HubBrowserContextMenuViewModel}
     * 
     * @param mapCommandService the {@linkplain IMapCommandService}
     * @param hubController the {@linkplain IHubController}
     */
    public HubBrowserContextMenuViewModel(IMapCommandService mapCommandService, IHubController hubController)
    {
        this.hubController = hubController;
        this.mapCommandService = mapCommandService;        
        this.InitializeObservables();
    }
    
    /**
     * Initializes the {@linkplain Observable} used by this view model 
     */
    private void InitializeObservables()
    {
        this.UpdateCanMapTopElement();                
        this.mapCommandService.CanExecuteObservable().subscribe(x -> this.UpdateCanMapTopElement());
        this.hubController.GetSessionEventObservable().subscribe(x -> this.UpdateCanMapTopElement());
    }

    /**
     * Updates the {@linkplain #CanMapTopElement()} value
     */
    private void UpdateCanMapTopElement()
    {
        this.canMapTopElement.Value(this.mapCommandService.CanExecute() 
                && this.browserType == IElementDefinitionBrowserViewModel.class 
                && this.hubController.GetOpenIteration().getTopElement() != null);
    }
}
