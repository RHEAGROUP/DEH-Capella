/*
 * CapellaMappedElementListView.java
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

import javax.swing.SwingUtilities;

import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;

import Enumerations.CapellaArchitecture;
import Renderers.CapellaArchitectureCellEditor;
import Renderers.CapellaArchitectureCellRenderer;
import ViewModels.MappedElementListView.Interfaces.ICapellaMappedElementListViewViewModel;

/**
 * The CapellaMappedElementListView is the {@linkplain MappedElementListView}
 */
@SuppressWarnings("serial")
public class CapellaMappedElementListView extends MappedElementListView<NamedElement>
{    
    /**
     * Initializes a new {@linkplain CapellaMappedElementListView}
     */
    public CapellaMappedElementListView()
    {
        super();
        this.objectBrowserTree.setDefaultRenderer(CapellaArchitecture.class, new CapellaArchitectureCellRenderer());
        this.objectBrowserTree.setDefaultEditor(CapellaArchitecture.class, new CapellaArchitectureCellEditor());
    }

    /**
     * Binds the {@linkplain #TViewModel} to the implementing view
     * 
     * @param viewModel the view model to bind
     */
    @Override
    public void Bind()
    {
        super.Bind();
        this.HideTargetArchitectureColumn();
    }

    /**
     * Verifies if the target architecture column should be hidden, if so, hides it.
     */
    private void HideTargetArchitectureColumn()
    {
        if(this.GetDataContext() instanceof ICapellaMappedElementListViewViewModel &&
                !((ICapellaMappedElementListViewViewModel)this.GetDataContext()).GetShouldDisplayTargetArchitectureColumn())
        {
            SwingUtilities.invokeLater(() ->
                this.objectBrowserTree.getColumnModel().removeColumn(this.objectBrowserTree.getColumnModel().getColumn(3)));
        }
    }
}
