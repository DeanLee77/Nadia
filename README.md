
# **Nadia** Rule/Inference Engine
Much simpler and easy Rules Engine not only to use but also maintain 'Rules/Policy' for businesses
<br/>
<br/>
Video is also avaiable at [NADIA Policy / Business rules Engine from NExST.R&DLabs](https://youtu.be/xyWjscJ3LxI) <br/>
or <br/>
another link is [ Introduction of NADIA Policy / Business rules Engine from NExST.R&DLabs.](https://youtu.be/O-itMgYHRvc)

# ***Relevant NADIA project list***
[NADIA Java REST](https://github.com/NExST-RnDLabs/NadiaRS) <br/>
[NADIA C sharp](https://github.com/DeanLee77/NADIA-C.Sharp)<br/>
[NADIA Python (In Progress)](https://github.com/DeanLee77/NADIA-Python/)<br/>
[NADIA C/C++ (In Progress)](https://github.com/DeanLee77/NADIA-CPP)


## 1. Introduction
This project is building a Rules(Policies)/Inference Engine with ease of use and maintain rules/policies. It aims to be:

* A rule author is allowed to write rules or policies in a plain text file for the engine rule parser
* A rule author or business person does NOT need to implement the rules/policies separately like other rules engines
* A user of the engine can carry out Foward-chaining and Backward-chaining with a given rule/policy set

## 2. Adding java library
Currently this project uses neither Maven nor Gradle hence you will have to add Jackson library into your project.
You can download following libraries by clicking links.

* [Jackson annotation](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-annotations/2.9.0)
* [Jackson core](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core/2.9.0)
* [Jackson databind](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind/2.9.0)

If you use Maven then add them into your pom.xml, or if your project build is Gradle then add them into your build.gradle file, or if your project is plain web application then put them into WEB-INF.lib folder

## 3. Roadmap
Add more features as follows;

* GUI for Rule IDE (it is just more than editor. working as an development IDE)
* Retrieving Rule/Policy file from database
* Workflow engine with GUI based diagram editor 
* Machine Learning type inference mechanism

* Please Note that due to the reason that there is a number of framework for GUI and server side implementation, we will try to implement Nadia engine with respective framework. The first implementation is React.js with Spring Boot, and the source is at [Nadia-R.S](https://github.com/DeanLee77/Nadia-R.S). Please email to 'nexst.rndlabs@gmail.com' if you do have any questions.

## 4. Contribution
If you would like to contribute to this project, then please create your own branch and name the branch clearly. Once the work is done in the branch then do 'pull request' and send an email to 'nexst.rndlabs@gmail.com'.

## 5. Make your own Rules/Policies
Please have a look at a file of testing rule. Within the example file, all indented rules uses 'Tab' key for indentation. The rule scanner considers of an indented rule as a child rule of previous rule in a rule text.

## Note:
If you need a commercial or coustomised version of Nadia engine, then please contact on 'nexst.rndlabs@gmail.com'.

## 6. How does it work
There are a number of key components as follows;

* Rule reader     : reads a rule/policy file, stream source, string source
* Rule scanner    : scans what 'Rule reader' reads
* Rule parser     : parses what 'Rule scanner' scans into a rule/policy graph
* TopoSort        : sorts the graph parsed by 'Rule parser'
* Inferece Engine : checking all truth or calculated value within forward-chaining, and retrieving next rule/policy within backward-chaining to check in order to complete rule set logic

### How Backward-chaining and Forward-chaining work
Suppose there are following rules:


1. IF either <br/>
      &nbsp;&nbsp;&nbsp;&nbsp;'statement B' is true; or <br/>
      &nbsp;&nbsp;&nbsp;&nbsp;'statement C' is true <br/>
   THEN <br/>
      &nbsp;&nbsp;&nbsp;&nbsp;'statement A' is true.
2. IF  both<br/>
      &nbsp;&nbsp;&nbsp;&nbsp;'statement D' is true; and <br/>
      &nbsp;&nbsp;&nbsp;&nbsp;'statement E' is true <br/>
   THEN <br/>
      &nbsp;&nbsp;&nbsp;&nbsp;'statement C' is true.
3. IF <br/>
      &nbsp;&nbsp;&nbsp;&nbsp;'statement F' is true<br/> 
   THEN <br/>
      &nbsp;&nbsp;&nbsp;&nbsp;'statement D' is false.
4. IF<br/> 
     &nbsp;&nbsp;&nbsp;&nbsp; 'statement G' is false <br/>
   THEN <br/>
      &nbsp;&nbsp;&nbsp;&nbsp;'statement E' is true.

#### Backward-chaining:
An inference engine when using backward chaining searches the inference rules until it finds one which has a consequent (Then clause) that matches a desired goal. For instance, if we want to know whether or not the rule of 'statement A' is 'true' or 'not true(false)', an engine finds out which rule has to be checked to conclude. In this case, the engine needs information about the rule of 'statement B' is 'true' or 'not true(false)', or 'statement F' and 'statement G' are 'true' or 'not true(false)' respectively.

#### Forward-chaining
An inference engine using forward chaining searches the inference rules until it finds one where the antecedent (If clause) is known to be true. For instance, if we do have facts that 'statement G' is true and 'statement F' is true then the engine concludes as follows;
* 'statement G' is true
* 'statement F' is true
* 'statement E' is false due to that 'statement G' is not false
* 'statement D' is false due to that 'statement F' is true
* 'statement C' is false due to that 'statement D' is true and 'statement E' is false
* 'statement B' is unknown due to that there is not given information to infer about 'statement B'
* 'statement A' is false with given information of 'statement B' and 'statement C', however it could be changed based on conclusion  of 'statement B' because 'statement B' is unknown.

## 7. License
Copyright (c) 2017-2020 individual contributors.
Nadia is open source project and released under AGPL 3.0 License.
