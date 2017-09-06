package nodePackage;

public class Record {
	 private int trueCount;
	    private int falseCount;
	    
	    public Record(int trueCount, int falseCount)
	    {
	        this.trueCount = trueCount;
	        this.falseCount = falseCount;
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
