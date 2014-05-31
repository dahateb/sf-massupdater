/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package my.massupdater.listener;


import java.util.HashMap;
import java.util.HashSet;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import my.massupdater.MassUpdateUI;

/**
 *
 * @author dan
 */
public class TableListener implements TableModelListener{
    
    private MassUpdateUI ui;
    private HashMap<Integer,HashSet<Integer>> changedFields;
    
    
    public TableListener(MassUpdateUI ui){
        this.ui = ui;
        changedFields = new HashMap<Integer,HashSet<Integer>>();
    }
    
    @Override
    public void tableChanged(TableModelEvent tme) {
        
        DefaultTableModel model = (DefaultTableModel)tme.getSource();
        
        if(changedFields.get(tme.getFirstRow()) == null){
            changedFields.put(tme.getFirstRow(), new HashSet<Integer>());
        }        
        changedFields.get(tme.getFirstRow()).add(tme.getColumn());
        ui.getErrorField().setText("Value changed for: " + model.getValueAt(tme.getFirstRow(), 0));
        
       // throw new UnsupportedOperationException("Not supported yet. " + model.getValueAt(tme.getFirstRow(), tme.getColumn())); //To change body of generated methods, choose Tools | Templates.
    }
    
    public HashMap<Integer,HashSet<Integer>> getChangedFields(){
        return changedFields;
    }
    
}
