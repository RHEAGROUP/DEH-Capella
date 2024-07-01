/*
 * BinaryRelationshipToCapellaTracesTestFixture.java
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
import org.polarsys.capella.core.data.capellacommon.GenericTrace;
import org.polarsys.capella.core.data.capellacore.Trace;
import org.polarsys.capella.core.data.cs.Interface;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.OrientationPortKind;
import org.polarsys.capella.core.data.information.Property;
import org.polarsys.capella.core.data.information.Unit;
import org.polarsys.capella.core.data.information.datatype.BooleanType;
import org.polarsys.capella.core.data.information.datatype.NumericType;
import org.polarsys.capella.core.data.information.datatype.StringType;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.data.requirement.SystemUserRequirement;

import DstController.IDstController;

import org.polarsys.capella.core.data.information.datavalue.DataValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralBooleanValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralNumericValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralStringValue;
import org.polarsys.capella.core.data.information.datavalue.NumericValue;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Reactive.ObservableCollection;
import Services.CapellaTransaction.ICapellaTransactionService;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Services.MappingConfiguration.IMappingConfigurationService;
import Utils.Ref;
import Utils.Stereotypes.CapellaComponentCollection;
import Utils.Stereotypes.HubRelationshipElementsCollection;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedHubRequirementRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ActualFiniteState;
import cdp4common.engineeringmodeldata.ActualFiniteStateList;
import cdp4common.engineeringmodeldata.BinaryRelationship;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.EngineeringModel;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.PossibleFiniteState;
import cdp4common.engineeringmodeldata.PossibleFiniteStateList;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
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

public class BinaryRelationshipToCapellaTracesTestFixture
{
    private static final String relationship0Name = "e0 -> r0";
    private static final String relationship1Name = "e2 -> e0";
    private ICapellaMappingConfigurationService mappingConfigurationService;
    private IHubController hubController;
    private BinaryRelationshipToCapellaTraces mappingRule;
    private HubRelationshipElementsCollection elements;
    private ElementDefinition elementDefinition0;
    private ElementDefinition elementDefinition1;
    private DomainOfExpertise domain;
    private Iteration iteration;
    private PhysicalComponent component0;
    private PhysicalComponent component1;
    private PhysicalComponent component2;
    private ElementDefinition elementDefinition2;
    private ICapellaTransactionService transactionService;
    private IDstController dstController;
    private cdp4common.engineeringmodeldata.Requirement requirement0;
    private BinaryRelationship relationship1;
    private BinaryRelationship relationship0;
    private SystemUserRequirement capellaRequirement;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        this.hubController = mock(IHubController.class);
        this.mappingConfigurationService = mock(ICapellaMappingConfigurationService.class);
        this.transactionService = mock(ICapellaTransactionService.class);
        this.dstController = mock(IDstController.class);
        
        this.SetupElements();
        when(this.dstController.GetMappedBinaryRelationshipsToTraces()).thenReturn(new ObservableCollection<Trace>());        
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
        
        this.mappingRule = new BinaryRelationshipToCapellaTraces(this.hubController, this.mappingConfigurationService, this.transactionService);
        this.mappingRule.dstController = this.dstController;
    }

    private void SetupElements()
    {
        this.domain = new DomainOfExpertise(UUID.randomUUID(), null, null);        
        this.iteration = new Iteration(UUID.randomUUID(), null, null);

        this.relationship0 = new BinaryRelationship();
        this.relationship0.setName(relationship0Name);
        this.relationship1 = new BinaryRelationship();
        this.relationship1.setName(relationship1Name);
        
        this.elementDefinition0 = mock(ElementDefinition.class);
        when(this.elementDefinition0.getIid()).thenReturn(UUID.randomUUID());
        when(this.elementDefinition0.getOwner()).thenReturn(this.domain);
        when(this.elementDefinition0.getName()).thenReturn("elementDefinition0");
        when(this.elementDefinition0.getShortName()).thenReturn("elementDefinition0");
        when(this.elementDefinition0.getRelationships()).thenReturn(Arrays.asList(this.relationship1, this.relationship0));
        
        this.requirement0 = mock(cdp4common.engineeringmodeldata.Requirement.class);
        when(this.requirement0.getOwner()).thenReturn(this.domain);
        when(this.requirement0.getIid()).thenReturn(UUID.randomUUID());
        when(this.requirement0.getName()).thenReturn("requirement0");
        when(this.requirement0.getShortName()).thenReturn("requirement0");
        when(this.requirement0.getRelationships()).thenReturn(Arrays.asList(this.relationship0));
        
        var requirementsSpecification = new RequirementsSpecification();
        requirementsSpecification.getRequirement().add(this.requirement0);
        
        this.elementDefinition1 = mock(ElementDefinition.class);
        when(this.elementDefinition1.getOwner()).thenReturn(this.domain);
        when(this.elementDefinition1.getIid()).thenReturn(UUID.randomUUID());
        when(this.elementDefinition1.getName()).thenReturn("elementDefinition1");
        when(this.elementDefinition1.getShortName()).thenReturn("elementDefinition1");
        
        this.elementDefinition2 = mock(ElementDefinition.class);
        when(this.elementDefinition2.getOwner()).thenReturn(this.domain);
        when(this.elementDefinition2.getIid()).thenReturn(UUID.randomUUID());
        when(this.elementDefinition2.getName()).thenReturn("elementDefinition2");
        when(this.elementDefinition2.getShortName()).thenReturn("elementDefinition2");
        when(this.elementDefinition2.getRelationships()).thenReturn(Arrays.asList(this.relationship1));
        
        this.iteration.getElement().add(this.elementDefinition0);
        this.iteration.getElement().add(this.elementDefinition1);
        this.iteration.getElement().add(this.elementDefinition2);
        
        this.relationship0.setSource(this.elementDefinition0);
        this.relationship0.setTarget(this.requirement0);

        this.relationship1.setSource(this.elementDefinition2);
        this.relationship1.setTarget(this.elementDefinition0);
        
        this.iteration.getRelationship().add(this.relationship0);
        this.iteration.getRelationship().add(this.relationship1);
        
        this.elements = new HubRelationshipElementsCollection();
        
        this.component0 = mock(PhysicalComponent.class);
        when(this.component0.getName()).thenReturn("component0");
        when(this.component0.isActor()).thenReturn(true);
        when(this.component0.getOutgoingTraces()).thenReturn(new BasicEList());
        when(this.component0.getIncomingTraces()).thenReturn(new BasicEList());
        this.component1 = mock(PhysicalComponent.class);
        when(this.component1.getName()).thenReturn("component1");
        when(this.component1.isAbstract()).thenReturn(true);
        when(this.component1.isHuman()).thenReturn(true);
        when(this.component1.eContents()).thenReturn(new BasicEList());
        when(this.component1.getOutgoingTraces()).thenReturn(new BasicEList());
        when(this.component1.getIncomingTraces()).thenReturn(new BasicEList());
        this.component2 = mock(PhysicalComponent.class);
        when(this.component2.getName()).thenReturn("element");
        when(this.component2.getOutgoingTraces()).thenReturn(new BasicEList());
        when(this.component2.getIncomingTraces()).thenReturn(new BasicEList());
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
        
        this.capellaRequirement = mock(SystemUserRequirement.class);
        when(this.capellaRequirement.getId()).thenReturn(UUID.randomUUID().toString());
        when(this.capellaRequirement.getOutgoingTraces()).thenReturn(new BasicEList());
        when(this.capellaRequirement.getIncomingTraces()).thenReturn(new BasicEList());
        
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
        
        this.elements.add(new MappedElementDefinitionRowViewModel(this.elementDefinition0, this.component0, MappingDirection.FromHubToDst));
        this.elements.add(new MappedHubRequirementRowViewModel(this.requirement0, this.capellaRequirement, MappingDirection.FromHubToDst));
        this.elements.add(new MappedElementDefinitionRowViewModel(this.elementDefinition2, this.component2, MappingDirection.FromHubToDst));
    }

    @Test
    public void VerifyTransform()
    {
        assertDoesNotThrow(() -> this.mappingRule.Transform(null));
        assertDoesNotThrow(() -> this.mappingRule.Transform(mock(List.class)));

        var createdTraces = new ArrayList<Trace>();
        
        when(this.transactionService.Create(GenericTrace.class)).thenAnswer(x -> 
        {
            var newTrace = mock(GenericTrace.class);
            createdTraces.add(newTrace);
            return newTrace;
        });
        
        var mapResult = this.mappingRule.Transform(this.elements);
        assertEquals(2, mapResult.size());
        assertEquals(2, createdTraces.size());
//        verify(createdTraces.get(1), times(1)).setSummary(this.relationship0Name);
        verify(createdTraces.get(0), times(1)).setSummary(this.relationship1Name);
    }
}
