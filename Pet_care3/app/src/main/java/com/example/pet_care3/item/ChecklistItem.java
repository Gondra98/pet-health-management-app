package com.example.pet_care3.item;

public class ChecklistItem {

    private String name;
    private boolean checked;

    public ChecklistItem(String name, boolean checked) {
        this.name = name;
        this.checked = checked;
    }

    public String getName() {
        return name;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
