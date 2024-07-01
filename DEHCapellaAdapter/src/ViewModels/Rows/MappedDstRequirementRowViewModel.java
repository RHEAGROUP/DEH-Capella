/*
 * MappedDstRequirementRowViewModel.java
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
package ViewModels.Rows;

import org.polarsys.capella.core.data.requirement.Requirement;

import Enumerations.MappingDirection;
import cdp4common.engineeringmodeldata.RequirementsSpecification;

/**
 * The {@linkplain MappedDstRequirementRowViewModel} is the row view model that represents a mapping between 
 * a {@linkplain RequirementsSpecification} and a {@linkplain Requirement}
 */
public class MappedDstRequirementRowViewModel extends MappedRequirementBaseRowViewModel
{
    /**
     * Initializes a new {@linkplain MappedRequirementsSpecificationRowViewModel}
     * 
     * @param thing the {@linkplain TThing} that is at one end of the mapping
     * @param dstElement the {@linkplain TDstElement} that is at the other end
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedDstRequirementRowViewModel(cdp4common.engineeringmodeldata.Requirement thing, Requirement dstElement, MappingDirection mappingDirection)
    {
        super(thing, dstElement, mappingDirection);
    }

    /**
     * Initializes a new {@linkplain MappedRequirementsSpecificationRowViewModel} with {@linkplain MappingDirection}.{@code FromDstToHub}
     * 
     * @param dstElement the {@linkplain TDstElement} that is at the other end
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedDstRequirementRowViewModel(Requirement dstElement, MappingDirection mappingDirection)
    {
        super(dstElement, mappingDirection);
    }
}
