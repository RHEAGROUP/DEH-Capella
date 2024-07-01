/*
 * ICapellaLogService.java
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

import org.apache.logging.log4j.Level;

import io.reactivex.Observable;

/**
 * The {@linkplain ICapellaLogService} is the interface definition for the service {@linkplain CapellaLogService}
 */
public interface ICapellaLogService 
{
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
    void Append(String message, Level level, Object... args);

    /**
     * Appends a string message with the specified {@linkplain Level}
     * 
     * @param message the {@linkplain String} message to display
     * @param level the {@linkplain Level}
     */
    void Append(String message, Level level);

    /**
     * Appends a string message with the default severity {@linkplain Level.INFO}
     * 
     * @param message the {@linkplain String} message to display
     * @param successStatus a value indicating the result of an action which the message describes
     */
    void Append(String message, boolean successStatus);

    /**
    * Appends a format-able string message using {@linkplain #String.format(String, args)} with the default severity {@linkplain Level}
    * 
    * @param message the {@linkplain String} message to display
    * @param args Arguments referenced by the format specifiers in the format
    * string. If there are more arguments than format specifiers, the
    * extra arguments are ignored. The number of arguments is
    * variable and may be zero
    */
    void Append(String message, Object... args);

    /**
     * Appends a string message with the default severity {@linkplain Level}
     * 
     * @param message the {@linkplain String} message to display
     */
    void Append(String message);

    /**
     * Appends a message alongside a {@linkplain Throwable}
     * 
     * @param message the {@linkplain String} message to display
     * @param exception the {@linkplain Throwable} to log
     */
    void Append(String message, Throwable exception);

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
    void Append(String message, Throwable exception, Object... args);
}
