package com.example.eposreceiptprintsample;

public class SpnModelsItem {
    private String modelName = "";
    private int modelConstant = 0;

    SpnModelsItem(String modelName, int modelConstant) {
        this.modelName = modelName;
        this.modelConstant = modelConstant;
    }

    public int getModelConstant() {
        return modelConstant;
    }

    @Override
    public String toString() {
        return modelName;
    }

}
