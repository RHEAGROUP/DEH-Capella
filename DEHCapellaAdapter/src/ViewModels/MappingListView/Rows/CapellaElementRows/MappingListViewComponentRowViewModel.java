/*
 * MappingListViewComponentRowViewModel.java
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
package ViewModels.MappingListView.Rows.CapellaElementRows;

import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.information.Property;

import Utils.Stereotypes.StereotypeUtils;
import ViewModels.MappingListView.Rows.MappingListViewBaseRowViewModel;
import ViewModels.MappingListView.Rows.MappingListViewContainerBaseRowViewModel;
import cdp4common.commondata.ClassKind;

/**
 * The MappingListViewComponentRowViewModel is the row view model that represents one {@linkplain Component} in a {@linkplain MappingListView}
 */
public class MappingListViewComponentRowViewModel extends MappingListViewContainerBaseRowViewModel<Component>
{    
    /**
     * Initializes a new {@linkplain MappingListViewElementDefinitionRowViewModel}
     * 
     * @param component the represented {@linkplain Component}
     */
    public MappingListViewComponentRowViewModel(Component component)
    {
        super(component, component.getId(), component.getName(), null, ClassKind.ElementDefinition);
    }
    
    /**
     * Computes the contained rows
     */
    @Override
    public void ComputeContainedRows() 
    {
        for (Property property : this.element.getContainedProperties())
        {
            this.containedRows.add(new MappingListViewBaseRowViewModel(property.getId(),
                    property.getName(), StereotypeUtils.GetValueRepresentation(property.getOwnedDefaultValue()), ClassKind.Parameter));
        }
    }
}