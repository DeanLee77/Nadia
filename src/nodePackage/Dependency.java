package nodePackage;


public class Dependency {
    private int dependencyType; //this variable is to store 'AND/OR' DependencyType between Nodes
    private Node parent; // this variable is to store a parent Node of this dependency
    private Node child; // this variable is to store a child Node of this dependency
    
//    public Dependency(Node child, String DependencyType)
      public Dependency(Node parent, Node child, int dependencyType)
    {
        this.parent = parent;
        this.child = child;
        this.dependencyType = dependencyType;
    }
    
    public Node getParentNode()
    {
        return parent;
    }
    public void setParentNode(Node parentNode)
    {
    	this.parent = parentNode;
    }
    public Node getChildNode()
    {
        return child;
    }
    
    public int getDependencyType()
    {
        return dependencyType;
    }
    
}
