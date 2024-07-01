/*
 * DstToHubBaseMappingRule.java
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

import static Utils.Operators.Operators.AreTheseEquals;
import static Utils.Stereotypes.StereotypeUtils.GetShortName;

import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;

import DstController.IDstController;
import HubController.IHubController;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Utils.Ref;
import cdp4common.commondata.ClassKind;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.sitedirectorydata.CategorizableThing;
import cdp4common.sitedirectorydata.Category;
import cdp4common.sitedirectorydata.ReferenceDataLibrary;
import cdp4common.types.ContainerList;
import cdp4dal.operations.ThingTransactionImpl;
import cdp4dal.operations.TransactionContextResolver;

/**
 * The DstToHubBaseMappingRule is the mapping rule for rules that maps from the DST to the HUB
 *  
 * @param <TInput> the input type the rule will process
 * @param <TOutput> the output type the rule will return
 */
public abstract class DstToHubBaseMappingRule<TInput extends Object, TOutput> extends CapellaBaseMappingRule<TInput, TOutput>
{
    /**
     * Initializes a new {@linkplain DstToHubBaseMappingRule}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain ICapellaMappingConfigurationService}
     */
    protected DstToHubBaseMappingRule(IHubController hubController, ICapellaMappingConfigurationService mappingConfiguration)
    {
        super(hubController, mappingConfiguration);
    }
    
    /**
     * Maps the specified by short name {@linkplain Category} to the provided {@linkplain ElementDefinition}
     * 
     * @param thing the {@linkplain ElementDefinition} to update
     * @param categoryName the {@linkplain String} name of the category
     * @param params of permissive classes
     */
    protected void MapCategory(CategorizableThing thing, String categoryName, ClassKind... permissibleClass)
    {
        try
        {
            Ref<Category> refCategory = new Ref<>(Category.class);
            var categoryShortName = GetShortName(categoryName);
            
            if(!(this.hubController.TryGetThingFromChainOfRdlBy(x -> AreTheseEquals(x.getShortName(),categoryShortName), refCategory))
                    && !this.TryCreateCategory(Pair.of(categoryShortName, categoryName), refCategory, permissibleClass))
            {
                return;
            }
    
            if(refCategory.HasValue())
            {
                    if(thing.getCategory().stream()
                                .filter(x -> AreTheseEquals(x.getShortName(), categoryShortName))
                                .findAny().isEmpty())
                    {
                        thing.getCategory().add(refCategory.Get());
                    }
            }
            else
            {
                this.logger.debug(String.format("The Category %s could not be found or created", categoryShortName));
            }
        }
        catch(Exception exception)
        {
            this.logger.catching(exception);
        }
    }
    
    /**
     * Tries to create the category with the specified {@linkplain categoryShortName}
     * 
     * @param categoryNames the {@linkplain Pair} of short name and name
     * @param refCategory the {@linkplain Ref} of Category
     * @param params of permissive classes
     * 
     * @return a value indicating whether the category has been successfully created and retrieved from the cache
     */
    private boolean TryCreateCategory(Pair<String, String> categoryNames, Ref<Category> refCategory, ClassKind... permissibleClass)
    {
        var newCategory = new Category();
        newCategory.setName(categoryNames.getRight());
        newCategory.setShortName(categoryNames.getLeft());
        newCategory.setIid(UUID.randomUUID());
        newCategory.getPermissibleClass().addAll(Arrays.asList(permissibleClass)); 

        ReferenceDataLibrary rdl = this.hubController.GetDehpOrModelReferenceDataLibrary().clone(false);
        rdl.getDefinedCategory().add(newCategory);
        
        return TryCreateReferenceDataLibraryThing(newCategory, rdl, refCategory);        
    }
    
    /**
     * Tries to add the specified {@linkplain newThing} to the provided {@linkplain ContainerList} and retrieved the new reference from the cache after save
     * 
     * @param <TThing> the type of {@linkplain Thing}
     * @param newThing the new {@linkplain Thing}
     * @param clonedReferenceDataLibrary the cloned {@linkplain ReferenceDataLibrary} where the {@linkplain newThing} is contained
     * @param refThing the {@linkplain Ref} acting as an out parameter here
     * @return a value indicating whether the {@linkplain newThing} has been successfully created and retrieved from the cache
     */
    protected <TThing extends Thing> boolean TryCreateReferenceDataLibraryThing(TThing newThing, ReferenceDataLibrary clonedReferenceDataLibrary, Ref<TThing> refThing)
    {
        try
        {
            var transaction = new ThingTransactionImpl(TransactionContextResolver.resolveContext(clonedReferenceDataLibrary), clonedReferenceDataLibrary);
            transaction.createOrUpdate(clonedReferenceDataLibrary);
            transaction.createOrUpdate(newThing);
            
            this.hubController.Write(transaction);
            this.hubController.RefreshReferenceDataLibrary(clonedReferenceDataLibrary);
            
            return this.hubController.TryGetThingFromChainOfRdlBy(x -> x.getIid().compareTo(newThing.getIid()) == 0, refThing);
        }
        catch(Exception exception)
        {
            this.logger.error(String.format("Could not create the %s because %s", newThing.getClassKind(), exception));
            this.logger.catching(exception);
            return false;
        }
    }
}
