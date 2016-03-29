package ua.tohateam.clipboardsnippy.extendable;

import java.util.*;

public class GroupItem {
	private String Name;
    private ArrayList<ChildItem> Items;
	private boolean Blocked;
	private int RowId;

	public void setRowId(int rowId) {
		RowId = rowId;
	}

	public int getRowId() {
		return RowId;
	}

	public void setBlocked(boolean blocked) {
		Blocked = blocked;
	}

	public boolean isBlocked() {
		return Blocked;
	}

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public ArrayList<ChildItem> getItems() {
        return Items;
    }

    public void setItems(ArrayList<ChildItem> Items) {
        this.Items = Items;
    }
}
