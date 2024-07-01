/*
 * BinaryRelationshipToCapellaTraces.java
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
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.polarsys.capella.core.data.capellacommon.GenericTrace;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.Trace;
import org.polarsys.capella.core.data.cs.Component;

import App.AppContainer;
import DstController.IDstController;
import HubController.IHubController;
import Services.CapellaTransaction.ICapellaTransactionService;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Services.MappingEngineService.MappingRule;
import Utils.Operators.Operators;
import Utils.Stereotypes.CapellaComponentCollection;
import Utils.Stereotypes.HubRelationshipElementsCollection;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.BinaryRelationship;
import cdp4common.engineeringmodeldata.ElementDefinition;

/**
 * The {@linkplain BinaryRelationshipToCapellaTraces} is the {@linkplain MappingRule} that maps {@linkplain Trace}s to {@linkplain BinaryRelationShip}
 */
public class BinaryRelationshipToCapellaTraces extends HubToDstBaseMappingRule<HubRelationshipElementsCollection, ArrayList<Trace>>
{
    /**
     * The result of this mapping rule
     */
    private ArrayList<Trace> result = new ArrayList<>();

    /**
     * The {@linkplain IDstController}
     */
    IDstController dstController;
    
    /**
     * Initializes a new {@linkplain ComponentToElementMappingRule}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain ICapellaMappingConfigurationService}
     * @param transactionService the {@linkplain ICapellaTransactionService}
     */
    public BinaryRelationshipToCapellaTraces(IHubController hubController, ICapellaMappingConfigurationService mappingConfiguration, ICapellaTransactionService transactionService)
    {
        super(hubController, mappingConfiguration, transactionService);
    }
    
    /**
     * Transforms an {@linkplain CapellaComponentCollection} of {@linkplain Component} to an {@linkplain ArrayList} of {@linkplain ElementDefinition}
     * 
     * @param input the {@linkplain CapellaComponentCollection} of {@linkplain Component} to transform
     * @return the {@linkplain ArrayList} of {@linkplain MappedElementDefinitionRowViewModel}
     */
    @Override
    public ArrayList<Trace> Transform(Object input)
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
     * Maps the provided collection of {@linkplain MappedElementRowViewModel}
     * 
     * @param mappedElementDefinitions the collection of {@linkplain MappedElementRowViewModel} to map
     */
    private void Map(HubRelationshipElementsCollection elements)
    {
        for (var relationshipAndPairs : this.GetMappableBinaryRelationships(elements).entrySet())
        {
            if(this.DoesThisRelationshipAlreadyExist(relationshipAndPairs))
            {
                continue;
            }
            
            this.result.add(this.CreateTrace(relationshipAndPairs));
        }
    }

    /**
     * Creates a new Capella {@linkplain Trace}
     * 
     * @param relationshipAndPairs a {@linkplain HashMap} of {@linkplain BinaryRelationship} and a {@linkplain Pair} of {@linkplain MappedElementRowViewModel}
     * @return a new {@linkplain Trace}
     */
    private Trace CreateTrace(Entry<BinaryRelationship, 
            Pair<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>, MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>>> relationshipAndPairs)
    {
        var newTrace = this.transactionService.Create(GenericTrace.class);
        newTrace.setSummary(relationshipAndPairs.getKey().getName());
        
        var sourceElement = relationshipAndPairs.getValue().getLeft().GetDstElement();
        
        newTrace.setSourceElement(this.transactionService.IsCloned(sourceElement) 
                ? this.transactionService.GetClone(sourceElement).GetOriginal()
                : sourceElement);

        var targetElement = relationshipAndPairs.getValue().getRight().GetDstElement();
        
        newTrace.setTargetElement(this.transactionService.IsCloned(targetElement) 
                ? this.transactionService.GetClone(targetElement).GetOriginal()
                : targetElement);
                
        return newTrace;
    }

    /**
     * Verifies that the trace already exist in the capella model
     * 
     * @param relationshipAndPairs a {@linkplain HashMap} of {@linkplain BinaryRelationship} and a {@linkplain Pair} of {@linkplain MappedElementRowViewModel}
     * @return an assert
     */
    private boolean DoesThisRelationshipAlreadyExist(
            Entry<BinaryRelationship, Pair<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>, MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>>> relationshipAndPairs)
    {
        var result = relationshipAndPairs.getValue().getLeft().GetDstElement()
                    .getOutgoingTraces().stream()
                    .anyMatch(x -> Operators.AreTheseEquals(x.getTargetElement().getId(), relationshipAndPairs.getValue().getRight().GetDstElement().getId()))
                || this.dstController.GetMappedBinaryRelationshipsToTraces().stream().anyMatch(x -> 
                        x.getTargetElement() != null && x.getSourceElement() != null 
                        && Operators.AreTheseEquals(x.getSourceElement().getId(), relationshipAndPairs.getValue().getLeft().GetDstElement().getId())
                        && Operators.AreTheseEquals(x.getTargetElement().getId(), relationshipAndPairs.getValue().getRight().GetDstElement().getId()));
        
        return result;
    }

    /**
     * Gets the mappable {@linkplain BinaryRelationship} and its target and source {@linkplain MappedElementRowViewModel}
     * 
     * @param elements the {@linkplain HubRelationshipElementsCollection}
     * @return a {@linkplain HashMap} of {@linkplain BinaryRelationship} and a {@linkplain Pair} of {@linkplain MappedElementRowViewModel}
     */
    private HashMap<BinaryRelationship, Pair<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>, MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>>> 
        GetMappableBinaryRelationships(HubRelationshipElementsCollection elements)
    {
        var relatedThings = new HashMap<BinaryRelationship, 
                Pair<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>, MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>>>();
        
        for (var mappedElementRowViewModel : elements.stream().filter(x -> x.GetHubElement() != null).collect(Collectors.toList()))
        {
            for(var relationship : mappedElementRowViewModel.GetHubElement().getRelationships().stream()
                    .filter(x -> x instanceof BinaryRelationship)
                    .map(x -> (BinaryRelationship)x).collect(Collectors.toList()))
            {
                var isTarget = Operators.AreTheseEquals(relationship.getTarget().getIid(), mappedElementRowViewModel.GetHubElement().getIid());
                
                var otherElement = elements.stream().filter(x -> isTarget 
                        ? Operators.AreTheseEquals(x.GetHubElement().getIid(), relationship.getSource().getIid())
                        : Operators.AreTheseEquals(x.GetHubElement().getIid(), relationship.getTarget().getIid()))
                .findFirst();
                
                if(otherElement.isPresent())
                {
                    if(isTarget)
                    {
                        relatedThings.put(relationship, Pair.of(otherElement.get(), mappedElementRowViewModel));
                    }
                    else
                    {
                        relatedThings.put(relationship, Pair.of(mappedElementRowViewModel, otherElement.get()));  
                    }
                }
            }
        }
        
        return relatedThings;
    }
}
