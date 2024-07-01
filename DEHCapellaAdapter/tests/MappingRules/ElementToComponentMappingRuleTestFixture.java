/*
 * ElementToComponentMappingRuleTestFixture.java
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
package MappingRules;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static Utils.Operators.Operators.AreTheseEquals;
import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.verification.Times;
import org.mockito.invocation.InvocationOnMock;
import org.polarsys.capella.core.data.capellacore.Feature;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.information.Property;
import org.polarsys.capella.core.data.information.Unit;
import org.polarsys.capella.core.data.information.datatype.DataType;
import org.polarsys.capella.core.data.information.datatype.PhysicalQuantity;
import org.polarsys.capella.core.data.information.datavalue.EnumerationLiteral;
import org.polarsys.capella.core.data.information.datavalue.LiteralNumericValue;
import org.polarsys.capella.core.data.information.datatype.Enumeration;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.pa.PhysicalComponent;

import DstController.IDstController;
import Enumerations.CapellaArchitecture;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.CapellaSession.ICapellaSessionService;
import Services.CapellaTransaction.ICapellaTransactionService;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Utils.Ref;
import Utils.Stereotypes.CapellaRequirementCollection;
import Utils.Stereotypes.HubElementCollection;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ElementUsage;
import cdp4common.engineeringmodeldata.InterfaceEndKind;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.Parameter;
import cdp4common.engineeringmodeldata.ParameterOverride;
import cdp4common.engineeringmodeldata.ParameterOverrideValueSet;
import cdp4common.engineeringmodeldata.ParameterSwitchKind;
import cdp4common.engineeringmodeldata.ParameterValueSet;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.BooleanParameterType;
import cdp4common.sitedirectorydata.Category;
import cdp4common.sitedirectorydata.DomainOfExpertise;
import cdp4common.sitedirectorydata.EnumerationParameterType;
import cdp4common.sitedirectorydata.EnumerationValueDefinition;
import cdp4common.sitedirectorydata.RatioScale;
import cdp4common.sitedirectorydata.SimpleQuantityKind;
import cdp4common.sitedirectorydata.SimpleUnit;
import cdp4common.sitedirectorydata.TextParameterType;
import cdp4common.types.ContainerList;
import cdp4common.types.ValueArray;

class ElementToComponentMappingRuleTestFixture
{
    private static final String literalEnumerationValue = "valueDefinition2";
    private IHubController hubController;
    private ICapellaSessionService sessionService;
    private ICapellaMappingConfigurationService mappingConfiguration;
    private ICapellaTransactionService transactionService;
    private ElementToComponentMappingRule mappingRule;
    private HubElementCollection elements;
    private IDstController dstController;
    private DomainOfExpertise domain;
    private Iteration iteration;
    private ElementDefinition elementDefinition1;
    private ElementDefinition elementDefinition0;
    private ElementDefinition elementDefinition2;
    private RatioScale scale;
    private SimpleQuantityKind quantityKind;
    private BooleanParameterType booleanParameterType;
    private ElementUsage elementUsage0;
    private ParameterOverride parameterOverride0;
    private SimpleUnit unit;
    private TextParameterType stringParameterType;
    private EnumerationParameterType enumParameterType;
    private BasicEList<EnumerationLiteral> literals;
    private ElementDefinition elementDefinition3;

    @BeforeEach
    public void Setup()
    {
        this.hubController = mock(IHubController.class);
        this.mappingConfiguration = mock(ICapellaMappingConfigurationService.class);
        this.sessionService = mock(ICapellaSessionService.class); 
        this.transactionService = mock(ICapellaTransactionService.class);
        this.dstController = mock(IDstController.class);
        
        when(this.transactionService.Create(any(Class.class), any(String.class)))
            .thenAnswer(x -> this.AnswerToTransactionServiceCreate(x));

        when(this.transactionService.Create(any(Class.class)))
            .thenAnswer(x -> this.AnswerToTransactionServiceCreate(x));
        
        this.mappingRule = new ElementToComponentMappingRule(this.hubController, this.mappingConfiguration, this.sessionService, this.transactionService);
        this.SetupElements();
        this.mappingRule.dstController = this.dstController;
    }
    
    private Object AnswerToTransactionServiceCreate(InvocationOnMock invocationData)
    {
        var type = invocationData.getArgument(0, Class.class);
        
        if(invocationData.getArguments().length == 2)
        {
            return MockElement(invocationData.getArgument(1, String.class), type);
        }
        
        return this.MockElement("", type);
    }

    private Object MockElement(String elementName, Class<? extends NamedElement> type)
    {
        var mock = mock(type);
        when(mock.getName()).thenReturn(elementName);
        
        if(Component.class.isAssignableFrom(type))
        {
            when(((Component)mock).getContainedProperties()).thenReturn(new BasicEList<Property>());
            when(((Component)mock).getOwnedFeatures()).thenReturn(new BasicEList<Feature>());
            when(mock.eContents()).thenReturn(new BasicEList<EObject>());
            
            if(PhysicalComponent.class.isAssignableFrom(type))
            {
                when(((PhysicalComponent)mock).getOwnedPhysicalComponents()).thenReturn(new BasicEList<PhysicalComponent>());
            }
            if(LogicalComponent.class.isAssignableFrom(type))
            {
                when(((LogicalComponent)mock).getOwnedLogicalComponents()).thenReturn(new BasicEList<LogicalComponent>());
            }
        }
        
        if(Enumeration.class.isAssignableFrom(type))
        {
            when(((Enumeration)mock).getOwnedLiterals()).thenReturn(literals);
        }
        
        if(Property.class.isAssignableFrom(type) && elementName.equals("enumParameterType"))
        {
            var enumerationType = mock(Enumeration.class);
            when(((Property)mock).getAbstractType()).thenReturn(enumerationType);
            when(enumerationType.getOwnedLiterals()).thenReturn(this.literals);
        }
        
        return mock;
    }

    @Test
    void VerifyTransform()
    {
        assertDoesNotThrow(() -> this.mappingRule.Transform(null));
        assertDoesNotThrow(() -> this.mappingRule.Transform(mock(List.class)));
        assertEquals(2, this.mappingRule.Transform(this.elements).size());

        when(this.dstController.TryGetElementBy(any(), any(Ref.class))).thenAnswer(x -> 
        {
            var refElement = x.getArgument(1, Ref.class);
            refElement.Set(this.MockElement("", PhysicalComponent.class));
            return true;
        });
        
        when(this.transactionService.Clone(any())).thenAnswer(x -> x.getArgument(0));
        
        assertEquals(2, this.mappingRule.Transform(this.elements).size());

        this.elements.clear();
        this.elements.add(new MappedElementDefinitionRowViewModel(elementDefinition2, null, MappingDirection.FromHubToDst));
        
        assertEquals(1, this.mappingRule.Transform(this.elements).size());
        
        verify(this.transactionService, times(39)).Create(any(Class.class));
        verify(this.transactionService, times(2)).AddReferenceDataToDataPackage(any(PhysicalQuantity.class));
        verify(this.transactionService, times(2)).AddReferenceDataToDataPackage(any(Unit.class));
    }
    
    private void SetupElements()
    {
        this.literals = new BasicEList<EnumerationLiteral>();
        var literal = mock(EnumerationLiteral.class);
        when(literal.getName()).thenReturn(literalEnumerationValue);
        this.literals.add(literal);
        
        this.elements = new HubElementCollection();
        
        this.domain = new DomainOfExpertise(UUID.randomUUID(), null, null);
        this.iteration = new Iteration(UUID.randomUUID(), null, null);
        
        this.scale = new RatioScale();
        this.scale.setName("scale");
        this.unit = new SimpleUnit();
        this.unit.setName("unit");
        this.unit.setShortName("unit");
        this.scale.setUnit(this.unit);
        
        this.quantityKind = new SimpleQuantityKind();
        this.quantityKind.setName("quantityKind");
        this.quantityKind.getPossibleScale().add(this.scale);
        this.quantityKind.setDefaultScale(this.scale);
        
        this.booleanParameterType = new BooleanParameterType();
        this.booleanParameterType.setName("booleanParameterType");
        
        this.stringParameterType = new TextParameterType();
        this.stringParameterType.setName("stringParameterType");

        this.enumParameterType = new EnumerationParameterType();
        this.enumParameterType.setName("enumParameterType");
        var definitions = new ArrayList<EnumerationValueDefinition>();
        var valueDefinition0 = new EnumerationValueDefinition();
        valueDefinition0.setName("valueDefinition0");
        valueDefinition0.setShortName("valueDefinition0");
        definitions.add(valueDefinition0);
        var valueDefinition1 = new EnumerationValueDefinition();
        valueDefinition1.setName("valueDefinition1");
        valueDefinition1.setShortName("valueDefinition1");
        definitions.add(valueDefinition1);
        var valueDefinition2 = new EnumerationValueDefinition();
        valueDefinition2.setName(literalEnumerationValue);
        valueDefinition2.setShortName(literalEnumerationValue);
        definitions.add(valueDefinition2);
        
        this.enumParameterType.getValueDefinition().addAll(definitions);
        
        var parameter0 = new Parameter();
        parameter0.setParameterType(this.quantityKind);
        parameter0.setOwner(this.domain);
        parameter0.setScale(this.scale);
        ParameterValueSet parameterValueSet0 = new ParameterValueSet();
        parameterValueSet0.setManual(new ValueArray(Arrays.asList("0.9883"), String.class));
        parameterValueSet0.setValueSwitch(ParameterSwitchKind.MANUAL);
        parameter0.getValueSet().add(parameterValueSet0);
        
        var parameter1 = new Parameter();
        parameter1.setParameterType(this.booleanParameterType);
        parameter1.setOwner(this.domain);
        ParameterValueSet parameterValueSet1 = new ParameterValueSet();
        parameterValueSet1.setManual(new ValueArray(Arrays.asList("true"), String.class));
        parameterValueSet1.setValueSwitch(ParameterSwitchKind.MANUAL);
        parameter1.getValueSet().add(parameterValueSet1);
        
        var parameter2 = new Parameter();
        parameter2.setParameterType(this.stringParameterType);
        parameter2.setOwner(this.domain);
        ParameterValueSet parameterValueSet2 = new ParameterValueSet();
        parameterValueSet2.setManual(new ValueArray(Arrays.asList("stringstring"), String.class));
        parameterValueSet2.setValueSwitch(ParameterSwitchKind.MANUAL);
        parameter2.getValueSet().add(parameterValueSet2);
        
        var parameter3 = new Parameter();
        parameter3.setParameterType(this.enumParameterType);
        parameter3.setOwner(this.domain);
        ParameterValueSet parameterValueSet3 = new ParameterValueSet();
        parameterValueSet3.setManual(new ValueArray(Arrays.asList(literalEnumerationValue), String.class));
        parameterValueSet3.setValueSwitch(ParameterSwitchKind.MANUAL);
        parameter3.getValueSet().add(parameterValueSet3);
        
        this.elementDefinition2 = new ElementDefinition();
        this.elementDefinition2.setName("elementDefinition2");
        this.elementDefinition2.setShortName("elementDefinition2");
        var category0 = new Category();
        category0.setName("category0");
        var category1 = new Category();
        category1.setName("category1");
        var category2 = new Category();
        category2.setName("Logical Component");
        this.elementDefinition2.getCategory().addAll(Arrays.asList(category0, category1, category2));
        
        this.elementDefinition0 = new ElementDefinition();
        this.elementDefinition0.setName("elementDefinition0");
        this.elementDefinition0.setShortName("elementDefinition0");
        this.elementDefinition1 = new ElementDefinition();
        this.elementDefinition1.setName("elementDefinition1");
        this.elementDefinition1.setShortName("elementDefinition1");

        this.elementDefinition3 = new ElementDefinition();
        this.elementDefinition3.setName("elementDefinition3");
        this.elementDefinition3.setShortName("elementDefinition3");
        this.elementUsage0 = new ElementUsage();
        this.elementUsage0.setName("elementUsage0");
        this.elementUsage0.setShortName("elementUsage0");
        this.elementUsage0.setElementDefinition(this.elementDefinition0);
        this.elementUsage0.setInterfaceEnd(InterfaceEndKind.NONE);
        
        this.parameterOverride0 = new ParameterOverride();
        this.parameterOverride0.setParameter(parameter0);
        ParameterOverrideValueSet parameterOverrideValueSet0 = new ParameterOverrideValueSet();
        parameterOverrideValueSet0.setParameterValueSet(parameterValueSet0);
        parameterOverrideValueSet0.setManual(new ValueArray(Arrays.asList("1.9585"), String.class));
        this.parameterOverride0.getValueSet().add(parameterOverrideValueSet0);

        this.elementDefinition0.getParameter().add(parameter0);
        this.elementDefinition0.getParameter().add(parameter1);
        this.elementDefinition0.getParameter().add(parameter2);
        this.elementDefinition2.getParameter().add(parameter3);
        this.elementDefinition1.getContainedElement().add(this.elementUsage0);
        
        this.iteration.getElement().add(this.elementDefinition0);
        this.iteration.getElement().add(this.elementDefinition1);
        
        var mappedElement0 = new MappedElementDefinitionRowViewModel(this.elementDefinition0, null, MappingDirection.FromHubToDst);
        mappedElement0.SetTargetArchitecture(CapellaArchitecture.PhysicalArchitecture);
        var mappedElement1 = new MappedElementDefinitionRowViewModel(elementDefinition1, null, MappingDirection.FromHubToDst);
        mappedElement1.SetTargetArchitecture(CapellaArchitecture.PhysicalArchitecture);
        
        this.elements.add(mappedElement0);
        this.elements.add(mappedElement1);
    }        
}
