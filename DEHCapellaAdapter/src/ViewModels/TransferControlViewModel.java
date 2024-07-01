/*
 * TransferControlViewModel.java
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

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;

import DstController.IDstController;
import Enumerations.MappingDirection;
import Reactive.ObservableValue;
import Services.CapellaLog.ICapellaLogService;
import Services.HistoryService.ICapellaLocalExchangeHistoryService;
import Services.LocalExchangeHistory.ILocalExchangeHistoryService;
import ViewModels.Interfaces.ITransferControlViewModel;
import io.reactivex.Observable;

/**
 * The {@linkplain TransferControlViewModel} is the base abstract view model for the transfer control from the impact view panel
 */
public class TransferControlViewModel implements ITransferControlViewModel
{
    /**
     * The {@linkplain IDstController}
     */
    private IDstController dstController;
    
    /**
     * The {@linkplain ICapellaLogService}
     */
    private ICapellaLogService logService;
    
    /**
     * The {@linkplain ICapellaLocalExchangeHistoryService}
     */
    private final ICapellaLocalExchangeHistoryService exchangeHistory;
    
    /**
     * The number of selected things to transfer
     */
    private ObservableValue<Integer> numberOfSelectedThings = new ObservableValue<>(0, Integer.class);
    
    /**
     * Gets the number of selected things to transfer
     * 
     * @return an {@linkplain Observable} of {@linkplain Integer}
     */
    @Override
    public Observable<Integer> GetNumberOfSelectedThing()
    {
        return this.numberOfSelectedThings.Observable();
    }
    
    /**
     * Initializes a new {@linkplain TransferControlViewModel}
     * 
     * @param dstController the {@linkplain IDstController}
     * @param logService the {@linkplain IMagicDrawUILogService}
     * @param exchangeHistory the {@linkplain ICapellaLocalExchangeHistoryService}
     */
    public TransferControlViewModel(IDstController dstController, ICapellaLogService logService, ICapellaLocalExchangeHistoryService exchangeHistory)
    {
        this.exchangeHistory = exchangeHistory;
        this.dstController = dstController;
        this.logService = logService;
        
        this.dstController.GetSelectedDstMapResultForTransfer()
            .Changed()
            .subscribe(x -> this.UpdateNumberOfSelectedThing(this.dstController.CurrentMappingDirection()));

        this.dstController.GetSelectedHubMapResultForTransfer()
            .Changed()
            .subscribe(x -> this.UpdateNumberOfSelectedThing(this.dstController.CurrentMappingDirection()));

        this.dstController.GetMappingDirection()
            .subscribe(mappingDirection ->
            {
                this.UpdateNumberOfSelectedThing(mappingDirection);
            });
    }

    /**
     * Updates the {@linkplain numberOfSelectedThing} based on a given {@linkplain MappingDirection}
     * 
     * @param mappingDirection the {@linkplain MappingDirection}
     */
    private void UpdateNumberOfSelectedThing(MappingDirection mappingDirection)
    {
        this.numberOfSelectedThings.Value(mappingDirection == MappingDirection.FromDstToHub 
                ? this.dstController.GetSelectedDstMapResultForTransfer().size()
                : this.dstController.GetSelectedHubMapResultForTransfer().size());
    }
    
    /**
     * Gets a {@linkplain Callable} of {@linkplain Boolean} to call when the transfer button is pressed
     * 
     * @return a {@linkplain Callable} of {@linkplain Boolean}
     */
    @Override
    public Callable<Boolean> GetOnTransferCallable()
    {
       return () -> 
       {
           StopWatch timer = StopWatch.createStarted();
           
           this.logService.Append("Transfer in progress...");
           
           boolean result = this.dstController.Transfer();

           this.exchangeHistory.Write();
           
           if(timer.isStarted())
           {
               timer.stop();
           }
           
           this.logService.Append(String.format("Transfer done in %s ms", timer.getTime(TimeUnit.MILLISECONDS)), result);
           
           return result;
       };
    }
}
