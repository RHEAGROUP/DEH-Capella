/*
 * CapellaMappingListViewViewModelTestFixture.java
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
package ViewModels.MappingListView;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.eclipse.emf.common.util.BasicEList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.information.Property;
import org.polarsys.capella.core.data.information.Unit;
import org.polarsys.capella.core.data.information.datatype.BooleanType;
import org.polarsys.capella.core.data.information.datatype.NumericType;
import org.polarsys.capella.core.data.information.datatype.StringType;
import org.polarsys.capella.core.data.information.datavalue.LiteralBooleanValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralNumericValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralStringValue;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.data.requirement.Requirement;

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Reactive.ObservableCollection;
import Services.CapellaTransaction.ICapellaTransactionService;
import ViewModels.Rows.MappedDstRequirementRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.engineeringmodeldata.ElementDefinition;

class CapellaMappingListViewViewModelTestFixture
{
    private IDstController dstController;
    private ICapellaTransactionService transactionService;
    private IHubController hubController;
    private CapellaMappingListViewViewModel viewModel;
    private Component physicalComponent0;
    private PhysicalComponent component1;
    private Requirement capellaRequirement0;

    @BeforeEach
    public void Setup()
    {
        this.dstController = mock(IDstController.class);
        this.hubController = mock(IHubController.class);
        this.transactionService = mock(ICapellaTransactionService.class);
        
        when(this.hubController.GetIsSessionOpen()).thenReturn(true);
        when(this.dstController.GetDstMapResult()).thenReturn(new ObservableCollection<>());
        when(this.dstController.GetHubMapResult()).thenReturn(new ObservableCollection<>());
        
        this.viewModel = new CapellaMappingListViewViewModel(this.dstController, this.hubController, this.transactionService);
    }
    
    @Test
    public void VerifyBuildTree()
    {
        assertDoesNotThrow(() -> this.viewModel.UpdateBrowserTrees(true));
        assertNotNull(this.viewModel.GetBrowserTreeModel());

        this.component1 = mock(PhysicalComponent.class);
        when(this.component1.getName()).thenReturn("component1");
        when(this.component1.isAbstract()).thenReturn(true);
        when(this.component1.isHuman()).thenReturn(true);
        when(this.component1.eContents()).thenReturn(new BasicEList());
        var properties = new BasicEList<Property>();
        
        var property0 = mock(Property.class);
        when(property0.getName()).thenReturn("Property0");
        when(property0.getType()).thenReturn(mock(StringType.class));
        var dataValue0 = mock(LiteralStringValue.class);
        when(dataValue0.getValue()).thenReturn("label");
        when(property0.getOwnedDefaultValue()).thenReturn(dataValue0);
        
        var property1 = mock(Property.class);
        when(property1.getName()).thenReturn("Property1");
        when(property1.getType()).thenReturn(mock(NumericType.class));
        var dataValue1 = mock(LiteralNumericValue.class);
        when(dataValue1.getValue()).thenReturn("53");
        var unit = mock(Unit.class);        
        when(unit.getName()).thenReturn("kg2");
        when(dataValue1.getUnit()).thenReturn(unit);
        when(property1.getOwnedDefaultValue()).thenReturn(dataValue1);
        
        var property2 = mock(Property.class);
        when(property2.getName()).thenReturn("Property2");
        when(property2.getType()).thenReturn(mock(BooleanType.class));
        var dataValue2 = mock(LiteralBooleanValue.class);
        when(dataValue2.isValue()).thenReturn(true);
        when(property2.getOwnedDefaultValue()).thenReturn(dataValue2);
        
        properties.add(property0);
        properties.add(property1);
        properties.add(property2);

        when(this.component1.getContainedProperties()).thenReturn(properties);

        this.capellaRequirement0 = mock(Requirement.class);
        when(this.capellaRequirement0.getName()).thenReturn("capellaRequirement0");
        
        var mappedDstElements = new ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>>();
        MappedElementRowViewModel<? extends DefinedThing, ? extends NamedElement> mappedElementDefinitionRowViewModel = new MappedElementDefinitionRowViewModel(new ElementDefinition(), this.physicalComponent0, MappingDirection.FromDstToHub);
        mappedDstElements.add((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedElementDefinitionRowViewModel);
        MappedElementRowViewModel<? extends DefinedThing, ? extends NamedElement> mappedDstRequirementRowViewModel = new MappedDstRequirementRowViewModel(new cdp4common.engineeringmodeldata.Requirement(), this.capellaRequirement0, MappingDirection.FromDstToHub);
        mappedDstElements.add((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedDstRequirementRowViewModel);
        
        var mappedHubElements = new ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>>();
        MappedElementRowViewModel<? extends DefinedThing, ? extends NamedElement> mappedElementDefinitionRowViewModel2 = new MappedElementDefinitionRowViewModel(new ElementDefinition(), this.physicalComponent0, MappingDirection.FromHubToDst);
        mappedHubElements.add((MappedElementRowViewModel<DefinedThing, NamedElement>) mappedElementDefinitionRowViewModel2);
        
        when(this.dstController.GetDstMapResult()).thenReturn(mappedDstElements);
        when(this.dstController.GetHubMapResult()).thenReturn(mappedHubElements);

        assertDoesNotThrow(() -> this.viewModel.UpdateBrowserTrees(true));
        assertNotNull(this.viewModel.GetBrowserTreeModel());
    }
}
