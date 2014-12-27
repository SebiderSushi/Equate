package com.llamacorp.unitcalc;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.llamacorp.unitcalc.UnitCurrency.OnConvertKeyUpdateFinishedListener;

public class UnitType {
	private static final String JSON_NAME = "name";
	private static final String JSON_UNIT_ARRAY = "unit_array";
	private static final String JSON_CURR_POS = "pos";
	private static final String JSON_IS_SELECTED = "selected";

	private String mName;
	private ArrayList<Unit> mUnitArray;
	private int mPrevUnitPos;
	private int mCurrUnitPos;
	private boolean mIsUnitSelected;
	private boolean mContainsDynamicUnits = false;

	/**
	 * Constructor
	 * @param hosting class must implement a function to do raw number conversion
	 */	
	public UnitType(String name){
		mName = name;
		mUnitArray = new ArrayList<Unit>();
		mIsUnitSelected = false;
	}


	public UnitType(JSONObject json) throws JSONException {
		this(json.getString(JSON_NAME));
		mCurrUnitPos = json.getInt(JSON_CURR_POS);
		mIsUnitSelected = json.getBoolean(JSON_IS_SELECTED);

		JSONArray jUnitArray = json.getJSONArray(JSON_UNIT_ARRAY);
		for (int i = 0; i < jUnitArray.length(); i++) {
			mUnitArray.add(Unit.getUnit(jUnitArray.getJSONObject(i)));
		}
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();

		JSONArray jUnitArray = new JSONArray();
		for (Unit unit : mUnitArray)
			jUnitArray.put(unit.toJSON());
		json.put(JSON_UNIT_ARRAY, jUnitArray);

		json.put(JSON_NAME, mName);
		json.put(JSON_CURR_POS, mCurrUnitPos);
		json.put(JSON_IS_SELECTED, mIsUnitSelected);
		return json;
	}


	/**
	 * Used to build a UnitType
	 */
	public void addUnit(Unit u){
		mUnitArray.add(u);
		if(u.isDynamic()) mContainsDynamicUnits = true;
	}

	/** Swap positions of units */	
	public void swapUnits(int pos1, int pos2){
		Collections.swap(mUnitArray, pos1, pos2);
		System.out.println(mUnitArray);
	}

	/**
	 * Find the position of the unit in the unit array
	 * @return -1 if selection failed, otherwise the position of the unit
	 */		
	public int findUnitPosition(Unit unit){
		for(int i=0;i<mUnitArray.size();i++){
			if(unit.equals(mUnitArray.get(i)))
				return i; //found the unit
		}
		return -1;  //if we didn't find the unit
	}


	/**
	 * If mCurrUnit not set, set mCurrUnit
	 * If mCurrUnit already set, call functions to perform a convert
	 */		
	public boolean selectUnit(int pos){
		//used to tell caller if we needed to do a conversion
		boolean requestConvert = false;
		//If we've already selected a unit, do conversion
		if(mIsUnitSelected){
			//if the unit is the same as before, de-select it
			if(mCurrUnitPos==pos){
				mIsUnitSelected=false;
				return requestConvert;
			}
			else {
				mPrevUnitPos = mCurrUnitPos;
				requestConvert = true;
			}
		}

		//Select new unit regardless
		mCurrUnitPos = pos;
		//Engage set flag
		mIsUnitSelected = true;
		return requestConvert;
	}

	/**
	 * Update values of units that are not static (currency) via
	 * each unit's own HTTP/JSON api call. Note that this refresh
	 * is asynchronous and will only happen sometime in the future 
	 * Internet connection permitting.
	 */	
	public void refreshDynamicUnits(Context c){
		if(containsDynamicUnits())
			for(Unit uc : mUnitArray){
				//check to make sure each unit supports updating
				if(uc.isDynamic()){
					UnitCurrency u = ((UnitCurrency) uc);
					//check to see if the update timeout has been reached
					if(u.isTimeoutReached(c))
						u.asyncRefresh(c);
				}
			}
	}

	/**
	 * Check to see if this UnitType holds any units that have values that
	 * need to be refreshed via the Internet
	 */
	public boolean containsDynamicUnits(){
		return mContainsDynamicUnits;
	}

	/** Check to see if unit at position pos is currently updating */
	public boolean isUnitUpdating(int pos){
		if(containsDynamicUnits())
			return ((UnitCurrency)mUnitArray.get(pos)).isUpdating();
		else
			return false;
	}

	/** Check to see if unit at position pos is dynamic */
	public boolean isUnitDynamic(int pos){
		return mUnitArray.get(pos).isDynamic();
	}

	public void setDynamicUnitCallback(OnConvertKeyUpdateFinishedListener callback) {
		if(containsDynamicUnits())
			for(int i=0; i<size(); i++)
				if(mUnitArray.get(i).isDynamic())
					((UnitCurrency)mUnitArray.get(i)).setCallback(callback);
	}

	/**
	 * Resets mIsUnitSelected flag
	 */		
	public void clearUnitSelection(){
		mIsUnitSelected = false;
	}

	public boolean isUnitSelected(){
		return mIsUnitSelected;
	}

	public String getUnitTypeName(){
		return mName;
	}

	/**
	 * @param Index of Unit in the mUnitArray list
	 * @return String name to be displayed on convert button
	 */
	public String getUnitDisplayName(int pos){
		return mUnitArray.get(pos).toString();
	}

	public String getLowercaseLongName(int pos){
		return mUnitArray.get(pos).getLowercaseLongName();
	}

	/** Method builds charSequence array of long names of undisplayed units
	 * @param Array of long names of units not being displayed
	 * @return Number of units being displayed, used to find undisplayed units
	 */
	public CharSequence[] getUndisplayedUnitNames(int numDispUnits){
		//ArrayList<Unit> subList = mUnitArray.subList(numDispUnits, mUnitArray.size());
		//return subList.toArray(new CharSequence[subLists.size()]);
		int arraySize = mUnitArray.size() - numDispUnits;
		CharSequence[] cs = new CharSequence[arraySize];
		for(int i=0;i<arraySize;i++){
			cs[i] = mUnitArray.get(numDispUnits+i).getLongName();
		}
		return cs;
	}

	public Unit getPrevUnit(){
		return mUnitArray.get(mPrevUnitPos);
	}

	public Unit getCurrUnit(){
		return mUnitArray.get(mCurrUnitPos);
	}

	public int size() {
		return mUnitArray.size();
	}

	public int getCurrUnitPos(){
		return mCurrUnitPos;
	}


}