/*
 * ProjectStructuralElementRowViewModel.java
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
import org.polarsys.capella.core.data.capellacore.Structure;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.ComponentPkg;

import Reactive.ObservableCollection;
import ViewModels.CapellaObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;
import ViewModels.ObjectBrowser.Interfaces.IRowViewModel;

/**
 * The {@linkplain ProjectStructuralElementRowViewModel} is the base row view model of all structural row view model for the Capella model browser
 * 
 * @param <TElement> the type of {@linkplain CapellaElement} this row view model represents
 * @param <TContainedElement> the type of direct children of this represented {@linkplain CapellaElement}
 */
public abstract class ProjectStructuralElementRowViewModel<TElement extends Structure, TContainedElement extends CapellaElement> 
    extends ElementRowViewModel<TElement> implements IHaveContainedRows<IElementRowViewModel<?>>
{
    /**
     * The {@linkplain Class} of the contained element
     */
    private Class<TContainedElement> containedElementClazz;

    /**
     * The {@linkplain ObservableCollection} of {@linkplain IElementRowViewModel}
     */
    private ObservableCollection<IElementRowViewModel<?>> containedRows = new ObservableCollection<IElementRowViewModel<?>>();
        
    /**
     * Gets the contained row the implementing view model has
     * 
     * @return An {@linkplain ObservableCollection} of {@linkplain IElementRowViewModel}
     */
    @Override
    public ObservableCollection<IElementRowViewModel<?>> GetContainedRows()
    {
        return this.containedRows;
    }

    /**
     * Initializes a new {@linkplain ComponentRowViewModel}
     * 
     * @param parent the {@linkplain IRowViewModel} parent of this view model
     * @param element the {@linkplain #TElement} represented by this row view model
     * @param containedElementClazz the {@linkplain Class} of the contained element
     */
    protected ProjectStructuralElementRowViewModel(IElementRowViewModel<?> parent, TElement element, Class<TContainedElement> containedElementClazz)
    {
        super(parent, element);
        this.containedElementClazz = containedElementClazz;
        this.ComputeContainedRows();
    }
    
    /**
     * Computes the contained rows of this row view model
     */
    @SuppressWarnings("unchecked")
    @Override
    public void ComputeContainedRows() 
    {
        if(this.GetElement() == null)
        {
            return;
        }
        
        for (var element : this.GetElement().eContents())
        {
            if(this.containedElementClazz.isAssignableFrom(element.getClass()))
            {
                this.AddToContainedRows((TContainedElement)element);
            }
            else if(element instanceof ComponentPkg)
            {
                this.GetContainedRows().add(new ComponentPackageRowViewModel(this, (ComponentPkg)element));
            }
        }
    }
    
    /**
     * Adds to the contained element the corresponding row view model representing the provided {@linkplain #TContainedElement}
     * 
     * @param element the {@linkplain #TContainedElement}
     */
    protected abstract void AddToContainedRows(TContainedElement element);
}
