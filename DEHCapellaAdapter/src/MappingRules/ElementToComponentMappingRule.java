/*
 * ElementToComponentMappingRule.java
 *
 * Copyright (c) 2020-2022 RHEA System S.A.
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

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.emf.common.util.EList;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.capellacore.TypedElement;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.Interface;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.ComponentPortKind;
import org.polarsys.capella.core.data.fa.OrientationPortKind;
import org.polarsys.capella.core.data.information.Port;
import org.polarsys.capella.core.data.information.Property;
import org.polarsys.capella.core.data.information.Unit;
import org.polarsys.capella.core.data.information.datatype.BooleanType;
import org.polarsys.capella.core.data.information.datatype.DataType;
import org.polarsys.capella.core.data.information.datatype.Enumeration;
import org.polarsys.capella.core.data.information.datatype.NumericTypeKind;
import org.polarsys.capella.core.data.information.datatype.PhysicalQuantity;
import org.polarsys.capella.core.data.information.datatype.StringType;
import org.polarsys.capella.core.data.information.datavalue.DataValue;
import org.polarsys.capella.core.data.information.datavalue.EnumerationLiteral;
import org.polarsys.capella.core.data.information.datavalue.LiteralBooleanValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralNumericValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralStringValue;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.data.pa.PhysicalComponentNature;
import org.polarsys.capella.core.data.requirement.RequirementsPkg;

import App.AppContainer;
import DstController.IDstController;
import Enumerations.CapellaArchitecture;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.CapellaSession.ICapellaSessionService;
import Services.CapellaTransaction.ICapellaTransactionService;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Utils.Ref;
import Utils.ValueSetUtils;
import Utils.Stereotypes.CapellaTypeEnumerationUtility;
import Utils.Stereotypes.HubElementCollection;
import Utils.Stereotypes.RequirementType;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.engineeringmodeldata.BinaryRelationship;
import cdp4common.engineeringmodeldata.ElementBase;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ElementUsage;
import cdp4common.engineeringmodeldata.InterfaceEndKind;
import cdp4common.engineeringmodeldata.ParameterOrOverrideBase;
import cdp4common.engineeringmodeldata.Relationship;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.BooleanParameterType;
import cdp4common.sitedirectorydata.Category;
import cdp4common.sitedirectorydata.EnumerationParameterType;
import cdp4common.sitedirectorydata.MeasurementScale;
import cdp4common.sitedirectorydata.MeasurementUnit;
import cdp4common.sitedirectorydata.NumberSetKind;
import cdp4common.sitedirectorydata.ParameterType;
import cdp4common.sitedirectorydata.QuantityKind;
import cdp4common.sitedirectorydata.TextParameterType;

/**
 * The {@linkplain ElementToComponentMappingRule} is the mapping rule implementation for transforming Capella {@linkplain Component} to {@linkplain ElementDefinition}
 */
public class ElementToComponentMappingRule extends HubToDstBaseMappingRule<HubElementCollection, ArrayList<MappedElementDefinitionRowViewModel>>
{
    /**
     * The {@linkplain ICapellaSessionService}
     */
    private final ICapellaSessionService sessionService;
        
    /**
     * The {@linkplain HubElementCollection} of {@linkplain MappedElementDefinitionRowViewModel}
     */
    private HubElementCollection elements;
    
    /**
     * The {@linkplain Collection} of {@linkplain Component} that were created during this mapping
     */
    private Collection<? extends Component> temporaryComponents = new ArrayList<>();

    /**
     * The {@linkplain Collection} of {@linkplain DataType} that were created during this mapping
     */
    private Collection<DataType> temporaryDataTypes = new ArrayList<>();

    /**
     * The {@linkplain Collection} of {@linkplain Unit} that were created during this mapping
     */
    private Collection<Unit> temporaryUnits = new ArrayList<>();

    /**
     * The {@linkplain HashMap} of {@linkplain ComponentPort} to connect
     */
    private HashMap<ElementUsage, ComponentPort> portsToConnect = new HashMap<>();

    /**
     * The {@linkplain HashMap} of {@linkplain Interface} that were created during this mapping
     */
    private HashMap<String, Interface> temporaryInterfaces = new HashMap<>();

    /**
     * Initializes a new {@linkplain ElementToComponentMappingRule}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain ICapellaMappingConfigurationService}
     * @param sessionService the {@linkplain ICapellaSessionService}
     * @param transactionService the {@linkplain ICapellaTransactionService}
     */
    public ElementToComponentMappingRule(IHubController hubController, ICapellaMappingConfigurationService mappingConfiguration,
            ICapellaSessionService sessionService, ICapellaTransactionService transactionService)
    {
        super(hubController, mappingConfiguration, transactionService);
        this.sessionService = sessionService;
    }
    
    /**
     * Transforms an {@linkplain HubElementCollection} of {@linkplain Component} to an {@linkplain ArrayList} of {@linkplain ElementDefinition}
     * 
     * @param input the {@linkplain HubElementCollection} of {@linkplain Component} to transform
     * @return the {@linkplain ArrayList} of {@linkplain MappedElementDefinitionRowViewModel}
     */
    @Override
    public ArrayList<MappedElementDefinitionRowViewModel> Transform(Object input)
    {
        try
        {
            if(this.dstController == null)
            {
                this.dstController = AppContainer.Container.getComponent(IDstController.class);
            }
            
            this.elements = this.CastInput(input);

            this.Map(this.elements);
            this.SaveMappingConfiguration(this.elements, MappingDirection.FromHubToDst);
            return new ArrayList<>(this.elements);
        }
        catch (Exception exception)
        {
            this.logger.catching(exception);
            return new ArrayList<>();
        }
        finally
        {
            this.temporaryUnits.clear();
            this.temporaryComponents.clear();
            this.temporaryDataTypes.clear();
            this.portsToConnect.clear();
            this.temporaryInterfaces.clear();
        }
    }
    
    /**
     * Maps the provided collection of {@linkplain ElementBase}
     * 
     * @param mappedElementDefinitions the collection of {@linkplain Component} to map
     */
    private void Map(HubElementCollection mappedElementDefinitions)
    {        
        for (var mappedElement : new ArrayList<MappedElementDefinitionRowViewModel>(mappedElementDefinitions))
        {
            if(mappedElement.GetDstElement() == null)
            {
                var component = this.GetOrCreateComponent(mappedElement.GetHubElement(), mappedElement.GetTargetArchitecture());
                mappedElement.SetDstElement(component.getLeft());
            }
            
            this.MapContainedElement(mappedElement, mappedElement.GetTargetArchitecture());
            this.MapProperties(mappedElement.GetHubElement(), mappedElement.GetDstElement());
            this.MapPort(mappedElement);
        }
        
        this.ConnectPorts();
    }
    
    /**
     * Connects the {@linkplain #portsToConnect} via {@linkplain Interfaces}
     */
    private void ConnectPorts()
    {
        for (var portElementUsage : this.portsToConnect.keySet())
        {            
            var port = this.portsToConnect.get(portElementUsage);
            
            for (var relationship : portElementUsage.getRelationships().stream()
                    .filter(x -> x instanceof BinaryRelationship)
                    .map(x -> (BinaryRelationship)x)
                    .collect(Collectors.toList()))
            {
                var refInterface = new Ref<>(Interface.class);

                if(!this.GetOrCreateInterface(relationship, refInterface))
                {
                    continue;
                }
                
                if(AreTheseEquals(relationship.getSource().getIid(), portElementUsage.getIid()))
                {
                    port.getRequiredInterfaces().add(refInterface.Get());
                }
                else
                {
                    port.getProvidedInterfaces().add(refInterface.Get());
                }
            }
        }
    }

    /**
     * Gets or create an {@linkplain Interface} based on the provided {@linkplain Relationship}
     * 
     * @param relationship the {@linkplain Relationship}
     * @param refInterface the {@linkplain Ref} of {@linkplain Interface}
     * @return an assert
     */
    private boolean GetOrCreateInterface(BinaryRelationship relationship, Ref<Interface> refInterface)
    {
        refInterface.Set(this.temporaryInterfaces .get(relationship.getName()));
        
        if(!this.dstController.TryGetElementBy(
                x -> x instanceof Interface && AreTheseEquals(relationship.getName(), ((Interface)x).getName(), true), refInterface))
        {
            Interface newInterface = this.transactionService.Create(Interface.class, relationship.getName());
            refInterface.Set(newInterface);
            this.temporaryInterfaces.put(relationship.getName(), newInterface);
        }
        
        return refInterface.HasValue();
    }

    /**
     * Maps the port for the specified {@linkplain MappedElementDefinitionRowViewModel}
     * 
     * @param mappedElement the {@linkplain MappedElementDefinitionRowViewModel}
     */
    private void MapPort(MappedElementDefinitionRowViewModel mappedElement)
    {
        for (ElementUsage containedUsage : mappedElement.GetHubElement().getContainedElement().stream()
                .filter(x -> x.getInterfaceEnd() != InterfaceEndKind.NONE).collect(Collectors.toList()))
        {
            Ref<ComponentPort> refPort = new Ref<>(ComponentPort.class);
            
            if(!this.GetOrCreatePort(containedUsage, mappedElement.GetDstElement(), refPort))
            {
                continue;
            }

            this.portsToConnect.put(containedUsage, refPort.Get());
            mappedElement.GetDstElement().getOwnedFeatures().removeIf(x -> AreTheseEquals(x.getId(), refPort.Get().getId()));
            mappedElement.GetDstElement().getOwnedFeatures().add(refPort.Get());
        }
    }
    
    /**
     * Gets or create the {@linkplain Port} based on the provided {@linkplain port}
     * 
     * @param port the {@linkplain ElementUsage} port 
     * @param parent the {@linkplain Class} parent
     * @param refPort the {@linkplain Ref} of {@linkplain ComponentPort}
     * @param refDefinition the {@linkplain Ref} of the definition block
     * @return a {@linkplain boolean}
     */
    private boolean GetOrCreatePort(ElementUsage port, Component parent, Ref<ComponentPort> refPort)
    {
        parent.getContainedComponentPorts().stream()
            .filter(x -> AreTheseEquals(x.getName(), port.getName()))
            .findFirst()
            .ifPresent(x -> refPort.Set(x));
                
        if(!refPort.HasValue() && !this.dstController.TryGetElementByName(port, refPort))
        {
            refPort.Set(this.transactionService.Create(ComponentPort.class, port.getName()));
        }
        
        refPort.Get().setKind(ComponentPortKind.STANDARD);
        refPort.Get().setOrientation(this.GetInterfaceEndForPort(port));
        
        return refPort.HasValue();
    }

    /**
     * Gets the {@linkplain ComponentPortKind} based on the specified {@linkplain ElementUsage} port
     * 
     * @param port the {@linkplain ElementUsage}
     * @return the corresponding {@linkplain ComponentPortKind}
     */
    private OrientationPortKind GetInterfaceEndForPort(ElementUsage port)
    {
        switch (port.getInterfaceEnd())
        {
            case INPUT:
                return OrientationPortKind.IN;
            case IN_OUT:
                return OrientationPortKind.INOUT;
            case OUTPUT:
                return OrientationPortKind.OUT;
            default:
                return OrientationPortKind.UNSET;
        }
    }
    
    /**
     * Updates the containment information of the provided parent and component
     * 
     * @param parent the {@linkplain Component} parent
     * @param componentWithPart the {@linkplain Pair} of child component and its {@linkplain Part}
     */
    private void UpdateContainement(Component parent, Pair<Component, Part> componentWithPart)
    {
        if(parent.eContents().stream()
                .filter(x -> x instanceof Component)
                .map(x -> (Component)x)
                .anyMatch(x -> componentWithPart != null && AreTheseEquals(x.getId(), componentWithPart.getLeft().getId())))
        {
            return;
        }
        
        if(parent instanceof PhysicalComponent)
        {
            this.UpdateContainement((PhysicalComponent)componentWithPart.getLeft(), ((PhysicalComponent)parent).getOwnedPhysicalComponents());
        }
        else if(parent instanceof LogicalComponent)
        {
            this.UpdateContainement((LogicalComponent)componentWithPart.getLeft(), ((LogicalComponent)parent).getOwnedLogicalComponents());
        }
        
        if(componentWithPart.getRight() != null)
        {
            parent.getOwnedFeatures().add(componentWithPart.getRight());
        }
    }

    /**
     * Updates the containment information of the provided children collection for the provided {@linkplain #TComponent}
     * 
     * @param <TComponent> the type of {@linkplain Component}
     * @param component the {@linkplain #TComponent}
     * @param ownedComponentCollection the {@linkplain EList} of {@linkplain #TComponent}
     */
    private <TComponent extends Component> void UpdateContainement(TComponent component, EList<TComponent> ownedComponentCollection)
    {
        ownedComponentCollection.removeIf(x -> AreTheseEquals(x.getId(), component.getId()));
        ownedComponentCollection.add(component);
    }
    
    /**
     * Maps the properties of the provided {@linkplain ElementDefinition}
     * 
     * @param hubElement the {@linkplain ElementDefinition} from which the properties are to be mapped
     * @param component the target {@linkplain Component}
     */
    private void MapProperties(ElementDefinition hubElement, Component component)
    {
        this.MapProperties(hubElement.getParameter(), component);
    }

    /**
     * Maps the properties of the provided {@linkplain ElementUsage}
     * 
     * @param hubElement the {@linkplain ElementUsage} from which the properties are to be mapped
     * @param component the target {@linkplain Component}
     */
    private void MapProperties(ElementUsage hubElement, Component component)
    {
        var allParametersAndOverrides = hubElement.getElementDefinition().getParameter().stream()
                .filter(x -> hubElement.getParameterOverride().stream()
                        .noneMatch(o -> AreTheseEquals(x.getParameterType().getIid(), o.getParameterType().getIid())))
                .map(x -> (ParameterOrOverrideBase)x)
                .collect(Collectors.toList());
        
        allParametersAndOverrides.addAll(hubElement.getParameterOverride());
        
        this.MapProperties(allParametersAndOverrides, component);
    }
    
    /**
     * Maps the properties of the provided {@linkplain Collection} of {@linkplain ParameterOrOverrideBase}
     * 
     * @param parameters the {@linkplain Collection} of {@linkplain ParameterOrOverrideBase} to map
     * @param component the target {@linkplain Component}
     */
    private void MapProperties(Collection<? extends ParameterOrOverrideBase> parameters, Component component)
    {
        for (var parameter : parameters)
        {
            var refParameterType = new Ref<>(DataType.class);
            var refProperty = new Ref<Property>(Property.class);
            
            if(!TryGetExistingProperty(component, parameter, refProperty))
            {
                this.GetOrCreateDataType(parameter, component, refParameterType);                             
                this.CreateProperty(parameter, refProperty, refParameterType);
                component.getOwnedFeatures().add(refProperty.Get());
            }
            else if(refProperty.Get().getType() != null)
            {
                refParameterType.Set((DataType)refProperty.Get().getType());
            }
            
            this.UpdateValue(parameter, refProperty, refParameterType);
        }
    }

    /**
     * Get or creates the {@linkplain DataType} that fits the provided {@linkplain Parameter}
     * 
     * @param parameter the {@linkplain MeasurementScale} to map
     * @param component the target {@linkplain Component}
     * @param refParameterType the {@linkplain Ref} of {@linkplain DataType} that will contains the output {@linkplain DataType}
     */
    private void GetOrCreateDataType(ParameterOrOverrideBase parameter, Component component, Ref<DataType> refParameterType)
    {
        if(parameter.getScale() != null)
        {
            this.GetOrCreateDataType(parameter.getScale(), component, refParameterType);
        }
        else
        {
            this.GetOrCreateDataType(parameter.getParameterType(), component, refParameterType);
        }
    }
    

    /**
     * Get or creates the {@linkplain DataType} that matches the provided {@linkplain ParameterType}
     * 
     * @param parameterType the {@linkplain ParameterType} to map
     * @param component the target {@linkplain Component}
     * @param refParameterType the {@linkplain Ref} of {@linkplain DataType} that will contains the output {@linkplain DataType}
     */
    private void GetOrCreateDataType(ParameterType parameterType, Component component, Ref<DataType> refParameterType)
    {
        this.QueryCollectionByNameAndShortName(parameterType, this.temporaryDataTypes, refParameterType);
        
        if(!refParameterType.HasValue() && !this.dstController.TryGetDataType(parameterType, this.sessionService.GetTopElement(), refParameterType))
        {
            var newDataType = this.transactionService.Create(this.GetDataType(parameterType), parameterType.getName());
            
            if(newDataType instanceof Enumeration)
            {
                this.CreateEnumerationLiterals((Enumeration)newDataType, (EnumerationParameterType)parameterType);
            }
            
            this.temporaryDataTypes.add(newDataType);
            this.transactionService.AddReferenceDataToDataPackage(newDataType);
            refParameterType.Set(newDataType);
        }
    }

    /**
     * Creates the possible {@linkplain EnumerationLiteral} for the provided {@linkplain Enumeration} based on the provided {@linkplain EnumerationParameterType}
     * 
     * @param enumerationDataType the {@linkplain Enumeration} data type
     * @param enumerationParameterType the {@linkplain EnumerationParameterType} parameter type
     */
    private void CreateEnumerationLiterals(Enumeration enumerationDataType, EnumerationParameterType enumerationParameterType)
    {
        for (var valueDefinition : enumerationParameterType.getValueDefinition())
        {
            enumerationDataType.getOwnedLiterals().add(this.transactionService.Create(EnumerationLiteral.class, valueDefinition.getName()));
        }
    }

    /**
     * Determine the {@linkplain DataType} {@linkplain Class} based on the provided {@linkplain ParameterType}
     * 
     * @param parameterType the {@linkplain ParameterType}
     * @return a {@linkplain Class} of {@linkplain DataType}
     */
    private Class<? extends DataType> GetDataType(ParameterType parameterType)
    {
        if(parameterType instanceof BooleanParameterType)
        {
            return BooleanType.class;
        }
        else if(parameterType instanceof EnumerationParameterType)
        {
            return Enumeration.class;
        }
        
        return StringType.class;
    }
    
    /**
     * Get or creates the {@linkplain DataType} that matches the provided {@linkplain MeasurementScale}
     * 
     * @param scale the {@linkplain MeasurementScale} to map
     * @param component the target {@linkplain Component}
     * @param refParameterType the {@linkplain Ref} of {@linkplain DataType} that will contains the output {@linkplain DataType}
     */
    private void GetOrCreateDataType(MeasurementScale scale, Component component, Ref<DataType> refParameterType)
    {
        this.QueryCollectionByNameAndShortName(scale, this.temporaryDataTypes, refParameterType);
        
        if(!refParameterType.HasValue() && !this.dstController.TryGetDataType(scale, this.sessionService.GetTopElement(), refParameterType))
        {
            var newDataType = this.transactionService.Create(PhysicalQuantity.class, scale.getName());
            newDataType.setKind(scale.getNumberSet() == NumberSetKind.INTEGER_NUMBER_SET ? NumericTypeKind.INTEGER : NumericTypeKind.FLOAT);
            
            if(scale.getUnit() != null)
            {
                newDataType.setUnit(this.GetOrCreateUnit(scale.getUnit()));
            }
            
            this.temporaryDataTypes.add(newDataType);
            this.transactionService.AddReferenceDataToDataPackage(newDataType);
            refParameterType.Set(newDataType);
        }
    }

    /**
     * Gets or creates the {@linkplain Unit} that matches the provided {@linkplain MeasurementUnit}
     * 
     * @param unit the {@linkplain MeasurementUnit} 
     * @return a matching {@linkplain Unit}
     */
    private Unit GetOrCreateUnit(MeasurementUnit unit)
    {
        var refUnit = new Ref<>(Unit.class);        

        this.QueryCollectionByNameAndShortName(unit, this.temporaryUnits, refUnit);
        
        if(!refUnit.HasValue() && !this.dstController.TryGetElementByName(unit, refUnit))
        {
            var newUnit = this.transactionService.Create(Unit.class, unit.getName());
            refUnit.Set(newUnit);
            this.temporaryUnits.add(newUnit);
            this.transactionService.AddReferenceDataToDataPackage(newUnit);
        }        
        
        return refUnit.Get();
    }
    
    /**
     * Updates the value of the provided {@linkplain Property}
     * 
     * @param parameter the {@linkplain ParameterOrOverrideBase} that contains the values to transfer
     * @param refProperty the {@linkplain Ref} of {@linkplain Property}
     * @param refScale the {@linkplain Ref} of {@linkplain DataType}
     */
    private void UpdateValue(ParameterOrOverrideBase parameter, Ref<Property> refProperty, Ref<DataType> refScale)
    {
        var refDataValue = new Ref<>(DataValue.class);
        
        if (refProperty.Get().getOwnedDefaultValue() != null)
        {
            refDataValue.Set(refProperty.Get().getOwnedDefaultValue());
        }
        else
        {
            refDataValue.Set(CreateDataValue(parameter, refScale));
            refProperty.Get().setOwnedDefaultValue(refDataValue.Get());
        }
        
        UpdateValue(refDataValue.Get(), parameter, refProperty);
    }
    
    /**
     * Updates the value of the provided {@linkplain Property}
     * 
     * @param dataValue the {@linkplain DataValue}
     * @param parameter the {@linkplain ParameterOrOverrideBase} that contains the values to transfer
     * @param refProperty the {@linkplain Ref} of {@linkplain Property} 
     */
    private void UpdateValue(DataValue dataValue, ParameterOrOverrideBase parameter, Ref<Property> refProperty)
    {
        var value = ValueSetUtils.QueryParameterBaseValueSet(parameter, 
                parameter.isOptionDependent() ? this.hubController.GetOpenIteration().getDefaultOption() : null, 
                        parameter.getStateDependence() != null ? parameter.getStateDependence().getActualState().get(0) : null);
        
        var valueString = value.getActualValue().get(0);
        
        if(dataValue instanceof LiteralNumericValue)
        {
            ((LiteralNumericValue)dataValue).setValue(valueString);
        }
        else if(dataValue instanceof LiteralBooleanValue)
        {
            ((LiteralBooleanValue)dataValue).setValue(Boolean.valueOf(valueString));
        }
        else if(dataValue instanceof LiteralStringValue)
        {
            ((LiteralStringValue)dataValue).setValue(valueString);
        }
        else if(dataValue instanceof EnumerationLiteral)
        {
            var enumerationValueDefinition = ((EnumerationParameterType)parameter.getParameterType()).getValueDefinition().stream()
                .filter(x -> AreTheseEquals(x.getShortName(), valueString, true) || AreTheseEquals(x.getName(), valueString, true))
                .findFirst();

            var enumerationType = refProperty.Get().getAbstractType();
            
            if(enumerationValueDefinition.isPresent() && enumerationType instanceof Enumeration)
            {
                ((Enumeration)enumerationType).getOwnedLiterals().stream()
                    .filter(x -> 
                        AreTheseEquals(x.getName(), enumerationValueDefinition.get().getName(), true))
                    .findFirst()
                    .ifPresent(x -> ((EnumerationLiteral)dataValue).setDomainValue(x));
            }
        }
    }

    /**
     * Gets the {@linkplain DataValue} class type
     * 
     * @param parameter the {@linkplain parameter}
     * @return a {@linkplain Class} of {@linkplain DataValue}
     */
    private Class<? extends DataValue> GetDataValueType(ParameterOrOverrideBase parameter)
    {
        if(parameter.getParameterType() instanceof QuantityKind)
        {
            return LiteralNumericValue.class;
        }
        if(parameter.getParameterType() instanceof BooleanParameterType)
        {
            return LiteralBooleanValue.class;
        }
        if(parameter.getParameterType() instanceof TextParameterType)
        {
            return LiteralStringValue.class;
        }
        if(parameter.getParameterType() instanceof EnumerationParameterType)
        {
            return EnumerationLiteral.class;
        }
        
        return LiteralStringValue.class;
    }
    
    /**
     * Creates the {@linkplain DataValue} based on the provided {@linkplain DataType}
     * 
     * @return a {@linkplain DataValue}
     */
    private DataValue CreateDataValue(ParameterOrOverrideBase parameter, Ref<DataType> dstDataType)
    {
        var valueType = this.GetDataValueType(parameter);
        
        DataValue dataValue = null;
        
        if(valueType != null)
        {
            dataValue = this.transactionService.Create(valueType);
            
            if(dataValue instanceof LiteralNumericValue && dstDataType.HasValue() && dstDataType.Get() instanceof PhysicalQuantity)
            {
                ((LiteralNumericValue)dataValue).setUnit(((PhysicalQuantity)dstDataType.Get()).getUnit());
            }
        }
        
        return dataValue;
    }

    /**
     * Creates a {@linkplain Property} based on the provided {@linkplain ParameterOrOverrideBase}
     * 
     * @param parameter the {@linkplain ParameterOrOverrideBase}
     * @param refProperty the {@linkplain Ref} {@linkplain Property}
     * @param dstDataType the {@linkplain Ref} of {@linkplain DataType}
     */
    private void CreateProperty(ParameterOrOverrideBase parameter, Ref<Property> refProperty, Ref<DataType> dstDataType)
    {
        var newProperty = this.transactionService.Create(Property.class, parameter.getParameterType().getName());
        
        if(dstDataType.HasValue())
        {
            newProperty.setAbstractType(dstDataType.Get());
        }
        
        var minimumCardinality = this.transactionService.Create(LiteralNumericValue.class);
        minimumCardinality.setValue(Integer.toString(1));
        newProperty.setOwnedMinCard(minimumCardinality);
        
        var maximumCardinality = this.transactionService.Create(LiteralNumericValue.class);
        maximumCardinality.setValue(Integer.toString(1));
        newProperty.setOwnedMaxCard(maximumCardinality);
        
        refProperty.Set(newProperty);
    }

    /**
     * Tries to get an existing {@linkplain Property} that matches the provided {@linkplain ParameterOrOverrideBase}
     * 
     * @param dstElement the {@linkplain Component}
     * @param parameter the {@linkplain ParameterOrOverrideBase} 
     * @param refProperty the {@linkplain Ref} of {@linkplain Property}
     * @return a value indicating whether the {@linkplain Property} could be found
     */
    private boolean TryGetExistingProperty(Component dstElement, ParameterOrOverrideBase parameter, Ref<Property> refProperty)
    {
        var optionalProperty = dstElement.getContainedProperties().stream()
                .filter(x -> AreTheseEquals(x.getName(), parameter.getParameterType().getName(), true))
                .findFirst();
        
        if(optionalProperty.isPresent())
        {
            refProperty.Set(optionalProperty.get());
        }
        
        return refProperty.HasValue();
    }

    /**
     * Maps the contained element of the provided {@linkplain MappedElementDefinitionRowViewModel} dst element
     * 
     * @param mappedElement the {@linkplain MappedElementDefinitionRowViewModel}
     */
    private void MapContainedElement(MappedElementDefinitionRowViewModel mappedElement, CapellaArchitecture targetArchitecture)
    {
        for (var containedUsage : mappedElement.GetHubElement().getContainedElement().stream()
                .filter(x -> x.getInterfaceEnd() == InterfaceEndKind.NONE).collect(Collectors.toList()))
        {
            Ref<Pair<Component, Part>> componentWithPart = new Ref<Pair<Component, Part>>(null);
            
            MappedElementDefinitionRowViewModel usageDefinitionMappedElement = this.elements.stream()
                    .filter(x -> ((x.GetDstElement() != null && AreTheseEquals(x.GetDstElement().getName(), containedUsage.getName(), true))
                            || AreTheseEquals(containedUsage.getElementDefinition().getIid(), x.GetHubElement().getIid()))
                            && x.GetTargetArchitecture() == targetArchitecture)
                    .findFirst()
                    .orElseGet(() -> 
                    {
                        componentWithPart.Set(this.GetOrCreateComponent(containedUsage, targetArchitecture));
                        
                        var newMappedElement = new MappedElementDefinitionRowViewModel(containedUsage.getElementDefinition(),
                                componentWithPart.Get().getLeft(), MappingDirection.FromHubToDst);
                        
                        newMappedElement.SetTargetArchitecture(targetArchitecture);
                        this.elements.add(newMappedElement);
                        return newMappedElement;
                    });
            
            if(usageDefinitionMappedElement.GetDstElement() == null)
            {
                componentWithPart.Set(this.GetOrCreateComponent(containedUsage, targetArchitecture));
                usageDefinitionMappedElement.SetDstElement(componentWithPart.Get().getLeft());
            }
            
            if(!componentWithPart.HasValue())
            {
                componentWithPart.Set(Pair.of(usageDefinitionMappedElement.GetDstElement(), null));
            }
            
            this.MapProperties(containedUsage, usageDefinitionMappedElement.GetDstElement());
            this.UpdateContainement(mappedElement.GetDstElement(), componentWithPart.Get());
            this.MapPort(usageDefinitionMappedElement);

            if(!AreTheseEquals(mappedElement.GetHubElement().getIid(), usageDefinitionMappedElement.GetHubElement().getIid()))
            {
                this.MapContainedElement(usageDefinitionMappedElement, targetArchitecture);
            }
        }
    }

    /**
     * Gets or creates a component based on an {@linkplain ElementDefinition}
     * 
     * @param elementBase the {@linkplain ElementBase}
     * @param targetArchitecture the {@linkplain CapellaArchitecture} that determines the type of the component
     * @return an existing or a new {@linkplain Component}
     */
    private Pair<Component, Part> GetOrCreateComponent(ElementBase elementBase, CapellaArchitecture targetArchitecture)
    {
        @SuppressWarnings("unchecked")
        var refComponentType = new Ref<>((Class<Class<? extends Component>>) Component.class.getClass());

        if(!this.TryGetComponentClass(elementBase, refComponentType, targetArchitecture))
        {
            refComponentType.Set(PhysicalComponent.class);
        }

        var component = this.GetOrCreateComponent(elementBase.getName(), refComponentType.Get());
        
        if(component != null)
        {
            return Pair.of(component, this.GetOrCreatePart(component));
        }
        
        return Pair.of(component, null);
    }
    
    /**
     * Creates the {@linkplain Part} if non existent otherwise returns null
     * 
     * @param typeReference the {@linkplain Component} type reference
     * @return a {@linkplain Part}
     */
    private Part GetOrCreatePart(Component typeReference)
    {
        if(typeReference.eContainer() != null && typeReference.eContainer() instanceof Component 
                && typeReference.eContents().stream()
                .anyMatch(x -> x instanceof Part 
                        && ((TypedElement)x).getType() != null 
                        && ((TypedElement)x).getType().getId().equals(typeReference.getId())))
        {
            return null;
        }
        
        var part = this.transactionService.Create(Part.class, typeReference.getName());
        part.setAbstractType(typeReference);
        return part;
    }

    /**
     * Gets the {@linkplain RequirementType} based on the {@linkplain Category} applied to the provided {@linkplain cdp4common.engineeringmodeldata.Requirement}
     * 
     * @param elementBase the {@linkplain ElementBase}
     * @param refComponentType the {@linkplain Ref} of {@linkplain RequirementType}
     * @param targetArchitecture the {@linkplain CapellaArchitecture} that determines the type of the component
     * @return a {@linkplain boolean} indicating whether the {@linkplain RequirementType} is different than the default value
     */
    private boolean TryGetComponentClass(ElementBase elementBase, Ref<Class<? extends Component>> refComponentType, CapellaArchitecture targetArchitecture)
    {
        if(targetArchitecture == CapellaArchitecture.LogicalArchitecture)
        {
            refComponentType.Set(LogicalComponent.class);
        }
        else if(targetArchitecture == CapellaArchitecture.PhysicalArchitecture)
        {
            refComponentType.Set(PhysicalComponent.class);
        }
        
        if(!refComponentType.HasValue())
        {        
            for (var category : elementBase.getCategory())
            {
                var componentType = CapellaTypeEnumerationUtility.ComponentTypeFrom(category.getName());
        
                if(componentType != null)
                {
                    refComponentType.Set((Class<? extends Component>) componentType.ClassType());
                    break;
                }
            }
        }
        
        return refComponentType.HasValue();
    }

    /**
     * Gets or creates the {@linkplain RequirementsPkg} that can represent the {@linkplain RequirementsSpecification}
     * 
     * @param <TComponent> the type of {@linkplain Component}
     * @param hubElementName the {@linkplain String} element name in the HUB side
     * @param componentType the {@linkplain Class} of {@linkplain #TComponent}
     * @return a {@linkplain RequirementsPkg}
     */
    private <TComponent extends Component> TComponent GetOrCreateComponent(String hubElementName, Class<TComponent> componentType)
    {
        var refElement = new Ref<>(componentType);
        
        var existingComponent = this.temporaryComponents.stream()
                .filter(x -> componentType.isInstance(x))
                .map(x -> componentType.cast(x))
                .filter(x -> AreTheseEquals(((NamedElement) x).getName(), hubElementName, true))
                .findFirst();
        
        if(existingComponent.isPresent())
        {
            refElement.Set(this.transactionService.Clone(existingComponent.get()));
        }
        else
        {
            if(!this.dstController.TryGetElementBy(x -> x instanceof NamedElement && 
                    AreTheseEquals(((NamedElement) x).getName(), hubElementName, true), refElement))
            {
                var newComponent = this.transactionService.Create(componentType, hubElementName);
                
                if(newComponent instanceof PhysicalComponent)
                {
                    ((PhysicalComponent)newComponent).setNature(PhysicalComponentNature.BEHAVIOR);
                }
                
                refElement.Set(newComponent);
            }
            else
            {
                refElement.Set(this.transactionService.Clone(refElement.Get()));
            }
        }
        
        return refElement.Get();
    }
    
    /**
     * Searches for the provided {@linkplain Collection} for an {@linkplain #TElement} 
     * where the name could match either the name or short name of the provided {@linkplain DefinedThing}.
     * If found, it assigns the value to the provided {@linkplain Ref}
     * 
     * @param <TElement> the type of {@linkplain NamedElement} to get
     * @param definedThing the {@linkplain DefinedThing}
     * @param collection the {@linkplain Collection} of {@linkplain #TElement} to query
     * @param refElement the {@linkplain Ref} of {@linkplain #TElement}
     */
    private <TElement extends NamedElement> void QueryCollectionByNameAndShortName(DefinedThing definedThing, 
            Collection<? extends TElement> collection, Ref<TElement> refElement)
    {
        collection.stream()
                .filter(x -> AreTheseEquals(x.getName(), definedThing.getName(), true) 
                        || AreTheseEquals(x.getName(), definedThing.getShortName(), true))
                .findAny()
                .ifPresent(x -> refElement.Set(x));
    }
}
