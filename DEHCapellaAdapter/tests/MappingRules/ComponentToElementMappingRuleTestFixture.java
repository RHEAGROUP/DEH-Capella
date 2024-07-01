/*
 * ComponentToElementMappingRuleTestFixture.java
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
package MappingRules;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.polarsys.capella.core.data.cs.Interface;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.OrientationPortKind;
import org.polarsys.capella.core.data.information.Property;
import org.polarsys.capella.core.data.information.Unit;
import org.polarsys.capella.core.data.information.datatype.BooleanType;
import org.polarsys.capella.core.data.information.datatype.NumericType;
import org.polarsys.capella.core.data.information.datatype.StringType;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.data.information.datavalue.DataValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralBooleanValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralNumericValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralStringValue;
import org.polarsys.capella.core.data.information.datavalue.NumericValue;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Services.MappingConfiguration.IMappingConfigurationService;
import Utils.Ref;
import Utils.Stereotypes.CapellaComponentCollection;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.EngineeringModel;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.sitedirectorydata.Category;
import cdp4common.sitedirectorydata.DomainOfExpertise;
import cdp4common.sitedirectorydata.EngineeringModelSetup;
import cdp4common.sitedirectorydata.MeasurementScale;
import cdp4common.sitedirectorydata.MeasurementUnit;
import cdp4common.sitedirectorydata.ModelReferenceDataLibrary;
import cdp4common.sitedirectorydata.RatioScale;
import cdp4common.sitedirectorydata.ScalarParameterType;
import cdp4common.sitedirectorydata.SimpleUnit;
import cdp4common.sitedirectorydata.SiteDirectory;
import cdp4common.sitedirectorydata.SiteReferenceDataLibrary;
import cdp4common.sitedirectorydata.TextParameterType;

public class ComponentToElementMappingRuleTestFixture
{
    private ICapellaMappingConfigurationService mappingConfigurationService;
    private IHubController hubController;
    private ComponentToElementMappingRule mappingRule;
    private CapellaComponentCollection elements;
    private ElementDefinition elementDefinition0;
    private ElementDefinition elementDefinition1;
    private DomainOfExpertise domain;
    private Iteration iteration;
    private PhysicalComponent component0;
    private PhysicalComponent component1;
    private PhysicalComponent component2;
    private ElementDefinition elementDefinition2;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        this.hubController = mock(IHubController.class);
        this.mappingConfigurationService = mock(ICapellaMappingConfigurationService.class);
        
        this.SetupElements();
        
        when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
        var modelReferenceDataLibrary = new ModelReferenceDataLibrary();
        var siteReferenceDataLibrary = new SiteReferenceDataLibrary();
        var siteDirectory = new SiteDirectory();
        var engineeringModelSetup = new EngineeringModelSetup();
        var engineeringModel = new EngineeringModel();
        
        modelReferenceDataLibrary.setRequiredRdl(siteReferenceDataLibrary);
        siteDirectory.getModel().add(engineeringModelSetup);
        
        siteDirectory.getSiteReferenceDataLibrary().add(siteReferenceDataLibrary);
        engineeringModelSetup.getRequiredRdl().add(modelReferenceDataLibrary);

        engineeringModel.setEngineeringModelSetup(engineeringModelSetup);
        engineeringModel.getIteration().add(this.iteration);
        
        when(this.hubController.GetDehpOrModelReferenceDataLibrary()).thenReturn(modelReferenceDataLibrary);
        when(this.hubController.TryGetThingFromChainOfRdlBy(any(Predicate.class), any(Ref.class))).thenReturn(false);

        this.mappingRule = new ComponentToElementMappingRule(this.hubController, this.mappingConfigurationService);
    }

    private void SetupElements()
    {
        this.domain = new DomainOfExpertise(UUID.randomUUID(), null, null);
        
        this.iteration = new Iteration(UUID.randomUUID(), null, null);
        
        this.elementDefinition0 = new ElementDefinition(UUID.randomUUID(), null, null);
        this.elementDefinition0.setOwner(this.domain);
        this.elementDefinition0.setName("elementDefinition0");
        this.elementDefinition0.setShortName("elementDefinition0");
        
        this.elementDefinition1 = new ElementDefinition(UUID.randomUUID(), null, null);
        this.elementDefinition1.setOwner(this.domain);
        this.elementDefinition1.setName("elementDefinition1");
        this.elementDefinition1.setShortName("elementDefinition1");
        
        this.elementDefinition2 = new ElementDefinition(UUID.randomUUID(), null, null);
        this.elementDefinition2.setOwner(this.domain);
        this.elementDefinition2.setName("element");
        this.elementDefinition2.setShortName("element");
        
        this.iteration.getElement().add(this.elementDefinition0);
        this.iteration.getElement().add(this.elementDefinition1);
        this.iteration.getElement().add(this.elementDefinition2);
        
        this.elements = new CapellaComponentCollection();
        
        this.component0 = mock(PhysicalComponent.class);
        when(this.component0.getName()).thenReturn("component0");
        when(this.component0.isActor()).thenReturn(true);
        this.component1 = mock(PhysicalComponent.class);
        when(this.component1.getName()).thenReturn("component1");
        when(this.component1.isAbstract()).thenReturn(true);
        when(this.component1.isHuman()).thenReturn(true);
        when(this.component1.eContents()).thenReturn(new BasicEList());
        this.component2 = mock(PhysicalComponent.class);
        when(this.component2.getName()).thenReturn("element");
        var component3 = mock(PhysicalComponent.class);
        when(component3.getName()).thenReturn("component3");
        when(component3.eContents()).thenReturn(new BasicEList());
        when(component3.getContainedProperties()).thenReturn(new BasicEList());
        when(this.component2.eContents()).thenReturn(new BasicEList(Arrays.asList(component3)));
        
        when(this.component0.eContents()).thenReturn(new BasicEList(Arrays.asList(this.component1, this.component2)));
        
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
        Unit unit = mock(Unit.class);        
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
        
        when(this.component0.getContainedProperties()).thenReturn(new BasicEList<Property>());
        when(this.component1.getContainedProperties()).thenReturn(properties);
        when(this.component2.getContainedProperties()).thenReturn(properties);
        
        var interface0 = mock(Interface.class);
        when(interface0.getName()).thenReturn("interface0");
        var port0 = mock(ComponentPort.class);
        when(port0.getName()).thenReturn("port0");
        when(port0.getOrientation()).thenReturn(OrientationPortKind.IN);
        when(port0.getProvidedInterfaces()).thenReturn(new BasicEList<Interface>(Arrays.asList(interface0)));
        when(port0.getRequiredInterfaces()).thenReturn(new BasicEList<Interface>());
        var port1 = mock(ComponentPort.class);
        when(port1.getOrientation()).thenReturn(OrientationPortKind.OUT);
        when(port1.getRequiredInterfaces()).thenReturn(new BasicEList<Interface>(Arrays.asList(interface0)));
        when(port1.getProvidedInterfaces()).thenReturn(new BasicEList<Interface>());
        var port2 = mock(ComponentPort.class);
        when(port2.getOrientation()).thenReturn(OrientationPortKind.INOUT);
        when(port2.getRequiredInterfaces()).thenReturn(new BasicEList<Interface>(Arrays.asList(interface0, interface0)));
        when(port2.getProvidedInterfaces()).thenReturn(new BasicEList<Interface>());
        var port3 = mock(ComponentPort.class);
        when(port3.getName()).thenReturn("port3");
        when(port3.getOrientation()).thenReturn(OrientationPortKind.UNSET);
        when(port3.getRequiredInterfaces()).thenReturn(new BasicEList<Interface>(Arrays.asList(interface0)));
        when(port3.getProvidedInterfaces()).thenReturn(new BasicEList<Interface>());
        
        when(this.component0.getContainedComponentPorts()).thenReturn(new BasicEList<ComponentPort>(Arrays.asList(port0)));
        when(this.component1.getContainedComponentPorts()).thenReturn(new BasicEList<ComponentPort>(Arrays.asList(port1)));
        when(this.component2.getContainedComponentPorts()).thenReturn(new BasicEList<ComponentPort>(Arrays.asList(port2, port3)));
        
        this.elements.add(new MappedElementDefinitionRowViewModel(this.elementDefinition0, this.component0, MappingDirection.FromDstToHub));
        this.elements.add(new MappedElementDefinitionRowViewModel(this.elementDefinition1, this.component1, MappingDirection.FromDstToHub));
        this.elements.add(new MappedElementDefinitionRowViewModel(this.component2, MappingDirection.FromDstToHub));
    }

    @Test
    public void VerifyTransform()
    {
        assertDoesNotThrow(() -> this.mappingRule.Transform(null));
        assertDoesNotThrow(() -> this.mappingRule.Transform(mock(List.class)));
        assertDoesNotThrow(() -> this.mappingRule.Transform(this.elements));
        
        when(this.hubController.TryGetThingFromChainOfRdlBy(any(Predicate.class), any(Ref.class)))
        .thenAnswer(new Answer<Boolean>() 
        {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable 
            {
                var arguments = invocation.getArguments();
                
                var ref = ((Ref<?>)arguments[1]);
                
                if(ref.GetType().isAssignableFrom(MeasurementScale.class))
                {
                    ((Ref<MeasurementScale>)ref).Set(new RatioScale());
                }
                if(ref.GetType().isAssignableFrom(Category.class))
                {
                    ((Ref<Category>)ref).Set(new Category());
                }
                else if(ref.GetType().isAssignableFrom(MeasurementUnit.class))
                {
                    ((Ref<MeasurementUnit>)ref).Set(new SimpleUnit());
                }
                else if(ref.GetType().isAssignableFrom(ScalarParameterType.class))
                {
                    TextParameterType parameterType = new TextParameterType();
                    parameterType.setShortName("Property0");
                    ((Ref<ScalarParameterType>)ref).Set(parameterType);
                }
                
                return true;
            }
        });
        
        assertDoesNotThrow(() -> this.mappingRule.Transform(this.elements));
    }
}
