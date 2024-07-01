/*
 * CapellaObjectBrowserTreeRowViewModel.java
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
package ViewModels.CapellaObjectBrowser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.netbeans.swing.outline.RowModel;

import ViewModels.CapellaObjectBrowser.Rows.ElementRowViewModel;
import ViewModels.CapellaObjectBrowser.Rows.PropertyValueBaseRowViewModel;
import ViewModels.CapellaObjectBrowser.Rows.RequirementRowViewModel;
import ViewModels.CapellaObjectBrowser.Rows.RootRowViewModel;
import Views.CapellaObjectBrowser;

/**
 * The {@linkplain CapellaObjectBrowserTreeRowViewModel} is the {@linkplain RowModel} implementation for the {@linkplain CapellaObjectBrowser}
 */
public class CapellaObjectBrowserTreeRowViewModel implements RowModel
{
    /**
     * The current class logger
     */
    private final Logger logger = LogManager.getLogger();
    
    /**
     * Gets column count for this tree grid needed to generate all the specified columns and also to compute rows values 
     * 
     * @return the total number of column
     */
    @Override
    public int getColumnCount()
    {
        return 2;
    }

    /**
     * Gets the value for the provided {@linkplain column} and {@linkplain rowViewModel}
     * 
     * @param rowViewModel the row
     * @param column the column
     * @return an {@linkplain Object} holding the value
     */
    @Override
    public Object getValueFor(Object rowViewModel, int column)
    {
        if(rowViewModel instanceof RootRowViewModel)
        {
            switch (column)
            {
                default: return "";
            }
        }
        
        if(rowViewModel instanceof ElementRowViewModel)
        {
            switch (column)
            {
                case 0 : 
                {
                    if(rowViewModel instanceof PropertyValueBaseRowViewModel)
                    {
                        return ((PropertyValueBaseRowViewModel<?>)rowViewModel).GetValueRepresentation();
                    }
                    else if(rowViewModel instanceof RequirementRowViewModel)
                    {
                        return ((RequirementRowViewModel)rowViewModel).GetDescription();
                    }
                    
                    return "";
                }
                                
                case 1 : return ((ElementRowViewModel<?>) rowViewModel).GetClassKind().getSimpleName().replace("Impl", "");
                default : return "-";
            }
        }
        
        return "-";
    }

    /**
     * Gets the column {@linkplain Class} for the specified {@linkplain column}
     * 
     * @param column the column index
     * @return the {@linkplain Class}/ type of value the column holds   
     */
    @Override
    public Class<?> getColumnClass(int column)
    {
        switch (column)
        {
            case 0 : return String.class;
            case 1 : return String.class;
            default : return null;
        }
    }

    /**
     * Gets a value indicating whether the specified (by the provided {@linkplain node} and {@linkplain column}) cell is editable
     * 
     * @param node the row view model
     * @param column the column index
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean isCellEditable(Object node, int column)
    {
        return false;
    }

    /**
     * Sets the value provided by {@linkplain value} to the node view model, typically it should call a setter on the row view model
     * 
     * @param node the row view model
     * @param column the column index
     */
    @Override
    public void setValueFor(Object node, int column, Object value) { }

    /**
     * Gets the column name based on its index
     * 
     * @param column the column index
     * @return a {@linkplain String} holding the column name
     */
    @Override
    public String getColumnName(int column)
    {
        switch (column) 
        {
            case 0 : return "Value";
            case 1 : return "Row Type";
            default : return null;
        }
    }
}
