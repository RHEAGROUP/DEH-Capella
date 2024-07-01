/*
 * AlertMoreThanOneCapellaModelOpenDialogViewModel.java
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
package ViewModels.Dialogs;

import Services.CapellaUserPreference.ICapellaUserPreferenceService;
import Services.CapellaUserPreference.UserPreferenceKey;
import ViewModels.Dialogs.Interfaces.IAlertMoreThanOneCapellaModelOpenDialogViewModel;
import ViewModels.Interfaces.IViewModel;

/**
 * The {@linkplain AlertMoreThanOneCapellaModelOpenDialogViewModel} is the view model for {@linkplain AlertMoreThanOneCapellaModelOpenDialog}
 */
public class AlertMoreThanOneCapellaModelOpenDialogViewModel implements IViewModel, IAlertMoreThanOneCapellaModelOpenDialogViewModel
{
    /**
     * The {@linkplain ICapellaUserPreferenceService}
     */
    private final ICapellaUserPreferenceService preferenceService;
    
    /**
     * A value indicating whether this dialog should be shown again in the future
     */
    private boolean shouldNeverRemindMe;

    /**
     * Sets the value for {@linkplain #neverRemindMe}
     * 
     * @param shouldNeverRemindMe the new value
     */
    @Override
    public void SetShouldNeverRemindMe(boolean shouldNeverRemindMe)
    {
        this.shouldNeverRemindMe = shouldNeverRemindMe;
    }    

    /**
     * Initializes a new {@linkplain AlertMoreThanOneCapellaModelOpenDialogViewModel}
     * 
     * @param preferenceService the {@linkplain ICapellaUserPreferenceService}
     */
    public AlertMoreThanOneCapellaModelOpenDialogViewModel(ICapellaUserPreferenceService preferenceService)
    {
        this.preferenceService = preferenceService;
    }
    
    /**
     * Saves the {@linkplain #neverRemindMe} preference
     */
    public void SaveShouldNeverRemindMe()
    {
        this.preferenceService.Save(UserPreferenceKey.ShouldNeverRemindMeThatMoreThanOneCapellaModelIsOpen, this.shouldNeverRemindMe);
    }    
}
