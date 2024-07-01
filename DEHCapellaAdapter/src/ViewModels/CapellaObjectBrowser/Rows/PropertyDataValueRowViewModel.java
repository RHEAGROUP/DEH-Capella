/*
 * PropertyValueRowViewModel.java
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

import org.polarsys.capella.core.data.information.datavalue.DataValue;
import org.polarsys.capella.core.data.information.datavalue.EnumerationLiteral;
import org.polarsys.capella.core.data.information.datavalue.LiteralBooleanValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralNumericValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralStringValue;

import Utils.Stereotypes.StereotypeUtils;
import ViewModels.CapellaObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IRowViewModel;

/**
 * The {@linkplain PropertyDataValueRowViewModel} is a row view model that represents a {@linkplain DataValue}
 */
public class PropertyDataValueRowViewModel extends PropertyValueBaseRowViewModel<DataValue>
{
    /**
     * Initializes a new {@linkplain ComponentRowViewModel}
     * 
     * @param parent the {@linkplain IRowViewModel} parent of this view model
     * @param element the {@linkplain DataValue} represented by this row view model
     * @param name the {@linkplain String} name of this row view model
     */
    public PropertyDataValueRowViewModel(IElementRowViewModel<?> parent, DataValue element, String name)
    {
        super(parent, element);
        this.SetName(name);
    }
    
    /**
     * Gets the value representation based on the represented {@linkplain TElement}
     * 
     * @return a {@linkplain String}
     */
    public String GetValueRepresentation()
    {
        return StereotypeUtils.GetValueRepresentation(this.GetElement());
    }
}
