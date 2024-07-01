/*
 * ElementRowViewModel.java
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

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.requirement.Requirement;

import ViewModels.CapellaObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;
import ViewModels.ObjectBrowser.Interfaces.IRowViewModel;

/**
 * The {@linkplain ElementRowViewModel} is the row view model that represents any {@linkplain CapellaElement} in the {@linkplain CapellaObjectBrowser}
 * 
 * @param <TElement> the type of {@linkplain CapellaElement} this row view model represents
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public abstract class ElementRowViewModel<TElement extends CapellaElement> implements IElementRowViewModel<TElement>
{    
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
     * @return a {@linkplain String}
     */
    @Override
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
     * @param element the {@linkplain #TElement} {@linkplain Element} which is represented
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
    protected void UpdateProperties()
    {
        if(element != null)
        {
            if(this.element instanceof Requirement && !StringUtils.isBlank(((Requirement)this.element).getRequirementId()))
            {
                var requirement = (Requirement)this.element;
                this.name = String.format("%s - %s", requirement.getRequirementId(), requirement.getName());
            }
            else if(this.element instanceof NamedElement)
            {
                this.name =  ((NamedElement)this.element).getName();
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

    /**
     * Updates the represented {@linkplain CapellaElement} with the specified one
     * 
     * @param capellaElement the new {@linkplain CapellaElement}
     * @param shouldHighlight a value indicating whether the highlight should be updated
     */
    @SuppressWarnings("unchecked")
    public void UpdateElement(CapellaElement capellaElement, boolean shouldHighlight)
    {
        this.element = (TElement)capellaElement;
        this.UpdateProperties();
        
        if(shouldHighlight)
        {
            this.isHighlighted = true;
        }
        
        if(this instanceof IHaveContainedRows)
        {
            var thisAsParent = (IHaveContainedRows<?>)this;
            
            thisAsParent.GetContainedRows().clear();
            thisAsParent.ComputeContainedRows();
        }
    }
}
