/*
 * ElementRowViewModel.java
 *
 * Copyright (c) 2020-2021 RHEA System S.A.
 *
 * Author: Sam Geren�, Alex Vorobiev, Nathanael Smiechowski, Antoine Th�ate
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.cs.ComponentPkg;

import ViewModels.CapellaObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IRowViewModel;

/**
 * The {@linkplain ElementRowViewModel} is the row view model that represents any {@linkplain Class} in the {@linkplain MagicDrawObjectBrowser}
 * 
 * @param TElement the type of {@linkplain Element} this row view model represents
 */
public abstract class ElementRowViewModel<TElement extends CapellaElement> implements IElementRowViewModel<TElement>
{
    /**
     * The current class Logger
     */
    protected final Logger Logger = LogManager.getLogger();
    
    /**
     * The value indicating whether this row should be highlighted as "selected for transfer"
     */
    private boolean isSelected;
    
    /**
     * Switches between the two possible values for the {@linkplain isSelected}
     * 
     * @return the new {@linkplain boolean} value
     */
    @Override
    public boolean SwitchIsSelectedValue()
    {
        return this.isSelected = !this.isSelected;
    }
    
    /**
     * Sets a value whether this row is selected
     * 
     * @param isSelected the {@linkplain boolean} value
     */
    @Override
    public void SetIsSelected(boolean isSelected)
    {
        this.isSelected = isSelected;
    }
    
    /**
     * Gets a value indicating whether this row should be highlighted as "selected for transfer"
     * 
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean GetIsSelected()
    {
        return this.isSelected;
    }
    
    /**
     * The value indicating whether this row should be highlighted in the tree
     */
    private boolean isHighlighted;
    
    /**
     * Sets a value whether this row is highlighted
     * 
     * @param isHighlighted the {@linkplain boolean} value
     */
    @Override
    public void SetIsHighlighted(boolean isHighlighted)
    {
        this.isHighlighted = isHighlighted;
    }
    
    /**
     * Gets a value indicating whether this row should be highlighted in the tree
     * 
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean GetIsHighlighted()
    {
        return this.isHighlighted;
    }
    
    /**
     * The name of the {@linkplain Element} represented by this row view model
     */
    private TElement element;
    
    /**
     * Gets the name of the {@linkplain Element} represented by this row view model
     * 
     * @return the represented {@linkplain Element}
     */
    @Override
    public TElement GetElement()
    {
        return this.element;
    }

    /**
     * The name of the {@linkplain Element} represented by this row view model
     */
    private String name;
    
    /**
     * Gets the name of the {@linkplain Element} represented by this row view model
     * 
     * @return
     */
    public String GetName()
    {
        return this.name;
    }
    
    /**
     * Sets the name of this row view model
     * 
     * @param name the new name
     */
    protected void SetName(String name)
    {
        this.name = name;
    }

    
    /**
     * The {@linkplain IRowViewModel} parent of this row view model
     */
    private IRowViewModel parent;

    /**
     * Gets the parent row view model of the current row
     * 
     * @return an {@linkplain IRowViewModel}
     */
    @Override
    public IRowViewModel GetParent()
    {
        return this.parent;
    }

    /**
     * A value indicating whether the current row is expanded
     */
    private boolean isExpanded;
    
    /**
     * Gets a value indicating whether the current row is expanded
     * 
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean GetIsExpanded()
    {
        return this.isExpanded;
    }
    
    /**
     * Sets a value indicating whether the current row is expanded
     * 
     * @return a {@linkplain boolean}
     */
    @Override
    public void SetIsExpanded(boolean isExpanded)
    {
        this.isExpanded = isExpanded;
    }

    /**
     * Initializes a new {@linkplain ElementRowViewModel}
     * 
     * @param parent the {@linkplain IElementRowViewModel} parent view model of this row view model
     * @param element the {@linkplain TElement} {@linkplain Element} which is represented
     */
    public ElementRowViewModel(IElementRowViewModel<?> parent, TElement element)
    {
        this.element = element;
        this.parent = parent;
        this.UpdateProperties();
    }
    
    /**
     * Updates this view model properties
     * 
     * @param name the name of this row view model
     */ 
    protected void UpdateProperties(String name)
    {
        this.name = name;
    }
    
    /**
     * Updates this view model properties
     */ 
    public void UpdateProperties()
    {
        if(element != null)
        {
            if(this.element instanceof ComponentPkg)
            {
                this.name = ((ComponentPkg)this.element).getName();
            }
            else
            {
                this.name = element.toString();
            }
        }
    }
        
    /**
     * Gets the string representation of the type of thing represented
     * 
     * @return a {@linkplain Stereotypes}
     */
    @Override
    public Class<? extends CapellaElement> GetClassKind()
    {
        return this.GetElement().getClass();
    }
}
