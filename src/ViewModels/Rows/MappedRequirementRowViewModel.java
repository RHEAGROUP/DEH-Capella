/*
 * MappedRequirementRowViewModel.java
 *
 * Copyright (c) 2020-2021 RHEA System S.A.
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
package ViewModels.Rows;

import org.polarsys.capella.core.data.requirement.Requirement;

import Enumerations.MappingDirection;
import cdp4common.engineeringmodeldata.RequirementsSpecification;

/**
 * The {@linkplain MappedRequirementsSpecificationRowViewModel} is the row view model that represents a mapping between a {@linkplain RequirementsSpecification} and a 
 */
public class MappedRequirementRowViewModel extends MappedElementRowViewModel<RequirementsSpecification, Requirement>
{
    /**
     * Initializes a new {@linkplain MappedRequirementsSpecificationRowViewModel}
     * 
     * @param thing the {@linkplain TThing} that is at one end of the mapping
     * @param dstElement the {@linkplain TDstElement} that is at the other end
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedRequirementRowViewModel(RequirementsSpecification thing, Requirement dstElement, MappingDirection mappingDirection)
    {
        super(thing, RequirementsSpecification.class, dstElement, mappingDirection);
    }

    /**
     * Initializes a new {@linkplain MappedRequirementsSpecificationRowViewModel} with {@linkplain MappingDirection}.{@code FromDstToHub}
     * 
     * @param dstElement the {@linkplain TDstElement} that is at the other end
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedRequirementRowViewModel(Requirement dstElement, MappingDirection mappingDirection)
    {
        super(RequirementsSpecification.class, dstElement, mappingDirection);
    }
    
    /**
     * Gets the string representation of the represented DST element    
     * 
     * @return a {@linkplain String}
     */
    @Override
    public String GetDstElementRepresentation()
    {
        return this.GetElementRepresentation(this.GetDstElement() == null ? "-" : this.GetDstElement().getName(),
                "Requirement", MappingDirection.FromHubToDst);
    }
    
    /**
     * Gets the string representation of the represented DST element
     * 
     * @return a {@linkplain String}
     */
    @Override
    public String GetHubElementRepresentation()
    {
        return this.GetHubElementRepresentation(RequirementsSpecification.class);
    }
}
