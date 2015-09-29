package io.github.ebaldino.sssshot;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JsonFormatter{

    public static String format(final JSONObject object) {
        final JsonVisitor visitor = new JsonVisitor(2, ' ');
        visitor.visit(object, 1);
        return visitor.toString();
    }

    private static class JsonVisitor{

        private StringBuilder builder = new StringBuilder();
        private int indentationSize;
        private char indentationChar;
        private Boolean blockended = false;
        private Boolean blockopened = false;  
        private String prevtype = "";
        private Integer structurelevel = 0;
		private String nl = System.getProperty("line.separator");
		
        public JsonVisitor(int indentationSize, char indentationChar){
            this.indentationSize = indentationSize;
            this.indentationChar = indentationChar;
        }

        private void visit(final JSONArray array, final int indent) {
            final int length = array.size();
            if(length == 0){
                write("[]", 0, "openclose");
            } else{
            	if (!blockended && !blockopened) {
            		write("[", 0, "openarr");
            	} else {
            		write("[", indent, "openarr");
            	}           	
                for(int i = 0; i < length; i++){
                    visit(array.get(i), indent + 1);
                }
                write("]", indent, "closearr");
            }

        }

        private void visit(final JSONObject obj, final int indent) {
            final int length = obj.size();
            if(length == 0){
                write("{}", 0, "openclose");                
            } else { 
            	if (!blockended && !blockopened) {
            		write("{", 0, "openobj");
            	} else {
            		write("{", indent, "openobj");
            	}            	
            	
                for (Object key : obj.keySet()) {

                	// Write the key into the string
                    write("\"" + key + "\": ", indent, "key");   
                    
                    // recurse the value
                    structurelevel++;
                    visit(obj.get(key), indent + 1);                    
                    structurelevel--;
                }
                
            	write("}", indent, "closeobj");  
            }

        }

        private void visit(final Object object, final int indent) {
            if(object instanceof JSONArray){
                visit((JSONArray) object, indent);
            } else if(object instanceof JSONObject){
                visit((JSONObject) object, indent);
            } else{
                if(object instanceof String){
                    write("\"" + (String) object + "\"", 0, "value");                	
                } else{
                    write(String.valueOf(object), 0, "value");
                }
            }

        }

        private void write(final String data, final int indent, String type){
           		
        	if (type.equals("key")) {
        		if (blockopened) {
            		builder.append(nl).append(indentStr(indent)).append(data);
        		} else {
            		builder.append("," + nl).append(indentStr(indent)).append(data);
        		}
        	}
        	
        	if (type.equals("value")) {
        		if (prevtype.equals("value")) {
            		builder.append("," + nl).append(indentStr(indent)).append(data);
        		} else {
        			builder.append(data);
        		}
        	}
        	
        	if (type.equals("openobj") || type.equals("openarr")) {
        		if (blockended) {
            		builder.append("," + nl).append(indentStr(indent)).append(data);            		
            	} else {
            		if (blockopened) {
            			builder.append(nl).append(indentStr(indent)).append(data);
            		} else {
            			builder.append(indentStr(indent)).append(data);
            		}
        		}
        	}        	       	
        	if (type.equals("closeobj")) {
        		builder.append(nl).append(indentStr(indent)).append(data);
        	}
        	if (type.equals("closearr")) {
        		builder.append(nl).append(indentStr(indent)).append(data);
        	}
        	        	
        	if (type.equals("openclose")) builder.append(data);
        	
        	
            blockended  = data.contentEquals("}") || data.contentEquals("]") || data.contentEquals("{}") || data.contentEquals("[]");
            blockopened = data.contentEquals("{") || data.contentEquals("[");
            prevtype = type;
        }

        private String indentStr(final int indent) {
        	String indentstring = "";
        	for(int i = 0; i < (indent * indentationSize); i++){
        		indentstring = indentstring + indentationChar;
        	}
        	return indentstring;
        }
        
        @Override
        public String toString(){
            return builder.toString();
        }

    }

}