/*
 * MappingListViewCapellaRequirementRowViewModel.java
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

import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.requirement.Requirement;

import Reactive.ObservableCollection;
import ViewModels.MappingListView.Rows.MappingListViewBaseRowViewModel;
import ViewModels.MappingListView.Rows.MappingListViewContainerBaseRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;
import cdp4common.commondata.ClassKind;

/**
 * The {@linkplain MappingListViewCapellaElementRowViewModel} is the row view model that represents one {@linkplain CapellaElement} in a {@linkplain MappingListView}
 */
public class MappingListViewCapellaRequirementRowViewModel extends MappingListViewContainerBaseRowViewModel<Requirement>
{    
    /**
     * Initializes a new {@linkplain MappingListViewCapellaRequirementRowViewModel}
     * 
     * @param requirement the represented {@linkplain Requirement}
     */
    public MappingListViewCapellaRequirementRowViewModel(Requirement requirement)
    {
        super(requirement, requirement.getId(), String.format("%s-%s", requirement.getRequirementId(), 
                requirement.getName()), requirement.getDescription(), ClassKind.Requirement);
    }
    
    /**
     * Computes the contained rows
     */
    @Override
    public void ComputeContainedRows() { }
}
