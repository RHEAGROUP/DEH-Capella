/*
 * CapellaArchitectureCellEditor.java
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
package Renderers;

import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.UIManager;

import Enumerations.CapellaArchitecture;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.engineeringmodeldata.ElementDefinition;

/**
 * The {@linkplain CapellaArchitectureCellEditor} is the
 * {@linkplain TableCellEditor} to enable editing
 */
@SuppressWarnings("serial")
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class CapellaArchitectureCellEditor extends DefaultCellEditor
{
    /**
     * Overrides the loosely typed {@linkplain #editorComponent}
     */
    private JComboBox<CapellaArchitecture> comboBox;

    /**
     * Initializes a {@linkplain CapellaArchitectureCellEditor}
     * 
     * @param comboBox the editor component used
     */
    @SuppressWarnings("unchecked")
    public CapellaArchitectureCellEditor()
    {
        super(new JComboBox<CapellaArchitecture>());
        this.comboBox = (JComboBox<CapellaArchitecture>)this.editorComponent;
        this.comboBox.setOpaque(true);
        this.comboBox.addActionListener(x -> fireEditingStopped());
    }

    /**
     * Sets an initial <code>value</code> for the editor. This will cause the editor
     * to <code>stopEditing</code> and lose any partially edited value if the editor
     * is editing when this method is called.
     * <p>
     *
     * @param table      the <code>JTable</code> that is asking the editor to edit;
     *                   can be <code>null</code>
     * @param value      the value of the cell to be edited; it is up to the
     *                   specific editor to interpret and draw the value.
     * @param isSelected true if the cell is to be rendered with highlighting
     * @param row        the row of the cell being edited
     * @param column     the column of the cell being edited
     * @return the component for editing
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        this.comboBox.setForeground(Color.black);
        this.comboBox.setBackground(UIManager.getColor("Button.background"));
        this.comboBox.setSelectedItem(value);
        var rowViewModel = table.getModel().getValueAt(row, 0);
        
        if(rowViewModel instanceof MappedElementRowViewModel && ((MappedElementRowViewModel<?,?>)rowViewModel).GetTThingClass() == ElementDefinition.class)
        {
            CapellaArchitecture[] possibleArchitectures = { CapellaArchitecture.PhysicalArchitecture, CapellaArchitecture.LogicalArchitecture};
            this.comboBox.setModel(new DefaultComboBoxModel<CapellaArchitecture>(possibleArchitectures));
        }
        else
        {
            this.comboBox.setModel(new DefaultComboBoxModel<CapellaArchitecture>(CapellaArchitecture.values()));
        }
        
        return this.comboBox;
    }

    /**
     * Gets the selected {@linkplain CapellaArchitecture}
     * 
     * @return a {@linkplain CapellaArchitecture}
     */
    @Override
    public Object getCellEditorValue()
    {
        return this.comboBox.getSelectedItem();
    }
}
