/*
 * BlockArchitectureRowViewModel.java
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
package ViewModels.CapellaObjectBrowser.Rows;

import org.polarsys.capella.core.data.capellacore.Structure;
import org.polarsys.capella.core.data.capellamodeller.Folder;
import org.polarsys.capella.core.data.cs.BlockArchitecture;
import org.polarsys.capella.core.data.cs.ComponentArchitecture;
import org.polarsys.capella.core.data.cs.ComponentPkg;
import org.polarsys.capella.core.data.requirement.RequirementsPkg;

import ViewModels.CapellaObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IRowViewModel;

/**
 * The {@linkplain BlockArchitectureRowViewModel} is the row view model that represents {@linkplain ComponentArchitecture}
 */
public class BlockArchitectureRowViewModel extends ProjectStructuralElementRowViewModel<BlockArchitecture, Structure>
{
    /**
     * Initializes a new {@linkplain ComponentRowViewModel}
     * 
     * @param parent the {@linkplain IRowViewModel} parent of this view model
     * @param element the {@linkplain ComponentArchitecture} that this row view model represents
     */
    public BlockArchitectureRowViewModel(IElementRowViewModel<?> parent, BlockArchitecture element)
    {
        super(parent, element, Structure.class);
    }
    
    /**
     * Adds to the contained element the corresponding row view model representing the provided {@linkplain TContainedElement}
     * 
     * @param element the {@linkplain TContainedElement}
     */
    @Override
    protected void AddToContainedRows(Structure element)
    {
        if(element instanceof ComponentPkg)
        {    
            this.GetContainedRows().add(new ComponentPackageRowViewModel(this, (ComponentPkg)element));
        }
        else if(element instanceof RequirementsPkg)
        {
            this.GetContainedRows().add(new RequirementPackageRowViewModel(this, (RequirementsPkg)element));
        }
        else if(element instanceof Folder)
        {
            this.GetContainedRows().add(new RequirementPackageRowViewModel(this, (Folder)element));
        }
    }
}
