/*
 * CapellaLocalExchangeHistoryService.java
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
package Services.HistoryService;

import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.information.Property;
import org.polarsys.capella.core.data.information.datavalue.DataValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralBooleanValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralNumericValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralStringValue;

import HubController.IHubController;
import Services.AdapterInfo.IAdapterInfoService;
import Services.LocalExchangeHistory.ILocalExchangeHistoryService;
import Services.LocalExchangeHistory.LocalExchangeHistoryService;
import Utils.Stereotypes.StereotypeUtils;
import cdp4common.ChangeKind;

/**
 * The {@linkplain CapellaLocalExchangeHistoryService} is the Capella Adpater implementation of {@linkplain ILocalExchangeHistoryService}
 */
public class CapellaLocalExchangeHistoryService extends LocalExchangeHistoryService implements ICapellaLocalExchangeHistoryService
{
    /**
     * Initializes a new {@linkplain CapellaLocalExchangeHistoryService}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param versionService the {@linkplain IAdapterVersionNumberService}
     */
    public CapellaLocalExchangeHistoryService(IHubController hubController, IAdapterInfoService versionService)
    {
        super(hubController, versionService);
    }

    /**
     * Appends a change in the log regarding the specified {@linkplain CapellaElement}
     * 
     * @param element the {@linkplain CapellaElement}
     * @param changeKind the {@linkplain ChangeKind}
     */
    @Override
    public void Append(NamedElement element, ChangeKind changeKind)
    {
        var modelCode = element.eContainer() instanceof NamedElement 
                ? String.format("%s.%s", ((NamedElement)element.eContainer()).getName(), element.getName())
                        : element.getName();

        var elementType = element.getClass().getSimpleName().replace("Impl", "");

        this.Append(modelCode, String.format("%s [%s] has been %sD", elementType, modelCode, changeKind));
    }

    /**
     * Appends a change in the log regarding the specified {@linkplain Property}s
     * 
     * @param clonedProperty the new {@linkplain Property}
     * @param originalProperty the old {@linkplain Property}
     */
    @Override
    public void Append(Property clonedProperty, Property originalProperty)
    {
        var valueToUpdateString = this.GetValueAsString(originalProperty.getOwnedDefaultValue());
        var newValueString = this.GetValueAsString(clonedProperty.getOwnedDefaultValue());
        
        var propertyName = String.format("%s.%s", 
                originalProperty.eContainer() instanceof NamedElement 
                    ? ((NamedElement) originalProperty.eContainer()).getName() 
                    : "", originalProperty.getName());
        
        this.Append(propertyName, String.format("Value: [%s] from Property [%s] has been updated to [%s]", valueToUpdateString, propertyName, newValueString));
    }

    /**
     * Gets the value as {@linkplain String}
     * 
     * @param value the {@linkplain DataValue}
     * @return a {@linkplain String}
     */
    private String GetValueAsString(DataValue value)
    {
        if(value instanceof LiteralNumericValue)
        {
            return StereotypeUtils.GetValueRepresentation((LiteralNumericValue)value);
        }
        else if(value instanceof LiteralBooleanValue)
        {
            return String.format("%s", ((LiteralBooleanValue)value).isValue());
        }
        else if(value instanceof LiteralStringValue)
        {
            return ((LiteralStringValue)value).getValue();
        }
        
        return "-";
    }    
}
