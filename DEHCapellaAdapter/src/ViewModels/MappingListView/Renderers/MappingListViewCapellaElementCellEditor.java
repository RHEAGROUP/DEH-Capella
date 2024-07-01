/*
 * MappingListViewCapellaElementCellEditor.java
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
package ViewModels.MappingListView.Renderers;

import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.requirement.Requirement;

import ViewModels.MappingListView.Rows.CapellaElementRows.MappingListViewCapellaRequirementRowViewModel;
import ViewModels.MappingListView.Rows.CapellaElementRows.MappingListViewComponentRowViewModel;

/**
 * The {@linkplain MappingListViewCapellaElementCellEditor} is the {@linkplain DefaultTableCellEditor} for the 
 * {@linkplain MappedElementListView} where the represented element is an {@linkplain DefinedThing}
 */
@SuppressWarnings("serial")
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class MappingListViewCapellaElementCellEditor extends MappingListViewElementBaseCellEditor<NamedElement>
{
    /**
     * Initializes a new {@linkplain MappingListViewCapellaElementCellEditor}
     */
    public MappingListViewCapellaElementCellEditor()
    {
        super(x -> x instanceof Component ? new MappingListViewComponentRowViewModel((Component)x)
                : new MappingListViewCapellaRequirementRowViewModel((Requirement)x));
    }
}
