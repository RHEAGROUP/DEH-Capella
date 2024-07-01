/*
 * CapellaBaseMappingRule.java
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

import java.util.Collection;
import java.util.stream.Collectors;

import org.polarsys.capella.core.data.capellacore.CapellaElement;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Services.MappingEngineService.MappingRule;
import ViewModels.Interfaces.IHaveTargetArchitecture;
import ViewModels.Rows.MappedElementRowViewModel;

/**
 * The CapellaBaseMappingRule is the base mapping rule for all the Capella adapter rules
 *  
 * @param <TInput> the input type the rule will process
 * @param <TOutput> the output type the rule will return
 */
public abstract class CapellaBaseMappingRule<TInput extends Object, TOutput> extends MappingRule<TInput, TOutput>
{
    /**
     * The {@linkplain IHubController}
     */
    protected final IHubController hubController;

    /**
     * The {@linkplain IMagicDrawMappingConfigurationService} instance
     */
    protected final ICapellaMappingConfigurationService mappingConfiguration;
    
    /**
     * Initializes a new {@linkplain DstToHubBaseMappingRule}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain IMagicDrawMappingConfigurationService}
     */
    protected CapellaBaseMappingRule(IHubController hubController, ICapellaMappingConfigurationService mappingConfiguration)
    {
        this.hubController = hubController;
        this.mappingConfiguration = mappingConfiguration;        
    }
    
    /**
     * Saves the mapping configuration
     * 
     * @param elements the {@linkplain Collection} of {@linkplain MappedElementRowViewModel} from which the DST element extends {@linkplain CapellaElement}
     * @param mappingDirection the {@linkplain MappingDirection} that applies to the provided mapped element 
     */
    protected void SaveMappingConfiguration(Collection<? extends MappedElementRowViewModel<?, ? extends CapellaElement>> elements, MappingDirection mappingDirection)
    {
        for (var element : elements.stream().filter(x -> x.GetHubElement() != null && x.GetDstElement() != null).collect(Collectors.toList()))
        {
            if(element instanceof IHaveTargetArchitecture)
            {
                this.mappingConfiguration.AddToExternalIdentifierMap(
                        element.GetHubElement().getIid(), element.GetDstElement().getId(), 
                        ((IHaveTargetArchitecture)element).GetTargetArchitecture(), mappingDirection);
            }
            else
            {
                this.mappingConfiguration.AddToExternalIdentifierMap(
                        element.GetHubElement().getIid(), element.GetDstElement().getId(), mappingDirection);
            }
        }
    }
}
