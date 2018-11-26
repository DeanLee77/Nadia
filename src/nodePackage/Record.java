package nodePackage;

public class Record {
	
	private String name;
	private String type;
	private int trueCount;
    private int falseCount;
	 
    public Record()
    {
    		name = "";
    		type = "";
    		trueCount=0;
    		falseCount=0;
    }
    public Record(String name, String type, int trueCount, int falseCount)
    {
    		this.name = name;
    		this.type = type;
        this.trueCount = trueCount;
        this.falseCount = falseCount;
    }
    
    public void setName(String name)
    {
    		this.name = name;
    }
    public String getName()
    {
    		return this.name;
    }
    
    public void setType(String type)
    {
    		this.type = type;
    }
    public String getType()
    {
    		return this.type;
    }
    
    public void setTrueCount(int trueCount)
    {
        this.trueCount = trueCount;
    }
    public void addTrueCount(int trueCount)
    {
        this.trueCount += trueCount;
    }
    public void incrementTrueCount()
    {
        this.trueCount++;
    }
    public int getTrueCount()
    {
        return this.trueCount;
    }
    
    public void setFalseCount(int falseCount)
    {
        this.falseCount = falseCount;
    }
    public void addFalseCount(int falseCount)
    {
        this.falseCount += falseCount;
    }
    public void incrementFalseCount()
    {
        this.falseCount++;
    }
    public int getFalseCount()
    {
        return this.falseCount;
    }

}
