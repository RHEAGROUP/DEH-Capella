/*
 * AlertMoreThanOneCapellaModelOpenDialog.java
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
package Views.Dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import Utils.ImageLoader.ImageLoader;
import ViewModels.Dialogs.AlertMoreThanOneCapellaModelOpenDialogViewModel;
import ViewModels.Dialogs.Interfaces.IAlertMoreThanOneCapellaModelOpenDialogViewModel;
import ViewModels.Interfaces.IViewModel;
import Views.Interfaces.IDialog;

/**
 * The {@linkplain AlertMoreThanOneCapellaModelOpenDialog} is the dialog where the user gets notify when more than one Capella model is open
 */
@SuppressWarnings("serial")
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class AlertMoreThanOneCapellaModelOpenDialog extends BaseDialog<Boolean> implements IDialog<IAlertMoreThanOneCapellaModelOpenDialogViewModel, Boolean>
{
    /**
     * This view attached {@linkplain AlertMoreThanOneCapellaModelOpenDialogViewModel} view model
     */
    private transient IAlertMoreThanOneCapellaModelOpenDialogViewModel dataContext;
    
    /**
     * View components declaration
     */
    private JCheckBox neverRemindMeCheckBox;
    private JButton okButton;

    /**
     * Initializes a new {@linkplain AlertMoreThanOneCapellaModelOpenDialog}
     */
    public AlertMoreThanOneCapellaModelOpenDialog() 
    {
        this.Initialize();
    }

    /**
     * Initializes this view components 
     */
    private void Initialize()
    {
        this.setTitle("DEH Capella Adapter warning");
        this.setSize(400, 248);
        this.setLocationRelativeTo(null);
        this.setIconImage(ImageLoader.GetIcon().getImage());
        this.setType(Type.POPUP);
        this.setModal(true);
        
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 150, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
        this.getContentPane().setLayout(gridBagLayout);
        
        JLabel lbltheDehCapella = new JLabel();
        lbltheDehCapella.setText("<html><p style=\"text-align: center;\">The DEH Capella Adapter does not fully support having multiple Capella projects open at the same time.</p>\r\n<p style=\"text-align: center;\">It is recommended to have at most one Capella project open at a time.</p><p style=\"text-align: center;\">&nbsp;</p><p style=\"text-align: center;\">Please leave one Capella project open to avoid any unexpected behavior from the DEH Capella Adapter.</p></html>");
        
        GridBagConstraints gbc_lbltheDehCapella = new GridBagConstraints();
        gbc_lbltheDehCapella.fill = GridBagConstraints.HORIZONTAL;
        gbc_lbltheDehCapella.gridwidth = 2;
        gbc_lbltheDehCapella.insets = new Insets(5, 5, 5, 5);
        gbc_lbltheDehCapella.gridx = 0;
        gbc_lbltheDehCapella.gridy = 0;
        this.getContentPane().add(lbltheDehCapella, gbc_lbltheDehCapella);
        
        this.neverRemindMeCheckBox = new JCheckBox("Never remind me again.");
        GridBagConstraints gbc_neverRemindMeCheckBox = new GridBagConstraints();
        gbc_neverRemindMeCheckBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_neverRemindMeCheckBox.insets = new Insets(5, 5, 5, 5);
        gbc_neverRemindMeCheckBox.gridx = 0;
        gbc_neverRemindMeCheckBox.gridy = 1;
        this.getContentPane().add(this.neverRemindMeCheckBox, gbc_neverRemindMeCheckBox);
        
        this.okButton = new JButton("OK");
        GridBagConstraints gbc_okButton = new GridBagConstraints();
        gbc_okButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_okButton.insets = new Insets(5, 5, 5, 5);
        gbc_okButton.gridx = 1;
        gbc_okButton.gridy = 2;
        this.getContentPane().add(this.okButton, gbc_okButton);
    }
    
    /**
     * Binds the {@linkplain #dataContext} viewModel to this view
     * 
     * @param viewModel the view model to bind
     */
    @Override
    public void Bind()
    {
        this.neverRemindMeCheckBox.addActionListener(x -> this.dataContext.SetShouldNeverRemindMe(this.neverRemindMeCheckBox.isSelected()));
        this.okButton.addActionListener(x -> this.CloseDialog(true));
    }

    /**
     * Closes the dialog and sets the {@link dialogResult}
     * 
     * @param result the {@linkplain TResult} to set
     */
    @Override
    public void CloseDialog(Boolean result)
    {
        this.dataContext.SaveShouldNeverRemindMe();
        super.CloseDialog(result);
    }
    
    /**
     * Sets the DataContext
     */
     @Override
    public void SetDataContext(IAlertMoreThanOneCapellaModelOpenDialogViewModel viewModel)
    {
        this.dataContext = viewModel;
        this.Bind();        
    }

    /**
     * Gets the DataContext
     * 
     * @return an {@link IViewModel}
     */
    @Override
    public IAlertMoreThanOneCapellaModelOpenDialogViewModel GetDataContext()
    {
        return this.dataContext;
    }
}
