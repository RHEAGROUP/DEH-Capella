/*
 * CapellaTracesToBinaryRelationship.java
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

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Predicate;

import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.capellacore.Trace;
import org.polarsys.capella.core.data.cs.Component;

import App.AppContainer;
import DstController.IDstController;
import HubController.IHubController;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Services.MappingEngineService.MappingRule;
import Utils.Operators.Operators;
import Utils.Stereotypes.CapellaComponentCollection;
import Utils.Stereotypes.CapellaTracedElementCollection;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.BinaryRelationship;
import cdp4common.engineeringmodeldata.ElementDefinition;

/**
 * The {@linkplain CapellaTracesToBinaryRelationship} is the {@linkplain MappingRule} that maps {@linkplain Trace}s to {@linkplain BinaryRelationShip}
 */
public class CapellaTracesToBinaryRelationship extends DstToHubBaseMappingRule<CapellaTracedElementCollection, ArrayList<BinaryRelationship>>
{
    /**
     * The result of this mapping rule
     */
    private ArrayList<BinaryRelationship> result = new ArrayList<>();
    
    /**
     * The {@linkplain IDstController}
     */
    IDstController dstController;

    /**
     * Initializes a new {@linkplain ComponentToElementMappingRule}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain ICapellaMappingConfigurationService}
     */
    public CapellaTracesToBinaryRelationship(IHubController hubController, ICapellaMappingConfigurationService mappingConfiguration)
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
    public ArrayList<BinaryRelationship> Transform(Object input)
    {
        try
        {
            if(this.dstController == null)
            {
                this.dstController = AppContainer.Container.getComponent(IDstController.class);
            }
            
            this.Map(this.CastInput(input));
            return new ArrayList<>(this.result);
        }
        catch (Exception exception)
        {
            this.logger.catching(exception);
            return new ArrayList<>();
        }
        finally
        {
            this.result.clear();
        }
    }

    /**
     * Maps the provided collection of  {@linkplain MappedElementRowViewModel}
     * 
     * @param mappedElementDefinitions the collection of {@linkplain MappedElementRowViewModel} to map
     */
    private void Map(CapellaTracedElementCollection elements)
    {
        for (var sourceElement : elements)
        {
            for (var outgoingTrace : sourceElement.GetDstElement().getOutgoingTraces())
            {
                var optionalTargetElement = elements.stream()
                        .filter(x -> outgoingTrace.getTargetElement() != null && Operators.AreTheseEquals(x.GetDstElement().getId(), outgoingTrace.getTargetElement().getId()))
                        .findFirst();
                
                if(!optionalTargetElement.isPresent())
                {
                    continue;
                }
                
                if(this.DoesRelationshipAlreadyExists(sourceElement.GetHubElement(), optionalTargetElement.get().GetHubElement()))
                {
                    continue;
                }
                                
                this.result.add(this.CreateBinaryRelationship(sourceElement, optionalTargetElement.get()));
            }
        }
    }

    /**
     * Create a {@linkplain BinaryRelationship}
     * 
     * @param sourceMappedElement the {@linkplain MappedElementRowViewModel} source
     * @param targetMappedElement the {@linkplain MappedElementRowViewModel} target
     * @return a {@linkplain BinaryRelationship}
     */
    private BinaryRelationship CreateBinaryRelationship(MappedElementRowViewModel<? extends Thing, ? extends CapellaElement> sourceMappedElement, 
            MappedElementRowViewModel<? extends Thing, ? extends CapellaElement> targetMappedElement)
    {
        var relationship = new BinaryRelationship();
        relationship.setOwner(this.hubController.GetCurrentDomainOfExpertise());
        relationship.setIid(UUID.randomUUID());
        relationship.setName(String.format("%s → %s", this.GetCapelleElementName(sourceMappedElement), this.GetCapelleElementName(targetMappedElement)));
        relationship.setSource(sourceMappedElement.GetHubElement());
        relationship.setTarget(targetMappedElement.GetHubElement());
        return relationship;
    }

    /**
     * Gets the name of the provided {@linkplain CapellaElement} from the {@linkplain MappedElementRowViewModel}
     * 
     * @param mappedElement the {@linkplain MappedElementRowViewModel}
     */
    private String GetCapelleElementName(
            MappedElementRowViewModel<? extends Thing, ? extends CapellaElement> mappedElement)
    {
        return mappedElement.GetDstElement() instanceof NamedElement 
                ? ((NamedElement)mappedElement.GetDstElement()).getName()
                : mappedElement.GetDstElement().getId();
    }

    /**
     * Verifies that no relationship already exists between the provided {@linkplain CapellaElement} source and the {@linkplain CapellaElement} target in the Hub
     * 
     * @param source the {@linkplain Thing} source
     * @param target the {@linkplain Thing} target
     * @return a {@linkplain boolean}
     */
    private boolean DoesRelationshipAlreadyExists(Thing source, Thing target)
    {
        Predicate<? super BinaryRelationship> predicate = x -> Operators.AreTheseEquals(x.getTarget().getIid(), target.getIid()) && 
                Operators.AreTheseEquals(x.getSource().getIid(), source.getIid());
        
        return this.hubController.GetOpenIteration().getRelationship().stream()
                    .filter(x -> x instanceof BinaryRelationship)
                    .map(x -> (BinaryRelationship)x)
                    .anyMatch(predicate) 
                || this.dstController.GetMappedTracesToBinaryRelationships().stream().anyMatch(predicate);
    }    
}
