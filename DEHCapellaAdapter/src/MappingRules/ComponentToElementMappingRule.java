/*
 * ComponentToElementMappingRule.java
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

import static Utils.Operators.Operators.AreTheseEquals;
import static Utils.Stereotypes.StereotypeUtils.GetShortName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.ComponentPkg;
import org.polarsys.capella.core.data.cs.Interface;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.information.Property;
import org.polarsys.capella.core.data.information.datavalue.LiteralBooleanValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralNumericValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralStringValue;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.pa.PhysicalComponent;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Utils.Ref;
import Utils.ValueSetUtils;
import Utils.Stereotypes.CapellaComponentCollection;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import cdp4common.commondata.ClassKind;
import cdp4common.commondata.Definition;
import cdp4common.engineeringmodeldata.BinaryRelationship;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ElementUsage;
import cdp4common.engineeringmodeldata.InterfaceEndKind;
import cdp4common.engineeringmodeldata.Parameter;
import cdp4common.engineeringmodeldata.ParameterOrOverrideBase;
import cdp4common.engineeringmodeldata.ParameterOverride;
import cdp4common.engineeringmodeldata.ParameterOverrideValueSet;
import cdp4common.engineeringmodeldata.ParameterSwitchKind;
import cdp4common.engineeringmodeldata.ParameterValueSet;
import cdp4common.engineeringmodeldata.ParameterValueSetBase;
import cdp4common.engineeringmodeldata.Relationship;
import cdp4common.sitedirectorydata.BooleanParameterType;
import cdp4common.sitedirectorydata.Category;
import cdp4common.sitedirectorydata.MeasurementScale;
import cdp4common.sitedirectorydata.MeasurementUnit;
import cdp4common.sitedirectorydata.NumberSetKind;
import cdp4common.sitedirectorydata.ParameterType;
import cdp4common.sitedirectorydata.QuantityKind;
import cdp4common.sitedirectorydata.RatioScale;
import cdp4common.sitedirectorydata.ReferenceDataLibrary;
import cdp4common.sitedirectorydata.SimpleQuantityKind;
import cdp4common.sitedirectorydata.SimpleUnit;
import cdp4common.sitedirectorydata.TextParameterType;
import cdp4common.types.ValueArray;

/**
 * The {@linkplain ComponentToElementMappingRule} is the mapping rule implementation for transforming Capella {@linkplain Component} to {@linkplain ElementDefinition}
 */
public class ComponentToElementMappingRule extends DstToHubBaseMappingRule<CapellaComponentCollection, ArrayList<MappedElementDefinitionRowViewModel>>
{
    /**
     * The string that indicates the language code for the {@linkplain Definition}
     * That contains the Capella Id of the mapped {@linkplain CapellaElement}
     */
    public static final String CIID = "CIID";
    
    /**
     * The string that specifies the {@linkplain ElementDefinition} representing ports
     */
    private static final String PORTELEMENTDEFINITIONNAME = "Port";

    /**
     * The string that specifies the {@linkplain ElementDefinition} representing ports
     */
    private static final String INTERFACECATEGORYNAME = "Interface";

    /**
     * The {@linkplain CapellaComponentCollection} of {@linkplain MappedElementDefinitionRowViewModel}
     */
    private CapellaComponentCollection elements;

    /**
     * The {@linkplain ElementDefinition} that represents the ports
     */
    private ElementDefinition portElementDefinition;
    
    /**
     * The collection of {@linkplain Triple} of {@linkplain ComponentPort}, {@linkplain MappedElementDefinitionRowViewModel}
     * and {@linkplain ElementUsage} representing a connected port, the {@linkplain MappedElementDefinitionRowViewModel} representing the parent
     * and the {@linkplain ElementUsage} corresponding to the {@linkplain ComponentPort}.
     * This collection serves for future relationship creation.
     */
    private List<Triple<ComponentPort, MappedElementDefinitionRowViewModel, ElementUsage>> portsToConnect = new ArrayList<>();
    
    /**
     * Initializes a new {@linkplain ComponentToElementMappingRule}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain ICapellaMappingConfigurationService}
     */
    public ComponentToElementMappingRule(IHubController hubController, ICapellaMappingConfigurationService mappingConfiguration)
    {
        super(hubController, mappingConfiguration);
    }
    
    /**
     * Transforms an {@linkplain CapellaComponentCollection} of {@linkplain Component} to an {@linkplain ArrayList} of {@linkplain ElementDefinition}
     * 
     * @param input the {@linkplain CapellaComponentCollection} of {@linkplain Component} to transform
     * @return the {@linkplain ArrayList} of {@linkplain MappedElementDefinitionRowViewModel}
     */
    @Override
    public ArrayList<MappedElementDefinitionRowViewModel> Transform(Object input)
    {
        try
        {
            this.elements = this.CastInput(input);
            this.Map(this.elements);
            this.SaveMappingConfiguration(this.elements, MappingDirection.FromDstToHub);
            return new ArrayList<>(this.elements);
        }
        catch (Exception exception)
        {
            this.logger.catching(exception);
            return new ArrayList<>();
        }
        finally
        {
            this.portsToConnect.clear();
        }
    }
    
    /**
     * Maps the provided collection of  {@linkplain Component}
     * 
     * @param mappedElementDefinitions the collection of {@linkplain Component} to map
     */
    private void Map(CapellaComponentCollection mappedElementDefinitions)
    {        
        for (MappedElementDefinitionRowViewModel mappedElement : new ArrayList<MappedElementDefinitionRowViewModel>(mappedElementDefinitions).stream()
                .filter(x -> x.DoesRepresentAnElementDefinitionComponentMapping())
                .collect(Collectors.toList()))
        {
            if(mappedElement.DoesRepresentAnElementDefinitionComponentMapping() && mappedElement.GetHubElement() == null)
            {
                mappedElement.SetHubElement(this.GetOrCreateElementDefinition((Component)mappedElement.GetDstElement()));
            }
            
            this.MapCategories(mappedElement);            
            this.MapContainedElement(mappedElement);
            this.MapProperties((ElementDefinition)mappedElement.GetHubElement(), (Component)mappedElement.GetDstElement());

            mappedElement.GetHubElement().setName(mappedElement.GetDstElement().getName());
            mappedElement.GetHubElement().setShortName(GetShortName(mappedElement.GetDstElement()));
        }
        
        this.MapPorts();
        this.MapInterfaces();
    }
    
    /**
     * Creates the {@linkplain BinaryRelationShip} that connects ports between each others
     * 
     * @implSpec the interface is retrieved the following way
     * - for each interface connected
     * - Select the realizations of each interface
     * - filter out the realizations that belongs to the current port owner (block)
     * - select then the interface and the block that owns the realization of the  interface 
     */
    private void MapInterfaces()
    {
        for (Triple<ComponentPort, MappedElementDefinitionRowViewModel, ElementUsage> portElementUsage : this.portsToConnect)
        {
            var port = portElementUsage.getLeft();
            
            if(port.getProvidedInterfaces().isEmpty() && port.getRequiredInterfaces().isEmpty())
            {
                continue;
            }
                       
            var sourcePortAndInterface = this.portsToConnect.stream()
                    .flatMap(x -> ((Component)x.getMiddle().GetDstElement()).getContainedComponentPorts().stream())
                    .map(x -> 
                    {
                        var matchingInterface = x.getProvidedInterfaces().stream()
                                .filter(i -> 
                                port.getRequiredInterfaces().stream()
                                        .anyMatch(pi -> 
                                        AreTheseEquals(i.getId(), pi.getId())))
                                .findFirst();
                        
                        return matchingInterface.isPresent() 
                                ? Pair.of(x, matchingInterface.get())
                                : null;
                    })
                    .filter(x -> x != null)
                    .findFirst();
            
            if(sourcePortAndInterface.isEmpty())
            {
                continue;
            }
            
            for (var capellaInterface : port.getRequiredInterfaces())
            {                                
                ElementUsage connectedPortElementUsage = this.portsToConnect.stream()
                        .filter(x -> AreTheseEquals(x.getLeft().getId(), sourcePortAndInterface.get().getLeft().getId()))
                        .map(x -> x.getRight())
                        .findFirst()
                        .orElse(this.hubController.GetOpenIteration().getElement().stream()
                                .flatMap(x -> x.getContainedElement().stream())
                                .filter(x -> AreTheseEquals(x.getName(), port.getName()))
                        .findFirst()
                        .orElse(null));
                
                if(connectedPortElementUsage == null)
                {
                    continue;
                }
                
                BinaryRelationship relationship = this.hubController.GetOpenIteration()
                        .getRelationship()
                        .stream()
                        .filter(BinaryRelationship.class::isInstance)
                        .map(BinaryRelationship.class::cast)
                        .filter(x -> AreTheseEquals(capellaInterface.getName(), x.getName())
                                && AreTheseEquals(x.getTarget().getIid(), portElementUsage.getRight().getIid())
                                && AreTheseEquals(x.getSource().getIid(), connectedPortElementUsage.getIid()))
                        .findFirst()    
                        .map(x -> x.clone(false))
                        .orElseGet(this.CreateBinaryRelationship(capellaInterface, connectedPortElementUsage, portElementUsage.getRight()));
                
                this.logger.info(String.format("BinaryRelationShip %s is linking element %s and element %s", relationship.getName(), portElementUsage.getRight().modelCode(null), connectedPortElementUsage.modelCode(null)));
                portElementUsage.getMiddle().GetRelationships().add(relationship);
            }
        }
    }

    /**
     * Creates a {@linkplain BinaryRelationship} based on the specified {@linkplain Interface}
     *  
     * @return a {@linkplain Supplier} of {@linkplain Relationship}
     */
    private Supplier<? extends BinaryRelationship> CreateBinaryRelationship(Interface portInterface, ElementUsage source, ElementUsage target)
    {
        var relationship = new BinaryRelationship();
        relationship.setIid(UUID.randomUUID());
        relationship.setOwner(this.hubController.GetCurrentDomainOfExpertise());
        relationship.setName(portInterface.getName());
        relationship.setSource(source);
        relationship.setTarget(target);
        
        this.MapCategory(relationship, INTERFACECATEGORYNAME, ClassKind.BinaryRelationship);
        
        return () -> relationship;
    }
    
    /**
     * Maps the proper {@linkplain Category} to the associated HUB element of the provided {@linkplain MappedElementDefinitionRowViewModel}
     * 
     * @param mappedElement the {@linkplain MappedElementDefinitionRowViewModel}
     */
    private void MapCategories(MappedElementDefinitionRowViewModel mappedElement)
    {
        if(mappedElement.GetDstElement().eContainer() instanceof ComponentPkg)
        {
            this.MapCategory(mappedElement.GetHubElement(), "System", ClassKind.ElementDefinition);
        }
        
        if(StringUtils.containsIgnoreCase(mappedElement.GetDstElement().getName(), "subsystem"))
        {
            this.MapCategory(mappedElement.GetHubElement(), "Subsystem", ClassKind.ElementDefinition);
        }
        
        if(mappedElement.DoesRepresentAnElementDefinitionComponentMapping())
        {
            var component = (Component)mappedElement.GetDstElement();
            
            if(component.isActor())
            {
                this.MapCategory(mappedElement.GetHubElement(), "Actor", ClassKind.ElementDefinition);
            }

            if(component.isHuman())
            {
                this.MapCategory(mappedElement.GetHubElement(), "Human", ClassKind.ElementDefinition);
            }

            if(component.isAbstract())
            {
                this.MapCategory(mappedElement.GetHubElement(), "Abstract", ClassKind.ElementDefinition);
            }
        }        
                
        if(mappedElement.GetDstElement() instanceof LogicalComponent)
        {
            this.MapCategory(mappedElement.GetHubElement(), "Logical Component", ClassKind.ElementDefinition);
        }
        
        if(mappedElement.GetDstElement() instanceof PhysicalComponent)
        {
            this.MapCategory(mappedElement.GetHubElement(), "Physical Component", ClassKind.ElementDefinition);
            
            var physicalComponent = (PhysicalComponent)mappedElement.GetDstElement();
                        
            this.MapCategory(mappedElement.GetHubElement(), physicalComponent.getNature().name(), ClassKind.ElementDefinition);
            this.MapCategory(mappedElement.GetHubElement(), physicalComponent.getKind().name(), ClassKind.ElementDefinition);
            
        }
    }
        
    /**
     * Maps the contained element of the provided {@linkplain mappedElement}
     * 
     * @param mappedElement the {@linkplain MappedElementDefinitionRowViewModel}
     */
    private void MapContainedElement(MappedElementDefinitionRowViewModel mappedElement)
    {
        for (var containedElement : mappedElement.GetDstElement().eContents()
                .stream()
                .filter(x -> x instanceof Part && ((Part)x).getAbstractType() != null)
                .map(x -> (Part)x)
                .collect(Collectors.toList()))
        {
            this.MapContainedElement((ElementDefinition)mappedElement.GetHubElement(), containedElement, (Component)containedElement.getAbstractType());
        }
    }
        
    /**
     * Maps the provided contained element
     * 
     * @param container the {@linkplain ElementDefinition} container
     * @param component the contained element to map
     */
    private void MapContainedElement(ElementDefinition container, Part part, Component component)
    {
        MappedElementDefinitionRowViewModel mappedElement = this.elements.stream()
                .filter(x -> x.DoesRepresentAnElementDefinitionComponentMapping() 
                        && x.GetDstElement() != null
                        && AreTheseEquals(x.GetDstElement().getId(), component.getId()))
                .findFirst()
                .orElseGet(() -> 
                {
                    MappedElementDefinitionRowViewModel element = 
                            new MappedElementDefinitionRowViewModel(
                                    this.GetOrCreateElementDefinition(component), component, MappingDirection.FromDstToHub);
                    
                    this.MapCategories(element);
                    this.elements.add(element);
                    this.MapContainedElement(element);
                    return element;
                });
        
        if(mappedElement.GetHubElement() == null)
        {
            mappedElement.SetHubElement(this.GetOrCreateElementDefinition(component));
        }
        
        this.MapProperties((ElementDefinition)mappedElement.GetHubElement(), component);
        
        var elementUsage = this.GetOrCreateElementUsage(container, part, mappedElement);
        
        this.MapProperties(elementUsage, (ElementDefinition)mappedElement.GetHubElement(), component);
        
        container.getContainedElement().removeIf(x -> AreTheseEquals(elementUsage.getIid(), x.getIid()));
        container.getContainedElement().add(elementUsage);
    }

    /**
     * Gets or create the {@linkplain ElementUsage} that matches the {@linkplain Component}
     * 
     * @param container the {@linkplain ElementDefinition} container
     * @param part the contained element to map
     * @param mappedElement the {@linkplain MappedElementDefinitionRowViewModel} of the container
     * @return an {@linkplain ElementUsage}
     */
    private ElementUsage GetOrCreateElementUsage(ElementDefinition container, Part part, MappedElementDefinitionRowViewModel mappedElement)
    {
        var usage = this.elements.stream().filter(x -> x != null)
                              .filter(x -> !x.DoesRepresentAnElementDefinitionComponentMapping() && AreTheseEquals(x.GetDstElement().getId(), part.getId()))
                              .map(x -> 
                              {
                                  var existingMappedUsage = (ElementUsage) x.GetHubElement();
                                  
                                  if(existingMappedUsage == null)
                                  {
                                      var newElementUsage = this.CreateElementUsage(container, part, (ElementDefinition)mappedElement.GetHubElement());
                                      x.SetHubElement(newElementUsage);
                                      return newElementUsage;
                                  }
                                  
                                  return existingMappedUsage;
                              })
                              .findFirst()
                              .orElse(null);
        
        if(usage == null)
        {
            usage = container.getContainedElement()
                        .stream()
                        .filter(x -> AreTheseEquals(part.getName(), x.getName()) && 
                                AreTheseEquals(mappedElement.GetHubElement().getIid(), x.getElementDefinition().getIid()))
                        .findFirst()
                        .map(x -> x.clone(false))
                        .orElseGet(() -> this.CreateElementUsage(container, part, (ElementDefinition)mappedElement.GetHubElement()));
            
            this.elements.add(new MappedElementDefinitionRowViewModel(usage, part, MappingDirection.FromDstToHub));
        }
        

        usage.setName(part.getName());
        usage.setShortName(part.getName());
        
        return usage;
    }
    
    /**
     * Create the {@linkplain ElementUsage} that matches the {@linkplain Component}
     * 
     * @param container the {@linkplain ElementDefinition} container
     * @param part the contained element to map
     * @param elementDefinition the {@linkplain ElementDefinition} definition of the returned {@linkplain ElementUsage}
     * @return an {@linkplain ElementUsage}
     */
    private ElementUsage CreateElementUsage(ElementDefinition container, Part part, ElementDefinition elementDefinition)
    {
        var newUsage = new ElementUsage();
        newUsage.setName(part.getName());
        newUsage.setShortName(part.getName());
        newUsage.setIid(UUID.randomUUID());
        newUsage.setOwner(this.hubController.GetCurrentDomainOfExpertise());
        newUsage.setElementDefinition(elementDefinition);

        var definition = new Definition();
        
        definition.setIid(UUID.randomUUID());
        definition.setContent(part.getId());
        definition.setLanguageCode(CIID);
        newUsage.getDefinition().add(definition);
        
        return newUsage;
    }
    
    /**
     * Gets an existing or creates an {@linkplain ElementDefinition} that will be mapped to the {@linkplain Component}
     * 
     * @param component the DST element {@linkplain Component}
     * @return an {@linkplain ElementDefinition}
     */
    private ElementDefinition GetOrCreateElementDefinition(Component component)
    {
        return this.GetOrCreateElementDefinition(component.getName());
    }

    /**
     * Gets an existing or creates an {@linkplain ElementDefinition} based on the provided {@linkplain shortName}
     *
     * @param name the DST element {@linkplain String} name
     * @return an {@linkplain ElementDefinition}
     */
    private ElementDefinition GetOrCreateElementDefinition(String name)
    {
        var shortName = GetShortName(name);
        
        Predicate<? super ElementDefinition> matcher = x -> AreTheseEquals(x.getShortName(), shortName, true) || AreTheseEquals(x.getName(), name);
        
        ElementDefinition elementDefinition = this.elements.stream()
                .filter(x -> x.GetHubElement() instanceof ElementDefinition)
                .map(x -> (ElementDefinition)x.GetHubElement())
                .filter(matcher)
                .findFirst()
                .orElse(this.hubController.GetOpenIteration()
                    .getElement()
                    .stream()
                    .filter(matcher)
                    .findFirst()
                    .map(x -> x.clone(false))
                    .orElse(null));
        
        if(elementDefinition == null)
        {
            elementDefinition = new ElementDefinition();
            elementDefinition.setIid(UUID.randomUUID());
            elementDefinition.setName(name);
            elementDefinition.setShortName(shortName);
            elementDefinition.setOwner(this.hubController.GetCurrentDomainOfExpertise());
            
            return elementDefinition;
        }

        elementDefinition.setName(name);
        elementDefinition.setShortName(shortName);

        return elementDefinition;
    }

    /**
     * Maps the attached ports of all the mapped {@linkplain Class}
     */
    private void MapPorts()
    {
        for (MappedElementDefinitionRowViewModel mappedElement : this.elements)
        {
            this.MapPorts(mappedElement);
        }
    }
    
    /**
     * Maps the attached ports of the {@linkplain Class} mapped in the specified {@linkplain MappedElementDefinitionRowViewModel}
     * 
     * @param mappedElement the {@linkplain MappedElementDefinitionRowViewModel}
     */
    private void MapPorts(MappedElementDefinitionRowViewModel mappedElement)
    {
        if(!mappedElement.DoesRepresentAnElementDefinitionComponentMapping())
        {
            return;
        }
        
        for (var port : ((Component)mappedElement.GetDstElement()).getContainedComponentPorts())
        {
            String portName = this.GetPortName(mappedElement, port);
            
            var portElementUsage = ((ElementDefinition)mappedElement.GetHubElement()).getContainedElement()
                    .stream()
                    .filter(x -> AreTheseEquals(x.getName(), portName))
                    .findFirst()
                    .map(x -> x.clone(false))
                    .orElseGet(() -> 
                        {    
                            ElementUsage elementUsage = new ElementUsage();
                            
                            elementUsage.setName(portName);
                            elementUsage.setShortName(GetShortName(portName));
                            elementUsage.setIid(UUID.randomUUID());
                            elementUsage.setOwner(this.hubController.GetCurrentDomainOfExpertise());
                            elementUsage.setElementDefinition(this.GetPortElementDefinition());
                            elementUsage.setInterfaceEnd(this.GetInterfaceEndForPort(port));
                                        
                            ((ElementDefinition)mappedElement.GetHubElement()).getContainedElement().add(elementUsage);
                            return elementUsage;
                        });
                       
            this.portsToConnect.add(Triple.of(port, mappedElement, portElementUsage));
        }
    }

    /**
     * Sets the interface end kind for the specified {@linkplain ElementUsage} based on the specified {@linkplain ComponentPort}
     * 
     * @param port the {@linkplain ComponentPort}
     * @return the corresponding {@linkplain InterfacEndKind}
     */
    private InterfaceEndKind GetInterfaceEndForPort(ComponentPort port)
    {
        switch (port.getOrientation())
        {
            case IN:
                return InterfaceEndKind.INPUT;
            case INOUT:
                return InterfaceEndKind.IN_OUT;
            case OUT:
                return InterfaceEndKind.OUTPUT;
            default:
                return InterfaceEndKind.UNDIRECTED;
        }
    }

    /**
     * Gets the {@linkplain ElementDefinition} that represents all ports 
     * 
     * @return the {@linkplain ElementDefinition} port
     */
    private ElementDefinition GetPortElementDefinition()
    {
        if(this.portElementDefinition != null)
        {
            return this.portElementDefinition;
        }
        
        this.portElementDefinition = this.GetOrCreateElementDefinition(PORTELEMENTDEFINITIONNAME);
        return this.portElementDefinition;
    }

    /**
     * Computes the port name
     * 
     * @param mappedElement the {@linkplain MappedElementDefinitionRowViewModel}
     * @param port the {@linkplain ComponentPort}
     * @return the port name as a string
     */
    private String GetPortName(MappedElementDefinitionRowViewModel mappedElement, ComponentPort port)
    {        
        if(!StringUtils.isBlank(port.getName()))
        {
            return port.getName();
        }
        
        if(port.getRequiredInterfaces().size() == 1)
        {
            return port.getRequiredInterfaces().get(0).getName();
        }
        
        long portNumber = ((ElementDefinition)mappedElement.GetHubElement()).getContainedElement().stream().filter(x -> x.getInterfaceEnd() != InterfaceEndKind.NONE).count();
        
        String nameAfterContainer = String.format("%s_port", mappedElement.GetHubElement().getName());
        
        if(portNumber > 0)
        {
            nameAfterContainer = String.format("%s%s", nameAfterContainer, portNumber);
        }
        
        return  nameAfterContainer;
    }
    
    /**
     * Maps the properties of the specified {@linkplain Component}
     * 
     * @param elementDefinition the {@linkplain ElementDefinition} that represents the Component
     * @param component the source {@linkplain Component}
     */
    private void MapProperties(ElementUsage elementUsage, ElementDefinition elementDefinition, Component component)
    {
        for (Property property : component.getContainedProperties())
        {
            var optionalParameterToOverride = elementDefinition.getParameter().stream()
                    .filter(x -> this.AreShortNamesEquals(x.getParameterType(), GetShortName(property)) 
                            || x.getParameterType().getName().compareToIgnoreCase(property.getName()) == 0)
                    .findAny();
            
            if(!optionalParameterToOverride.isPresent())
            {
                continue;
            }
            
            var optionalParameterOverride = elementUsage.getParameterOverride().stream()
                    .filter(x -> AreTheseEquals(x.getParameter().getIid(), optionalParameterToOverride.get().getIid()))
                    .findAny();
            
            Ref<String> refValue = new Ref<>(String.class, "");
            
            if(!this.TryGetValueFromProperty(property, refValue))
            {
                continue;
            }
            
            if(optionalParameterOverride.isPresent() && !this.DoesParameterRequiresToBeUpdated(property, optionalParameterOverride.get(), refValue))
            {
                continue;
            }
            
            ParameterOverride parameterOverride;
            
            if (optionalParameterOverride.isPresent())
            {
                parameterOverride = optionalParameterOverride.get().clone(true);
            }
            else
            {
                parameterOverride = new ParameterOverride(UUID.randomUUID(), null, null);
                parameterOverride.setOwner(this.hubController.GetCurrentDomainOfExpertise());
                parameterOverride.setParameter(optionalParameterToOverride.get());
            }
            
            var clonedParameterOverride = parameterOverride.clone(true);
            
            this.UpdateValueSet(clonedParameterOverride, refValue);
            elementUsage.getParameterOverride().removeIf(x -> AreTheseEquals(x.getIid(), clonedParameterOverride.getIid()));
            elementUsage.getParameterOverride().add(clonedParameterOverride);
        }
    }
    
    /**
     * Verifies that the provided parameter needs to be updated based on the provided {@linkplain Property} value
     * 
     * @param property the {@linkplain Property}
     * @param parameter the {@linkplain Parameter}
     * @param refValue the {@linkplain Ref} of {@linkplain String} that might hold the {@linkplain Property} value
     * @return an assert
     */
    private boolean DoesParameterRequiresToBeUpdated(Property property, ParameterOrOverrideBase parameter, Ref<String> refValue)
    {        
        var originalValue = ValueSetUtils.QueryParameterBaseValueSet(parameter, 
                parameter.isOptionDependent() ? this.hubController.GetOpenIteration().getDefaultOption() : null, 
                        parameter.getStateDependence() != null ? parameter.getStateDependence().getActualState().get(0) : null)
                .getManual().get(0);
        
        return !AreTheseEquals(refValue.Get(), originalValue);
    }

    /**
     * Maps the properties of the specified {@linkplain Component}
     * 
     * @param elementDefinition the {@linkplain ElementDefinition} that represents the Component
     * @param component the source {@linkplain Component}
     */
    @SuppressWarnings("resource")
    private void MapProperties(ElementDefinition elementDefinition, Component component)
    {
        for (Property property : component.getContainedProperties())
        {
            var existingParameter = elementDefinition.getParameter().stream()
                    .filter(x -> this.AreShortNamesEquals(x.getParameterType(), GetShortName(property)) 
                            || x.getParameterType().getName().compareToIgnoreCase(property.getName()) == 0)
                    .findAny();

            var refParameterType = new Ref<>(ParameterType.class);
            Parameter parameter = null;
            
            var refValue = new Ref<>(String.class, "");
            var hasValue = this.TryGetValueFromProperty(property, refValue);
            
            if(!existingParameter.isPresent() && hasValue)
            {
                if(this.TryCreateParameterType(property, refParameterType))
                {
                    parameter = new Parameter();
                    parameter.setIid(UUID.randomUUID());
                    parameter.setOwner(this.hubController.GetCurrentDomainOfExpertise());
                    parameter.setParameterType(refParameterType.Get());

                    if(refParameterType.Get() instanceof QuantityKind)
                    {
                        parameter.setScale(((QuantityKind)refParameterType.Get()).getDefaultScale());
                    }

                    parameter = parameter.clone(true);
                }
                else
                {
                    this.logger.error(String.format("Could not create ParameterType %s", property.getName()));
                    continue;
                }
            }
            else if (existingParameter.isPresent() && hasValue)
            {
                parameter = existingParameter.get().clone(true);
            }
            else
            {
                this.logger.error(String.format("Could not map property %s for element definition %s", property.getName(), elementDefinition.getName()));
                continue;
            }

            if(!existingParameter.isPresent() || (existingParameter.isPresent() && this.DoesParameterRequiresToBeUpdated(property, parameter, refValue)))
            {
                this.UpdateValueSet(parameter, refValue);
            
                var parameterIid = parameter.getIid();
                elementDefinition.getParameter().removeIf(x -> AreTheseEquals(x.getIid(), parameterIid));
                elementDefinition.getParameter().add(parameter);
            }
        }
        
        this.logger.info(String.format("ElementDefinition has %s parameters", elementDefinition.getParameter().size()));
    }
    
    /**
     * Tries to extract the value from the provided property and returns it as string
     * 
     * @return a value indicating whether the value has been extracted
     */
    private boolean TryGetValueFromProperty(Property property, Ref<String> refValue)
    {
        var valueSpecification = property.getOwnedDefaultValue();
        
        if(valueSpecification instanceof LiteralNumericValue)
        {
            refValue.Set(((LiteralNumericValue)valueSpecification).getValue());
        }
        else if(valueSpecification instanceof LiteralBooleanValue) 
        {
            refValue.Set(String.valueOf(((LiteralBooleanValue)valueSpecification).isValue()));
        }
        else if(valueSpecification instanceof LiteralStringValue) 
        {
            refValue.Set(((LiteralStringValue)valueSpecification).getValue());
        }
        
        return refValue.HasValue();
    }

    /**
     * Tries to create a {@linkplain ParameterType} if it doesn't exist yet in the chain of rdls
     * 
     * @param property the {@linkplain Property} to create the {@linkplain ParameterType} from
     * @param refParameterType the {@linkplain Ref} of {@linkplain ParameterType} 
     * @return a value indicating whether the {@linkplain ParameterType} has been successfully created or retrieved from the chain of rdls
     */
    private boolean TryCreateParameterType(Property property, Ref<ParameterType> refParameterType)
    {
        try
        {
            String shortName = GetShortName(property.getName());
            
            if(!this.hubController.TryGetThingFromChainOfRdlBy(x -> this.AreShortNamesEquals(x, shortName) 
                    || x.getName().compareToIgnoreCase(property.getName()) == 0, refParameterType))
            {
                ParameterType parameterType = null;
                
                if(property.getOwnedDefaultValue() instanceof LiteralNumericValue)
                {
                    parameterType = new SimpleQuantityKind();

                    SimpleQuantityKind quantityKind = ((SimpleQuantityKind)parameterType);
                    
                    Ref<MeasurementScale> refScale = new Ref<>(MeasurementScale.class);
                    
                    if(this.TryCreateOrGetMeasurementScale(property, refScale))
                    {
                        quantityKind.getAllPossibleScale().add(refScale.Get());
                        quantityKind.setDefaultScale(refScale.Get());
                    }
                }                
                else if(property.getOwnedDefaultValue() instanceof LiteralBooleanValue)
                {
                    parameterType = new BooleanParameterType();
                }
                else if(property.getOwnedDefaultValue() instanceof LiteralStringValue)
                {
                    parameterType = new TextParameterType();
                }                
                else
                {
                    this.logger.error(String.format("The property %s value type isn't supported by the adapter", property.getName()));
                }
                                
                if(parameterType != null)
                {
                    parameterType.setIid(UUID.randomUUID());
                    parameterType.setName(property.getName());
                    parameterType.setShortName(shortName);
                    parameterType.setSymbol(shortName.substring(0, 1));
                    
                    ReferenceDataLibrary referenceDataLibrary = this.hubController.GetDehpOrModelReferenceDataLibrary().clone(false);
                    referenceDataLibrary.getParameterType().add(parameterType);
                    return this.TryCreateReferenceDataLibraryThing(parameterType, referenceDataLibrary, refParameterType);
                }
                
                return false;
            }
            
            return true;
        }
        catch(Exception exception)
        {
            this.logger.error(String.format("Could not create the parameter type with the shortname: %s, because %s", property.getName(), exception));
            this.logger.catching(exception);
            return false;
        }
    }

    /**
     * Tries to create a new {@linkplain MeasurementScale} based on the provided {@linkplain valueSpecification}
     * 
     * @param property the typed property
     * @param refScale the {@linkplain Ref} of {@linkplain MeasurementScale} as out parameter
     * @return a {@linkplain boolean} indicating whether the {@linkplain refScale} is not null
     */
    @SuppressWarnings("resource")
    private boolean TryCreateOrGetMeasurementScale(Property property, Ref<MeasurementScale> refScale)
    {
        var unit = ((LiteralNumericValue)property.getOwnedDefaultValue()).getUnit();
        
        String unitName;
        String scaleShortName;
        
        if(unit == null || unit.getName() == null)
        {
            unitName = "1";
            scaleShortName = "-";
        }
        else
        {
            unitName = unit.getName();
            scaleShortName = GetShortName(unitName);
        }
        
        
        if(!this.hubController.TryGetThingFromChainOfRdlBy(x -> x.getShortName().equals(scaleShortName), refScale))
        {
            MeasurementScale newScale = new RatioScale();
            newScale.setName(unitName);
            newScale.setNumberSet(NumberSetKind.REAL_NUMBER_SET);
            
            Ref<MeasurementUnit> refMeasurementUnit = new Ref<>(MeasurementUnit.class);
            
            if(!this.TryCreateOrGetMeasurementUnit(unitName, refMeasurementUnit))
            {   
                return false;
            }
            
            newScale.setUnit(refMeasurementUnit.Get());
            newScale.setShortName(refMeasurementUnit.Get().getShortName());
            
            var referenceDataLibrary = this.hubController.GetDehpOrModelReferenceDataLibrary().clone(false);
            referenceDataLibrary.getScale().add(newScale);
            return this.TryCreateReferenceDataLibraryThing(newScale, referenceDataLibrary, refScale);
        }
        
        return true;
    }
    
    /**
     * Tries to create a new {@linkplain MeasurementScale} based on the provided {@linkplain valueSpecification}
     * or to retrieve it from the cache
     * 
     * @param unitName the unit name
     * @param refMeasurementUnit the {@linkplain Ref} of {@linkplain MeasurementUnit} as out parameter
     * @return a {@linkplain boolean} indicating whether the {@linkplain refMeasurementUnit} is not null
     */
    private boolean TryCreateOrGetMeasurementUnit(String unitName, Ref<MeasurementUnit> refMeasurementUnit)
    {
        String unitShortName = GetShortName(unitName);
        
        if(!this.hubController.TryGetThingFromChainOfRdlBy(x -> x.getShortName().equals(unitShortName) || x.getName().equals(unitName) || x.getShortName().equals("-"), refMeasurementUnit))
        {
            var newMeasurementUnit = new SimpleUnit();
            newMeasurementUnit.setName(unitName);
            newMeasurementUnit.setShortName(unitShortName);

            var referenceDataLibrary = this.hubController.GetDehpOrModelReferenceDataLibrary().clone(false);
            referenceDataLibrary.getUnit().add(newMeasurementUnit);
            return this.TryCreateReferenceDataLibraryThing(newMeasurementUnit, referenceDataLibrary, refMeasurementUnit);
        }
        
        return true;
    }
    
    /**
     * Updates the correct value set for the provided {@linkplain ParameterOverride}
     * 
     * @param parameter the {@linkplain ParameterOverride}
     * @param refValue the {@linkplain Ref} of {@linkplain String} holding the value to assign
     */
    private void UpdateValueSet(ParameterOverride parameter, Ref<String> refValue)
    {
        this.UpdateValueSet(parameter, ParameterOverride.class, ParameterOverrideValueSet.class, refValue);
    }
    
    /**
     * Updates the correct value set for the provided {@linkplain Parameter}
     * 
     * @param parameter the {@linkplain Parameter}
     * @param refValue the {@linkplain Ref} of {@linkplain String} holding the value to assign
     */
    private void UpdateValueSet(Parameter parameter, Ref<String> refValue)
    {
        this.UpdateValueSet(parameter, Parameter.class, ParameterValueSet.class, refValue);
    }
    
    /**
     * Updates the correct value set for the provided {@linkplain Parameter}
     * 
     * @param <TParameter> the type of {@linkplain ParameterOrOverrideBase}
     * @param <TValueSet> the type of {@linkplain ParameterValueSetBase}
     * @param parameter the {@linkplain #TParameter} to update
     * @param parameterTypeClass the {@linkplain Class} of {@linkplain #TParameter}
     * @param valueSetTypeClass the {@linkplain Class} of {@linkplain #TValueSet}
     * @param refValue the {@linkplain Ref} of {@linkplain String} holding the value to assign
     */
    @SuppressWarnings("unchecked")
    private <TParameter extends ParameterOrOverrideBase, TValueSet extends ParameterValueSetBase> void UpdateValueSet(TParameter parameter, 
            Class<TParameter> parameterTypeClass, Class<TValueSet> valueSetTypeClass, Ref<String> refValue)
    {
        TValueSet valueSet = null;
               
        if(!parameter.getValueSets().isEmpty())
        {
            valueSet = (TValueSet) ValueSetUtils.QueryParameterBaseValueSet(parameter, 
                    parameter.isOptionDependent() ? this.hubController.GetOpenIteration().getDefaultOption() : null, 
                            parameter.getStateDependence() != null ? parameter.getStateDependence().getActualState().get(0) : null);
        }
        else
        {
            try
            {
                valueSet = valueSetTypeClass.getConstructor().newInstance();
                
                if(valueSet instanceof ParameterOverrideValueSet)
                {
                    ((ParameterOverrideValueSet)valueSet).setParameterValueSet((ParameterValueSet)ValueSetUtils.QueryParameterBaseValueSet(
                            ((ParameterOverride)parameter).getParameter(),
                            parameter.isOptionDependent() ? this.hubController.GetOpenIteration().getDefaultOption() : null, 
                            parameter.getStateDependence() != null ? parameter.getStateDependence().getActualState().get(0) : null));
                }
                
                valueSet.setIid(UUID.randomUUID());
                valueSet.setReference(new ValueArray<>(Arrays.asList(""), String.class));
                valueSet.setFormula(new ValueArray<>(Arrays.asList(""), String.class));
                valueSet.setPublished(new ValueArray<>(Arrays.asList(""), String.class));
                valueSet.setComputed(new ValueArray<>(Arrays.asList(""), String.class));
                valueSet.setValueSwitch(ParameterSwitchKind.MANUAL);
                
                if(Parameter.class.isAssignableFrom(parameterTypeClass))
                {
                    ((Parameter)parameter).getValueSet().add((ParameterValueSet) valueSet);
                }
                else
                {
                    ((ParameterOverride)parameter).getValueSet().add((ParameterOverrideValueSet) valueSet);
                }
            }
            catch (Exception exception)
            {
                this.logger.catching(exception);
            }
        }

        ValueArray<String> newValue = new ValueArray<>(Arrays.asList(refValue.Get()), String.class);
        
        valueSet.setManual(newValue);
        valueSet.setValueSwitch(ParameterSwitchKind.MANUAL);
    }
}
