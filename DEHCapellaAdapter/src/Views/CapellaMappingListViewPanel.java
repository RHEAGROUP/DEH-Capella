/*
 * CapellaMappingListViewPanel.java
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
package Views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import Annotations.ExludeFromCodeCoverageGeneratedReport;
import App.AppContainer;
import ViewModels.MappingListView.Interfaces.IMappingListViewViewModel;
import Views.MappingList.MappingListView;
import Views.ViewParts.BaseViewPart;

/**
 * The {@linkplain CapellaMappingListViewPanel} is the main panel view for the Hub controls like session controls and tree views.
 * This view is meant to be integrated into another container view specific to DST
 */
@ExludeFromCodeCoverageGeneratedReport
public class CapellaMappingListViewPanel extends BaseViewPart<IMappingListViewViewModel, CapellaMappingListView>
{
    /**
     * Initializes a new {@linkplain CapellaHubBrowserPanel}
     */
    public CapellaMappingListViewPanel()
    {
        super(MappingListView.class.getSimpleName());
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
        this.View = new CapellaMappingListView();
        this.SetDataContext(AppContainer.Container.getComponent(IMappingListViewViewModel.class));
        this.Container.add(this.View);
    }

    /**
     * Binds the {@linkplain TViewModel} viewModel to the implementing view
     * 
     * @param viewModel the view model to bind
     */
    @Override
    public void Bind()
    {
        this.View.SetDataContext(this.DataContext);
    }
}
