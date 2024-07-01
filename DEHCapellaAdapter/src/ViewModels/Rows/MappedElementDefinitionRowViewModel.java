/*
 * MappedElementDefinitionRowViewModel.java
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

import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.cs.Component;

import Enumerations.CapellaArchitecture;
import Enumerations.MappingDirection;
import Reactive.ObservableValue;
import ViewModels.Interfaces.IHaveTargetArchitecture;
import cdp4common.engineeringmodeldata.ElementBase;
import cdp4common.engineeringmodeldata.ElementDefinition;
import io.reactivex.Observable;

/**
 * The {@linkplain MappedElementDefinitionRowViewModel} is the row view model that represents a mapping between an {@linkplain ElementDefinition} and a {@linkplain Component}
 */
public class MappedElementDefinitionRowViewModel extends MappedElementRowViewModel<ElementBase, NamedElement> implements IHaveTargetArchitecture
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
     * Initializes a new {@linkplain MappedElementDefinitionRowViewModel}
     * 
     * @param thing the {@linkplain TThing} that is at one end of the mapping
     * @param dstElement the {@linkplain TDstElement} that is at the other end
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedElementDefinitionRowViewModel(ElementBase thing, NamedElement dstElement, MappingDirection mappingDirection)
    {
        super(thing, ElementBase.class, dstElement, mappingDirection);
    }

    /**
     * Initializes a new {@linkplain MappedElementDefinitionRowViewModel}
     * 
     * @param thing the {@linkplain TThing} that is at one end of the mapping
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedElementDefinitionRowViewModel(ElementBase thing, MappingDirection mappingDirection)
    {
        super(thing, ElementBase.class, null, mappingDirection);
    }

    /**
     * Initializes a new {@linkplain MappedElementDefinitionRowViewModel} with {@linkplain MappingDirection}.{@code FromDstToHub}
     * 
     * @param dstElement the {@linkplain TDstElement}
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedElementDefinitionRowViewModel(NamedElement dstElement, MappingDirection mappingDirection)
    {
        super(ElementBase.class, dstElement, mappingDirection);
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
                "Component", MappingDirection.FromHubToDst);
    }
    
    /**
     * Gets the string representation of the represented DST element
     * 
     * @return a {@linkplain String}
     */
    @Override
    public String GetHubElementRepresentation()
    {
        return this.GetHubElementRepresentation(ElementDefinition.class);
    }

    /**
     * Gets a value indicating whether this {@linkplain MappedElementDefinitionRowViewModel} represents a mapping between an element definition and a component
     * 
     * @return An boolean
     */
    public boolean DoesRepresentAnElementDefinitionComponentMapping()
    {
        return (this.GetMappingDirection() == MappingDirection.FromHubToDst && this.GetHubElement() instanceof ElementDefinition)
               || (this.GetMappingDirection() == MappingDirection.FromDstToHub && this.GetDstElement() instanceof Component);
    }
}
