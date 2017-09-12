package factValuePackage;

import java.time.LocalDate;
import java.util.List;



public abstract class FactValue {


	public static FactDefiStringValue parseDefiString(String s) {
		return new FactDefiStringValue(s);
	}
	
	public static FactStringValue parse(String s) {
		return new FactStringValue(s);
	}
	
	public static FactIntegerValue parse(int i) {
		return new FactIntegerValue(i);
	}
	
	public static FactDateValue parse(LocalDate cal) {
		return new FactDateValue(cal);
	}
	
	public static FactDoubleValue parse(double d) {
		return new FactDoubleValue(d);
	}
	
	public static FactBooleanValue<?> parse(boolean b)
	{
		return new FactBooleanValue(b);
	}
	
	public static FactListValue<?> parse(List<FactValue> l)
	{
		return new FactListValue(l);
	}
	
	public static FactURLValue parseURL(String url)
	{
		return new FactURLValue(url);
	}
	
	public static FactHashValue parseHash(String hash)
	{
		return new FactHashValue(hash);
	}
	
	public static FactUUIDValue parseUUID(String uuid)
	{
		return new FactUUIDValue(uuid);
	}

	
	public abstract FactValueType getType() ;
	public abstract <T> void setDefaultValue(T str);
	public abstract <T> T getValue();
	public abstract <T> T getDefaultValue();
	
}
