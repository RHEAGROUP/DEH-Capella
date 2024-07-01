/*
 * CapellaLocalExchangeHistoryServiceTestFixture.java
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

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.polarsys.capella.core.data.information.Property;
import org.polarsys.capella.core.data.information.datatype.BooleanType;
import org.polarsys.capella.core.data.information.datatype.NumericType;
import org.polarsys.capella.core.data.information.datavalue.LiteralBooleanValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralNumericValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralStringValue;
import org.polarsys.capella.core.data.pa.PhysicalComponent;

import HubController.IHubController;
import Services.AdapterInfo.IAdapterInfoService;
import Services.AdapterInfo.IAdapterInfoService;
import cdp4common.ChangeKind;
import cdp4common.sitedirectorydata.DomainOfExpertise;
import cdp4common.sitedirectorydata.Person;

class CapellaLocalExchangeHistoryServiceTestFixture
{
    private CapellaLocalExchangeHistoryService service;
    private IHubController hubController;
    private IAdapterInfoService adapterInfoService;
    
    @BeforeEach
    void Setup()
    {
        this.hubController = mock(IHubController.class);
        var person = new Person();
        when(this.hubController.GetActivePerson()).thenReturn(person);
        var domain = new DomainOfExpertise();
        when(this.hubController.GetCurrentDomainOfExpertise()).thenReturn(domain);
        this.adapterInfoService = mock(IAdapterInfoService.class);
        
        this.service = new CapellaLocalExchangeHistoryService(this.hubController, this.adapterInfoService);
    }
    
    @Test
    void VerifyAppend()
    {
        var element = mock(PhysicalComponent.class);
        when(element.getName()).thenReturn("element0");
        
        var property0 = mock(Property.class);
        var property1 = mock(Property.class);
        
        var literalNumericValue = mock(LiteralNumericValue.class);
        var numericType = mock(NumericType.class);
        when(literalNumericValue.getType()).thenReturn(numericType);
        when(literalNumericValue.getValue()).thenReturn("2");
        
        var literalBooleanValue = mock(LiteralBooleanValue.class);
        var booleanType = mock(BooleanType.class);
        when(literalBooleanValue.getType()).thenReturn(booleanType);
        when(literalBooleanValue.isValue()).thenReturn(true);
        
        var literalStringValue = mock(LiteralStringValue.class);
        var stringType = mock(NumericType.class);
        when(literalStringValue.getType()).thenReturn(stringType);
        when(literalStringValue.getValue()).thenReturn("value");
        
        when(property0.getOwnedDefaultValue()).thenReturn(literalNumericValue);
        when(property1.getOwnedDefaultValue()).thenReturn(literalNumericValue);
        assertDoesNotThrow(() -> this.service.Append(property0, property1));
        when(property0.getOwnedDefaultValue()).thenReturn(literalBooleanValue);
        when(property1.getOwnedDefaultValue()).thenReturn(literalBooleanValue);
        assertDoesNotThrow(() -> this.service.Append(property0, property1));
        when(property0.getOwnedDefaultValue()).thenReturn(literalStringValue);
        when(property1.getOwnedDefaultValue()).thenReturn(literalStringValue);
        assertDoesNotThrow(() -> this.service.Append(property0, property1));
        
        assertDoesNotThrow(() -> this.service.Append(element, ChangeKind.UPDATE));
    }
}
