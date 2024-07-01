/*
 * DstController.java
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
package DstController;

import static Utils.Operators.Operators.AreTheseEquals;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.EnumerationPropertyType;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.capellacore.Namespace;
import org.polarsys.capella.core.data.capellacore.Trace;
import org.polarsys.capella.core.data.capellacore.TypedElement;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.Interface;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.information.datatype.DataType;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.data.pa.PhysicalComponentPkg;
import org.polarsys.capella.core.data.pa.deployment.PartDeploymentLink;
import org.polarsys.capella.core.data.requirement.Requirement;
import org.polarsys.capella.core.data.requirement.RequirementsPkg;
import org.polarsys.capella.core.model.helpers.BlockArchitectureExt;

import Enumerations.CapellaArchitecture;
import Enumerations.MappingDirection;
import HubController.IHubController;
import MappingRules.ComponentToElementMappingRule;
import MappingRules.ElementToComponentMappingRule;
import Reactive.ObservableCollection;
import Reactive.ObservableValue;
import Services.CapellaLog.ICapellaLogService;
import Services.CapellaSession.ICapellaSessionService;
import Services.CapellaTransaction.ClonedReferenceElement;
import Services.CapellaTransaction.ICapellaTransactionService;
import Services.CapellaUserPreference.ICapellaUserPreferenceService;
import Services.CapellaUserPreference.UserPreferenceKey;
import Services.HistoryService.ICapellaLocalExchangeHistoryService;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;import Services.MappingConfiguration.IMappingConfigurationService;
import Services.MappingEngineService.IMappableThingCollection;
import Services.MappingEngineService.IMappingEngineService;
import Services.NavigationService.INavigationService;
import Utils.Ref;
import Utils.Stereotypes.CapellaComponentCollection;
import Utils.Stereotypes.CapellaRequirementCollection;
import Utils.Stereotypes.CapellaTracedElementCollection;
import Utils.Stereotypes.HubElementCollection;
import Utils.Stereotypes.HubRelationshipElementsCollection;
import Utils.Stereotypes.HubRequirementCollection;
import ViewModels.Interfaces.IMappedElementRowViewModel;
import ViewModels.Rows.MappedDstRequirementRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedHubRequirementRowViewModel;
import ViewModels.Rows.MappedRequirementBaseRowViewModel;
import Views.Dialogs.AlertMoreThanOneCapellaModelOpenDialog;
import cdp4common.ChangeKind;
import cdp4common.commondata.ClassKind;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.Definition;
import cdp4common.commondata.NamedThing;
import cdp4common.commondata.ShortNamedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.BinaryRelationship;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ElementUsage;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.Parameter;
import cdp4common.engineeringmodeldata.ParameterOrOverrideBase;
import cdp4common.engineeringmodeldata.ParameterOverride;
import cdp4common.engineeringmodeldata.ParameterValueSetBase;
import cdp4common.engineeringmodeldata.Relationship;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.engineeringmodeldata.ValueSet;
import cdp4common.sitedirectorydata.EnumerationParameterType;
import cdp4common.sitedirectorydata.MeasurementScale;
import cdp4common.types.ContainerList;
import cdp4dal.exceptions.TransactionException;
import cdp4dal.operations.ThingTransaction;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * The {@linkplain DstController} is a class that manage transfer and connection to attached running instance of Capella
 */
public final class DstController implements IDstController
{
    /**
     * Gets this running DST adapter name
     */
    public static final String THISTOOLNAME = "DEH-CAPELLA";

    /**
     * The current class Logger
     */
    private final Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain IMappingEngine} instance
     */
    private final IMappingEngineService mappingEngine;

    /**
     * The {@linkplain IHubController} instance
     */
    private final IHubController hubController;

    /**
     * The {@linkplain ICapellaLogService} instance
     */
    private final ICapellaLogService logService;

    /**
     * The {@linkplain IMappingConfigurationService} instance
     */
    private final ICapellaMappingConfigurationService mappingConfigurationService;
    
    /**
     * The {@linkplain ICapellaSessionService} instance 
     */
    private final ICapellaSessionService capellaSessionService;

    /**
     * The {@linkplain ICapellaTransactionService} instance
     */
    private final ICapellaTransactionService transactionService;
    
    /**
     * The {@linkplain ICapellaLocalExchangeHistoryService} instance
     */
    private final ICapellaLocalExchangeHistoryService exchangeHistory;

    /**
     * The {@linkplain INavigationService} instance
     */
    private final INavigationService navigationService;

    /**
     * The {@linkplain ICapellaUserPreferenceService} instance
     */
    private final ICapellaUserPreferenceService userPreferenceService;

    /**
     * A value indicating whether the {@linkplain DstController} should load mapping when the HUB session is refresh or reloaded
     */
    private boolean isHubSessionRefreshSilent;

    /**
     * The private collection of mapped {@linkplain BinaryRelationship} to {@linkplain Traces}
     */
    private ObservableCollection<Trace> mappedBinaryRelationshipsToTraces = new ObservableCollection<>();
    
    /**
     * Gets the {@linkplain ObservableCollection} of mapped {@linkplain Trace}s
     * 
     * @return a {@linkplain ObservableCollection} of mapped {@linkplain Trace}s
     */
    @Override
    public ObservableCollection<Trace> GetMappedBinaryRelationshipsToTraces()
    {
        return this.mappedBinaryRelationshipsToTraces;
    }
    
    /**
     * The private collection of mapped {@linkplain Traces} to  {@linkplain BinaryRelationship}
     */
    private ObservableCollection<BinaryRelationship> mappedTracesToBinaryRelationships = new ObservableCollection<>();

    /**
     * Gets the {@linkplain ObservableCollection} of mapped {@linkplain BinaryRelationship}s
     * 
     * @return a {@linkplain ObservableCollection} of mapped {@linkplain BinaryRelationship}s
     */
    @Override
    public ObservableCollection<BinaryRelationship> GetMappedTracesToBinaryRelationships()
    {
        return this.mappedTracesToBinaryRelationships;
    }
    
    /**
     * Backing field for {@linkplain GetDstMapResult}
     */
    private ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>> hubMapResult = new ObservableCollection<>();
    
    /**
     * Gets The {@linkplain ObservableCollection} of Hub map result
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain Class}
     */
    @Override
    public ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>> GetHubMapResult()
    {
        return this.hubMapResult;
    }
    
    /**
     * Backing field for {@linkplain GetDstMapResult}
     */
    private ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>> dstMapResult = new ObservableCollection<>();

    /**
     * Gets The {@linkplain ObservableCollection} of DST map result
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain MappedElementRowViewModel}
     */
    @Override
    public ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>> GetDstMapResult()
    {
        return this.dstMapResult;
    }
    
    /**
     * Backing field for {@linkplain GetSelectedHubMapResultForTransfer}
     */    
    private ObservableCollection<CapellaElement> selectedHubMapResultForTransfer = new ObservableCollection<>();
    
    /**
     * Gets the {@linkplain ObservableCollection} of that are selected for transfer to the Capella
     * 
     * @return an {@linkplain ObservableCollection} {@linkplain CapellaElement}
     */
    @Override
    public ObservableCollection<CapellaElement> GetSelectedHubMapResultForTransfer()
    {
        return this.selectedHubMapResultForTransfer;
    }
    
    /**
     * Backing field for {@linkplain GetSelectedDstMapResultForTransfer}
     */
    private ObservableCollection<Thing> selectedDstMapResultForTransfer = new ObservableCollection<>(Thing.class);
    
    /**
     * Gets the {@linkplain ObservableCollection} of {@linkplain Thing} that are selected for transfer to the Hub
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain Thing}
     */
    @Override
    public ObservableCollection<Thing> GetSelectedDstMapResultForTransfer()
    {
        return this.selectedDstMapResultForTransfer;
    }
    
    /**
     * Backing field for {@linkplain GeMappingDirection}
     */
    private ObservableValue<MappingDirection> currentMappingDirection = new ObservableValue<>(MappingDirection.FromDstToHub, MappingDirection.class);

    /**
     * Gets the {@linkplain Observable} of {@linkplain MappingDirection} from {@linkplain currentMappingDirection}
     * 
     * @return a {@linkplain Observable} of {@linkplain MappingDirection}
     */
    @Override
    public Observable<MappingDirection> GetMappingDirection()
    {
        return this.currentMappingDirection.Observable();
    }

    /**
     * Gets the current {@linkplain MappingDirection} from {@linkplain currentMappingDirection}
     * 
     * @return the {@linkplain MappingDirection}
     */
    @Override
    public MappingDirection CurrentMappingDirection()
    {
        return this.currentMappingDirection.Value();
    }

    /**
     * Switches the {@linkplain MappingDirection}
     * 
     * @return the new {@linkplain MappingDirection}
     */
    @Override
    public MappingDirection ChangeMappingDirection()
    {
        this.currentMappingDirection.Value(
                this.currentMappingDirection.Value() == MappingDirection.FromDstToHub 
                ? MappingDirection.FromHubToDst
                : MappingDirection.FromDstToHub);
        
        return this.currentMappingDirection.Value();
    }
    
    /**
     * Gets an {@linkplain Observable} of value indicating whether there is any session open in Capella
     * 
     * @return {@linkplain Observable} of {@linkplain Boolean} 
     */
    @Override
    public Observable<Boolean> HasAnyOpenSessionObservable()
    {
        return this.capellaSessionService.HasAnyOpenSessionObservable();
    }
    
    /**
     * Gets a value indicating whether there is any session open in Capella
     * 
     * @return a {@linkplain Boolean} 
     */
    @Override
    public boolean HasAnyOpenSession()
    {
        return this.capellaSessionService.HasAnyOpenSession();
    }
        
    /**
     * Initializes a new {@linkplain DstController}
     * 
     * @param mappingEngine the {@linkplain IMappingEngine} instance
     * @param hubController the {@linkplain IHubController} instance
     * @param logService the {@linkplain ICapellaLogService} instance
     * @param mappingConfigurationService the {@linkplain ICapellaMappingConfigurationService} instance
     * @param capellaSessionService the {@linkplain ICapellaSessionService} instance
     * @param transactionService the {@linkplain ICapellaTransactionService} instance
     * @param exchangeHistory the {@linkplain ICapellaLocalExchangeHistoryService} instance
     * @param userPreferenceService the {@linkplain CapellaUserPreferenceService} instance
     * @param navigationService the {@linkplain INavigationService} instance
     */
    public DstController(IMappingEngineService mappingEngine, IHubController hubController, ICapellaLogService logService, 
            ICapellaMappingConfigurationService mappingConfigurationService, ICapellaSessionService capellaSessionService,
            ICapellaTransactionService transactionService, ICapellaLocalExchangeHistoryService exchangeHistory,
            ICapellaUserPreferenceService userPreferenceService, INavigationService navigationService)
    {
        this.mappingEngine = mappingEngine;
        this.hubController = hubController;
        this.logService = logService;
        this.mappingConfigurationService = mappingConfigurationService;
        this.capellaSessionService = capellaSessionService;
        this.transactionService = transactionService;
        this.exchangeHistory = exchangeHistory;
        this.userPreferenceService = userPreferenceService;
        this.navigationService = navigationService;
        
        this.hubController.GetIsSessionOpenObservable().subscribe(isSessionOpen ->
        {
            if(!isSessionOpen)
            {
                this.hubMapResult.clear();
                this.dstMapResult.clear();
                this.mappedTracesToBinaryRelationships.clear();
                this.mappedBinaryRelationshipsToTraces.clear();
                this.selectedDstMapResultForTransfer.clear();
                this.selectedHubMapResultForTransfer.clear();
            }
        });
        
        this.capellaSessionService.SessionUpdated()
            .subscribe(x -> this.LoadMapping());
        
        this.hubController.GetSessionEventObservable()
            .subscribe(x -> 
            {
                if(!this.isHubSessionRefreshSilent)
                {
                    this.LoadMapping();
                }
            });
        
        if(!this.userPreferenceService.Get(UserPreferenceKey.ShouldNeverRemindMeThatMoreThanOneCapellaModelIsOpen, Boolean.class, false))
        {
            Ref<Boolean> isDialogOpen = new Ref<>(Boolean.class, false);
            
            Observable.combineLatest(this.capellaSessionService.HasAnyOpenSessionObservable().startWith(this.HasAnyOpenSession()), 
                    this.hubController.GetIsSessionOpenObservable(),
                    (hasAnyCapellaModelOpen, isHubSessionOpen) -> 
                        hasAnyCapellaModelOpen && isHubSessionOpen 
                        && this.capellaSessionService.GetOpenSessions().size() > 1
                        && !this.userPreferenceService.Get(UserPreferenceKey.ShouldNeverRemindMeThatMoreThanOneCapellaModelIsOpen, Boolean.class, false))
                 .filter(x -> x)
                 .subscribe(x -> 
                 {
                     if(!isDialogOpen.Get())
                     {
                         isDialogOpen.Set(true);
                         
                         EventQueue.invokeLater(() -> 
                         {
                             this.navigationService.ShowDialog(new AlertMoreThanOneCapellaModelOpenDialog());
                             isDialogOpen.Set(false);
                         });
                     }
                 });
        }
        
        this.GetDstMapResult().ItemsAdded().subscribe(x -> this.MapTraces(MappingDirection.FromDstToHub));
        this.GetHubMapResult().ItemsAdded().subscribe(x -> this.MapTraces(MappingDirection.FromHubToDst));
    }

    /**
     * Adds or removes available traces for transfer to Capella
     */
    private void AddOrRemoveTracesForTransfer()
    {
        this.selectedHubMapResultForTransfer.removeIf(x -> x instanceof Trace);
        
        var transferableTraces = this.mappedBinaryRelationshipsToTraces.stream()
                .filter(x -> this.selectedHubMapResultForTransfer.stream().anyMatch(m -> AreTheseEquals(m.getId(), x.getTargetElement().getId()))
                        && this.selectedHubMapResultForTransfer.stream().anyMatch(m -> AreTheseEquals(m.getId(), x.getSourceElement().getId())))
                .collect(Collectors.toList());
        
        this.selectedHubMapResultForTransfer.addAll(transferableTraces);
    }

    /**
     * Adds or removes available BinaryRelationship for transfer to the Hub
     */
    private void AddOrRemoveBinaryRelationshipForTransfer()
    {
        this.selectedHubMapResultForTransfer.removeIf(x -> x instanceof BinaryRelationship);

        var transferableBinaryRelationship = this.mappedTracesToBinaryRelationships.stream()
                .filter(x -> this.selectedDstMapResultForTransfer.stream().anyMatch(m -> AreTheseEquals(m.getIid(), x.getTarget().getIid()))
                        && this.selectedDstMapResultForTransfer.stream().anyMatch(m -> AreTheseEquals(m.getIid(), x.getSource().getIid())))
                .collect(Collectors.toList());
        
        this.selectedDstMapResultForTransfer.addAll(transferableBinaryRelationship);
        
    }

    /**
     * Loads the saved mapping and applies the mapping rule to the loaded things
     */
    @Override
    public void LoadMapping()
    {
        StopWatch timer = StopWatch.createStarted();
        
        this.transactionService.Reset();
        
        var mappedElements = this.mappingConfigurationService.LoadMapping();
        
        var allMappedCapellaComponents = new CapellaComponentCollection();
        var allMappedCapellaRequirements = new CapellaRequirementCollection();
        var allMappedHubElements = new HubElementCollection();
        var allMappedHubRequirements = new HubRequirementCollection();
        
        mappedElements.stream()
            .filter(x -> x.GetMappingDirection() == MappingDirection.FromDstToHub)
            .forEach(x -> SortMappedElementByType(allMappedCapellaComponents, allMappedCapellaRequirements, x));
    
        mappedElements.stream()
            .filter(x -> x.GetMappingDirection() == MappingDirection.FromHubToDst)
            .forEach(x -> SortMappedElementByType(allMappedHubElements, allMappedHubRequirements, x));
    
        this.dstMapResult.clear();
        this.hubMapResult.clear();
        this.selectedHubMapResultForTransfer.clear();
        this.selectedDstMapResultForTransfer.clear();
        
        var result = this.Map(allMappedCapellaComponents, MappingDirection.FromDstToHub)
                   & this.Map(allMappedCapellaRequirements, MappingDirection.FromDstToHub)
                   & this.Map(allMappedHubElements, MappingDirection.FromHubToDst)
                   & this.Map(allMappedHubRequirements, MappingDirection.FromHubToDst);
    
        timer.stop();
            
        if(!result)
        {
            this.logService.Append(String.format("Could not load %s saved mapped things for some reason, check the log for details", mappedElements.size()), Level.ERROR);
            mappedElements.clear();
            return;
        }
        
        this.logService.Append(String.format("Loaded %s saved mapping, done in %s ms", mappedElements.size(), timer.getTime(TimeUnit.MILLISECONDS)));
    }

    /**
     * Sorts the {@linkplain IMappedElementRowViewModel} and adds it to the relevant collection of one of the two provided
     * 
     * @param allMappedElement the {@linkplain Collection} of {@linkplain MappedElementDefinitionRowViewModel}
     * @param allMappedRequirements the {@linkplain Collection} of {@linkplain MappedRequirementBaseRowViewModel}
     * @param mappedRowViewModel the {@linkplain IMappedElementRowViewModel} to sort
     */
    private void SortMappedElementByType(ArrayList<MappedElementDefinitionRowViewModel> allMappedElement,
            ArrayList<? extends MappedRequirementBaseRowViewModel> allMappedRequirements, IMappedElementRowViewModel mappedRowViewModel)
    {
        if(mappedRowViewModel.GetTThingClass().isAssignableFrom(ElementDefinition.class))
        {
            allMappedElement.add((MappedElementDefinitionRowViewModel) mappedRowViewModel);
        }
        else if(mappedRowViewModel instanceof MappedDstRequirementRowViewModel)
        {
            ((CapellaRequirementCollection)allMappedRequirements).add((MappedDstRequirementRowViewModel) mappedRowViewModel);
        }
        else if(mappedRowViewModel instanceof MappedHubRequirementRowViewModel)
        {
            ((HubRequirementCollection)allMappedRequirements).add((MappedHubRequirementRowViewModel) mappedRowViewModel);
        }
    }

    /**
     * Maps the traces/BinaryRelationship from either the {@linkplain #dstMapResult} or the {@linkplain #hubMapResult} depending on the provided {@linkplain MappingDirection}
     * 
     * @param mappingDirection the {@linkplain MappingDirection}
     * @return a {@linkplain boolean} indicating whether the mapping operation went well
     */
    @SuppressWarnings("unchecked")
    private boolean MapTraces(MappingDirection mappingDirection)
    {
        IMappableThingCollection input = null;
        
        if(mappingDirection == MappingDirection.FromDstToHub)
        {
            input = new CapellaTracedElementCollection();
            ((ArrayList<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>>) input).addAll(this.dstMapResult);
        }
        else if(mappingDirection == MappingDirection.FromHubToDst)
        {
            input = new HubRelationshipElementsCollection();
            ((ArrayList<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>>) input).addAll(this.hubMapResult);
        }

        return this.MapTraces(input, mappingDirection);
    }
    
    /**
     * Maps the {@linkplain input} by calling the {@linkplain IMappingEngine}
     * and assign the map result to the dstMapResult or the hubMapResult
     * 
     * @param input the {@linkplain IMappableThingCollection} in other words the  {@linkplain Collection} of {@linkplain Object} to map
     * @param mappingDirection the {@linkplain MappingDirection} towards the {@linkplain IMappableThingCollection} maps to
     * @return a {@linkplain boolean} indicating whether the mapping operation went well
     */
    @SuppressWarnings("unchecked")
    private boolean MapTraces(IMappableThingCollection input, MappingDirection mappingDirection)
    {
        this.logService.Append("Mapping of Traces/BinaryRelationships in progress");
        var output = new Ref<ArrayList<?>>(null);
        var result = new Ref<Boolean>(Boolean.class, false);
        
        if(this.TryMap(input, output, result));
        {
            if(mappingDirection == MappingDirection.FromDstToHub)
            {
                var mappedBinaryRelationship = (ArrayList<BinaryRelationship>)output.Get();
                this.mappedTracesToBinaryRelationships.removeIf(x -> mappedBinaryRelationship.stream().anyMatch(r -> AreTheseEquals(x.getIid(), r.getIid())));
                this.mappedTracesToBinaryRelationships.addAll(mappedBinaryRelationship);
                this.logService.Append("%s Binary Relationships were mapped from Capella Traces", output.Get().size());
            }
            else if(mappingDirection == MappingDirection.FromHubToDst)
            {
                var mappedTraces = (ArrayList<Trace>)output.Get();
                this.mappedBinaryRelationshipsToTraces.removeIf(x -> mappedTraces.stream().anyMatch(t -> AreTheseEquals(t.getSummary(), x.getSummary())));
                this.mappedBinaryRelationshipsToTraces.addAll(mappedTraces);
                this.logService.Append("%s Capella Traces were mapped from Binary Relationships", output.Get().size());
            }
        }
        
        return result.Get();
    }

    /**
     * Maps the {@linkplain input} by calling the {@linkplain IMappingEngine}
     * and assign the map result to the dstMapResult or the hubMapResult
     * 
     * @param input the {@linkplain IMappableThingCollection} in other words the  {@linkplain Collection} of {@linkplain Object} to map
     * @param mappingDirection the {@linkplain MappingDirection} towards the {@linkplain IMappableThingCollection} maps to
     * @return a {@linkplain boolean} indicating whether the mapping operation went well
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean Map(IMappableThingCollection input, MappingDirection mappingDirection)
    {
        var output = new Ref<ArrayList<?>>(null);
        var result = new Ref<Boolean>(Boolean.class, false);
        
        if(this.TryMap(input, output, result));
        {
            var resultAsCollection = (ArrayList<MappedElementRowViewModel<DefinedThing, NamedElement>>) output.Get();
            
            if(resultAsCollection != null && !resultAsCollection.isEmpty())
            {
                if (mappingDirection == MappingDirection.FromDstToHub
                        && resultAsCollection.stream().allMatch(x -> x.GetHubElement() instanceof Thing || x.GetHubElement() == null))
                {
                    this.dstMapResult.removeIf(x -> resultAsCollection.stream()
                            .filter(d -> d.GetHubElement() == null)
                            .anyMatch(d -> AreTheseEquals(((Thing) d.GetHubElement()).getIid(), x.GetHubElement().getIid())));
        
                    this.selectedDstMapResultForTransfer.clear();                
                    return this.dstMapResult.addAll(resultAsCollection.stream().filter(x -> x != null).collect(Collectors.toList()));
                }
                else if (mappingDirection == MappingDirection.FromHubToDst
                        && resultAsCollection.stream().allMatch(x -> x.GetDstElement() instanceof CapellaElement))
                {
                    this.hubMapResult.removeIf(x -> resultAsCollection.stream()
                            .anyMatch(d -> AreTheseEquals(d.GetDstElement().getId(), x.GetDstElement().getId())));
    
                    this.selectedHubMapResultForTransfer.clear();
                    return this.hubMapResult.addAll(resultAsCollection);
                }
            }
        }

        return result.Get();
    }

    /**
     * Tries to map the provided {@linkplain IMappableThingCollection}
     * 
     * @param input the {@linkplain IMappableThingCollection}
     * @param output the  {@linkplain ArrayList} output of whatever mapping rule returns
     * @param result the result to return {@linkplain #Map(IMappableThingCollection, MappingDirection)} from in case the mapping fails
     * @return a value that is true when the {@linkplain IMappableThingCollection} mapping succeed
     */
    private boolean TryMap(IMappableThingCollection input, Ref<ArrayList<?>> output, Ref<Boolean> result)
    {
        if(input.isEmpty())
        {
            result.Set(true);
        }
        
        Object outputAsObject = this.mappingEngine.Map(input);

        if(outputAsObject instanceof ArrayList<?>)
        {
            output.Set((ArrayList<?>)outputAsObject);
        }
        
        return output.HasValue();
    }
    
    /**
     * Transfers the selected things to be transfered depending on the current {@linkplain MappingDirection}
     * 
     * @return a value indicating that all transfer could be completed
     */
    @Override
    public boolean Transfer()
    { 
        MutablePair<Boolean, Boolean> result = MutablePair.of(true, true);
        
        try
        {
            this.isHubSessionRefreshSilent = true;
               
            switch(this.CurrentMappingDirection())
            {
                case FromDstToHub:
                    result = this.TransferToHub();
                    break;
                case FromHubToDst:
                    result.left &= this.TransferToDst();
                    break;
                default:
                    result = MutablePair.of(false, false);
                    break;        
            }
            
            if(result.getRight().booleanValue())
            {
                this.SaveMappingConfiguration();
                result.left &= this.hubController.Refresh();
            }
        } 
        catch (TransactionException exception)
        {
            this.logger.catching(exception);
        }
        finally
        {
            (this.CurrentMappingDirection() == MappingDirection.FromHubToDst ? this.selectedHubMapResultForTransfer : this.selectedDstMapResultForTransfer).clear();
            this.isHubSessionRefreshSilent = false;
            this.logService.Append("Reloading the mapping configuration in progress...");
            this.LoadMapping();
        }
        
        return result.getLeft();
    }
    
    /**
     * Saves the mapping configuration
     * 
     * @throws TransactionException
     */
    private void SaveMappingConfiguration() throws TransactionException
    {
        if(!this.mappingConfigurationService.IsTheCurrentIdentifierMapTemporary())
        {
            this.logService.Append("Saving the mapping configuration in progress...");

            Pair<Iteration, ThingTransaction> iterationTransaction = this.hubController.GetIterationTransaction();

            Iteration iterationClone = iterationTransaction.getLeft();
            ThingTransaction transaction = iterationTransaction.getRight();
            this.mappingConfigurationService.PersistExternalIdentifierMap(transaction, iterationClone);
            transaction.createOrUpdate(iterationClone);
            
            this.hubController.Write(transaction);
            this.hubController.Refresh();
            this.mappingConfigurationService.RefreshExternalIdentifierMap();
        }
    }

    /**
     * Transfers all the {@linkplain CapellaElement} contained in the {@linkplain hubMapResult} to the DST
     * 
     * @return a value indicating that all transfer could be completed
     */
    public boolean TransferToDst()
    {
        try
        {
            var result = this.transactionService.Commit(() -> this.PrepareElementsForTransferToCapella());
            this.logService.Append(String.format("Transfered %s elements to Capella", this.selectedHubMapResultForTransfer.size()), result);
            
            return result;
        } 
        catch (Exception exception)
        {
            this.logService.Append(String.format("The transfer to Capella failed because %s : %s", exception.getClass().getSimpleName(), exception.toString()), exception);
            this.logger.catching(exception);
            return false;
        }
    }

    /**
     * Prepares and transfers the actual changes selected in {@linkplain #selectedHubMapResultForTransfer}
     */
    private void PrepareElementsForTransferToCapella()
    {
        this.AddOrRemoveTracesForTransfer();
        
        var targetArchitecture = CapellaArchitecture.PhysicalArchitecture;
        
        for (var element : this.selectedHubMapResultForTransfer)
        {
            targetArchitecture = this.transactionService.GetTargetArchitecture(element);
            
            if(element instanceof Requirement)
            {
                this.PrepareRequirement((Requirement)element, targetArchitecture);
            }
            else if(element instanceof Component)
            {
                if(this.transactionService.IsCloned(element))
                {
                    this.PrepareComponent((Component)element);
                }
                else if(this.transactionService.IsCloned(element.eContainer()))
                {   
                    this.PrepareComponentContainer((Component)element);
                }

                this.PrepareInterfaces((Component)element);
            }
            
            if(element instanceof Namespace)
            {
                this.PrepareTraces((Namespace) element);
            }
        }

        this.PrepareDeployementLinks(targetArchitecture);
    }


    /**
     * Prepares the deployement link by updating the part
     * 
     * @param element
     */
    private void PrepareDeployementLinks(CapellaArchitecture architecture)
    {
        var topElement = architecture != null ? this.capellaSessionService.GetTopElement(architecture) : this.capellaSessionService.GetTopElement();
        
        for(var part : topElement.eContents().stream().filter(x -> x instanceof Part).map(x -> (Part)x).collect(Collectors.toList()))
        {
            var partsToDeploy = this.hubMapResult.stream()
                    .map(x -> this.transactionService.GetOriginal(x.GetDstElement()))
                    .filter(x -> x.eContainer() != null && x.eContainer() instanceof Component && AreTheseEquals(((Component)x.eContainer()).getId(), topElement.getId()))
                    .flatMap(x -> x.eContents().stream().filter(p -> p instanceof Part).map(p -> (Part)p))
                    .collect(Collectors.toList());
            
            part.getOwnedDeploymentLinks().clear();
            
            for(var partToDeploy : partsToDeploy)
            {
                var newLink = this.transactionService.Create(PartDeploymentLink.class);
                newLink.setLocation(part);
                newLink.setDeployedElement(partToDeploy);
                part.getOwnedDeploymentLinks().add(newLink);
            }
        }        
    }
    
    /**
     * Prepares the deployement link by updating the part
     * 
     * @param element
     */
    private void PrepareDeployementLinks(Component element)
    {
        if(element.eContainer() instanceof PhysicalComponent)
        {
            var originalContainer = this.transactionService.GetOriginal((PhysicalComponent)element.eContainer());
            
            var part = originalContainer.eContents().stream()
                    .filter(x -> x instanceof Part)
                    .map(x -> (Part)x)
                    .filter(x -> x.getAbstractType() != null && x.getAbstractType().getId().equals(element.getId()) 
                            && AreTheseEquals(x.getName(), element.getName()))
                    .findFirst()
                    .orElse(null);
            
            if(part == null)
            {
                return;
            }
            
            for(var containedPart : element.getOwnedFeatures().stream().filter(x -> x instanceof Part).map(x -> (Part)x).collect(Collectors.toList()))
            {
                if(part.getOwnedDeploymentLinks().stream().filter(x -> AreTheseEquals(x.getDeployedElement().getId(), containedPart.getId())).findFirst().isPresent())
                {
                    return;
                }
                
                var newLink = this.transactionService.Create(PartDeploymentLink.class);
                newLink.setLocation(part);
                newLink.setDeployedElement(containedPart);
                part.getOwnedDeploymentLinks().add(newLink);
            }
        }
        
    }

    /**
     * Prepares all the {@linkplain Traces} that can be added to the model where the specified {@linkplain Component} is the source element
     * 
     * @param element the {@linkplain Component} source element
     */
    private void PrepareTraces(Namespace element)
    {
        var original = element;
        
        if(this.transactionService.IsCloned(element))
        {
           original = this.transactionService.GetClone(element).GetOriginal();
        }
        
        original.getOwnedTraces().addAll(this.selectedHubMapResultForTransfer.stream().filter(x -> x instanceof Trace)
            .map(x -> (Trace)x)
            .filter(x -> AreTheseEquals(x.getSourceElement().getId(), element.getId()))
            .collect(Collectors.toList()));
    }

    /**
     * Prepares all the {@linkplain Interface}s that the provided {@linkplain Component} ports use
     * 
     * @param element the {@linkplain Component}
     */
    private void PrepareInterfaces(Component element)
    {
        var allInterfaces = element.getContainedComponentPorts().stream()
                .flatMap(x -> Stream.concat(x.getProvidedInterfaces().stream(), x.getRequiredInterfaces().stream()))
                .filter(x -> this.transactionService.IsNew(x))
                .collect(Collectors.toList());

        var targetArchitecture = element instanceof PhysicalComponent ? 
                CapellaArchitecture.PhysicalArchitecture : CapellaArchitecture.LogicalArchitecture;
        
        for (Interface interfaceToAdd : allInterfaces)
        {
            var interfacePackage = BlockArchitectureExt.getInterfacePkg(
                    this.capellaSessionService.GetArchitectureInstance(targetArchitecture), true);
            
            interfacePackage.getOwnedInterfaces().add(interfaceToAdd);
            this.exchangeHistory.Append(interfaceToAdd, ChangeKind.CREATE);
        }
        
        this.PrepareInterfacesForChildren(element);
    }

    /**
     * Prepares all the {@linkplain Interface}s that the provided {@linkplain Component} children ports use
     * 
     * @param element the {@linkplain Component}
     */
    private void PrepareInterfacesForChildren(Component element)
    {
        for (var childComponent : element.eContents().stream()
                .filter(x -> x instanceof Component)
                .map(x -> (Component)x)
                .collect(Collectors.toList()))
        {
            this.PrepareInterfaces(childComponent);
        }
    }

    /**
     * Prepares for transfer the provided {@linkplain Requirement}
     * 
     * @param element the {@linkplain Requirement} element to transfer
     * @param targetArchitecture the {@linkplain CapellaArchitecture} where to transfer the {@linkplain Requirement}
     */
    private void PrepareRequirement(Requirement element, CapellaArchitecture targetArchitecture)
    {
        if(this.transactionService.IsCloned(element))
        {
            var clonedReference = this.transactionService.GetClone(element);
            clonedReference.GetOriginal().setDescription(clonedReference.GetClone().getDescription());
            clonedReference.GetOriginal().setName(clonedReference.GetClone().getName());
            clonedReference.GetOriginal().setRequirementId(element.getRequirementId());
            this.exchangeHistory.Append(element, ChangeKind.UPDATE);
        }
        
        var container = (RequirementsPkg)element.eContainer();
        Boolean containerIsCloned = null;
        
        while(container != null && !(containerIsCloned = this.transactionService.IsCloned(container)) && container.eContainer() instanceof RequirementsPkg)
        {                    
            container = (RequirementsPkg)container.eContainer();
        }

        final RequirementsPkg containerToUpdate = container;
        
        if(containerToUpdate == null)
        {
            return;
        }
        
        if(containerIsCloned.booleanValue())
        {
            this.UpdateRequirementPackage(containerToUpdate);
            this.exchangeHistory.Append(containerToUpdate, ChangeKind.UPDATE);
        }
        else
        {
            var architecture = this.capellaSessionService.GetArchitectureInstance(targetArchitecture);
            architecture.getOwnedRequirementPkgs().removeIf(x -> AreTheseEquals(x.getId(), containerToUpdate.getId()));
            architecture.getOwnedRequirementPkgs().add(containerToUpdate);

            this.exchangeHistory.Append(containerToUpdate, ChangeKind.CREATE);
            this.exchangeHistory.Append(element, ChangeKind.CREATE);
        }
    }

    /**
     * Updates the provided cloned {@linkplain RequirementsPkg}
     * 
     * @param containerToUpdate the {@linkplain RequirementsPkg}
     */
    private void UpdateRequirementPackage(RequirementsPkg containerToUpdate)
    {
        var requirementPkgCloneReference = this.transactionService.GetClone(containerToUpdate);
        
        this.UpdateChildrenOfType(requirementPkgCloneReference.GetOriginal().getOwnedRequirementPkgs(), 
                requirementPkgCloneReference.GetClone().getOwnedRequirementPkgs());
        
        this.UpdateChildrenOfType(requirementPkgCloneReference.GetOriginal().getOwnedRequirements(), 
                requirementPkgCloneReference.GetClone().getOwnedRequirements());
    }

    /**
     * Adds the new {@linkplain #TElement} contained in the cloned collection to the original collection
     * 
     * @param <TElement> the type of {@linkplain CapellaElement} the collections contains
     * @param originalCollection the original collection
     * @param clonedCollection the cloned collection
     */
    private <TElement extends CapellaElement> void UpdateChildrenOfType(EList<TElement> originalCollection,
            EList<TElement> clonedCollection)
    {
        var childrenPackagesToAdd = clonedCollection.stream()
                .filter(x -> originalCollection.stream()
                            .noneMatch(e -> AreTheseEquals(x.getId(), e.getId(), true)))
                .collect(Collectors.toList());

        originalCollection.addAll(childrenPackagesToAdd);
    }


    /**
     * Prepares the specified {@linkplain Component} by updating its container only
     * 
     * @param element the {@linkplain Component} element to add to its defined container
     */
    private void PrepareComponentContainer(Component element)
    {
        if(element instanceof PhysicalComponent)
        {
            if(element.eContainer() instanceof PhysicalComponent)
            {
                this.PrepareComponentContainer((PhysicalComponent)element.eContainer(), (PhysicalComponent)element, x -> x.getOwnedPhysicalComponents());
            }
            else if(element.eContainer() instanceof PhysicalComponentPkg)
            {
                this.PrepareComponentContainer((PhysicalComponentPkg)element.eContainer(), (PhysicalComponent)element, x -> x.getOwnedPhysicalComponents());
            }
        }
        else if(element instanceof LogicalComponent)
        {
            this.PrepareComponentContainer((LogicalComponent)element.eContainer(), (LogicalComponent)element, x -> x.getOwnedLogicalComponents());
        }
    }
    
    /**
     * Prepares for transfer the specified {@linkplain #TElement} by updating its container only
     * 
     * @param <TElement> the type of {@linkplain Component} to prepare
     * @param container the {@linkplain #TElement} element to update
     * @param element the {@linkplain #TElement} element to transfer
     * @param childrenSelector a {@linkplain Function} that returns a {@linkplain Collection} of {@linkplain #TElement}, 
     * used to query the contained {@linkplain #TElement} regardless of whether the input {@linkplain #TElement} is a clone or an original
     */
    private <TElement extends Component, TElementContainer extends NamedElement> void PrepareComponentContainer(TElementContainer container, TElement element, Function<TElementContainer, EList<TElement>> childrenSelector)
    {
        var original = this.transactionService.GetClone(container).GetOriginal();
        childrenSelector.apply(original).removeIf(x -> AreTheseEquals(x.getId(), element.getId()));
        childrenSelector.apply(original).add(element);
        this.exchangeHistory.Append(container, ChangeKind.UPDATE);
        this.exchangeHistory.Append(element, ChangeKind.CREATE);

        if(container instanceof PhysicalComponentPkg || container instanceof Component 
                && container.eContents().stream()
                .anyMatch(x -> x instanceof Part 
                        && ((Part)x).getAbstractType() != null 
                        && ((Part)x).getAbstractType().getId().equals(element.getId()) 
                        && AreTheseEquals(((Part)x).getName(), element.getName())))
        {
            return;
        }
        
        var part = this.transactionService.Create(Part.class, element.getName());
        part.setAbstractType(element);
        
        ((Component)original).getOwnedFeatures().add(part);
    }
    
    /**
     * Prepares for transfer the specified {@linkplain Component}
     * 
     * @param element the {@linkplain Component}
     */
    private void PrepareComponent(Component element)
    {
        if(element instanceof PhysicalComponent)
        {
            this.PrepareComponent((PhysicalComponent)element, x -> x.getOwnedPhysicalComponents());
        }
        else if(element instanceof LogicalComponent)
        {
            this.PrepareComponent((LogicalComponent)element, x -> x.getOwnedLogicalComponents());
        }
        
        this.exchangeHistory.Append(element, ChangeKind.UPDATE);
    }

    /**
     * Prepares for transfer the specified {@linkplain element}
     * 
     * @param <TElement> the type of {@linkplain Component} to prepare
     * @param element the {@linkplain #TElement} element to transfer
     * @param childrenSelector a {@linkplain Function} that returns a {@linkplain Collection} of {@linkplain #TElement}, 
     * used to query the contained {@linkplain #TElement} regardless of whether the input {@linkplain #TElement} is a clone or an original
     */
    private <TElement extends Component> void PrepareComponent(TElement element, Function<TElement, EList<TElement>> childrenSelector)
    {
        var clonedReference = this.transactionService.GetClone(element);
        
        clonedReference.GetOriginal().setName(clonedReference.GetClone().getName());
        
        if(element instanceof PhysicalComponent)
        {
            ((PhysicalComponent)clonedReference.GetOriginal()).setNature(((PhysicalComponent)clonedReference.GetClone()).getNature());
            ((PhysicalComponent)clonedReference.GetOriginal()).setKind(((PhysicalComponent)clonedReference.GetClone()).getKind());
        }
        
        for (var clonedProperty : clonedReference.GetClone().getContainedProperties().stream().collect(Collectors.toList()))
        {
            var optionalProperty = clonedReference.GetOriginal().getContainedProperties().stream()
                    .filter(x -> AreTheseEquals(x.getId(), clonedProperty.getId()))
                    .findFirst();
            
            if(optionalProperty.isPresent())
            {
                optionalProperty.get().setOwnedDefaultValue(clonedProperty.getOwnedDefaultValue());

                this.exchangeHistory.Append(clonedProperty, optionalProperty.get());
                continue;
            }
            
            clonedReference.GetOriginal().getOwnedFeatures().add(clonedProperty);
            this.exchangeHistory.Append(clonedProperty, ChangeKind.CREATE);
        }
        
        for (var containedElement : childrenSelector.apply(clonedReference.GetClone()).stream().collect(Collectors.toList()))
        {
            if(childrenSelector.apply(clonedReference.GetOriginal()).stream()
                    .anyMatch(x -> AreTheseEquals(x.getId(), containedElement.getId())))
            {
                continue;
            }
            
            childrenSelector.apply(clonedReference.GetOriginal()).add(containedElement);
        }
        
        this.PrepareParts(clonedReference);
        
        for (var clonedPort : clonedReference.GetClone().getContainedComponentPorts())
        {
            var optionalPort = clonedReference.GetOriginal().getContainedComponentPorts().stream()
                    .filter(x -> AreTheseEquals(x.getId(), clonedPort.getId()))
                    .findFirst();
            
            if(optionalPort.isPresent())
            {
                this.UpdateInterfaces(clonedPort.getProvidedInterfaces(), optionalPort.get().getProvidedInterfaces());
                this.UpdateInterfaces(clonedPort.getRequiredInterfaces(), optionalPort.get().getRequiredInterfaces());
                
                this.exchangeHistory.Append(optionalPort.get(), ChangeKind.UPDATE);
                continue;
            }
            
            clonedReference.GetOriginal().getOwnedFeatures().add(clonedPort);
            this.exchangeHistory.Append(clonedPort, ChangeKind.CREATE);
        }
    }

    /**
     * Prepares the owned parts and its deploymnent links
     * 
     * @param <TElement> The type of element
     * @param clonedReference TYhe {@linkplain ClonedReferenceElement}
     */
    private <TElement extends Component> void PrepareParts(ClonedReferenceElement<TElement> clonedReference)
    {
        for (var part : clonedReference.GetClone().getContainedParts())
        {            
            if(this.transactionService.IsCloned(part))
            {
                var clonedPart = this.transactionService.GetClone(part);
                
                clonedPart.GetOriginal().setName(clonedPart.GetClone().getName());
                
                for (var clonedProperty : clonedPart.GetClone().getOwnedPropertyValues().stream().collect(Collectors.toList()))
                {
                    var optionalProperty = clonedPart.GetOriginal().getOwnedPropertyValues().stream()
                            .filter(x -> AreTheseEquals(x.getId(), clonedProperty.getId()))
                            .findFirst();
                    
                    if(optionalProperty.isPresent())
                    {
                        ElementToComponentMappingRule.UpdatePartPropertyValue(optionalProperty.get(), clonedProperty);
                        continue;
                    }
                    
                    part.getOwnedPropertyValues().add(clonedProperty);
                    this.exchangeHistory.Append(clonedProperty, ChangeKind.CREATE);
                }

                for (var deploymentLink : clonedPart.GetClone().getOwnedDeploymentLinks().stream().collect(Collectors.toList()))
                {
                    var existingDeploymentLink = clonedPart.GetOriginal().getOwnedDeploymentLinks().stream()
                            .filter(x -> AreTheseEquals(x.getId(), deploymentLink.getId()))
                            .findFirst();
                    
                    if(existingDeploymentLink.isPresent())
                    {                        
                        if(existingDeploymentLink.get().getDeployedElement() == null)
                        {
                            part.getOwnedDeploymentLinks().remove(existingDeploymentLink.get());
                        }
                    }

                    var optionalDeployedPart = this.hubMapResult.stream()
                            .filter(x -> x.GetDstElement() instanceof Part && deploymentLink.getDeployedElement() != null 
                                                                           && AreTheseEquals(x.GetDstElement().getId(), deploymentLink.getDeployedElement().getId()))
                            .map(x -> (Part)x.GetDstElement())
                            .findFirst();
                    
                    if(optionalDeployedPart.isPresent())
                    {
                        deploymentLink.setDeployedElement(optionalDeployedPart.get());
                    }
                    
                    part.getOwnedDeploymentLinks().removeIf(x -> AreTheseEquals(x.getId(), deploymentLink.getId()));
                    part.getOwnedDeploymentLinks().add(deploymentLink);
                }                
            }
            
            clonedReference.GetOriginal().getOwnedFeatures().removeIf(x -> x instanceof Part && AreTheseEquals(x.getId(), part.getId()));
            clonedReference.GetOriginal().getOwnedFeatures().add(part);
        }
    }

    /**
     * Updates the {@linkplain Interface} referenced through the provided {@linkplain Collection} of {@linkplain Interface}
     * 
     * @param clonedInterfaces the {@linkplain EList} of cloned {@linkplain Interface}s
     * @param originalInterfaces the {@linkplain EList} of original {@linkplain Interface}s
     */
    private void UpdateInterfaces(EList<Interface> clonedInterfaces, EList<Interface> originalInterfaces)
    {
        for (var interfaceToUpdate : clonedInterfaces)
        {
            originalInterfaces.removeIf(x -> AreTheseEquals(x.getId(), interfaceToUpdate.getId()));
            originalInterfaces.add(interfaceToUpdate);
        }
    }
        
    /**
     * Transfers all the {@linkplain Thing} contained in the {@linkplain dstMapResult} to the Hub
     * 
     * @return a {@linkplain MutablePair} of value where one indicates that all transfer could be completed and
     * the other one indicates whether the mapping configuration should be persisted
     */
    @Override
    public MutablePair<Boolean, Boolean> TransferToHub()
    {
        try
        {
            Pair<Iteration, ThingTransaction> iterationTransaction = this.hubController.GetIterationTransaction();
            Iteration iterationClone = iterationTransaction.getLeft();
            ThingTransaction transaction = iterationTransaction.getRight();

            if(!this.hubController.TrySupplyAndCreateLogEntry(transaction))
            {
                this.logService.Append("Transfer to the HUB aborted!");
                return MutablePair.of(true, false);
            }
            
            this.PrepareThingsForTransfer(iterationClone, transaction);
            this.hubController.Write(transaction);
            
            boolean result = this.hubController.Refresh();
            this.PrepareParameterOverrides();
            result &= this.hubController.Refresh();
            this.UpdateParameterValueSets();
            return MutablePair.of(result, true);
        }
        catch (Exception exception)
        {
            this.logService.Append(String.format("The transfer to the HUB failed because %s : %s", exception.getClass().getSimpleName(), exception.toString()), exception);
            return MutablePair.of(false, true);
        }
    }

   /**
    * Prepares all the {@linkplain ParameterOverrides}s that are to be updated or created
    * 
    * @throws TransactionException can throw {@linkplain TransactionException}
    */
    @Annotations.ExludeFromCodeCoverageGeneratedReport
   private void PrepareParameterOverrides() throws TransactionException
    {
       Pair<Iteration, ThingTransaction> iterationTransaction = this.hubController.GetIterationTransaction();
       Iteration iterationClone = iterationTransaction.getLeft();
       ThingTransaction transaction = iterationTransaction.getRight();
       
        var elementDefinitions = this.selectedDstMapResultForTransfer.stream()
                .filter(x -> x instanceof ElementDefinition)
                .map(x -> (ElementDefinition)x)
                .filter(x -> !x.getContainedElement().isEmpty())
                .filter(x -> x.getContainedElement().stream().anyMatch(u -> !u.getParameterOverride().isEmpty()))
                .collect(Collectors.toList());
        
        for (var elementDefinition : elementDefinitions)
        {
            var refElementDefinition = new Ref<>(ElementDefinition.class);
            
            if(this.hubController.TryGetThingById(elementDefinition.getIid(), refElementDefinition))
            {
                var updatedElementDefinition = refElementDefinition.Get().clone(false);
                this.AddOrUpdateIterationAndTransaction(updatedElementDefinition, iterationClone.getElement(), transaction);
                this.PrepareElementUsageForTransfer(iterationClone, transaction, updatedElementDefinition, true);
            }           
        }

        transaction.createOrUpdate(iterationClone);
        this.hubController.Write(transaction);
    }

    /**
     * Updates the {@linkplain ValueSet} with the new values
     * 
     * @throws TransactionException can throw {@linkplain TransactionException}
     */
    public void UpdateParameterValueSets() throws TransactionException
    {
        Pair<Iteration, ThingTransaction> iterationTransaction = this.hubController.GetIterationTransaction();
        Iteration iterationClone = iterationTransaction.getLeft();
        ThingTransaction transaction = iterationTransaction.getRight();
        
        var allParameterOverrides = this.selectedDstMapResultForTransfer.stream()
                .filter(x -> x instanceof ElementDefinition)
                .flatMap(x -> ((ElementDefinition)x).getContainedElement().stream())
                .flatMap(x -> x.getParameterOverride().stream())
                .filter(x -> x.getOriginal() != null)
                .collect(Collectors.toList());
        
        var allParameters = this.selectedDstMapResultForTransfer.stream()
                .filter(x -> x instanceof ElementDefinition)
                .flatMap(x -> ((ElementDefinition)x).getParameter().stream())
                .filter(x -> x.getOriginal() != null)
                .collect(Collectors.toList());
        
        this.UpdateParameterValueSets(transaction, allParameters, Parameter.class);
        this.UpdateParameterValueSets(transaction, allParameterOverrides, ParameterOverride.class);
        
        transaction.createOrUpdate(iterationClone);
        this.hubController.Write(transaction);
        
        this.logService.Append("%s ParameterOverrides and %s Parameter have been updated or created", allParameterOverrides.size(), allParameters.size());
    }
    
    /**
     * Updates the value sets of the provided {@linkplain Collection} of {@linkplain #TParameter}
     * 
     * @param <TParameter> the type of {@linkplain ParameterOrOverrideBase}
     * @param transaction the {@linkplain ThingTransaction}
     * @param allParameters the collection of {@linkplain #TParameter} to update
     * @param clazz the {@linkplain Class} of {@linkplain #TParameter}
     * @throws TransactionException can throw {@linkplain TransactionException}
     */
    @Annotations.ExludeFromCodeCoverageGeneratedReport
    private <TParameter extends ParameterOrOverrideBase> void UpdateParameterValueSets(ThingTransaction transaction, List<TParameter> allParameters, Class<TParameter> clazz) throws TransactionException
    {
        for(var parameter : allParameters)
        {
            var refNewParameter = new Ref<>(Parameter.class);
            
            if(this.hubController.TryGetThingById(parameter.getIid(), refNewParameter))
            {
                var newParameterCloned = refNewParameter.Get().clone(false);
    
                for (int index = 0; index < parameter.getValueSets().size(); index++)
                {
                    var clone = newParameterCloned.getValueSet().get(index).clone(false);
                    this.UpdateValueSet(clone, parameter.getValueSets().get(index));
                    transaction.createOrUpdate(clone);
                }

                transaction.createOrUpdate(newParameterCloned);
            }
        }
    }
    
    /**
     * Updates the specified {@linkplain ParameterValueSetBase}
     * 
     * @param clone the {@linkplain ParameterValueSetBase} to update
     * @param valueSet the {@linkplain ValueSet} that contains the new values
     */
    private void UpdateValueSet(ParameterValueSetBase clone, ValueSet valueSet)
    {
        this.exchangeHistory.Append(clone, valueSet);
        clone.setManual(valueSet.getManual());
        clone.setValueSwitch(valueSet.getValueSwitch());
    }

    /**
     * Prepares all the {@linkplain Thing}s that are to be updated or created
     * 
     * @param iterationClone the {@linkplain Iteration} clone
     * @param transaction the {@linkplain ThingTransaction}
     * @throws TransactionException can throw {@linkplain TransactionException}
     */
    @Annotations.ExludeFromCodeCoverageGeneratedReport
    private void PrepareThingsForTransfer(Iteration iterationClone, ThingTransaction transaction) throws TransactionException
    {
        this.AddOrRemoveBinaryRelationshipForTransfer();
        ArrayList<Thing> thingsToTransfer = new ArrayList<>(this.selectedDstMapResultForTransfer);
        
        Predicate<? super MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> selectedMappedElement = 
                x -> this.selectedDstMapResultForTransfer.stream().anyMatch(t -> AreTheseEquals(t.getIid(), x.GetHubElement().getIid()));
                
        Collection<Relationship> relationships = this.dstMapResult.stream()
                .filter(selectedMappedElement)
                .flatMap(x -> x.GetRelationships().stream())
                .collect(Collectors.toList());
        
        this.logService.Append("Processing %s relationship(s)", relationships.size() + this.selectedDstMapResultForTransfer.stream().filter(x -> x instanceof BinaryRelationship).count());
        
        thingsToTransfer.addAll(relationships);
                
        for (Thing thing : thingsToTransfer)
        {
            switch(thing.getClassKind())
            {
                case ElementDefinition:
                    this.PrepareElementDefinitionForTransfer(iterationClone, transaction, (ElementDefinition)thing);
                    break;
                case Requirement:
                    this.PrepareRequirementForTransfer(iterationClone, transaction, thing.getContainerOfType(RequirementsSpecification.class));
                    break;
                case RequirementsSpecification:
                    this.PrepareRequirementForTransfer(iterationClone, transaction, (RequirementsSpecification)thing);
                    break;
                case BinaryRelationship:
                    this.AddOrUpdateIterationAndTransaction((BinaryRelationship)thing, iterationClone.getRelationship(), transaction);
                    break;
                default:
                    break;
            }
            
            if(thing.getContainer() == null)
            {
                this.logService.Append("%s thing %s has a null container", thing.getClassKind(), Level.ERROR, thing.getUserFriendlyName());
            }
        }
    }

    /**
     * Prepares the provided {@linkplain ElementDefinition} for transfer
     * 
     * @param iterationClone the {@linkplain Iteration} clone
     * @param transaction the {@linkplain ThingTransaction}
     * @param elementDefinition the {@linkplain ElementDefinition} to prepare
     * @throws TransactionException can throw {@linkplain TransactionException}
     */
    private void PrepareElementDefinitionForTransfer(Iteration iterationClone, ThingTransaction transaction, 
            ElementDefinition elementDefinition) throws TransactionException
    {
        this.PrepareElementUsageForTransfer(iterationClone, transaction, elementDefinition, false);

        this.AddOrUpdateIterationAndTransaction(elementDefinition, iterationClone.getElement(), transaction);
        
        this.PrepareParameterOrOverrideForTransfer(transaction, elementDefinition.getParameter());
    }

    /**
     * Prepares the provided {@linkplain ElementDefinition} contained {@linkplain ElementUsages} for transfer
     * 
     * @param iterationClone the {@linkplain Iteration} clone
     * @param transaction the {@linkplain ThingTransaction}
     * @param elementDefinition the {@linkplain ElementDefinition} that might contain {@linkplain ElementUsages}
     * @throws TransactionException can throw {@linkplain TransactionException}
     */
    private void PrepareElementUsageForTransfer(Iteration iterationClone, ThingTransaction transaction,
            ElementDefinition elementDefinition, boolean shouldPrepareParameterOverride) throws TransactionException
    {
        for (ElementUsage elementUsage : elementDefinition.getContainedElement())
        {
           this.AddOrUpdateIterationAndTransaction(elementUsage.getElementDefinition().clone(false), iterationClone.getElement(), transaction);
           this.AddOrUpdateIterationAndTransaction(elementUsage, elementDefinition.getContainedElement(), transaction);
           
           if(transaction.getAddedThing().stream().anyMatch(x -> AreTheseEquals(x.getIid(), elementUsage.getIid())))
           {
               this.PrepareDefinition(transaction, elementUsage);
           }
           
           if(shouldPrepareParameterOverride)
           {
               this.PrepareParameterOrOverrideForTransfer(transaction, elementUsage.getParameterOverride());
           }
        }
    }

    /**
     * Prepare the provided parameters
     * 
     * @param <TParameter> the type of {@linkplain ParameterOrOverrideBase} to prepare
     * @param transaction the {@linkplain ThingTransaction}
     * @param parameters the {@linkplain ContainerList} of {@linkplain ParameterOrOverrideBase} to prepare
     * @param clazz the {@linkplain Class} of {@linkplain #TParameter}
     * @throws TransactionException can throw {@linkplain TransactionException}
     */
    private <TParameter extends ParameterOrOverrideBase> void PrepareParameterOrOverrideForTransfer(ThingTransaction transaction, ContainerList<TParameter> parameters) throws TransactionException
    {
        for(var parameter : parameters.stream().filter(x -> x.getOriginal() != null || x.getRevisionNumber() == 0).collect(Collectors.toList()))
        {
            transaction.createOrUpdate(parameter);
        }
    }
    
    /**
     * Prepares any transferable {@linkplain Definition} from the provided {@linkplain DefinedThing}
     * 
     * @param transaction the {@linkplain ThingTransaction}
     * @param definedThing the {@linkplain DefinedThing} that can contain a transferable {@linkplain Definition}
     * @throws TransactionException can throw {@linkplain TransactionException}
     */
    private void PrepareDefinition(ThingTransaction transaction, DefinedThing definedThing) throws TransactionException
    {
        var definition = definedThing.getDefinition().stream()
                                                .filter(x -> AreTheseEquals(x.getLanguageCode(), ComponentToElementMappingRule.CIID))
                                                .findFirst();
        
        if(definition.isPresent())
        {
            this.AddOrUpdateIterationAndTransaction(definition.get(), definedThing.getDefinition(), transaction);
        }
    }

    /**
     * Prepares the provided {@linkplain ElementDefinition} for transfer
     * 
     * @param iterationClone the {@linkplain Iteration} clone
     * @param transaction the {@linkplain ThingTransaction}
     * @param requirementsSpecification the {@linkplain RequirementsSpecification} to prepare
     * @throws TransactionException can throw {@linkplain TransactionException}
     */
    private void PrepareRequirementForTransfer(Iteration iterationClone, ThingTransaction transaction, 
            RequirementsSpecification requirementsSpecification) throws TransactionException
    {
        this.AddOrUpdateIterationAndTransaction(requirementsSpecification, iterationClone.getRequirementsSpecification(), transaction);
        
        ContainerList<RequirementsGroup> groups = requirementsSpecification.getGroup();
        
        this.RegisterRequirementsGroups(transaction, groups);
        
        for(var requirement : requirementsSpecification.getRequirement())
        {
            transaction.createOrUpdate(requirement);
            
            for (Definition definition : requirement.getDefinition())
            {
                transaction.createOrUpdate(definition);
            }
        }
    }

    /**
     * Registers the {@linkplain RequirementsGroup} to be created or updated
     * 
     * @param transaction the {@linkplain ThingTransaction}
     * @param groups the {@linkplain ContainerList} of {@linkplain RequirementsGroup}
     * @throws TransactionException can throw {@linkplain TransactionException}
     */
    private void RegisterRequirementsGroups(ThingTransaction transaction, ContainerList<RequirementsGroup> groups) throws TransactionException
    {
        for(RequirementsGroup requirementsGroup : groups)
        {
            transaction.createOrUpdate(requirementsGroup);
            
            if(!requirementsGroup.getGroup().isEmpty())
            {
                this.RegisterRequirementsGroups(transaction, requirementsGroup.getGroup());
            }
        }
    }

    /**
     * Updates the {@linkplain ThingTransaction} and the {@linkplain ContainerList} with the provided {@linkplain Thing}
     * 
     * @param <T> the Type of the current {@linkplain Thing}
     * @param thing the {@linkplain Thing}
     * @param containerList the {@linkplain ContainerList} of {@linkplain Thing} typed as T
     * @param transaction the {@linkplain ThingTransaction}
     * @throws TransactionException can throw {@linkplain TransactionException}
     */
    @Annotations.ExludeFromCodeCoverageGeneratedReport
    private <T extends Thing> void AddOrUpdateIterationAndTransaction(T thing, ContainerList<T> containerList, ThingTransaction transaction) throws TransactionException
    {
        try
        {
            if(thing.getContainer() == null || containerList.stream().noneMatch(x -> x.getIid().equals(thing.getIid())))
            {
                containerList.add(thing);
                this.exchangeHistory.Append(thing, ChangeKind.CREATE);   
            }
            else
            {
                this.exchangeHistory.Append(thing, ChangeKind.UPDATE);
            }

            transaction.createOrUpdate(thing);
        }
        catch (Exception exception)
        {
            this.logger.catching(exception);
        }
    }

    /**
     * Adds or Removes all {@linkplain TElement} from/to the relevant selected things to transfer
     * depending on whether the {@linkplain ClassKind} was specified
     * 
     * @param classKind the {@linkplain ClassKind} of the {@linkplain Thing}s to add or remove depending on which impact view it has been called from
     * @param shouldRemove a value indicating whether the things are to be removed
     */
    @Override
    public void AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind classKind, boolean shouldRemove)
    {
        if(classKind == null)
        {
            this.AddOrRemoveAllFromSelectedHubMapResultForTransfer(shouldRemove);
        }
        else
        {
            this.AddOrRemoveAllFromSelectedDstMapResultForTransfer(classKind, shouldRemove);
        }
    }
    
    /**
     * Adds or Removes all {@linkplain Thing} from/to the relevant selected things to transfer
     * 
     * @param classKind the {@linkplain ClassKind} of the {@linkplain Thing}s to add or remove depending on which impact view it has been called from
     * @param shouldRemove a value indicating whether the things are to be removed
     */
    private void AddOrRemoveAllFromSelectedDstMapResultForTransfer(ClassKind classKind, boolean shouldRemove)
    {
        Predicate<? super Thing> predicateClassKind = x -> classKind == ClassKind.RequirementsSpecification ? x.getClassKind() == ClassKind.Requirement : x.getClassKind() == classKind;
        
        this.selectedDstMapResultForTransfer.removeIf(predicateClassKind);
        
        if(!shouldRemove)
        {
            this.selectedDstMapResultForTransfer.addAll(
                    this.dstMapResult.stream()
                        .filter(x -> x.GetHubElement() != null)
                        .map(MappedElementRowViewModel::GetHubElement)
                        .filter(predicateClassKind)
                        .collect(Collectors.toList()));
        }
    }

    /**
     * Adds or Removes all {@linkplain Class} from/to the relevant selected things to transfer
     * 
     * @param shouldRemove a value indicating whether the things are to be removed
     */
    private void AddOrRemoveAllFromSelectedHubMapResultForTransfer(boolean shouldRemove)
    {
        this.selectedHubMapResultForTransfer.clear();
        
        if(!shouldRemove)
        {
            this.selectedHubMapResultForTransfer.addAll(this.hubMapResult.stream()
                    .map(x -> x.GetDstElement())
                    .collect(Collectors.toList()));
        }
    }
    
    /**
     * Tries to get the corresponding element based on the provided {@linkplain DefinedThing} name or short name. 
     * 
     * @param <TElement> the type of {@linkplain CapellaElement} to query
     * @param thing the {@linkplain DefinedThing} that can potentially match a {@linkplain #TElement} 
     * @param refElement the {@linkplain Ref} of {@linkplain #TElement}
     * @return a value indicating whether the {@linkplain CapellaElement} has been found
     */
    public <TElement extends CapellaElement> boolean TryGetElementByName(DefinedThing thing, Ref<TElement> refElement)
    {
        return this.TryGetElementBy(x -> x instanceof NamedElement
                && (AreTheseEquals(thing.getName(), ((NamedElement)x).getName(), true)
                || AreTheseEquals(thing.getShortName(), ((NamedElement)x).getName(), true)), refElement);
    }
        
    /**
     * Tries to get the corresponding element that has the provided Id
     * 
     * @param <TElement> the type of {@linkplain CapellaElement} to query
     * @param elementId the {@linkplain String} id of the searched element
     * @param refElement the {@linkplain Ref} of {@linkplain #TElement}
     * @return a value indicating whether the {@linkplain CapellaElement} has been found
     */
    public <TElement extends CapellaElement> boolean TryGetElementById(String elementId, Ref<TElement> refElement)
    {
        return this.TryGetElementBy(x -> AreTheseEquals(elementId, x.getId()), refElement);
    }
    
    /**
     * Tries to get the corresponding element that answer to the provided {@linkplain Predicate}
     * 
     * @param <TElement> the type of {@linkplain CapellaElement} to query
     * @param predicate the {@linkplain Predicate} to verify in order to match the element
     * @param refElement the {@linkplain Ref} of {@linkplain #TElement}
     * @return a value indicating whether the {@linkplain CapellaElement} has been found
     */
    @Override
    @SuppressWarnings("unchecked")
    public <TElement extends CapellaElement> boolean TryGetElementBy(Predicate<? super CapellaElement> predicate, Ref<TElement> refElement)
    {
        var elementsBySession = this.capellaSessionService.GetAllCapellaElementsFromOpenSessions();
        
        for (var elements : elementsBySession.values())
        {
            var element = elements.stream().filter(x -> refElement.GetType().isInstance(x)).filter(predicate).findFirst();
            
            if(element.isPresent())
            {
                refElement.Set((TElement) element.get());
                break;
            }
        }
        
        return refElement.HasValue();
    }


    /**
     * Tries to get a {@linkplain EnumerationPropertyType} that matches the provided {@linkplain EnumerationParameterType}
     * 
     * @param thing the {@linkplain #TThing} of reference
     * @param referenceElement a {@linkplain CapellaElement} that will point to the right session
     * @param refDataType the {@linkplain Ref} of {@linkplain DataType}
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean TryGetEnumerationPropertyType(EnumerationParameterType thing, CapellaElement referenceElement, Ref<EnumerationPropertyType> refDataType)
    {
        var sessionUri = this.capellaSessionService.GetSession(referenceElement).getSessionResource().getURI();
        
        var elementsBySession = this.capellaSessionService.GetAllCapellaElementsFromOpenSessions();
                
        var dataTypes = elementsBySession.get(sessionUri).stream()
                .filter(x -> x instanceof EnumerationPropertyType)
                .map(x -> (EnumerationPropertyType)x)
                .collect(Collectors.toList());
        
        var optionalDatatype = dataTypes.stream()
                .filter(x -> AreTheseEquals(x.getName(), thing.getName(), true) 
                        || AreTheseEquals(x.getName(), thing.getShortName(), true))
                .findAny();
        
        if(optionalDatatype.isPresent())
        {
            refDataType.Set(optionalDatatype.get());
        }
        
        return refDataType.HasValue();
    }
    
    /**
     * Tries to get a {@linkplain DataType} that matches the provided {@linkplain MeasurementScale}
     * 
     * @param <TThing> the type of {@linkplain Thing} that is {@linkplain NamedThing} and {@linkplain ShortNamedThing}
     * @param thing the {@linkplain #TThing} of reference
     * @param referenceElement a {@linkplain CapellaElement} that will point to the right session
     * @param refDataType the {@linkplain Ref} of {@linkplain DataType}
     * @return a {@linkplain boolean}
     */
    @Override
    public <TThing extends NamedThing & ShortNamedThing> boolean TryGetDataType(TThing thing, CapellaElement referenceElement, Ref<DataType> refDataType)
    {
        var sessionUri = this.capellaSessionService.GetSession(referenceElement).getSessionResource().getURI();
        
        var elementsBySession = this.capellaSessionService.GetAllCapellaElementsFromOpenSessions();
                
        var dataTypes = elementsBySession.get(sessionUri).stream()
                .filter(x -> x instanceof DataType)
                .map(x -> (DataType)x)
                .collect(Collectors.toList());
        
        var optionalScale = dataTypes.stream()
                .filter(x -> AreTheseEquals(x.getName(), thing.getName(), true) 
                        || AreTheseEquals(x.getName(), thing.getShortName(), true))
                .findAny();
        
        if(optionalScale.isPresent())
        {
            refDataType.Set(optionalScale.get());
        }
        
        return refDataType.HasValue();
    }
}
