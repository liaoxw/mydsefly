package com.myds.efly;

public class Params {
	public enum MODEL{
		NONE(0),
		CITY(1),
		VIEW(2),
		HOTEL_INFO(3),
		HOTEL_IMAGE(4),
		HOTEL_ROOM_STATUS(5),
		HOTEL_ROOM_INFO(6),
		HOTEL_DESC(7);
		
		private final int val;
		
		private MODEL(int i){
			this.val = i;
		}
		public int getValue(){
			return this.val;
		}
	}	
}
