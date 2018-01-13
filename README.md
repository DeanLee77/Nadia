
# **Nadia** Rule/Inference Engine
Much simpler and easy Rules Engine not only to use but also maintain 'Rules/Policy' for businesses

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

![alt tag](https://user-images.githubusercontent.com/21071046/34905432-7103a7e6-f8ac-11e7-9db7-a33f288e131c.png=250x)Please Note that due to the reason that there is a number of framework for GUI and server side implementation, we will try to implement Nadia engine with respective framework. The first implementation is React.js with Spring Boot, and the source is at [Nadia-R.S](https://github.com/DeanLee77/Nadia-R.S). Please email to 'suho2k12@icloud.com' if you do have any questions.

## 4. Contribution
If you would like to contribute to this project, then please create your own branch and name the branch clearly. Once the work is done in the branch then do 'pull request' and send an email to 'suho2k12@icloud.com'.

## 5. Make your own Rules/Policies
Please have a look at a file of testing rule. Within the example file, all indented rules uses 'Tab' key for indentation. The rule scanner considers of an indented rule as a child rule of previous rule in a rule text.

## 6. How does it work
There are a number of key components as follows;

* Rule reader     : reads a rule/policy file, stream source, string source
* Rule scanner    : scans what 'Rule reader' reads
* Rule parser     : parses what 'Rule scanner' scans into a rule/policy graph
* TopoSort        : sorts the graph parsed by 'Rule parser'
* Inferece Engine : checking all truth or calculated value within forward-chaining, and retrieving next rule/policy within backward-chaining to check in order to complete rule set logic

### How Backward-chaining and Forward-chaining work
Suppose there are following rules:


1. IF B or C is true THEN A is true.
2. IF D and E are true THEN C is true.
3. IF F is true THEN D is false.
4. IF G is false THEN E is true.

#### Backward-chaining:
An inference engine when using backward chaining searches the inference rules until it finds one which has a consequent (Then clause) that matches a desired goal. For instance, if we want to know whether or not the rule statement of 'A is true' is true, an engine finds out which rule has to be checked to conclude. In this case, the engine needs information about the rule statement of 'B is true', or 'F is true' and 'G is false'

#### Forward-chaining
An inference engine using forward chaining searches the inference rules until it finds one where the antecedent (If clause) is known to be true. For instance, if we do have facts that 'G is false' statement is false and 'F is true'statement is true then the engine concludes as follows;
* 'G is false' statement is false
* 'F is true' statement is true
* 'E is true' statement is false
* 'D is false' statement is true
* 'C is true' statement is false
* 'B is true' statement is unknown
* 'A is true' statement is unknown

## 7. License
Copyright (c) 2017 individual contributors.
Nadia is open source project and released under AGPL 3.0 License.
