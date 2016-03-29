package ua.tohateam.clipboardsnippy.extendable;

public class ChildItem {
	private String Name;
	private int RowId;

	public void setRowId(int rowId) {
		RowId = rowId;
	}

	public int getRowId() {
		return RowId;
	}

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }
}
