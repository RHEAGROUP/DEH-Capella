/*
 * RootRowViewModel.java
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

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.Structure;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.capellamodeller.SystemEngineering;
import org.polarsys.capella.core.data.cs.BlockArchitecture;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.ComponentArchitecture;
import org.polarsys.capella.core.data.oa.OperationalAnalysis;
import org.polarsys.capella.core.data.pa.PhysicalArchitecture;

/**
 * The {@linkplain RootRowViewModel} represents the root element in one containment tree
 */
public class RootRowViewModel extends ProjectStructuralElementRowViewModel<SystemEngineering, CapellaElement>
{
    /**
     * Initializes a new {@linkplain RootRowViewModel}
     * 
     * @param name the name of this row
     */
    public RootRowViewModel(String name)
    {
        super(null, null, null);
        this.UpdateProperties(name);
    }
    
    /**
     * Initializes a new {@linkplain RootRowViewModel}
     * 
     * @param name the name of this row
     * @param elements the {@linkplain Collection} of {@linkplain Notifier}
     */
    public RootRowViewModel(String name, Collection<Notifier> elements)
    {
        super(null, null, CapellaElement.class);
        this.UpdateProperties(name, elements);
    }
    
    /**
     * Initializes a new {@linkplain RootRowViewModel}
     * 
     * @param name the name of this row
     * @param elements the {@linkplain Collection} of {@linkplain Notifier}
     */
    public RootRowViewModel(String name, List<EObject> elements)
    {
        super(null, null, CapellaElement.class);
        this.UpdateProperties(name, elements);
    }
    /**
     * Updates this view model properties
     * 
     * @param elements the children element that this row contains
     */
    protected void UpdateProperties(String name, List<EObject> elements)
    {
        super.UpdateProperties(name);
        
        for (var element : elements)
        {
            if(element instanceof Component)
            {
                this.GetContainedRows().add(new ComponentRowViewModel(null, (Component)element));
            }
            else if(element instanceof ComponentArchitecture)
            {
                this.GetContainedRows().add(new BlockArchitectureRowViewModel(null, (ComponentArchitecture) element));
            }
        }
    }
    
    /**
     * Updates this view model properties
     * 
     * @param elements the children element that this row contains
     */
    protected void UpdateProperties(String name, Collection<Notifier> elements)
    {
        super.UpdateProperties(name);
        
        var systemEngineering = elements.stream()
                .filter(x -> x instanceof Project)
                .map(x -> (Project)x)
                .flatMap(x -> x.eContents().stream())
                .filter(x -> x instanceof SystemEngineering)
                .map(x -> (SystemEngineering)x)
                .findFirst();
        
        if(systemEngineering.isPresent())
        {
            this.UpdateElement(systemEngineering.get(), false);
        }
    }


    /**
     * Adds to the contained element the corresponding row view model representing the provided {@linkplain TContainedElement}
     * 
     * @param element the {@linkplain TContainedElement}
     */
    @Override
    protected void AddToContainedRows(CapellaElement element)
    {
        if(element instanceof BlockArchitecture)
        {
            this.GetContainedRows().add(new BlockArchitectureRowViewModel(this, (BlockArchitecture)element));
        }
    }
}
