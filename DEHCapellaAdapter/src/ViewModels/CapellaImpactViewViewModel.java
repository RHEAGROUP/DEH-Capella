/*
 * CapellaImpactViewViewModel.java
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
package ViewModels;

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.eclipse.emf.ecore.EObject;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.cs.BlockArchitecture;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.la.LogicalComponentPkg;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.data.pa.PhysicalComponentPkg;
import org.polarsys.capella.core.data.requirement.Requirement;
import org.polarsys.capella.core.data.requirement.RequirementsPkg;

import DstController.IDstController;
import Enumerations.CapellaArchitecture;
import Enumerations.MappingDirection;
import Reactive.ObservableCollection;
import Services.CapellaSession.ICapellaSessionService;
import Services.CapellaTransaction.ICapellaTransactionService;
import Utils.Ref;
import ViewModels.CapellaObjectBrowser.CapellaObjectBrowserTreeRowViewModel;
import ViewModels.CapellaObjectBrowser.CapellaObjectBrowserTreeViewModel;
import ViewModels.CapellaObjectBrowser.CapellaObjectBrowserViewModel;
import ViewModels.CapellaObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.CapellaObjectBrowser.Rows.ElementRowViewModel;
import ViewModels.CapellaObjectBrowser.Rows.RootRowViewModel;
import ViewModels.Interfaces.ICapellaImpactViewViewModel;
import ViewModels.Interfaces.IHaveTargetArchitecture;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.Iteration;
import io.reactivex.Observable;

/**
 * The {@linkplain CapellaImpactViewViewModel} is the main view model for the requirement impact view in the impact view panel
 */
public class CapellaImpactViewViewModel extends CapellaObjectBrowserViewModel implements ICapellaImpactViewViewModel
{
    /**
     * The {@linkplain IDstController}
     */
    private final IDstController dstController;
    
    /**
     * The {@linkplain ICapellaTransactionService}
     */
    private final ICapellaTransactionService transactionService;

    /**
     * Initializes a new {@linkplain RequirementImpactViewViewModel}
     * 
     * @param dstController the {@linkplain IDstController}
     * @param sessionService the {@linkplain ICapellaSessionService}
     * @param transactionService the {@linkplain ICapellaTransactionService}
     */
    public CapellaImpactViewViewModel(IDstController dstController, ICapellaSessionService sessionService, ICapellaTransactionService transactionService)
    {
        super(sessionService);
        this.dstController = dstController;
        this.transactionService = transactionService;

        this.InitializesObservables();
        this.UpdateBrowserTrees(this.SessionService.HasAnyOpenSession());
    }

    /**
     * Initializes the needed subscription on {@linkplain Observable}
     */
    private void InitializesObservables()
    {
        this.dstController.HasAnyOpenSessionObservable()
            .subscribe(this::UpdateBrowserTrees, this.logger::catching);
                        
        this.dstController.GetHubMapResult()
            .ItemsAdded()
            .subscribe(x -> this.UpdateBrowserTrees(this.SessionService.HasAnyOpenSession()), this.logger::catching);
        
        this.dstController.GetHubMapResult()
            .IsEmptyObservable()
            .subscribe(isEmpty ->
            {
                if(isEmpty)
                {
                    this.UpdateBrowserTrees(this.SessionService.HasAnyOpenSession());
                }
            });

        this.dstController.GetSelectedHubMapResultForTransfer()
            .ItemsAdded()
            .subscribe(x ->
            {
                for(var thing : x)
                {
                    this.SwitchIsSelected(thing, true);
                }
                
                this.shouldRefreshTree.Value(true);
            });

        this.dstController.GetSelectedHubMapResultForTransfer()
            .ItemRemoved()
            .subscribe(x -> 
            {
                this.SwitchIsSelected(x, false);
                this.shouldRefreshTree.Value(true);
            });
        
        this.dstController.GetSelectedHubMapResultForTransfer()
            .IsEmptyObservable()
            .subscribe(x -> 
            {
                this.DeselectRow((RootRowViewModel)this.browserTreeModel.Value().getRoot());
                this.shouldRefreshTree.Value(true);
            });
    }

    /**
     * De-selects every row in the tree
     * 
     * @param rowViewModel the current row view model to de-select
     */
    @SuppressWarnings("unchecked")
    private void DeselectRow(IElementRowViewModel<?> rowViewModel)
    {
        this.SwitchIsSelected(rowViewModel, false);
        
        if(rowViewModel instanceof IHaveContainedRows)
        {
            for (var childRowViewModel : ((IHaveContainedRows<? extends IElementRowViewModel<?>>)rowViewModel).GetContainedRows())
            {
                this.DeselectRow(childRowViewModel);
            }
        }
    }

    /**
     * Sets is selected property on the row view model that represents the provided {@linkplain CapellaElement}
     * 
     * @param element The {@linkplain CapellaElement} to find the corresponding row view model
     * @param shouldSelect A value indicating whether the row view model should set as selected
     */
    private void SwitchIsSelected(CapellaElement element, boolean shouldSelect)
    {
        var refRowViewModel = new Ref<ElementRowViewModel<? extends CapellaElement>>(null);
        
        if(this.TryGetRowViewModelById(((RootRowViewModel) this.browserTreeModel.Value().getRoot()).GetContainedRows(), element, refRowViewModel))
        {        
            this.SwitchIsSelected(refRowViewModel.Get(), shouldSelect);
        }
    }

    /**
     * Sets is selected property on the row view model 
     * 
     * @param shouldSelect a value indicating whether the row view model should set as selected
     * @param rowViewModel the row view model to select or de-select
     */
    private void SwitchIsSelected(IElementRowViewModel<? extends CapellaElement> rowViewModel, boolean shouldSelect)
    {
        if(!rowViewModel.GetIsSelected() && shouldSelect)
        {
            rowViewModel.SetIsSelected(true);
        }
        else if(rowViewModel.GetIsSelected() && !shouldSelect)
        {
            rowViewModel.SetIsSelected(false);            
        }
    }

    /**
     * Computes the difference for the provided {@linkplain Thing}
     * 
     * @return a {@linkplain RootRowViewModel}
     */
    protected RootRowViewModel ComputeDifferences()
    {
        var model = this.browserTreeModel.Value();
        
        if(model == null)
        {
            return null;
        }
        
        var rootRowViewModel = (RootRowViewModel) model.getRoot();
        
        try
        {
            for (var mappedElementRowViewModel : this.dstController.GetHubMapResult())
            {
                var refRowViewModel = new Ref<ElementRowViewModel<? extends CapellaElement>>(null);
                
                if(this.TryGetRowViewModelById(rootRowViewModel.GetContainedRows(), mappedElementRowViewModel.GetDstElement(), refRowViewModel)
                        && this.VerifyArchitecture(mappedElementRowViewModel, refRowViewModel.Get().GetElement()))
                {
                    refRowViewModel.Get().UpdateElement(mappedElementRowViewModel.GetDstElement(), true);
                    continue;
                }
                
                var refCapellaElementParentToUpdate = new Ref<>(CapellaElement.class);
                
                if(this.TryToFindParent(rootRowViewModel.GetContainedRows(), mappedElementRowViewModel.GetDstElement().eContainer(), refRowViewModel, refCapellaElementParentToUpdate)
                        && this.VerifyArchitecture(mappedElementRowViewModel, refCapellaElementParentToUpdate.Get()))
                {
                    refRowViewModel.Get().UpdateElement(refCapellaElementParentToUpdate.Get(), true);
                }
                else
                {
                    this.ComputeDifferences(rootRowViewModel, mappedElementRowViewModel);
                }                
            }
        }
        catch(Exception exception)
        {
            this.logger.catching(exception);
        }
            
        return rootRowViewModel;
    }

    /**
     * Computes the differences when none of the modifiable parent are contained in the tree already
     * 
     * @param rootRowViewModel the {@linkplain RootRowViewModel}
     * @param mappedElementRowViewModel the {@linkplain MappedElementRowViewModel}
     */
    @SuppressWarnings("unchecked")
    private void ComputeDifferences(RootRowViewModel rootRowViewModel, MappedElementRowViewModel<? extends Thing, ? extends CapellaElement> mappedElementRowViewModel)
    {
        var parent = this.FindUncontainedParent(mappedElementRowViewModel.GetDstElement());
        var refRowViewModel = new Ref<ElementRowViewModel<? extends CapellaElement>>(null);
        
        if(mappedElementRowViewModel.GetDstElement() instanceof Requirement 
                && this.TryGetRowViewModelOfType(rootRowViewModel.GetContainedRows(), 
                        ((IHaveTargetArchitecture)mappedElementRowViewModel).GetTargetArchitecture().GetArchitectureClass(), refRowViewModel))
        {
            var architectureRowViewModel = this.transactionService.Clone((BlockArchitecture)refRowViewModel.Get().GetElement());
            architectureRowViewModel.getOwnedRequirementPkgs().removeIf(x -> AreTheseEquals(x.getId(), parent.getId()));
            architectureRowViewModel.getOwnedRequirementPkgs().add((RequirementsPkg)parent);
            refRowViewModel.Get().UpdateElement(architectureRowViewModel, true);
        }
        else if(mappedElementRowViewModel.GetDstElement() instanceof PhysicalComponent 
                && this.TryGetRowViewModelOfType(rootRowViewModel.GetContainedRows(), PhysicalComponentPkg.class, refRowViewModel))
        {
            var rootPhysicalElementRowViewModel = ((IHaveContainedRows<? extends ElementRowViewModel<?>>)refRowViewModel.Get()).GetContainedRows().get(0);
            var rootElement = this.transactionService.Clone((PhysicalComponent)rootPhysicalElementRowViewModel.GetElement());
            rootElement.getOwnedPhysicalComponents().removeIf(x -> AreTheseEquals(x.getId(), parent.getId()));

            if(!AreTheseEquals(rootElement.getId(), parent.getId()))
            {
                if(parent instanceof PhysicalComponent)
                {
                    rootElement.getOwnedPhysicalComponents().add((PhysicalComponent)parent);
                }
                else if(parent instanceof PhysicalComponentPkg)
                {
                    rootElement.getOwnedPhysicalComponentPkgs().add((PhysicalComponentPkg)parent);
                }
                
                rootPhysicalElementRowViewModel.UpdateElement(rootElement, true);
            }
        }
        else if(mappedElementRowViewModel.GetDstElement() instanceof LogicalComponent 
                && this.TryGetRowViewModelOfType(rootRowViewModel.GetContainedRows(), LogicalComponentPkg.class, refRowViewModel))
        {
            var rootLogicalElementRowViewModel = ((IHaveContainedRows<? extends ElementRowViewModel<?>>)refRowViewModel.Get()).GetContainedRows().get(0);
            var rootElement = this.transactionService.Clone((LogicalComponent)rootLogicalElementRowViewModel.GetElement());
            rootElement.getOwnedLogicalComponents().removeIf(x -> AreTheseEquals(x.getId(), parent.getId()));
            
            if(!AreTheseEquals(rootElement.getId(), parent.getId()))
            {
                rootElement.getOwnedLogicalComponents().add((LogicalComponent)parent);
                rootLogicalElementRowViewModel.UpdateElement(rootElement, true);
            }            
        }
    }

    /**
     * Verifies that the target {@linkplain CapellaElement} is contained in the {@linkplain CapellaArchitecture} specified by the provided {@linkplain MappedElementRowViewModel} 
     * 
     * @param mappedElementRowViewModel the {@linkplain MappedElementRowViewModel}
     * @param targetElement the {@linkplain CapellaElement} to update
     * @return a {@linkplain boolean} value
     */
    private boolean VerifyArchitecture(MappedElementRowViewModel<? extends Thing, ? extends CapellaElement> mappedElementRowViewModel, CapellaElement targetElement)
    {
        if(!(mappedElementRowViewModel instanceof IHaveTargetArchitecture))
        {
            return false;
        }
        
        var sourceArchitecture = CapellaArchitecture.From(this.transactionService.GetOriginal(targetElement));
        var targetArchitecture = ((IHaveTargetArchitecture)mappedElementRowViewModel).GetTargetArchitecture();
        return (sourceArchitecture != null ? sourceArchitecture : targetArchitecture)  == targetArchitecture;
    }

    /**
     * Finds the parent of the provided {@linkplain CapellaElement} where its container is null
     * 
     * @param element the {@linkplain CapellaElement} from which to get the parent
     * @return a {@linkplain CapellaElement}
     */
    private CapellaElement FindUncontainedParent(CapellaElement element)
    {
        EObject parent = element;
        var previousParent = (CapellaElement)parent;
        
        while(parent != null && parent instanceof CapellaElement)
        {
            previousParent = (CapellaElement)parent;
            parent = parent.eContainer();
        }
        
        return previousParent;
    }

    /**
     * Tries to get the parent row view model
     * 
     * @param containedRows the root children row view models 
     * @param parent the direct parent
     * @param refRowViewModel the {@linkplain Ref} of {@linkplain ElementRowViewModel} that can contain the searched parent row view model
     * @param refContainer the {@linkplain Ref} of {@linkplain CapellaElement} that can contain the {@linkplain CapellaElement} parent in case it is not the provided parent
     * @return a value indicating the parent row view model has been found
     */
    private boolean TryToFindParent(ObservableCollection<IElementRowViewModel<? extends CapellaElement>> containedRows,
            EObject parent, Ref<ElementRowViewModel<? extends CapellaElement>> refRowViewModel, Ref<CapellaElement> refContainer)
    {
        var container = parent;
        
        while(container != null && container instanceof CapellaElement && !TryGetRowViewModelById(containedRows, container, refRowViewModel))
        {
            container = container.eContainer();
        }
        
        refContainer.Set((CapellaElement)container);
        
        return refRowViewModel.HasValue();
    }

    /**
     * Gets the {@linkplain Thing} by its Iid from the capella sessions
     * 
     * @param childrenCollection the {@linkplain Collection} collection from the parent row view model
     * @param element the {@linkplain EObject} represented by the searched {@linkplain TElementRowViewModel}
     * @param refElement the {@linkplain Ref} of {@linkplain TElementRowViewModel} as ref parameter
     */
    private  boolean TryGetRowViewModelOfType(Collection<IElementRowViewModel<? extends CapellaElement>> childrenCollection, 
            Class<? extends CapellaElement> clazz, Ref<ElementRowViewModel<? extends CapellaElement>> refElement)
    {
        Predicate<IElementRowViewModel<? extends CapellaElement>> a  = x -> clazz.isAssignableFrom(x.GetElement().getClass());
        return this.TryGetRowViewModelBy(childrenCollection, a, refElement);
    }
    
    /**
     * Gets the {@linkplain Thing} by its Iid from the capella sessions
     * 
     * @param childrenCollection the {@linkplain Collection} collection from the parent row view model
     * @param element the {@linkplain EObject} represented by the searched {@linkplain TElementRowViewModel}
     * @param refElement the {@linkplain Ref} of {@linkplain TElementRowViewModel} as ref parameter
     */
    private boolean TryGetRowViewModelById(Collection<IElementRowViewModel<? extends CapellaElement>> childrenCollection, 
            EObject element, Ref<ElementRowViewModel<? extends CapellaElement>> refElement)
    {
        return this.TryGetRowViewModelBy(childrenCollection, x -> AreTheseEquals(x.GetElement().getId(), ((CapellaElement)element).getId()), refElement);
    }
        
    /**
     * Gets the {@linkplain Thing} by its Iid from the capella sessions
     * 
     * @param childrenCollection the {@linkplain Collection} collection from the parent row view model
     * @param predicate the {@linkplain Predicate} that test the view models against the specified check
     * @param refElement the {@linkplain Ref} of {@linkplain TElementRowViewModel} as ref parameter
     */
    @SuppressWarnings("unchecked")
    private boolean TryGetRowViewModelBy(Collection<IElementRowViewModel<? extends CapellaElement>> childrenCollection, 
            Predicate<IElementRowViewModel<? extends CapellaElement>> predicate, Ref<ElementRowViewModel<? extends CapellaElement>> refElement)
    {
        if(childrenCollection == null || childrenCollection.isEmpty())
        {
            return false;
        }

        for (var childRowViewModel : childrenCollection)
        {
            if (childRowViewModel.GetElement() != null && predicate.test(childRowViewModel))
            {
                refElement.Set((ElementRowViewModel<? extends CapellaElement>)childRowViewModel);
                break;
            }
            
            if(childRowViewModel instanceof IHaveContainedRows)
            {
                if(this.TryGetRowViewModelBy(((IHaveContainedRows<IElementRowViewModel<? extends CapellaElement>>)childRowViewModel).GetContainedRows(), predicate, refElement))
                {
                    break;
                }
            }
        }
        
        return refElement.HasValue();
    }
    
    /**
     * Updates this view model {@linkplain TreeModel}
     * 
     * @param isConnected a value indicating whether the session is open
     */
    @Override
    protected void UpdateBrowserTrees(Boolean isConnected)
    {
        if(this.browserTreeModel.Value() == null || this.dstController.GetHubMapResult().isEmpty())
        {
            this.SetOutlineModel(DefaultOutlineModel.createOutlineModel(
                    new CapellaObjectBrowserTreeViewModel(this.SessionService.GetModels()), new CapellaObjectBrowserTreeRowViewModel(), true));
        }
        if(isConnected || !this.dstController.GetHubMapResult().isEmpty())
        {
            var treeModel = new CapellaObjectBrowserTreeViewModel(this.ComputeDifferences());                        
            this.SetOutlineModel(DefaultOutlineModel.createOutlineModel(treeModel, new CapellaObjectBrowserTreeRowViewModel(), true));
        }
    
        this.isTheTreeVisible.Value(isConnected);
    }
    
    /**
     * Updates the {@linkplain browserTreeModel} based on the provided {@linkplain Iteration}
     * 
     * @param iteration the {@linkplain Iteration}
     */
    protected void SetOutlineModel(OutlineModel model)
    {
        this.UpdateHighlightOnRows(model);
        this.browserTreeModel.Value(model);
        
        if(model.getChildCount(model.getRoot()) == 1)
        {
            var child = model.getChild(model.getRoot(), 0);
            var path = new TreePath(model.getRoot()).pathByAddingChild(child);
            model.getTreePathSupport().expandPath(path);
        }        
    }

    /**
     * Updates the <code>IsHighlighted</code> property on each row of the specified model
     * 
     * @param model the {@linkplain OutlineModel}
     */
    private void UpdateHighlightOnRows(OutlineModel model)
    {
        var rowViewModel = model.getRoot();
        
        if(rowViewModel instanceof IHaveContainedRows)
        {
            this.UpdateHiglightOnRows((IElementRowViewModel<?>)rowViewModel);
        }
    }

    /**
     * Updates the <code>IsHighlighted</code> property on each row of the specified model
     * 
     * @param rowViewModel the {@linkplain IElementRowViewModel}
     */
    @SuppressWarnings("unchecked")
    private void UpdateHiglightOnRows(IElementRowViewModel<?> rowViewModel)
    {
        if(rowViewModel instanceof IHaveContainedRows)
        {
            for (var childRow : ((IHaveContainedRows<IElementRowViewModel<?>>)rowViewModel).GetContainedRows())
            {
                if(childRow.GetElement() == null)
                {
                    continue;
                }
                
                boolean isHighlighted = this.dstController.GetHubMapResult().stream()
                        .anyMatch(r -> AreTheseEquals(r.GetDstElement().getId(), childRow.GetElement().getId()))
                        || this.transactionService.IsClonedOrNew(childRow.GetElement());
                
                childRow.SetIsHighlighted(isHighlighted);
                
                this.UpdateHiglightOnRows(childRow);
            }
        }
    }

    /**
     * Compute eligible rows where the represented {@linkplain Thing} can be transfered,
     * and return the filtered collection for feedback application on the tree
     * 
     * @param selectedRow the selected view model {@linkplain ElementRowViewModel}
     */
    @Override
    public void OnSelectionChanged(ElementRowViewModel<? extends CapellaElement> selectedRow) 
    {
        this.AddOrRemoveSelectedRowToTransfer(selectedRow);
    }

    /**
     * Adds or remove the {@linkplain Thing} to/from the relevant collection depending on the {@linkplain MappingDirection}
     * 
     * @param rowViewModel the {@linkplain ElementRowViewModel} that contains the element to add or remove
     */
    private void AddOrRemoveSelectedRowToTransfer(IElementRowViewModel<?> rowViewModel)
    {
        if(rowViewModel.SwitchIsSelectedValue())
        {
            this.AddOrRemoveSelectedRowToTransfer(rowViewModel, row -> 
            {
                if(this.dstController.GetSelectedHubMapResultForTransfer().stream().noneMatch(x -> x == row.GetElement()))
                {
                    this.dstController.GetSelectedHubMapResultForTransfer().add(row.GetElement());
                }
                
                return true;
            }, new Ref<>(Boolean.class));
        }
        else
        {
            this.AddOrRemoveSelectedRowToTransfer(rowViewModel, 
                    row -> !this.dstController.GetSelectedHubMapResultForTransfer().removeIf(x -> x == row.GetElement()), new Ref<>(Boolean.class));
        }
    }
    
    /**
     * Adds or delete the {@linkplain Thing} to/from the relevant collection depending on the {@linkplain MappingDirection}
     * 
     * @param rowViewModel the {@linkplain ElementRowViewModel} that contains the element to add or remove
     * @param addOrRemoveFunction the {@linkplain Consumer} to call/accept on each row, that allows this method to either add or delete to/from the relevant collection
     */
    @SuppressWarnings("unchecked")
    private void AddOrRemoveSelectedRowToTransfer(IElementRowViewModel<?> rowViewModel, Function<IElementRowViewModel<?>, Boolean> addOrRemoveFunction, Ref<Boolean> refCanBeSelectedOrDeselected)
    {
        if(rowViewModel instanceof IHaveContainedRows)
        {
            for (var childRow : ((IHaveContainedRows<IElementRowViewModel<?>>)rowViewModel).GetContainedRows())
            {
                this.AddOrRemoveSelectedRowToTransfer(childRow, addOrRemoveFunction, refCanBeSelectedOrDeselected);
            }
        }
        
        if(rowViewModel != null && rowViewModel.GetElement() != null && this.dstController.GetHubMapResult().stream()
                .anyMatch(r -> AreTheseEquals(r.GetDstElement().getId(), rowViewModel.GetElement().getId())))
        {
            refCanBeSelectedOrDeselected.Set(addOrRemoveFunction.apply(rowViewModel));
        }
        
        if(refCanBeSelectedOrDeselected.HasValue())
        {
            rowViewModel.SetIsSelected(refCanBeSelectedOrDeselected.Get());
        }
        else
        {
            rowViewModel.SetIsSelected(false);
        }
    }
}
