/*
 * CapellaImpactViewPanel.java
 *
 * Copyright (c) 2020-2021 RHEA System S.A.
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
package Views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import Annotations.ExludeFromCodeCoverageGeneratedReport;
import App.AppContainer;
import ViewModels.Interfaces.ICapellaImpactViewPanelViewModel;
import Views.ImpactViewPanel;
import Views.ContextMenu.ImpactViewContextMenu;
import Views.ViewParts.BaseViewPart;
import io.reactivex.Observable;

/**
 * The {@linkplain CapellaImpactViewPanel} is the main panel view for the Hub controls like session controls and tree views.
 * This view is meant to be integrated into another container view specific to DST * 
 */
@ExludeFromCodeCoverageGeneratedReport
public class CapellaImpactViewPanel extends BaseViewPart<ICapellaImpactViewPanelViewModel, ImpactViewPanel>
{
    /**
     * The {@linkplain ImpactViewContextMenu} context menu view for the Capella impact view
     */
    private ImpactViewContextMenu capellaContextMenu;
    
    /**
     * Initializes a new {@linkplain CapellaImpactViewPanel}
     */
    public CapellaImpactViewPanel()
    {
        super(ImpactViewPanel.class.getSimpleName());
        this.capellaContextMenu = new ImpactViewContextMenu();
    }
    
    /**
     * Creates the part control for this {@linkplain ViewPart}
     * 
     * @param parent the {@linkplain Composite} parent
     */
    @Override
    public void createPartControl(Composite parent)
    {
        this.CreateBasePartControl(parent);
        this.View = new ImpactViewPanel();
        this.SetDataContext(AppContainer.Container.getComponent(ICapellaImpactViewPanelViewModel.class));
        this.Container.add(this.View);
    }
    
    /**
     * Binds the <code>TViewModel viewModel</code> to the implementing view
     * 
     * @param <code>viewModel</code> the view model to bind
     */
    @Override
    public void Bind()
    {
       this.DataContext.GetIsSessionOpen().subscribe(x -> 
       {     
           this.View.SetSavedMappingconfigurationCollection(this.DataContext.GetSavedMappingconfigurationCollection());
       });

       Observable.combineLatest(Observable.fromArray(true), this.DataContext.GetIsSessionOpen(),
               (hasOneMagicDrawModelOpen, isHubSessionOpen) -> 
                   hasOneMagicDrawModelOpen && isHubSessionOpen)
           .subscribe(x -> this.View.SetLoadMappingControlsIsEnable(x));
       
       this.View.AttachOnSaveLoadMappingConfiguration(x -> this.DataContext.OnSaveLoadMappingConfiguration(x));
       
       this.View.AttachOnChangeDirection(this.DataContext.GetOnChangeMappingDirectionCallable());

       this.View.GetElementDefinitionBrowser().SetDataContext(this.DataContext.GetElementDefinitionImpactViewViewModel());
       this.View.GetRequirementBrowser().SetDataContext(this.DataContext.GetRequirementDefinitionImpactViewViewModel());
       this.capellaContextMenu.SetDataContext(this.DataContext.GetContextMenuViewModel());
       this.View.BindNumberOfSelectedThingToTransfer(this.DataContext.GetTransferControlViewModel().GetNumberOfSelectedThing());
       this.View.SetContextMenuDataContext(this.DataContext.GetContextMenuViewModel());
       this.View.AttachOnTransfer(this.DataContext.GetTransferControlViewModel().GetOnTransferCallable());
    }
}
