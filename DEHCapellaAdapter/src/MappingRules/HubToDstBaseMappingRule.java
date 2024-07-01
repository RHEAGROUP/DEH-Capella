/*
 * HubToDstBaseMappingRule.java
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

import DstController.IDstController;
import HubController.IHubController;
import Services.CapellaTransaction.ICapellaTransactionService;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;

/**
 * The HubToDstBaseMappingRule is the mapping rule for rules that maps from the HUB to DST
 *  
 * @param <TInput> the input type the rule will process
 * @param <TOutput> the output type the rule will return
 */
public abstract class HubToDstBaseMappingRule<TInput extends Object, TOutput> extends CapellaBaseMappingRule<TInput, TOutput>
{
    /**
     * The {@linkplain IDstController} instance
     */
    IDstController dstController;
    
    /**
     * The {@linkplain ICapellaTransactionService} instance
     */
    final ICapellaTransactionService transactionService;
    
    /**
     * Initializes a new {@linkplain DstToHubBaseMappingRule}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain IMagicDrawMappingConfigurationService}
     * @param transactionService the {@linkplain ICapellaTransactionService}
     */
    protected HubToDstBaseMappingRule(IHubController hubController, ICapellaMappingConfigurationService mappingConfiguration, ICapellaTransactionService transactionService)
    {
        super(hubController, mappingConfiguration);
        this.transactionService = transactionService;
    }    
}
