/*
 * CapellaMappingListViewViewModel.java
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
package ViewModels.MappingListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.swing.tree.TreeModel;

import org.apache.commons.lang3.tuple.Triple;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.polarsys.capella.core.data.capellacore.NamedElement;

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.CapellaTransaction.ICapellaTransactionService;
import Utils.StreamExtensions;
import ViewModels.MappingListView.Interfaces.IMappingListViewViewModel;
import ViewModels.Rows.MappedDstRequirementRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedHubRequirementRowViewModel;
import Views.CapellaMappingListView;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.Thing;

/**
 * The {@linkplain CapellaMappingListViewViewModel} is the main view model for the {@linkplain CapellaMappingListView}
 */
public class CapellaMappingListViewViewModel extends MappingListViewViewModel<IDstController> implements IMappingListViewViewModel
{
    /**
     * The {@linkplain ICapellaTransactionService}
     */
    private ICapellaTransactionService transactionService;

    /**
     * Initializes a new {@linkplain CapellaMappingListViewViewModel}
     * 
     * @param dstController the {@linkplain IDstController}
     * @param hubController the {@linkplain IHubController}
     * @param transactionService the {@linkplain ICapellaTransactionService}
     */
    public CapellaMappingListViewViewModel(IDstController dstController, IHubController hubController, ICapellaTransactionService transactionService)
    {
        super(dstController, hubController);
        this.transactionService = transactionService;
        this.InitializeObservable();
    }

    /**
     * Updates this view model {@linkplain TreeModel}
     * 
     * @param shouldDisplayTree a value indicating whether the tree should be made visible
     */
    @Override
    protected void UpdateBrowserTrees(Boolean shouldDisplayTree)
    {
        this.isTheTreeVisible.Value(shouldDisplayTree);
        
        if(shouldDisplayTree)
        {
            this.browserTreeModel.Value(DefaultOutlineModel.createOutlineModel(
                    new MappingListViewTreeViewModel<NamedElement>(this.SortMappedElements()), 
                    new MappingListViewTreeRowViewModel<NamedElement>(NamedElement.class), true));
        }
    }

    /**
     * Sorts the mapped elements
     * 
     * @return a {@linkplain Collection} of {@linkplain Triple} of the {@linkplain #TDstElement} the {@linkplain MappingDirection} and the {@linkplain Thing}
     */
    private Collection<Triple<? extends NamedElement, MappingDirection, ? extends Thing>> SortMappedElements()
    {
        var allElements = new ArrayList<MappedElementRowViewModel<? extends DefinedThing, ? extends NamedElement>>();
        
        allElements.addAll(StreamExtensions.OfType(this.dstController.GetHubMapResult().stream(), MappedElementDefinitionRowViewModel.class)
                .filter(x -> x.DoesRepresentAnElementDefinitionComponentMapping())
                .collect(Collectors.toList()));
        
        allElements.addAll(StreamExtensions.OfType(this.dstController.GetHubMapResult(), MappedHubRequirementRowViewModel.class));
        
        allElements.addAll(StreamExtensions.OfType(this.dstController.GetDstMapResult().stream(), MappedElementDefinitionRowViewModel.class)
                .filter(x -> x.DoesRepresentAnElementDefinitionComponentMapping())
                .collect(Collectors.toList()));
        
        allElements.addAll(StreamExtensions.OfType(this.dstController.GetDstMapResult(), MappedDstRequirementRowViewModel.class));
        
        var result = new ArrayList<Triple<? extends NamedElement, MappingDirection, ? extends Thing>>();
        
        for (var mappedElement : allElements)
        {
            if(mappedElement.GetMappingDirection() == MappingDirection.FromDstToHub)
            {
                result.add(Triple.of(mappedElement.GetDstElement(), mappedElement.GetMappingDirection(), 
                        mappedElement.GetHubElement().getOriginal() == null 
                            ? mappedElement.GetHubElement() 
                            : mappedElement.GetHubElement().getOriginal()));
            }
            else
            {
                result.add(Triple.of(this.transactionService.IsCloned(mappedElement.GetDstElement())
                        ? this.transactionService.GetClone(mappedElement.GetDstElement()).GetOriginal()
                        : mappedElement.GetDstElement(), mappedElement.GetMappingDirection(), 
                        mappedElement.GetHubElement()));
            }
        }
        
        return result;
    }
}
