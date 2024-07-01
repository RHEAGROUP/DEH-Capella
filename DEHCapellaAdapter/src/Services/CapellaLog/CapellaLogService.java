/*
 * CapellaLogService.java
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
package Services.CapellaLog;

import java.util.Arrays;
import java.util.Optional;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.ILog;

/**
 * The {@linkplain CapellaLogService} provides an easy way to report status messages 
 * to the default {@linkplain Logger} as well as to the eclipse UI logger
 */
public class CapellaLogService implements ICapellaLogService
{
    /**
     * The current class {@linkplain Logger}
     */
    private Logger logger = LogManager.getLogger();
    
    /**
     * The eclipse logger that is used to report to the error log tab
     */
    private final ILog platformLogger;
    
    /**
     * Initializes a new {@linkplain CapellaLogService}
     * 
     * @param platformLogger the {@linkplain ILog} to be used by this service
     */
    public CapellaLogService(ILog platformLogger)
    {
        this.platformLogger = platformLogger;
    }
    
    /**
     * Appends a string message with the default severity {@linkplain Level}
     * 
     * @param message the {@linkplain String} message to display
     */
    @Override
    public void Append(String message)
    {
        this.Append(message, Level.INFO);
    }
    
    /**
    * Appends a format-able string message using {@linkplain #String.format(String, args)} with the default severity {@linkplain Level}
    * 
    * @param message the {@linkplain String} message to display
    * @param args Arguments referenced by the format specifiers in the format
    * string. If there are more arguments than format specifiers, the
    * extra arguments are ignored. The number of arguments is
    * variable and may be zero
    */
    @Override
    public void Append(String message, Object... args)
    {
        this.Append(String.format(message, args));
    }
        
    /**
     * Appends a string message with the default severity {@linkplain Level.INFO}
     * 
     * @param message the {@linkplain String} message to display
     * @param successStatus a value indicating the result of an action which the message describes
     */
    @Override
    public void Append(String message, boolean successStatus)
    {
        if(successStatus)
        {
            this.Append(String.format("%s %s", message, " with success"), Level.INFO);
        }
        else
        {
            this.Append(String.format("%s %s", message, " with errors, check the Capella log file for more details."), Level.ERROR);
        }
    }
    /**
     * Appends a message alongside a {@linkplain Throwable}
     * 
     * @param message the {@linkplain String} message to display
     * @param exception the {@linkplain Throwable} to log
     * @param args Arguments referenced by the format specifiers in the format
     * string. If there are more arguments than format specifiers, the
     * extra arguments are ignored. The number of arguments is
     * variable and may be zero
     */
    @Override
    public void Append(String message, Throwable exception, Object... args)
    {
        this.Append(String.format(message, args), exception);
    }
    
    /**
     * Appends a message alongside a {@linkplain Throwable}
     * 
     * @param message the {@linkplain String} message to display
     * @param exception the {@linkplain Throwable} to log
     */
    @Override
    public void Append(String message, Throwable exception)
    {
        platformLogger.error(this.GetLogEntryMessage(message, Level.ERROR), exception);
                
        String logMessage = String.format("%s %s", this.GetCaller(), message);
        
        this.logger.catching(exception);
        this.logger.error(logMessage);      
    }
    
    /**
     * Appends a string message with the specified {@linkplain Level}
     * 
     * @param message the {@linkplain String} message to display
     * @param level the {@linkplain Level}
     */
    @Override
    public void Append(String message, Level level)
    {
        var text = this.GetLogEntryMessage(message, level);
        
        if(level == Level.INFO)
        {
            platformLogger.info(text);
        }
        else if(level == Level.WARN)
        {
            platformLogger.warn(text);
        }
        else if(level == Level.ERROR)
        {
            platformLogger.error(text);
        }
        
        String logMessage = String.format("%s %s", this.GetCaller(), message);
        this.logger.log(level, logMessage);        
    }

    /**
     * Gets the formated message
     * 
     * @param message the {@linkplain String} message to display
     * @param level the {@linkplain Level}
     * @return
     */
    private String GetLogEntryMessage(String message, Level level)
    {
        return String.format("[DEHCapellaAdapter] [%s] %s", level, message);
    }

    /**
     * Appends a format-able string message using {@linkplain #String.format(String, args)} with the specified {@linkplain Level}
     * 
     * @param message the {@linkplain String} message to display
     * @param level the {@linkplain Level}
     * @param args Arguments referenced by the format specifiers in the format
     * string. If there are more arguments than format specifiers, the
     * extra arguments are ignored. The number of arguments is
     * variable and may be zero
     */
     @Override
     public void Append(String message, Level level, Object... args)
     {
         this.Append(String.format(message, args),  level);
     }
         
    /**
     * Gets the caller class and line number as a string
     * 
     * @return a {@linkplain String}
     */
    private Object GetCaller()
    {
        try
        {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            
            Optional<StackTraceElement> stackElement = Arrays.asList(stackTrace).stream()
                    .skip(1)
                    .filter(x -> !x.getFileName().contains(this.getClass().getSimpleName()))
                    .findFirst();
            
            if(stackElement.isPresent())
            {
                return String.format("%s {Line %s} -", stackElement.get().getClassName(), 
                        stackElement.get().getLineNumber());
            }
        } 
        catch (Exception exception)
        {
            this.logger.catching(exception);
        }        
        
        return "";
    }
}
