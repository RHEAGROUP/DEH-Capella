/*
 * ElementDefinitionImpactViewViewModel.java
 *
 * Copyright (c) 2020-2024 Starion Group S.A.
 *
 * Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski 
 *
 * This file is part of DEH-MDSYSML
 *
 * The DEH-MDSYSML is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * The DEH-MDSYSML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package ViewModels;

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.stream.Collectors;

import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;

import DstController.IDstController;
import HubController.IHubController;
import Utils.Ref;
import ViewModels.Interfaces.IElementDefinitionImpactViewViewModel;
import ViewModels.ObjectBrowser.ElementDefinitionTree.ElementDefinitionBrowserTreeRowViewModel;
import ViewModels.ObjectBrowser.ElementDefinitionTree.ElementDefinitionBrowserTreeViewModel;
import ViewModels.ObjectBrowser.ElementDefinitionTree.Rows.IterationElementDefinitionRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IThingRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Iteration;

/**
 * The {@linkplain RequirementImpactViewViewModel} is the main view model for the requirement impact view in the impact view panel
 */
public class ElementDefinitionImpactViewViewModel extends ImpactViewBaseViewModel<ElementDefinition> implements IElementDefinitionImpactViewViewModel
{
    /**
     * Initializes a new {@linkplain ElementDefinitionImpactViewViewModel}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param dstController the {@linkplain IDstController}
     */
    public ElementDefinitionImpactViewViewModel(IHubController hubController, IDstController dstController)
    {
        super(hubController, dstController, ElementDefinition.class);    
    }

    /**
     * Computes the difference for the provided {@linkplain Thing}
     * 
     * @param iteration the cloned iteration for display purpose
     * @param thing the current {@linkplain Thing} thing
     */
    @Override
    protected void ComputeDifferences(Iteration iteration, ElementDefinition thing)
    {
        try
        {            
            if(iteration.getElement().stream().noneMatch(x -> this.DoTheseThingsRepresentTheSameThing(x, thing)))
            {            
                iteration.getElement().add(thing);
            }
            else
            {
                Ref<Integer> index = new Ref<>(Integer.class, null);
                
                iteration.getElement()
                        .stream()
                        .filter(x -> this.DoTheseThingsRepresentTheSameThing(x, thing))
                        .findFirst()
                        .ifPresent(x -> index.Set(iteration.getElement().indexOf(x)));
                
                if(index.HasValue() && iteration.getElement().removeIf(x -> this.DoTheseThingsRepresentTheSameThing(thing, x)))
                {
                    iteration.getElement().add(index.Get(), thing);
                }
            }
        }
        catch(Exception exception)
        {
            this.logger.catching(exception);
        }
    }    

    /**
     * Creates a new {@linkplain OutlineModel} based on the provided {@linkplain Iteration}
     * 
     * @param iteration the {@linkplain Iteration}
     */
    @Override
    protected OutlineModel CreateNewModel(Iteration iteration)
    {
        return DefaultOutlineModel.createOutlineModel(
                new ElementDefinitionBrowserTreeViewModel(iteration), 
                new ElementDefinitionBrowserTreeRowViewModel(), true);
    }
    
    /**
     * Gets the {@linkplain IThingRowViewModel} that represent the {@linkplain Thing}
     * 
     * @param thing the {@linkplain ElementDefinition} 
     * @return the {@linkplain IThingRowViewModel} of {@linkplain ElementDefinition}
     */
    @Override
    protected IThingRowViewModel<ElementDefinition> GetRowViewModelFromThing(ElementDefinition thing)
    {
        IterationElementDefinitionRowViewModel iterationRowViewModel = (IterationElementDefinitionRowViewModel) this.browserTreeModel.Value().getRoot();
        
        return iterationRowViewModel.GetContainedRows().stream()
            .filter(x -> AreTheseEquals(thing.getIid(), x.GetThing().getIid()))
            .findFirst()
            .orElse(null);
    }
}
