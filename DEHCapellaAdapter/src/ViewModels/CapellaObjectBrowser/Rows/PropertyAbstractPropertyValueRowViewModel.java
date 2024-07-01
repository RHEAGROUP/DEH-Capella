/*
 * PropertyAbstractPropertyValueRowViewModel.java
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

import org.polarsys.capella.core.data.capellacore.AbstractPropertyValue;
import org.polarsys.capella.core.data.capellacore.BooleanPropertyValue;
import org.polarsys.capella.core.data.capellacore.FloatPropertyValue;
import org.polarsys.capella.core.data.capellacore.IntegerPropertyValue;
import org.polarsys.capella.core.data.capellacore.StringPropertyValue;
import org.polarsys.capella.core.data.information.datavalue.DataValue;

import ViewModels.CapellaObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IRowViewModel;

/**
 * The {@linkplain PropertyAbstractPropertyValueRowViewModel} is a row view model that represents a {@linkplain DataValue}
 */
public class PropertyAbstractPropertyValueRowViewModel extends PropertyValueBaseRowViewModel<AbstractPropertyValue>
{
    /**
     * Initializes a new {@linkplain ComponentRowViewModel}
     * 
     * @param parent the {@linkplain IRowViewModel} parent of this view model
     * @param element the {@linkplain AbstractPropertyValue} represented by this row view model
     */
    public PropertyAbstractPropertyValueRowViewModel(IElementRowViewModel<?> parent, AbstractPropertyValue element)
    {
        super(parent, element);
    }

    /**
     * Gets the value representation based on the represented {@linkplain AbstractPropertyValue}
     * 
     * @return a {@linkplain String}
     */
    @Override
    public String GetValueRepresentation()
    {
        if(this.GetElement() instanceof BooleanPropertyValue)
        {
            return String.valueOf(((BooleanPropertyValue)this.GetElement()).isValue());
        }
        else if(this.GetElement() instanceof FloatPropertyValue)
        {
            return String.valueOf(((FloatPropertyValue)this.GetElement()).getValue());
        }
        else if(this.GetElement() instanceof IntegerPropertyValue)
        {
            return String.valueOf(((IntegerPropertyValue)this.GetElement()).getValue());
        }
        else if(this.GetElement() instanceof StringPropertyValue)
        {
            return ((StringPropertyValue)this.GetElement()).getValue();
        }

        return "";
    }
}
