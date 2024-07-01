/*
 * CapellaMappingListView.java
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

import org.polarsys.capella.core.data.capellacore.NamedElement;

import ViewModels.MappingListView.Renderers.MappingListViewCapellaElementCellEditor;
import ViewModels.MappingListView.Renderers.MappingListViewCapellaElementCellRenderer;
import Views.MappingList.MappingListView;

/**
 * The {@linkplain CappelaMappingListView} is the {@linkplain MappingListView} implementation for the Capella adapter 
 */
@SuppressWarnings("serial")
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class CapellaMappingListView extends MappingListView
{
    /**
     * Initializes a new {@linkplain CapellaMappingListView}
     */
    public CapellaMappingListView()
    {
        super();
        this.objectBrowserTree.setDefaultRenderer(NamedElement.class, new MappingListViewCapellaElementCellRenderer());
        this.objectBrowserTree.setDefaultEditor(NamedElement.class, new MappingListViewCapellaElementCellEditor());
    }
}
