/*
 * DstControllerTestFixture.java
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
package DstController;

import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.pde.internal.core.project.RequirementSpecification;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.verification.Times;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.Feature;
import org.polarsys.capella.core.data.capellacore.Trace;
import org.polarsys.capella.core.data.cs.BlockArchitecture;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.Interface;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.information.Property;
import org.polarsys.capella.core.data.information.datatype.DataType;
import org.polarsys.capella.core.data.information.datavalue.DataValue;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.data.requirement.Requirement;
import org.polarsys.capella.core.data.requirement.RequirementsPkg;
import org.polarsys.capella.core.data.requirement.SystemFunctionalRequirement;
import org.polarsys.capella.core.data.requirement.SystemUserRequirement;

import com.google.common.collect.ImmutableList;

import Enumerations.CapellaArchitecture;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.CapellaLog.ICapellaLogService;
import Services.CapellaSession.ICapellaSessionService;
import Services.CapellaTransaction.ClonedReferenceElement;
import Services.CapellaTransaction.ICapellaTransactionService;
import Services.CapellaUserPreference.ICapellaUserPreferenceService;
import Services.HistoryService.ICapellaLocalExchangeHistoryService;
import Services.LocalExchangeHistory.ILocalExchangeHistoryService;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Services.MappingConfiguration.IMappingConfigurationService;
import Services.MappingEngineService.IMappableThingCollection;
import Services.MappingEngineService.IMappingEngineService;
import Services.NavigationService.INavigationService;
import Utils.Ref;
import ViewModels.Interfaces.IMappedElementRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedHubRequirementRowViewModel;
import ViewModels.Rows.MappedDstRequirementRowViewModel;
import cdp4common.commondata.ClassKind;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ElementUsage;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.Parameter;
import cdp4common.engineeringmodeldata.ParameterOverride;
import cdp4common.engineeringmodeldata.ParameterOverrideValueSet;
import cdp4common.engineeringmodeldata.ParameterValueSet;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.BooleanParameterType;
import cdp4common.types.ValueArray;
import cdp4dal.Session;
import cdp4dal.exceptions.TransactionException;
import cdp4dal.operations.ThingTransaction;
import io.reactivex.Observable;

public class DstControllerTestFixture
{
    private IMappingEngineService mappingEngine;
    private DstController controller;
    private IHubController hubController;
    private ICapellaLogService logService;
    private ICapellaMappingConfigurationService mappingConfigurationService;
    private ICapellaSessionService capellaSessionService;
    private ICapellaTransactionService transactionService;
    private ICapellaLocalExchangeHistoryService transferHistory;
    private INavigationService navigationService;
    private ICapellaUserPreferenceService userPreference;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        this.mappingEngine = mock(IMappingEngineService.class);
        this.hubController = mock(IHubController.class);
        this.logService = mock(ICapellaLogService.class);
        this.mappingConfigurationService = mock(ICapellaMappingConfigurationService.class);
        this.capellaSessionService = mock(ICapellaSessionService.class);
        this.transactionService = mock(ICapellaTransactionService.class);
        this.transferHistory = mock(ICapellaLocalExchangeHistoryService.class);
        this.userPreference = mock(ICapellaUserPreferenceService.class);
        this.navigationService = mock(INavigationService.class);
        
        when(this.userPreference.Get(any(), any(), any())).thenReturn(true);
        
        when(this.capellaSessionService.SessionUpdated())
            .thenReturn(Observable.fromArray(mock(org.eclipse.sirius.business.api.session.Session.class)));
        
        when(this.capellaSessionService.SessionUpdated())
            .thenReturn(Observable.fromArray(mock(org.eclipse.sirius.business.api.session.Session.class)));
        
        when(this.hubController.GetIsSessionOpenObservable()).thenReturn(Observable.fromArray(false, true));
        
        when(this.hubController.GetSessionEventObservable()).thenReturn(Observable.fromArray(true));
        
        var mappedThings0 = mock(MappedElementRowViewModel.class);
        var mappedThings1 = mock(MappedElementRowViewModel.class);
        
        var elementDefinition = new ElementDefinition();
        when(mappedThings0.GetHubElement()).thenReturn(elementDefinition);
        
        var requirement = new cdp4common.engineeringmodeldata.Requirement();
        when(mappedThings1.GetHubElement()).thenReturn(requirement);
        
        this.controller = new DstController(this.mappingEngine, this.hubController, this.logService, 
                this.mappingConfigurationService, this.capellaSessionService, this.transactionService, this.transferHistory, this.userPreference, this.navigationService);
        
        this.controller.GetDstMapResult().add(mappedThings0);
        this.controller.GetDstMapResult().add(mappedThings1);
    }

    @Test
    public void VerifyLoadMapping()
    {
        assertDoesNotThrow(() -> this.controller.LoadMapping());
        
        var loadedMapping = new ArrayList<IMappedElementRowViewModel>();
        
        loadedMapping.addAll(
                Arrays.asList(
                    (MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>)
                    new MappedElementDefinitionRowViewModel(new ElementDefinition(), mock(LogicalComponent.class), MappingDirection.FromDstToHub),
                    (MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>)
                    new MappedElementDefinitionRowViewModel(new ElementDefinition(), mock(LogicalComponent.class), MappingDirection.FromHubToDst),
                    (MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>)
                    new MappedDstRequirementRowViewModel(new cdp4common.engineeringmodeldata.Requirement(), mock(SystemUserRequirement.class), MappingDirection.FromDstToHub),
                    (MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>)
                    new MappedHubRequirementRowViewModel(new cdp4common.engineeringmodeldata.Requirement(), mock(SystemUserRequirement.class), MappingDirection.FromHubToDst)
                    ));
        
        when(this.mappingConfigurationService.LoadMapping()).thenReturn(loadedMapping);
        when(this.mappingEngine.Map(any())).thenReturn(loadedMapping);
        assertDoesNotThrow(() -> this.controller.LoadMapping());
        
        verify(this.mappingConfigurationService, times(4)).LoadMapping();
    }
    
    @Test
    public void VerifyMap()
    {
        assertFalse(this.controller.Map(mock(IMappableThingCollection.class), MappingDirection.FromDstToHub));
        var mapResult = new ArrayList<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>>();
        
        mapResult.addAll(
            Arrays.asList(
                (MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>)
                new MappedElementDefinitionRowViewModel(new ElementDefinition(), mock(LogicalComponent.class), MappingDirection.FromDstToHub),
                (MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>)
                new MappedElementDefinitionRowViewModel(new ElementDefinition(), mock(LogicalComponent.class), MappingDirection.FromHubToDst),
                (MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>)
                new MappedDstRequirementRowViewModel(new cdp4common.engineeringmodeldata.Requirement(), mock(SystemUserRequirement.class), MappingDirection.FromDstToHub),
                (MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>)
                new MappedDstRequirementRowViewModel(new cdp4common.engineeringmodeldata.Requirement(), mock(SystemUserRequirement.class), MappingDirection.FromHubToDst)
                ));
        
        when(this.mappingEngine.Map(any())).thenReturn(mapResult);
        assertTrue(this.controller.Map(mock(IMappableThingCollection.class), MappingDirection.FromDstToHub));
        assertTrue(this.controller.Map(mock(IMappableThingCollection.class), MappingDirection.FromHubToDst));
        assertFalse(this.controller.Map(mock(IMappableThingCollection.class), null));
    }
    
    @Test
    public void VerifyTransfer() throws TransactionException
    {
        var requirementsSpecification = new RequirementsSpecification();
        requirementsSpecification.getGroup().add(new RequirementsGroup());
        var requirement = new cdp4common.engineeringmodeldata.Requirement();
        requirementsSpecification.getRequirement().add(requirement);
        requirementsSpecification.setIid(UUID.randomUUID());
        
        var elementDefinition = new ElementDefinition();
        elementDefinition.setIid(UUID.randomUUID());
        var elementUsage = new ElementUsage();
        elementUsage.setIid(UUID.randomUUID());
        elementUsage.setElementDefinition(elementDefinition);
        elementDefinition.getContainedElement().add(elementUsage);
        
        var parameter = new Parameter();
        var parameterValueSet = new ParameterValueSet();
        parameterValueSet.setManual(new ValueArray(Arrays.asList("-"), String.class));
        parameterValueSet.setReference(new ValueArray(Arrays.asList("-"), String.class));
        parameterValueSet.setComputed(new ValueArray(Arrays.asList("-"), String.class));
        parameter.getValueSet().add(parameterValueSet);
        elementDefinition.getParameter().add(parameter.clone(true));
        
        var parameterOverride = new ParameterOverride().clone(false);
        parameterOverride.setParameter(parameter);
        var parameterOverrideValueSet = new ParameterOverrideValueSet();
        parameterOverrideValueSet.setParameterValueSet(parameterValueSet);
        parameterOverrideValueSet.setManual(new ValueArray(Arrays.asList("-"), String.class));
        parameterOverrideValueSet.setReference(new ValueArray(Arrays.asList("-"), String.class));
        parameterOverrideValueSet.setComputed(new ValueArray(Arrays.asList("-"), String.class));
        parameterOverride.getValueSet().add(parameterOverrideValueSet);
        elementUsage.getParameterOverride().add(parameterOverride.clone(true));
        
        when(this.hubController.TrySupplyAndCreateLogEntry(any(ThingTransaction.class))).thenReturn(true);
        
        when(this.hubController.TryGetThingById(any(), any())).thenAnswer(
                new Answer()
                {
                    @Override
                    public Object answer(InvocationOnMock arg0) throws Throwable
                    {
                        var refParameter = (Ref<?>)arg0.getArguments()[1];
                        
                        if(Parameter.class.isAssignableFrom(refParameter.GetType()))
                        {
                            ((Ref<Parameter>)refParameter).Set(parameter);
                        } 
                        else if(ParameterOverride.class.isAssignableFrom(refParameter.GetType()))
                        {
                            ((Ref<ParameterOverride>)refParameter).Set(parameterOverride);
                        }
                        else if(ElementDefinition.class.isAssignableFrom(refParameter.GetType()))
                        {
                            ((Ref<ElementDefinition>)refParameter).Set(elementDefinition);
                        }
                        
                        return true;
                    }
                });
        
        this.controller.GetSelectedDstMapResultForTransfer().add(requirement);
        this.controller.GetSelectedDstMapResultForTransfer().add(elementDefinition);
        var transaction = mock(ThingTransaction.class);
        when(transaction.getAddedThing()).thenReturn(ImmutableList.of((Thing)new ElementDefinition()));
        when(this.hubController.GetIterationTransaction()).thenReturn(Pair.of(new Iteration(), transaction));
        this.controller.ChangeMappingDirection();
        
        when(this.transactionService.Commit(any(Runnable.class))).thenAnswer(x -> 
        {
            x.getArgument(0, Runnable.class).run();
            return true;
        });
        
        when(this.hubController.Refresh()).thenReturn(true);
        assertTrue(this.controller.Transfer());
        this.controller.ChangeMappingDirection();
        assertTrue(this.controller.Transfer());
        when(this.hubController.Refresh()).thenReturn(true);
        this.controller.GetSelectedDstMapResultForTransfer().add(requirement);
        this.controller.GetSelectedDstMapResultForTransfer().add(elementDefinition);
        assertTrue(this.controller.Transfer());
        verify(this.hubController, times(10)).Refresh();
    }
    
    @Test
    public void VerifyUpdateParameterValueSets() throws TransactionException
    {
        when(this.hubController.GetIterationTransaction()).thenReturn(Pair.of(new Iteration(), mock(ThingTransaction.class)));
        assertDoesNotThrow(() -> this.controller.UpdateParameterValueSets());
        verify(this.hubController, times(1)).Write(any(ThingTransaction.class));
    }
    
    @Test
    public void AddOrRemoveAllFromSelectedThingsToTransfer()
    {
        assertEquals(0, this.controller.GetSelectedDstMapResultForTransfer().size());
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind.ElementDefinition, false));
        assertEquals(1, this.controller.GetSelectedDstMapResultForTransfer().size());
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind.Requirement, false));
        assertEquals(2, this.controller.GetSelectedDstMapResultForTransfer().size());
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind.Requirement, true));
        assertEquals(1, this.controller.GetSelectedDstMapResultForTransfer().size());
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind.ElementDefinition, true));
        assertEquals(0, this.controller.GetSelectedDstMapResultForTransfer().size());
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind.ElementUsage, false));
        assertEquals(0, this.controller.GetSelectedDstMapResultForTransfer().size());
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(null, false));
        assertEquals(0, this.controller.GetSelectedHubMapResultForTransfer().size());
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(null, true));
        assertEquals(0, this.controller.GetSelectedHubMapResultForTransfer().size());
    }
    
    @Test
    public void VerifyTryGetDataType()
    {
        var dataType0 = mock(DataType.class);
        when(dataType0.getName()).thenReturn("dataType0");
        var dataType1 = mock(DataType.class);
        when(dataType1.getName()).thenReturn("dataType1");
        
        this.SetupCapellaSession(dataType0, dataType1);

        var refDataType = new Ref<>(DataType.class);
        var parameterType = new BooleanParameterType();
        parameterType.setName("dataType");
        
        assertFalse(this.controller.TryGetDataType(parameterType, null, refDataType));
        assertFalse(refDataType.HasValue());
        parameterType.setName("dataType1");
        assertTrue(this.controller.TryGetDataType(parameterType, null, refDataType));
        assertTrue(refDataType.HasValue());
        assertSame(dataType1, refDataType.Get());
    }
    
    @Test
    public void VerifyTryGetElementBy()
    {
        var component0 = mock(PhysicalComponent.class);
        when(component0.getName()).thenReturn("component0");
        when(component0.getId()).thenReturn(UUID.randomUUID().toString());
        var component1 = mock(PhysicalComponent.class);
        when(component1.getName()).thenReturn("component1");
        when(component1.getId()).thenReturn(UUID.randomUUID().toString());
        var component2 = mock(LogicalComponent.class);
        when(component2.getName()).thenReturn("component2");
        when(component2.getId()).thenReturn(UUID.randomUUID().toString());
        
        this.SetupCapellaSession(component0, component1, component2);

        var refElement = new Ref<>(Component.class);
        
        var elementDefinition = new ElementDefinition();
        elementDefinition.setIid(UUID.randomUUID());
        
        assertFalse(this.controller.TryGetElementByName(elementDefinition, refElement));
        assertFalse(this.controller.TryGetElementById(elementDefinition.getIid().toString(), refElement));
        elementDefinition.setName(component0.getName());
        when(component1.getId()).thenReturn(elementDefinition.getIid().toString());
        assertTrue(this.controller.TryGetElementById(elementDefinition.getIid().toString(), refElement));
        assertSame(component1, refElement.Get());
        refElement.Set(null);
        assertTrue(this.controller.TryGetElementByName(elementDefinition, refElement));
        assertSame(component0, refElement.Get());
    }
    
    private void SetupCapellaSession(CapellaElement... elements)
    {
        var session = mock(org.eclipse.sirius.business.api.session.Session.class);
        var sessionResource = mock(Resource.class);
        var sessionUri = org.eclipse.emf.common.util.URI.createFileURI("ur.i");
        when(sessionResource.getURI()).thenReturn(sessionUri);
        when(session.getSessionResource()).thenReturn(sessionResource);
        when(this.capellaSessionService.GetSession(any())).thenReturn(session);
        var sessionElements = new HashMap<org.eclipse.emf.common.util.URI, List<CapellaElement>>();
        sessionElements.put(sessionUri, Arrays.<CapellaElement>asList(elements));
        when(this.capellaSessionService.GetAllCapellaElementsFromOpenSessions()).thenReturn(sessionElements);
    }
    
    @Test
    public void VerifyTransferToDst() throws TransactionException
    {
        var dataValue0 = mock(DataValue.class);
        var property0 = mock(Property.class);
        when(property0.getOwnedDefaultValue()).thenReturn(dataValue0);
        var dataValue1 = mock(DataValue.class);
        var property1 = mock(Property.class);
        when(property1.getOwnedDefaultValue()).thenReturn(dataValue1);
        var dataValue2 = mock(DataValue.class);
        var property2 = mock(Property.class);
        when(property2.getOwnedDefaultValue()).thenReturn(dataValue2);
        var dataValue3 = mock(DataValue.class);
        var property3 = mock(Property.class);
        when(property3.getOwnedDefaultValue()).thenReturn(dataValue3);
        
        var portInterface = mock(Interface.class);
        
        var port0 = mock(ComponentPort.class);
        when(port0.getProvidedInterfaces()).thenReturn(new BasicEList<Interface>(Arrays.asList(portInterface)));
        when(port0.getRequiredInterfaces()).thenReturn(new BasicEList<Interface>());
        var port1 = mock(ComponentPort.class);
        when(port1.getRequiredInterfaces()).thenReturn(new BasicEList<Interface>(Arrays.asList(portInterface)));
        when(port1.getProvidedInterfaces()).thenReturn(new BasicEList<Interface>());
        
        var component0 = mock(PhysicalComponent.class);
        when(component0.getName()).thenReturn("component0");
        when(component0.getId()).thenReturn(UUID.randomUUID().toString());
        when(component0.getContainedProperties()).thenReturn(new BasicEList<Property>(Arrays.asList(property0)));
        when(component0.getOwnedFeatures()).thenReturn(new BasicEList<Feature>(Arrays.asList(property0)));
        when(component0.getOwnedPhysicalComponents()).thenReturn(new BasicEList<PhysicalComponent>());
        when(component0.eContents()).thenReturn(new BasicEList<EObject>());
        when(component0.getContainedComponentPorts()).thenReturn(new BasicEList<ComponentPort>(Arrays.asList(port0)));
        when(component0.getOwnedTraces()).thenReturn(new BasicEList<Trace>());
        
        var component1 = mock(PhysicalComponent.class);
        when(component1.getName()).thenReturn("component1");
        when(component1.getId()).thenReturn(UUID.randomUUID().toString());
        when(component1.getContainedProperties()).thenReturn(new BasicEList<Property>(Arrays.asList(property1, property2)));
        when(component1.getOwnedFeatures()).thenReturn(new BasicEList<Feature>(Arrays.asList(property1)));
        when(component1.getOwnedPhysicalComponents()).thenReturn(new BasicEList<PhysicalComponent>(Arrays.asList(component0)));
        when(component1.eContents()).thenReturn(new BasicEList<EObject>());
        when(component1.getContainedComponentPorts()).thenReturn(new BasicEList<ComponentPort>(Arrays.asList(port1)));
        when(component1.getOwnedTraces()).thenReturn(new BasicEList<Trace>());
        
        var component2 = mock(LogicalComponent.class);
        when(component2.getName()).thenReturn("component2");
        when(component2.getId()).thenReturn(UUID.randomUUID().toString());
        when(component2.getContainedProperties()).thenReturn(new BasicEList<Property>(Arrays.asList(property3)));
        when(component2.getOwnedFeatures()).thenReturn(new BasicEList<Feature>());
        when(component2.getOwnedLogicalComponents()).thenReturn(new BasicEList<LogicalComponent>());
        when(component2.eContents()).thenReturn(new BasicEList<EObject>());
        when(component2.getContainedComponentPorts()).thenReturn(new BasicEList<ComponentPort>());
        when(component2.getOwnedTraces()).thenReturn(new BasicEList<Trace>());
        
        
        var systemRequirement = mock(SystemFunctionalRequirement.class);
        when(systemRequirement.getName()).thenReturn("component2");
        when(systemRequirement.getId()).thenReturn(UUID.randomUUID().toString());
        when(systemRequirement.getOwnedTraces()).thenReturn(new BasicEList<Trace>());
        
        var requirementPackage = mock(RequirementsPkg.class);
        when(requirementPackage.eContents()).thenReturn(new BasicEList<EObject>(Arrays.asList(systemRequirement)));
        when(requirementPackage.getOwnedRequirementPkgs()).thenReturn(new BasicEList<RequirementsPkg>());
        when(requirementPackage.getOwnedRequirements()).thenReturn(new BasicEList<Requirement>());
        when(requirementPackage.getOwnedTraces()).thenReturn(new BasicEList<Trace>());
        
        when(systemRequirement.eContainer()).thenReturn(requirementPackage);
        
        when(this.transactionService.GetTargetArchitecture(any())).thenReturn(CapellaArchitecture.PhysicalArchitecture);
        when(this.hubController.Refresh()).thenReturn(true);
        
        var transaction = mock(ThingTransaction.class);
        when(this.hubController.GetIterationTransaction()).thenReturn(Pair.of(new Iteration(), transaction));
        when(this.mappingConfigurationService.IsTheCurrentIdentifierMapTemporary()).thenReturn(false);

        assertFalse(this.controller.TransferToDst());
        when(this.transactionService.Commit(any())).thenReturn(true);
        assertTrue(this.controller.TransferToDst());
        
        when(this.transactionService.Commit(any())).thenAnswer(x ->
        {
            try
            {
                x.getArgument(0, Runnable.class).run();
                return true;
            }
            catch(Exception exception)
            {
                exception.printStackTrace();
                System.out.println(exception);
                return false;
            }
        });
        
        assertTrue(this.controller.TransferToDst());
        
        this.controller.GetSelectedHubMapResultForTransfer().add(component0);
        this.controller.GetSelectedHubMapResultForTransfer().add(component1);
        this.controller.GetSelectedHubMapResultForTransfer().add(component2);
        this.controller.GetSelectedHubMapResultForTransfer().add(systemRequirement);
        
        when(this.capellaSessionService.GetArchitectureInstance(any(CapellaArchitecture.class))).thenAnswer(x -> 
        {
            var architectureInstance = mock(BlockArchitecture.class);
            when(architectureInstance.getOwnedRequirementPkgs()).thenReturn(new BasicEList<RequirementsPkg>());
            return architectureInstance;
        });
        
        assertTrue(this.controller.TransferToDst());

        this.controller.GetSelectedHubMapResultForTransfer().add(component0);
        this.controller.GetSelectedHubMapResultForTransfer().add(component1);
        this.controller.GetSelectedHubMapResultForTransfer().add(component2);
        this.controller.GetSelectedHubMapResultForTransfer().add(systemRequirement);
        
        when(this.transactionService.IsCloned(any())).thenReturn(true);
        
        when(this.transactionService.GetClone(any())).thenAnswer(x -> 
        {
            var clonedReference = mock(ClonedReferenceElement.class);
            when(clonedReference.GetClone()).thenReturn(x.getArgument(0));
            when(clonedReference.GetOriginal()).thenReturn(x.getArgument(0));
            return clonedReference;
        });

        assertTrue(this.controller.TransferToDst());
    }
}
