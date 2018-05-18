package teleABM;

import java.awt.Color;

public enum  LandUse {
	
		
		   SOY (0xFFCC00) { //yellow    //1
			public String toString() {
				return "soy";
			}
		}, CORN (0xFF9900) {//orange    //5
			public String toString() {
				return "corn";
			}
		}, GRASSLAND(0x33FF00) {
			public String toString() {
				return "grassland";
			}
		}, FOREST (0x154229) {    //4
			public String toString() {
				return "forest";
			}
		}, RICE(0xFFFFFF) { //white    //2
			public String toString() {
				return "rice";
			}
		}, OTHERCROPS(0xCCFF00) {  //lime    //3 OTHER CROPS
			public String toString(){
				return "otherCrops";
			}
		},  NEWCLEARED (0xFF9900) { //clay
				public String toString() {
					return "newCleared";
				}
			}, FALLOW (0xFF1099	) { //pink
				public String toString() {
					return "fallow";
				}
			}, COTTON(0xFF9910){			
				public String toString(){
					return "cotton";
				}
			}, BUILDING(0xFF9810) {
				public String toString(){
					return "building";
				}
			}, WATER (0x0000FF) {
				public String toString(){
					return "water";
				}
			}, SECONDSOY (0xFFCC00) {
				public String toString(){
					return "secondsoy";
				}
			}, SOYCOTTON(0xFFCC05){
				public String toString(){
					return "soycotton";
					}
			}
			
			
		   ;
		

		
	    private final Color color;
		
		private LandUse(int rgb) {
			color = new Color(rgb);
		}
		
		private LandUse(Color color) {
			this.color = color;
		}
		
		public Color getColor() {
			return color;
		}
	}


