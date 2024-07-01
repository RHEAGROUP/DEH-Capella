/*
 * CapellaTransaction.java
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
package Services.CapellaTransaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.polarsys.capella.common.ef.command.AbstractReadWriteCommand;

import Utils.Ref;

/**
 * The {@linkplain CapellaTransaction} class is an implementation of {@linkplain AbstractReadWriteCommand} where any changes to the model should be applied
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class CapellaTransaction extends AbstractReadWriteCommand
{
    /**
     * The current class logger
     */
    private final Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain Runnable}
     */
    private Runnable transactionMethod;
    
    /**
     * The {@linkplain Ref} of {@linkplain Boolean} holding the result
     */
    private Ref<Boolean> result;
    
    /**
     * Initializes a new {@linkplain CapellaTransaction}
     * 
     * @param transactionMethod the {@linkplain Runnable} that this transaction wraps
     * @param result the {@linkplain Ref} of {@linkplain Boolean} holding result
     */
    CapellaTransaction(Runnable transactionMethod, Ref<Boolean> result)
    {
        this.transactionMethod = transactionMethod;
        this.result = result;
    }
    
    /**
     * When an object implementing interface Runnable is used to create a thread, starting the thread causes 
     * the object's run method to be called in that separately executing thread.
     * The general contract of the method run is that it may take any action whatsoever.
     */
    @Override
    public void run()
    {
        try
        {
            transactionMethod.run();
            result.Set(true);
        }
        catch(Exception exception)
        {
            logger.catching(exception);
            result.Set(false);
        }
    }
}
