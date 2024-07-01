/*
 * PackageRowViewModel.java
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

import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.ComponentPkg;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.information.Property;
import org.polarsys.capella.core.data.capellacore.Relationship;

import Reactive.ObservableCollection;
import ViewModels.CapellaObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;
import ViewModels.ObjectBrowser.Interfaces.IRowViewModel;

/**
 * The {@linkplain ComponentRowViewModel} is is the row view model that represents a {@linkplain Component}
 */
public class ComponentRowViewModel extends ElementRowViewModel<Component> implements IHaveContainedRows<IElementRowViewModel<? extends CapellaElement>>
{
    /**
     * The {@linkplain ObservableCollection} of {@linkplain IElementRowViewModel}
     */
    private ObservableCollection<IElementRowViewModel<? extends CapellaElement>> containedRows = new ObservableCollection<IElementRowViewModel<? extends CapellaElement>>();
        
    /**
     * Gets the contained row the implementing view model has
     * 
     * @return An {@linkplain ObservableCollection} of {@linkplain IElementRowViewModel}
     */
    @Override
    public ObservableCollection<IElementRowViewModel<? extends CapellaElement>> GetContainedRows()
    {
        return this.containedRows;
    }
    
    /**
     * Initializes a new {@linkplain ComponentRowViewModel}
     * 
     * @param parent the {@linkplain IRowViewModel} parent of this view model
     * @param element the {@linkplain Component} represented in this row view model
     */
    public ComponentRowViewModel(IElementRowViewModel<?> parent, Component element)
    {
        super(parent, element);
        this.ComputeContainedRows();
    }
    
    /**
     * Computes the contained rows of this row view model
     */
    @Override
    public void ComputeContainedRows()
    {
        for (var element : this.GetElement().eContents())
        {
            if(element instanceof Part)
            {
                this.GetContainedRows().add(new PartRowViewModel(this, (Part)element));
            }
            else if(element instanceof Property)
            {
                this.GetContainedRows().add(new PropertyRowViewModel(this, (Property)element));
            }
            else if(element instanceof Component)
            {
                this.GetContainedRows().add(new ComponentRowViewModel(this, (Component)element));
            }
            else if(element instanceof ComponentPkg)
            {
                this.GetContainedRows().add(new ComponentPackageRowViewModel(this, (ComponentPkg)element));
            }
            else if(element instanceof Relationship)
            {
                this.GetContainedRows().add(new RelationshipRowViewModel(this, (Relationship)element));
            }
        }
    }
}
