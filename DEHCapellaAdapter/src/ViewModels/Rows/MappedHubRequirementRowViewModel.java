/*
 * MappedHubRequirementRowViewModel.java
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
package ViewModels.Rows;

import org.polarsys.capella.core.data.requirement.Requirement;

import Enumerations.CapellaArchitecture;
import Enumerations.MappingDirection;
import Reactive.ObservableValue;
import ViewModels.Interfaces.IHaveTargetArchitecture;
import io.reactivex.Observable;

/**
 * The {@linkplain MappedHubRequirementRowViewModel} is the row view model that represents a mapping between 
 * a {@linkplain cdp4common.engineeringmodeldata.Requirement} and a {@linkplain Requirement}
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class MappedHubRequirementRowViewModel extends MappedRequirementBaseRowViewModel implements IHaveTargetArchitecture
{
    /**
     * Backing field for {@linkplain #GetTargetArchitecture()}, {@linkplain #SetTargetArchitecture(Object)} and {@linkplain #GetTargetArchitectureObservable()}
     */
    private ObservableValue<CapellaArchitecture> targetArchitecture = new ObservableValue<>(CapellaArchitecture.PhysicalArchitecture, CapellaArchitecture.class);
        
    /**
     * Gets the selected {@linkplain CapellaArchitecture}
     * 
     * @return the {@linkplain CapellaArchitecture}
     */
    @Override
    public CapellaArchitecture GetTargetArchitecture()
    {
        return this.targetArchitecture.Value();
    }

    /**
     * Sets the selected {@linkplain CapellaArchitecture}
     * 
     * @param value the new {@linkplain Object} value
     */
    @Override
    public void SetTargetArchitecture(Object value)
    {
        if(value instanceof CapellaArchitecture)
        {
            this.targetArchitecture.Value((CapellaArchitecture)value);
        }
    }
    
    /**
     * Gets the {@linkplain Observable} of {@linkplain CapellaArchitecture}
     * 
     * @return an {@linkplain Observable} of {@linkplain CapellaArchitecture}
     */
    @Override
    public Observable<CapellaArchitecture> GetTargetArchitectureObservable()
    {
        return this.targetArchitecture.Observable();
    }
    
    /**
     * Initializes a new {@linkplain MappedHubRequirementRowViewModel}
     * 
     * @param thing the {@linkplain TThing} that is at one end of the mapping
     * @param dstElement the {@linkplain TDstElement} that is at the other end
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedHubRequirementRowViewModel(cdp4common.engineeringmodeldata.Requirement thing, Requirement dstElement, MappingDirection mappingDirection)
    {
        super(thing, dstElement, mappingDirection);
    }

    /**
     * Initializes a new {@linkplain MappedHubRequirementRowViewModel} with {@linkplain MappingDirection}.{@code FromDstToHub}
     * 
     * @param dstElement the {@linkplain TDstElement} that is at one end of the mapping
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedHubRequirementRowViewModel(Requirement dstElement, MappingDirection mappingDirection)
    {
        super(dstElement, mappingDirection);
    }
    
    /**
     * Initializes a new {@linkplain MappedHubRequirementRowViewModel} with {@linkplain MappingDirection}.{@code FromHubToDst}
     * 
     * @param hubElement the {@linkplain Requirement} that is at one end of the mapping
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedHubRequirementRowViewModel(cdp4common.engineeringmodeldata.Requirement hubElement, MappingDirection mappingDirection)
    {
        super(hubElement, null, mappingDirection);
    }
}
