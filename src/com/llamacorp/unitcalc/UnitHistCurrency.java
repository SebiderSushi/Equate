package com.llamacorp.unitcalc;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class UnitHistCurrency extends Unit {
	private static String JSON_URL_RATE_TAG = "Rate";
	
	private String mNameSuffix;
	private String mLongNameSuffix;
	
	private int mYearIndex = 0;
	private int mIndexStartYearOffset;
	private ArrayList<Double> mHistoricalValues;

	public UnitHistCurrency(String name, String longName, ArrayList<Double> values,
			int indexStartYear, int defaultStartYear){
		mNameSuffix = name;
		mLongNameSuffix = longName;
		mHistoricalValues = values;
		mIndexStartYearOffset = indexStartYear;
		if(defaultStartYear - indexStartYear < values.size())
			mYearIndex = defaultStartYear - indexStartYear;
		setNewYear(mYearIndex);
	}	

	/** Load in the update time */
	public UnitHistCurrency(JSONObject json) throws JSONException {
		super(json);
	}

	/** Save the update time */
	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		return json;
	}
	
	@Override
	public String convertTo(Unit toUnit, String expressionToConv) {
		return expressionToConv + "*" + toUnit.getValue() + "/" + getValue();
	}

	public void setNewYear(int index){
		mYearIndex = index;
		setValue(mHistoricalValues.get(mYearIndex));
		refreshNames();
	}
	
	private void refreshNames(){
		String sYear = String.valueOf(getSelectedYear());
		sYear = sYear + " ";
		setDispName(sYear + mNameSuffix);
		setLongName(sYear + mLongNameSuffix);		
	}
	
	private int getSelectedYear(){
		return mYearIndex + mIndexStartYearOffset;
	}
}

