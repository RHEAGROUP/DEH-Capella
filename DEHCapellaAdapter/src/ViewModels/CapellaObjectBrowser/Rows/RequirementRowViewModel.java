/*
 * RequirementRowViewModel.java
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

import Utils.Stereotypes.CapellaTypeEnumerationUtility;
import org.polarsys.capella.core.data.requirement.Requirement;
import Utils.Stereotypes.RequirementType;
import ViewModels.CapellaObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IRowViewModel;

/**
 * The {@linkplain RequirementRowViewModel} is the base row view model that represents a {@linkplain Requirement}
 */
public class RequirementRowViewModel extends ElementRowViewModel<Requirement>
{    
    /**
     * Initializes a new {@linkplain ComponentRowViewModel}
     * 
     * @param parent the {@linkplain IRowViewModel} parent of this view model
     * @param element the {@linkplain TRequirement} represented by this row view model
     */
    public RequirementRowViewModel(IElementRowViewModel<?> parent, Requirement element)
    {
        super(parent, element);
    }

    /**
     * Gets the description for the represented {@linkplain Requirement}
     * 
     * @return a {@linkplain String}
     */
    public String GetDescription()
    {
        return this.GetElement().getDescription();
    }
    
    /**
     * Gets the {@linkplain RequirementType} matching the {@linkplain TRequirement}
     * 
     * @return the {@linkplain RequirementType}
     */
    public RequirementType GetRequirementType()
    {
        return CapellaTypeEnumerationUtility.From(this.GetElement().getClass());
    }
}
