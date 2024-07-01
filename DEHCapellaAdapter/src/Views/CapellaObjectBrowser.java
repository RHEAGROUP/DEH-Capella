/*
 * CapellaObjectBrowser.java
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
package Views;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;

import org.apache.commons.lang3.tuple.Pair;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;

import Annotations.ExludeFromCodeCoverageGeneratedReport;
import Renderers.CapellaObjectBrowserRenderDataProvider;
import ViewModels.CapellaObjectBrowser.Interfaces.ICapellaObjectBrowserViewModel;
import ViewModels.CapellaObjectBrowser.Rows.ElementRowViewModel;
import ViewModels.Interfaces.IImpactViewContextMenuViewModel;
import ViewModels.Interfaces.IViewModel;
import Views.ObjectBrowser.ObjectBrowserBase;

/**
 * The {@linkplain CapellaObjectBrowser} is view that display the Capella model tree
 */
@ExludeFromCodeCoverageGeneratedReport
@SuppressWarnings("serial")
public class CapellaObjectBrowser extends ObjectBrowserBase<ICapellaObjectBrowserViewModel, IImpactViewContextMenuViewModel>
{
    /**
     * This view attached {@linkplain IViewModel}
     */
    private ICapellaObjectBrowserViewModel dataContext;
    
    /**
     * Initializes a new {@linkplain MagicDrawObjectBrowser}
     */
    public CapellaObjectBrowser()
    {
        super();
        this.objectBrowserTree.setRenderDataProvider(new CapellaObjectBrowserRenderDataProvider());
        this.objectBrowserTree.setRootVisible(false);
    }

    /**
     * Sets the DataContext
     * 
     * @param viewModel the {@link IViewModel} to assign
     */
    @Override
    public void SetDataContext(ICapellaObjectBrowserViewModel viewModel)
    {
        this.dataContext = viewModel;
        this.Bind();
    }
    
    /**
     * Gets the DataContext
     * 
     * @return An {@link IViewModel}
     */
    @Override
    public ICapellaObjectBrowserViewModel GetDataContext()
    {
        return this.dataContext;
    }
    
    /**
     * Handles the selection when the user changes it
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void OnSelectionChanged()
    {
        int selectedRowIndex = objectBrowserTree.getSelectedRow();

        var row = Pair.of(selectedRowIndex, (ElementRowViewModel<? extends CapellaElement>) objectBrowserTree.getValueAt(selectedRowIndex, 0));

        if(!(row.getRight() instanceof ElementRowViewModel))
        {
            return;
        }
        
        dataContext.OnSelectionChanged((ElementRowViewModel<? extends CapellaElement>)row.getRight());

        SwingUtilities.invokeLater(() -> {
            objectBrowserTree.tableChanged(new TableModelEvent(objectBrowserTree.getOutlineModel(), row.getLeft()));
        });
    }
}
