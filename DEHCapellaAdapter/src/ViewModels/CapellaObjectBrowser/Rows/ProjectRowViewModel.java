/*
 * ResourceRowViewModel.java
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
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.capellamodeller.SystemEngineering;

import ViewModels.CapellaObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IRowViewModel;

/**
 * The {@linkplain ProjectRowViewModel} is the row view model that represents a {@linkplain Project}
 */
public class ProjectRowViewModel extends ProjectStructuralElementRowViewModel<Project, SystemEngineering>
{
    /**
     * Initializes a new {@linkplain ProjectRowViewModel}
     * 
     * @param parent the {@linkplain IRowViewModel} parent of this view model
     * @param element the {@linkplain Structure} that this row view model represents
     */
    public ProjectRowViewModel(IElementRowViewModel<?> parent, Project element)
    {
        super(parent, element, SystemEngineering.class);
    }
    
    /**
     * Adds to the contained element the corresponding row view model representing the provided {@linkplain TContainedElement}
     * 
     * @param element the {@linkplain TContainedElement}
     */
    @Override
    protected void AddToContainedRows(SystemEngineering element)
    {
        this.GetContainedRows().add(new SystemEngineeringRowViewModel(this, (SystemEngineering)element));
    }
}
