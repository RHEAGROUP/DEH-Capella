/*
 * RequirementImpactViewViewModel.java
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

import java.util.Optional;

import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;

import DstController.IDstController;
import HubController.IHubController;
import Utils.Ref;
import ViewModels.Interfaces.IRequirementImpactViewViewModel;
import ViewModels.ObjectBrowser.Interfaces.IThingRowViewModel;
import ViewModels.ObjectBrowser.RequirementTree.RequirementBrowserTreeRowViewModel;
import ViewModels.ObjectBrowser.RequirementTree.RequirementBrowserTreeViewModel;
import ViewModels.ObjectBrowser.RequirementTree.Rows.IterationRequirementRowViewModel;
import ViewModels.ObjectBrowser.RequirementTree.Rows.RequirementRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.RequirementsSpecification;

/**
 * The {@linkplain RequirementImpactViewViewModel} is the main view model for the requirement impact view in the impact view panel
 */
public class RequirementImpactViewViewModel extends ImpactViewBaseViewModel<cdp4common.engineeringmodeldata.Requirement> implements IRequirementImpactViewViewModel
{
    /**
     * Initializes a new {@linkplain RequirementImpactViewViewModel}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param dstController the {@linkplain IDstController}
     */
    public RequirementImpactViewViewModel(IHubController hubController, IDstController dstController)
    {
        super(hubController, dstController, cdp4common.engineeringmodeldata.Requirement.class);
    }

    /**
     * Computes the difference for the provided {@linkplain Thing}
     * 
     * @param iteration the cloned iteration for display purpose
     * @param thing the current {@linkplain Thing} thing
     */
    @Override
    protected void ComputeDifferences(Iteration iteration, cdp4common.engineeringmodeldata.Requirement thing)
    {
        try
        {
            if(iteration.getRequirementsSpecification().stream().noneMatch(x -> this.DoTheseThingsRepresentTheSameThing(x, thing.getContainerOfType(RequirementsSpecification.class))))
            {
                iteration.getRequirementsSpecification().add(thing.getContainerOfType(RequirementsSpecification.class));
            }
            else
            {
                Ref<Integer> index = new Ref<>(Integer.class, null);
                
                iteration.getRequirementsSpecification()
                        .stream()
                        .filter(x -> this.DoTheseThingsRepresentTheSameThing(x, thing.getContainerOfType(RequirementsSpecification.class)))
                        .findFirst()
                        .ifPresent(x -> index.Set(iteration.getRequirementsSpecification().indexOf(x)));
                
                if(index.HasValue() && iteration.getRequirementsSpecification().remove(index.Get().intValue()) != null)
                {
                    iteration.getRequirementsSpecification().add(index.Get(), thing.getContainerOfType(RequirementsSpecification.class));
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
            new RequirementBrowserTreeViewModel(iteration),
            new RequirementBrowserTreeRowViewModel(), true);
    }

    /**
     * Gets the {@linkplain IThingRowViewModel} that represent the {@linkplain Thing}
     * 
     * @param thing the {@linkplain ElementDefinition} 
     * @return the {@linkplain IThingRowViewModel} of {@linkplain ElementDefinition}
     */
    @Override
    protected IThingRowViewModel<cdp4common.engineeringmodeldata.Requirement> GetRowViewModelFromThing(cdp4common.engineeringmodeldata.Requirement thing)
    {
        IterationRequirementRowViewModel iterationRowViewModel = (IterationRequirementRowViewModel) this.browserTreeModel.Value().getRoot();
        
        Optional<RequirementRowViewModel> optionalDefinition = iterationRowViewModel.GetContainedRows().stream()
                .flatMap(x -> x.GetAllContainedRowsOfType(RequirementRowViewModel.class).stream())
                .filter(x -> x.GetThing().getIid().equals(thing.getIid()))
                .findFirst();
        
        if(optionalDefinition.isPresent())
        {
            return optionalDefinition.get();
        }
        
        return null;
    }
}
